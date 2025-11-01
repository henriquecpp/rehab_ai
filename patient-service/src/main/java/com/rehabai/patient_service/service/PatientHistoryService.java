package com.rehabai.patient_service.service;

import com.rehabai.patient_service.dto.PatientDtos;
import com.rehabai.patient_service.model.*;
import com.rehabai.patient_service.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PatientHistoryService {

    private final ClinicalNoteRepository noteRepo;
    private final ConditionRepository conditionRepo;
    private final AllergyRepository allergyRepo;
    private final MedicationRepository medicationRepo;
    private final VitalRepository vitalRepo;
    private final UserClient userClient;

    public PatientHistoryService(ClinicalNoteRepository noteRepo,
                                 ConditionRepository conditionRepo,
                                 AllergyRepository allergyRepo,
                                 MedicationRepository medicationRepo,
                                 VitalRepository vitalRepo,
                                 UserClient userClient) {
        this.noteRepo = noteRepo;
        this.conditionRepo = conditionRepo;
        this.allergyRepo = allergyRepo;
        this.medicationRepo = medicationRepo;
        this.vitalRepo = vitalRepo;
        this.userClient = userClient;
    }

    // Notes
    @Transactional
    public PatientDtos.NoteResponse addNote(UUID userId, PatientDtos.NoteCreateRequest req) {
        userClient.requireActivePatient(userId);
        ClinicalNote n = new ClinicalNote();
        n.setUserId(userId);
        n.setNote(req.note());
        n.setAuthorId(req.authorId());
        n.setTimestamp(req.timestamp() != null ? req.timestamp() : OffsetDateTime.now());
        n = noteRepo.save(n);
        return new PatientDtos.NoteResponse(n.getId(), n.getUserId(), n.getNote(), n.getAuthorId(), n.getTimestamp());
    }

    @Transactional(readOnly = true)
    public List<PatientDtos.NoteResponse> listNotes(UUID userId) {
        return noteRepo.findByUserIdOrderByTimestampDesc(userId)
                .stream().map(n -> new PatientDtos.NoteResponse(n.getId(), n.getUserId(), n.getNote(), n.getAuthorId(), n.getTimestamp()))
                .toList();
    }

    // Conditions
    @Transactional
    public PatientDtos.ConditionResponse addCondition(UUID userId, PatientDtos.ConditionCreateRequest req) {
        userClient.requireActivePatient(userId);
        Condition c = new Condition();
        c.setUserId(userId);
        c.setCode(req.code());
        c.setDescription(req.description());
        c.setOnsetDate(req.onsetDate());
        c.setResolvedDate(req.resolvedDate());
        c = conditionRepo.save(c);
        return new PatientDtos.ConditionResponse(c.getId(), c.getUserId(), c.getCode(), c.getDescription(), c.getOnsetDate(), c.getResolvedDate(), c.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public List<PatientDtos.ConditionResponse> listConditions(UUID userId) {
        return conditionRepo.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(c -> new PatientDtos.ConditionResponse(c.getId(), c.getUserId(), c.getCode(), c.getDescription(), c.getOnsetDate(), c.getResolvedDate(), c.getCreatedAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public PatientDtos.ConditionResponse getCondition(UUID userId, UUID conditionId) {
        Condition c = conditionRepo.findById(conditionId).orElseThrow(() -> new IllegalArgumentException("condition_not_found"));
        if (!c.getUserId().equals(userId)) throw new IllegalArgumentException("condition_not_found");
        return new PatientDtos.ConditionResponse(c.getId(), c.getUserId(), c.getCode(), c.getDescription(), c.getOnsetDate(), c.getResolvedDate(), c.getCreatedAt());
    }

    @Transactional
    public void deleteCondition(UUID userId, UUID conditionId) {
        Condition c = conditionRepo.findById(conditionId).orElseThrow(() -> new IllegalArgumentException("condition_not_found"));
        if (!c.getUserId().equals(userId)) throw new IllegalArgumentException("condition_not_found");
        conditionRepo.deleteById(conditionId);
    }

    // Allergies
    @Transactional
    public PatientDtos.AllergyResponse addAllergy(UUID userId, PatientDtos.AllergyCreateRequest req) {
        userClient.requireActivePatient(userId);
        Allergy a = new Allergy();
        a.setUserId(userId);
        a.setSubstance(req.substance());
        a.setReaction(req.reaction());
        a.setSeverity(req.severity());
        a.setRecordedAt(req.recordedAt() != null ? req.recordedAt() : OffsetDateTime.now());
        a = allergyRepo.save(a);
        return new PatientDtos.AllergyResponse(a.getId(), a.getUserId(), a.getSubstance(), a.getReaction(), a.getSeverity(), a.getRecordedAt());
    }

    @Transactional(readOnly = true)
    public List<PatientDtos.AllergyResponse> listAllergies(UUID userId) {
        return allergyRepo.findByUserIdOrderByRecordedAtDesc(userId)
                .stream().map(a -> new PatientDtos.AllergyResponse(a.getId(), a.getUserId(), a.getSubstance(), a.getReaction(), a.getSeverity(), a.getRecordedAt()))
                .toList();
    }

    // Medications
    @Transactional
    public PatientDtos.MedicationResponse addMedication(UUID userId, PatientDtos.MedicationCreateRequest req) {
        userClient.requireActivePatient(userId);
        Medication m = new Medication();
        m.setUserId(userId);
        m.setDrugName(req.drugName());
        m.setDose(req.dose());
        m.setRoute(req.route());
        m.setFrequency(req.frequency());
        m.setStartDate(req.startDate());
        m.setEndDate(req.endDate());
        m = medicationRepo.save(m);
        return new PatientDtos.MedicationResponse(m.getId(), m.getUserId(), m.getDrugName(), m.getDose(), m.getRoute(), m.getFrequency(), m.getStartDate(), m.getEndDate());
    }

    @Transactional(readOnly = true)
    public List<PatientDtos.MedicationResponse> listMedications(UUID userId) {
        return medicationRepo.findByUserId(userId)
                .stream().map(m -> new PatientDtos.MedicationResponse(m.getId(), m.getUserId(), m.getDrugName(), m.getDose(), m.getRoute(), m.getFrequency(), m.getStartDate(), m.getEndDate()))
                .toList();
    }

    // Vitals
    @Transactional
    public PatientDtos.VitalResponse addVital(UUID userId, PatientDtos.VitalCreateRequest req) {
        userClient.requireActivePatient(userId);
        Vital v = new Vital();
        v.setUserId(userId);
        v.setType(req.type());
        v.setValueJson(req.valueJson());
        v.setRecordedAt(req.recordedAt() != null ? req.recordedAt() : OffsetDateTime.now());
        v = vitalRepo.save(v);
        return new PatientDtos.VitalResponse(v.getId(), v.getUserId(), v.getType(), v.getValueJson(), v.getRecordedAt());
    }

    @Transactional(readOnly = true)
    public List<PatientDtos.VitalResponse> listVitals(UUID userId) {
        return vitalRepo.findByUserIdOrderByRecordedAtDesc(userId)
                .stream().map(v -> new PatientDtos.VitalResponse(v.getId(), v.getUserId(), v.getType(), v.getValueJson(), v.getRecordedAt()))
                .toList();
    }
}
