package com.app.auth.controller;

import com.app.auth.dto.MedicineDto;
import com.app.auth.service.MedicineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for medicine search operations.
 * Optimized for prescription writing workflow.
 */
@RestController
@RequestMapping("/api/medicines")
public class MedicineController {

    @Autowired
    private MedicineService medicineService;

    /**
     * Search medicines by prefix.
     * 
     * Endpoint: GET /api/medicines/search?query=para
     * 
     * Requirements:
     * - Query must be at least 3 characters
     * - Returns maximum 20 results
     * - Results ordered alphabetically
     * - Uses database index for performance
     * 
     * Example response:
     * [
     *   {
     *     "id": 1,
     *     "medicineName": "Paracetamol 500mg",
     *     "composition1": "Paracetamol",
     *     "packSize": "Strip of 10 tablets",
     *     "manufacturer": "Cipla Ltd"
     *   }
     * ]
     * 
     * @param query Search query (minimum 3 characters required)
     * @return List of matching medicines (max 20)
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchMedicines(@RequestParam String query) {
        try {
            List<MedicineDto> medicines = medicineService.searchMedicines(query);
            return ResponseEntity.ok(medicines);
        } catch (IllegalArgumentException e) {
            // Return error for invalid query (< 3 characters)
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Get medicine details by ID.
     * 
     * Endpoint: GET /api/medicines/{id}
     * 
     * @param id Medicine ID
     * @return Medicine details
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getMedicineById(@PathVariable Integer id) {
        try {
            MedicineDto medicine = medicineService.getMedicineById(id);
            return ResponseEntity.ok(medicine);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
}
