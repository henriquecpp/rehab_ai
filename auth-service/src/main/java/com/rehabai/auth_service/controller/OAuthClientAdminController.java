package com.rehabai.auth_service.controller;

import com.rehabai.auth_service.model.OAuthClient;
import com.rehabai.auth_service.repository.OAuthClientRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/admin/oauth/clients")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class OAuthClientAdminController {

    private final OAuthClientRepository repo;

    @GetMapping
    public ResponseEntity<List<OAuthClient>> list() {
        return ResponseEntity.ok(repo.findAll());
    }

    @GetMapping("/{clientId}")
    public ResponseEntity<OAuthClient> get(@PathVariable String clientId) {
        return repo.findById(clientId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<OAuthClient> create(@Valid @RequestBody OAuthClient client) {
        if (repo.existsById(client.getClientId())) {
            return ResponseEntity.status(409).build();
        }
        OAuthClient saved = repo.save(client);
        return ResponseEntity.created(URI.create("/admin/oauth/clients/" + saved.getClientId())).body(saved);
    }

    @PutMapping("/{clientId}")
    public ResponseEntity<OAuthClient> update(@PathVariable String clientId, @Valid @RequestBody OAuthClient update) {
        return repo.findById(clientId).map(existing -> {
            existing.setClientSecret(update.getClientSecret());
            existing.setScopes(update.getScopes());
            existing.setGrantTypes(update.getGrantTypes());
            existing.setRedirectUris(update.getRedirectUris());
            return ResponseEntity.ok(repo.save(existing));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{clientId}")
    public ResponseEntity<Void> delete(@PathVariable String clientId) {
        if (!repo.existsById(clientId)) return ResponseEntity.notFound().build();
        repo.deleteById(clientId);
        return ResponseEntity.noContent().build();
    }
}
