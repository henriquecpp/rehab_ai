package com.rehabai.patient_service.consumer;

import com.rehabai.patient_service.config.RabbitConfig;
import com.rehabai.patient_service.events.PatientDataRequestEvent;
import com.rehabai.patient_service.events.PatientDataResponseEvent;
import com.rehabai.patient_service.model.Allergy;
import com.rehabai.patient_service.model.Condition;
import com.rehabai.patient_service.model.Medication;
import com.rehabai.patient_service.repository.AllergyRepository;
import com.rehabai.patient_service.repository.ConditionRepository;
import com.rehabai.patient_service.repository.MedicationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class PatientDataRequestListener {

    private static final Logger log = LoggerFactory.getLogger(PatientDataRequestListener.class);

    private final AllergyRepository allergyRepository;
    private final ConditionRepository conditionRepository;
    private final MedicationRepository medicationRepository;
    private final RabbitTemplate rabbitTemplate;

    public PatientDataRequestListener(
            AllergyRepository allergyRepository,
            ConditionRepository conditionRepository,
            MedicationRepository medicationRepository,
            RabbitTemplate rabbitTemplate) {
        this.allergyRepository = allergyRepository;
        this.conditionRepository = conditionRepository;
        this.medicationRepository = medicationRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = RabbitConfig.PATIENT_DATA_REQUEST_QUEUE)
    public void handlePatientDataRequest(PatientDataRequestEvent event) {
        log.info("[Patient] Received patient data request: requestId={}, userId={}, traceId={}",
                event.requestId(), event.userId(), event.traceId());

        try {
            UUID userId = event.userId();

            List<Condition> conditions = conditionRepository.findByUserIdOrderByOnsetDateDesc(userId);
            List<Allergy> allergies = allergyRepository.findByUserIdOrderByRecordedAtDesc(userId);
            List<Medication> medications = medicationRepository.findByUserIdOrderByStartDateDesc(userId);

            List<PatientDataResponseEvent.ConditionInfo> conditionInfos = conditions.stream()
                    .map(c -> new PatientDataResponseEvent.ConditionInfo(
                            c.getCode(),
                            c.getDescription(),
                            c.getOnsetDate(),
                            c.getResolvedDate()
                    ))
                    .toList();

            List<PatientDataResponseEvent.AllergyInfo> allergyInfos = allergies.stream()
                    .map(a -> new PatientDataResponseEvent.AllergyInfo(
                            a.getSubstance(),
                            a.getReaction(),
                            a.getSeverity()
                    ))
                    .toList();

            List<PatientDataResponseEvent.MedicationInfo> medicationInfos = medications.stream()
                    .filter(m -> m.getEndDate() == null || m.getEndDate().isAfter(java.time.LocalDate.now()))
                    .map(m -> new PatientDataResponseEvent.MedicationInfo(
                            m.getDrugName(),
                            m.getDose(),
                            m.getRoute(),
                            m.getFrequency(),
                            m.getStartDate(),
                            m.getEndDate()
                    ))
                    .toList();

            PatientDataResponseEvent.PatientMedicalData medicalData =
                    new PatientDataResponseEvent.PatientMedicalData(
                            conditionInfos,
                            allergyInfos,
                            medicationInfos
                    );

            PatientDataResponseEvent response = new PatientDataResponseEvent(
                    event.requestId(),
                    event.userId(),
                    event.traceId(),
                    medicalData
            );

            rabbitTemplate.convertAndSend(
                    RabbitConfig.EXCHANGE,
                    "patient.data.response",
                    response
            );

            log.info("[Patient] Sent patient data response: requestId={}, conditions={}, allergies={}, medications={}",
                    event.requestId(), conditionInfos.size(), allergyInfos.size(), medicationInfos.size());

        } catch (Exception e) {
            log.error("[Patient] Error processing patient data request: requestId={}, error={}",
                    event.requestId(), e.getMessage(), e);
        }
    }
}

