package com.app.auth.service;

import com.app.auth.dto.MedicineDto;
import java.util.List;

public interface MedicineService {
    /**
     * Search medicines by prefix.
     * Requires minimum 3 characters in query.
     * Returns maximum 20 results ordered alphabetically.
     * 
     * @param query Search query (minimum 3 characters)
     * @return List of matching medicines
     * @throws IllegalArgumentException if query length < 3
     */
    List<MedicineDto> searchMedicines(String query);
    
    /**
     * Get medicine by ID.
     * 
     * @param id Medicine ID
     * @return Medicine details
     * @throws IllegalArgumentException if medicine not found
     */
    MedicineDto getMedicineById(Integer id);
}
