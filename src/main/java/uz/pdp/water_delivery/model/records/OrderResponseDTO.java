package uz.pdp.water_delivery.model.records;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import uz.pdp.water_delivery.model.dto.Location;
import uz.pdp.water_delivery.model.enums.OrderStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponseDTO(
        @JsonProperty("id")
        Long id,

        @JsonProperty("status")
        OrderStatus status,

        @JsonProperty("location")
        Location location,

        @JsonProperty("day")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        @JsonSerialize(using = LocalDateSerializer.class)
        @JsonDeserialize(using = LocalDateDeserializer.class)
        LocalDate day,

        @JsonProperty("date")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        @JsonSerialize(using = LocalDateTimeSerializer.class)
        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        LocalDateTime date,

        @JsonProperty("phone")
        String phone,

        @JsonProperty("products")
        List<OrderProductDto> products
) {
    public boolean isValid() {
        return id != null && location != null &&
                location.getLatitude() != null &&
                location.getLongitude() != null;
    }

    public boolean hasValidLocation() {
        if (location == null) return false;
        Double lat = location.getLatitude();
        Double lon = location.getLongitude();
        return lat != null && lon != null &&
                lat >= -90 && lat <= 90 &&
                lon >= -180 && lon <= 180;
    }
}