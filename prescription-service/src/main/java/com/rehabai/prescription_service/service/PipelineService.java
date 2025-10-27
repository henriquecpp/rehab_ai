package com.rehabai.prescription_service.service;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

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
        this.meterRegistry = meterRegistry;
        this.observationRegistry = observationRegistry;
        this.tracer = tracer;
    }

    @Transactional
    public void processFile(UUID fileId, byte[] content, String filename, String contentType) {
        WorkflowRun run = new WorkflowRun();
        run.setFileId(fileId);
        run.setCurrentStage(WorkflowStage.EXTRACTION);
        try {
            String traceId = tracer != null && tracer.currentSpan() != null ? tracer.currentSpan().context().traceId() : null;
            run.setTraceId(traceId);
        } catch (Exception ignore) {}
        run = runRepo.save(run);

        Timer.Sample total = Timer.start(meterRegistry);
        Observation overallObs = Observation.start("pipeline.run", observationRegistry)
                .lowCardinalityKeyValue("service", "prescription-service");
        try (Observation.Scope scope = overallObs.openScope()) {
            // Extraction Stage
            run.setCurrentStage(WorkflowStage.EXTRACTION);
            runRepo.save(run);
            Observation extractObs = Observation.createNotStarted("pipeline.stage", observationRegistry)
                    .lowCardinalityKeyValue("stage", "extraction");
            Timer.Sample extractTimer = Timer.start(meterRegistry);
            Extraction ext;
            String extractedText;
            try (Observation.Scope s = extractObs.start().openScope()) {
                var ocr = ocrService.extract(content, filename, contentType);
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

            // Normalization Stage
            run.setCurrentStage(WorkflowStage.NORMALIZATION);
            runRepo.save(run);
            Observation normObs = Observation.createNotStarted("pipeline.stage", observationRegistry)
                    .lowCardinalityKeyValue("stage", "normalization");
            Timer.Sample normTimer = Timer.start(meterRegistry);
            Normalization norm;
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
                normObs.stop();
                normTimer.stop(Timer.builder("pipeline.stage.latency").tag("stage", "normalization").register(meterRegistry));
            }

            // Prescription Stage
            run.setCurrentStage(WorkflowStage.PRESCRIPTION);
            runRepo.save(run);
            Observation prescObs = Observation.createNotStarted("pipeline.stage", observationRegistry)
                    .lowCardinalityKeyValue("stage", "prescription");
            Timer.Sample prescTimer = Timer.start(meterRegistry);
            try (Observation.Scope s = prescObs.start().openScope()) {
                var llm = llmService.generate(norm.getNormalizedTerms());
                Prescription pr = new Prescription();
                pr.setNormalizationId(norm.getId());
                pr.setPrescriptionText(llm.prescriptionText());
                pr.setParametersJson(llm.parametersJson());
                pr.setPromptVersion("v1");
                pr.setModelUsed(llm.modelUsed());
                pr.setGuardrailStatus(llm.guardrailStatus());
                prescriptionRepo.save(pr);
                meterRegistry.counter("pipeline.stage.success", "stage", "prescription").increment();
            } catch (Exception ex) {
                prescObs.error(ex);
                meterRegistry.counter("pipeline.stage.failure", "stage", "prescription").increment();
                throw ex;
            } finally {
                prescObs.stop();
                prescTimer.stop(Timer.builder("pipeline.stage.latency").tag("stage", "prescription").register(meterRegistry));
            }

            run.setCurrentStage(WorkflowStage.DONE);
            run.setStatus(WorkflowStatus.COMPLETED);
            runRepo.save(run);
            meterRegistry.counter("pipeline.run.completed").increment();
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

    private String escape(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", " ");
    }
}
