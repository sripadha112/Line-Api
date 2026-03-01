package com.app.auth.service.impl;

import com.app.auth.dto.FamilyMemberDto;
import com.app.auth.entity.FamilyMember;
import com.app.auth.repository.FamilyMemberRepository;
import com.app.auth.service.FamilyMemberService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FamilyMemberServiceImpl implements FamilyMemberService {

    private final FamilyMemberRepository repository;

    public FamilyMemberServiceImpl(FamilyMemberRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<FamilyMemberDto> getFamilyMembersForUser(Long userId) {
        return repository.findByUserId(userId).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public FamilyMemberDto createFamilyMember(FamilyMemberDto dto) {
        FamilyMember fm = new FamilyMember();
        fm.setUserId(dto.getUserId());
        fm.setName(dto.getName());
        fm.setRelationship(dto.getRelationship());
        fm.setAge(dto.getAge());
        fm.setDob(dto.getDob());
        fm.setGender(dto.getGender());
        fm.setContact(dto.getContact());
        FamilyMember saved = repository.save(fm);
        return toDto(saved);
    }

    @Override
    public FamilyMemberDto updateFamilyMember(Long id, FamilyMemberDto dto) {
        FamilyMember fm = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Family member not found"));
        if (dto.getName() != null) fm.setName(dto.getName());
        if (dto.getRelationship() != null) fm.setRelationship(dto.getRelationship());
        if (dto.getAge() != null) fm.setAge(dto.getAge());
        if (dto.getDob() != null) fm.setDob(dto.getDob());
        if (dto.getGender() != null) fm.setGender(dto.getGender());
        if (dto.getContact() != null) fm.setContact(dto.getContact());
        FamilyMember saved = repository.save(fm);
        return toDto(saved);
    }

    @Override
    public void deleteFamilyMember(Long id) {
        repository.deleteById(id);
    }

    private FamilyMemberDto toDto(FamilyMember fm) {
        FamilyMemberDto dto = new FamilyMemberDto();
        dto.setId(fm.getId());
        dto.setUserId(fm.getUserId());
        dto.setName(fm.getName());
        dto.setAge(fm.getAge());
        dto.setRelationship(fm.getRelationship());
        dto.setDob(fm.getDob());
        dto.setGender(fm.getGender());
        dto.setContact(fm.getContact());
        return dto;
    }
}
