package com.rehabai.auth_service.controller;

import com.rehabai.auth_service.dto.LoginRequest;
import com.rehabai.auth_service.dto.RefreshRequest;
import com.rehabai.auth_service.dto.RegisterRequest;
import com.rehabai.auth_service.model.UserRole;
import com.rehabai.auth_service.security.JwtUtil;
import com.rehabai.auth_service.service.RefreshTokenService;
import com.rehabai.auth_service.service.UserService;
import com.rehabai.auth_service.service.UserServiceClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean UserService userService;
    @MockBean PasswordEncoder passwordEncoder;
    @MockBean JwtUtil jwtUtil;
    @MockBean RefreshTokenService refreshTokenService;

    @Test
    void register_whenEmailExists_returns400() throws Exception {
        Mockito.doThrow(new IllegalArgumentException("email_exists"))
                .when(userService).registerNewUser(any(RegisterRequest.class));

        mvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"a@a.com\",\"password\":\"secret123\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("email_exists"));
    }

    @Test
    void login_whenInvalidPassword_returns401() throws Exception {
        UserDetails ud = User.withUsername("a@a.com").password("enc").authorities("ROLE_PATIENT").build();
        Mockito.when(userService.loadUserByUsername("a@a.com")).thenReturn(ud);
        Mockito.when(passwordEncoder.matches(eq("wrong"), eq("enc"))).thenReturn(false);

        mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"a@a.com\",\"password\":\"wrong\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(content().string("invalid_credentials"));
    }

    @Test
    void refresh_whenInvalidToken_returns401() throws Exception {
        Mockito.doThrow(new IllegalArgumentException("invalid_refresh_token"))
                .when(refreshTokenService).rotate(any(UUID.class));

        mvc.perform(post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"00000000-0000-0000-0000-000000000000\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(content().string("invalid_refresh_token"));
    }
}

