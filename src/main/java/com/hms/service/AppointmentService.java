package com.hms.service;

import com.hms.dto.AppointmentRequest;
import com.hms.entity.*;
import com.hms.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentService.class);

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientService patientService;
    private final DoctorService doctorService;
    private final EmailService emailService;

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
        Doctor doctor = doctorService.getDoctorByEmail(doctorEmail);
        Appointment appt = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));

        if (!appt.getDoctor().getId().equals(doctor.getId())) {
            throw new IllegalArgumentException("Appointment not found");
        }
        Appointment.Status previousStatus = appt.getStatus();
        if (status == Appointment.Status.CANCELLED && previousStatus != Appointment.Status.CANCELLED) {
            appt.setCancelledAt(LocalDateTime.now());
        }
        appt.setStatus(status);
        Appointment saved = appointmentRepository.save(appt);
        if (status != previousStatus && status == Appointment.Status.CONFIRMED) {
            notifyPatient(saved, "confirmed");
        } else if (status != previousStatus && status == Appointment.Status.CANCELLED) {
            notifyPatient(saved, "cancelled");
        }
        return saved;
    }

    public Appointment rescheduleForDoctor(Long appointmentId, LocalDateTime appointmentDate, String doctorEmail) {
        Doctor doctor = doctorService.getDoctorByEmail(doctorEmail);
        Appointment appt = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));

        if (!appt.getDoctor().getId().equals(doctor.getId())) {
            throw new IllegalArgumentException("Appointment not found");
        }
        if (appt.getStatus() == Appointment.Status.CANCELLED || appt.getStatus() == Appointment.Status.COMPLETED) {
            throw new IllegalArgumentException("Cancelled or completed appointments cannot be rescheduled");
        }
        if (appointmentDate.equals(appt.getAppointmentDate())) {
            return appt;
        }

        appt.setAppointmentDate(appointmentDate);
        Appointment saved = appointmentRepository.save(appt);
        notifyPatient(saved, "rescheduled");
        return saved;
    }

    public Appointment rescheduleForPatient(Long appointmentId, LocalDateTime appointmentDate, String patientEmail) {
        Patient patient = patientService.getPatientByEmail(patientEmail);
        Appointment appt = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));

        if (!appt.getPatient().getId().equals(patient.getId())) {
            throw new IllegalArgumentException("Appointment not found");
        }
        if (appt.getStatus() == Appointment.Status.CANCELLED || appt.getStatus() == Appointment.Status.COMPLETED) {
            throw new IllegalArgumentException("Cancelled or completed appointments cannot be rescheduled");
        }
        if (appointmentDate.equals(appt.getAppointmentDate())) {
            return appt;
        }

        appt.setAppointmentDate(appointmentDate);
        Appointment saved = appointmentRepository.save(appt);
        notifyPatient(saved, "rescheduled");
        return saved;
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
        Appointment saved = appointmentRepository.save(appt);
        notifyPatient(saved, "cancelled");
        return saved;
    }

    private void notifyPatient(Appointment appointment, String action) {
        try {
            emailService.sendAppointmentUpdateEmail(
                    appointment.getPatient().getUser().getEmail(),
                    appointment.getPatient().getUser().getFullName(),
                    appointment.getDoctor().getUser().getFullName(),
                    appointment.getAppointmentDate(),
                    action
            );
        } catch (RuntimeException exception) {
            log.error("Could not send appointment {} email for appointment {}", action, appointment.getId(), exception);
        }
    }
}
