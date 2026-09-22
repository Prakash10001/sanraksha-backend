package com.hms.controller;

import com.hms.dto.PatientProfileResponse;
import com.hms.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @GetMapping("/me")
    public PatientProfileResponse getMyProfile(Authentication authentication) {
        return patientService.getMyProfile(authentication.getName());
    }
}
