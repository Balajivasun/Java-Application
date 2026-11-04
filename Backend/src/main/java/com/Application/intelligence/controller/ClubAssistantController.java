package com.skyline.intelligence.controller;

import com.skyline.auth.repository.UserRepository;
import com.skyline.event.repository.EventRepository;
import com.skyline.membership.model.Membership;
import com.skyline.membership.repository.MembershipRepository;
import com.skyline.merchandise.repository.MerchandiseProductRepository;
import com.skyline.volunteer.repository.TaskRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/v1/intelligence")
@RequiredArgsConstructor
public class ClubAssistantController {

    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;
    private final EventRepository eventRepository;
    private final MerchandiseProductRepository productRepository;
    private final TaskRepository taskRepository;

    @PostMapping("/assistant")
    public ResponseEntity<?> queryAssistant(org.springframework.security.core.Authentication authentication, @RequestBody AssistantQueryRequest request) {
        String query = request.getQuery() != null ? request.getQuery().toLowerCase() : "";
        String userEmail = authentication != null ? authentication.getName() : null;

        boolean isAuthorizedCoordinator = false;
        if (userEmail != null) {
            var userOpt = userRepository.findByEmail(userEmail);
            if (userOpt.isPresent()) {
                var role = userOpt.get().getRole();
                isAuthorizedCoordinator = (role == com.skyline.auth.model.Role.EVENT_COORDINATOR ||
                                            role == com.skyline.auth.model.Role.SUPER_ADMIN ||
                                            role == com.skyline.auth.model.Role.CLUB_ADMIN ||
                                            role == com.skyline.auth.model.Role.EVENT_ORGANIZER);
            }
        }

        String answer = "";

        if (!isAuthorizedCoordinator) {
            // Student / Public Response
            if (query.contains("event") || query.contains("gala") || query.contains("date")) {
                answer = "Annual Spring Gala 2026 is scheduled at Grand Auditorium. Member tickets are ₹250 and Non-Member tickets are ₹600.";
            } else if (query.contains("hoodie") || query.contains("merch") || query.contains("price")) {
                answer = "Student Association Hoodies are available in Sizes S, M, L, XL at ₹1,299 for members and ₹1,799 for non-members.";
            } else {
                answer = "🔒 Notice: Detailed financial ledgers, budget breakdowns, and internal expense reports are restricted to Event Coordinators. You can check upcoming events, purchase tickets, and track volunteer points.";
            }
        } else {
            // Event Coordinator Full Access Response
            if (query.contains("budget") || query.contains("finance") || query.contains("revenue") || query.contains("expense") || query.contains("dj") || query.contains("cost")) {
                answer = "📊 Event Coordinator Financial Report: Total Revenue Collected: ₹48,500.00 | Total Expenses Logged: ₹7,000.00 (₹5,000 for DJ & Sound + ₹2,000 Bake Sale Refreshments by Volunteer Ajay) | Net Event Surplus: ₹41,500.00.";
            } else if (query.contains("member") || query.contains("how many active")) {
                long activeMembers = membershipRepository.findAll().stream()
                        .filter(m -> m.getStatus() == Membership.MembershipStatus.ACTIVE)
                        .count();
                answer = "Verified Active Paid Members: " + activeMembers + " students. Annual dues rate: ₹500/year.";
            } else if (query.contains("task") || query.contains("volunteer")) {
                long totalTasks = taskRepository.count();
                answer = "Active Volunteer Board: " + totalTasks + " tasks logged. Volunteer points leaderboard active (+50 pts per completed task, +10 pts per ticket scan).";
            } else {
                answer = "Coordinator Intel Desk: System Operational. Total Accounts: " + userRepository.count() + ", Active Events: " + eventRepository.count() + ", Total Revenue: ₹48,500.00, Net Balance: ₹41,500.00.";
            }
        }

        return ResponseEntity.ok(Map.of(
                "query", request.getQuery(),
                "answer", answer,
                "isCoordinatorAccess", isAuthorizedCoordinator,
                "source", isAuthorizedCoordinator ? "Authorized Event Coordinator Ledger" : "Student Public Information Portal"
        ));
    }

    @Data
    public static class AssistantQueryRequest {
        private String query;
    }
}
