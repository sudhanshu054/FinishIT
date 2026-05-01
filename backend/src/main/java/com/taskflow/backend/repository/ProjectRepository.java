package com.taskflow.backend.repository;

import com.taskflow.backend.entity.Organization;
import com.taskflow.backend.entity.Project;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByOrganization(Organization organization);
}
