package com.skyline.volunteer.controller;

import com.skyline.auth.model.User;
import com.skyline.auth.repository.UserRepository;
import com.skyline.volunteer.model.Task;
import com.skyline.volunteer.repository.TaskRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {
        return ResponseEntity.ok(taskRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody CreateTaskRequest request) {
        User assignedUser = null;
        if (request.getAssignedUserId() != null) {
            assignedUser = userRepository.findById(request.getAssignedUserId()).orElse(null);
        }

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .assignedUser(assignedUser)
                .priority(request.getPriority() != null ? request.getPriority() : Task.TaskPriority.MEDIUM)
                .status(Task.TaskStatus.TO_DO)
                .dueDate(request.getDueDate())
                .build();

        Task saved = taskRepository.save(task);
        return ResponseEntity.ok(saved);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateTaskStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        String statusStr = body.get("status");
        if (statusStr != null) {
            Task.TaskStatus newStatus = Task.TaskStatus.valueOf(statusStr);
            if (newStatus == Task.TaskStatus.DONE && task.getStatus() != Task.TaskStatus.DONE) {
                User assigned = task.getAssignedUser();
                if (assigned != null) {
                    int currentPts = assigned.getVolunteerPoints() != null ? assigned.getVolunteerPoints() : 0;
                    assigned.setVolunteerPoints(currentPts + 50);
                    userRepository.save(assigned);
                }
            }
            task.setStatus(newStatus);
            taskRepository.save(task);
        }

        return ResponseEntity.ok(task);
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<?> getVolunteerLeaderboard() {
        List<User> volunteers = userRepository.findAll().stream()
                .filter(u -> u.getRole() == com.skyline.auth.model.Role.VOLUNTEER || u.getVolunteerPoints() != null && u.getVolunteerPoints() > 0)
                .sorted((u1, u2) -> Integer.compare(
                        u2.getVolunteerPoints() != null ? u2.getVolunteerPoints() : 0,
                        u1.getVolunteerPoints() != null ? u1.getVolunteerPoints() : 0))
                .toList();
        return ResponseEntity.ok(volunteers);
    }

    @Data
    public static class CreateTaskRequest {
        private String title;
        private String description;
        private Long assignedUserId;
        private Task.TaskPriority priority;
        private java.time.LocalDate dueDate;
    }
}
