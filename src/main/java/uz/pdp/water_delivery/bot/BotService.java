package uz.pdp.water_delivery.bot;


import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.model.request.ReplyKeyboardRemove;
import com.pengrad.telegrambot.request.EditMessageCaption;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.water_delivery.bot.delivery.BotDelivery;
import uz.pdp.water_delivery.bot.handlers.state.TelegramStateDispatcher;
import uz.pdp.water_delivery.dto.Location;
import uz.pdp.water_delivery.entity.*;
import uz.pdp.water_delivery.entity.enums.OrderStatus;
import uz.pdp.water_delivery.entity.enums.RoleName;
import uz.pdp.water_delivery.entity.enums.TelegramState;
import uz.pdp.water_delivery.repo.*;
import uz.pdp.water_delivery.services.service.DeleteMessageService;
import uz.pdp.water_delivery.services.service.UserService;
import uz.pdp.water_delivery.utils.PhoneRepairUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BotService  {

    private final TelegramBot telegramBot;
    private final TelegramUserRepository telegramUserRepository;
    private final UserRepository userRepository;
    private final BotUtils botUtils;
    private final UserService userService;
    private final RegionRepository regionRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final BottleTypesRepository bottleTypesRepository;
    private final DeliveryTimeRepository deliveryTimeRepository;
    private final OrderRepository orderRepository;
    private final RoleRepository roleRepository;
    private final DeleteMessageService deleteMessageService;
    private BotDelivery botDelivery;
    private BasketRepository basketRepository;
    private OrderProductRepository orderProductRepository;
    private PasswordEncoder passwordEncoder;
    private TelegramStateDispatcher telegramStateDispatcher;

    @Autowired
    public void setBotDelivery(BotDelivery botDelivery) {
        this.botDelivery = botDelivery;
    }


    @Transactional
    public TelegramUser getTelegramUserOrCreate(Long chatId) {
        return telegramUserRepository.findByChatId(chatId).orElseGet(() -> {
            TelegramUser newUser = new TelegramUser(chatId);
            telegramUserRepository.save(newUser);
            return newUser;
        });
    }


    public void acceptStartSendShareContact(Message message, TelegramUser telegramUser) {
//        telegramStateDispatcher.dispatch(message, telegramUser);
        if (telegramUser.getState().equals(TelegramState.HAS_ORDER)) {

        } else if (telegramUser.getState().equals(TelegramState.WAITING_OPERATOR) || telegramUser.getState().equals(TelegramState.WAITING_OPERATOR_CHANGE_LOCATION)) {

        } else if (telegramUser.getState().equals(TelegramState.SHARE_LOCATION)) {

        } else if (telegramUser.getUser() != null && telegramUser.getUser().getPhone() != null) {
            if (isDeliveryUser(telegramUser.getUser())) {
                handleDeliveryUser(message, telegramUser);
            } else if (isUser(telegramUser.getUser())) {
                telegramUser.setState(TelegramState.CABINET);
                telegramUserRepository.save(telegramUser);
                sendCabinet(telegramUser);
            }
        } else {
            SendMessage sendMessage = new SendMessage(telegramUser.getChatId(), BotConstant.PLEASE_SHARE_CONTACT);
            sendMessage.replyMarkup(BotUtils.getGeneratedContactButton());
            SendResponse sendResponse = telegramBot.execute(sendMessage);
            Integer messageId = sendResponse.message().messageId();
            deleteMessageService.archivedForDeletingMessages(telegramUser, message.messageId(), BotConstant.START);
            deleteMessageService.archivedForDeletingMessages(telegramUser, messageId, BotConstant.PLEASE_SHARE_CONTACT);
            telegramUser.setState(TelegramState.SHARE_CONTACT);
            telegramUserRepository.save(telegramUser);
        }
    }



    public void saveContactSendMessage(Message message, TelegramUser telegramUser) {
        String contact = PhoneRepairUtil.repair(message.contact().phoneNumber());
        User user = userService.createdOrFindUser(contact);
        Region region = regionRepository.findByName("Toshkent");
        telegramUser.setRegion(region);
        deleteMessageService.archivedForDeletingMessages(telegramUser, message.messageId(), "Please share contact");
        if (user != null) {
            user.setFirstName(message.contact().firstName());
            user.setLastName(message.contact().lastName());
            telegramUser.setUser(user);
            telegramUserRepository.save(telegramUser);
            userRepository.save(user);
        }
        if (isDeliveryUser(telegramUser.getUser())) {
            handleDeliveryUser(message, telegramUser);
        } else if (isUser(telegramUser.getUser())) {
            telegramUser.setState(TelegramState.CABINET);
            telegramUser.getUser().setNewUser(false);
            telegramUserRepository.save(telegramUser);
            sendCabinet(telegramUser);
        } else {
            handleRegularUser(message, telegramUser, contact);
        }
    }

    private boolean isUser(User user) {
        return user.getRoles() != null &&
                user.getRoles().stream()
                        .anyMatch(role -> role.getRoleName().equals(RoleName.ROLE_USER));
    }

    private boolean isDeliveryUser(User user) {
        return user.getRoles() != null &&
                user.getRoles().stream()
                        .anyMatch(role -> role.getRoleName().equals(RoleName.ROLE_DELIVERY));
    }

    private void handleDeliveryUser(Message message, TelegramUser telegramUser) {
        deleteMessageService.archivedForDeletingMessages(telegramUser, message.messageId(), "Please share contact");
        SendMessage sendMessage = new SendMessage(
                telegramUser.getChatId(),
                telegramUser.getUser().getPassword() != null ? BotConstant.PLEASE_ENTER_PASSWORD : BotConstant.NEW_PASSWORD
        );
        sendMessage.replyMarkup(new ReplyKeyboardRemove(true));
        telegramUser.setState(telegramUser.getUser().getPassword() != null ? TelegramState.ENTER_OLD_PASSWORD_DELIVERY : TelegramState.ENTER_PASSWORD_DELIVERY);
        telegramUserRepository.save(telegramUser);
        SendResponse sendResponse = telegramBot.execute(sendMessage);
        Integer messageId = sendResponse.message().messageId();
        deleteMessageService.archivedForDeletingMessages(telegramUser, messageId, telegramUser.getUser().getPassword() != null ? BotConstant.PLEASE_ENTER_PASSWORD : BotConstant.NEW_PASSWORD);
    }

    private void handleRegularUser(Message message, TelegramUser telegramUser, String contact) {
        telegramUser.getUser().setPhone(contact);
        telegramUser.setUser(telegramUser.getUser());
        sendLocationButton(telegramUser);
        //sendRegionSelectionMessage(telegramUser);
    }

   /* private void sendRegionSelectionMessage(TelegramUser telegramUser) {
        SendMessage sendMessage = new SendMessage(
                telegramUser.getChatId(),
                BotConstant.PLEASE_CHOOSE_REGION
        );
        sendMessage.replyMarkup(botUtils.getGeneratedRegionButtons());
        SendResponse sendResponse = telegramBot.execute(sendMessage);
        Integer messageId = sendResponse.message().messageId();
        deleteMessageService.archivedForDeletingMessages(telegramUser, messageId, BotConstant.PLEASE_CHOOSE_REGION);
        telegramUser.setState(TelegramState.SHARE_REGION);
        telegramUserRepository.save(telegramUser);
    }*/



    public void saveLocationSendMessage(Message message, TelegramUser telegramUser) {
        ReplyKeyboardRemove removeKeyboard = new ReplyKeyboardRemove(true);
        telegramUser.setLocation(new Location(message.location().latitude().doubleValue(), message.location().longitude().doubleValue()));
        deleteMessageService.archivedForDeletingMessages(telegramUser, message.messageId(), "Please share location");
        sendDoneMessage(telegramUser, removeKeyboard);
        telegramUser.setState(TelegramState.WAITING_OPERATOR);
        telegramUserRepository.save(telegramUser);
        messagingTemplate.convertAndSend("/topic/status", "Location updated for user: " + telegramUser.getChatId());
    }

    private void sendDoneMessage(TelegramUser telegramUser, ReplyKeyboardRemove removeKeyboard) {
        SendMessage sendMessage = new SendMessage(
                telegramUser.getChatId(),
                BotConstant.DONE
        );
        sendMessage.replyMarkup(removeKeyboard);
        SendResponse sendResponse = telegramBot.execute(sendMessage);
        Integer messageId = sendResponse.message().messageId();
        deleteMessageService.archivedForDeletingMessages(telegramUser, messageId, "Location received");
    }

    public void sendDoneMessage(TelegramUser telegramUser) {
        SendMessage sendMessage = new SendMessage(
                telegramUser.getChatId(),
                BotConstant.DONE_ALREADY
        );
        SendResponse sendResponse = telegramBot.execute(sendMessage);
        Integer messageId = sendResponse.message().messageId();
        deleteMessageService.archivedForDeletingMessages(telegramUser, messageId, "Location received");
    }

    public void sendLocationButton(TelegramUser tgUser) {
        SendMessage message = new SendMessage(tgUser.getChatId(), BotConstant.PLEASE_SHARE_LOCATION);
        message.replyMarkup(botUtils.getGeneratedLocationButton());
        SendResponse sendResponse = telegramBot.execute(message);
        Integer messageId = sendResponse.message().messageId();
        deleteMessageService.archivedForDeletingMessages(tgUser, messageId, BotConstant.PLEASE_SHARE_LOCATION);
        tgUser.setState(TelegramState.SHARE_LOCATION);
        if (checkUser(tgUser)) {
            tgUser.getUser().setNewUser(true);
        }
        tgUser.getUser().setRoles(List.of(roleRepository.findByRoleName(RoleName.ROLE_USER)));
        telegramUserRepository.save(tgUser);
    }

    private boolean checkUser(TelegramUser tgUser) {
        List<Order> order = orderRepository.findAllByTelegramUser(tgUser);
        return order.isEmpty();
    }

    public void sendCabinet(Message message, TelegramUser tgUser) {
        if (!tgUser.getState().equals(TelegramState.CABINET)) {
            deleteMessageService.archivedForDeletingMessages(tgUser, message.messageId(), "Your cabinet will be here");
            SendMessage sendMessage = new SendMessage(tgUser.getChatId(), BotConstant.MENU);
            sendMessage.replyMarkup(botUtils.getGeneratedCabinetButtons());
            SendResponse sendResponse = telegramBot.execute(sendMessage);
            Integer messageId = sendResponse.message().messageId();
            deleteMessageService.deleteMessageAll(telegramBot, tgUser);
            deleteMessageService.archivedForDeletingMessages(tgUser, messageId, BotConstant.MENU);
            tgUser.setState(TelegramState.START_ORDERING);
            telegramUserRepository.save(tgUser);
        }
    }

    public void sendCabinet(TelegramUser tgUser) {
        if (tgUser.getState().equals(TelegramState.CABINET)) {
            SendMessage sendMessage = new SendMessage(tgUser.getChatId(), BotConstant.MENU);
            sendMessage.replyMarkup(botUtils.getGeneratedCabinetButtons());
            SendResponse sendResponse = telegramBot.execute(sendMessage);
            Integer messageId = sendResponse.message().messageId();
            deleteMessageService.deleteMessageAll(telegramBot, tgUser);
            deleteMessageService.archivedForDeletingMessages(tgUser, messageId, BotConstant.MENU);
            tgUser.setState(TelegramState.START_ORDERING);
            telegramUserRepository.save(tgUser);
        }
    }

    @NotNull
    private SendMessage createBottlePhoto(TelegramUser telegramUser) {
        List<BottleTypes> allBottleTypes = bottleTypesRepository.findAll();
        SendMessage sendMessage = new SendMessage(telegramUser.getChatId(), BotConstant.SELECT_BOTTLE_TYPE);
        sendMessage.replyMarkup(botUtils.generateBottleButton(allBottleTypes));
        return sendMessage;
    }


    public void startOrdering(Message message, TelegramUser telegramUser) {
        String text = message.text();
        if (text.equals(BotConstant.ORDER_BTN)) {
            deleteMessageService.archivedForDeletingMessages(telegramUser, message.messageId(), BotConstant.ORDER_BTN);
            startOrderingFunktion(telegramUser);
        }
    }

    private void startOrderingFunktion(TelegramUser telegramUser) {
        List<BottleTypes> allBottleTypes = bottleTypesRepository.findAll();
        if (allBottleTypes.isEmpty()) {
            SendMessage sendMessage = new SendMessage(
                    telegramUser.getChatId(),
                    BotConstant.NO_BOTTLE_TYPE
            );
            SendResponse sendResponse = telegramBot.execute(sendMessage);
            Integer messageId = sendResponse.message().messageId();
            deleteMessageService.archivedForDeletingMessages(telegramUser, messageId, "Suv mavjud emas!");
            telegramUser.setState(TelegramState.CABINET);
            telegramUserRepository.save(telegramUser);
            sendCabinet(telegramUser);
        } else {
            SendMessage sendMessage = createBottlePhoto(telegramUser);
            SendResponse sendResponse = telegramBot.execute(sendMessage);
            Integer messageId = sendResponse.message().messageId();
            deleteMessageService.archivedForDeletingMessages(telegramUser, messageId, "Suv mavjud");
            telegramUser.setState(TelegramState.SELECT_BOTTLE_TYPE);
            telegramUserRepository.save(telegramUser);
            return;
        }
    }



    public void acceptBottleTypeShowSelectNumber(Message message, TelegramUser telegramUser) {
        String type = message.text();
        if (handlePredefinedActions(type, telegramUser)) return;
        BottleTypes bottleTypes = bottleTypesRepository.findByType(type)
                .orElseThrow(() -> new RuntimeException("Bad bottle type"));

        updateTelegramUserBottleType(telegramUser, bottleTypes);

        updateBottleCountFromBasket(telegramUser);

        sendBottleTypeMessage(telegramUser, bottleTypes);

    }

    private boolean handlePredefinedActions(String type, TelegramUser telegramUser) {
        if (type.equals(BotConstant.CANCEL)) {
            deleteMessageService.deleteMessageAll(telegramBot, telegramUser);
            sendCabinet(telegramUser);
            return true;
        } else if (type.equals(BotConstant.BASKET)) {
            showBasket(telegramUser);
            return true;
        } else if (type.equals(BotConstant.BACK)) {
            sendCabinet(telegramUser);
            return true;
        }
        return false;
    }

    private void updateTelegramUserBottleType(TelegramUser telegramUser, BottleTypes bottleTypes) {
        telegramUser.setBottleTypes(bottleTypes);
        telegramUserRepository.save(telegramUser);
    }

    private void updateBottleCountFromBasket(TelegramUser telegramUser) {
        Basket basket = basketRepository.findByTelegramUserAndBottleType(
                telegramUser,
                telegramUser.getBottleTypes()
        );
        if (basket != null) {
            telegramUser.setBottleCount(basket.getAmount());
            telegramUserRepository.save(telegramUser);
        }
    }

    private void sendBottleTypeMessage(TelegramUser telegramUser, BottleTypes bottleTypes) {
        SendPhoto sendMessage = new SendPhoto(telegramUser.getChatId(), bottleTypes.getImage());
        sendMessage.caption(generatedTextForBottleType(bottleTypes, telegramUser));
        sendMessage.replyMarkup(botUtils.generateBottleNumberButtons(telegramUser));

        SendResponse response = telegramBot.execute(sendMessage);
        Integer messageId = response.message().messageId();

        telegramUser.setEditingMessageId(messageId);
        telegramUser.setState(TelegramState.SELECT_BOTTLE_NUMBER);
        telegramUserRepository.save(telegramUser);
        deleteMessageService.archivedForDeletingMessages(telegramUser, messageId, "Select bottle number");
    }

    @Transactional
    public String generatedTextForBottleType(BottleTypes bottleTypes, TelegramUser telegramUser) {
        double price = bottleTypes.getPrice();
        double totalPrice = telegramUser.getBottleCount() * price;

        return String.format(
                "🥤 Bakalashka turi: %s\n" +
                        "🔢 Soni: %d ta\n" +
                        "💲 Har birining narxi: %.2f so'm\n" +
                        "💰 Jami narxi: %.2f so'm\n",
                bottleTypes.getType(),
                telegramUser.getBottleCount(),
                price,
                totalPrice
        );
    }



    public void changeBottleNumber(CallbackQuery message, TelegramUser telegramUser) {
        String data = message.data();
        switch (data) {
            case BotConstant.ADD_TO_BASKET -> {
                processAddToBasket(telegramUser);
                telegramUser.setBottleCount(1);
                telegramUserRepository.save(telegramUser);
                SendMessage sendMessage = new SendMessage(
                        telegramUser.getChatId(),
                        "✅ Savatchaga qo'shildi!"
                );
                SendResponse sendResponse = telegramBot.execute(sendMessage);
                Integer messageId = sendResponse.message().messageId();
                deleteMessageService.archivedForDeletingMessages(telegramUser, messageId, "Savatchaga qo'shildi!");
                startOrderingFunktion(telegramUser);
                return;
            }
            case BotConstant.CANCEL_BTN -> {
                resetUserState(telegramUser);
                return;
            }
            case BotConstant.PLUS -> increaseBottleCount(telegramUser);
            case BotConstant.MINUS -> decreaseBottleCount(telegramUser);
            default -> throw new IllegalArgumentException("Unknown action: ");
        }

        telegramUserRepository.save(telegramUser);
        updateMessageWithBottleInfo(telegramUser);
    }


    private void resetUserState(TelegramUser telegramUser) {
        telegramUser.setBottleCount(1);
        telegramUser.setState(TelegramState.CABINET);
        telegramUserRepository.save(telegramUser);
        sendCabinet(telegramUser);
    }

    private void increaseBottleCount(TelegramUser telegramUser) {
        telegramUser.setBottleCount(telegramUser.getBottleCount() + 1);
    }

    private void decreaseBottleCount(TelegramUser telegramUser) {
        if (telegramUser.getBottleCount() > 1) {
            telegramUser.setBottleCount(telegramUser.getBottleCount() - 1);
        }
    }

    private void updateMessageWithBottleInfo(TelegramUser telegramUser) {
        String updatedText = generatedTextForBottleType(telegramUser.getBottleTypes(), telegramUser);
        EditMessageCaption editMessageText = new EditMessageCaption(
                telegramUser.getChatId(),
                telegramUser.getEditingMessageId());
        editMessageText.caption(updatedText);
        editMessageText.replyMarkup(botUtils.generateBottleNumberButtons(telegramUser));
        telegramBot.execute(editMessageText);
    }


    private void processAddToBasket(TelegramUser telegramUser) {
        BottleTypes bottleTypes = telegramUser.getBottleTypes();

        Basket existingBasket = basketRepository.findByTelegramUserAndBottleType(telegramUser, bottleTypes);
        if (existingBasket != null) {
            existingBasket.setAmount(telegramUser.getBottleCount());
            basketRepository.save(existingBasket);
        } else {
            Basket newBasket = Basket.builder()
                    .telegramUser(telegramUser)
                    .amount(telegramUser.getBottleCount())
                    .bottleType(bottleTypes)
                    .build();
            basketRepository.save(newBasket);
        }
    }


//    public LocalDate getDate(String day) {
//        if (day.equals("TODAY")) {
//            return LocalDate.now();
//        } else {
//            return LocalDate.now().plusDays(1);
//        }
//    }



    public void acceptOrderTimeAndShowConfirmation(CallbackQuery callbackQuery, TelegramUser tgUser) {
        if ("ignore".equals(callbackQuery.data())) {
            return;
        }

        List<Basket> baskets = basketRepository.findAllByTelegramUser(tgUser);
        StringBuilder basketInfoBuilder = new StringBuilder();
        double totalPrice = 0;

        for (Basket basket : baskets) {
            double discountedPrice = basket.getBottleType().getPrice() * basket.getAmount();
            int remainingAmount = basket.getAmount();

            if (basket.getBottleType().getSale_active()
                    && basket.getAmount() >= basket.getBottleType().getSale_discount()) {

                int saleAmount = basket.getBottleType().getSale_amount();
                int saleDiscount = basket.getBottleType().getSale_discount();

                remainingAmount = basket.getAmount() - saleAmount;

                discountedPrice = remainingAmount * basket.getBottleType().getPrice();
            }

            totalPrice += discountedPrice;

            basketInfoBuilder.append(generateBasketInfo(basket, remainingAmount, discountedPrice)).append("\n");
        }


        String orderInfo = """
                <b>🔖 Buyurtmangiz:</b>
                %s
                <b>💰 Umumiy narx:</b> %s sum
                """.formatted(
                basketInfoBuilder.toString(),
                String.format("%.2f", totalPrice)
        );

        SendMessage sendMessage = new SendMessage(tgUser.getChatId(), orderInfo);
        sendMessage.parseMode(ParseMode.HTML);
        sendMessage.replyMarkup(botUtils.generateConfirmOrderButtons());

        tgUser.setState(TelegramState.CREATE_ORDER);
        telegramUserRepository.save(tgUser);

        SendResponse sendResponse = telegramBot.execute(sendMessage);
        Integer messageId = sendResponse.message().messageId();
        deleteMessageService.archivedForDeletingMessages(tgUser, messageId, orderInfo);
    }

    public String generateBasketInfo(Basket basket, int remainingAmount, double discountedPrice) {
        StringBuilder result = new StringBuilder();
        result.append("""
                <b>🔖 Mahsulot nomi:</b> %s
                <b>🔢 Soni:</b> %d ta
                <b>💵 Narxi:</b> %d sum
                <b>💰 Jami narxi:</b> %d sum
                """.formatted(
                basket.getBottleType().getType(),
                basket.getAmount(),
                basket.getBottleType().getPrice(),
                basket.getBottleType().getPrice() * basket.getAmount()
        ));

        if (basket.getBottleType().getSale_active() && basket.getAmount() >= basket.getBottleType().getSale_discount()) {
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


    public String generateOrderTxt(TelegramUser tgUser) {
        return BotConstant.ORDER_INFO.formatted(
                tgUser.getBottleTypes().getType(),
                tgUser.getBottleCount(),
                tgUser.calcTotalAmountOfCurrentOrder(),
                tgUser.getCurrentOrderDay(),
                tgUser.getCurrentOrderDeliveryTime()
        );
    }

    @Transactional
    public void makeAnOrder(CallbackQuery callbackQuery, TelegramUser tgUser) {
        if (callbackQuery.data().equals(BotConstant.CANCEL) && tgUser.getState().equals(TelegramState.CREATE_ORDER)) {
            tgUser.setBottleCount(1);
            tgUser.setState(TelegramState.CABINET);
            telegramUserRepository.save(tgUser);
            sendCabinet(tgUser);
        } else if (callbackQuery.data().equals(BotConstant.CONFIRM_ORDER)) {
            Order order = Order.builder()
                    .id(tgUser.generateOrderId())
                    .location(tgUser.getLocation())
                    .region(tgUser.getRegion())
                    .district(tgUser.getDistrict())
                    .telegramUser(tgUser)
                    .orderStatus(OrderStatus.CREATED)
                    .phone(tgUser.getUser().getPhone())
                    .day(LocalDate.now())
                    .build();
            orderRepository.save(order);
            List<Basket> baskets = basketRepository.findAllByTelegramUser(tgUser);
            for (Basket basket : baskets) {
                OrderProduct orderProduct = OrderProduct.builder()
                        .order(order)
                        .amount(basket.getAmount())
                        .bottleTypes(basket.getBottleType())
                        .priceAtPurchase(basket.getBottleType().getPrice())
                        .discountAtPurchase(basket.getBottleType().getSale_active() ? basket.getBottleType().getSale_amount() : null)
                        .wasOnSale(basket.getBottleType().getSale_active())
                        .build();
                orderProductRepository.save(orderProduct);
            }
            basketRepository.deleteAllByTelegramUser(tgUser);
            SendMessage message = new SendMessage(tgUser.getChatId(), BotConstant.ORDER_FINISH_MSG);
            SendResponse sendResponse = telegramBot.execute(message);
            Integer messageId = sendResponse.message().messageId();
            deleteMessageService.archivedForDeletingMessages(tgUser, messageId, BotConstant.ORDER_FINISH_MSG);
            tgUser.setBottleCount(1);
            List<Order> orders = orderRepository.findByTelegramUserAndOrderStatus(tgUser, OrderStatus.COMPLETED);
            tgUser.getUser().setNewUser(orders.isEmpty());
            tgUser.setState(TelegramState.HAS_ORDER);
            telegramUserRepository.save(tgUser);
            messagingTemplate.convertAndSend("/topic/orders", "Order created for user: " + tgUser.getChatId());

        }
    }


    private DeliveryTime extractDeliveryTimeFromCallbackQuery(CallbackQuery callbackQuery) {
        Long deliveryTimeId = Long.parseLong(callbackQuery.data());
        return deliveryTimeRepository.findById(deliveryTimeId).orElseThrow(() -> new RuntimeException("delivery time not found"));
    }

    private LocalDate extractDayFromCallbackQuery(CallbackQuery callbackQuery) {
        int deliveryTimeId = Integer.parseInt(callbackQuery.data());
        if (deliveryTimeId == 1 || deliveryTimeId == 2 || deliveryTimeId == 3) {
            return LocalDate.now();
        } else {
            return LocalDate.now().plusDays(1);
        }
    }


    public void sendCabinetDelivery(Message message, TelegramUser telegramUser) {
        User user = getOrCreateUser(telegramUser);
        user.setPassword(message.text());
        telegramUser.setState(TelegramState.ENTER_PASSWORD_DELIVERY_CONFIRM);
        sendMessage(telegramUser, BotConstant.CONFIRM_PASSWORD);
        deleteMessageService.archivedForDeletingMessages(telegramUser, message.messageId(), BotConstant.CONFIRM_PASSWORD);
        telegramUserRepository.save(telegramUser);
    }



    public void sendCabinetConfirmCode(Message message, TelegramUser telegramUser) {
        if (message.text().equals(telegramUser.getUser().getPassword())) {
            telegramUser.setState(TelegramState.START_DELIVERY);
            deleteMessageService.archivedForDeletingMessages(telegramUser, message.messageId(), BotConstant.CONFIRM_PASSWORD);
            telegramUserRepository.save(telegramUser);
            botDelivery.startDelivery(message, telegramUser);
        } else {
            SendMessage sendMessage = new SendMessage(
                    telegramUser.getChatId(),
                    BotConstant.INCORRECT_PASSWORD
            );
            SendResponse sendResponse = telegramBot.execute(sendMessage);
            Integer messageId = sendResponse.message().messageId();
            deleteMessageService.deleteMessageAll(telegramBot, telegramUser);
            deleteMessageService.archivedForDeletingMessages(telegramUser, message.messageId(), BotConstant.INCORRECT_PASSWORD);
            deleteMessageService.archivedForDeletingMessages(telegramUser, messageId, BotConstant.INCORRECT_PASSWORD);
        }
    }


    public void sendCabinetOldPassword(Message message, TelegramUser telegramUser) {
        if (isPasswordCorrect(message.text(), telegramUser)) {
            telegramUser.getUser().setPassword(passwordEncoder.encode(message.text()));
            deleteMessageService.archivedForDeletingMessages(telegramUser, message.messageId(), BotConstant.OLD_PASSWORD);
        } else {
            deleteMessageService.deleteMessageAll(telegramBot, telegramUser);
            sendMessage(telegramUser, BotConstant.INCORRECT_PASSWORD);
            deleteMessageService.archivedForDeletingMessages(telegramUser, message.messageId(), BotConstant.INCORRECT_PASSWORD);
            return;
        }
        botDelivery.deliveryMenu(message, telegramUser);
    }



    public void sendUserDidNotAnswerPhone(TelegramUser tgUser) {
        SendMessage sendMessage = new SendMessage(
                tgUser.getChatId(),
                BotConstant.USER_DID_NOT_ANSWER
        );
        SendResponse sendResponse = telegramBot.execute(sendMessage);
        Integer messageId = sendResponse.message().messageId();
        deleteMessageService.archivedForDeletingMessages(tgUser, messageId, BotConstant.USER_DID_NOT_ANSWER);
    }


    private User getOrCreateUser(TelegramUser telegramUser) {
        Optional<User> user = userRepository.findByPhone(telegramUser.getUser().getPhone());
        if (user.isEmpty()) {
            user = Optional.ofNullable((telegramUser.getUser()));
        }
        return user.orElse(null);
    }

    private boolean isPasswordCorrect(String inputPassword, TelegramUser telegramUser) {
        return passwordEncoder.matches(inputPassword, telegramUser.getUser().getPassword());
    }

    private void sendMessage(TelegramUser chatId, String messageText) {
        SendMessage sendMessage = new SendMessage(chatId.getChatId(), messageText);
        SendResponse sendResponse = telegramBot.execute(sendMessage);
        Integer messageId = sendResponse.message().messageId();
        deleteMessageService.archivedForDeletingMessages(chatId, messageId, messageText);
    }


    public void setting(Message message, TelegramUser telegramUser) {
        SendMessage sendMessage = new SendMessage(
                telegramUser.getChatId(),
                BotConstant.SETTING);
        sendMessage.replyMarkup(botUtils.getGeneratedSettingButtons());
        SendResponse sendResponse = telegramBot.execute(sendMessage);
        Integer messageId = sendResponse.message().messageId();
        deleteMessageService.deleteMessageAll(telegramBot, telegramUser);
        deleteMessageService.archivedForDeletingMessages(telegramUser, message.messageId(), BotConstant.SETTING);
        deleteMessageService.archivedForDeletingMessages(telegramUser, messageId, BotConstant.SETTING);
        telegramUser.setState(TelegramState.SETTING);
        telegramUserRepository.save(telegramUser);
    }


    public void saveNewLocation(Message message, TelegramUser telegramUser) {
        Location location = new Location(message.location().latitude().doubleValue(), message.location().longitude().doubleValue());
        telegramUser.setLocation(location);
        telegramUserRepository.save(telegramUser);
        SendMessage sendMessage = new SendMessage(
                telegramUser.getChatId(),
                BotConstant.LOCATION_SAVED
        );
        sendMessage.replyMarkup(botUtils.getGeneratedSettingButtons());
        SendResponse sendResponse = telegramBot.execute(sendMessage);
        Integer messageId = sendResponse.message().messageId();
        deleteMessageService.archivedForDeletingMessages(telegramUser, message.messageId(), "New location saved");
        deleteMessageService.archivedForDeletingMessages(telegramUser, messageId, BotConstant.LOCATION_SAVED);
        telegramUser.setState(TelegramState.WAITING_OPERATOR_CHANGE_LOCATION);
        telegramUser.setChangeLocation(true);
        telegramUserRepository.save(telegramUser);
        messagingTemplate.convertAndSend("/topic/status", "Order created for user: " + telegramUser.getChatId());
    }


    public void sendNewLocationButton(Message message, TelegramUser telegramUser) {
        SendMessage sendMessage = new SendMessage(
                telegramUser.getChatId(),
                BotConstant.LOCATION
        );
        sendMessage.replyMarkup(botUtils.getChangeLocationButton());
        SendResponse sendResponse = telegramBot.execute(sendMessage);
        Integer messageId = sendResponse.message().messageId();
        deleteMessageService.archivedForDeletingMessages(telegramUser, message.messageId(), "New location button");
        deleteMessageService.archivedForDeletingMessages(telegramUser, messageId, BotConstant.LOCATION);
        telegramUser.setState(TelegramState.SAVE_NEW_LOCATION);
        telegramUserRepository.save(telegramUser);
    }

    public void sendMyOrders(Message message, TelegramUser telegramUser) {
        deleteMessageService.archivedForDeletingMessages(telegramUser, message.messageId(), "Buyurtma mahsulotlari");

        List<Order> orders = orderRepository.findAllByTelegramUser(telegramUser);

        if (orders.isEmpty()) {
            sendMessage(telegramUser, BotConstant.NO_ORDERS);
            return;
        }

        sendMessage(telegramUser, "📋 Buyurtmalarim:\n\n");

        for (Order order : orders) {
            List<OrderProduct> orderProducts = orderProductRepository.findAllByOrder(order);

            for (OrderProduct orderProduct : orderProducts) {

                StringBuilder messageBuilder = new StringBuilder();
                messageBuilder.append("🆔 Buyurtma raqami: <b>").append(order.getId()).append("</b>\n")
                        .append("🍼 Mahsulot turi: <b>").append(orderProduct.getBottleTypes().getType()).append("</b>\n")
                        .append("🔢 Soni: <b>").append(orderProduct.getAmount()).append(" ta</b>\n");

                if (orderProduct.isOnSale()) {
                    int discountPrice = orderProduct.getBottleTypes().getPrice() * orderProduct.getBottleTypes().getSale_amount();
                    messageBuilder.append("🔒 Chegirma: <b>").append(orderProduct.getBottleTypes().getSale_amount()).append(" ta</b>\n");
                    messageBuilder.append("💰 Chegirma narxi: <b>").append(discountPrice).append(" so'm</b>\n");
                } else {
                    messageBuilder.append("💵 Oddiy narxi: <b>").append(orderProduct.getBottleTypes().getPrice()).append(" so'm</b>\n");
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



    public void sendPleaseWaitingOperator(Message message, TelegramUser telegramUser) {
        deleteMessageService.archivedForDeletingMessages(telegramUser, message.messageId(), "Please waiting operator");
        sendMessage(telegramUser, BotConstant.PLEASE_WAITING_OPERATOR);
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
            sendMessage(telegramUser, BotConstant.NO_BASKET);
            telegramUser.setState(TelegramState.CABINET);
            telegramUserRepository.save(telegramUser);
            sendCabinet(telegramUser);
            return;
        }
        int totalPrice = 0;

        for (Basket basket : baskets) {
            StringBuilder messageBuilder = new StringBuilder();
            int remainingAmount = basket.getAmount();
            int finalTotalPrice = basket.getBottleType().getPrice() * basket.getAmount();

            messageBuilder.append("🛒 Mahsulot: ")
                    .append("<b>").append(basket.getBottleType().getType()).append("</b>\n")
                    .append("🔢 Soni: ")
                    .append("<b>").append(basket.getAmount()).append(" ta</b>\n")
                    .append("💵 Narxi: ")
                    .append("<b>").append(basket.getBottleType().getPrice()).append(" so'm</b>\n")
                    .append("💰 Jami narx: ")
                    .append("<b>").append(finalTotalPrice).append(" so'm</b>\n\n");

            if (basket.getBottleType().getSale_active() && basket.getAmount() >= basket.getBottleType().getSale_discount()) {
                int giftAmount = basket.getBottleType().getSale_amount();
                int giftPrice = giftAmount * basket.getBottleType().getPrice();

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



    public void settingMenu(Message message, TelegramUser telegramUser) {
        switch (message.text()) {
            case BotConstant.NEW_LOCATION -> {
                SendMessage sendMessage = new SendMessage(
                        telegramUser.getChatId(),
                        BotConstant.NEW_LOCATION_MESSAGE
                );
                sendMessage.replyMarkup(botUtils.getChangeLocationButton());
                SendResponse sendResponse = telegramBot.execute(sendMessage);
                Integer messageId = sendResponse.message().messageId();
                deleteMessageService.archivedForDeletingMessages(telegramUser, message.messageId(), "New location button");
                deleteMessageService.archivedForDeletingMessages(telegramUser, messageId, BotConstant.NEW_LOCATION_MESSAGE);
                telegramUser.setState(TelegramState.SAVE_NEW_LOCATION);
                telegramUserRepository.save(telegramUser);
            }
            case BotConstant.MY_ORDERS -> sendMyOrders(message, telegramUser);
            default -> sendCabinet(telegramUser);
        }
    }

//
//    public void decreaseBasketAmount(Integer basketId, TelegramUser telegramUser) {
//        Optional<Basket> basketOptional = basketRepository.findById(basketId);
//
//        if (basketOptional.isPresent()) {
//            Basket basket = basketOptional.get();
//
//            if (basket.getAmount() > 1) {
//                basket.setAmount(basket.getAmount() - 1);
//                basketRepository.save(basket);
//                updateBasketMessage(telegramUser, basket, basket.getMessageId());
//            } else {
//                basketRepository.delete(basket);
//                return;
//            }
//        }
//    }


//
//    public void increaseBasketAmount(Integer uuid, TelegramUser telegramUser) {
//        Optional<Basket> basket = basketRepository.findById(uuid);
//
//        if (basket.isPresent()) {
//            Basket currentBasket = basket.get();
//
//            currentBasket.setAmount(currentBasket.getAmount() + 1);
//            basketRepository.save(currentBasket);
//
//            Integer messageId = currentBasket.getMessageId();
//            if (messageId != null) {
//                updateBasketMessage(telegramUser, currentBasket, messageId);
//            }
//        }
//    }


    private void updateBasketMessage(TelegramUser telegramUser, Basket basket, Integer messageId) {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("🛒 Mahsulot: ")
                .append("<b>").append(basket.getBottleType().getType()).append("</b>\n")
                .append("🔢 Soni: ")
                .append("<b>").append(basket.getAmount()).append(" ta</b>\n")
                .append("💵 Narxi: ")
                .append("<b>").append(basket.getBottleType().getPrice()).append(" so'm</b>\n")
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


    @Autowired
    public void setBasketRepository(BasketRepository basketRepository) {
        this.basketRepository = basketRepository;
    }

//
//    public void updateBasketAmount(String data, TelegramUser telegramUser) {
//        String[] splitData = data.contains("_") ? data.split("_") : new String[]{data};
//        String action = splitData[0];
//        String basketId = splitData.length > 1 ? splitData[1] : null;
//
//        if (basketId != null) {
//            Optional<Basket> basketOptional = basketRepository.findById(Integer.valueOf(basketId));
//            basketOptional.ifPresent(basket -> {
//                if (BotConstant.PLUS.equalsIgnoreCase(action)) {
//                    increaseBasketAmount(basket.getId(), telegramUser);
//                } else if (BotConstant.MINUS.equalsIgnoreCase(action) && basket.getAmount() > 0) {
//                    decreaseBasketAmount(basket.getId(), telegramUser);
//                }
//                updateBasketMessage(telegramUser, basket, basket.getMessageId());
//            });
//        }
//
//    }


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


    public void showDeliveryTimeMenu(CallbackQuery messageCallback, TelegramUser telegramUser, String data) {
        telegramUser.setState(TelegramState.CONFIRM_ORDER);
        telegramUserRepository.save(telegramUser);
    }

    @Autowired
    public void setOrderProductRepository(OrderProductRepository orderProductRepository) {
        this.orderProductRepository = orderProductRepository;
    }


    public void removeBasketProduct(TelegramUser telegramUser, String data) {
        long basketId = Long.parseLong(data.split("_")[1]);
        basketRepository.deleteById(basketId);
        deleteMessageService.deleteMessageAll(telegramBot, telegramUser);
        showBasket(telegramUser);
    }

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired
    public void setTelegramStateDispatcher(TelegramStateDispatcher telegramStateDispatcher) {
        this.telegramStateDispatcher = telegramStateDispatcher;
    }
}
