package com.hms.service;

import com.hms.dto.AdminStatsResponse;
import com.hms.dto.RecentPatientResponse;
import com.hms.dto.ScheduleItemResponse;
import com.hms.entity.Appointment;
import com.hms.repository.AppointmentRepository;
import com.hms.repository.DoctorRepository;
import com.hms.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;

    public AdminStatsResponse getStats() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        long totalPatients = patientRepository.count();
        long totalDoctors = doctorRepository.count();
        long appointmentsToday = appointmentRepository
                .findByAppointmentDateBetween(startOfDay, endOfDay).size();
        long pendingAppointments = appointmentRepository
                .countByStatus(Appointment.Status.PENDING);

        return new AdminStatsResponse(totalPatients, totalDoctors, appointmentsToday, pendingAppointments);
    }

    public List<RecentPatientResponse> getRecentPatients() {
        return patientRepository.findTop5ByOrderByIdDesc().stream()
                .map(p -> new RecentPatientResponse(
                        p.getUser().getFullName(),
                        "PT-" + String.format("%05d", p.getId()),
                        p.getGender(),
                        p.getUser().getCreatedAt() != null
                                ? p.getUser().getCreatedAt().toLocalDate().toString()
                                : "—"
                ))
                .collect(Collectors.toList());
    }

    public List<ScheduleItemResponse> getTodaySchedule() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        return appointmentRepository.findByAppointmentDateBetween(startOfDay, endOfDay).stream()
                .sorted(Comparator.comparing(Appointment::getAppointmentDate))
                .map(a -> new ScheduleItemResponse(
                        a.getAppointmentDate().toLocalTime().toString(),
                        a.getPatient().getUser().getFullName(),
                        a.getDoctor().getUser().getFullName(),
                        a.getDoctor().getSpecialization(),
                        a.getStatus().name()
                ))
                .collect(Collectors.toList());
    }
}