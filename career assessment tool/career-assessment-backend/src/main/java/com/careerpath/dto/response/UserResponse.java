package com.careerpath.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String grade;
    private String bio;
    private String city;
    private String role;        // "STUDENT" or "ADMIN"
    private String createdAt;

    // Assessment completion status (populated for admin student list)
    private String personalityStatus;
    private String skillsStatus;
    private String interestStatus;
}
