package com.skyline.ticketing.model;

import com.skyline.auth.model.User;
import com.skyline.event.model.TicketCategory;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_category_id", nullable = false)
    private TicketCategory ticketCategory;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, unique = true)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum ReservationStatus {
        HELD,
        CONFIRMED,
        EXPIRED,
        CANCELLED
    }
}
