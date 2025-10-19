package com.example.auth.service.impl;

import com.example.auth.dto.DoctorSearchDto;
import com.example.auth.dto.DoctorSearchResponseDto;
import com.example.auth.entity.DoctorDetails;
import com.example.auth.entity.DoctorWorkplace;
import com.example.auth.repository.DoctorRepository;
import com.example.auth.repository.DoctorWorkplaceRepository;
import com.example.auth.service.DoctorService;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;
    private final DoctorWorkplaceRepository workplaceRepository;

    public DoctorServiceImpl(DoctorRepository doctorRepository, DoctorWorkplaceRepository workplaceRepository) {
        this.doctorRepository = doctorRepository;
        this.workplaceRepository = workplaceRepository;
    }

    @Override
    public List<DoctorSearchDto> searchDoctors(String keyword) {
        System.out.println("[DEBUG] Searching for keyword: '" + keyword + "'");
        
        // First, let's try to get all doctors to see if there's data
        List<DoctorDetails> allDoctors = doctorRepository.findAllDoctors();
        System.out.println("[DEBUG] Total doctors in database: " + allDoctors.size());
        
        if (!allDoctors.isEmpty()) {
            System.out.println("[DEBUG] Sample doctor names:");
            allDoctors.stream().limit(5).forEach(d -> 
                System.out.println("  - ID: " + d.getId() + ", Name: '" + d.getFullName() + "'")
            );
        }
        
        List<DoctorDetails> doctors = doctorRepository.searchDoctors(keyword);
        System.out.println("[DEBUG] Search results count: " + doctors.size());
        
        return doctors.stream().map(d -> {
            DoctorSearchDto dto = new DoctorSearchDto();
            dto.setId(d.getId());
            dto.setFullName(d.getFullName());
            dto.setSpecialization(d.getSpecialization());
            dto.setDesignation(d.getDesignation());
            dto.setAddress(d.getAddress());
            dto.setPincode(d.getPincode());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<DoctorSearchResponseDto> enhancedSearchDoctors(String keyword) {
        System.out.println("[DEBUG] Enhanced search for keyword: '" + keyword + "'");
        
        List<DoctorSearchResponseDto> results = new ArrayList<>();
        Map<Long, DoctorSearchResponseDto> doctorMap = new HashMap<>();
        
        // Determine search type
        boolean isNumericId = isNumeric(keyword);
        
        // 1. Search by doctor ID or doctor name (return all workspaces)
        if (isNumericId) {
            Long doctorId = Long.parseLong(keyword);
            DoctorDetails doctor = doctorRepository.findByDoctorId(doctorId);
            if (doctor != null) {
                DoctorSearchResponseDto dto = createDoctorDto(doctor);
                dto.setWorkplaces(getAllWorkplacesForDoctor(doctorId));
                results.add(dto);
                return results;
            }
        }
        
        // Search by doctor name, specialization, designation, and clinic name (return all workspaces)
        List<DoctorDetails> doctorsBySearch = doctorRepository.searchDoctors(keyword);
        System.out.println("[DEBUG] Found " + doctorsBySearch.size() + " doctors by comprehensive search");
        for (DoctorDetails doctor : doctorsBySearch) {
            System.out.println("[DEBUG] Doctor found: " + doctor.getFullName() + " - " + doctor.getSpecialization());
            DoctorSearchResponseDto dto = createDoctorDto(doctor);
            dto.setWorkplaces(getAllWorkplacesForDoctor(doctor.getId()));
            doctorMap.put(doctor.getId(), dto);
        }
        
        // 2. Search by hospital name (return only matching workspaces)
        List<DoctorWorkplace> workplacesByName = workplaceRepository.findByWorkplaceNameContaining(keyword);
        for (DoctorWorkplace workplace : workplacesByName) {
            DoctorDetails doctor = workplace.getDoctor();
            DoctorSearchResponseDto dto = doctorMap.computeIfAbsent(doctor.getId(), 
                k -> createDoctorDto(doctor));
            
            if (dto.getWorkplaces() == null) {
                dto.setWorkplaces(new ArrayList<>());
            }
            
            // Only add this specific workplace, not all workplaces
            if (!hasWorkplace(dto.getWorkplaces(), workplace.getId())) {
                dto.getWorkplaces().add(createWorkplaceDto(workplace));
            }
        }
        
        // 3. Search by area/pincode (return only matching workspaces)
        List<DoctorWorkplace> workplacesByArea = workplaceRepository.findByAreaOrPincode(keyword);
        for (DoctorWorkplace workplace : workplacesByArea) {
            DoctorDetails doctor = workplace.getDoctor();
            DoctorSearchResponseDto dto = doctorMap.computeIfAbsent(doctor.getId(), 
                k -> createDoctorDto(doctor));
            
            if (dto.getWorkplaces() == null) {
                dto.setWorkplaces(new ArrayList<>());
            }
            
            // Only add this specific workplace, not all workplaces
            if (!hasWorkplace(dto.getWorkplaces(), workplace.getId())) {
                dto.getWorkplaces().add(createWorkplaceDto(workplace));
            }
        }
        
        // If search by area didn't match exactly, try pincode lookup for similar areas
        if (workplacesByArea.isEmpty() && !isNumericId) {
            // This would require a service to get pincode from area name
            // For now, we'll skip this advanced feature
        }
        
        List<DoctorSearchResponseDto> finalResults = new ArrayList<>(doctorMap.values());
        System.out.println("[DEBUG] Enhanced search returning " + finalResults.size() + " results");
        return finalResults;
    }
    
    private boolean isNumeric(String str) {
        try {
            Long.parseLong(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    private DoctorSearchResponseDto createDoctorDto(DoctorDetails doctor) {
        DoctorSearchResponseDto dto = new DoctorSearchResponseDto();
        dto.setDoctorId(doctor.getId());
        dto.setDoctorName(doctor.getFullName());
        dto.setSpecialization(doctor.getSpecialization());
        dto.setDesignation(doctor.getDesignation());
        // profileImage and experience fields are not available in current entity
        dto.setProfileImage(null);
        dto.setExperience(null);
        return dto;
    }
    
    private List<DoctorSearchResponseDto.WorkplaceDto> getAllWorkplacesForDoctor(Long doctorId) {
        List<DoctorWorkplace> workplaces = workplaceRepository.findByDoctorId(doctorId);
        return workplaces.stream()
                .map(this::createWorkplaceDto)
                .collect(Collectors.toList());
    }
    
    private DoctorSearchResponseDto.WorkplaceDto createWorkplaceDto(DoctorWorkplace workplace) {
        DoctorSearchResponseDto.WorkplaceDto dto = new DoctorSearchResponseDto.WorkplaceDto();
        dto.setWorkplaceId(workplace.getId());
        dto.setWorkplaceName(workplace.getWorkplaceName());
        dto.setWorkplaceType(workplace.getWorkplaceType());
        dto.setAddress(workplace.getAddress());
        dto.setCity(workplace.getCity());
        dto.setState(workplace.getState());
        dto.setPincode(workplace.getPincode());
        dto.setCountry(workplace.getCountry());
        dto.setContactNumber(workplace.getContactNumber());
        dto.setMorningStartTime(formatTime(workplace.getMorningStartTime()));
        dto.setMorningEndTime(formatTime(workplace.getMorningEndTime()));
        dto.setEveningStartTime(formatTime(workplace.getEveningStartTime()));
        dto.setEveningEndTime(formatTime(workplace.getEveningEndTime()));
        dto.setCheckingDurationMinutes(workplace.getCheckingDurationMinutes());
        dto.setIsPrimary(workplace.getIsPrimary());
        return dto;
    }
    
    private String formatTime(LocalTime time) {
        if (time == null) return null;
        return time.format(DateTimeFormatter.ofPattern("HH:mm"));
    }
    
    private boolean hasWorkplace(List<DoctorSearchResponseDto.WorkplaceDto> workplaces, Long workplaceId) {
        return workplaces.stream().anyMatch(w -> w.getWorkplaceId().equals(workplaceId));
    }

    @Override
    public List<DoctorSearchResponseDto> findNearbyDoctors(String location) {
        Map<Long, DoctorSearchResponseDto> doctorMap = new HashMap<>();
        
        // Search workplaces by location (city, state, or pincode)
        List<DoctorWorkplace> nearbyWorkplaces = workplaceRepository.findByAreaOrPincode(location);
        
        // If direct search doesn't yield results, try exact pincode match
        if (nearbyWorkplaces.isEmpty() && isNumeric(location)) {
            nearbyWorkplaces = workplaceRepository.findByPincode(location);
        }
        
        // Group workplaces by doctor and create response DTOs
        for (DoctorWorkplace workplace : nearbyWorkplaces) {
            DoctorDetails doctor = workplace.getDoctor();
            
            // Get or create doctor DTO
            DoctorSearchResponseDto dto = doctorMap.computeIfAbsent(doctor.getId(), 
                k -> createDoctorDto(doctor));
            
            // Initialize workplaces list if null
            if (dto.getWorkplaces() == null) {
                dto.setWorkplaces(new ArrayList<>());
            }
            
            // Add only the matching workplace (not all doctor's workplaces)
            if (!hasWorkplace(dto.getWorkplaces(), workplace.getId())) {
                dto.getWorkplaces().add(createWorkplaceDto(workplace));
            }
        }
        
        return new ArrayList<>(doctorMap.values());
    }
}