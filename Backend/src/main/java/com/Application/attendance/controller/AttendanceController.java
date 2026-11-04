package com.skyline.attendance.controller;

import com.skyline.attendance.model.CheckIn;
import com.skyline.attendance.model.CheckInRepository;
import com.skyline.attendance.model.Ticket;
import com.skyline.attendance.repository.TicketRepository;
import com.skyline.auth.model.User;
import com.skyline.auth.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final TicketRepository ticketRepository;
    private final CheckInRepository checkInRepository;
    private final UserRepository userRepository;

    @PostMapping("/scan")
    @PreAuthorize("hasAnyRole('VOLUNTEER', 'EVENT_COORDINATOR', 'CLUB_ADMIN', 'EVENT_ORGANIZER', 'SUPER_ADMIN')")
    @Transactional
    public ResponseEntity<?> scanTicket(Authentication authentication, @RequestBody ScanRequest request) {
        User scannedBy = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Organizer not found"));

        String rawCode = request.getTicketCode();
        if (rawCode.contains("SKYLINE:v1:")) {
            String[] parts = rawCode.split(":");
            if (parts.length >= 3) {
                rawCode = parts[2];
            }
        }

        Optional<Ticket> ticketOpt = ticketRepository.findByTicketCode(rawCode);

        if (ticketOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("status", "INVALID", "message", "❌ Invalid ticket code. Not found in system."));
        }

        Ticket ticket = ticketOpt.get();

        if (ticket.getStatus() == Ticket.TicketStatus.CHECKED_IN) {
            return ResponseEntity.badRequest().body(Map.of("status", "DUPLICATE", "message", "🔴 RED ALERT: Ticket already scanned & used!"));
        }

        if (ticket.getStatus() != Ticket.TicketStatus.ISSUED) {
            return ResponseEntity.badRequest().body(Map.of("status", "INVALID", "message", "❌ Ticket is cancelled or refunded."));
        }

        // Atomically update ticket status & record check-in
        ticket.setStatus(Ticket.TicketStatus.CHECKED_IN);
        ticketRepository.save(ticket);

        // Award +10 Volunteer Points to scanning staff
        int currentPts = scannedBy.getVolunteerPoints() != null ? scannedBy.getVolunteerPoints() : 0;
        scannedBy.setVolunteerPoints(currentPts + 10);
        userRepository.save(scannedBy);

        CheckIn checkIn = CheckIn.builder()
                .ticket(ticket)
                .event(ticket.getBooking().getEvent())
                .scannedBy(scannedBy)
                .scannedAt(LocalDateTime.now())
                .build();
        checkInRepository.save(checkIn);

        return ResponseEntity.ok(Map.of(
                "status", "VALID",
                "message", "🟢 GREEN LIGHT: Ticket Validated (+10 Volunteer Points Earned!)",
                "eventTitle", ticket.getBooking().getEvent().getTitle(),
                "categoryName", ticket.getTicketCategory().getName(),
                "studentName", ticket.getBooking().getUser().getFullName(),
                "studentId", ticket.getBooking().getUser().getStudentId(),
                "scannerPoints", scannedBy.getVolunteerPoints()
        ));
    }

    @GetMapping("/my-tickets")
    public ResponseEntity<?> getMyTickets(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Ticket> tickets = ticketRepository.findByBookingUserId(user.getId());
        return ResponseEntity.ok(tickets);
    }

    @Data
    public static class ScanRequest {
        private String ticketCode;
    }
}
