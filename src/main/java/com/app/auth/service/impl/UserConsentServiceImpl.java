package com.app.auth.service.impl;

import com.app.auth.dto.UserConsentDto;
import com.app.auth.entity.UserConsent;
import com.app.auth.repository.AppointmentRepository;
import com.app.auth.repository.FamilyMemberRepository;
import com.app.auth.repository.FutureTwoDayAppointmentRepository;
import com.app.auth.repository.PastAppointmentRepository;
import com.app.auth.repository.PrescriptionRepository;
import com.app.auth.repository.UserConsentRepository;
import com.app.auth.repository.UserDetailsRepository;
import com.app.auth.service.UserConsentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserConsentServiceImpl implements UserConsentService {

    private static final Logger log = LoggerFactory.getLogger(UserConsentServiceImpl.class);

    private final UserConsentRepository consentRepository;
    private final UserDetailsRepository userDetailsRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final AppointmentRepository appointmentRepository;
    private final PastAppointmentRepository pastAppointmentRepository;
    private final FutureTwoDayAppointmentRepository futureTwoDayAppointmentRepository;
    private final PrescriptionRepository prescriptionRepository;

    public UserConsentServiceImpl(UserConsentRepository consentRepository,
                                  UserDetailsRepository userDetailsRepository,
                                  FamilyMemberRepository familyMemberRepository,
                                  AppointmentRepository appointmentRepository,
                                  PastAppointmentRepository pastAppointmentRepository,
                                  FutureTwoDayAppointmentRepository futureTwoDayAppointmentRepository,
                                  PrescriptionRepository prescriptionRepository) {
        this.consentRepository = consentRepository;
        this.userDetailsRepository = userDetailsRepository;
        this.familyMemberRepository = familyMemberRepository;
        this.appointmentRepository = appointmentRepository;
        this.pastAppointmentRepository = pastAppointmentRepository;
        this.futureTwoDayAppointmentRepository = futureTwoDayAppointmentRepository;
        this.prescriptionRepository = prescriptionRepository;
    }

    @Override
    public void submitConsents(Long userId, UserConsentDto.SubmitConsentRequest request) {
        if (request.getConsents() == null) return;

        for (UserConsentDto.ConsentRecord record : request.getConsents()) {
            // Withdraw any existing active consent for this type first
            Optional<UserConsent> existing = consentRepository.findActiveConsent(userId, record.getConsentType());
            existing.ifPresent(c -> {
                c.setWithdrawnAt(OffsetDateTime.now());
                consentRepository.save(c);
            });

            // Insert a new consent record
            UserConsent consent = new UserConsent(
                userId,
                record.getConsentType(),
                record.isConsented(),
                request.getAppVersion(),
                request.getPlatform(),
                request.getPolicyVersion() != null ? request.getPolicyVersion() : "1.0"
            );
            consentRepository.save(consent);
            log.info("Consent recorded: userId={} type={} granted={}", userId, record.getConsentType(), record.isConsented());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserConsentDto.ConsentStatusResponse getConsentStatus(Long userId) {
        List<UserConsent> active = consentRepository.findActiveConsentsByUser(userId);

        List<UserConsentDto.ConsentStatusItem> items = active.stream()
            .map(c -> new UserConsentDto.ConsentStatusItem(
                c.getConsentType(),
                c.isConsented(),
                c.getConsentTimestamp(),
                c.getWithdrawnAt()
            ))
            .collect(Collectors.toList());

        return new UserConsentDto.ConsentStatusResponse(userId, items);
    }

    @Override
    public void withdrawConsent(Long userId, UserConsentDto.WithdrawConsentRequest request) {
        OffsetDateTime now = OffsetDateTime.now();

        if (request.getConsentType() == null || request.getConsentType().isBlank()) {
            // Withdraw ALL consents
            List<UserConsent> active = consentRepository.findActiveConsentsByUser(userId);
            active.forEach(c -> c.setWithdrawnAt(now));
            consentRepository.saveAll(active);
            log.info("All consents withdrawn for userId={}", userId);
        } else {
            Optional<UserConsent> consent = consentRepository.findActiveConsent(userId, request.getConsentType());
            consent.ifPresent(c -> {
                c.setWithdrawnAt(now);
                consentRepository.save(c);
                log.info("Consent withdrawn: userId={} type={}", userId, request.getConsentType());
            });
        }
    }

    @Override
    public void deleteHealthData(Long userId) {
        // Anonymise medical fields on the user record – keep account alive
        userDetailsRepository.findById(userId).ifPresent(user -> {
            user.setBloodGroup(null);
            user.setHeightCm(null);
            user.setWeightKg(null);
            user.setBloodPressureSystolic(null);
            user.setBloodPressureDiastolic(null);
            user.setHeartRate(null);
            user.setBloodOxygenLevel(null);
            user.setBodyTemperature(null);
            user.setHasDiabetes(false);
            user.setHasHypertension(false);
            user.setHasHeartDisease(false);
            user.setHasKidneyDisease(false);
            user.setHasLiverDisease(false);
            user.setCurrentMedicationsStr(null);
            user.setAllergiesStr(null);
            user.setChronicDiseasesStr(null);
            user.setPreviousSurgeriesStr(null);
            user.setVaccinationsStr(null);
            user.setFamilyMedicalHistory(null);
            user.setEmergencyContactName(null);
            user.setEmergencyContactNumber(null);
            user.setEmergencyContactRelation(null);
            user.setUpdatedAt(OffsetDateTime.now());
            userDetailsRepository.save(user);
        });

        // Delete family members
        familyMemberRepository.findByUserId(userId)
            .forEach(fm -> familyMemberRepository.delete(fm));

        // Withdraw all consents (DPDP – data deleted = purpose fulfilled)
        List<UserConsent> active = consentRepository.findActiveConsentsByUser(userId);
        active.forEach(c -> c.setWithdrawnAt(OffsetDateTime.now()));
        consentRepository.saveAll(active);

        log.info("Health data erased for userId={}", userId);
    }

    @Override
    public void deleteUserAccount(Long userId) {
        // 1. Delete dependent records first
        appointmentRepository.deleteByUserId(userId);
        pastAppointmentRepository.deleteByUserId(userId);
        futureTwoDayAppointmentRepository.deleteByUserId(userId);
        prescriptionRepository.deleteByUserId(Math.toIntExact(userId));
        familyMemberRepository.deleteByUserId(userId);
        consentRepository.deleteAllByUserId(userId);

        // 2. Hard delete user row from user_details
        userDetailsRepository.deleteById(userId);

        log.info("User account hard-deleted for userId={}", userId);
    }
}
