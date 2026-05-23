package com.kedada.backend.auth.service;

import com.kedada.backend.auth.dto.AuthResponse;
import com.kedada.backend.auth.dto.LoginRequest;
import com.kedada.backend.auth.dto.RegisterRequest;
import com.kedada.backend.auth.entity.AppUser;
import com.kedada.backend.auth.repository.AppUserRepository;
import com.kedada.backend.auth.security.AuthenticatedUser;
import com.kedada.backend.auth.security.JwtService;
import com.kedada.backend.common.exception.BusinessConflictException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AppUserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(AppUserRepository repository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (repository.existsByEmailIgnoreCase(email)) {
            throw new BusinessConflictException("Email is already registered");
        }

        AppUser user = new AppUser();
        user.setEmail(email);
        user.setName(request.name().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole("USER");

        return toAuthResponse(repository.save(user));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        AppUser user = repository.findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }
        return toAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse currentUser(AuthenticatedUser authenticatedUser) {
        AppUser user = repository.findById(authenticatedUser.id())
                .orElseThrow(() -> new BadCredentialsException("Invalid token"));
        return toAuthResponse(user);
    }

    private AuthResponse toAuthResponse(AppUser user) {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole()
        );
        return toAuthResponse(authenticatedUser);
    }

    private AuthResponse toAuthResponse(AuthenticatedUser user) {
        return new AuthResponse("Bearer", jwtService.createToken(user), user.id(), user.email(), user.name());
    }
}
