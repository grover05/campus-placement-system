package com.pms.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Join entity representing the many-to-many relationship between
 * Student and Drive, enriched with application status/history.
 */
@Entity
@Table(name = "applications", uniqueConstraints = {
        @UniqueConstraint(name = "uk_student_drive", columnNames = {"student_id", "drive_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drive_id", nullable = false)
    private Drive drive;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private ApplicationStatus status = ApplicationStatus.APPLIED;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime appliedAt = LocalDateTime.now();

    private LocalDateTime updatedAt;
}
