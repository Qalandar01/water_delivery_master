package uz.pdp.water_delivery.projection;

import org.springframework.beans.factory.annotation.Value;

import java.util.UUID;

public interface OrderProjection {

    UUID getId();
    @Value("#{target.district.name}")
    String getDistrictName();

    Integer getBottleCount();

    @Value("#{target.bottleTypes.type}")
    String getBottleType();

    @Value("#{target.courier!=null ? target.courier.id:null}")
    UUID getCourier();

}
