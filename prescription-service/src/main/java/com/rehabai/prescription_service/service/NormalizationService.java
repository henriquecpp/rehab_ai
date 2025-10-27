package com.rehabai.prescription_service.service;

import com.rehabai.prescription_service.llm.LlmService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NormalizationService {

    public record Result(String normalizedJson, String rulesJson, double confidence) {}

    private final boolean useLlm;
    private final LlmService llmService;
    private final MeterRegistry meterRegistry;
    private final ObservationRegistry observationRegistry;

    public NormalizationService(@Value("${normalization.useLlm:false}") boolean useLlm,
                                LlmService llmService,
                                MeterRegistry meterRegistry,
                                ObservationRegistry observationRegistry) {
        this.useLlm = useLlm;
        this.llmService = llmService;
        this.meterRegistry = meterRegistry;
        this.observationRegistry = observationRegistry;
    }

    public Result normalize(String extractedText) {
        Observation obs = Observation.createNotStarted("pipeline.stage", observationRegistry)
                .lowCardinalityKeyValue("stage", "normalization.llm." + (useLlm ? "on" : "off"));
        Timer.Sample timer = Timer.start(meterRegistry);
        try (Observation.Scope s = obs.start().openScope()) {
            if (useLlm) {
                try {
                    String normalized = llmService.normalizeText(extractedText);
                    String rules = "{\"rulesApplied\":[\"llm-normalizer\"]}";
                    meterRegistry.counter("normalization.method", "type", "llm").increment();
                    return new Result(normalized, rules, 0.85);
                } catch (Exception e) {
                    obs.error(e);
                    meterRegistry.counter("normalization.llm.failure").increment();
                }
            }
            String normalized = "{\"findings\": [\"" + escape(extractedText) + "\"], \"contraindications\": []}";
            String rules = "{\"rulesApplied\": [\"basic-trim\", \"lowercase\"]}";
            meterRegistry.counter("normalization.method", "type", "basic").increment();
            return new Result(normalized, rules, 0.70);
        } finally {
            obs.stop();
            timer.stop(Timer.builder("pipeline.stage.latency").tag("stage", "normalization").register(meterRegistry));
        }
    }

    private String escape(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", " ");
    }
}
