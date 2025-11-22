package com.rehabai.prescription_service.llm;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rehabai.prescription_service.events.PatientDataResponseEvent;
import com.rehabai.prescription_service.model.GuardrailStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.ValidationException;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class LlmService {

    private static final Logger log = LoggerFactory.getLogger(LlmService.class);

    private record AnthropicMessage(String role, String content) {}
    private record AnthropicRequest(
            @JsonProperty("anthropic_version") String anthropicVersion,
            @JsonProperty("max_tokens") int maxTokens,
            List<AnthropicMessage> messages
    ) {}

    private record AnthropicResponseContent(String type, String text) {}
    private record AnthropicResponse(List<AnthropicResponseContent> content) {}

    private final BedrockRuntimeClient bedrock;
    private final ObjectMapper objectMapper;
    private final boolean useBedrock;
    private final String modelId;
    private final boolean guardrailsEnabled;
    private final String guardrailId;
    private final String guardrailVersion;
    public record Result(String rawText, String structuredJson, String modelId, GuardrailStatus guardrailStatus) {}

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

    public Result generatePrescription(String normalizedJson, String traceId) {
        return generatePrescription(normalizedJson, null, traceId);
    }

    public Result generatePrescription(String normalizedJson, PatientDataResponseEvent patientData, String traceId) {
        String prompt = buildPrompt(normalizedJson, patientData);
        log.info("Prompt={}", prompt);
        String usedModel = useBedrock ? modelId : "stub";
        String output;
        GuardrailStatus gs = GuardrailStatus.OK;

        try {
            if (useBedrock) {
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
                output = invokeClaude(prompt, 4096);
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


    private String buildPrompt(String normalizedJson, PatientDataResponseEvent patientData) {

        log.info("normalizedJson={}\npatientData={}\n", normalizedJson, patientData);
        StringBuilder patientContext = new StringBuilder();

        if (patientData != null && patientData.medicalData() != null) {
            var data = patientData.medicalData();

            patientContext.append("\n\n=== CONTEXTO DE SEGURANÇA DO PACIENTE (HISTÓRICO) ===\n");
            patientContext.append("ATENÇÃO: Use estes dados APENAS para validar contraindicações. NÃO crie exercícios para tratar estas condições a menos que estejam na mesma região anatômica da lesão principal.\n");

            if (data.conditions() != null && !data.conditions().isEmpty()) {
                patientContext.append("\n[CONDIÇÕES PRÉ-EXISTENTES]:\n");
                for (var condition : data.conditions()) {
                    patientContext.append("- ").append(condition.description());
                    if (condition.code() != null) {
                        patientContext.append(" (CID: ").append(condition.code()).append(")");
                    }
                    patientContext.append("\n");
                }
            }

            if (data.allergies() != null && !data.allergies().isEmpty()) {
                patientContext.append("\n[ALERGIAS - RESTRIÇÃO ABSOLUTA]:\n");
                for (var allergy : data.allergies()) {
                    patientContext.append("- ").append(allergy.substance());
                    if (allergy.reaction() != null) {
                        patientContext.append(" (Reação: ").append(allergy.reaction()).append(")");
                    }
                    patientContext.append("\n");
                }
            }

            if (data.medications() != null && !data.medications().isEmpty()) {
                patientContext.append("\n[MEDICAMENTOS EM USO]:\n");
                for (var med : data.medications()) {
                    patientContext.append("- ").append(med.drugName());
                    patientContext.append("\n");
                }
            }
            patientContext.append("=== FIM DO CONTEXTO DE SEGURANÇA ===\n\n");
        }

        String template = """
        Você é um especialista em reabilitação física e fisioterapia. Sua tarefa é gerar EXCLUSIVAMENTE um JSON VÁLIDO.
        Não escreva absolutamente nada fora do JSON final. NÃO escreva explicações.
        
        INSTRUÇÕES DE AUTOVALIDAÇÃO (INTERNAS - Chain of Thought):
        Antes de gerar a resposta final, verifique mentalmente cada item:
        1. O JSON é válido, sem markdown e sem comentários.
        2. "repetitions" e "duration" contêm APENAS NÚMEROS (proibido 's', 'min', strings).
        3. REGRA DE TEMPO: Se for alongamento/isometria, o tempo (em segundos) está em "duration" e "repetitions" é 1.
        4. REGRA DE CONTAGEM: Se for movimento, o número está em "repetitions" e "duration" é null.
        5. "confidence" é estritamente um float entre 0.0 e 1.0.
        6. A "description" de cada exercício segue o roteiro: Posição + Execução + Cuidados.
        7. As frequências são exatamente: "DIARIO", "SEMANAL" ou "TRES_VEZES_SEMANA".
        
        ESTRUTURA EXATA (JSON):
        {
          "title": string,
          "description": string,
          "diagnosis": string,
          "exercises": [
            {
              "name": string,
              "description": string,
              "sets": number (int),
              "repetitions": number (int),
              "duration": number (int, em segundos) or null,
              "frequency": "DIARIO" or "SEMANAL" or "TRES_VEZES_SEMANA"
            }
          ],
          "goals": [string, string, string],
          "duration": number (dias totais),
          "frequency": "DIARIO" or "SEMANAL" or "TRES_VEZES_SEMANA",
          "confidence": number (0.0-1.0)
        }
        
        >>> REGRAS DE MAPEAMENTO DE DADOS (CRÍTICO) <<<
        CASO 1: EXERCÍCIO DE REPETIÇÃO (Isotônico/Movimento)
        - sets: N
        - repetitions: N (número de repetições)
        - duration: null
        
        CASO 2: EXERCÍCIO DE TEMPO (Isométrico/Alongamento)
        - sets: N
        - repetitions: 1
        - duration: N (tempo em segundos APENAS NÚMERO)
        
        >>> DIRETRIZES DE QUALIDADE DE CONTEÚDO (CAMPO 'description') <<<
        O campo "description" deve ser um mini-tutorial detalhado contendo:
        1. POSIÇÃO INICIAL: Como se preparar.
        2. EXECUÇÃO: O movimento passo a passo.
        3. CUIDADO: Erros comuns a evitar.
        Exemplo BOM: "Posicione uma faixa elástica acima dos joelhos. Desça em um agachamento controlado, mantendo os joelhos alinhados com os pés. Volte à posição inicial empurrando os quadris para trás."
        
        >>> HIERARQUIA CLÍNICA <<<
        1. FONTE DA VERDADE: Use APENAS o 'LAUDO ATUAL' para definir o tratamento.
        2. SEGURANÇA: Use 'CONDIÇÕES PRÉ-EXISTENTES' APENAS para evitar contraindicações.
           - Aplique ISOLAMENTO ANATÔMICO: Se a condição pré-existente for em região diferente da lesão atual, ignore-a na seleção dos exercícios.
        3. PARAMETROS: 3 a 5 exercícios, progressão segura.
        
        %s
        
        DADOS DE ENTRADA (LAUDO ATUAL):
        %s
        
        RESPOSTA:
        Forneça APENAS o JSON final validado.
        """;

        return template.formatted(patientContext.toString(), normalizedJson);
    }
}