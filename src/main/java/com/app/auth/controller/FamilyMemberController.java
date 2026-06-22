package com.app.auth.controller;

import com.app.auth.config.AuthAccess;
import com.app.auth.config.QueryParamIdCrypto;
import com.app.auth.dto.FamilyMemberDto;
import com.app.auth.service.FamilyMemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/{userId}/family-members")
public class FamilyMemberController {

    private final FamilyMemberService familyMemberService;

    public FamilyMemberController(FamilyMemberService familyMemberService) {
        this.familyMemberService = familyMemberService;
    }

    @GetMapping
    public ResponseEntity<List<FamilyMemberDto>> list(@PathVariable("userId") String encodedUserId) {
        Long userId = QueryParamIdCrypto.decodeLong(encodedUserId);
        AuthAccess.requireSelf(userId);
        return ResponseEntity.ok(familyMemberService.getFamilyMembersForUser(userId));
    }

    @PostMapping
    public ResponseEntity<FamilyMemberDto> create(@PathVariable("userId") String encodedUserId, @RequestBody FamilyMemberDto dto) {
        Long userId = QueryParamIdCrypto.decodeLong(encodedUserId);
        AuthAccess.requireSelf(userId);
        dto.setUserId(userId);
        FamilyMemberDto created = familyMemberService.createFamilyMember(dto);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FamilyMemberDto> update(@PathVariable("userId") String encodedUserId, @PathVariable("id") String encodedId, @RequestBody FamilyMemberDto dto) {
        Long userId = QueryParamIdCrypto.decodeLong(encodedUserId);
        Long id = QueryParamIdCrypto.decodeLong(encodedId);
        AuthAccess.requireSelf(userId);
        dto.setUserId(userId);
        FamilyMemberDto updated = familyMemberService.updateFamilyMember(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable("userId") String encodedUserId, @PathVariable("id") String encodedId) {
        Long userId = QueryParamIdCrypto.decodeLong(encodedUserId);
        Long id = QueryParamIdCrypto.decodeLong(encodedId);
        AuthAccess.requireSelf(userId);
        familyMemberService.deleteFamilyMember(userId, id);
        return ResponseEntity.ok("Deleted");
    }
}
