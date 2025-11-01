package com.rehabai.auth_service.controller;

import com.rehabai.auth_service.security.JwtUtil;
import com.rehabai.auth_service.service.RefreshTokenService;
import com.rehabai.auth_service.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerErrorTest {

    @Autowired MockMvc mvc;

    @MockitoBean UserService userService;
    @MockitoBean PasswordEncoder passwordEncoder;
    @MockitoBean JwtUtil jwtUtil;
    @MockitoBean RefreshTokenService refreshTokenService;

    @Test
    void login_invalidCredentials_returns401() throws Exception {
        UserDetails ud = User.withUsername("john@example.com").password("hashed").roles("PATIENT").build();
        Mockito.when(userService.loadUserByUsername("john@example.com")).thenReturn(ud);
        Mockito.when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        String body = "{\"email\":\"john@example.com\",\"password\":\"wrong\"}";
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_invalidToken_returns401() throws Exception {
        String body = "{\"refreshToken\":\"not-a-uuid\"}";
        mvc.perform(post("/auth/refresh").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
    }
}
