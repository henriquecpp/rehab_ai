package com.rehabai.prescription_service.consumer;

import com.rehabai.prescription_service.events.PatientDataResponseEvent;
import com.rehabai.prescription_service.service.PatientDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PatientDataResponseListener {

    private static final Logger log = LoggerFactory.getLogger(PatientDataResponseListener.class);

    private final PatientDataService patientDataService;

    public PatientDataResponseListener(PatientDataService patientDataService) {
        this.patientDataService = patientDataService;
    }

    @RabbitListener(queues = "${amqp.patientDataResponseQueue:patient.data.response}")
    public void handlePatientDataResponse(PatientDataResponseEvent event) {
        log.info("[Prescription] Received patient data response: requestId={}, userId={}, conditions={}, allergies={}, medications={}",
                event.requestId(), event.userId(),
                event.medicalData().conditions().size(),
                event.medicalData().allergies().size(),
                event.medicalData().medications().size());

        patientDataService.completeRequest(event.requestId(), event);
    }
}

