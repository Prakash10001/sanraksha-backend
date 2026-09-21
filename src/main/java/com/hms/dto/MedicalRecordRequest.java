package com.hms.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter
public class MedicalRecordRequest {
    @NotNull
    private Long patientId;

    private String diagnosis;
    private String prescription;
    private String notes;
}
