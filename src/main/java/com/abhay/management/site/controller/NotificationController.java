package com.abhay.management.site.controller;

import com.abhay.management.site.dto.AuthDto;
import com.abhay.management.site.dto.UserManagementDto;
import com.abhay.management.site.security.EmployeePrincipal;
import com.abhay.management.site.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final UserManagementService userManagementService;

    // ── GET /api/notifications — my notifications ─────────────────────────────
    @GetMapping
    public ResponseEntity<List<UserManagementDto.NotificationResponse>> getMyNotifications(
            @AuthenticationPrincipal EmployeePrincipal principal) {
        return ResponseEntity.ok(
                userManagementService.getNotifications(UUID.fromString(principal.getUserId())));
    }

    // ── GET /api/notifications/unread-count ───────────────────────────────────
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @AuthenticationPrincipal EmployeePrincipal principal) {
        long count = userManagementService.getUnreadCount(
                UUID.fromString(principal.getUserId()));
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    // ── PATCH /api/notifications/{id}/read ───────────────────────────────────
    @PatchMapping("/{id}/read")
    public ResponseEntity<AuthDto.ApiResponse> markRead(
            @PathVariable UUID id,
            @AuthenticationPrincipal EmployeePrincipal principal) {
        userManagementService.markNotificationRead(
                id, UUID.fromString(principal.getUserId()));
        return ResponseEntity.ok(AuthDto.ApiResponse.ok("Notification marked as read."));
    }

    // ── PATCH /api/notifications/read-all ────────────────────────────────────
    @PatchMapping("/read-all")
    public ResponseEntity<AuthDto.ApiResponse> markAllRead(
            @AuthenticationPrincipal EmployeePrincipal principal) {
        userManagementService.markAllNotificationsRead(
                UUID.fromString(principal.getUserId()));
        return ResponseEntity.ok(AuthDto.ApiResponse.ok("All notifications marked as read."));
    }
}
