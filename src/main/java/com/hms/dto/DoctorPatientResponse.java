package com.hms.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class DoctorPatientResponse {
    private Long patientId;
    private String patientCode;
    private String fullName;
    private String gender;
    private List<DoctorPatientAppointmentResponse> appointments;
}
