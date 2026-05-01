package com.taskflow.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ProjectDtos {
    public record CreateProjectRequest(
            @NotBlank @Size(max = 150) String name,
            @Size(max = 500) String description
    ) {}

    public record ProjectResponse(Long id, String name, String description) {}
}
