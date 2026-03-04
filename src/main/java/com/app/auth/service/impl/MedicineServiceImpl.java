package com.app.auth.service.impl;

import com.app.auth.dto.MedicineDto;
import com.app.auth.entity.Medicine;
import com.app.auth.repository.MedicineRepository;
import com.app.auth.service.MedicineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MedicineServiceImpl implements MedicineService {

    @Autowired
    private MedicineRepository medicineRepository;

    @Override
    public List<MedicineDto> searchMedicines(String query) {
        // Validate query length
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Search query cannot be empty");
        }
        
        String trimmedQuery = query.trim();
        if (trimmedQuery.length() < 3) {
            throw new IllegalArgumentException("Search query must be at least 3 characters");
        }
        
        // Prepare LIKE pattern in Java (lowercase + %)
        String pattern = trimmedQuery.toLowerCase() + "%";
        
        // Perform prefix search
        List<Medicine> medicines = medicineRepository.searchByPrefix(pattern);
        
        // Convert to DTO
        return medicines.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public MedicineDto getMedicineById(Integer id) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Medicine not found with id: " + id));
        return convertToDto(medicine);
    }

    /**
     * Convert Medicine entity to MedicineDto.
     * Returns only essential fields for prescription writing.
     */
    private MedicineDto convertToDto(Medicine medicine) {
        MedicineDto dto = new MedicineDto();
        dto.setId(medicine.getId());
        dto.setMedicineName(medicine.getMedicineName());
        dto.setComposition1(medicine.getComposition1());
        dto.setComposition2(medicine.getComposition2());
        dto.setPriceInr(medicine.getPriceInr());
        dto.setManufacturer(medicine.getManufacturer());
        dto.setPackSize(medicine.getPackSize());
        dto.setType(medicine.getType());
        dto.setDoctorSpecialization(medicine.getDoctorSpecialization());
        dto.setUsageScore(medicine.getUsageScore());
        return dto;
    }
}
