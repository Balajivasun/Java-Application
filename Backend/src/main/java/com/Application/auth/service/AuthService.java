package com.skyline.auth.service;

import com.skyline.auth.dto.*;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    UserDTO getCurrentUser(String username);

    MessageResponse logout(String token);

    java.util.List<UserDTO> getPendingCoordinators();

    MessageResponse approveCoordinator(Long userId);

    MessageResponse rejectCoordinator(Long userId);
}
