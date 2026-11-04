package com.skyline.auth.security;

import com.skyline.auth.model.User;
import com.skyline.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Support searching by either email or studentId
        User user = userRepository.findByEmail(username)
                .orElseGet(() -> userRepository.findByStudentId(username)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found with email/studentId: " + username)));

        return CustomUserDetails.build(user);
    }
}
