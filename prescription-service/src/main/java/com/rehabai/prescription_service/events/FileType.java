package com.rehabai.prescription_service.events;

public enum FileType {
    PRESCRIPTION,  // Laudo médico/prescrição para processamento de IA
    EXAM,          // Exames laboratoriais
    REPORT,        // Relatórios médicos
    IMAGE,         // Imagens (raio-x, ressonância, etc)
    OTHER          // Outros documentos
}

