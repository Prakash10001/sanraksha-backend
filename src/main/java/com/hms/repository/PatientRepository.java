package com.hms.repository;

import com.hms.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByUserId(Long userId);
    Optional<Patient> findByUser_Email(String email);
    Optional<Patient> findByUser_EmailIgnoreCase(String email);
    Page<Patient> findByUser_FullNameContainingIgnoreCaseOrUser_EmailContainingIgnoreCase(
            String name, String email, Pageable pageable);
    List<Patient> findTop5ByOrderByIdDesc();
}