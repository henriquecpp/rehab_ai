package com.rehabai.patient_service.service;

import com.rehabai.patient_service.dto.PatientDtos;
import com.rehabai.patient_service.model.Patient;
import com.rehabai.patient_service.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PatientService {
    private final PatientRepository repository;

    public PatientService(PatientRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public PatientDtos.Response create(PatientDtos.CreateRequest req) {
        Patient p = new Patient();
        p.setFullName(req.fullName());
        p.setEmail(req.email());
        p.setDateOfBirth(req.dateOfBirth());
        p.setGender(req.gender());
        Patient saved = repository.save(p);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<PatientDtos.Response> list() {
        return repository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public PatientDtos.Response get(UUID id) {
        return repository.findById(id).map(this::toDto).orElseThrow(() -> new RuntimeException("Patient not found"));
    }

    @Transactional
    public PatientDtos.Response update(UUID id, PatientDtos.UpdateRequest req) {
        Patient p = repository.findById(id).orElseThrow(() -> new RuntimeException("Patient not found"));
        p.setFullName(req.fullName());
        p.setEmail(req.email());
        p.setDateOfBirth(req.dateOfBirth());
        p.setGender(req.gender());
        return toDto(repository.save(p));
    }

    @Transactional
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Patient not found");
        }
        repository.deleteById(id);
    }

    private PatientDtos.Response toDto(Patient p) {
        return new PatientDtos.Response(p.getId(), p.getFullName(), p.getEmail(), p.getDateOfBirth(), p.getGender());
    }
}

