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
     * Search in composition_1 and composition_2 columns.
     * Uses database index for fast lookups.
     * Returns medicines where primary OR secondary composition matches the query (case-insensitive).
     * Orders results: composition_1 matches first, then by composition_1 alphabetically.
     *
     * @param pattern The search pattern (e.g., 'para%') - prepared by service layer
     * @return List of matching medicines, limited to 20 results
     */
    @Query(value = "SELECT * FROM all_medicines " +
                   "WHERE LOWER(composition_1) LIKE :pattern " +
                   "OR LOWER(composition_2) LIKE :pattern " +
                   "ORDER BY " +
                   "CASE WHEN LOWER(composition_1) LIKE :pattern THEN 0 ELSE 1 END, " +
                   "composition_1 ASC " +
                   "LIMIT 20",
           nativeQuery = true)
    List<Medicine> searchByPrefix(@Param("pattern") String pattern);
}
