package com.rehabai.prescription_service.ocr;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

@Component
public class Tess4jOcrProvider implements OcrProvider {
    private static final Logger log = LoggerFactory.getLogger(Tess4jOcrProvider.class);

    private final Tesseract tesseract;
    private final int maxPages;

    public Tess4jOcrProvider(@Value("${ocr.tessdataPath:}") String tessdataPath,
                             @Value("${ocr.lang:por+eng}") String lang,
                             @Value("${ocr.maxPages:5}") int maxPages) {
        this.tesseract = new Tesseract();
        if (tessdataPath != null && !tessdataPath.isBlank()) {
            this.tesseract.setDatapath(tessdataPath);
        }
        this.tesseract.setLanguage(lang);
        this.maxPages = Math.max(1, maxPages);
    }

    @Override
    public OCRService.Result ocr(byte[] fileBytes, String filename, String contentType) {
        try {
            boolean isPdf = filename != null && filename.toLowerCase().endsWith(".pdf");
            StringBuilder allText = new StringBuilder();
            if (isPdf) {
                try (PDDocument pdf = Loader.loadPDF(fileBytes)) {
                    PDFRenderer renderer = new PDFRenderer(pdf);
                    int pages = Math.min(pdf.getNumberOfPages(), maxPages);
                    for (int i = 0; i < pages; i++) {
                        BufferedImage image = renderer.renderImageWithDPI(i, 300);
                        String text = tesseract.doOCR(image);
                        if (text != null && !text.isBlank()) {
                            if (allText.length() > 0) allText.append("\n\n");
                            allText.append(text.trim());
                        }
                    }
                }
            } else {
                BufferedImage image = ImageIO.read(new ByteArrayInputStream(fileBytes));
                if (image != null) {
                    String text = tesseract.doOCR(image);
                    if (text != null) allText.append(text.trim());
                }
            }
            return new OCRService.Result(allText.toString(), name(), allText.length() > 0 ? 0.75 : 0.0);
        } catch (TesseractException te) {
            log.error("Tesseract OCR failed: {}", te.getMessage(), te);
            return new OCRService.Result("", name(), 0.0);
        } catch (Exception e) {
            log.error("OCR processing error: {}", e.getMessage(), e);
            return new OCRService.Result("", name(), 0.0);
        }
    }

    @Override
    public String name() { return "tess4j"; }
}
