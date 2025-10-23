package com.rehabai.auth_service.controller;

import com.rehabai.auth_service.dto.AuthResponse;
import com.rehabai.auth_service.dto.LoginRequest;
import com.rehabai.auth_service.dto.RegisterRequest;
import com.rehabai.auth_service.model.User;
import com.rehabai.auth_service.security.JwtUtil;
import com.rehabai.auth_service.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserService userService, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        try {
            User created = userService.registerNewUser(req);
            UserDetails ud = userService.loadUserByUsername(created.getUsername());
            String token = jwtUtil.generateToken(ud);
            long expiresIn = jwtUtil.getExpirationMs();
            return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(token, expiresIn));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        try {
            UserDetails ud = userService.loadUserByUsername(req.username());
            if (ud == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("invalid_credentials");
            }
            // Password is stored encoded in ud.getPassword()
            if (!passwordEncoder.matches(req.password(), ud.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("invalid_credentials");
            }

            String token = jwtUtil.generateToken(ud);
            long expiresIn = jwtUtil.getExpirationMs();
            return ResponseEntity.ok(new AuthResponse(token, expiresIn));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("invalid_credentials");
        }
    }
}
