package com.hms.controller;

import com.hms.dto.AdminStatsResponse;
import com.hms.dto.AuthResponse;
import com.hms.dto.RecentPatientResponse;
import com.hms.dto.RegisterRequest;
import com.hms.dto.ScheduleItemResponse;
import com.hms.entity.Role;
import com.hms.service.AdminService;
import com.hms.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final AuthService authService;

    @GetMapping("/stats")
    public AdminStatsResponse stats() {
        return adminService.getStats();
    }

    @GetMapping("/recent-patients")
    public List<RecentPatientResponse> recentPatients() {
        return adminService.getRecentPatients();
    }

    @GetMapping("/today-schedule")
    public List<ScheduleItemResponse> todaySchedule() {
        return adminService.getTodaySchedule();
    }

    @PostMapping("/patients")
    public ResponseEntity<AuthResponse> createPatient(@Valid @RequestBody RegisterRequest request) {
        request.setRole(Role.PATIENT);
        return ResponseEntity.ok(authService.register(request));
    }
}