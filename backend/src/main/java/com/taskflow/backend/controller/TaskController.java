package com.taskflow.backend.controller;

import com.taskflow.backend.dto.TaskDtos;
import com.taskflow.backend.security.UserPrincipal;
import com.taskflow.backend.service.TaskService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public TaskDtos.TaskResponse create(@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody TaskDtos.CreateTaskRequest request) {
        return taskService.createTask(principal, request);
    }

    @GetMapping
    public List<TaskDtos.TaskResponse> listMyTasks(@AuthenticationPrincipal UserPrincipal principal) {
        return taskService.listMyTasks(principal);
    }

    @PreAuthorize("hasRole('MEMBER')")
    @PatchMapping("/{id}/status")
    public TaskDtos.TaskResponse updateStatus(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("id") Long id,
            @Valid @RequestBody TaskDtos.UpdateTaskStatusRequest request
    ) {
        return taskService.updateStatus(principal, id, request);
    }

    @GetMapping("/dashboard")
    public TaskDtos.DashboardResponse dashboard(@AuthenticationPrincipal UserPrincipal principal) {
        return taskService.dashboard(principal);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/members")
    public List<Map<String, Object>> members(@AuthenticationPrincipal UserPrincipal principal) {
        return taskService.listMembers(principal);
    }
}
