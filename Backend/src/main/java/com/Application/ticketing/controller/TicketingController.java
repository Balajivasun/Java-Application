package com.skyline.ticketing.controller;

import com.skyline.ticketing.model.Booking;
import com.skyline.ticketing.repository.BookingRepository;
import com.skyline.ticketing.service.TicketingService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/v1/ticketing")
@RequiredArgsConstructor
public class TicketingController {

    private final TicketingService ticketingService;
    private final BookingRepository bookingRepository;

    @PostMapping("/reserve")
    public ResponseEntity<?> reserveTickets(Authentication authentication, @RequestBody ReserveRequest request) {
        try {
            Booking booking = ticketingService.reserveTickets(
                    authentication.getName(),
                    request.getCategoryId(),
                    request.getQuantity(),
                    request.getIdempotencyKey()
            );
            return ResponseEntity.ok(booking);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "CAPACITY_EXCEEDED", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/simulate-payment/{bookingId}")
    public ResponseEntity<?> simulatePayment(@PathVariable Long bookingId) {
        try {
            Booking booking = ticketingService.confirmPaymentSimulation(bookingId);
            return ResponseEntity.ok(Map.of("message", "Payment processed successfully!", "booking", booking));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/my-bookings")
    public ResponseEntity<?> getMyBookings(Authentication authentication) {
        return ResponseEntity.ok(bookingRepository.findAll());
    }

    @Data
    public static class ReserveRequest {
        private Long categoryId;
        private Integer quantity;
        private String idempotencyKey;
    }
}
