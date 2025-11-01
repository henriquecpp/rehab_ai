package com.rehabai.patient_service.service;

import com.rehabai.patient_service.dto.PatientDtos;
import com.rehabai.patient_service.model.Condition;
import com.rehabai.patient_service.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientHistoryServiceTest {

    @Mock ClinicalNoteRepository noteRepo;
    @Mock ConditionRepository conditionRepo;
    @Mock AllergyRepository allergyRepo;
    @Mock MedicationRepository medicationRepo;
    @Mock VitalRepository vitalRepo;
    @Mock UserClient userClient;

    PatientHistoryService service;

    @BeforeEach
    void setup() {
        service = new PatientHistoryService(noteRepo, conditionRepo, allergyRepo, medicationRepo, vitalRepo, userClient);
    }

    @Test
    void addCondition_persists_and_returns_dto() {
        UUID userId = UUID.randomUUID();
        PatientDtos.ConditionCreateRequest req = new PatientDtos.ConditionCreateRequest("A10", "Test", LocalDate.of(2024,1,1), null);

        // user is active
        doNothing().when(userClient).requireActivePatient(userId);

        // when saving, return entity with id
        when(conditionRepo.save(any(Condition.class))).thenAnswer(inv -> {
            Condition c = inv.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        PatientDtos.ConditionResponse resp = service.addCondition(userId, req);

        // verify persistence
        ArgumentCaptor<Condition> cap = ArgumentCaptor.forClass(Condition.class);
        verify(conditionRepo).save(cap.capture());
        Condition saved = cap.getValue();
        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getCode()).isEqualTo("A10");
        assertThat(saved.getDescription()).isEqualTo("Test");
        assertThat(saved.getOnsetDate()).isEqualTo(LocalDate.of(2024,1,1));

        // verify DTO
        assertThat(resp.id()).isNotNull();
        assertThat(resp.userId()).isEqualTo(userId);
        assertThat(resp.code()).isEqualTo("A10");
        assertThat(resp.description()).isEqualTo("Test");
        assertThat(resp.onsetDate()).isEqualTo(LocalDate.of(2024,1,1));
    }

    @Test
    void addCondition_throws_when_user_inactive() {
        UUID userId = UUID.randomUUID();
        PatientDtos.ConditionCreateRequest req = new PatientDtos.ConditionCreateRequest(null, null, null, null);
        doThrow(new IllegalArgumentException("user_inactive")).when(userClient).requireActivePatient(userId);
        assertThrows(IllegalArgumentException.class, () -> service.addCondition(userId, req));
        verifyNoInteractions(conditionRepo);
    }

    @Test
    void listConditions_returns_mapped_dtos_in_repo_order() {
        UUID userId = UUID.randomUUID();
        Condition c1 = new Condition();
        c1.setId(UUID.randomUUID());
        c1.setUserId(userId);
        c1.setCode("A10");
        c1.setDescription("Cond 1");
        c1.setOnsetDate(LocalDate.of(2023,1,1));
        c1.setCreatedAt(OffsetDateTime.now().minusDays(1));

        Condition c2 = new Condition();
        c2.setId(UUID.randomUUID());
        c2.setUserId(userId);
        c2.setCode("B20");
        c2.setDescription("Cond 2");
        c2.setOnsetDate(LocalDate.of(2024,2,2));
        c2.setCreatedAt(OffsetDateTime.now());

        when(conditionRepo.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of(c2, c1));

        List<PatientDtos.ConditionResponse> list = service.listConditions(userId);
        assertThat(list).hasSize(2);
        assertThat(list.get(0).id()).isEqualTo(c2.getId());
        assertThat(list.get(0).code()).isEqualTo("B20");
        assertThat(list.get(1).id()).isEqualTo(c1.getId());
        assertThat(list.get(1).code()).isEqualTo("A10");
    }
}
