package com.skyline.ticketing.repository;

import com.skyline.ticketing.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Optional<Reservation> findByIdempotencyKey(String idempotencyKey);
}
