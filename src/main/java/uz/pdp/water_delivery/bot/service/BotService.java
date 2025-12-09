package uz.pdp.water_delivery.bot.service;


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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.water_delivery.bot.BotConstant;
import uz.pdp.water_delivery.bot.BotUtils;
import uz.pdp.water_delivery.bot.TelegramUser;
import uz.pdp.water_delivery.bot.delivery.BotDelivery;
import uz.pdp.water_delivery.bot.handlers.state.TelegramStateDispatcher;
import uz.pdp.water_delivery.model.dto.Location;
import uz.pdp.water_delivery.model.entity.*;
import uz.pdp.water_delivery.model.enums.OrderStatus;
import uz.pdp.water_delivery.model.enums.RoleName;
import uz.pdp.water_delivery.model.enums.TelegramState;
import uz.pdp.water_delivery.model.repo.*;
import uz.pdp.water_delivery.services.DeleteMessageService;
import uz.pdp.water_delivery.services.FileService;
import uz.pdp.water_delivery.services.UserService;
import uz.pdp.water_delivery.utils.PhoneRepairUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BotService {

    private final TelegramBot telegramBot;
    private final TelegramUserRepository telegramUserRepository;
    private final UserRepository userRepository;
    private final BotUtils botUtils;
    private final UserService userService;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final RoleRepository roleRepository;
    private final DeleteMessageService deleteMessageService;
    private BotDelivery botDelivery;
    private BasketRepository basketRepository;
    private OrderProductRepository orderProductRepository;
    private PasswordEncoder passwordEncoder;
    private FileService fileService;
    private BotNavigationService botNavigationService;
    private OrderBotService orderBotService;
    private BasketBotService basketBotService;

    @Autowired
    public void setBotDelivery(BotDelivery botDelivery) {
        this.botDelivery = botDelivery;
    }


    @Transactional
    public TelegramUser getTelegramUserOrCreate(Long chatId) {
        try {
            return telegramUserRepository.findByChatId(chatId).orElseGet(() -> {
                TelegramUser newUser = new TelegramUser(chatId);
                return telegramUserRepository.saveAndFlush(newUser); // Use saveAndFlush
            });
        } catch (DataIntegrityViolationException e) {
            // If another thread created it, fetch it
            return telegramUserRepository.findByChatId(chatId)
                    .orElseThrow(() -> new RuntimeException("Failed to get or create user"));
        }
    }


//    public void acceptStartSendShareContact(Message message, TelegramUser telegramUser) {

    /// /        telegramStateDispatcher.dispatch(message, telegramUser);
//        if (telegramUser.getState().equals(TelegramState.HAS_ORDER)) {
//
//        } else if (telegramUser.getState().equals(TelegramState.WAITING_OPERATOR) || telegramUser.getState().equals(TelegramState.WAITING_OPERATOR_CHANGE_LOCATION)) {
//
//        } else if (telegramUser.getState().equals(TelegramState.SHARE_LOCATION)) {
//
//        } else if (telegramUser.getUser() != null && telegramUser.getUser().getPhone() != null) {
//            if (isDeliveryUser(telegramUser.getUser())) {
//                handleDeliveryUser(message, telegramUser);
//            } else if (isUser(telegramUser.getUser())) {
//                telegramUser.setState(TelegramState.CABINET);
//                telegramUserRepository.save(telegramUser);
//                sendCabinet(telegramUser);
//            }
//        } else {
//            SendMessage sendMessage = new SendMessage(telegramUser.getChatId(), uz.pdp.water_delivery.bot.BotConstant.PLEASE_SHARE_CONTACT);
//            sendMessage.replyMarkup(BotUtils.getGeneratedContactButton());
//            SendResponse sendResponse = telegramBot.execute(sendMessage);
//            Integer messageId = sendResponse.message().messageId();
//            deleteMessageService.archivedForDeletingMessages(telegramUser, message.messageId(), uz.pdp.water_delivery.bot.BotConstant.START);
//            deleteMessageService.archivedForDeletingMessages(telegramUser, messageId, uz.pdp.water_delivery.bot.BotConstant.PLEASE_SHARE_CONTACT);
//            telegramUser.setState(TelegramState.SHARE_CONTACT);
//            telegramUserRepository.save(telegramUser);
//        }
//    }


//    public void saveContactSendMessage(Message message, TelegramUser telegramUser) {
//        String contact = PhoneRepairUtil.repair(message.contact().phoneNumber());
//        User user = userService.createdOrFindUser(contact);
//        telegramUser.setRegion("Toshkent");
//        deleteMessageService.archivedForDeletingMessages(telegramUser, message.messageId(), "Please share contact");
//        if (user != null) {
//            user.setFirstName(message.contact().firstName());
//            user.setLastName(message.contact().lastName());
//            telegramUser.setUser(user);
//            telegramUserRepository.save(telegramUser);
//            userRepository.save(user);
//        }
//        if (isDeliveryUser(telegramUser.getUser())) {
//            handleDeliveryUser(message, telegramUser);
//        } else if (isUser(telegramUser.getUser())) {
//            telegramUser.setState(TelegramState.CABINET);
//            telegramUser.getUser().setNewUser(false);
//            telegramUserRepository.save(telegramUser);
//            sendCabinet(telegramUser);
//        } else {
//            handleRegularUser(message, telegramUser, contact);
//        }
//    }

//    private boolean isUser(User user) {
//        return user.getRoles() != null &&
//                user.getRoles().stream()
//                        .anyMatch(role -> role.getRoleName().equals(RoleName.ROLE_USER));
//    }
//
//    private boolean isDeliveryUser(User user) {
//        return user.getRoles() != null &&
//                user.getRoles().stream()
//                        .anyMatch(role -> role.getRoleName().equals(RoleName.ROLE_DELIVERY));
//    }
//
//    private void handleDeliveryUser(Message message, TelegramUser telegramUser) {
//        deleteMessageService.archivedForDeletingMessages(telegramUser, message.messageId(), "Please share contact");
//        SendMessage sendMessage = new SendMessage(
//                telegramUser.getChatId(),
//                telegramUser.getUser().getPassword() != null ? uz.pdp.water_delivery.bot.BotConstant.PLEASE_ENTER_PASSWORD : uz.pdp.water_delivery.bot.BotConstant.NEW_PASSWORD
//        );
//        sendMessage.replyMarkup(new ReplyKeyboardRemove(true));
//        telegramUser.setState(telegramUser.getUser().getPassword() != null ? TelegramState.ENTER_OLD_PASSWORD_DELIVERY : TelegramState.ENTER_PASSWORD_DELIVERY);
//        telegramUserRepository.save(telegramUser);
//        SendResponse sendResponse = telegramBot.execute(sendMessage);
//        Integer messageId = sendResponse.message().messageId();
//        deleteMessageService.archivedForDeletingMessages(telegramUser, messageId, telegramUser.getUser().getPassword() != null ? uz.pdp.water_delivery.bot.BotConstant.PLEASE_ENTER_PASSWORD : uz.pdp.water_delivery.bot.BotConstant.NEW_PASSWORD);
//    }
//
//    private void handleRegularUser(Message message, TelegramUser telegramUser, String contact) {
//        telegramUser.getUser().setPhone(contact);
//        telegramUser.setUser(telegramUser.getUser());
//        sendLocationButton(telegramUser);
//        //sendRegionSelectionMessage(telegramUser);
//    }

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
    public void sendMessage(TelegramUser chatId, String messageText) {
        SendMessage sendMessage = new SendMessage(chatId.getChatId(), messageText);
        SendResponse sendResponse = telegramBot.execute(sendMessage);
        Integer messageId = sendResponse.message().messageId();
        deleteMessageService.archivedForDeletingMessages(chatId, messageId, messageText);
    }


    @NotNull
    public SendMessage createProductPhoto(TelegramUser telegramUser) {
        List<Product> products = productRepository.findAll();
        SendMessage sendMessage = new SendMessage(telegramUser.getChatId(), BotConstant.SELECT_BOTTLE_TYPE);
        sendMessage.replyMarkup(botUtils.generateProductButton(products));
        return sendMessage;
    }


    public void sendProductMessage(TelegramUser telegramUser, Product product) {
        SendPhoto sendMessage = new SendPhoto(telegramUser.getChatId(), fileService.getProductImageContent(product));
        sendMessage.caption(basketBotService.generatedTextForProduct(product, telegramUser));
        sendMessage.replyMarkup(botUtils.generateProductNumberButtons(telegramUser));

        SendResponse response = telegramBot.execute(sendMessage);
        Integer messageId = response.message().messageId();

        telegramUser.setEditingMessageId(messageId);
        telegramUser.setState(TelegramState.SELECT_BOTTLE_NUMBER);
        telegramUserRepository.save(telegramUser);
        deleteMessageService.archivedForDeletingMessages(telegramUser, messageId, "Select bottle number");
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
            double discountedPrice = basket.getProduct().getPrice() * basket.getAmount();
            int remainingAmount = basket.getAmount();

            if (basket.getProduct().getSale_active()
                    && basket.getAmount() >= basket.getProduct().getSale_discount()) {

                int saleAmount = basket.getProduct().getSale_amount();
                int saleDiscount = basket.getProduct().getSale_discount();

                remainingAmount = basket.getAmount() - saleAmount;

                discountedPrice = remainingAmount * basket.getProduct().getPrice();
            }

            totalPrice += discountedPrice;

            basketInfoBuilder.append(basketBotService.generateBasketInfo(basket, remainingAmount, discountedPrice)).append("\n");
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


    private LocalDate extractDayFromCallbackQuery(CallbackQuery callbackQuery) {
        int deliveryTimeId = Integer.parseInt(callbackQuery.data());
        if (deliveryTimeId == 1 || deliveryTimeId == 2 || deliveryTimeId == 3) {
            return LocalDate.now();
        } else {
            return LocalDate.now().plusDays(1);
        }
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
            case BotConstant.MY_ORDERS -> orderBotService.sendMyOrders(message, telegramUser);
            default -> botNavigationService.sendCabinet(telegramUser);
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





    public void showDeliveryTimeMenu(CallbackQuery messageCallback, TelegramUser telegramUser, String data) {
        telegramUser.setState(TelegramState.CONFIRM_ORDER);
        telegramUserRepository.save(telegramUser);
    }

    @Autowired
    public void setOrderProductRepository(OrderProductRepository orderProductRepository) {
        this.orderProductRepository = orderProductRepository;
    }




    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired
    public void setTelegramStateDispatcher(TelegramStateDispatcher telegramStateDispatcher) {
    }

    @Autowired
    public void setFileService(FileService fileService) {
        this.fileService = fileService;
    }

    @Autowired
    public void setBotNavigationService(BotNavigationService botNavigationService) {
        this.botNavigationService = botNavigationService;
    }

    @Autowired
    public void setOrderBotService(OrderBotService orderBotService) {
        this.orderBotService = orderBotService;
    }

    @Autowired
    public void setBasketBotService(BasketBotService basketBotService) {
        this.basketBotService = basketBotService;
    }
}
