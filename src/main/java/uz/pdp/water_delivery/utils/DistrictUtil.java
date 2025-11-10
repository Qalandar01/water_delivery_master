package uz.pdp.water_delivery.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uz.pdp.water_delivery.dto.Location;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class DistrictUtil {

    @Value("${distance.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public String getDistrictName(Location location) {
        String url = String.format(
                "https://maps.googleapis.com/maps/api/geocode/json?latlng=%s,%s&key=%s",
                location.getLatitude(), location.getLongitude(), apiKey);

        String response = restTemplate.getForObject(url, String.class);
        return parseDistrictFromResponse(response);
    }

    public String getFullAddress(Location location) {
        String url = String.format(
                "https://maps.googleapis.com/maps/api/geocode/json?latlng=%s,%s&key=%s",
                location.getLatitude(), location.getLongitude(), apiKey);

        String response = restTemplate.getForObject(url, String.class);
        return parseFullAddressFromResponse(response);
    }

    private String parseDistrictFromResponse(String response) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode results = root.path("results");

            if (results.isArray() && results.size() > 0) {
                JsonNode addressComponents = results.get(0).path("address_components");
                String sublocality = getComponent(addressComponents, "sublocality");

                return String.format("%s",
                        sublocality.isEmpty() ? "Topilmadi" : sublocality);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Tuman topilmadi";
    }


    private String parseFullAddressFromResponse(String response) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode results = root.path("results");

            if (results.isArray() && results.size() > 0) {
                JsonNode addressComponents = results.get(0).path("formatted_address");
                return addressComponents.asText();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Manzil topilmadi";
    }

    private String getComponent(JsonNode components, String type) {
        for (JsonNode component : components) {
            JsonNode types = component.path("types");
            if (types.isArray()) {
                for (JsonNode t : types) {
                    if (t.asText().equals(type)) {
                        String longName = component.path("long_name").asText();
                        return longName.replace(" District", "").trim();
                    }
                }
            }
        }
        return "";
    }

}
