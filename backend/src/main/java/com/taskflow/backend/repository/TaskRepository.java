package com.taskflow.backend.repository;

import com.taskflow.backend.entity.AppUser;
import com.taskflow.backend.entity.Organization;
import com.taskflow.backend.entity.Task;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByAssignedTo(AppUser user);

    @Query("select t from Task t where t.project.organization = :organization")
    List<Task> findByOrganization(Organization organization);

    @Query("select count(t) from Task t where t.assignedTo = :user and t.deadline < :now and t.status <> com.taskflow.backend.entity.TaskStatus.DONE")
    long countOverdueByMember(AppUser user, LocalDateTime now);
}
