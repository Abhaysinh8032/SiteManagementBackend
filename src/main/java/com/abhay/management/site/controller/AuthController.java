package com.abhay.management.site.controller;

import com.abhay.management.site.dto.AuthDto;
import com.abhay.management.site.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

	@Autowired
    private  AuthService authService;

    /**
     * POST /api/auth/signup
     *
     * Register a new user (worker or admin).
     * Workers will receive status=PENDING and no JWT token.
     * Admins will receive status=ACTIVE and a JWT token immediately.
     *
     * Request body:
     * {
     *   "employeeId": "EMP001",
     *   "name": "Ravi Kumar",
     *   "password": "securePass123",
     *   "role": "WORKER",          // or "ADMIN"
     *   "phoneNumber": "9876543210",  // optional
     *   "fcmToken": "fcm-device-token" // optional
     * }
     */
    @PostMapping("/signup")
    public ResponseEntity<AuthDto.ApiResponse> signup(
            @Valid @RequestBody AuthDto.SignupRequest request) {
        try {
            AuthDto.ApiResponse response = authService.signup(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(AuthDto.ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * POST /api/auth/login
     *
     * Authenticate with Employee ID + password.
     * Returns a JWT Bearer token on success.
     *
     * Request body:
     * {
     *   "employeeId": "EMP001",
     *   "password": "securePass123",
     *   "fcmToken": "updated-fcm-token"  // optional, refreshes push token
     * }
     *
     * Response:
     * {
     *   "token": "eyJhb...",
     *   "tokenType": "Bearer",
     *   "expiresInMs": 86400000,
     *   "user": {
     *     "id": "uuid",
     *     "employeeId": "EMP001",
     *     "name": "Ravi Kumar",
     *     "role": "WORKER",
     *     "status": "ACTIVE"
     *   }
     * }
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody AuthDto.LoginRequest request) {
        try {
            AuthDto.AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            // Bad credentials
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(AuthDto.ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            // Pending / rejected / inactive account
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(AuthDto.ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * GET /api/auth/health
     * Quick health check — no auth required.
     */
    @GetMapping("/health")
    public ResponseEntity<AuthDto.ApiResponse> health() {
        return ResponseEntity.ok(AuthDto.ApiResponse.ok("Construction Tracker API is running."));
    }
}
