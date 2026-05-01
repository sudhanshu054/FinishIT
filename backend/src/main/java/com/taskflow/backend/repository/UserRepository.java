package com.taskflow.backend.repository;

import com.taskflow.backend.entity.AppUser;
import com.taskflow.backend.entity.Organization;
import com.taskflow.backend.entity.Role;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmailIgnoreCase(String email);
    List<AppUser> findByOrganizationAndRole(Organization organization, Role role);
}
