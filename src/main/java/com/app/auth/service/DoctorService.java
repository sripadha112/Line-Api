package com.app.auth.service;

import com.app.auth.dto.DoctorSearchDto;
import com.app.auth.dto.DoctorSearchResponseDto;
import java.util.List;

public interface DoctorService {
    List<DoctorSearchDto> searchDoctors(String keyword);
    List<DoctorSearchResponseDto> enhancedSearchDoctors(String keyword);
    List<DoctorSearchResponseDto> findNearbyDoctors(String location);
}
