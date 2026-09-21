package com.hms.dto;

import lombok.*;

@Getter @Setter
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String fullName;
    private String role;
}
