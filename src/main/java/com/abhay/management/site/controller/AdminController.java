package com.abhay.management.site.controller;

import com.abhay.management.site.dto.UserManagementDto;
import com.abhay.management.site.enums.UserStatus;
import com.abhay.management.site.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserManagementService userManagementService;

    // GET /api/admin/users — all users
    @GetMapping("/users")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserManagementDto.UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userManagementService.getAllUsers());
    }

    // GET /api/admin/users/active — for task assignment dropdown
    // NOTE: No @PreAuthorize here — any logged-in user can call this
    @GetMapping("/users/active")
    public ResponseEntity<List<UserManagementDto.UserResponse>> getActiveUsers() {
        return ResponseEntity.ok(userManagementService.getActiveUsers());
    }

    // GET /api/admin/users/pending — pending approval list
    @GetMapping("/users/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserManagementDto.UserResponse>> getPendingUsers() {
        return ResponseEntity.ok(userManagementService.getUsersByStatus(UserStatus.PENDING));
    }

    // PATCH /api/admin/users/{id}/approve
    @PatchMapping("/users/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserManagementDto.UserResponse> approveUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userManagementService.approveUser(id));
    }

    // PATCH /api/admin/users/{id}/reject
    @PatchMapping("/users/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserManagementDto.UserResponse> rejectUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userManagementService.rejectUser(id));
    }

    // PATCH /api/admin/users/{id}/deactivate
    @PatchMapping("/users/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserManagementDto.UserResponse> deactivateUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userManagementService.deactivateUser(id));
    }
}
