package uz.pdp.water_delivery.services.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uz.pdp.water_delivery.dto.BottleEditView;
import uz.pdp.water_delivery.dto.BottleTypeDTO;
import uz.pdp.water_delivery.dto.request.GiftWaterRequest;
import uz.pdp.water_delivery.entity.BottleTypes;
import uz.pdp.water_delivery.repo.BottleTypesRepository;
import uz.pdp.water_delivery.repo.OrderProductRepository;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BottleService {

    private final BottleTypesRepository bottleTypesRepository;
    private final OrderProductRepository orderProductRepository;

    public List<BottleTypes> getActiveBottleTypesWithOrderCount() {
        List<BottleTypes> bottleTypes = bottleTypesRepository.findAllByActiveTrue();

        for (BottleTypes type : bottleTypes) {
            long orderCount = orderProductRepository.countByBottleTypes(type);
            type.setOrderCount(orderCount);
        }

        return bottleTypes;
    }

    public void updateGiftWater(GiftWaterRequest req) {

        validateRequest(req);

        BottleTypes bottle = bottleTypesRepository.findById(req.getBottleTypeId())
                .orElseThrow(() -> new IllegalArgumentException("Bunday ID ga ega butilka topilmadi!"));

        bottle.setSale_amount(req.getSaleAmount());
        bottle.setSale_discount(req.getSaleDiscount());
        bottle.setSale_active(Boolean.TRUE.equals(req.getSaleActive()));

        if (Boolean.TRUE.equals(req.getSaleActive())) {
            bottle.setSale_startDate(req.getSaleStartTime());
            bottle.setSale_endDate(req.getSaleEndTime());
        } else {
            bottle.setSale_startDate(null);
            bottle.setSale_endDate(null);
        }

        bottleTypesRepository.save(bottle);
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

        BottleTypes bottleType = bottleTypesRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Bunday ID ga ega butilka topilmadi!")
                );

        clearDiscountFields(bottleType);

        bottleTypesRepository.save(bottleType);
    }

    private void clearDiscountFields(BottleTypes bottleType) {
        bottleType.setSale_amount(null);
        bottleType.setSale_discount(null);
        bottleType.setSale_active(false);
        bottleType.setSale_startDate(null);
        bottleType.setSale_endDate(null);
    }

    @Transactional
    public void createBottle(BottleTypeDTO dto) throws IOException {

        String type = dto.getType().trim();

        if (bottleTypesRepository.existsByType(type)) {
            throw new IllegalArgumentException("Bunday idish turi mavjud!");
        }

        BottleTypes bottle = new BottleTypes();
        bottle.setType(type);
        bottle.setPrice(dto.getPrice());
        bottle.setActive(dto.isActive());
        bottle.setDescription(dto.getDescription());
        bottle.setReturnable(dto.isReturnable());

        MultipartFile file = dto.getImage();
        if (file != null && !file.isEmpty()) {
            bottle.setImage(file.getBytes());
        }

        bottleTypesRepository.save(bottle);
    }

    public BottleEditView getBottleEditView(Long id) {
        BottleTypes bottleType = bottleTypesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Bottle not found"));

        BottleTypeDTO dto = mapToDTO(bottleType);
        String base64Image = encodeBase64(bottleType.getImage());

        return new BottleEditView(dto, base64Image);
    }

    private BottleTypeDTO mapToDTO(BottleTypes entity) {
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
        BottleTypes bottle = bottleTypesRepository.findById(dto.getId())
                .orElseThrow(() -> new EntityNotFoundException("Bottle not found"));

        mapDtoToEntity(dto, bottle);
        updateImageIfProvided(dto, bottle);

        bottleTypesRepository.save(bottle);
    }

    private void mapDtoToEntity(BottleTypeDTO dto, BottleTypes bottle) {
        bottle.setType(dto.getType().trim());
        bottle.setDescription(dto.getDescription());
        bottle.setPrice(dto.getPrice());
        bottle.setActive(dto.isActive());
        bottle.setReturnable(dto.isReturnable());
    }

    private void updateImageIfProvided(BottleTypeDTO dto, BottleTypes bottle) throws IOException {
        MultipartFile image = dto.getImage();
        if (image != null && !image.isEmpty()) {
            bottle.setImage(image.getBytes());
        }
    }

    @Transactional
    public void deleteBottle(Long id) {
        BottleTypes bottle = bottleTypesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Bottle not found."));
        bottleTypesRepository.delete(bottle);
    }

}
