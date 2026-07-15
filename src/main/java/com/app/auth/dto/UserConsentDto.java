package com.app.auth.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public class UserConsentDto {

    // --- Request DTOs ---

    public static class ConsentRecord {
        private String consentType;
        private boolean consented;

        public String getConsentType() { return consentType; }
        public void setConsentType(String consentType) { this.consentType = consentType; }
        public boolean isConsented() { return consented; }
        public void setConsented(boolean consented) { this.consented = consented; }
    }

    public static class SubmitConsentRequest {
        private List<ConsentRecord> consents;
        private String appVersion;
        private String platform;
        private String policyVersion;

        public List<ConsentRecord> getConsents() { return consents; }
        public void setConsents(List<ConsentRecord> consents) { this.consents = consents; }
        public String getAppVersion() { return appVersion; }
        public void setAppVersion(String appVersion) { this.appVersion = appVersion; }
        public String getPlatform() { return platform; }
        public void setPlatform(String platform) { this.platform = platform; }
        public String getPolicyVersion() { return policyVersion; }
        public void setPolicyVersion(String policyVersion) { this.policyVersion = policyVersion; }
    }

    public static class WithdrawConsentRequest {
        private String consentType; // null = withdraw ALL
        private String reason;

        public String getConsentType() { return consentType; }
        public void setConsentType(String consentType) { this.consentType = consentType; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    // --- Response DTOs ---

    public static class ConsentStatusItem {
        private String consentType;
        private boolean active;
        private OffsetDateTime grantedAt;
        private OffsetDateTime withdrawnAt;

        public ConsentStatusItem(String consentType, boolean active,
                                 OffsetDateTime grantedAt, OffsetDateTime withdrawnAt) {
            this.consentType = consentType;
            this.active = active;
            this.grantedAt = grantedAt;
            this.withdrawnAt = withdrawnAt;
        }

        public String getConsentType() { return consentType; }
        public boolean isActive() { return active; }
        public OffsetDateTime getGrantedAt() { return grantedAt; }
        public OffsetDateTime getWithdrawnAt() { return withdrawnAt; }
    }

    public static class ConsentStatusResponse {
        private Long userId;
        private List<ConsentStatusItem> consents;
        private OffsetDateTime fetchedAt;

        public ConsentStatusResponse(Long userId, List<ConsentStatusItem> consents) {
            this.userId = userId;
            this.consents = consents;
            this.fetchedAt = OffsetDateTime.now();
        }

        public Long getUserId() { return userId; }
        public List<ConsentStatusItem> getConsents() { return consents; }
        public OffsetDateTime getFetchedAt() { return fetchedAt; }
    }

    // Consent type constants
    public static final String MEDICAL_DATA_STORAGE = "MEDICAL_DATA_STORAGE";
    public static final String DIAGNOSTIC_SHARING   = "DIAGNOSTIC_SHARING";
    public static final String PRESCRIPTION_STORAGE = "PRESCRIPTION_STORAGE";
    public static final String PUSH_NOTIFICATIONS   = "PUSH_NOTIFICATIONS";
    public static final String ANALYTICS            = "ANALYTICS";
}
