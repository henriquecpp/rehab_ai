package com.rehabai.user_service.service;

import com.rehabai.user_service.dto.ConsentDtos;
import com.rehabai.user_service.model.Consent;
import com.rehabai.user_service.repository.ConsentRepository;
import com.rehabai.user_service.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ConsentService {

    private final ConsentRepository consentRepository;
    private final UserRepository userRepository;

    public ConsentService(ConsentRepository consentRepository, UserRepository userRepository) {
        this.consentRepository = consentRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ConsentDtos.Response create(UUID userId, ConsentDtos.CreateRequest req) {
        // Garantir que o usuário existe
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("user_not_found: " + userId);
        }
        Consent c = new Consent();
        c.setUserId(userId);
        c.setType(req.type());
        c.setGranted(req.granted());
        c.setTimestamp(req.timestamp() != null ? req.timestamp() : OffsetDateTime.now());
        c = consentRepository.save(c);
        return toDto(c);
    }

    @Transactional
    public ConsentDtos.Response revoke(UUID userId, ConsentDtos.RevokeRequest req) {
        if (req.type() == null || req.type().isBlank()) {
            throw new IllegalArgumentException("type_required");
        }
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("user_not_found: " + userId);
        }
        Consent c = new Consent();
        c.setUserId(userId);
        c.setType(req.type());
        c.setGranted(false);
        c.setTimestamp(OffsetDateTime.now());
        c = consentRepository.save(c);
        return toDto(c);
    }

    @Transactional(readOnly = true)
    public List<ConsentDtos.Response> listByUser(UUID userId, String type) {
        if (type == null || type.isBlank()) {
            return consentRepository.findByUserIdOrderByTimestampDesc(userId)
                    .stream().map(this::toDto).toList();
        }
        return consentRepository.findByUserIdAndTypeOrderByTimestampDesc(userId, type)
                .stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public ConsentDtos.Response latestByType(UUID userId, String type) {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("type_required");
        }
        return consentRepository.findTopByUserIdAndTypeOrderByTimestampDesc(userId, type)
                .map(this::toDto)
                .orElseThrow(() -> new IllegalArgumentException("consent_not_found"));
    }

    private ConsentDtos.Response toDto(Consent c) {
        return new ConsentDtos.Response(c.getId(), c.getUserId(), c.getType(), c.getGranted(), c.getTimestamp());
    }
}
