package com.rehabai.auth_service.controller;

import com.rehabai.auth_service.dto.ConsentRequest;
import com.rehabai.auth_service.service.UserService;
import com.rehabai.auth_service.service.UserServiceClient;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

@RestController
@RequestMapping("/auth/consents")
public class ConsentController {

    private final UserService userService;
    private final UserServiceClient userClient;

    public ConsentController(UserService userService, UserServiceClient userClient) {
        this.userService = userService;
        this.userClient = userClient;
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody ConsentRequest req, Authentication auth) {
        try {
            String email = auth.getName();
            var u = userService.getUserByEmail(email);
            var created = userClient.createConsent(u.id(), new UserServiceClient.ConsentCreateRequest(
                    req.type(), req.granted(), req.timestamp()
            ));
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (HttpClientErrorException.NotFound nf) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("user_not_found");
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("consent_error");
        }
    }

    @GetMapping
    public ResponseEntity<List<UserServiceClient.ConsentResponse>> list(@RequestParam(required = false) String type,
                                                                        Authentication auth) {
        String email = auth.getName();
        var u = userService.getUserByEmail(email);
        var consents = userClient.listConsents(u.id(), type);
        return ResponseEntity.ok(consents);
    }
}

