package com.skyline.membership.controller;

import com.skyline.auth.model.User;
import com.skyline.auth.repository.UserRepository;
import com.skyline.membership.model.Membership;
import com.skyline.membership.repository.MembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/v1/memberships")
@RequiredArgsConstructor
public class MembershipController {

    private final MembershipRepository membershipRepository;
    private final UserRepository userRepository;

    @GetMapping("/my")
    public ResponseEntity<?> getMyMembership(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<Membership> membershipOpt = membershipRepository.findByUserId(user.getId());

        if (membershipOpt.isPresent()) {
            return ResponseEntity.ok(membershipOpt.get());
        } else {
            return ResponseEntity.ok(Map.of("status", "NO_MEMBERSHIP", "message", "No active membership found"));
        }
    }

    @PostMapping("/pay-dues")
    public ResponseEntity<?> payDues(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Membership membership = membershipRepository.findByUserId(user.getId())
                .orElseGet(() -> Membership.builder()
                        .user(user)
                        .startDate(LocalDate.now())
                        .expiryDate(LocalDate.now().plusYears(1))
                        .duesPaid(new BigDecimal("500.00"))
                        .status(Membership.MembershipStatus.ACTIVE)
                        .build());

        membership.setStatus(Membership.MembershipStatus.ACTIVE);
        membership.setExpiryDate(LocalDate.now().plusYears(1));
        membership.setDuesPaid(new BigDecimal("500.00"));

        membershipRepository.save(membership);

        return ResponseEntity.ok(Map.of("message", "Membership annual dues (₹500.00) paid successfully!", "membership", membership));
    }

    @GetMapping("/verify/{studentId}")
    public ResponseEntity<?> verifyMemberStatus(@PathVariable String studentId) {
        Optional<User> userOpt = userRepository.findByStudentId(studentId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.ok(Map.of("activeMember", false, "reason", "Student ID not found"));
        }

        Optional<Membership> membershipOpt = membershipRepository.findByUserId(userOpt.get().getId());
        boolean isActive = membershipOpt.isPresent() && membershipOpt.get().getStatus() == Membership.MembershipStatus.ACTIVE;

        return ResponseEntity.ok(Map.of(
                "studentId", studentId,
                "fullName", userOpt.get().getFullName(),
                "activeMember", isActive,
                "status", membershipOpt.map(m -> m.getStatus().name()).orElse("NONE")
        ));
    }
}
