package com.taskflow.backend.service;

import com.taskflow.backend.dto.ProjectDtos;
import com.taskflow.backend.entity.AppUser;
import com.taskflow.backend.entity.Project;
import com.taskflow.backend.repository.ProjectRepository;
import com.taskflow.backend.repository.UserRepository;
import com.taskflow.backend.security.UserPrincipal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    public ProjectDtos.ProjectResponse create(UserPrincipal principal, ProjectDtos.CreateProjectRequest request) {
        AppUser admin = getUser(principal.id());
        Project project = new Project();
        project.setName(request.name().trim());
        project.setDescription(request.description());
        project.setOrganization(admin.getOrganization());
        project.setCreatedBy(admin);
        Project saved = projectRepository.save(project);
        return new ProjectDtos.ProjectResponse(saved.getId(), saved.getName(), saved.getDescription());
    }

    public List<ProjectDtos.ProjectResponse> list(UserPrincipal principal) {
        AppUser user = getUser(principal.id());
        return projectRepository.findByOrganization(user.getOrganization())
                .stream().map(p -> new ProjectDtos.ProjectResponse(p.getId(), p.getName(), p.getDescription())).toList();
    }

    private AppUser getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
