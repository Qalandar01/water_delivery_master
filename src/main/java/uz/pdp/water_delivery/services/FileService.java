package uz.pdp.water_delivery.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import uz.pdp.water_delivery.model.entity.Product;
import uz.pdp.water_delivery.model.entity.ProductImage;
import uz.pdp.water_delivery.model.entity.ProductImageContent;
import uz.pdp.water_delivery.repo.ProductImageContentRepository;
import uz.pdp.water_delivery.repo.ProductImageRepository;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class FileService {
    private final ProductImageRepository productImageRepository;
    private final ProductImageContentRepository productImageContentRepository;

    @Transactional
    public ProductImage saveProductImage(MultipartFile imageFile) {
        try {
            if (imageFile == null || imageFile.isEmpty()) {
                throw new IllegalArgumentException("Image file cannot be null or empty");
            }

            String originalFilename = imageFile.getOriginalFilename();
            String contentType = imageFile.getContentType();

            // Create ProductImage metadata
            ProductImage productImage = ProductImage.builder()
                    .fileName(originalFilename)
                    .fileType(contentType)
                    .build();

            ProductImage savedImage = productImageRepository.save(productImage);

            // Create binary content entity
            ProductImageContent content = ProductImageContent.builder()
                    .content(imageFile.getBytes())
                    .productImage(savedImage)
                    .build();
            productImageContentRepository.save(content);


            return savedImage;

        } catch (IOException e) {
            throw new RuntimeException("Failed to read uploaded image bytes", e);
        }
    }

    public byte[] getProductImageContent(Product product) {
        return productImageContentRepository.findByProductImage_Id(product.getProductImage().getId()).get(0).getContent();

    }
}
