package com.app.auth.service;

import com.app.auth.dto.FamilyMemberDto;

import java.util.List;

public interface FamilyMemberService {
    List<FamilyMemberDto> getFamilyMembersForUser(Long userId);
    FamilyMemberDto createFamilyMember(FamilyMemberDto dto);
    FamilyMemberDto updateFamilyMember(Long id, FamilyMemberDto dto);
    void deleteFamilyMember(Long id);
}
