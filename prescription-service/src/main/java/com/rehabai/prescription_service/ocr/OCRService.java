package com.rehabai.prescription_service.ocr;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class OCRService {

    private static final Logger log = LoggerFactory.getLogger(OCRService.class);

    private final OcrProvider tess4jProvider;
    private final OcrProvider textractProvider;
    private final boolean useTextract;

    private final MeterRegistry registry;
    private final Counter pdfTextSuccess;
    private final Counter pdfTextFallback;
    private final Counter textRejected;

    // Heuristic thresholds (calibráveis)
    private final int minCharsNoWs;
    private final double minLetterRatio;
    private final int minUniqueChars;
    private final double minNonWsDensity;
    private final double minConfidence;

    public OCRService(Tess4jOcrProvider tess4jProvider,
                      TextractOcrProvider textractOcrProvider,
                      @Value("${ocr.useTextract:false}") boolean useTextract,
                      MeterRegistry registry,
                      @Value("${ocr.heuristic.minCharsNoWs:30}") int minCharsNoWs,
                      @Value("${ocr.heuristic.minLetterRatio:0.15}") double minLetterRatio,
                      @Value("${ocr.heuristic.minUniqueChars:10}") int minUniqueChars,
                      @Value("${ocr.heuristic.minNonWsDensity:0.30}") double minNonWsDensity,
                      @Value("${ocr.fallback.minConfidence:0.2}") double minConfidence) {
        this.tess4jProvider = tess4jProvider;
        this.textractProvider = textractOcrProvider;
        this.useTextract = useTextract;
        this.registry = registry;
        this.pdfTextSuccess = registry.counter("ocr.pdf_text.success");
        this.pdfTextFallback = registry.counter("ocr.pdf_text.fallback");
        this.textRejected = registry.counter("ocr.text.rejected");
        this.minCharsNoWs = minCharsNoWs;
        this.minLetterRatio = minLetterRatio;
        this.minUniqueChars = minUniqueChars;
        this.minNonWsDensity = minNonWsDensity;
        this.minConfidence = minConfidence;
    }

    public record Result(String text, String engine, double confidence) {}

    public Result extract(byte[] fileBytes, String filename, String contentType) {
        boolean isPdf = isPdf(filename, contentType);
        boolean isImage = isImage(filename, contentType);

        // 1) PDF: tentar extrair texto "nativo" primeiro (PDFBox)
        if (isPdf) {
            try {
                String text = PdfTextExtractor.extract(fileBytes);
                if (hasMeaningfulText(text)) {
                    pdfTextSuccess.increment();
                    return new Result(text, "pdfbox-text", 0.95);
                } else {
                    textRejected.increment();
                    pdfTextFallback.increment();
                    log.info("PDF sem texto significativo; fazendo fallback para OCR (prov: {})", chooseProviderName(filename, contentType));
                }
            } catch (Exception e) {
                pdfTextFallback.increment();
                log.warn("Falha ao extrair texto nativo do PDF; usando OCR. Causa: {}", e.getMessage());
            }
            // Para PDF, preferimos o provedor de OCR de páginas (tess4j); Textract síncrono não cobre PDF multi-página
            return timedOcr(fileBytes, filename, contentType, "pdf");
        }

        // 2) Imagem: usar OCR direto (Textract AnalyzeDocument quando habilitado)
        if (isImage) {
            return timedOcr(fileBytes, filename, contentType, "image");
        }

        // 3) Outros tipos (txt etc.): tentar tratar como texto puro
        String asText = new String(fileBytes, StandardCharsets.UTF_8);
        if (hasMeaningfulText(asText)) {
            return new Result(asText, "plain-text", 0.99);
        } else {
            textRejected.increment();
            // Último recurso: empurrar para OCR (tratar como imagem desconhecida)
            return timedOcr(fileBytes, filename, contentType, "unknown");
        }
    }

    private OcrProvider chooseProvider(String filename, String contentType) {
        if (isPdf(filename, contentType)) {
            return tess4jProvider;
        }
        if (isImage(filename, contentType)) {
            return useTextract ? textractProvider : tess4jProvider;
        }
        return tess4jProvider;
    }

    private String chooseProviderName(String filename, String contentType) {
        return chooseProvider(filename, contentType).name();
    }

    private Result timedOcr(byte[] fileBytes, String filename, String contentType, String typeTag) {
        OcrProvider provider = chooseProvider(filename, contentType);
        Timer.Sample sample = Timer.start(registry);
        try {
            Result r = provider.ocr(fileBytes, filename, contentType);
            // Cross-provider fallback: if Textract yielded vazio/baixo para imagem, tentar Tess4J
            boolean low = r == null || r.text() == null || r.text().isBlank() || r.confidence() < minConfidence;
            if (low && provider == textractProvider && isImage(filename, contentType)) {
                registry.counter("ocr.provider.fallback.cross", "from", provider.name(), "to", tess4jProvider.name(), "type", typeTag).increment();
                log.info("Fallback cross-provider: {} -> {}", provider.name(), tess4jProvider.name());
                // medir também a latência do fallback
                Timer.Sample fb = Timer.start(registry);
                Result fbRes = tess4jProvider.ocr(fileBytes, filename, contentType);
                fb.stop(Timer.builder("ocr.provider.latency")
                        .tag("provider", tess4jProvider.name())
                        .tag("type", typeTag)
                        .register(registry));
                registry.counter("ocr.provider.calls", "provider", tess4jProvider.name(), "type", typeTag).increment();
                return fbRes;
            }
            return r;
        } finally {
            sample.stop(Timer.builder("ocr.provider.latency")
                    .tag("provider", provider.name())
                    .tag("type", typeTag)
                    .register(registry));
            registry.counter("ocr.provider.calls", "provider", provider.name(), "type", typeTag).increment();
        }
    }

    private boolean isPdf(String filename, String contentType) {
        return (contentType != null && contentType.equalsIgnoreCase("application/pdf"))
                || (filename != null && filename.toLowerCase().endsWith(".pdf"));
    }

    private boolean isImage(String filename, String contentType) {
        boolean ct = contentType != null && contentType.toLowerCase().startsWith("image/");
        boolean ext = filename != null && filename.toLowerCase().matches(".*\\.(png|jpg|jpeg|tif|tiff)$");
        return ct || ext;
    }

    // Heurística de "texto significativo" calibrável
    private boolean hasMeaningfulText(String text) {
        if (text == null) return false;
        String raw = text;
        // Remover controles
        String trimmed = raw.replaceAll("[\\u0000-\\u001F]", "");
        if (trimmed.isBlank()) return false;
        String noSpaces = trimmed.replaceAll("\\s+", "");
        if (noSpaces.length() < minCharsNoWs) return false;

        long letters = trimmed.chars().filter(ch -> Character.isLetter(ch)).count();
        double letterRatio = (double) letters / Math.max(1, trimmed.length());
        if (letterRatio < minLetterRatio) return false;

        long uniqueChars = trimmed.chars().distinct().count();
        if (uniqueChars < minUniqueChars) return false;

        double nonWsDensity = (double) noSpaces.length() / Math.max(1, trimmed.length());
        return nonWsDensity >= minNonWsDensity;
    }
}
