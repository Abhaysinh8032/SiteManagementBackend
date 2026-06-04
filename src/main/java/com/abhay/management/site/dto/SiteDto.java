package com.abhay.management.site.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

public class SiteDto {

    // ── Create / Update request ───────────────────────────────────────────────
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SiteRequest {

        @NotBlank(message = "Site name is required")
        @Size(max = 150, message = "Site name max 150 characters")
        private String name;

        @NotBlank(message = "Location is required")
        @Size(max = 300, message = "Location max 300 characters")
        private String location;

        private String description;
    }

    // ── Response ──────────────────────────────────────────────────────────────
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SiteResponse {
        private UUID   id;
        private String name;
        private String location;
        private String description;
        private boolean isActive;
        private int     memberCount;
        private String  createdByName;
        private UUID    createdById;
        private LocalDateTime createdAt;
    }

    // ── Assign member request ─────────────────────────────────────────────────
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AssignMemberRequest {

        @NotBlank(message = "User ID is required")
        private String userId;
    }

    // ── Member response ───────────────────────────────────────────────────────
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class MemberResponse {
        private UUID   id;
        private UUID   userId;
        private String employeeId;
        private String name;
        private String role;
        private LocalDateTime assignedAt;
    }
}
