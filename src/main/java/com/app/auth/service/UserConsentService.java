package com.app.auth.service;

import com.app.auth.dto.UserConsentDto;

public interface UserConsentService {

    /** Record/update a batch of consent choices for a user */
    void submitConsents(Long userId, UserConsentDto.SubmitConsentRequest request);

    /** Get current active consent status for a user */
    UserConsentDto.ConsentStatusResponse getConsentStatus(Long userId);

    /** Withdraw one or all consents for a user */
    void withdrawConsent(Long userId, UserConsentDto.WithdrawConsentRequest request);

    /** Delete ALL health/medical data for a user (Right to Erasure) – keeps account */
    void deleteHealthData(Long userId);

    /** Hard delete the entire user account and all associated data */
    void deleteUserAccount(Long userId);
}
