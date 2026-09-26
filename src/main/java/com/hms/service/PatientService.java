package com.hms.service;

import com.hms.dto.PatientProfileResponse;
import com.hms.dto.UpdatePatientProfileRequest;
import com.hms.entity.Patient;
import com.hms.entity.Role;
import com.hms.repository.PatientRepository;
import com.hms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;

    /** Resolves the Patient profile linked to a logged-in user's email (from the JWT). */
    @Transactional
    public Patient getPatientByEmail(String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return patientRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    if (user.getRole() != Role.PATIENT) {
                        throw new IllegalArgumentException("No patient profile linked to this account");
                    }
                    return patientRepository.save(Patient.builder()
                            .user(user)
                            .mustResetPassword(user.isMustResetPassword())
                            .build());
                });
    }

    @Transactional
    public PatientProfileResponse getMyProfile(String email) {
        Patient patient = getPatientByEmail(email);
        return toProfileResponse(patient);
    }

    @Transactional
    public PatientProfileResponse updateMyProfile(String email, UpdatePatientProfileRequest request) {
        Patient patient = getPatientByEmail(email);
        patient.setPhone(request.getPhone().trim());
        patient.setGender(request.getGender().trim());
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setBloodGroup(request.getBloodGroup().trim());
        patient.setAddress(request.getAddress().trim());

        return toProfileResponse(patientRepository.save(patient));
    }

    private PatientProfileResponse toProfileResponse(Patient patient) {
        return new PatientProfileResponse(
                patient.getId(),
                patient.getUser().getFullName(),
                patient.getUser().getEmail(),
                patient.getUser().getRole().name(),
                patient.getUser().getCreatedAt(),
                patient.getUser().getGender(),
                patient.getDateOfBirth(),
                patient.getGender(),
                patient.getPhone(),
                patient.getAddress(),
                patient.getBloodGroup()
        );
    }
}
