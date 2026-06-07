package com.abhay.management.site.service;

import com.abhay.management.site.dto.TaskDto;
import com.abhay.management.site.entity.*;
import com.abhay.management.site.enums.TaskStatus;
import com.abhay.management.site.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository       taskRepository;
    private final SiteRepository       siteRepository;
    private final UserRepository       userRepository;
    private final WorkUpdateRepository workUpdateRepository;
    private final NotificationRepository notificationRepository;
    private final SiteMemberRepository   siteMemberRepository;   // ← added

    // ── Create and assign task (admin only) ───────────────────────────────────

    @Transactional
    public TaskDto.TaskResponse createTask(TaskDto.CreateTaskRequest request, UUID createdByAdminId) {
        Site site = siteRepository.findById(request.getSiteId())
                .orElseThrow(() -> new IllegalArgumentException("Site not found."));

        User assignedTo = userRepository.findById(request.getAssignedToId())
                .orElseThrow(() -> new IllegalArgumentException("Assigned user not found."));

        User createdBy = userRepository.findById(createdByAdminId)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found."));

        // ── AUTO-ASSIGN: add worker to site if not already a member ──────────
        // This means admin only needs to create a task — no separate
        // "Manage Members" step required. Worker automatically sees
        // the site on their next login.
        if (!siteMemberRepository.existsBySiteIdAndUserId(
                site.getId(), assignedTo.getId())) {

            SiteMember membership = SiteMember.builder()
                    .site(site)
                    .user(assignedTo)
                    .assignedBy(createdBy)
                    .build();
            siteMemberRepository.save(membership);

            log.info("Auto-assigned {} to site '{}' via task creation by {}",
                    assignedTo.getEmployeeId(),
                    site.getName(),
                    createdBy.getEmployeeId());
        } else {
            log.debug("{} is already a member of site '{}'",
                    assignedTo.getEmployeeId(), site.getName());
        }
        // ── END AUTO-ASSIGN ──────────────────────────────────────────────────

        Task task = Task.builder()
                .site(site)
                .assignedTo(assignedTo)
                .createdBy(createdBy)
                .title(request.getTitle())
                .description(request.getDescription())
                .status(TaskStatus.PENDING)
                .dueDate(request.getDueDate())
                .build();

        task = taskRepository.save(task);
        log.info("Task '{}' created for user {} on site {} by admin {}",
                task.getTitle(), assignedTo.getEmployeeId(),
                site.getName(), createdBy.getEmployeeId());

        // Save notification record (FCM push will be wired in Cycle 2)
        saveNotification(task, assignedTo);

        return toResponse(task);
    }

    // ── Get tasks for a site ──────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<TaskDto.TaskResponse> getTasksBySite(UUID siteId) {
        return taskRepository.findAllBySiteId(siteId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── Get tasks assigned to a worker ────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<TaskDto.TaskResponse> getTasksByUser(UUID userId) {
        return taskRepository.findAllByAssignedToId(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── Get single task ───────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public TaskDto.TaskResponse getTaskById(UUID taskId) {
        return toResponse(findTaskById(taskId));
    }

    // ── Update task status ────────────────────────────────────────────────────

    @Transactional
    public TaskDto.TaskResponse updateTaskStatus(UUID taskId, TaskStatus newStatus, UUID requestingUserId) {
        Task task = findTaskById(taskId);

        // Worker can only update status of tasks assigned to them
        if (!task.getAssignedTo().getId().equals(requestingUserId) &&
                !task.getCreatedBy().getId().equals(requestingUserId)) {
            throw new IllegalStateException("You are not authorized to update this task.");
        }

        task.setStatus(newStatus);
        task = taskRepository.save(task);
        log.info("Task '{}' status updated to {} by user {}", task.getTitle(), newStatus, requestingUserId);
        return toResponse(task);
    }

    // ── Post a work update ────────────────────────────────────────────────────

    @Transactional
    public TaskDto.WorkUpdateResponse addWorkUpdate(UUID taskId, TaskDto.WorkUpdateRequest request, UUID userId) {
        Task task = findTaskById(taskId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        WorkUpdate update = WorkUpdate.builder()
                .task(task)
                .user(user)
                .updateText(request.getUpdateText())
                .statusAtUpdate(task.getStatus().name())
                .build();

        update = workUpdateRepository.save(update);
        log.info("Work update posted on task '{}' by {}", task.getTitle(), user.getEmployeeId());
        return toWorkUpdateResponse(update);
    }

    // ── Get work updates for a task ───────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<TaskDto.WorkUpdateResponse> getWorkUpdates(UUID taskId) {
        findTaskById(taskId); // validate task exists
        return workUpdateRepository.findAllByTaskIdOrderByCreatedAtDesc(taskId)
                .stream()
                .map(this::toWorkUpdateResponse)
                .collect(Collectors.toList());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Task findTaskById(UUID taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found."));
    }

    private void saveNotification(Task task, User recipient) {
        Notification notification = Notification.builder()
                .user(recipient)
                .task(task)
                .title("New Task Assigned")
                .message("You have been assigned: " + task.getTitle() +
                        " on site " + task.getSite().getName())
                .isRead(false)
                .fcmSent(false) // will be true after FCM integration in Cycle 2
                .build();
        notificationRepository.save(notification);
    }

    private TaskDto.TaskResponse toResponse(Task task) {
        int updateCount = workUpdateRepository.findAllByTaskIdOrderByCreatedAtDesc(task.getId()).size();
        return TaskDto.TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .dueDate(task.getDueDate())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .updateCount(updateCount)
                .site(TaskDto.TaskResponse.SiteInfo.builder()
                        .id(task.getSite().getId())
                        .name(task.getSite().getName())
                        .location(task.getSite().getLocation())
                        .build())
                .assignedTo(TaskDto.TaskResponse.UserInfo.builder()
                        .id(task.getAssignedTo().getId())
                        .employeeId(task.getAssignedTo().getEmployeeId())
                        .name(task.getAssignedTo().getName())
                        .role(task.getAssignedTo().getRole().name())
                        .build())
                .createdBy(TaskDto.TaskResponse.UserInfo.builder()
                        .id(task.getCreatedBy().getId())
                        .employeeId(task.getCreatedBy().getEmployeeId())
                        .name(task.getCreatedBy().getName())
                        .role(task.getCreatedBy().getRole().name())
                        .build())
                .build();
    }

    private TaskDto.WorkUpdateResponse toWorkUpdateResponse(WorkUpdate update) {
        return TaskDto.WorkUpdateResponse.builder()
                .id(update.getId())
                .taskId(update.getTask().getId())
                .taskTitle(update.getTask().getTitle())
                .userId(update.getUser().getId())
                .userName(update.getUser().getName())
                .employeeId(update.getUser().getEmployeeId())
                .updateText(update.getUpdateText())
                .statusAtUpdate(update.getStatusAtUpdate())
                .createdAt(update.getCreatedAt())
                .build();
    }
}
