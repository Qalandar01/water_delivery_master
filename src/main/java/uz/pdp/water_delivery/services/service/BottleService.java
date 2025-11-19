package uz.pdp.water_delivery.services.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uz.pdp.water_delivery.dto.BottleEditView;
import uz.pdp.water_delivery.dto.BottleTypeDTO;
import uz.pdp.water_delivery.dto.request.GiftWaterRequest;
import uz.pdp.water_delivery.entity.Product;
import uz.pdp.water_delivery.repo.OrderProductRepository;
import uz.pdp.water_delivery.repo.ProductRepository;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BottleService {

    private final ProductRepository productRepository;
    private final OrderProductRepository orderProductRepository;

    public List<Product> getActiveProductsWithOrderCount() {
        List<Product> products = productRepository.findAllByActiveTrue();

        for (Product product : products) {
            long orderCount = orderProductRepository.countByProduct(product);
            product.setOrderCount(orderCount);
        }

        return products;
    }

    public void updateGiftWater(GiftWaterRequest req) {

        validateRequest(req);

        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Bunday ID ga ega butilka topilmadi!"));

        product.setSale_amount(req.getSaleAmount());
        product.setSale_discount(req.getSaleDiscount());
        product.setSale_active(Boolean.TRUE.equals(req.getSaleActive()));

        if (Boolean.TRUE.equals(req.getSaleActive())) {
            product.setSale_startDate(req.getSaleStartTime());
            product.setSale_endDate(req.getSaleEndTime());
        } else {
            product.setSale_startDate(null);
            product.setSale_endDate(null);
        }

        productRepository.save(product);
    }

    private void validateRequest(GiftWaterRequest req) {

        if (req.getSaleDiscount() < 0 || req.getSaleDiscount() > 100) {
            throw new IllegalArgumentException("Chegirma 0-100 orasida bo'lishi kerak.");
        }

        if (Boolean.TRUE.equals(req.getSaleActive())) {

            if (req.getSaleStartTime() == null || req.getSaleEndTime() == null) {
                throw new IllegalArgumentException("Chegirma aktiv bo'lsa, sana majburiy.");
            }

            if (!req.getSaleStartTime().isBefore(req.getSaleEndTime())) {
                throw new IllegalArgumentException("Tugash sanasi boshlanish sanasidan katta bo'lishi kerak.");
            }
        }
    }

    public void deleteDiscount(Long id) {

        Product bottleType = productRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Bunday ID ga ega butilka topilmadi!")
                );

        clearDiscountFields(bottleType);

        productRepository.save(bottleType);
    }

    private void clearDiscountFields(Product bottleType) {
        bottleType.setSale_amount(null);
        bottleType.setSale_discount(null);
        bottleType.setSale_active(false);
        bottleType.setSale_startDate(null);
        bottleType.setSale_endDate(null);
    }

    @Transactional
    public void createBottle(BottleTypeDTO dto) throws IOException {

        String type = dto.getType().trim();

        if (productRepository.existsByType(type)) {
            throw new IllegalArgumentException("Bunday idish turi mavjud!");
        }

        Product bottle = new Product();
        bottle.setType(type);
        bottle.setPrice(dto.getPrice());
        bottle.setActive(dto.isActive());
        bottle.setDescription(dto.getDescription());
        bottle.setReturnable(dto.isReturnable());

        MultipartFile file = dto.getImage();
        if (file != null && !file.isEmpty()) {
            bottle.setImage(file.getBytes());
        }

        productRepository.save(bottle);
    }

    public BottleEditView getBottleEditView(Long id) {
        Product bottleType = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Bottle not found"));

        BottleTypeDTO dto = mapToDTO(bottleType);
        String base64Image = encodeBase64(bottleType.getImage());

        return new BottleEditView(dto, base64Image);
    }

    private BottleTypeDTO mapToDTO(Product entity) {
        BottleTypeDTO dto = new BottleTypeDTO();
        dto.setId(entity.getId());
        dto.setType(entity.getType());
        dto.setDescription(entity.getDescription());
        dto.setPrice(entity.getPrice());
        dto.setActive(entity.isActive());
        dto.setReturnable(entity.isReturnable());
        return dto;
    }

    private String encodeBase64(byte[] image) {
        return Base64.getEncoder().encodeToString(image);
    }

    @Transactional
    public void updateBottle(BottleTypeDTO dto) throws IOException {
        Product bottle = productRepository.findById(dto.getId())
                .orElseThrow(() -> new EntityNotFoundException("Bottle not found"));

        mapDtoToEntity(dto, bottle);
        updateImageIfProvided(dto, bottle);

        productRepository.save(bottle);
    }

    private void mapDtoToEntity(BottleTypeDTO dto, Product bottle) {
        bottle.setType(dto.getType().trim());
        bottle.setDescription(dto.getDescription());
        bottle.setPrice(dto.getPrice());
        bottle.setActive(dto.isActive());
        bottle.setReturnable(dto.isReturnable());
    }

    private void updateImageIfProvided(BottleTypeDTO dto, Product bottle) throws IOException {
        MultipartFile image = dto.getImage();
        if (image != null && !image.isEmpty()) {
            bottle.setImage(image.getBytes());
        }
    }

    @Transactional
    public void deleteBottle(Long id) {
        Product bottle = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Bottle not found."));
        productRepository.delete(bottle);
    }

}
