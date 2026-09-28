package com.hms.controller;

import com.hms.dto.DoctorProfileResponse;
import com.hms.entity.Doctor;
import com.hms.entity.DoctorApprovalStatus;
import com.hms.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/doctors")
@RequiredArgsConstructor
public class AdminDoctorController {

    private final DoctorRepository doctorRepository;

    @GetMapping
    public List<Doctor> getDoctorApplications(
            @RequestParam(required = false) String status) {

        if (status == null || status.isBlank()) {
            return doctorRepository.findAll();
        }

        DoctorApprovalStatus approvalStatus =
                DoctorApprovalStatus.valueOf(status.toUpperCase());

        return doctorRepository.findAll().stream()
                .filter(doctor -> doctor.getApprovalStatus() == approvalStatus)
                .toList();
    }

    @PatchMapping("/{doctorId}/approval")
    public ResponseEntity<Doctor> updateDoctorApproval(
            @PathVariable Long doctorId,
            @RequestBody ApprovalRequest request) {

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));

        DoctorApprovalStatus newStatus =
                DoctorApprovalStatus.valueOf(request.status().toUpperCase());

        if (newStatus != DoctorApprovalStatus.APPROVED
                && newStatus != DoctorApprovalStatus.REJECTED) {
            throw new IllegalArgumentException(
                    "Status must be APPROVED or REJECTED");
        }

        doctor.setApprovalStatus(newStatus);

        if (newStatus == DoctorApprovalStatus.REJECTED) {
            doctor.setRejectionReason(request.rejectionReason());
        } else {
            doctor.setRejectionReason(null);
        }

        return ResponseEntity.ok(doctorRepository.save(doctor));
    }

    public record ApprovalRequest(
            String status,
            String rejectionReason
    ) {
    }
}