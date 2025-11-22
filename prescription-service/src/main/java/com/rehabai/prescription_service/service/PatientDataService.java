package com.rehabai.prescription_service.service;

import com.rehabai.prescription_service.events.PatientDataResponseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


@Service
public class PatientDataService {

    private static final Logger log = LoggerFactory.getLogger(PatientDataService.class);

    private final Map<UUID, CompletableFuture<PatientDataResponseEvent>> pendingRequests = new ConcurrentHashMap<>();


    public CompletableFuture<PatientDataResponseEvent> createPendingRequest(UUID requestId) {
        CompletableFuture<PatientDataResponseEvent> future = new CompletableFuture<>();
        pendingRequests.put(requestId, future);

        CompletableFuture.delayedExecutor(10, TimeUnit.SECONDS).execute(() -> {
            if (!future.isDone()) {
                future.completeExceptionally(new RuntimeException("Timeout waiting for patient data"));
                pendingRequests.remove(requestId);
            }
        });

        return future;
    }


    public void completeRequest(UUID requestId, PatientDataResponseEvent response) {
        CompletableFuture<PatientDataResponseEvent> future = pendingRequests.remove(requestId);
        if (future != null) {
            future.complete(response);
            log.info("Patient data request completed: requestId={}", requestId);
        } else {
            log.warn("Received patient data response for unknown requestId={}", requestId);
        }
    }
}

