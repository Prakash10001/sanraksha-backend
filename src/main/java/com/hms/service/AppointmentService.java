package com.hms.service;

import com.hms.dto.AppointmentRequest;
import com.hms.entity.*;
import com.hms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientService patientService;

    public Appointment book(String patientEmail, AppointmentRequest request) {
        Patient patient = patientService.getPatientByEmail(patientEmail);

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentDate(request.getAppointmentDate())
                .reason(request.getReason())
                .status(Appointment.Status.PENDING)
                .build();

        return appointmentRepository.save(appointment);
    }

    public List<Appointment> getForPatient(Long patientId) {
        return appointmentRepository.findByPatientId(patientId);
    }

    public List<Appointment> getForDoctor(Long doctorId) {
        return appointmentRepository.findByDoctorId(doctorId);
    }

    public Appointment updateStatus(Long appointmentId, Appointment.Status status) {
        Appointment appt = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
        appt.setStatus(status);
        return appointmentRepository.save(appt);
    }

    public Appointment updateStatusForDoctor(Long appointmentId, Appointment.Status status, String doctorEmail) {
        Doctor doctor = doctorRepository.findAll().stream()
                .filter(candidate -> candidate.getUser().getEmail().equalsIgnoreCase(doctorEmail))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No doctor profile linked to this account"));
        Appointment appt = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));

        if (!appt.getDoctor().getId().equals(doctor.getId())) {
            throw new IllegalArgumentException("Appointment not found");
        }
        appt.setStatus(status);
        return appointmentRepository.save(appt);
    }

    public Appointment cancelForPatient(Long appointmentId, String patientEmail) {
        Patient patient = patientService.getPatientByEmail(patientEmail);
        Appointment appt = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));

        if (!appt.getPatient().getId().equals(patient.getId())) {
            throw new IllegalArgumentException("Appointment not found");
        }
        if (appt.getStatus() == Appointment.Status.CANCELLED) {
            return appt;
        }
        if (appt.getStatus() == Appointment.Status.COMPLETED) {
            throw new IllegalArgumentException("Completed appointments cannot be cancelled");
        }

        appt.setStatus(Appointment.Status.CANCELLED);
        appt.setCancelledAt(LocalDateTime.now());
        return appointmentRepository.save(appt);
    }
}
