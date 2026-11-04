package com.skyline.attendance.model;

import com.skyline.auth.model.User;
import com.skyline.event.model.Event;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "check_ins", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"ticket_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckIn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scanned_by_user_id", nullable = false)
    private User scannedBy;

    @Builder.Default
    private LocalDateTime scannedAt = LocalDateTime.now();
}
