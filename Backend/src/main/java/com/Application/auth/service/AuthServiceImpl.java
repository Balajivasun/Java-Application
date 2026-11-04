package com.skyline.auth.service;

import com.skyline.auth.dto.*;
import com.skyline.auth.model.RevokedToken;
import com.skyline.auth.model.User;
import com.skyline.auth.repository.RevokedTokenRepository;
import com.skyline.auth.repository.UserRepository;
import com.skyline.auth.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RevokedTokenRepository revokedTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Error: Email is already in use!");
        }

        if (userRepository.existsByStudentId(request.getStudentId())) {
            throw new IllegalArgumentException("Error: Student ID is already registered!");
        }

        boolean isPending = request.getRole() == com.skyline.auth.model.Role.EVENT_COORDINATOR;

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .studentId(request.getStudentId())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(request.getRole())
                .approved(!isPending)
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);

        String token = jwtUtils.generateTokenFromUsername(user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .user(UserDTO.fromEntity(user))
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        // Retrieve email if user provided studentId or email
        User user = userRepository.findByEmail(request.getUsername())
                .orElseGet(() -> userRepository.findByStudentId(request.getUsername())
                        .orElseThrow(() -> new IllegalArgumentException("Invalid username or password")));

        if (user.getRole() == com.skyline.auth.model.Role.EVENT_COORDINATOR && Boolean.FALSE.equals(user.getApproved())) {
            throw new IllegalArgumentException("Your Event Coordinator registration is pending approval by Staff or Super Admin.");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getEmail(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        return AuthResponse.builder()
                .token(jwt)
                .tokenType("Bearer")
                .user(UserDTO.fromEntity(user))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getCurrentUser(String username) {
        User user = userRepository.findByEmail(username)
                .orElseGet(() -> userRepository.findByStudentId(username)
                        .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + username)));
        return UserDTO.fromEntity(user);
    }

    @Override
    @Transactional
    public MessageResponse logout(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            String jwt = token.substring(7);
            if (!revokedTokenRepository.existsByToken(jwt)) {
                RevokedToken revokedToken = RevokedToken.builder()
                        .token(jwt)
                        .revokedAt(LocalDateTime.now())
                        .build();
                revokedTokenRepository.save(revokedToken);
            }
        }
        return new MessageResponse("Logged out successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<UserDTO> getPendingCoordinators() {
        return userRepository.findByRoleAndApproved(com.skyline.auth.model.Role.EVENT_COORDINATOR, false)
                .stream()
                .map(UserDTO::fromEntity)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional
    public MessageResponse approveCoordinator(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        user.setApproved(true);
        userRepository.save(user);
        return new MessageResponse("Event Coordinator approved successfully");
    }

    @Override
    @Transactional
    public MessageResponse rejectCoordinator(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        userRepository.delete(user);
        return new MessageResponse("Event Coordinator request rejected");
    }
}
