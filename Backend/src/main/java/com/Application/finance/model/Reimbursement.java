package com.skyline.finance.model;

import com.skyline.auth.model.User;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "reimbursements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reimbursement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private BigDecimal amount;

    private String receiptUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReimbursementStatus status;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum ReimbursementStatus {
        PENDING,
        APPROVED,
        REJECTED,
        REIMBURSED
    }
}
