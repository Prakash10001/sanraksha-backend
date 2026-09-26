package com.hms.service;

import com.hms.entity.Appointment;
import com.hms.entity.Doctor;
import com.hms.entity.Patient;
import com.hms.entity.User;
import com.hms.repository.AppointmentRepository;
import com.hms.repository.DoctorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private PatientService patientService;

    @Mock
    private DoctorService doctorService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AppointmentService appointmentService;

    @Test
    void rescheduleForPatient_shouldSaveAndNotify_whenPatientOwnsAppointment() {
        Patient patient = Patient.builder()
                .id(12L)
                .user(User.builder()
                        .email("patient@example.com")
                        .fullName("Anita Sharma")
                        .build())
                .build();
        Doctor doctor = Doctor.builder()
                .user(User.builder().fullName("Dr. Sharma").build())
                .build();
        Appointment appointment = Appointment.builder()
                .id(1L)
                .patient(patient)
                .doctor(doctor)
                .appointmentDate(LocalDateTime.of(2026, 9, 27, 9, 0))
                .build();
        LocalDateTime newAppointmentDate = LocalDateTime.of(2026, 9, 27, 10, 30);

        when(patientService.getPatientByEmail("patient@example.com")).thenReturn(patient);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(appointment)).thenReturn(appointment);

        Appointment result = appointmentService.rescheduleForPatient(1L, newAppointmentDate, "patient@example.com");

        assertThat(result.getAppointmentDate()).isEqualTo(newAppointmentDate);
        verify(appointmentRepository).save(appointment);
        verify(emailService).sendAppointmentUpdateEmail(
                "patient@example.com", "Anita Sharma", "Dr. Sharma", newAppointmentDate, "rescheduled");
    }

    @Test
    void rescheduleForPatient_shouldNotSave_whenAppointmentBelongsToAnotherPatient() {
        Patient authenticatedPatient = Patient.builder().id(12L).build();
        Patient appointmentPatient = Patient.builder().id(13L).build();
        Appointment appointment = Appointment.builder()
                .id(1L)
                .patient(appointmentPatient)
                .appointmentDate(LocalDateTime.of(2026, 9, 27, 9, 0))
                .build();

        when(patientService.getPatientByEmail("patient@example.com")).thenReturn(authenticatedPatient);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> appointmentService.rescheduleForPatient(
                1L, LocalDateTime.of(2026, 9, 27, 10, 30), "patient@example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Appointment not found");

        verify(appointmentRepository, never()).save(any());
        verifyNoInteractions(emailService);
    }
}
