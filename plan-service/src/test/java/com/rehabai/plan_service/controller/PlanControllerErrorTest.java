package com.rehabai.plan_service.controller;

import com.rehabai.plan_service.service.PlanService;
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

@WebMvcTest(PlanController.class)
@AutoConfigureMockMvc(addFilters = false)
class PlanControllerErrorTest {

    @Autowired MockMvc mvc;

    @MockitoBean PlanService planService;

    @Test
    void getPlan_whenNotFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(planService.getPlan(id)).thenThrow(new IllegalArgumentException("Plan not found: " + id));

        mvc.perform(get("/plans/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void createPlan_whenInvalidBody_returns400() throws Exception {
        // Missing required fields triggers @Valid failure
        mvc.perform(post("/plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
