package uz.pdp.water_delivery.bot.delivery;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.water_delivery.bot.BotConstant;
import uz.pdp.water_delivery.bot.BotService;
import uz.pdp.water_delivery.bot.BotUtils;
import uz.pdp.water_delivery.bot.TelegramUser;
import uz.pdp.water_delivery.dto.Location;
import uz.pdp.water_delivery.entity.*;
import uz.pdp.water_delivery.entity.enums.OrderStatus;
import uz.pdp.water_delivery.entity.enums.TelegramState;
import uz.pdp.water_delivery.repo.*;
import uz.pdp.water_delivery.services.service.DeleteMessageService;
import uz.pdp.water_delivery.utils.DistanceUtil;
import uz.pdp.water_delivery.utils.LogErrorFile;
import uz.pdp.water_delivery.utils.RouteDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BotDelivery {

    private final TelegramBot telegramBot;
    private final TelegramUserRepository telegramUserRepository;
    private final OrderRepository orderRepository;
    private final BotUtils botUtils;
    private final CourierRepository courierRepository;
    private final DistanceUtil distanceUtil;
    private final CurrentOrdersRepository currentOrdersRepository;
    private final LogErrorFile logErrorFile;
    private BotService botService;
    private DeleteMessageService deleteMessageService;

    @Autowired
    private OrderProductRepository orderProductRepository;

    @Autowired
    public void setBotService(BotService botService) {
        this.botService = botService;
    }


    public void startDelivery(Message message, TelegramUser telegramUser) {
        try {
            SendMessage sendMessage = new SendMessage(
                    telegramUser.getChatId(),
                    BotConstant.START_DELIVERY_MESSAGE
            );
            sendMessage.replyMarkup(botUtils.getStartedDelivery());
            telegramUser.setState(TelegramState.START_DELIVERY);
            SendResponse sendResponse = telegramBot.execute(sendMessage);
            Integer messageId = sendResponse.message().messageId();
            deleteMessageService.deleteMessageAll(telegramBot, telegramUser);
            deleteMessageService.archivedForDeletingMessages(telegramUser, message.messageId(), BotConstant.START_DELIVERY_MESSAGE);
            deleteMessageService.archivedForDeletingMessages(telegramUser, messageId, "startDelivery");
        } catch (Exception e) {
            logErrorFile.logError(e, "startDelivery", message.messageId(), telegramUser.getChatId());

        }
    }

    public void startDeliveryAndShareOrders(TelegramUser telegramUser) {
        Courier courier = courierRepository.findByUserId(telegramUser.getUser().getId());
        List<CurrentOrders> sortedOrders = currentOrdersRepository.findSortedOrders(courier.getId(), OrderStatus.ASSIGNED, OrderStatus.NEXT_ORDER);
        CurrentOrders currentOrders = sortedOrders.get(0);
        Order order = currentOrders.getOrder();
        String messageTxt = generateOrderMessage(order);
        SendMessage sendMessage = new SendMessage(telegramUser.getChatId(), messageTxt);
        sendMessage.replyMarkup(botUtils.generateStartDelivered());
        telegramUser.setState(TelegramState.START_DELIVERED);
        SendResponse sendResponse = telegramBot.execute(sendMessage);
        Integer messageId = sendResponse.message().messageId();
        deleteMessageService.deleteMessageAll(telegramBot, telegramUser);
        deleteMessageService.archivedForDeletingMessages(telegramUser, messageId, messageTxt);
        telegramUserRepository.save(telegramUser);
    }

    public String generateOrderMessage(Order order) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Buyurtma:")
                .append("\n")
                .append("Tel: ").append(order.getPhone()).append("\n")
                .append("Maxsulotlar: ").append(order.getPhone()).append("\n");
        List<OrderProduct> orderProducts = orderProductRepository.findByOrderId(order.getId());
        for (OrderProduct orderProduct : orderProducts) {
            BottleTypes product = orderProduct.getBottleTypes();
            stringBuilder
                    .append(product.getType()).append(":").append(orderProduct.getAmount()).append(" ta").append("\n");
        }
        stringBuilder.append("Lokatsiya: ").append(botUtils.makeLinkFromLocation(order.getLocation()));
        return stringBuilder.toString();
    }

    @Transactional
    public void chooseDeliveryTime(CallbackQuery message, TelegramUser telegramUser) {
//        try {
//            Integer deliveryTimeId = Integer.parseInt(message.data());
//            DeliveryTime deliveryTime = deliveryTimeRepository.findById(deliveryTimeId).orElse(null);
//
//            if (deliveryTime == null) {
//                sendTelegramMessage(telegramUser, "❌ Noto'g'ri yetkazish vaqti.");
//                return;
//            }
//
//            telegramUser.setCurrentOrderDeliveryTime(deliveryTime);
//            telegramUserRepository.save(telegramUser);
//
//            List<Order> orders = findCourierOrders(telegramUser, deliveryTime);
//
//            if (orders.isEmpty()) {
//                sendTelegramMessage(telegramUser, "❌ Bu vaqtda buyurtma yo'q.");
//                startDeliveryAndShareOrders(telegramUser);
//                return;
//            }
//
//            if (deliveryTime.getDay().equals("Ertaga")) {
//                processTomorrowOrders(telegramUser, orders, message);
//                return;
//            }
//
//            if (deliveryTime.getDay().equals("Bugun")) {
//                processTodayOrders(telegramUser, orders, deliveryTime, message);
//            }
//
//        } catch (Exception e) {
//            logErrorFile.logError(e, "chooseDeliveryTime", telegramUser.getChatId());
//        }
    }

    private List<Order> findCourierOrders(TelegramUser telegramUser, DeliveryTime deliveryTime) {
        Courier courier = courierRepository.findByUserId(telegramUser.getUser().getId());
        return orderRepository.findAllByCourierIdAndOrderStatusInAndDeliveryTime(
                courier.getId(),
                List.of(OrderStatus.ASSIGNED),
                deliveryTime
        );
    }

    private void processTomorrowOrders(TelegramUser telegramUser, List<Order> orders, CallbackQuery messageText) {
        SendMessage sendMessage = new SendMessage(telegramUser.getChatId(), sendOrders(orders));
        SendResponse sendResponse = telegramBot.execute(sendMessage);
        Integer messageId = sendResponse.message().messageId();
        deleteMessageService.archivedForDeletingMessages(telegramUser, messageId, "Ertaga buyurtmalari");
        startDelivery(messageText.message(), telegramUser);
        telegramUserRepository.save(telegramUser);
    }

    private void sendOrdersToDelivery(TelegramUser telegramUser, List<Order> orders) {
        SendMessage sendMessage = new SendMessage(telegramUser.getChatId(), sendOrders(orders));
        sendMessage.replyMarkup(botUtils.generateStartDelivered());
        telegramUser.setState(TelegramState.START_DELIVERED);
        SendResponse sendResponse = telegramBot.execute(sendMessage);
        Integer messageId = sendResponse.message().messageId();
        deleteMessageService.deleteMessageAll(telegramBot, telegramUser);
        deleteMessageService.archivedForDeletingMessages(telegramUser, messageId, sendOrders(orders));
        telegramUserRepository.save(telegramUser);

    }


    public String sendOrders(List<Order> orders) {
        StringBuilder result = new StringBuilder();
        Map<LocalDate, List<Order>> dateOrderMap = orders.stream()
                .filter(order -> order.getOrderStatus().equals(OrderStatus.ASSIGNED) || order.getOrderStatus().equals(OrderStatus.NEXT_ORDER))
                .collect(Collectors.groupingBy(Order::getDay));
        dateOrderMap.forEach((date, dateOrders) -> {
            Map<String, Map<String, Long>> districtOrderMap = dateOrders.stream()
                    .collect(Collectors.groupingBy(order -> order.getDistrict().getName(),
                            Collectors.groupingBy(order -> order.getDeliveryTime().toString(),
                                    Collectors.counting()
                            )
                    ));
            result.append("📅 Sana: ").append(date).append("\n");
            districtOrderMap.forEach((district, timeMap) -> {
                long totalOrders = timeMap.values().stream().mapToLong(Long::longValue).sum();
                result.append("📍 ").append(district).append("  ").append(totalOrders).append(" ta buyurtma\n");
                timeMap.forEach((timeRange, count) -> {
                    result.append("    🕙 ").append(timeRange);
                    String statusIcons = dateOrders.stream()
                            .filter(order -> order.getDeliveryTime().toString().equals(timeRange) && order.getDistrict().getName().equals(district))
                            .map(order -> order.getOrderStatus() == OrderStatus.ASSIGNED ? "  ✅" : "  📴")
                            .distinct()
                            .collect(Collectors.joining(" "));
                    result.append(statusIcons).append("\n");
                });
            });
        });
        return result.toString();
    }


    public void shareLocationDelivery(CallbackQuery message, TelegramUser telegramUser) {
        if (telegramUser.getState().equals(TelegramState.START_DELIVERED)) {
            if (message.data().equals(BotConstant.BACK)) {
                deliveryMenu(telegramUser);
            } else if (message.data().equals(BotConstant.START_DELIVERED)) {

            }
        }
//        else {
//            SendMessage sendMessage = new SendMessage(
//                    telegramUser.getChatId(),
//                    BotConstant.SHARE_LOCATION
//            );
//            sendMessage.replyMarkup(botUtils.getGeneratedLocationButton());
//            telegramUser.setState(TelegramState.SHARE_LOCATION_DELIVERY);
//            SendResponse sendResponse = telegramBot.execute(sendMessage);
//            Integer messageId = sendResponse.message().messageId();
//            deleteMessageService.archivedForDeletingMessages(telegramUser, messageId, BotConstant.SHARE_LOCATION);
//            telegramUserRepository.save(telegramUser);
//        }
    }

    public void deliveryMenu(Message message, TelegramUser telegramUser) {
        SendMessage sendMessage = new SendMessage(
                telegramUser.getChatId(),
                BotConstant.START_DELIVERY_MESSAGE);
        sendMessage.replyMarkup(botUtils.getStartedDelivery());
        SendResponse sendResponse = telegramBot.execute(sendMessage);
        Integer messageId = sendResponse.message().messageId();
        deleteMessageService.archivedForDeletingMessages(telegramUser, message.messageId(), BotConstant.START_DELIVERY_MESSAGE);
        deleteMessageService.deleteMessageAll(telegramBot, telegramUser);
        deleteMessageService.archivedForDeletingMessages(telegramUser, messageId, BotConstant.START_DELIVERY_MESSAGE);
        telegramUser.setState(TelegramState.START_DELIVERY);
        telegramUserRepository.save(telegramUser);
    }

    public void deliveryMenu(TelegramUser telegramUser) {
        SendMessage sendMessage = new SendMessage(
                telegramUser.getChatId(),
                BotConstant.START_DELIVERY_MESSAGE);
        sendMessage.replyMarkup(botUtils.getStartedDelivery());
        SendResponse sendResponse = telegramBot.execute(sendMessage);
        Integer messageId = sendResponse.message().messageId();
        deleteMessageService.deleteMessageAll(telegramBot, telegramUser);
        deleteMessageService.archivedForDeletingMessages(telegramUser, messageId, BotConstant.START_DELIVERY_MESSAGE);
        telegramUser.setState(TelegramState.START_DELIVERY);
        telegramUserRepository.save(telegramUser);
    }

    public void startedDelivery(CallbackQuery message, TelegramUser telegramUser) {
        try {
            String callbackData = message.data();
            String[] split = callbackData.split("_");


            if (split.length < 2) {
                sendTelegramMessage(telegramUser, "❌ Noto'g'ri buyurtma ma'lumotlari.");
                logErrorFile.logError(new Exception("Noto'g'ri buyurtma ma'lumotlari."), "startedDelivery", telegramUser.getChatId());
                return;
            }

            String orderIdStr = split[0];
            Long orderId = Long.parseLong(split[1]);

            Optional<CurrentOrders> currentOrdersOpt = currentOrdersRepository.findByOrderId(orderId);
            if (currentOrdersOpt.isEmpty()) {
                sendTelegramMessage(telegramUser, "❌ Buyurtma topilmadi.");
                return;
            }

            CurrentOrders currentOrders = currentOrdersOpt.get();
            if (orderIdStr.equals(BotConstant.START_DELIVERED_ORDER)) {
                editingReplayMessage(telegramUser, currentOrders);
            } else if (orderIdStr.equals(BotConstant.PHONE_OFF)) {
                currentOrders.getOrder().setOrderStatus(OrderStatus.WAITING_PHONE);
                currentOrders.setWaitingTime(LocalDateTime.now());
                currentOrdersRepository.save(currentOrders);
                sendTelegramMessage(currentOrders.getOrder().getTelegramUser(),
                        BotConstant.NO_PHONE_CONNECTION);

                saveLocationDeliverySendMessage(message.message(), telegramUser);
                return;
            } else if (orderIdStr.equals(BotConstant.WAITING_PHONE)) {
                currentOrders.getOrder().setOrderStatus(OrderStatus.WAITING_PHONE);
                currentOrders.setWaitingTime(LocalDateTime.now());
                currentOrdersRepository.save(currentOrders);
                sendTelegramMessage(currentOrders.getOrder().getTelegramUser(),
                        BotConstant.NO_PHONE_CONNECTION);
                return;
            } else if (orderIdStr.equals(BotConstant.PAYMENT_DONE)) {
                completedOrder(message, telegramUser, currentOrders);
                return;
            }

        } catch (NumberFormatException e) {
            sendTelegramMessage(telegramUser, "❌ Noto'g'ri buyurtma ID.");
        } catch (Exception e) {
            logErrorFile.logError(e, "startedDelivery", telegramUser.getChatId());
            sendTelegramMessage(telegramUser, "❌ Xatolik yuz berdi, keyinroq qayta urinib ko'ring.");
        }
    }


    private void completedOrder(CallbackQuery message, TelegramUser telegramUser, CurrentOrders currentOrders) {
        try {
            List<OrderProduct> orderProducts = orderProductRepository.findByOrderId(currentOrders.getOrder().getId());
            StringBuilder productDetails = new StringBuilder("📜 Mahsulotlar ro‘yxati:\n");

            double totalOriginalPrice = 0.0;
            double totalDiscountedPrice = 0.0;
            boolean isDiscounted = false;

            for (OrderProduct orderProduct : orderProducts) {
                if (orderProduct.getBottleTypes().isReturnable()) {
                    currentOrders.getOrder().getTelegramUser().getUser().setLastBottleCount(orderProduct.getAmount());
                }
                double productPrice = orderProduct.getBottleTypes().getPrice();
                int productAmount = orderProduct.getAmount();
                double productTotalPrice = productPrice * productAmount;

                if (orderProduct.getBottleTypes().getSale_active() && orderProduct.getAmount() >= orderProduct.getBottleTypes().getSale_discount()) {
                    isDiscounted = true;
                    productAmount = orderProduct.getAmount() - orderProduct.getBottleTypes().getSale_amount();
                    double discountedTotalPrice = productAmount * productPrice;
                    totalDiscountedPrice += discountedTotalPrice;

                    productDetails.append("🔹 <b>")
                            .append(orderProduct.getBottleTypes().getType())
                            .append("</b> - <i>")
                            .append(orderProduct.getAmount())
                            .append(" ta</i>, Narxi: <b>")
                            .append(orderProduct.getBottleTypes().getPrice())
                            .append(" so'm</b>\n");
                } else {
                    totalDiscountedPrice += productTotalPrice;
                    productDetails.append("🔹 <b>")
                            .append(orderProduct.getBottleTypes().getType())
                            .append("</b> - <i>")
                            .append(orderProduct.getAmount())
                            .append(" ta</i>, Narxi: <b>")
                            .append(orderProduct.getBottleTypes().getPrice())
                            .append(" so'm</b>\n");
                }

                totalOriginalPrice += productTotalPrice;
            }

            currentOrders.getOrder().setTotalPrice(totalDiscountedPrice);
            String finalMessage;

            if (isDiscounted) {
                finalMessage = "✅ <b>Buyurtmangiz muvaffaqiyatli yakunlandi!</b> \n\n" +
                        productDetails +
                        "\n💰 Avvalgi narx: <b>" + totalOriginalPrice + " so'm</b>\n" +
                        "💰 Chegirmadan keyingi narx: <b>" + totalDiscountedPrice + " so'm</b>\n" +
                        "🕒 Buyurtma vaqti: <b>" + botUtils.getTimeRange(currentOrders) + "</b>\n\n" +
                        "👏 <b>Bizni tanlaganingiz uchun tashakkur!</b>";
            } else {
                finalMessage = "✅ <b>Buyurtmangiz muvaffaqiyatli yakunlandi!</b> \n\n" +
                        productDetails +
                        "\n💰 Umumiy narx: <b>" + totalOriginalPrice + " so'm</b>\n" +
                        "🕒 Buyurtma vaqti: <b>" + botUtils.getTimeRange(currentOrders) + "</b>\n\n" +
                        "👏 <b>Bizni tanlaganingiz uchun tashakkur!</b>";
            }

            SendMessage message1 = new SendMessage(currentOrders.getOrder().getTelegramUser().getChatId(), finalMessage);
            message1.parseMode(ParseMode.HTML);
            telegramBot.execute(message1);

            currentOrders.getOrder().setOrderStatus(OrderStatus.COMPLETED);
            currentOrders.setFinishTime(LocalDateTime.now());
            currentOrdersRepository.save(currentOrders);
            currentOrders.getOrder().getTelegramUser().setState(TelegramState.CABINET);
            telegramUserRepository.save(currentOrders.getOrder().getTelegramUser());
            orderRepository.save(currentOrders.getOrder());
            botService.sendCabinet(currentOrders.getOrder().getTelegramUser());
            saveLocationDeliverySendMessage(message.message(), telegramUser);
        } catch (Exception e) {
            logErrorFile.logError(e, "completedOrder", message.message().messageId(), telegramUser.getChatId());
        }
    }


    private void editingReplayMessage(TelegramUser telegramUser, CurrentOrders currentOrders) {
        try {
            if (currentOrders != null && currentOrders.getOrder() != null && currentOrders.getOrder().getLocation() != null) {
                currentOrders.getOrder().setOrderStatus(OrderStatus.DELIVERING);
                currentOrders.setStartTime(LocalDateTime.now());
                CurrentOrders savedCurrentOrders = currentOrdersRepository.save(currentOrders);
                Long chatId = telegramUser.getChatId();
                Integer messageId = telegramUser.getEditingCurrentMessageId();

                if (chatId != null && messageId != null) {
                    String orderDetails = sendClosestOrderDetails(currentOrders);
                    EditMessageText editMessageText = new EditMessageText(
                            chatId,
                            messageId,
                            orderDetails
                    );
                    editMessageText.parseMode(ParseMode.Markdown);
                    editMessageText.replyMarkup(botUtils.getFunctionButtons(savedCurrentOrders));

                    sendTelegramMessage(currentOrders.getOrder().getTelegramUser(),
                            BotConstant.START_DELIVERING_ORDER);

                    telegramBot.execute(editMessageText);
                } else {
                    sendTelegramMessage(telegramUser, "❌ Buyurtma topilmadi.");
                }
            } else {
                sendTelegramMessage(telegramUser, "❌ Buyurtma topilmadi.");
            }
        } catch (Exception e) {
            logErrorFile.logError(e, "editingReplayMessage", telegramUser.getEditingCurrentMessageId(), telegramUser.getChatId());
        }
    }


    @Transactional
    public void saveLocationDeliverySendMessage(Message message, TelegramUser telegramUser) {
        try {
            if (message.location() != null) {
                if (saveLocationFunction(message, telegramUser)) {
                    deleteMessageService.archivedForDeletingMessages(telegramUser, message.messageId(), message.text());
                    return;
                }
            }

            Courier courier = courierRepository.findByUserId(telegramUser.getUser().getId());
            List<CurrentOrders> currentOrders = currentOrdersRepository.findSortedOrders(
                    courier.getId(),
                    OrderStatus.ASSIGNED,
                    OrderStatus.WAITING_PHONE
            );

            if (!currentOrders.isEmpty()) {
                CurrentOrders closestOrder = currentOrders.get(0);

                String messageText = sendClosestOrderDetails(closestOrder);
                SendMessage sendMessage = new SendMessage(telegramUser.getChatId(), messageText);
                sendMessage.parseMode(ParseMode.Markdown);
                sendMessage.replyMarkup(botUtils.getFunctionButtons(closestOrder));
                telegramUser.setState(TelegramState.DELIVERY_STARTED);
                SendResponse messageId = telegramBot.execute(sendMessage);
                Integer messagedId = messageId.message().messageId();
                telegramUser.setEditingCurrentMessageId(messagedId);
                telegramUserRepository.save(telegramUser);

            } else {
                sendTelegramMessage(telegramUser, "🚫 Barcha buyurtmalar tugallandi.");
                telegramUser.setCurrentOrderCount(0);
                telegramUserRepository.save(telegramUser);
                startDeliveryAndShareOrders(telegramUser);
            }

        } catch (Exception e) {
            logErrorFile.logError(e, "saveLocationDeliverySendMessage", message.messageId(), telegramUser.getChatId());
        }
    }


    private boolean saveLocationFunction(Message message, TelegramUser telegramUser) {
        try {
            Location location = Location.builder()
                    .latitude(message.location().latitude().doubleValue())
                    .longitude(message.location().longitude().doubleValue())
                    .build();
            telegramUser.setLocation(location);
            telegramUser.setCurrentOrderCount(0);
            telegramUserRepository.save(telegramUser);

            Courier courier = courierRepository.findByUserId(telegramUser.getUser().getId());
            if (courier != null) {
                List<Order> orders = orderRepository.findAllByCourierIdAndOrderStatusAndDeliveryTime(
                        courier.getId(), OrderStatus.ASSIGNED, telegramUser.getCurrentOrderDeliveryTime());

                if (orders.isEmpty()) {
                    sendTelegramMessage(telegramUser, "🚫 Bu vaqtda buyurtma yo'q.");
                    startDeliveryAndShareOrders(telegramUser);
                    return true;
                }

                List<Order> optimizedOrders = optimizeOrderRoute(orders, telegramUser.getLocation());
                if (optimizedOrders.isEmpty()) {
                    sendTelegramMessage(telegramUser, "🚫 Buyurtma topilmadi.");
                    startDeliveryAndShareOrders(telegramUser);
                    return true;
                }
                AtomicInteger orderCount = new AtomicInteger(0);
                optimizedOrders.forEach(order -> {
                    CurrentOrders currentOrder = new CurrentOrders();
                    currentOrder.setOrder(order);
                    currentOrder.setOrderCount(orderCount.getAndIncrement());
                    currentOrdersRepository.save(currentOrder);
                });
            }
            return false;
        } catch (Exception e) {
            logErrorFile.logError(e, "saveLocationFunction", message.messageId(), telegramUser.getChatId());
        }
        return false;
    }


    private List<Order> optimizeOrderRoute(List<Order> orders, Location courierLocation) {
        List<Location> orderLocations = orders.stream()
                .map(Order::getLocation)
                .collect(Collectors.toList());
        RouteDetails routeDetails = distanceUtil.getOptimizedRouteDetails(orderLocations, courierLocation);
        if (routeDetails == null || routeDetails.getWaypointOrder().length == 0) {
            logErrorFile.logError(new Exception("Route details is null"), "optimizeOrderRoute", orders.get(0).getId());
            return orders;
        }
        List<Order> optimizedOrders = new ArrayList<>();
        for (int index : routeDetails.getWaypointOrder()) {
            optimizedOrders.add(orders.get(index));
        }
        return optimizedOrders;
    }


    private String sendClosestOrderDetails(CurrentOrders closestOrder) {
        String locationInfo = "";
        String locationUrl = "";

        if (closestOrder.getOrder().getOrderStatus().equals(OrderStatus.DELIVERING)) {
            locationUrl = "https://www.google.com/maps?q=" + closestOrder.getOrder().getLocation().getLatitude() + "," + closestOrder.getOrder().getLocation().getLongitude();
            locationInfo = String.format("📍 Location: [Manzilga o'tish](%s)\n", locationUrl);
        }

        String bottleInfo;
        if (!closestOrder.getOrder().getTelegramUser().getUser().getNewUser()) {
            bottleInfo = String.format("🔴 Qaytariladi: %d ta idish", closestOrder.getOrder().getTelegramUser().getUser().getLastBottleCount());
        } else {
            bottleInfo = "🙋‍♂️ Yangi mijoz";
        }

        List<OrderProduct> orderProducts = orderProductRepository.findByOrderId(closestOrder.getOrder().getId());

        StringBuilder productsInfo = new StringBuilder("🛒 Mahsulotlar:\n");
        for (OrderProduct orderProduct : orderProducts) {
            String discountInfo = "";
            if (orderProduct.getBottleTypes().getSale_active()) {
                int saleAmount = orderProduct.getBottleTypes().getSale_amount();
                discountInfo = String.format(" (Chegirma: %d ta)", saleAmount);
            }
            productsInfo.append(String.format(
                    "   - %s: %d ta%s\n",
                    orderProduct.getBottleTypes().getType(),
                    orderProduct.getAmount(),
                    discountInfo
            ));
        }

        return String.format(
                "✅ Eng yaqin buyurtma: %s %s\n" +
                        "%s" +
                        "🕐 Buyurtma vaqti: %s\n" +
                        "📍 Manzil: %s\n" +
                        "%s" +
                        "💰 Jami narxi: %d so'm\n" +
                        "☎️ Telefon raqami: %s\n" +
                        "%s",
                closestOrder.getOrder().getTelegramUser().getUser().getFirstName(),
                closestOrder.getOrder().getTelegramUser().getUser().getLastName(),
                productsInfo.toString(),
                botUtils.getTimeRange(closestOrder),
                closestOrder.getOrder().getTelegramUser().getAddressLine(),
                locationInfo,
                getPrice(closestOrder),
                closestOrder.getOrder().getTelegramUser().getUser().getDisplayPhone(),
                bottleInfo
        );
    }

    private int getPrice(CurrentOrders closestOrder) {
        List<OrderProduct> orderProducts = orderProductRepository.findByOrderId(closestOrder.getOrder().getId());
        int price = 0;
        for (OrderProduct orderProduct : orderProducts) {
            if (orderProduct.getBottleTypes().getSale_active()) {
                int saleAmount = orderProduct.getBottleTypes().getSale_amount();
                int discountedQuantity = Math.max(0, orderProduct.getAmount() - saleAmount);
                price += discountedQuantity * orderProduct.getBottleTypes().getPrice();
            } else {
                price += orderProduct.getAmount() * orderProduct.getBottleTypes().getPrice();
            }
        }
        return price;
    }


    private void sendTelegramMessage(TelegramUser telegramUser, String message) {
        SendMessage sendMessage = new SendMessage(telegramUser.getChatId(), message);
        SendResponse sendResponse = telegramBot.execute(sendMessage);
        Integer messageId = sendResponse.message().messageId();
        deleteMessageService.archivedForDeletingMessages(telegramUser, messageId, message);
    }

    /// /        return currentOrder.getOrder().getBottleCount() * currentOrder.getOrder().getBottleTypes().getPrice();
//    }
    @Autowired
    public void setDeleteMessageService(DeleteMessageService deleteMessageService) {
        this.deleteMessageService = deleteMessageService;
    }

}
