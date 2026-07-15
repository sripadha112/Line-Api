package com.app.auth.controller;

import com.app.auth.config.AuthAccess;
import com.app.auth.config.QueryParamIdCrypto;
import com.app.auth.dto.UserConsentDto;
import com.app.auth.service.UserConsentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * DPDP Act compliant consent and data-rights controller.
 *
 * Endpoints:
 *  POST   /api/user/{userId}/consent           - Submit initial or updated consent choices
 *  GET    /api/user/{userId}/consent            - Get current consent status
 *  POST   /api/user/{userId}/consent/withdraw   - Withdraw one or all consents
 *  DELETE /api/user/{userId}/health-data        - Erase all health/medical data (keep account)
 *  DELETE /api/user/{userId}/account            - Hard delete entire account and related records
 */
@RestController
@RequestMapping("/api/user")
public class UserConsentController {

    private final UserConsentService consentService;

    public UserConsentController(UserConsentService consentService) {
        this.consentService = consentService;
    }

    /** Submit / update granular consent choices */
    @PostMapping("/{userId}/consent")
    public ResponseEntity<Map<String, String>> submitConsents(
            @PathVariable("userId") String encodedUserId,
            @RequestBody UserConsentDto.SubmitConsentRequest request) {
        Long userId = QueryParamIdCrypto.decodeLong(encodedUserId);
        AuthAccess.requireSelf(userId);
        consentService.submitConsents(userId, request);
        return ResponseEntity.ok(Map.of("status", "Consents recorded successfully"));
    }

    /** Retrieve current active consent status */
    @GetMapping("/{userId}/consent")
    public ResponseEntity<UserConsentDto.ConsentStatusResponse> getConsentStatus(
            @PathVariable("userId") String encodedUserId) {
        Long userId = QueryParamIdCrypto.decodeLong(encodedUserId);
        AuthAccess.requireSelf(userId);
        return ResponseEntity.ok(consentService.getConsentStatus(userId));
    }

    /** Withdraw one specific consent or all consents */
    @PostMapping("/{userId}/consent/withdraw")
    public ResponseEntity<Map<String, String>> withdrawConsent(
            @PathVariable("userId") String encodedUserId,
            @RequestBody UserConsentDto.WithdrawConsentRequest request) {
        Long userId = QueryParamIdCrypto.decodeLong(encodedUserId);
        AuthAccess.requireSelf(userId);
        consentService.withdrawConsent(userId, request);
        return ResponseEntity.ok(Map.of("status", "Consent withdrawn successfully"));
    }

    /**
     * DPDP Right to Erasure – delete only health/medical data, keep the account.
     * This allows users to clear sensitive medical info without losing their booking history.
     */
    @DeleteMapping("/{userId}/health-data")
    public ResponseEntity<Map<String, String>> deleteHealthData(
            @PathVariable("userId") String encodedUserId) {
        Long userId = QueryParamIdCrypto.decodeLong(encodedUserId);
        AuthAccess.requireSelf(userId);
        consentService.deleteHealthData(userId);
        return ResponseEntity.ok(Map.of("status", "Health data deleted successfully"));
    }

    /**
     * DPDP Right to Erasure – hard delete entire account and related records.
     */
    @DeleteMapping("/{userId}/account")
    public ResponseEntity<Map<String, String>> deleteUserAccount(
            @PathVariable("userId") String encodedUserId) {
        Long userId = QueryParamIdCrypto.decodeLong(encodedUserId);
        AuthAccess.requireSelf(userId);
        consentService.deleteUserAccount(userId);
        return ResponseEntity.ok(Map.of("status", "Account deleted successfully"));
    }
}
