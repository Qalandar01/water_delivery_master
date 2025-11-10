package uz.pdp.water_delivery.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uz.pdp.water_delivery.dto.Location;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class DistanceUtil {

    @Value("${distance.api.key}")
    private String API_KEY;

    private final ObjectMapper objectMapper;

    public DistanceUtil() {
        this.objectMapper = new ObjectMapper();
    }

    public RouteDetails getOptimizedRouteDetails(List<Location> locations, Location from) {
        RestTemplate restTemplate = new RestTemplate();
        String url = buildDirectionsApiUrl(locations, from);
        log.debug("Request URL: {}", url);

        String response = restTemplate.getForObject(url, String.class);

        getDebug(response);

        if (response != null) {
            int[] waypointOrder = getOptimizedOrder(response);
            long totalTime = getTotalTime(response);
            String polyline = getRoutePolyline(response);
            return new RouteDetails(waypointOrder, totalTime, polyline);
        } else {
            log.error("No response received from Directions API");
            return null;
        }
    }

    private static void getDebug(String response) {
        log.debug("Full JSON response: {}", response);
    }

    private String buildDirectionsApiUrl(List<Location> locations, Location from) {
        StringBuilder url = new StringBuilder("https://api.routing.yandex.net/v2/route?");
        url.append("waypoints=").append(from.getLatitude()).append(",").append(from.getLongitude());

        for (Location location : locations) {
            url.append("|").append(location.getLatitude()).append(",").append(location.getLongitude());
        }

        url.append("&apikey=").append(API_KEY); // O'zingizning Yandex API kalitingizni kiriting
        url.append("&mode=driving"); // Yandex API uchun rejimni tanlash (masalan, "driving", "walking", yoki "bicycling")
        return url.toString();
    }


    public int[] getOptimizedOrder(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode routes = root.path("routes");

            if (routes.isArray() && routes.size() > 0) {
                JsonNode waypointOrderNode = routes.get(0).path("waypoint_order");
                if (waypointOrderNode.isMissingNode() || !waypointOrderNode.isArray()) {
                    log.error("JSON response does not contain 'waypoint_order'");
                    getDebug(jsonResponse);
                    return new int[0];
                }

                int[] waypointOrder = new int[waypointOrderNode.size()];
                for (int i = 0; i < waypointOrderNode.size(); i++) {
                    waypointOrder[i] = waypointOrderNode.get(i).asInt();
                }
                return waypointOrder;
            } else {
                log.error("No routes found in JSON response");
                getDebug(jsonResponse);
            }
        } catch (Exception e) {
            log.error("Exception while parsing waypoint order", e);
            getDebug(jsonResponse);
        }
        return new int[0];
    }

    public long getTotalTime(String jsonResponse) {
        long totalTime = 0;
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode routes = root.path("routes");
            if (routes.isArray() && routes.size() > 0) {
                JsonNode legs = routes.get(0).path("legs");
                if (legs.isArray()) {
                    for (JsonNode leg : legs) {
                        JsonNode duration = leg.path("duration").path("value");
                        if (duration.isLong() || duration.isInt()) {
                            totalTime += duration.asLong();
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Exception while parsing total time", e);
        }
        return totalTime;
    }

    public String getRoutePolyline(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode routes = root.path("routes");

            if (routes.isArray() && routes.size() > 0) {
                JsonNode overviewPolyline = routes.get(0).path("overview_polyline").path("points");
                if (!overviewPolyline.isMissingNode()) {
                    return overviewPolyline.asText();
                } else {
                    log.error("No polyline found in JSON response");
                    getDebug(jsonResponse);
                }
            }
        } catch (Exception e) {
            log.error("Exception while parsing polyline", e);
        }
        return "";
    }


}
