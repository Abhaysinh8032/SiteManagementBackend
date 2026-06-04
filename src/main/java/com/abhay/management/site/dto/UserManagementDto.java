package com.abhay.management.site.dto;

import com.abhay.management.site.enums.UserRole;
import com.abhay.management.site.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

public class UserManagementDto {

    // ── Full user response (admin view) ───────────────────────────────────────
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UserResponse {
        private UUID        id;
        private String      employeeId;
        private String      name;
        private UserRole    role;
        private UserStatus  status;
        private String      phoneNumber;
        private LocalDateTime createdAt;
    }

    // ── Approve / reject request ──────────────────────────────────────────────
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ApprovalRequest {
        // "ACTIVE" to approve, "REJECTED" to reject
        private String decision; // APPROVED | REJECTED
    }

    // ── Notification response ─────────────────────────────────────────────────
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class NotificationResponse {
        private UUID   id;
        private String title;
        private String message;
        private boolean isRead;
        private UUID    taskId;
        private String  taskTitle;
        private LocalDateTime sentAt;
    }
}
