package com.hms.service;

import com.hms.dto.MedicalRecordRequest;
import com.hms.entity.MedicalRecord;
import com.hms.entity.Patient;
import com.hms.repository.MedicalRecordRepository;
import com.hms.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final PatientRepository patientRepository;
    private final DoctorService doctorService;

    public MedicalRecord addRecord(String doctorEmail, MedicalRecordRequest request) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        var doctor = doctorService.getDoctorByEmail(doctorEmail);

        MedicalRecord record = MedicalRecord.builder()
                .patient(patient)
                .doctor(doctor)
                .diagnosis(request.getDiagnosis())
                .prescription(request.getPrescription())
                .notes(request.getNotes())
                .build();

        return medicalRecordRepository.save(record);
    }

    public List<MedicalRecord> getForPatient(Long patientId) {
        return medicalRecordRepository.findByPatientIdOrderByVisitDateDesc(patientId);
    }
}
