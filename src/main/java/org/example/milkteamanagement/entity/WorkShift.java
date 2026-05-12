package org.example.milkteamanagement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "work_shifts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkShift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false)
    private UserAccount staff;

    @Column(nullable = false)
    private LocalDateTime shiftStart;

    @Column(nullable = false)
    private LocalDateTime shiftEnd;

    @Column(length = 255)
    private String note;
}

