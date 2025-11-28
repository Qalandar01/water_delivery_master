package uz.pdp.water_delivery.controller.file;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import uz.pdp.water_delivery.model.entity.Product;
import uz.pdp.water_delivery.repo.ProductRepository;
import uz.pdp.water_delivery.services.FileService;

import java.io.IOException;
import java.io.OutputStream;

@Controller
public class FileController {

    private final ProductRepository productRepository;
    private final FileService fileService;

    public FileController(ProductRepository productRepository, FileService fileService) {
        this.productRepository = productRepository;
        this.fileService = fileService;
    }

    @GetMapping("/admin/product/image/{id}")
    public void getImage(
            @PathVariable Long id,
            HttpServletResponse response
    ) throws IOException {

        Product product = productRepository.findById(id)
                .orElse(null);

        if (product == null || product.getProductImage() == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Image not found");
            return;
        }

        response.setContentType("image/jpeg");
        response.setContentLength(fileService.getProductImageContent(product).length);

        try (OutputStream os = response.getOutputStream()) {
            os.write(fileService.getProductImageContent(product));
        }catch (Exception ex){
            System.out.println("Error getting product image");
        }
    }


}
