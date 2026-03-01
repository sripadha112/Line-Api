package com.app.auth.controller;

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
    public ResponseEntity<List<FamilyMemberDto>> list(@PathVariable Long userId) {
        return ResponseEntity.ok(familyMemberService.getFamilyMembersForUser(userId));
    }

    @PostMapping
    public ResponseEntity<FamilyMemberDto> create(@PathVariable Long userId, @RequestBody FamilyMemberDto dto) {
        dto.setUserId(userId);
        FamilyMemberDto created = familyMemberService.createFamilyMember(dto);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FamilyMemberDto> update(@PathVariable Long userId, @PathVariable Long id, @RequestBody FamilyMemberDto dto) {
        dto.setUserId(userId);
        FamilyMemberDto updated = familyMemberService.updateFamilyMember(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long userId, @PathVariable Long id) {
        familyMemberService.deleteFamilyMember(id);
        return ResponseEntity.ok("Deleted");
    }
}
