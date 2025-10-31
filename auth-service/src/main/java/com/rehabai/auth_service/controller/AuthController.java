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
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    public AuthController(UserService userService, PasswordEncoder passwordEncoder, JwtUtil jwtUtil,
                          RefreshTokenService refreshTokenService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        try {
            userService.registerNewUser(req);
            UserDetails ud = userService.loadUserByUsername(req.email());
            UserServiceClient.UserResponse u = userService.getUserByEmail(req.email());
            String token = jwtUtil.generateToken(ud, Map.of("user_id", u.id().toString()));
            long expiresIn = jwtUtil.getExpirationMs();

            var rt = refreshTokenService.issueForUser(u.id());
            long refreshExpiresIn = refreshTokenService.getRefreshExpirationMs();

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new AuthResponse(token, "Bearer", expiresIn, rt.getTokenId().toString(), refreshExpiresIn));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
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

            UserServiceClient.UserResponse u = userService.getUserByEmail(req.email());
            String token = jwtUtil.generateToken(ud, Map.of("user_id", u.id().toString()));
            long expiresIn = jwtUtil.getExpirationMs();

            var rt = refreshTokenService.issueForUser(u.id());
            long refreshExpiresIn = refreshTokenService.getRefreshExpirationMs();

            return ResponseEntity.ok(new AuthResponse(token, "Bearer", expiresIn, rt.getTokenId().toString(), refreshExpiresIn));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("invalid_credentials");
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
