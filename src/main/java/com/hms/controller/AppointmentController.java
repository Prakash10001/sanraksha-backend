package com.hms.controller;

import com.hms.dto.AppointmentRequest;
import com.hms.entity.Appointment;
import com.hms.service.AppointmentService;
import com.hms.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final PatientService patientService;

    @PostMapping
    public ResponseEntity<Appointment> book(Authentication auth, @Valid @RequestBody AppointmentRequest request) {
        String email = auth.getName(); // set by JwtAuthFilter
        return ResponseEntity.ok(appointmentService.book(email, request));
    }

    @GetMapping("/me")
    public List<Appointment> myAppointments(Authentication auth) {
        var patient = patientService.getPatientByEmail(auth.getName());
        return appointmentService.getForPatient(patient.getId());
    }

    @PutMapping("/{id}/status")
    public Appointment updateStatus(@PathVariable Long id, @RequestParam Appointment.Status status) {
        return appointmentService.updateStatus(id, status);
    }
}
