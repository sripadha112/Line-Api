package com.app.auth.repository;

import com.app.auth.entity.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Integer> {

    /**
     * Search in medicine_name and composition_2 columns.
     * Uses database index for fast lookups.
     * Returns medicines where name OR composition contains the query (case-insensitive).
     * Orders results: name matches first, then by medicine name alphabetically.
     * 
     * @param pattern The search pattern (e.g., 'para%') - prepared by service layer
     * @return List of matching medicines, limited to 20 results
     */
    @Query(value = "SELECT * FROM all_medicines " +
                   "WHERE LOWER(medicine_name) LIKE :pattern " +
                   "OR LOWER(composition_2) LIKE :pattern " +
                   "ORDER BY " +
                   "CASE WHEN LOWER(medicine_name) LIKE :pattern THEN 0 ELSE 1 END, " +
                   "medicine_name ASC " +
                   "LIMIT 20",
           nativeQuery = true)
    List<Medicine> searchByPrefix(@Param("pattern") String pattern);
}
