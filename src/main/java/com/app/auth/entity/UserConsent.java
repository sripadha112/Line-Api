package com.app.auth.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "user_consent", indexes = {
    @Index(name = "idx_consent_user_id", columnList = "user_id"),
    @Index(name = "idx_consent_type", columnList = "consent_type")
})
public class UserConsent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * Consent type constants:
     * MEDICAL_DATA_STORAGE   - consent to store health vitals, conditions, medications
     * DIAGNOSTIC_SHARING     - consent to share data with treating doctor
     * PRESCRIPTION_STORAGE   - consent to store prescriptions
     * PUSH_NOTIFICATIONS     - consent to receive push notifications
     * ANALYTICS              - consent for anonymised usage analytics
     */
    @Column(name = "consent_type", nullable = false, length = 100)
    private String consentType;

    @Column(name = "consented", nullable = false)
    private boolean consented;

    @Column(name = "consent_timestamp", nullable = false)
    private OffsetDateTime consentTimestamp;

    @Column(name = "withdrawn_at")
    private OffsetDateTime withdrawnAt;

    @Column(name = "app_version", length = 30)
    private String appVersion;

    @Column(name = "platform", length = 20) // android / ios / web
    private String platform;

    @Column(name = "policy_version", length = 20)
    private String policyVersion;

    public UserConsent() {}

    public UserConsent(Long userId, String consentType, boolean consented,
                       String appVersion, String platform, String policyVersion) {
        this.userId = userId;
        this.consentType = consentType;
        this.consented = consented;
        this.consentTimestamp = OffsetDateTime.now();
        this.appVersion = appVersion;
        this.platform = platform;
        this.policyVersion = policyVersion;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getConsentType() { return consentType; }
    public void setConsentType(String consentType) { this.consentType = consentType; }
    public boolean isConsented() { return consented; }
    public void setConsented(boolean consented) { this.consented = consented; }
    public OffsetDateTime getConsentTimestamp() { return consentTimestamp; }
    public void setConsentTimestamp(OffsetDateTime consentTimestamp) { this.consentTimestamp = consentTimestamp; }
    public OffsetDateTime getWithdrawnAt() { return withdrawnAt; }
    public void setWithdrawnAt(OffsetDateTime withdrawnAt) { this.withdrawnAt = withdrawnAt; }
    public String getAppVersion() { return appVersion; }
    public void setAppVersion(String appVersion) { this.appVersion = appVersion; }
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    public String getPolicyVersion() { return policyVersion; }
    public void setPolicyVersion(String policyVersion) { this.policyVersion = policyVersion; }
}
