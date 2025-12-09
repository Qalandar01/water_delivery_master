package uz.pdp.water_delivery.bot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.EditMessageCaption;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.water_delivery.bot.BotConstant;
import uz.pdp.water_delivery.bot.BotUtils;
import uz.pdp.water_delivery.bot.TelegramUser;
import uz.pdp.water_delivery.model.entity.Basket;
import uz.pdp.water_delivery.model.entity.Product;
import uz.pdp.water_delivery.model.enums.TelegramState;
import uz.pdp.water_delivery.model.repo.BasketRepository;
import uz.pdp.water_delivery.model.repo.ProductRepository;
import uz.pdp.water_delivery.model.repo.TelegramUserRepository;
import uz.pdp.water_delivery.services.DeleteMessageService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BasketBotService {

    private final BasketRepository basketRepository;
    private final BotNavigationService botNavigationService;
    private final TelegramUserRepository telegramUserRepository;
    private final TelegramBot telegramBot;
    private final BotUtils botUtils;
    private final DeleteMessageService deleteMessageService;
    private final ProductRepository productRepository;
    private final BotService botService;
    private final OrderBotService orderBotService;


    public void acceptProductShowSelectNumber(Message message, TelegramUser telegramUser) {
        String type = message.text();
        if (handlePredefinedActions(type, telegramUser)) return;
        Product product = productRepository.findByType(type)
                .orElseThrow(() -> new RuntimeException("Bad Product type"));

        updateTelegramUserProduct(telegramUser, product);

        updateProductFromBasket(telegramUser);

        botService.sendProductMessage(telegramUser, product);

    }

    private boolean handlePredefinedActions(String type, TelegramUser telegramUser) {
        if (type.equals(BotConstant.CANCEL)) {
            deleteMessageService.deleteMessageAll(telegramBot, telegramUser);
            botNavigationService.sendCabinet(telegramUser);
            return true;
        } else if (type.equals(BotConstant.BASKET)) {
            showBasket(telegramUser);
            return true;
        } else if (type.equals(BotConstant.BACK)) {
            botNavigationService.sendCabinet(telegramUser);
            return true;
        }
        return false;
    }

    private void updateTelegramUserProduct(TelegramUser telegramUser, Product product) {
        telegramUser.setProduct(product);
        telegramUserRepository.save(telegramUser);
    }

    private void updateProductFromBasket(TelegramUser telegramUser) {
        Basket basket = basketRepository.findByTelegramUserAndProduct(
                telegramUser,
                telegramUser.getProduct()
        );
        if (basket != null) {
            telegramUser.setProductCount(basket.getAmount());
            telegramUserRepository.save(telegramUser);
        }
    }

    @Transactional
    public String generatedTextForProduct(Product product, TelegramUser telegramUser) {
        double price = product.getPrice();
        double totalPrice = telegramUser.getProductCount() * price;

        return String.format(
                "🥤 Bakalashka turi: %s\n" +
                        "🔢 Soni: %d ta\n" +
                        "💲 Har birining narxi: %.2f so'm\n" +
                        "💰 Jami narxi: %.2f so'm\n",
                product.getType(),
                telegramUser.getProductCount(),
                price,
                totalPrice
        );
    }

    public void changeProductNumber(CallbackQuery message, TelegramUser telegramUser) {
        String data = message.data();
        switch (data) {
            case BotConstant.ADD_TO_BASKET -> {
                processAddToBasket(telegramUser);
                telegramUser.setProductCount(1);
                telegramUserRepository.save(telegramUser);
                SendMessage sendMessage = new SendMessage(
                        telegramUser.getChatId(),
                        "✅ Savatchaga qo'shildi!"
                );
                SendResponse sendResponse = telegramBot.execute(sendMessage);
                Integer messageId = sendResponse.message().messageId();
                deleteMessageService.archivedForDeletingMessages(telegramUser, messageId, "Savatchaga qo'shildi!");
                orderBotService.startOrderingFunction(telegramUser);
                return;
            }
            case BotConstant.CANCEL_BTN -> {
                resetUserState(telegramUser);
                return;
            }
            case BotConstant.PLUS -> increaseProductCount(telegramUser);
            case BotConstant.MINUS -> decreaseProductCount(telegramUser);
            default -> throw new IllegalArgumentException("Unknown action: ");
        }

        telegramUserRepository.save(telegramUser);
        updateMessageWithProductInfo(telegramUser);
    }


    private void resetUserState(TelegramUser telegramUser) {
        telegramUser.setProductCount(1);
        telegramUser.setState(TelegramState.CABINET);
        telegramUserRepository.save(telegramUser);
        botNavigationService.sendCabinet(telegramUser);
    }

    private void increaseProductCount(TelegramUser telegramUser) {
        telegramUser.setProductCount(telegramUser.getProductCount() + 1);
    }

    private void decreaseProductCount(TelegramUser telegramUser) {
        if (telegramUser.getProductCount() > 1) {
            telegramUser.setProductCount(telegramUser.getProductCount() - 1);
        }
    }

    private void updateMessageWithProductInfo(TelegramUser telegramUser) {
        String updatedText = generatedTextForProduct(telegramUser.getProduct(), telegramUser);
        EditMessageCaption editMessageText = new EditMessageCaption(
                telegramUser.getChatId(),
                telegramUser.getEditingMessageId());
        editMessageText.caption(updatedText);
        editMessageText.replyMarkup(botUtils.generateProductNumberButtons(telegramUser));
        telegramBot.execute(editMessageText);
    }


    private void processAddToBasket(TelegramUser telegramUser) {
        Product product = telegramUser.getProduct();

        Basket existingBasket = basketRepository.findByTelegramUserAndProduct(telegramUser, product);
        if (existingBasket != null) {
            existingBasket.setAmount(telegramUser.getProductCount());
            basketRepository.save(existingBasket);
        } else {
            Basket newBasket = Basket.builder()
                    .telegramUser(telegramUser)
                    .amount(telegramUser.getProductCount())
                    .product(product)
                    .build();
            basketRepository.save(newBasket);
        }
    }

    public String generateBasketInfo(Basket basket, int remainingAmount, double discountedPrice) {
        StringBuilder result = new StringBuilder();
        result.append("""
                <b>🔖 Mahsulot nomi:</b> %s
                <b>🔢 Soni:</b> %d ta
                <b>💵 Narxi:</b> %d sum
                <b>💰 Jami narxi:</b> %d sum
                """.formatted(
                basket.getProduct().getType(),
                basket.getAmount(),
                basket.getProduct().getPrice(),
                basket.getProduct().getPrice() * basket.getAmount()
        ));

        if (basket.getProduct().getSale_active() && basket.getAmount() >= basket.getProduct().getSale_discount()) {
            result.append("""
                    <b>🎁 Sovg'a miqdori:</b> %d ta
                    <b>💰 Chegirma bilan qolgan narx:</b> %s sum
                    """.formatted(
                    basket.getAmount() - remainingAmount,
                    String.format("%.2f", discountedPrice)
            ));
        }

        return result.toString();
    }

    public void showBasket(Message message, TelegramUser telegramUser) {
        deleteMessageService.deleteMessageAll(telegramBot, telegramUser);
        deleteMessageService.archivedForDeletingMessages(telegramUser, message.messageId(), "Show basket");
        showMenuBasket(telegramUser);
    }

    public void showBasket(TelegramUser telegramUser) {
        deleteMessageService.deleteMessageBasket(telegramBot, telegramUser);
        showMenuBasket(telegramUser);
    }

    private void showMenuBasket(TelegramUser telegramUser) {
        List<Basket> baskets = basketRepository.findAllByTelegramUser(telegramUser);
        if (baskets.isEmpty()) {
            botService.sendMessage(telegramUser, BotConstant.NO_BASKET);
            telegramUser.setState(TelegramState.CABINET);
            telegramUserRepository.save(telegramUser);
            botNavigationService.sendCabinet(telegramUser);
            return;
        }
        int totalPrice = 0;

        for (Basket basket : baskets) {
            StringBuilder messageBuilder = new StringBuilder();
            int remainingAmount = basket.getAmount();
            Long finalTotalPrice = basket.getProduct().getPrice() * basket.getAmount();

            messageBuilder.append("🛒 Mahsulot: ")
                    .append("<b>").append(basket.getProduct().getType()).append("</b>\n")
                    .append("🔢 Soni: ")
                    .append("<b>").append(basket.getAmount()).append(" ta</b>\n")
                    .append("💵 Narxi: ")
                    .append("<b>").append(basket.getProduct().getPrice()).append(" so'm</b>\n")
                    .append("💰 Jami narx: ")
                    .append("<b>").append(finalTotalPrice).append(" so'm</b>\n\n");

            if (basket.getProduct().getSale_active() && basket.getAmount() >= basket.getProduct().getSale_discount()) {
                int giftAmount = basket.getProduct().getSale_amount();
                Long giftPrice = giftAmount * basket.getProduct().getPrice();

                finalTotalPrice -= giftPrice;

                messageBuilder.append("🎁 Sovg'a miqdori: ")
                        .append("<b>").append(giftAmount).append(" ta</b>\n")
                        .append("🎁 Chegirma : - ")
                        .append("<b>").append(giftPrice).append(" so'm</b>\n")
                        .append("💰 Chegirmada to'lanadigan narx: ")
                        .append("<b>").append(finalTotalPrice).append(" so'm</b>\n\n");
            }

            totalPrice += finalTotalPrice;

            SendMessage sendMessage = new SendMessage(telegramUser.getChatId(), messageBuilder.toString());
            sendMessage.replyMarkup(botUtils.getBasketButton(basket));
            sendMessage.parseMode(ParseMode.HTML);

            SendResponse sendResponse = telegramBot.execute(sendMessage);
            Integer messageId = sendResponse.message().messageId();

            basket.setMessageId(messageId);
            basketRepository.save(basket);

            deleteMessageService.archivedForDeletingMessages(telegramUser, messageId, messageBuilder.toString());
            telegramUser.setState(TelegramState.MY_BASKET);
            telegramUserRepository.save(telegramUser);
        }

        String totalMessageBuilder = "📊 Umumiy summa: " +
                "<b>" + totalPrice + " so'm</b>\n";

        SendMessage finalMessage = new SendMessage(telegramUser.getChatId(), totalMessageBuilder + "\n✅ ");
        finalMessage.parseMode(ParseMode.HTML);
        finalMessage.replyMarkup(botUtils.getCreateOrClearOrderButton());
        SendResponse finalResponse = telegramBot.execute(finalMessage);

        telegramUser.setState(TelegramState.MY_BASKET);
        Integer finalMessageId = finalResponse.message().messageId();
        deleteMessageService.archivedForDeletingMessages(telegramUser, finalMessageId, "Buyurtmani tasdiqlash tugmasi");
    }

    private void updateBasketMessage(TelegramUser telegramUser, Basket basket, Integer messageId) {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("🛒 Mahsulot: ")
                .append("<b>").append(basket.getProduct().getType()).append("</b>\n")
                .append("🔢 Soni: ")
                .append("<b>").append(basket.getAmount()).append(" ta</b>\n")
                .append("💵 Narxi: ")
                .append("<b>").append(basket.getProduct().getPrice()).append(" so'm</b>\n")
                .append("💰 Jami: ")
                .append("<b>").append(String.format("%.2f", basket.getTotalPrice())).append(" so'm</b>\n\n");

        EditMessageText editMessageText = new EditMessageText(
                telegramUser.getChatId(),
                messageId,
                messageBuilder.toString()
        );
        editMessageText.parseMode(ParseMode.HTML);
        editMessageText.replyMarkup(botUtils.getBasketButton(basket));

        telegramBot.execute(editMessageText);
    }

    public void deleteBasket(String data, TelegramUser telegramUser) {
        String[] splitData = data.contains("_") ? data.split("_") : new String[]{data};
        String basketId = splitData.length > 1 ? splitData[1] : null;
        if (basketId != null) {
            Optional<Basket> basketOptional = basketRepository.findById(Long.valueOf(basketId));
            basketOptional.ifPresent(basket -> {
                basketRepository.delete(basket);
                showBasket(telegramUser);
            });
        }

    }
    public void removeBasketProduct(TelegramUser telegramUser, String data) {
        long basketId = Long.parseLong(data.split("_")[1]);
        basketRepository.deleteById(basketId);
        deleteMessageService.deleteMessageAll(telegramBot, telegramUser);
        showBasket(telegramUser);
    }
}
