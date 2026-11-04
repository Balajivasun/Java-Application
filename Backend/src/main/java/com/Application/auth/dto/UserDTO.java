package com.skyline.auth.dto;

import com.skyline.auth.model.Role;
import com.skyline.auth.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String email;
    private String studentId;
    private String fullName;
    private String phone;
    private Role role;
    private Integer volunteerPoints;
    private Boolean approved;
    private LocalDateTime createdAt;

    public static UserDTO fromEntity(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .studentId(user.getStudentId())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .role(user.getRole())
                .volunteerPoints(user.getVolunteerPoints() != null ? user.getVolunteerPoints() : 0)
                .approved(user.getApproved() != null ? user.getApproved() : true)
                .createdAt(user.getCreatedAt())
                .build();
    }
}
