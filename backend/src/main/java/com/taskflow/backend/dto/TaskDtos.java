package com.taskflow.backend.dto;

import com.taskflow.backend.entity.TaskStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Map;

public class TaskDtos {
    public record CreateTaskRequest(
            @NotNull Long projectId,
            @NotNull Long memberId,
            @NotBlank @Size(max = 180) String title,
            @Size(max = 1000) String description,
            @NotNull @Future LocalDateTime deadline
    ) {}

    public record UpdateTaskStatusRequest(@NotNull TaskStatus status) {}

    public record TaskResponse(
            Long id,
            String title,
            String description,
            TaskStatus status,
            LocalDateTime deadline,
            Long assignedToId,
            String assignedToEmail,
            Long projectId,
            String projectName
    ) {}

    public record DashboardResponse(
            long total,
            long todo,
            long inProgress,
            long done,
            long overdue,
            Map<Long, Long> progressByMember
    ) {}
}
