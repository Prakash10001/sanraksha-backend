package com.hms.controller;

import com.hms.dto.AdminStatsResponse;
import com.hms.dto.RecentPatientResponse;
import com.hms.dto.ScheduleItemResponse;
import com.hms.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

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
}