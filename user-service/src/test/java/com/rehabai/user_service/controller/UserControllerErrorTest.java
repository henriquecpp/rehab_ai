package com.rehabai.user_service.controller;

import com.rehabai.user_service.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerErrorTest {

    @Autowired MockMvc mvc;

    @MockitoBean UserService userService;

    @Test
    void getUser_whenNotFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(userService.get(id)).thenThrow(new IllegalArgumentException("not found"));
        mvc.perform(get("/users/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void createUser_whenInvalid_returns400() throws Exception {
        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
