package uz.pdp.water_delivery.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.pdp.water_delivery.dto.CompanyDTO;
import uz.pdp.water_delivery.dto.CurrentOrdersDTO;
import uz.pdp.water_delivery.dto.OrdersPageData;
import uz.pdp.water_delivery.entity.Courier;
import uz.pdp.water_delivery.entity.enums.OrderStatus;
import uz.pdp.water_delivery.entity.enums.TelegramState;
import uz.pdp.water_delivery.projection.SimpleWaitingUser;
import uz.pdp.water_delivery.repo.CourierRepository;
import uz.pdp.water_delivery.repo.CurrentOrdersRepository;
import uz.pdp.water_delivery.repo.TelegramUserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OperatorService {

    private final TelegramUserRepository telegramUserRepository;
    private final CurrentOrdersRepository currentOrdersRepository;
    private final CourierRepository courierRepository;

    public List<SimpleWaitingUser> getWaitingUsers() {
        return telegramUserRepository
                .findAllByStateInOrderByCreatedAt(List.of(TelegramState.WAITING_OPERATOR));
    }

    public Integer countUsersWithChangedLocation() {
        return telegramUserRepository.countByChangeLocation(true);
    }
    public OrdersPageData getOrdersPageData(Long courierId) {
        List<Courier> couriers = courierRepository.findAllByIsActive(true);

        Courier currentCourier = findSelectedCourier(couriers, courierId);

        List<CurrentOrdersDTO> currentOrders = currentOrdersRepository
                .findSortedOrders(OrderStatus.ASSIGNED, OrderStatus.WAITING_PHONE)
                .stream()
                .map(o -> new CurrentOrdersDTO(
                        o.getOrder().getId(),
                        o.getOrder().getLocation(),
                        o.getOrder().getCourier().getId()
                ))
                .toList();

        CompanyDTO company = new CompanyDTO(
                "Shift Academy",
                69.280697,
                41.327692
        );

        return new OrdersPageData(couriers, currentCourier, currentOrders, company, List.of());
    }

    private Courier findSelectedCourier(List<Courier> couriers, Long id) {
        if (id == null) return couriers.get(0);
        return couriers.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElse(couriers.get(0));
    }
}
