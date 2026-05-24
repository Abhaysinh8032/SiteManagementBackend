package com.abhay.management.site.dto;

import com.abhay.management.site.enums.UserRole;
import com.abhay.management.site.enums.UserStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;


public class AuthDto {

    // ── Signup request ───────────────────────────────────────────────────────
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SignupRequest {

        @NotBlank(message = "Employee ID is required")
        @Size(min = 3, max = 50, message = "Employee ID must be between 3 and 50 characters")
        private String employeeId;

        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        private String name;

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String password;

        @NotNull(message = "Role is required")
        private UserRole role;

        private String phoneNumber;

        // FCM token sent on registration for immediate notification readiness
        private String fcmToken;
    }

    // ── Login request ────────────────────────────────────────────────────────
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LoginRequest {

        @NotBlank(message = "Employee ID is required")
        private String employeeId;

        @NotBlank(message = "Password is required")
        private String password;

        // Updated FCM token on each login
        private String fcmToken;
    }

    // ── Auth response (shared for login & signup) ────────────────────────────
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuthResponse {

        private String token;
        private String tokenType;
        private Long expiresInMs;
        private UserInfo user;
    }

    // ── User info (embedded in auth response) ────────────────────────────────
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserInfo {

        private UUID id;
        private String employeeId;
        private String name;
        private UserRole role;
        private UserStatus status;
    }

    // ── Generic API response wrapper ─────────────────────────────────────────
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ApiResponse {

        private boolean success;
        private String message;
        private Object data;

        public static ApiResponse ok(String message) {
            return ApiResponse.builder().success(true).message(message).build();
        }

        public static ApiResponse ok(String message, Object data) {
            return ApiResponse.builder().success(true).message(message).data(data).build();
        }

        public static ApiResponse error(String message) {
            return ApiResponse.builder().success(false).message(message).build();
        }
    }
}
