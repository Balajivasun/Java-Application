package com.skyline.volunteer.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.skyline.auth.model.User;
import com.skyline.event.model.Event;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "event_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "categories"})
    private Event event;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_user_id")
    @JsonIgnoreProperties({"password", "createdAt"})
    private User assignedUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;

    private LocalDate dueDate;

    public enum TaskPriority {
        LOW,
        MEDIUM,
        HIGH
    }

    public enum TaskStatus {
        TO_DO,
        IN_PROGRESS,
        DONE
    }
}
