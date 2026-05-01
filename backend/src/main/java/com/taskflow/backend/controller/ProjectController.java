package com.taskflow.backend.controller;

import com.taskflow.backend.dto.ProjectDtos;
import com.taskflow.backend.security.UserPrincipal;
import com.taskflow.backend.service.ProjectService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ProjectDtos.ProjectResponse create(@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody ProjectDtos.CreateProjectRequest request) {
        return projectService.create(principal, request);
    }

    @GetMapping
    public List<ProjectDtos.ProjectResponse> list(@AuthenticationPrincipal UserPrincipal principal) {
        return projectService.list(principal);
    }
}
