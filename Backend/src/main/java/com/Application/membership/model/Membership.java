package com.skyline.membership.model;

import com.skyline.auth.model.User;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "memberships")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Membership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MembershipStatus status;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate expiryDate;

    @Column(nullable = false)
    private BigDecimal duesPaid;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum MembershipStatus {
        PENDING,
        ACTIVE,
        EXPIRED,
        SUSPENDED
    }
}
