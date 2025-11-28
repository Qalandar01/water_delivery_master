package uz.pdp.water_delivery.services;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uz.pdp.water_delivery.model.dto.request.ProductRequestDTO;
import uz.pdp.water_delivery.model.dto.request.GiftWaterRequest;
import uz.pdp.water_delivery.model.entity.Product;
import uz.pdp.water_delivery.model.entity.ProductImage;
import uz.pdp.water_delivery.model.entity.ProductImageContent;
import uz.pdp.water_delivery.model.mapper.ProductMapper;
import uz.pdp.water_delivery.model.records.admin.ProductResponseDTO;
import uz.pdp.water_delivery.model.repo.OrderProductRepository;
import uz.pdp.water_delivery.model.repo.ProductImageContentRepository;
import uz.pdp.water_delivery.model.repo.ProductRepository;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final OrderProductRepository orderProductRepository;
    private final FileService fileService;
    private final ProductImageContentRepository productImageContentRepository;

    public List<ProductResponseDTO> getActiveProductsWithOrderCount() {

        List<Product> products = productRepository.findAllByActiveTrue();

        List<Object[]> counts = orderProductRepository.findOrderCountGroupedByProductNative();

        Map<Long, Long> countMap = counts.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));

        products.forEach(product ->
                product.setOrderCount(countMap.getOrDefault(product.getId(), 0L))
        );

        return products.stream()
                .map(ProductMapper::mapToResponse)
                .toList();
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

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Bunday ID ga ega butilka topilmadi!")
                );

        clearDiscountFields(product);

        productRepository.save(product);
    }

    private void clearDiscountFields(Product product) {
        product.setSale_amount(null);
        product.setSale_discount(null);
        product.setSale_active(false);
        product.setSale_startDate(null);
        product.setSale_endDate(null);
    }

    @Transactional
    public void createProduct(ProductRequestDTO dto) {

        String type = dto.getType().trim();

        if (productRepository.existsByType(type)) {
            throw new IllegalArgumentException("Bunday idish turi mavjud!");
        }

        Product product = new Product();
        product.setType(type);
        product.setPrice(dto.getPrice());
        product.setActive(dto.isActive());
        product.setDescription(dto.getDescription());
        product.setReturnable(dto.isReturnable());

        ProductImage  productImage = fileService.saveProductImage(dto.getImage());
        product.setProductImage(productImage);

        productRepository.save(product);
    }

    public ProductRequestDTO getProductEditView(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        return ProductMapper.mapToRequest(product);
    }



    private String encodeBase64(byte[] image) {
        return Base64.getEncoder().encodeToString(image);
    }

    @Transactional
    public void updateProduct(ProductRequestDTO dto) {
        Product product = productRepository.findById(dto.getId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        mapDtoToEntity(dto, product);
        updateImageIfProvided(dto, product);

        productRepository.save(product);
    }

    private void mapDtoToEntity(ProductRequestDTO dto, Product product) {
        product.setType(dto.getType().trim());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setActive(dto.isActive());
        product.setReturnable(dto.isReturnable());
    }

    private void updateImageIfProvided(ProductRequestDTO dto, Product product){
        MultipartFile image = dto.getImage();
        if (image != null && !image.isEmpty()) {
            product.setProductImage(fileService.saveProductImage(image));
        }
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found."));
        product.setIsDeleted(true);
        productRepository.save(product);
    }

}
