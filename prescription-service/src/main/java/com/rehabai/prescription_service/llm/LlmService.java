package com.rehabai.prescription_service.llm;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rehabai.prescription_service.model.GuardrailStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.ValidationException;: Para capturar erros de guardrail

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class LlmService {

    private static final Logger log = LoggerFactory.getLogger(LlmService.class);

    // Representa a request para a API Anthropic Messages
    private record AnthropicMessage(String role, String content) {}
    private record AnthropicRequest(
            @JsonProperty("anthropic_version") String anthropicVersion,
            @JsonProperty("max_tokens") int maxTokens,
            List<AnthropicMessage> messages
    ) {}

    // Representa a resposta da API Anthropic Messages
    private record AnthropicResponseContent(String type, String text) {}
    private record AnthropicResponse(List<AnthropicResponseContent> content) {}

    private final BedrockRuntimeClient bedrock;
    private final ObjectMapper objectMapper;
    private final boolean useBedrock;
    private final String modelId;
    private final boolean guardrailsEnabled;
    private final String guardrailId;
    private final String guardrailVersion;
    public record Result(String prescriptionText, String parametersJson, String modelUsed, GuardrailStatus guardrailStatus) {}

    public LlmService(BedrockRuntimeClient bedrock,
                      ObjectMapper objectMapper,
                      @Value("${llm.useBedrock:false}") boolean useBedrock,
                      @Value("${llm.modelId:anthropic.claude-haiku-4-5-20251001-v1:0}") String modelId,
                      @Value("${guardrails.enabled:true}") boolean guardrailsEnabled,
                      @Value("${guardrails.id:}") String guardrailId,            
                      @Value("${guardrails.version:DRAFT}") String guardrailVersion
    ) {
        this.bedrock = bedrock;
        this.objectMapper = objectMapper;
        this.useBedrock = useBedrock;
        this.modelId = modelId;
        this.guardrailsEnabled = guardrailsEnabled;
        this.guardrailId = guardrailId;        
        this.guardrailVersion = guardrailVersion;
    }

    public Result generate(String normalizedJson) {
        String prompt = buildPrompt(normalizedJson);
        String usedModel = useBedrock ? modelId : "stub";
        String output;
        GuardrailStatus gs = GuardrailStatus.OK;

        try {
            if (useBedrock) {
                // MUDANÇA: Chama o método genérico de invocação
                output = invokeClaude(prompt, 4096);
            } else {
                output = "{\"plan\": \"Exemplo de plano gerado\", \"phases\": []}";
            }
        } catch (ValidationException ve) {
            log.warn("Bedrock call blocked or invalid: {}", ve.getMessage());
            if (ve.getMessage() != null && ve.getMessage().contains("guardrail")) {
                gs = GuardrailStatus.BLOCKED;
                output = "{\"plan\": \"Geração bloqueada pela política de segurança\"}";
            } else {
                output = "{\"plan\": \"Erro de validação\"}";
            }
            usedModel = "error";
        } catch (Exception e) {
            log.error("Bedrock invocation failed: {}", e.getMessage(), e);
            output = "{\"plan\": \"Erro ao gerar plano\"}";
            usedModel = "error";
        }

        String params = "{\"promptVersion\": \"v1\"}";
        return new Result(output, params, usedModel, gs);
    }

    public String normalizeText(String extractedText) {
        String prompt = "Normalize clinical findings from the following text into a compact JSON with keys: findings[], contraindications[], conditions[], and include ICD-10 codes if possible. Respond with JSON only. Text: " + extractedText;
        String output;
        try {
            if (useBedrock) {
                output = invokeClaude(prompt, 2048); // Tokens menores para normalização
            } else {
                output = "{\"findings\":[\"Sample finding\"],\"contraindications\":[],\"conditions\":[],\"codes\":[]}";
            }
        } catch (Exception e) {
            log.error("Bedrock normalization failed: {}", e.getMessage(), e);
            output = "{\"findings\":[],\"contraindications\":[],\"conditions\":[],\"codes\":[]}";
        }
        return output;
    }

    private String invokeClaude(String prompt, int maxTokens) throws Exception {
        var message = new AnthropicMessage("user", prompt);
        var requestPayload = new AnthropicRequest(
                "bedrock-2023-05-31",
                maxTokens,
                List.of(message)
        );

        String requestBody = objectMapper.writeValueAsString(requestPayload);

        var reqBuilder = InvokeModelRequest.builder()
                .modelId(modelId)
                .body(SdkBytes.fromString(requestBody, StandardCharsets.UTF_8))
                .accept("application/json")
                .contentType("application/json");

        if (guardrailsEnabled && guardrailId != null && !guardrailId.isEmpty()) {
            reqBuilder.guardrailIdentifier(guardrailId)
                    .guardrailVersion(guardrailVersion);
        }

        var resp = bedrock.invokeModel(reqBuilder.build());
        String responseBody = resp.body().asUtf8String();

        var anthropicResponse = objectMapper.readValue(responseBody, AnthropicResponse.class);

        var response = anthropicResponse.content().stream()
                .filter(c -> "text".equals(c.type()))
                .map(AnthropicResponseContent::text)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No text content in Bedrock response"));

        log.debug("Bedrock response = {}",response);
        return response;
    }

    private String buildPrompt(String normalizedJson) {
        return """
            Você é um especialista em reabilitação física e fisioterapia. Baseado nos achados clínicos normalizados fornecidos,
            gere um plano de reabilitação completo e estruturado em formato JSON.
            
            Dados de entrada (JSON normalizado):
            %s
            
            Gere um JSON com a seguinte estrutura EXATA:
            {
              "title": "Título descritivo do plano (ex: Plano de Reabilitação Pós-Cirúrgica de Joelho)",
              "description": "Descrição detalhada do plano e objetivos principais",
              "diagnosis": "Diagnóstico principal baseado nos achados",
              "exercises": [
                {
                  "name": "Nome do exercício",
                  "description": "Descrição detalhada de como executar",
                  "sets": 3,
                  "repetitions": 10,
                  "duration": null ou número em segundos,
                  "frequency": "DIARIO" ou "SEMANAL" ou "TRES_VEZES_SEMANA"
                }
              ],
              "goals": [
                "Objetivo 1: específico e mensurável",
                "Objetivo 2: específico e mensurável",
                "Objetivo 3: específico e mensurável"
              ],
              "duration": 30,
              "frequency": "DIARIO",
              "confidence": 0.85
            }
            
            IMPORTANTE:
            - Inclua pelo menos 3-5 exercícios específicos e seguros
            - Os exercícios devem ser progressivos (do mais simples ao mais complexo)
            - Cada exercício deve ter instruções claras
            - Os objetivos devem ser SMART (específicos, mensuráveis, alcançáveis, relevantes, temporais)
            - A duração deve ser realista (geralmente 15-60 dias)
            - Considere contraindicações e limitações do paciente
            - Responda APENAS com o JSON, sem texto adicional
            """.formatted(normalizedJson);
    }
}