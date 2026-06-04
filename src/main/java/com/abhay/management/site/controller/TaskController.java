package com.abhay.management.site.controller;

import com.abhay.management.site.dto.TaskDto;
import com.abhay.management.site.enums.TaskStatus;
import com.abhay.management.site.security.EmployeePrincipal;
import com.abhay.management.site.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    // ── POST /api/tasks — admin only ──────────────────────────────────────────
    // Creates task and assigns it to a worker
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TaskDto.TaskResponse> createTask(
            @Valid @RequestBody TaskDto.CreateTaskRequest request,
            @AuthenticationPrincipal EmployeePrincipal principal) {

        TaskDto.TaskResponse task = taskService.createTask(
                request, UUID.fromString(principal.getUserId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(task);
    }

    // ── GET /api/tasks/site/{siteId} ──────────────────────────────────────────
    // All tasks for a specific site (admin sees all, worker sees only their own)
    @GetMapping("/site/{siteId}")
    public ResponseEntity<List<TaskDto.TaskResponse>> getTasksBySite(
            @PathVariable UUID siteId,
            @AuthenticationPrincipal EmployeePrincipal principal) {

        List<TaskDto.TaskResponse> tasks;
        if ("ADMIN".equals(principal.getRole())) {
            tasks = taskService.getTasksBySite(siteId);
        } else {
            // Worker: filter to only their tasks on this site
            tasks = taskService.getTasksBySite(siteId)
                    .stream()
                    .filter(t -> t.getAssignedTo().getId()
                            .equals(UUID.fromString(principal.getUserId())))
                    .toList();
        }
        return ResponseEntity.ok(tasks);
    }

    // ── GET /api/tasks/my — worker's own tasks (all sites) ───────────────────
    @GetMapping("/my")
    public ResponseEntity<List<TaskDto.TaskResponse>> getMyTasks(
            @AuthenticationPrincipal EmployeePrincipal principal) {
        return ResponseEntity.ok(
                taskService.getTasksByUser(UUID.fromString(principal.getUserId())));
    }

    // ── GET /api/tasks/{id} ───────────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<TaskDto.TaskResponse> getTaskById(@PathVariable UUID id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    // ── PATCH /api/tasks/{id}/status ──────────────────────────────────────────
    // Both admin and assigned worker can update status
    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskDto.TaskResponse> updateTaskStatus(
            @PathVariable UUID id,
            @Valid @RequestBody TaskDto.UpdateStatusRequest request,
            @AuthenticationPrincipal EmployeePrincipal principal) {

        TaskDto.TaskResponse task = taskService.updateTaskStatus(
                id, request.getStatus(), UUID.fromString(principal.getUserId()));
        return ResponseEntity.ok(task);
    }

    // ── POST /api/tasks/{id}/updates — post a work update ─────────────────────
    @PostMapping("/{id}/updates")
    public ResponseEntity<TaskDto.WorkUpdateResponse> addWorkUpdate(
            @PathVariable UUID id,
            @Valid @RequestBody TaskDto.WorkUpdateRequest request,
            @AuthenticationPrincipal EmployeePrincipal principal) {

        TaskDto.WorkUpdateResponse update = taskService.addWorkUpdate(
                id, request, UUID.fromString(principal.getUserId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(update);
    }

    // ── GET /api/tasks/{id}/updates ───────────────────────────────────────────
    @GetMapping("/{id}/updates")
    public ResponseEntity<List<TaskDto.WorkUpdateResponse>> getWorkUpdates(
            @PathVariable UUID id) {
        return ResponseEntity.ok(taskService.getWorkUpdates(id));
    }
}
