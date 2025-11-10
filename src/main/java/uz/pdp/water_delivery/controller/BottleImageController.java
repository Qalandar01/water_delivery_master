package uz.pdp.water_delivery.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import uz.pdp.water_delivery.entity.BottleTypes;
import uz.pdp.water_delivery.repo.BottleTypesRepository;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;

@Controller
public class BottleImageController {

    private final BottleTypesRepository bottleTypesRepository;

    public BottleImageController(BottleTypesRepository bottleTypesRepository) {
        this.bottleTypesRepository = bottleTypesRepository;
    }

    @GetMapping("/admin/bottle/image/{id}")
    @ResponseBody
    public void getBottleImage(@PathVariable Integer id, HttpServletResponse response) throws IOException {
        Optional<BottleTypes> bottleType = bottleTypesRepository.findById(id);
        if (bottleType.isPresent() && bottleType.get().getImage() != null) {
            byte[] imageBytes = bottleType.get().getImage();
            response.setContentType("image/jpeg");
            try (OutputStream outputStream = response.getOutputStream()) {
                outputStream.write(imageBytes);
                outputStream.flush();
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

}
