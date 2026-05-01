package com.taskflow.backend.security;

import com.taskflow.backend.entity.AppUser;
import com.taskflow.backend.entity.Role;

public record UserPrincipal(
        Long id,
        String email,
        Role role,
        Long organizationId
) {
    public static UserPrincipal from(AppUser user) {
        return new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getOrganization().getId()
        );
    }
}
