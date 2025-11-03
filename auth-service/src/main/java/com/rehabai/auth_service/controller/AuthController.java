package com.rehabai.auth_service.controller;

import com.rehabai.auth_service.dto.AuthResponse;
import com.rehabai.auth_service.dto.LoginRequest;
import com.rehabai.auth_service.dto.RefreshRequest;
import com.rehabai.auth_service.dto.RegisterRequest;
import com.rehabai.auth_service.dto.LogoutRequest;
import com.rehabai.auth_service.security.JwtUtil;
import com.rehabai.auth_service.service.RefreshTokenService;
import com.rehabai.auth_service.service.UserService;
import com.rehabai.auth_service.service.UserServiceClient;
import com.rehabai.auth_service.model.UserRole;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req,
                                      @RequestHeader(value = "Authorization", required = false) String authHeader,
                                      Authentication authentication) {
        try {
            // Enforce role creation rules
            if (req.role() == UserRole.ADMIN) {
                boolean bootstrap = userService.noAdminsExist();
                boolean callerIsAdmin = authentication != null && authentication.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                if (!bootstrap && !callerIsAdmin) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("admin_only");
                }
            }

            userService.registerNewUser(req);
            UserDetails ud = userService.loadUserByUsername(req.email());

            UserServiceClient.CredentialsResponse creds = userService.getCredentialsByEmail(req.email());
            String token = jwtUtil.generateToken(ud, Map.of("user_id", creds.id().toString()));
            long expiresIn = jwtUtil.getExpirationMs();

            var rt = refreshTokenService.issueForUser(creds.id());
            long refreshExpiresIn = refreshTokenService.getRefreshExpirationMs();

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new AuthResponse(token, "Bearer", expiresIn, rt.getTokenId().toString(), refreshExpiresIn));
        } catch (org.springframework.web.client.HttpClientErrorException.BadRequest ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "email_already_exists", "message", "Email já cadastrado"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (Exception ex) {
            org.slf4j.LoggerFactory.getLogger(AuthController.class)
                .error("Unexpected error during registration for {}", req.email(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "internal_error", "message", "An unexpected error occurred"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        try {
            UserDetails ud = userService.loadUserByUsername(req.email());
            if (ud == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("invalid_credentials");
            }
            if (!passwordEncoder.matches(req.password(), ud.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("invalid_credentials");
            }


            UserServiceClient.CredentialsResponse creds = userService.getCredentialsByEmail(req.email());
            String token = jwtUtil.generateToken(ud, Map.of("user_id", creds.id().toString()));
            long expiresIn = jwtUtil.getExpirationMs();

            var rt = refreshTokenService.issueForUser(creds.id());
            long refreshExpiresIn = refreshTokenService.getRefreshExpirationMs();

            return ResponseEntity.ok(new AuthResponse(token, "Bearer", expiresIn, rt.getTokenId().toString(), refreshExpiresIn));
        } catch (org.springframework.security.core.userdetails.UsernameNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("invalid_credentials");
        } catch (org.springframework.web.client.HttpClientErrorException ex) {
            org.slf4j.LoggerFactory.getLogger(AuthController.class)
                .error("User service error during login for {}: {}", req.email(), ex.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", "service_unavailable", "message", "Authentication service temporarily unavailable"));
        } catch (Exception ex) {
            org.slf4j.LoggerFactory.getLogger(AuthController.class)
                .error("Unexpected error during login for {}", req.email(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "internal_error", "message", "An unexpected error occurred"));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshRequest req) {
        try {
            UUID tokenId = UUID.fromString(req.refreshToken());
            var newRt = refreshTokenService.rotate(tokenId);
            UserServiceClient.UserResponse u = userService.getUserById(newRt.getUserId());
            UserDetails ud = userService.buildUserDetailsFrom(u);
            String token = jwtUtil.generateToken(ud, Map.of("user_id", u.id().toString()));
            long expiresIn = jwtUtil.getExpirationMs();
            long refreshExpiresIn = refreshTokenService.getRefreshExpirationMs();

            return ResponseEntity.ok(new AuthResponse(token, "Bearer", expiresIn, newRt.getTokenId().toString(), refreshExpiresIn));
        } catch (IllegalArgumentException ex) {
            String msg = ex.getMessage();
            if ("expired_refresh_token".equals(msg)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("expired_refresh_token");
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("invalid_refresh_token");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest req) {
        try {
            UUID tokenId = UUID.fromString(req.refreshToken());
            refreshTokenService.revokeToken(tokenId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/logout_all")
    public ResponseEntity<Void> logoutAll(@RequestHeader(name = "Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String token = authHeader.substring(7);
        try {
            String userIdStr = jwtUtil.getClaimAsString(token, "user_id");
            if (userIdStr == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            UUID userId = UUID.fromString(userIdStr);
            refreshTokenService.revokeAllForUser(userId);
            return ResponseEntity.noContent().build();
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
