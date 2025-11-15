package uz.pdp.water_delivery.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import uz.pdp.water_delivery.entity.BottleTypes;
import uz.pdp.water_delivery.repo.BottleTypesRepository;

import java.io.IOException;
import java.io.OutputStream;

@Controller
public class BottleImageController {

    private final BottleTypesRepository bottleTypesRepository;

    public BottleImageController(BottleTypesRepository bottleTypesRepository) {
        this.bottleTypesRepository = bottleTypesRepository;
    }

    @GetMapping("/admin/bottle/image/{id}")
    public void getBottleImage(
            @PathVariable Long id,
            HttpServletResponse response
    ) throws IOException {

        BottleTypes bottleType = bottleTypesRepository.findById(id)
                .orElse(null);

        if (bottleType == null || bottleType.getImage() == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Image not found");
            return;
        }

        response.setContentType("image/jpeg");
        response.setContentLength(bottleType.getImage().length);

        try (OutputStream os = response.getOutputStream()) {
            os.write(bottleType.getImage());
        }
    }


}
