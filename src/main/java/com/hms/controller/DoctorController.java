package com.hms.controller;

import com.hms.entity.Doctor;
import com.hms.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping
    public List<Doctor> getAll() {
        return doctorService.getAll();
    }

    @GetMapping("/search")
    public List<Doctor> search(@RequestParam String specialization) {
        return doctorService.searchBySpecialization(specialization);
    }
}
