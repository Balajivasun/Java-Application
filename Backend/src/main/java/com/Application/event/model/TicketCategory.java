package com.skyline.event.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "ticket_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    @JsonIgnore
    private Event event;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal memberPrice;

    @Column(nullable = false)
    private BigDecimal nonMemberPrice;

    @Column(nullable = false)
    private Integer totalQuantity;

    @Builder.Default
    private Integer reservedQuantity = 0;

    @Builder.Default
    private Integer soldQuantity = 0;
}
