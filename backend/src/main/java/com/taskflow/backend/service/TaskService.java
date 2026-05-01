package com.taskflow.backend.service;

import com.taskflow.backend.dto.TaskDtos;
import com.taskflow.backend.entity.AppUser;
import com.taskflow.backend.entity.Project;
import com.taskflow.backend.entity.Role;
import com.taskflow.backend.entity.Task;
import com.taskflow.backend.entity.TaskStatus;
import com.taskflow.backend.repository.ProjectRepository;
import com.taskflow.backend.repository.TaskRepository;
import com.taskflow.backend.repository.UserRepository;
import com.taskflow.backend.security.UserPrincipal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository, ProjectRepository projectRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
    }

    public TaskDtos.TaskResponse createTask(UserPrincipal principal, TaskDtos.CreateTaskRequest request) {
        AppUser admin = getUser(principal.id());
        AppUser member = getUser(request.memberId());
        if (member.getRole() != Role.MEMBER || !member.getOrganization().getId().equals(admin.getOrganization().getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Member must be part of your organization");
        }
        Project project = projectRepository.findById(request.projectId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        if (!project.getOrganization().getId().equals(admin.getOrganization().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Project out of your organization");
        }
        Task task = new Task();
        task.setTitle(request.title().trim());
        task.setDescription(request.description());
        task.setDeadline(request.deadline());
        task.setStatus(TaskStatus.TODO);
        task.setAssignedTo(member);
        task.setCreatedBy(admin);
        task.setProject(project);
        return toResponse(taskRepository.save(task));
    }

    public java.util.List<TaskDtos.TaskResponse> listMyTasks(UserPrincipal principal) {
        AppUser user = getUser(principal.id());
        if (user.getRole() == Role.ADMIN) {
            return taskRepository.findByOrganization(user.getOrganization()).stream().map(this::toResponse).toList();
        }
        return taskRepository.findByAssignedTo(user).stream().map(this::toResponse).toList();
    }

    public TaskDtos.TaskResponse updateStatus(UserPrincipal principal, Long taskId, TaskDtos.UpdateTaskStatusRequest request) {
        AppUser user = getUser(principal.id());
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        if (!task.getAssignedTo().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only assignee can update progress");
        }
        task.setStatus(request.status());
        return toResponse(taskRepository.save(task));
    }

    public TaskDtos.DashboardResponse dashboard(UserPrincipal principal) {
        AppUser user = getUser(principal.id());
        var tasks = user.getRole() == Role.ADMIN
                ? taskRepository.findByOrganization(user.getOrganization())
                : taskRepository.findByAssignedTo(user);
        long total = tasks.size();
        long todo = tasks.stream().filter(t -> t.getStatus() == TaskStatus.TODO).count();
        long inProgress = tasks.stream().filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS).count();
        long done = tasks.stream().filter(t -> t.getStatus() == TaskStatus.DONE).count();
        long overdue = user.getRole() == Role.ADMIN
                ? tasks.stream().filter(t -> t.getDeadline().isBefore(LocalDateTime.now()) && t.getStatus() != TaskStatus.DONE).count()
                : taskRepository.countOverdueByMember(user, LocalDateTime.now());

        Map<Long, Long> progressByMember = tasks.stream()
                .collect(Collectors.groupingBy(t -> t.getAssignedTo().getId(), Collectors.counting()));
        return new TaskDtos.DashboardResponse(total, todo, inProgress, done, overdue, progressByMember);
    }

    public java.util.List<Map<String, Object>> listMembers(UserPrincipal principal) {
        AppUser user = getUser(principal.id());
        return userRepository.findByOrganizationAndRole(user.getOrganization(), Role.MEMBER).stream()
                .map(m -> Map.<String, Object>of("id", m.getId(), "email", m.getEmail()))
                .toList();
    }

    private TaskDtos.TaskResponse toResponse(Task task) {
        return new TaskDtos.TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getDeadline(),
                task.getAssignedTo().getId(),
                task.getAssignedTo().getEmail(),
                task.getProject().getId(),
                task.getProject().getName()
        );
    }

    private AppUser getUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
