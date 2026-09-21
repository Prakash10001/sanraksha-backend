package com.hms.service;

import com.hms.entity.PasswordResetToken;
import com.hms.entity.User;
import com.hms.repository.PasswordResetTokenRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
import com.hms.config.JwtUtil;
import com.hms.dto.*;
import com.hms.entity.*;
import com.hms.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("An account with this email already exists");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();
        user = userRepository.save(user);

        // Create the linked profile row depending on role
        if (request.getRole() == Role.DOCTOR) {
            Doctor doctor = Doctor.builder()
                    .user(user)
                    .specialization(
                        request.getSpecialization() != null ? request.getSpecialization() : "General Physician"
                    )
                    .build();
            doctorRepository.save(doctor);
        } else if (request.getRole() == Role.PATIENT) {
            Patient patient = Patient.builder()
                    .user(user)
                    .build();
            patientRepository.save(patient);
        }

        try {
            emailService.sendWelcomeEmail(user.getEmail(), user.getFullName());
        } catch (RuntimeException exception) {
            log.error("User {} was registered, but the welcome email could not be sent", user.getEmail(), exception);
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getFullName(), user.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getFullName(), user.getRole().name());
    }

    public void requestPasswordReset(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            passwordResetTokenRepository.save(new PasswordResetToken(token, user));
            emailService.sendPasswordResetEmail(user.getEmail(), token);
        });
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid password reset token"));

        if (resetToken.isUsed() || resetToken.isExpired()) {
            throw new IllegalArgumentException("Password reset token is invalid or expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }
}
