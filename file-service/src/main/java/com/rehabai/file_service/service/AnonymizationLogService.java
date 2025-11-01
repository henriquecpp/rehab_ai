package com.rehabai.file_service.service;

import com.rehabai.file_service.model.AnonymizationLog;
import com.rehabai.file_service.repository.AnonymizationLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnonymizationLogService {

    private final AnonymizationLogRepository repo;

    @Transactional(readOnly = true)
    public List<AnonymizationLog> listByFile(UUID fileId) {
        return repo.findByFileIdOrderByTimestampDesc(fileId);
    }

    @Transactional
    public AnonymizationLog add(UUID fileId, String ruleApplied, String fieldChanged) {
        AnonymizationLog log = new AnonymizationLog();
        log.setFileId(fileId);
        log.setRuleApplied(ruleApplied);
        log.setFieldChanged(fieldChanged);
        log.setTimestamp(OffsetDateTime.now());
        return repo.save(log);
    }
}
