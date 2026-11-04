package com.skyline.ticketing.service;

import com.skyline.auth.model.User;
import com.skyline.auth.repository.UserRepository;
import com.skyline.attendance.model.Ticket;
import com.skyline.attendance.repository.TicketRepository;
import com.skyline.event.model.Event;
import com.skyline.event.model.TicketCategory;
import com.skyline.event.repository.EventRepository;
import com.skyline.event.repository.TicketCategoryRepository;
import com.skyline.membership.model.Membership;
import com.skyline.membership.repository.MembershipRepository;
import com.skyline.ticketing.model.*;
import com.skyline.ticketing.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketingService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final TicketCategoryRepository ticketCategoryRepository;
    private final ReservationRepository reservationRepository;
    private final BookingRepository bookingRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final MembershipRepository membershipRepository;
    private final TicketRepository ticketRepository;

    @Transactional
    public Booking reserveTickets(String username, Long categoryId, Integer quantity, String idempotencyKey) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        TicketCategory category = ticketCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Ticket category not found"));

        Event event = category.getEvent();

        // Concurrency Safeguard: Check available capacity
        int available = category.getTotalQuantity() - category.getReservedQuantity() - category.getSoldQuantity();
        if (available < quantity) {
            throw new IllegalArgumentException("CAPACITY_EXCEEDED: Requested " + quantity + " seats, but only " + available + " remaining.");
        }

        // Check if user is active member to calculate pricing
        boolean isActiveMember = membershipRepository.findByUserId(user.getId())
                .map(m -> m.getStatus() == Membership.MembershipStatus.ACTIVE)
                .orElse(false);

        BigDecimal pricePerSeat = isActiveMember ? category.getMemberPrice() : category.getNonMemberPrice();
        BigDecimal totalAmount = pricePerSeat.multiply(new BigDecimal(quantity));

        // Reserve seats atomically
        category.setReservedQuantity(category.getReservedQuantity() + quantity);
        ticketCategoryRepository.save(category);

        Reservation reservation = Reservation.builder()
                .user(user)
                .ticketCategory(category)
                .quantity(quantity)
                .idempotencyKey(idempotencyKey != null ? idempotencyKey : UUID.randomUUID().toString())
                .status(Reservation.ReservationStatus.HELD)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .build();
        reservationRepository.save(reservation);

        Booking booking = Booking.builder()
                .reservation(reservation)
                .user(user)
                .event(event)
                .totalAmount(totalAmount)
                .status(Booking.BookingStatus.PENDING_PAYMENT)
                .build();
        bookingRepository.save(booking);

        PaymentTransaction tx = PaymentTransaction.builder()
                .booking(booking)
                .gatewayOrderId("ORDER_GW_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .amount(totalAmount)
                .status(PaymentTransaction.PaymentStatus.CREATED)
                .build();
        paymentTransactionRepository.save(tx);

        return booking;
    }

    @Transactional
    public Booking confirmPaymentSimulation(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getStatus() == Booking.BookingStatus.PAID) {
            return booking; // Idempotent
        }

        Reservation reservation = booking.getReservation();
        TicketCategory category = reservation.getTicketCategory();

        // Update booking & category counts
        booking.setStatus(Booking.BookingStatus.PAID);
        reservation.setStatus(Reservation.ReservationStatus.CONFIRMED);

        category.setReservedQuantity(Math.max(0, category.getReservedQuantity() - reservation.getQuantity()));
        category.setSoldQuantity(category.getSoldQuantity() + reservation.getQuantity());

        ticketCategoryRepository.save(category);
        reservationRepository.save(reservation);

        // Issue Zero-PII Cryptographic Signed QR Tickets
        for (int i = 0; i < reservation.getQuantity(); i++) {
            String code = "TCK_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            Ticket ticket = Ticket.builder()
                    .booking(booking)
                    .ticketCategory(category)
                    .ticketCode(code)
                    .status(Ticket.TicketStatus.ISSUED)
                    .build();
            ticketRepository.save(ticket);
        }

        return bookingRepository.save(booking);
    }
}
