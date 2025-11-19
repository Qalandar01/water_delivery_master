package uz.pdp.water_delivery.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import uz.pdp.water_delivery.entity.Product;
import uz.pdp.water_delivery.repo.ProductRepository;

import java.io.IOException;
import java.io.OutputStream;

@Controller
public class FileController {

    private final ProductRepository productRepository;

    public FileController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping("/admin/product/image/{id}")
    public void getImage(
            @PathVariable Long id,
            HttpServletResponse response
    ) throws IOException {

        Product product = productRepository.findById(id)
                .orElse(null);

        if (product == null || product.getImage() == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Image not found");
            return;
        }

        response.setContentType("image/jpeg");
        response.setContentLength(product.getImage().length);

        try (OutputStream os = response.getOutputStream()) {
            os.write(product.getImage());
        }
    }


}
