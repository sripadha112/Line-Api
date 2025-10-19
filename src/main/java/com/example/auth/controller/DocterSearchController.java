package com.example.auth.controller;

import com.example.auth.dto.DoctorSearchDto;
import com.example.auth.dto.DoctorSearchResponseDto;
import com.example.auth.service.DoctorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
public class DocterSearchController {

    private final DoctorService doctorService;

    public DocterSearchController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<DoctorSearchDto>> searchDoctors(@RequestParam("keyword") String keyword) {
        return ResponseEntity.ok(doctorService.searchDoctors(keyword));
    }
    
    @GetMapping("/search/enhanced")
    public ResponseEntity<List<DoctorSearchResponseDto>> enhancedSearchDoctors(
            @RequestParam("keyword") String keyword) {
        List<DoctorSearchResponseDto> results = doctorService.enhancedSearchDoctors(keyword);
        return ResponseEntity.ok(results);
    }
    
    @GetMapping("/search/nearby")
    public ResponseEntity<List<DoctorSearchResponseDto>> findNearbyDoctors(
            @RequestParam("location") String location) {
        List<DoctorSearchResponseDto> results = doctorService.findNearbyDoctors(location);
        return ResponseEntity.ok(results);
    }
}


