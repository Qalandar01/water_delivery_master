package uz.pdp.water_delivery.controller.courier;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import uz.pdp.water_delivery.services.CourierService;
import uz.pdp.water_delivery.utils.LogErrorFile;

@Controller
@RequiredArgsConstructor
public class CourierController {

    private final LogErrorFile logErrorFile;
    private final CourierService courierService;




}

