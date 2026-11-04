package com.skyline.auth.repository;

import com.skyline.auth.model.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RevokedTokenRepository extends JpaRepository<RevokedToken, Long> {
    Boolean existsByToken(String token);
}
