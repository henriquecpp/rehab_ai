package com.rehabai.prescription_service.ocr;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.textract.TextractClient;
import software.amazon.awssdk.services.textract.model.AnalyzeDocumentRequest;
import software.amazon.awssdk.services.textract.model.BlockType;
import software.amazon.awssdk.services.textract.model.DetectDocumentTextRequest;
import software.amazon.awssdk.services.textract.model.Document;
import software.amazon.awssdk.services.textract.model.FeatureType;

@Component
public class TextractOcrProvider implements OcrProvider {
    private static final Logger log = LoggerFactory.getLogger(TextractOcrProvider.class);
    private final TextractClient textract;
    private final boolean useAnalyze;

    private final Counter analyzeSuccess;
    private final Counter analyzeFailure;
    private final Counter detectSuccess;
    private final Counter detectFailure;
    private final Counter fallbackToDetect;

    public TextractOcrProvider(TextractClient textract,
                               @Value("${ocr.textract.useAnalyze:true}") boolean useAnalyze,
                               MeterRegistry registry) {
        this.textract = textract;
        this.useAnalyze = useAnalyze;
        this.analyzeSuccess = registry.counter("ocr.textract.analyze.success");
        this.analyzeFailure = registry.counter("ocr.textract.analyze.failure");
        this.detectSuccess = registry.counter("ocr.textract.detect.success");
        this.detectFailure = registry.counter("ocr.textract.detect.failure");
        this.fallbackToDetect = registry.counter("ocr.textract.fallback.detect");
    }

    @Override
    public OCRService.Result ocr(byte[] fileBytes, String filename, String contentType) {
        Document doc = Document.builder().bytes(SdkBytes.fromByteArray(fileBytes)).build();
        try {
            if (useAnalyze) {
                var req = AnalyzeDocumentRequest.builder()
                        .document(doc)
                        .featureTypes(FeatureType.FORMS, FeatureType.TABLES)
                        .build();
                var resp = textract.analyzeDocument(req);
                StringBuilder sb = new StringBuilder();
                resp.blocks().forEach(b -> {
                    if (b.blockType() == BlockType.LINE && b.text() != null) {
                        sb.append(b.text()).append('\n');
                    }
                });
                analyzeSuccess.increment();
                return new OCRService.Result(sb.toString(), name(), 0.93);
            } else {
                var resp = textract.detectDocumentText(DetectDocumentTextRequest.builder().document(doc).build());
                StringBuilder sb = new StringBuilder();
                resp.blocks().forEach(b -> {
                    if (b.blockType() == BlockType.LINE && b.text() != null) {
                        sb.append(b.text()).append('\n');
                    }
                });
                detectSuccess.increment();
                return new OCRService.Result(sb.toString(), name(), 0.90);
            }
        } catch (Exception e) {
            log.error("Textract primary call failed (analyze? {}): {}", useAnalyze, e.getMessage());
            if (useAnalyze) {
                analyzeFailure.increment();
                // Fallback to Detect
                try {
                    var resp = textract.detectDocumentText(DetectDocumentTextRequest.builder().document(doc).build());
                    StringBuilder sb = new StringBuilder();
                    resp.blocks().forEach(b -> {
                        if (b.blockType() == BlockType.LINE && b.text() != null) {
                            sb.append(b.text()).append('\n');
                        }
                    });
                    fallbackToDetect.increment();
                    detectSuccess.increment();
                    return new OCRService.Result(sb.toString(), name(), 0.90);
                } catch (Exception e2) {
                    detectFailure.increment();
                    log.error("Textract detect fallback also failed: {}", e2.getMessage());
                    return new OCRService.Result("", name(), 0.0);
                }
            } else {
                detectFailure.increment();
                return new OCRService.Result("", name(), 0.0);
            }
        }
    }

    @Override
    public String name() { return "textract"; }
}
