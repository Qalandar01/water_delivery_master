package uz.pdp.water_delivery.bot;


import com.pengrad.telegrambot.model.request.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.pdp.water_delivery.dto.Location;
import uz.pdp.water_delivery.entity.*;
import uz.pdp.water_delivery.entity.enums.OrderStatus;
import uz.pdp.water_delivery.repo.DeliveryTimeRepository;
import uz.pdp.water_delivery.repo.ProductRepository;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BotUtils {

    private final ProductRepository productRepository;
    private final DeliveryTimeRepository deliveryTimeRepository;

    public static Keyboard getGeneratedContactButton() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(
                new KeyboardButton(BotConstant.SHARE_CONTACT).requestContact(true)
        ).resizeKeyboard(true).oneTimeKeyboard(true);
        return replyKeyboardMarkup;
    }


    public Keyboard getGeneratedLocationButton() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(
                new KeyboardButton(BotConstant.SHARE_LOCATION).requestLocation(true)
        ).resizeKeyboard(true).oneTimeKeyboard(true);
        return replyKeyboardMarkup;
    }
    public Keyboard getChangeLocationButton() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(
                new KeyboardButton(BotConstant.SHARE_LOCATION).requestLocation(true)
        ).resizeKeyboard(true).oneTimeKeyboard(true);
        replyKeyboardMarkup.addRow(new KeyboardButton(BotConstant.BACK));
        return replyKeyboardMarkup;
    }

    public InlineKeyboardMarkup getStartedDelivery() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton button = new InlineKeyboardButton(BotConstant.START_DELIVERY)
                .callbackData(BotConstant.START_DELIVERY);
        inlineKeyboardMarkup.addRow(button);
        return inlineKeyboardMarkup;
    }


