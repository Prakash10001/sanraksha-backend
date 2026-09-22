package com.hms.service;

import com.hms.dto.PatientProfileResponse;
import com.hms.entity.Patient;
import com.hms.repository.PatientRepository;
import com.hms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;

    /** Resolves the Patient profile linked to a logged-in user's email (from the JWT). */
    public Patient getPatientByEmail(String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return patientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("No patient profile linked to this account"));
    }

    public PatientProfileResponse getMyProfile(String email) {
        Patient patient = getPatientByEmail(email);
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
