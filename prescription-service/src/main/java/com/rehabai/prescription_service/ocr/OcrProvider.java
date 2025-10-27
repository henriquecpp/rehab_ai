package com.rehabai.prescription_service.ocr;

public interface OcrProvider {
    OCRService.Result ocr(byte[] fileBytes, String filename, String contentType);
    String name();
}

