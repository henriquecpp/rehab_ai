package com.rehabai.prescription_service.llm;

import com.rehabai.prescription_service.model.GuardrailStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

@Service
public class LlmService {

    private static final Logger log = LoggerFactory.getLogger(LlmService.class);

    private final BedrockRuntimeClient bedrock;
    private final boolean useBedrock;
    private final String modelId;
    private final boolean guardrailsEnabled;

    public record Result(String prescriptionText, String parametersJson, String modelUsed, GuardrailStatus guardrailStatus) {}

    public LlmService(BedrockRuntimeClient bedrock,
                      @Value("${llm.useBedrock:false}") boolean useBedrock,
                      @Value("${llm.modelId:anthropic.claude-3-haiku}") String modelId,
                      @Value("${guardrails.enabled:true}") boolean guardrailsEnabled) {
        this.bedrock = bedrock;
        this.useBedrock = useBedrock;
        this.modelId = modelId;
        this.guardrailsEnabled = guardrailsEnabled;
    }

    public Result generate(String normalizedJson) {
        String prompt = buildPrompt(normalizedJson);
        String output;
        String usedModel = useBedrock ? modelId : "stub";
        try {
            if (useBedrock) {
                var req = InvokeModelRequest.builder()
                        .modelId(modelId)
                        .body(SdkBytes.fromString("{\"prompt\": " + jsonEscape(prompt) + "}", StandardCharsets.UTF_8))
                        .build();
                var resp = bedrock.invokeModel(req);
                output = resp.body().asUtf8String();
            } else {
                output = "{\"plan\": \"Exemplo de plano gerado\", \"phases\": []}";
            }
        } catch (Exception e) {
            log.error("Bedrock invocation failed: {}", e.getMessage(), e);
            output = "{\"plan\": \"Erro ao gerar plano\"}";
            usedModel = "error";
        }

        GuardrailStatus gs = GuardrailStatus.OK;
        if (guardrailsEnabled && violatesGuardrails(output)) {
            gs = GuardrailStatus.BLOCKED;
        }
        String params = "{\"promptVersion\": \"v1\"}";
        return new Result(output, params, usedModel, gs);
    }

    public String normalizeText(String extractedText) {
        String prompt = "Normalize clinical findings from the following text into a compact JSON with keys: findings[], contraindications[], conditions[], and include ICD-10 codes if possible. Respond with JSON only. Text: " + extractedText;
        String output;
        try {
            if (useBedrock) {
                var req = InvokeModelRequest.builder()
                        .modelId(modelId)
                        .body(SdkBytes.fromString("{\"prompt\": " + jsonEscape(prompt) + "}", StandardCharsets.UTF_8))
                        .build();
                var resp = bedrock.invokeModel(req);
                output = resp.body().asUtf8String();
            } else {
                output = "{\"findings\":[\"" + escape(extractedText) + "\"],\"contraindications\":[],\"conditions\":[],\"codes\":[]}";
            }
        } catch (Exception e) {
            log.error("Bedrock normalization failed: {}", e.getMessage(), e);
            output = "{\"findings\":[],\"contraindications\":[],\"conditions\":[],\"codes\":[]}";
        }
        return output;
    }

    private String buildPrompt(String normalizedJson) {
        return "Você é um especialista em reabilitação. Dado o JSON de achados normalizados, gere um plano por fases com metas e critérios de progressão, em JSON estruturado. Entradas: " + normalizedJson;
    }

    private boolean violatesGuardrails(String output) {
        Pattern p = Pattern.compile("(?i)(cpf|rg|senha|password)");
        return p.matcher(output).find();
    }

    private String jsonEscape(String s) {
        String escaped = s.replace("\\", "\\\\").replace("\"", "\\\"");
        return "\"" + escaped + "\"";
    }

    private String escape(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", " ");
    }
}
