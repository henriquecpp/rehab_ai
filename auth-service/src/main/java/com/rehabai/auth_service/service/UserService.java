package com.rehabai.auth_service.service;

import com.rehabai.auth_service.dto.RegisterRequest;
import com.rehabai.auth_service.model.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {

    private final UserServiceClient userClient;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserServiceClient userClient, PasswordEncoder passwordEncoder) {
        this.userClient = userClient;
        this.passwordEncoder = passwordEncoder;
    }

    public void registerNewUser(RegisterRequest req) {
        String passwordHash = passwordEncoder.encode(req.password());
        UserRole role = req.role() != null ? req.role() : UserRole.PATIENT;
        UserServiceClient.CreateUserRequest createReq = new UserServiceClient.CreateUserRequest(
                req.email(), req.fullName(), passwordHash, role
        );
        try {
            userClient.createUser(createReq);
        } catch (HttpClientErrorException.BadRequest ex) {
            // Map "email já cadastrado" or similar to a domain error code used by controller
            throw new IllegalArgumentException("email_exists");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            UserServiceClient.CredentialsResponse creds = userClient.getCredentialsByEmail(username);
            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + creds.role().name()));
            boolean disabled = creds.active() != null && !creds.active();
            return org.springframework.security.core.userdetails.User.withUsername(creds.email())
                    .password(creds.passwordHash() != null ? creds.passwordHash() : "")
                    .authorities(authorities)
                    .accountExpired(false)
                    .accountLocked(false)
                    .credentialsExpired(false)
                    .disabled(disabled)
                    .build();
        } catch (HttpClientErrorException.NotFound ex) {
            throw new UsernameNotFoundException("User not found");
        }
    }

    // Helpers for refresh-token flow
    public UserServiceClient.UserResponse getUserByEmail(String email) {
        return userClient.getByEmail(email);
    }

    public UserServiceClient.UserResponse getUserById(UUID id) {
        return userClient.getById(id);
    }

    public UserDetails buildUserDetailsFrom(UserServiceClient.UserResponse u) {
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + u.role().name()));
        boolean disabled = u.active() != null && !u.active();
        return org.springframework.security.core.userdetails.User.withUsername(u.email())
                .password("")
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(disabled)
                .build();
    }

    public boolean noAdminsExist() {
        return !userClient.anyAdmin();
    }
}
