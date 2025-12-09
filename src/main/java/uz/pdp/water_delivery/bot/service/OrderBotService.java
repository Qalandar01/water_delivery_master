package uz.pdp.water_delivery.bot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.water_delivery.bot.BotConstant;
import uz.pdp.water_delivery.bot.TelegramUser;
import uz.pdp.water_delivery.model.entity.Basket;
import uz.pdp.water_delivery.model.entity.Order;
import uz.pdp.water_delivery.model.entity.OrderProduct;
import uz.pdp.water_delivery.model.entity.Product;
import uz.pdp.water_delivery.model.enums.OrderStatus;
import uz.pdp.water_delivery.model.enums.TelegramState;
import uz.pdp.water_delivery.model.repo.*;
import uz.pdp.water_delivery.services.DeleteMessageService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderBotService {

    private final BotNavigationService navigationService;
    private final DeleteMessageService deleteMessageService;
    private final ProductRepository productRepository;
    private final TelegramBot telegramBot;
    private final TelegramUserRepository telegramUserRepository;
    private final BotService botService;
    private final OrderRepository orderRepository;
    private final BasketRepository basketRepository;
    private final OrderProductRepository orderProductRepository;

    public void startOrdering(Message message, TelegramUser telegramUser) {
        String text = message.text();
        if (text.equals(uz.pdp.water_delivery.bot.BotConstant.ORDER_BTN)) {
            deleteMessageService.archivedForDeletingMessages(telegramUser, message.messageId(), uz.pdp.water_delivery.bot.BotConstant.ORDER_BTN);
            startOrderingFunction(telegramUser);
        }
    }

    public void startOrderingFunction(TelegramUser telegramUser) {
        List<Product> products = productRepository.findAll();
        if (products.isEmpty()) {
            SendMessage sendMessage = new SendMessage(
                    telegramUser.getChatId(),
                    uz.pdp.water_delivery.bot.BotConstant.NO_BOTTLE_TYPE
            );
            SendResponse sendResponse = telegramBot.execute(sendMessage);
            Integer messageId = sendResponse.message().messageId();
            deleteMessageService.archivedForDeletingMessages(telegramUser, messageId, "Suv mavjud emas!");
            telegramUser.setState(TelegramState.CABINET);
            telegramUserRepository.save(telegramUser);
            navigationService.sendCabinet(telegramUser);
        } else {
            SendMessage sendMessage = botService.createProductPhoto(telegramUser);
            SendResponse sendResponse = telegramBot.execute(sendMessage);
            Integer messageId = sendResponse.message().messageId();
            deleteMessageService.archivedForDeletingMessages(telegramUser, messageId, "Suv mavjud");
            telegramUser.setState(TelegramState.SELECT_BOTTLE_TYPE);
            telegramUserRepository.save(telegramUser);
            return;
        }
    }


    @Transactional
    public void makeAnOrder(CallbackQuery callbackQuery, TelegramUser tgUser) {
        if (callbackQuery.data().equals(BotConstant.CANCEL) && tgUser.getState().equals(TelegramState.CREATE_ORDER)) {
            tgUser.setProductCount(1);
            tgUser.setState(TelegramState.CABINET);
            telegramUserRepository.save(tgUser);
            navigationService.sendCabinet(tgUser);
        } else if (callbackQuery.data().equals(BotConstant.CONFIRM_ORDER)) {
            Order order = Order.builder()
                    .id(tgUser.generateOrderId())
                    .location(tgUser.getLocation())
                    .region(tgUser.getRegion())
                    .district(tgUser.getDistrict())
                    .telegramUser(tgUser)
                    .isDeleted(false)
                    .orderStatus(OrderStatus.CREATED)
                    .phone(tgUser.getUser().getPhone())
                    .build();
            orderRepository.save(order);
            List<Basket> baskets = basketRepository.findAllByTelegramUser(tgUser);
            for (Basket basket : baskets) {
                OrderProduct orderProduct = OrderProduct.builder()
                        .order(order)
                        .amount(basket.getAmount())
                        .product(basket.getProduct())
                        .priceAtPurchase(basket.getProduct().getPrice())
                        .discountAtPurchase(basket.getProduct().getSale_active() ? basket.getProduct().getSale_amount() : null)
                        .wasOnSale(basket.getProduct().getSale_active())
                        .build();
                orderProductRepository.save(orderProduct);
            }
            basketRepository.deleteAllByTelegramUser(tgUser);
            SendMessage message = new SendMessage(tgUser.getChatId(), BotConstant.ORDER_FINISH_MSG);
            SendResponse sendResponse = telegramBot.execute(message);
            Integer messageId = sendResponse.message().messageId();
            deleteMessageService.archivedForDeletingMessages(tgUser, messageId, BotConstant.ORDER_FINISH_MSG);
            tgUser.setProductCount(1);
            List<Order> orders = orderRepository.findByTelegramUserAndOrderStatus(tgUser, OrderStatus.COMPLETED);
            tgUser.getUser().setNewUser(orders.isEmpty());
            tgUser.setState(TelegramState.HAS_ORDER);
            telegramUserRepository.save(tgUser);
        }
    }
    public void sendMyOrders(Message message, TelegramUser telegramUser) {
        deleteMessageService.archivedForDeletingMessages(telegramUser, message.messageId(), "Buyurtma mahsulotlari");

        List<Order> orders = orderRepository.findAllByTelegramUser(telegramUser);

        if (orders.isEmpty()) {
            botService.sendMessage(telegramUser, BotConstant.NO_ORDERS);
            return;
        }

        botService.sendMessage(telegramUser, "📋 Buyurtmalarim:\n\n");

        for (Order order : orders) {
            List<OrderProduct> orderProducts = orderProductRepository.findAllByOrder(order);

            for (OrderProduct orderProduct : orderProducts) {

                StringBuilder messageBuilder = new StringBuilder();
                messageBuilder.append("🆔 Buyurtma raqami: <b>").append(order.getId()).append("</b>\n")
                        .append("🍼 Mahsulot turi: <b>").append(orderProduct.getProduct().getType()).append("</b>\n")
                        .append("🔢 Soni: <b>").append(orderProduct.getAmount()).append(" ta</b>\n");

                if (orderProduct.isOnSale()) {
                    Long discountPrice = orderProduct.getProduct().getPrice() * orderProduct.getProduct().getSale_amount();
                    messageBuilder.append("🔒 Chegirma: <b>").append(orderProduct.getProduct().getSale_amount()).append(" ta</b>\n");
                    messageBuilder.append("💰 Chegirma narxi: <b>").append(discountPrice).append(" so'm</b>\n");
                } else {
                    messageBuilder.append("💵 Oddiy narxi: <b>").append(orderProduct.getProduct().getPrice()).append(" so'm</b>\n");
                }

                messageBuilder.append("💵 Umumiy narxi: <b>").append(orderProduct.getTotalPrice()).append(" so'm</b>\n\n");

                SendMessage sendMessage = new SendMessage(telegramUser.getChatId(), messageBuilder.toString());
                sendMessage.parseMode(ParseMode.HTML);
                SendResponse sendResponse = telegramBot.execute(sendMessage);
                Integer messageId = sendResponse.message().messageId();
                deleteMessageService.archivedForDeletingMessages(telegramUser, messageId, messageBuilder.toString());
            }
        }
    }

}
