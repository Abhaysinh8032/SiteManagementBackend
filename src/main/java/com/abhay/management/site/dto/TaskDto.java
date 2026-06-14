package com.abhay.management.site.dto;

import com.abhay.management.site.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

public class TaskDto {

    // ── Create task request ───────────────────────────────────────────────────
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreateTaskRequest {

        @NotNull(message = "Site ID is required")
        private UUID siteId;

        @NotNull(message = "Assigned user ID is required")
        private UUID assignedToId;

        @NotBlank(message = "Task title is required")
        @Size(max = 200, message = "Title max 200 characters")
        private String title;

        private String description;

        private LocalDateTime dueDate;
    }

    // ── Update task status request ────────────────────────────────────────────
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UpdateStatusRequest {

        @NotNull(message = "Status is required")
        private TaskStatus status;
    }

    // NEW — admin can update description
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UpdateDescriptionRequest {
        @Size(max = 2000)
        private String description;
    }
    // ── Task response ─────────────────────────────────────────────────────────
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class TaskResponse {
        private UUID       id;
        private SiteInfo   site;
        private UserInfo   assignedTo;
        private UserInfo   createdBy;
        private String     title;
        private String     description;
        private TaskStatus status;
        private LocalDateTime dueDate;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private int        updateCount;

        @Data @NoArgsConstructor @AllArgsConstructor @Builder
        public static class SiteInfo {
            private UUID   id;
            private String name;
            private String location;
        }

        @Data @NoArgsConstructor @AllArgsConstructor @Builder
        public static class UserInfo {
            private UUID   id;
            private String employeeId;
            private String name;
            private String role;
        }
    }

    // ── Work update request ───────────────────────────────────────────────────
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class WorkUpdateRequest {

        @NotBlank(message = "Update text is required")
        @Size(max = 2000, message = "Update text max 2000 characters")
        private String updateText;
    }

    // ── Work update response ──────────────────────────────────────────────────
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class WorkUpdateResponse {
        private UUID   id;
        private UUID   taskId;
        private String taskTitle;
        private UUID   userId;
        private String userName;
        private String employeeId;
        private String updateText;
        private String statusAtUpdate;
        private LocalDateTime createdAt;
    }
}
