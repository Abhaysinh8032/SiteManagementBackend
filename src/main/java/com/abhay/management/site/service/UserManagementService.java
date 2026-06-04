package com.abhay.management.site.service;

import com.abhay.management.site.dto.UserManagementDto;
import com.abhay.management.site.entity.Notification;
import com.abhay.management.site.entity.User;
import com.abhay.management.site.enums.UserStatus;
import com.abhay.management.site.repository.NotificationRepository;
import com.abhay.management.site.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserManagementService {

    private final UserRepository         userRepository;
    private final NotificationRepository notificationRepository;

    // ── Get all users (admin) ─────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<UserManagementDto.UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());
    }

    // ── Get users by status — e.g. all PENDING workers ────────────────────────

    @Transactional(readOnly = true)
    public List<UserManagementDto.UserResponse> getUsersByStatus(UserStatus status) {
        return userRepository.findAllByStatus(status)
                .stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());
    }

    // ── Get all ACTIVE users (used for task assignment dropdown) ──────────────

    @Transactional(readOnly = true)
    public List<UserManagementDto.UserResponse> getActiveUsers() {
        return userRepository.findAllByStatus(UserStatus.ACTIVE)
                .stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());
    }

    // ── Approve a pending worker ───────────────────────────────────────────────

    @Transactional
    public UserManagementDto.UserResponse approveUser(UUID userId) {
        User user = findUserById(userId);

        if (user.getStatus() != UserStatus.PENDING) {
            throw new IllegalStateException(
                    "User is not in PENDING state. Current status: " + user.getStatus());
        }

        user.setStatus(UserStatus.ACTIVE);
        user = userRepository.save(user);
        log.info("User {} approved and set to ACTIVE", user.getEmployeeId());
        return toUserResponse(user);
    }

    // ── Reject a pending worker ───────────────────────────────────────────────

    @Transactional
    public UserManagementDto.UserResponse rejectUser(UUID userId) {
        User user = findUserById(userId);

        if (user.getStatus() != UserStatus.PENDING) {
            throw new IllegalStateException(
                    "User is not in PENDING state. Current status: " + user.getStatus());
        }

        user.setStatus(UserStatus.REJECTED);
        user = userRepository.save(user);
        log.info("User {} rejected", user.getEmployeeId());
        return toUserResponse(user);
    }

    // ── Deactivate a user ─────────────────────────────────────────────────────

    @Transactional
    public UserManagementDto.UserResponse deactivateUser(UUID userId) {
        User user = findUserById(userId);
        user.setStatus(UserStatus.INACTIVE);
        user = userRepository.save(user);
        log.info("User {} deactivated", user.getEmployeeId());
        return toUserResponse(user);
    }

    // ── Get notifications for a user ──────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<UserManagementDto.NotificationResponse> getNotifications(UUID userId) {
        return notificationRepository.findAllByUserIdOrderBySentAtDesc(userId)
                .stream()
                .map(this::toNotificationResponse)
                .collect(Collectors.toList());
    }

    // ── Mark notification as read ─────────────────────────────────────────────

    @Transactional
    public void markNotificationRead(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found."));

        if (!notification.getUser().getId().equals(userId)) {
            throw new IllegalStateException("Not authorized to mark this notification.");
        }

        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    // ── Mark all notifications as read ────────────────────────────────────────

    @Transactional
    public void markAllNotificationsRead(UUID userId) {
        List<Notification> unread = notificationRepository.findAllByUserIdAndIsReadFalse(userId);
        LocalDateTime now = LocalDateTime.now();
        unread.forEach(n -> {
            n.setIsRead(true);
            n.setReadAt(now);
        });
        notificationRepository.saveAll(unread);
    }

    // ── Unread notification count ─────────────────────────────────────────────

    @Transactional(readOnly = true)
    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private User findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
    }

    private UserManagementDto.UserResponse toUserResponse(User user) {
        return UserManagementDto.UserResponse.builder()
                .id(user.getId())
                .employeeId(user.getEmployeeId())
                .name(user.getName())
                .role(user.getRole())
                .status(user.getStatus())
                .phoneNumber(user.getPhoneNumber())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private UserManagementDto.NotificationResponse toNotificationResponse(Notification n) {
        return UserManagementDto.NotificationResponse.builder()
                .id(n.getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .isRead(n.getIsRead())
                .taskId(n.getTask() != null ? n.getTask().getId() : null)
                .taskTitle(n.getTask() != null ? n.getTask().getTitle() : null)
                .sentAt(n.getSentAt())
                .build();
    }
}
