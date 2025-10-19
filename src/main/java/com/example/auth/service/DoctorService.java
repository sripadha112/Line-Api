package com.example.auth.service;

import com.example.auth.dto.DoctorSearchDto;
import com.example.auth.dto.DoctorSearchResponseDto;
import java.util.List;

public interface DoctorService {
    List<DoctorSearchDto> searchDoctors(String keyword);
    List<DoctorSearchResponseDto> enhancedSearchDoctors(String keyword);
    List<DoctorSearchResponseDto> findNearbyDoctors(String location);
}
