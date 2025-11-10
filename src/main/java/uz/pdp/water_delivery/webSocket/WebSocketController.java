package uz.pdp.water_delivery.webSocket;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/websocket")
@RequiredArgsConstructor
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping("/send-status")
    public void sendStatusUpdate(@RequestParam("message") String message) {
        messagingTemplate.convertAndSend("/topic/status", message);
    }
    @PostMapping("/send-orders")
    public void sendOrdersUpdate(@RequestParam("message") String message) {
        messagingTemplate.convertAndSend("/topic/orders", message);
    }
}
