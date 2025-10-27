package com.rehabai.notification_service.consumer;

import com.rehabai.notification_service.events.FileUploadedEvent;
import com.rehabai.notification_service.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FileUploadedNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(FileUploadedNotificationListener.class);

    private final EmailService emailService;
    private final String from;
    private final String to;

    public FileUploadedNotificationListener(EmailService emailService,
                                            @Value("${spring.mail.username}") String from,
                                            @Value("${notification.mail.to:${spring.mail.username}}") String to) {
        this.emailService = emailService;
        this.from = from;
        this.to = to;
    }

    @RabbitListener(queues = "${amqp.notificationQueue:notification.file.uploaded}")
    public void handle(FileUploadedEvent evt) {
        log.info("[Notification] Received file.uploaded: id={}, name={}, size={} bytes", evt.id(), evt.originalName(), evt.sizeBytes());
        String subject = "Arquivo recebido: " + (evt.originalName() != null ? evt.originalName() : evt.id());
        String body = "Um novo arquivo foi recebido e armazenado.\n" +
                "ID: " + evt.id() + "\n" +
                "Bucket/Key: " + evt.bucket() + "/" + evt.s3Path() + "\n" +
                "Tamanho: " + evt.sizeBytes() + " bytes\n" +
                "SHA-256: " + evt.hashSha256();
        try {
            emailService.send(from, to, subject, body);
        } catch (Exception e) {
            log.error("[Notification] Failed to send email for file {}: {}", evt.id(), e.getMessage(), e);
        }
    }
}

