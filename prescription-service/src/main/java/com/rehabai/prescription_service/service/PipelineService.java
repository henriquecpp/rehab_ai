package com.rehabai.prescription_service.service;

import com.rehabai.prescription_service.config.RabbitConfig;
import com.rehabai.prescription_service.events.PatientDataRequestEvent;
import com.rehabai.prescription_service.events.PatientDataResponseEvent;
import com.rehabai.prescription_service.llm.LlmService;
import com.rehabai.prescription_service.model.*;
import com.rehabai.prescription_service.repository.*;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class PipelineService {

    private static final Logger log = LoggerFactory.getLogger(PipelineService.class);

    private final com.rehabai.prescription_service.ocr.OCRService ocrService;
    private final NormalizationService normalizationService;
    private final LlmService llmService;
    private final WorkflowRunRepository runRepo;
    private final ExtractionRepository extractionRepo;
    private final NormalizationRepository normalizationRepo;
    private final PrescriptionRepository prescriptionRepo;
    private final AiTraceRepository aiTraceRepo;
    private final PatientDataService patientDataService;
    private final RabbitTemplate rabbitTemplate;

    private final MeterRegistry meterRegistry;
    private final ObservationRegistry observationRegistry;
    private final Tracer tracer;

    public PipelineService(com.rehabai.prescription_service.ocr.OCRService ocrService,
                           NormalizationService normalizationService,
                           LlmService llmService,
                           WorkflowRunRepository runRepo,
                           ExtractionRepository extractionRepo,
                           NormalizationRepository normalizationRepo,
                           PrescriptionRepository prescriptionRepo,
                           AiTraceRepository aiTraceRepo,
                           PatientDataService patientDataService,
                           RabbitTemplate rabbitTemplate,
                           MeterRegistry meterRegistry,
                           ObservationRegistry observationRegistry,
                           Tracer tracer) {
        this.ocrService = ocrService;
        this.normalizationService = normalizationService;
        this.llmService = llmService;
        this.runRepo = runRepo;
        this.extractionRepo = extractionRepo;
        this.normalizationRepo = normalizationRepo;
        this.prescriptionRepo = prescriptionRepo;
        this.aiTraceRepo = aiTraceRepo;
        this.patientDataService = patientDataService;
        this.rabbitTemplate = rabbitTemplate;
        this.meterRegistry = meterRegistry;
        this.observationRegistry = observationRegistry;
        this.tracer = tracer;
    }

    @Transactional
    public void processFile(UUID fileId, UUID userId, byte[] content, String filename, String contentType) {
        WorkflowRun run = new WorkflowRun();
        run.setFileId(fileId);
        run.setUserId(userId);
        run.setCurrentStage(WorkflowStage.EXTRACTION);

        String traceId;
        try {
            traceId = tracer != null && tracer.currentSpan() != null ? tracer.currentSpan().context().traceId() : null;
        } catch (Exception ignore) {
            traceId = null;
        }

        if (traceId == null || traceId.isEmpty()) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }

        run.setTraceId(traceId);
        run = runRepo.save(run);

        Timer.Sample total = Timer.start(meterRegistry);
        Observation overallObs = Observation.start("pipeline.run", observationRegistry)
                .lowCardinalityKeyValue("service", "prescription-service");
        try (Observation.Scope overallScope = overallObs.openScope()) {
            run.setCurrentStage(WorkflowStage.EXTRACTION);
            runRepo.save(run);
            Observation extractObs = Observation.createNotStarted("pipeline.stage", observationRegistry)
                    .lowCardinalityKeyValue("stage", "extraction");
            Timer.Sample extractTimer = Timer.start(meterRegistry);

            Extraction ext;
            String extractedText;
            try (Observation.Scope s = extractObs.start().openScope()) {
                var ocr = ocrService.extract(content, filename, contentType);
                log.debug("OCR extracted text: {}", ocr.text());
                extractedText = ocr.text();
                ext = new Extraction();
                ext.setFileId(fileId);
                ext.setFindingsJson("{\"text\": \"" + escape(extractedText) + "\"}");
                ext.setContraindicationsJson("[]");
                ext.setModelUsed(ocr.engine());
                ext.setConfidenceScore(ocr.confidence());
                ext = extractionRepo.save(ext);
                meterRegistry.counter("pipeline.stage.success", "stage", "extraction").increment();
            } catch (Exception ex) {
                extractObs.error(ex);
                meterRegistry.counter("pipeline.stage.failure", "stage", "extraction").increment();
                throw ex;
            } finally {
                extractObs.stop();
                extractTimer.stop(Timer.builder("pipeline.stage.latency").tag("stage", "extraction").register(meterRegistry));
            }

            run.setCurrentStage(WorkflowStage.NORMALIZATION);
            runRepo.save(run);
            Observation normObs = Observation.createNotStarted("pipeline.stage", observationRegistry)
                    .lowCardinalityKeyValue("stage", "normalization");
            Timer.Sample normTimer = Timer.start(meterRegistry);

            Normalization norm = null;
            long startNorm = System.currentTimeMillis();
            try (Observation.Scope s = normObs.start().openScope()) {
                var normRes = normalizationService.normalize(extractedText);
                norm = new Normalization();
                norm.setExtractionId(ext.getId());
                norm.setNormalizedTerms(normRes.normalizedJson());
                norm.setRulesApplied(normRes.rulesJson());
                norm.setConfidence(normRes.confidence());
                norm = normalizationRepo.save(norm);
                meterRegistry.counter("pipeline.stage.success", "stage", "normalization").increment();
            } catch (Exception ex) {
                normObs.error(ex);
                meterRegistry.counter("pipeline.stage.failure", "stage", "normalization").increment();
                throw ex;
            } finally {
                int latencyNorm = (int) (System.currentTimeMillis() - startNorm);
                saveAiTrace(run.getTraceId(), "normalizer",
                        truncate(extractedText, 500),
                        truncate(norm != null ? norm.getNormalizedTerms() : null, 500),
                        latencyNorm, false);

                normObs.stop();
                normTimer.stop(Timer.builder("pipeline.stage.latency").tag("stage", "normalization").register(meterRegistry));
            }

            run.setCurrentStage(WorkflowStage.PRESCRIPTION);
            runRepo.save(run);
            Observation prescObs = Observation.createNotStarted("pipeline.stage", observationRegistry)
                    .lowCardinalityKeyValue("stage", "prescription");
            Timer.Sample prescTimer = Timer.start(meterRegistry);
            Prescription presc = null;
            long startPresc = System.currentTimeMillis();

            PatientDataResponseEvent patientData = null;
            try {
                UUID requestId = UUID.randomUUID();
                CompletableFuture<PatientDataResponseEvent> patientDataFuture =
                    patientDataService.createPendingRequest(requestId);

                PatientDataRequestEvent request = new PatientDataRequestEvent(
                    requestId, userId, run.getTraceId()
                );

                log.info("Requesting patient data: requestId={}, userId={}", requestId, userId);
                rabbitTemplate.convertAndSend(
                    RabbitConfig.EXCHANGE,
                    "patient.data.request",
                    request
                );

                patientData = patientDataFuture.get(8, TimeUnit.SECONDS);
                log.info("Received patient data: conditions={}, allergies={}, medications={}",
                    patientData.medicalData().conditions().size(),
                    patientData.medicalData().allergies().size(),
                    patientData.medicalData().medications().size());
            } catch (Exception ex) {
                log.warn("Failed to get patient data, proceeding without it: {}", ex.getMessage());
            }

            try (Observation.Scope s = prescObs.start().openScope()) {
                var llmRes = llmService.generatePrescription(
                    norm.getNormalizedTerms(),
                    patientData,
                    run.getTraceId()
                );
                presc = new Prescription();
                presc.setFileId(fileId);
                presc.setUserId(userId);
                presc.setNormalizationId(norm.getId());
                presc.setPrescriptionText(llmRes.rawText());
                presc.setParametersJson(llmRes.structuredJson());
                presc.setModelUsed(llmRes.modelId());
                presc.setGuardrailStatus(llmRes.guardrailStatus());
                presc = prescriptionRepo.save(presc);
                meterRegistry.counter("pipeline.stage.success", "stage", "prescription").increment();
            } catch (Exception ex) {
                prescObs.error(ex);
                meterRegistry.counter("pipeline.stage.failure", "stage", "prescription").increment();
                throw ex;
            } finally {
                int latencyPresc = (int) (System.currentTimeMillis() - startPresc);
                saveAiTrace(run.getTraceId(), "prescription-generator",
                        truncate(norm.getNormalizedTerms(), 500),
                        truncate(presc != null ? presc.getParametersJson() : null, 500),
                        latencyPresc, true);
                prescObs.stop();
                prescTimer.stop(Timer.builder("pipeline.stage.latency").tag("stage", "prescription").register(meterRegistry));
            }

            run.setCurrentStage(WorkflowStage.DONE);
            run.setStatus(WorkflowStatus.COMPLETED);
            runRepo.save(run);
            overallObs.lowCardinalityKeyValue("status", "success");
        } catch (Exception e) {
            log.error("Pipeline failed for file {}: {}", fileId, e.getMessage(), e);
            run.setCurrentStage(WorkflowStage.ERROR);
            run.setStatus(WorkflowStatus.FAILED);
            runRepo.save(run);
            meterRegistry.counter("pipeline.run.failed").increment();
            overallObs.error(e);
        } finally {
            overallObs.stop();
            total.stop(Timer.builder("pipeline.run.latency").register(meterRegistry));
        }
    }

    private void saveAiTrace(String traceId, String agentName, String inputSummary,
                             String outputSummary, int latencyMs, boolean blockedByGuardrail) {
        try {
            AiTrace trace = new AiTrace();
            trace.setTraceId(traceId);
            trace.setAgentName(agentName);
            trace.setInputSummary(inputSummary);
            trace.setOutputSummary(outputSummary);
            trace.setLatencyMs(latencyMs);
            trace.setBlockedByGuardrail(blockedByGuardrail);
            aiTraceRepo.save(trace);
        } catch (Exception e) {
            log.warn("Failed to save AI trace: {}", e.getMessage());
        }
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
    }

    private String escape(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", " ");
    }
}
