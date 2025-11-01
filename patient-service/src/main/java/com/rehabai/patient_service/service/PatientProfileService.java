package com.rehabai.patient_service.service;

import com.rehabai.patient_service.dto.PatientDtos;
import com.rehabai.patient_service.model.PatientProfile;
import com.rehabai.patient_service.repository.PatientProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PatientProfileService {

    private final PatientProfileRepository repo;
    private final UserClient userClient;

    public PatientProfileService(PatientProfileRepository repo, UserClient userClient) {
        this.repo = repo;
        this.userClient = userClient;
    }

    @Transactional(readOnly = true)
    public PatientDtos.ProfileResponse get(UUID userId) {
        return repo.findByUserId(userId).map(this::toDto)
                .orElseThrow(() -> new IllegalArgumentException("profile_not_found"));
    }

    @Transactional
    public PatientDtos.ProfileResponse upsert(UUID userId, PatientDtos.ProfileRequest req) {
        userClient.requireActivePatient(userId);
        PatientProfile p = repo.findByUserId(userId).orElseGet(() -> {
            PatientProfile np = new PatientProfile();
            np.setUserId(userId);
            return np;
        });
        p.setPreferredLanguage(req.preferredLanguage());
        p.setBiologicalSex(req.biologicalSex());
        p.setDateOfBirth(req.dateOfBirth());
        p.setNotes(req.notes());
        p = repo.save(p);
        return toDto(p);
    }

    private PatientDtos.ProfileResponse toDto(PatientProfile p) {
        return new PatientDtos.ProfileResponse(p.getId(), p.getUserId(), p.getPreferredLanguage(), p.getBiologicalSex(), p.getDateOfBirth(), p.getNotes(), p.getCreatedAt(), p.getUpdatedAt());
    }
}

