package uz.pdp.water_delivery.services.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.pdp.water_delivery.entity.District;
import uz.pdp.water_delivery.repo.DistrictRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DistrictService {
    private final DistrictRepository districtRepository;

    public List<District> getAllDistricts() {
        return districtRepository.getAllDistricts();
    }
}
