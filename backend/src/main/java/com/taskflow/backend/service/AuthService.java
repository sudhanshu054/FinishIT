package com.taskflow.backend.service;

import com.taskflow.backend.dto.AuthDtos;
import com.taskflow.backend.entity.AppUser;
import com.taskflow.backend.entity.Organization;
import com.taskflow.backend.repository.OrganizationRepository;
import com.taskflow.backend.repository.UserRepository;
import com.taskflow.backend.security.JwtService;
import com.taskflow.backend.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, OrganizationRepository organizationRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthDtos.AuthResponse signup(AuthDtos.SignupRequest request) {
        userRepository.findByEmailIgnoreCase(request.email()).ifPresent(u -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        });
        Organization org = organizationRepository.findByNameIgnoreCase(request.organizationName())
                .orElseGet(() -> {
                    Organization created = new Organization();
                    created.setName(request.organizationName().trim());
                    return organizationRepository.save(created);
                });

        AppUser user = new AppUser();
        user.setEmail(request.email().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setOrganization(org);
        user.setRole(request.role());
        userRepository.save(user);
        return toResponse(user);
    }

    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        AppUser user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        return toResponse(user);
    }

    private AuthDtos.AuthResponse toResponse(AppUser user) {
        String token = jwtService.generateToken(UserPrincipal.from(user));
        return new AuthDtos.AuthResponse(token, user.getId(), user.getEmail(), user.getRole(), user.getOrganization().getName());
    }
}