//    public InlineKeyboardMarkup getGeneratedRegionButtons() {
//        List<Region> regions = regionRepository.findAll();
//        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
//
//        for (Region region : regions) {
//            InlineKeyboardButton button = new InlineKeyboardButton("🏛️ " + region.getName())
//                    .callbackData(BotConstant.REGION + region.getId());
//            inlineKeyboardMarkup.addRow(button);
//        }
//
//        return inlineKeyboardMarkup;
//    }


    public InlineKeyboardMarkup generateBottleButton10L() {
        Product product = productRepository.findById(1L).orElseThrow();
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton button10L = new InlineKeyboardButton("\uD83C\uDF79 10L")
                .callbackData(BotConstant.BOTTLE_TYPE + product.getId());
        inlineKeyboardMarkup.addRow(button10L);
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup generateBottleButton20L() {
        Product product = productRepository.findById(2L).orElseThrow();
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton button20L = new InlineKeyboardButton("\uD83C\uDF79 20L")
                .callbackData(BotConstant.BOTTLE_TYPE + product.getId());
        inlineKeyboardMarkup.addRow(button20L);
        return inlineKeyboardMarkup;
    }


    public InlineKeyboardMarkup generateProductNumberButtons(TelegramUser telegramUser) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        keyboardMarkup.addRow(createQuantityButtons(telegramUser));
        keyboardMarkup.addRow(createActionButtons());
        return keyboardMarkup;
    }

    private InlineKeyboardButton[] createQuantityButtons(TelegramUser telegramUser) {
        return new InlineKeyboardButton[]{
                new InlineKeyboardButton(BotConstant.MINUS).callbackData(BotConstant.MINUS),
                new InlineKeyboardButton(String.valueOf(telegramUser.getProductCount())).callbackData("number"),
                new InlineKeyboardButton(BotConstant.PLUS).callbackData(BotConstant.PLUS)
        };
    }


    private InlineKeyboardButton[] createActionButtons() {
        return new InlineKeyboardButton[]{
                new InlineKeyboardButton(BotConstant.CANCEL_BTN).callbackData(BotConstant.CANCEL_BTN ),
                new InlineKeyboardButton(BotConstant.ADD_TO_BASKET).callbackData(BotConstant.ADD_TO_BASKET)
        };
    }


    public InlineKeyboardMarkup generateDeliveryTimeButtons() {
        List<DeliveryTime> deliveryTimes = deliveryTimeRepository.findAllByOrderByIdAsc();
        List<InlineKeyboardButton> todayButtons = new ArrayList<>();
        List<InlineKeyboardButton> tomorrowButtons = new ArrayList<>();
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        LocalTime now = LocalTime.now();

        for (DeliveryTime time : deliveryTimes) {
            LocalTime startTime = time.getStartTime();
            if (time.getDay().equals("Bugun") && startTime.isAfter(now)) {
                InlineKeyboardButton button = new InlineKeyboardButton(
                        "🕘  " + time.getStartTime() + "-" + time.getEndTime()
                ).callbackData(String.valueOf(time.getId()));
                todayButtons.add(button);
            } else if (time.getDay().equals("Ertaga")) {
                InlineKeyboardButton button = new InlineKeyboardButton(
                        "🕙  " + time.getStartTime() + "-" + time.getEndTime()
                ).callbackData(String.valueOf(time.getId()));
                tomorrowButtons.add(button);
            }
        }

        if (!todayButtons.isEmpty()) {
            InlineKeyboardButton todayButton1 = new InlineKeyboardButton("Bugun ▼").callbackData("ignore");
            keyboardMarkup.addRow(todayButton1);
        }
        InlineKeyboardButton tomorrowButton2 = new InlineKeyboardButton("Ertaga ▼").callbackData("ignore");
        for (InlineKeyboardButton todayButton : todayButtons) {
            keyboardMarkup.addRow(todayButton);
        }

        keyboardMarkup.addRow(tomorrowButton2);
        for (InlineKeyboardButton tomorrowButton : tomorrowButtons) {
            keyboardMarkup.addRow(tomorrowButton);
        }
        return keyboardMarkup;
    }

    public static InlineKeyboardMarkup getGeneratedStartDeliveryButtons() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.addRow(
                new InlineKeyboardButton(BotConstant.PASSWORD).callbackData(BotConstant.PASSWORD)
        );
        return inlineKeyboardMarkup;
    }

    public static InlineKeyboardMarkup getGeneratedPasswordButtons() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.addRow(new InlineKeyboardButton(BotConstant.NEW_PASSWORD).callbackData(BotConstant.NEW_PASSWORD));
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup generateOrdersButtons(List<Order> orders) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        for (Order order : orders) {
            keyboardMarkup.addRow(
                    new InlineKeyboardButton(order.getDistrict() + " " + getTimeRange(order))
                            .callbackData(String.valueOf(order.getId()))
            );
        }
        keyboardMarkup.addRow(
                new InlineKeyboardButton(BotConstant.BACK).callbackData(BotConstant.BACK)
        );
        return keyboardMarkup;
    }

    public String getTimeRange(Order order) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return order.getDeliveryTime().getStartTime().format(formatter) + " - " + order.getDeliveryTime().getEndTime().format(formatter);
    }

    public String getTimeRange(CurrentOrders currentOrders) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return currentOrders.getOrder().getDeliveryTime().getStartTime().format(formatter) + " - " + currentOrders.getOrder().getDeliveryTime().getEndTime().format(formatter);
    }

    public InlineKeyboardMarkup generateStartDelivered() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup(
                new InlineKeyboardButton(BotConstant.START_DELIVERED).callbackData(BotConstant.START_DELIVERED));
        inlineKeyboardMarkup.addRow(new InlineKeyboardButton(BotConstant.BACK).callbackData(BotConstant.BACK));
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getFunctionButtons(CurrentOrders currentOrders) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        if (currentOrders != null && currentOrders.getOrder() != null) {
            if (OrderStatus.ASSIGNED.equals(currentOrders.getOrder().getOrderStatus())) {
                markup.addRow(new InlineKeyboardButton(BotConstant.START_DELIVERED_ORDER)
                        .callbackData(BotConstant.START_DELIVERED_ORDER + "_" + currentOrders.getOrder().getId()));
            } else if (OrderStatus.DELIVERING.equals(currentOrders.getOrder().getOrderStatus())) {
                markup.addRow(new InlineKeyboardButton(BotConstant.PAYMENT_DONE)
                        .callbackData(BotConstant.PAYMENT_DONE + "_" + currentOrders.getOrder().getId()));
            }
            markup.addRow(new InlineKeyboardButton(BotConstant.NEXT)
                    .callbackData(BotConstant.NEXT + "_" + currentOrders.getOrder().getId()));
            markup.addRow(new InlineKeyboardButton(BotConstant.PHONE_OFF)
                    .callbackData(BotConstant.PHONE_OFF + "_" + currentOrders.getOrder().getId()));
        }
        return markup;
    }

    public InlineKeyboardMarkup optionsBtn(Order order) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.addRow(new InlineKeyboardButton(BotConstant.ARRIVED).callbackData(BotConstant.ARRIVED));
        inlineKeyboardMarkup.addRow(new InlineKeyboardButton(BotConstant.BACK).callbackData(BotConstant.BACK),
                new InlineKeyboardButton(BotConstant.NEXT).callbackData(BotConstant.NEXT));
        return inlineKeyboardMarkup;
    }

    public ReplyKeyboardMarkup getGeneratedCabinetButtons() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(
                new KeyboardButton(BotConstant.ORDER_BTN));
        replyKeyboardMarkup.addRow(new KeyboardButton(BotConstant.BASKET));
        replyKeyboardMarkup.addRow(new KeyboardButton(BotConstant.SETTING));
        return replyKeyboardMarkup.resizeKeyboard(true);
    }


    public Keyboard getGeneratedSettingButtons() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(
                new KeyboardButton(BotConstant.NEW_LOCATION));
        replyKeyboardMarkup.addRow(new KeyboardButton(BotConstant.MY_ORDERS));
        replyKeyboardMarkup.addRow(new KeyboardButton(BotConstant.CABINET));
        return replyKeyboardMarkup.resizeKeyboard(true);
    }


    public ReplyKeyboardMarkup generateProductButton(List<Product> products) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup("");
        List<KeyboardButton> row;

        if (products == null || products.isEmpty()) {
            return replyKeyboardMarkup;
        }

        for (int i = 0; i < products.size(); i += 2) {
            row = new ArrayList<>();
            row.add(new KeyboardButton(products.get(i).getType()));
            if (i + 1 < products.size()) {
                row.add(new KeyboardButton(products.get(i + 1).getType()));
            }
            replyKeyboardMarkup.addRow(row.toArray(new KeyboardButton[0]));
        }

        row = Arrays.asList(
                new KeyboardButton(BotConstant.BACK),
                new KeyboardButton(BotConstant.BASKET)
        );
        replyKeyboardMarkup.addRow(row.toArray(new KeyboardButton[0]));
        return replyKeyboardMarkup.resizeKeyboard(true);
    }



    public InlineKeyboardMarkup generateConfirmOrderButtons() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup(
                new InlineKeyboardButton(
                        BotConstant.CANCEL
                ).callbackData(BotConstant.CANCEL),
                new InlineKeyboardButton(
                        BotConstant.CONFIRM_BTN
                ).callbackData(BotConstant.CONFIRM_ORDER)
        );
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getBasketButton(Basket basket) {
        return new InlineKeyboardMarkup(
                new InlineKeyboardButton(BotConstant.DELETE + " " + basket.getProduct().getType())
                        .callbackData(BotConstant.DELETE + "_" + basket.getId())
        );
    }


    public InlineKeyboardMarkup getCreateOrClearOrderButton() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup(
                new InlineKeyboardButton(BotConstant.CREATE_ORDER)
                        .callbackData(BotConstant.CONFIRM_ORDER));
        inlineKeyboardMarkup.addRow(new InlineKeyboardButton(BotConstant.CABINET)
                .callbackData(BotConstant.CABINET));
        return inlineKeyboardMarkup;
    }

    public String makeLinkFromLocation(Location location) {
        double lon = location.getLongitude();
        double lat = location.getLatitude();
        return String.format("https://yandex.com/maps/?rtext=~%f,%f", lat, lon);
    }


}

