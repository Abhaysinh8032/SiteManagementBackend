package com.abhay.management.site.service;

import com.abhay.management.site.dto.AuthDto;
import com.abhay.management.site.entity.User;
import com.abhay.management.site.enums.UserRole;
import com.abhay.management.site.enums.UserStatus;
import com.abhay.management.site.repository.UserRepository;
import com.abhay.management.site.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private static final String PENDING = "PENDING";
	private static final String REJECTED = "REJECTED";
	private static final String INACTIVE = "INACTIVE";
	private static final String ACTIVE = "ACTIVE";
	
	@Autowired
	private  UserRepository userRepository;
	
	@Autowired
    private  PasswordEncoder passwordEncoder;
	
	@Autowired
    private  JwtUtil jwtUtil;

    // ────────────────────────────────────────────────────────────────────────
    //  SIGNUP
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Register a new user.
     *
     * Business rules:
     * - Employee ID must be unique.
     * - Admins are set to ACTIVE immediately (first admin bootstraps the system).
     * - Workers are set to PENDING — they cannot log in until an admin approves them.
     * - No JWT is returned for PENDING workers; they should see "awaiting approval" screen.
     */
    @Transactional
    public AuthDto.ApiResponse signup(AuthDto.SignupRequest request) {

        // 1. Check for duplicate employee ID
        if (userRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new IllegalArgumentException(
                    "Employee ID '" + request.getEmployeeId() + "' is already registered.");
        }

        // 2. Determine initial status
        //    Admins are activated immediately; workers need approval
        UserStatus initialStatus = request.getRole() == UserRole.ADMIN
                ? UserStatus.ACTIVE
                : UserStatus.PENDING;

        // 3. Build and persist the user
        User user = User.builder()
                .employeeId(request.getEmployeeId())
                .name(request.getName())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .status(initialStatus)
                .phoneNumber(request.getPhoneNumber())
                .fcmToken(request.getFcmToken())
                .build();

        user = userRepository.save(user);
        log.info("New user registered: {} [{}] status={}", user.getEmployeeId(), user.getRole(), user.getStatus());

        // 4. Return appropriate message
        if (initialStatus == UserStatus.PENDING) {
            return AuthDto.ApiResponse.ok(
                    "Registration successful. Your account is pending approval by a supervisor.",
                    AuthDto.UserInfo.builder()
                            .id(user.getId())
                            .employeeId(user.getEmployeeId())
                            .name(user.getName())
                            .role(user.getRole())
                            .status(user.getStatus())
                            .build()
            );
        }

        // Admin: return JWT immediately
        String token = jwtUtil.generateToken(
                user.getEmployeeId(),
                user.getRole().name(),
                user.getId().toString());

        return AuthDto.ApiResponse.ok(
                "Admin account created successfully.",
                buildAuthResponse(user, token)
        );
    }

    // ────────────────────────────────────────────────────────────────────────
    //  LOGIN
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Authenticate a user with Employee ID + password.
     *
     * Business rules:
     * - PENDING users receive a clear "not yet approved" error.
     * - REJECTED / INACTIVE users are blocked with a descriptive message.
     * - On successful login the FCM token is refreshed so push notifications
     *   always reach the correct device.
     * - A fresh JWT is generated and returned.
     */
    @Transactional
    public AuthDto.AuthResponse login(AuthDto.LoginRequest request) {

        // 1. Locate user by employee ID
        User user = userRepository.findByEmployeeId(request.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid Employee ID or password."));

        // 2. Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid Employee ID or password.");
        }

        // 3. Enforce account status checks
        switch (user.getStatus()) {
            case PENDING -> throw new IllegalStateException(
                    "Your account is awaiting approval from a supervisor. Please try again later.");
            case REJECTED -> throw new IllegalStateException(
                    "Your account has been rejected. Please contact your site manager.");
            case INACTIVE -> throw new IllegalStateException(
                    "Your account has been deactivated. Please contact your site manager.");
            case ACTIVE -> {
                // All good — proceed
            }
        }

        // 4. Refresh FCM token if provided (device may have rotated it)
        if (request.getFcmToken() != null && !request.getFcmToken().isBlank()) {
            user.setFcmToken(request.getFcmToken());
            userRepository.save(user);
            log.debug("FCM token refreshed for user: {}", user.getEmployeeId());
        }

        // 5. Generate JWT
        String token = jwtUtil.generateToken(
                user.getEmployeeId(),
                user.getRole().name(),
                user.getId().toString());

        log.info("User logged in: {} [{}]", user.getEmployeeId(), user.getRole());

        return buildAuthResponse(user, token);
    }

    // ── private helpers ───────────────────────────────────────────────────────

    private AuthDto.AuthResponse buildAuthResponse(User user, String token) {
        return AuthDto.AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresInMs(jwtUtil.getExpirationMs())
                .user(AuthDto.UserInfo.builder()
                        .id(user.getId())
                        .employeeId(user.getEmployeeId())
                        .name(user.getName())
                        .role(user.getRole())
                        .status(user.getStatus())
                        .build())
                .build();
    }
}
