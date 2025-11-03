package com.rehabai.user_service.service;

import com.rehabai.user_service.dto.UserDtos;
import com.rehabai.user_service.model.User;
import com.rehabai.user_service.model.UserRole;
import com.rehabai.user_service.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public UserDtos.Response create(UserDtos.CreateRequest req) {
        if (repository.existsByEmail(req.email())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email já cadastrado: " + req.email());
        }

        User user = new User();
        user.setEmail(req.email());
        user.setFullName(req.fullName());
        user.setPasswordHash(req.passwordHash());
        user.setRole(req.role() != null ? req.role() : UserRole.PATIENT);

        User saved = repository.save(user);
        log.info("User created: id={}, email={}, role={}", saved.getId(), saved.getEmail(), saved.getRole());

        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<UserDtos.Response> list() {
        return repository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<UserDtos.Response> listByRole(UserRole role) {
        return repository.findByRole(role).stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<UserDtos.Response> listActive() {
        return repository.findByActiveTrue().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public UserDtos.Response get(UUID id) {
        return repository.findById(id)
            .map(this::toDto)
            .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public UserDtos.Response getByEmail(String email) {
        return repository.findByEmail(email)
            .map(this::toDto)
            .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + email));
    }

    @Transactional(readOnly = true)
    public UserDtos.CredentialsResponse getCredentialsByEmail(String email) {
        return repository.findByEmail(email)
            .map(u -> new UserDtos.CredentialsResponse(u.getId(), u.getEmail(), u.getPasswordHash(), u.getRole(), u.getActive()))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado: " + email));
    }

    @Transactional
    public UserDtos.Response update(UUID id, UserDtos.UpdateRequest req) {
        User user = repository.findByIdWithLock(id)
            .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + id));

        boolean hasChanges = false;

        if (req.fullName() != null && !req.fullName().equals(user.getFullName())) {
            user.setFullName(req.fullName());
            hasChanges = true;
        }
        if (req.role() != null && !req.role().equals(user.getRole())) {
            user.setRole(req.role());
            hasChanges = true;
            log.warn("User role changed: id={}, oldRole={}, newRole={}",
                    id, user.getRole(), req.role());
        }
        if (req.active() != null && !req.active().equals(user.getActive())) {
            user.setActive(req.active());
            hasChanges = true;
        }

        if (!hasChanges) {
            log.debug("No changes detected for user update: id={}", id);
            return toDto(user);
        }

        User updated = repository.save(user);
        log.info("User updated: id={}, email={}", updated.getId(), updated.getEmail());

        return toDto(updated);
    }

    @Transactional
    public void delete(UUID id, UUID authenticatedUserId) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Usuário não encontrado: " + id);
        }

        // Prevent self-deletion
        if (id.equals(authenticatedUserId)) {
            throw new IllegalArgumentException("Não é permitido deletar seu próprio usuário. Use deactivate se necessário.");
        }

        repository.deleteById(id);
        log.warn("User deleted: id={} by admin={}", id, authenticatedUserId);

        // TODO: Publish UserDeletedEvent to RabbitMQ for other services to react
    }


    @Transactional
    public UserDtos.Response activate(UUID id) {
        User user = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + id));
        user.setActive(true);
        return toDto(repository.save(user));
    }

    @Transactional
    public UserDtos.Response deactivate(UUID id) {
        User user = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + id));
        user.setActive(false);
        return toDto(repository.save(user));
    }

    @Transactional
    public UserDtos.Response changeRole(UUID id, UserRole role) {
        if (role == null) throw new IllegalArgumentException("role_required");
        User user = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + id));
        user.setRole(role);
        return toDto(repository.save(user));
    }

    @Transactional
    public UserDtos.Response changePassword(UUID id, String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank()) throw new IllegalArgumentException("password_hash_required");
        User user = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + id));
        user.setPasswordHash(passwordHash);
        return toDto(repository.save(user));
    }

    @Transactional(readOnly = true)
    public long count() {
        return repository.count();
    }

    @Transactional(readOnly = true)
    public boolean anyAdminExists() {
        return repository.existsByRole(UserRole.ADMIN);
    }

    private UserDtos.Response toDto(User user) {
        return new UserDtos.Response(
            user.getId(),
            user.getEmail(),
            user.getFullName(),
            user.getRole(),
            user.getActive()
        );
    }
}
