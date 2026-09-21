package com.hms.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;

@Entity
@Table(name = "doctors")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(nullable = false, length = 100)
    private String specialization;

    @Column(length = 100)
    private String department;

    @Builder.Default
    @Column(name = "available_from")
    private LocalTime availableFrom = LocalTime.of(9, 0);

    @Builder.Default
    @Column(name = "available_to")
    private LocalTime availableTo = LocalTime.of(17, 0);
}
