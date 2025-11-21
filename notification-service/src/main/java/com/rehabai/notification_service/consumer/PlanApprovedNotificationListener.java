package com.rehabai.notification_service.consumer;

import com.rehabai.notification_service.events.PlanApprovedEvent;
import com.rehabai.notification_service.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class PlanApprovedNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(PlanApprovedNotificationListener.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final EmailService emailService;
    private final String from;
    private final boolean enabled;

    public PlanApprovedNotificationListener(
            EmailService emailService,
            @Value("${spring.mail.username}") String from,
            @Value("${notification.plan-approved.enabled:true}") boolean enabled) {
        this.emailService = emailService;
        this.from = from;
        this.enabled = enabled;
    }

    @RabbitListener(queues = "${amqp.planApprovedQueue:notification.plan.approved}")
    public void handle(PlanApprovedEvent event) {
        log.info("[Notification] Received plan.approved: planId={}, userId={}, email={}, title={}",
                event.planId(), event.userId(), event.userEmail(), event.planTitle());

        if (!enabled) {
            log.info("[Notification] Plan approval notifications are disabled, skipping");
            return;
        }

        try {
            // Validate user email is present
            if (event.userEmail() == null || event.userEmail().isBlank()) {
                log.warn("[Notification] Cannot send notification - user email is missing for userId={}",
                    event.userId());
                return;
            }

            String subject = "✅ Seu Plano de Reabilitação foi Aprovado!";
            String body = buildEmailBody(event);
            String to = event.userEmail();

            emailService.send(from, to, subject, body);

            log.info("[Notification] Plan approval notification sent: planId={}, userId={}, email={}",
                    event.planId(), event.userId(), to);

        } catch (Exception e) {
            log.error("[Notification] Failed to send plan approval notification for plan {}: {}",
                    event.planId(), e.getMessage(), e);
        }
    }

    private String buildEmailBody(PlanApprovedEvent event) {
        StringBuilder body = new StringBuilder();

        body.append("Olá!\n\n");
        body.append("Temos uma ótima notícia! Seu plano de reabilitação foi aprovado e está pronto para começar.\n\n");

        body.append("📋 Detalhes do Plano:\n");
        body.append("───────────────────────────────────────\n");
        body.append("Título: ").append(event.planTitle()).append("\n");

        if (event.planDescription() != null && !event.planDescription().isEmpty()) {
            body.append("Descrição: ").append(event.planDescription()).append("\n");
        }

        body.append("Data de Aprovação: ").append(event.approvedAt().format(DATE_FORMATTER)).append("\n");
        body.append("ID do Plano: ").append(event.planId()).append("\n");
        body.append("───────────────────────────────────────\n\n");

        body.append("🎯 Próximos Passos:\n");
        body.append("1. Acesse o sistema para visualizar os detalhes completos do seu plano\n");
        body.append("2. Revise os exercícios e objetivos estabelecidos\n");
        body.append("3. Comece sua jornada de recuperação!\n\n");

        body.append("💪 Dicas Importantes:\n");
        body.append("• Siga rigorosamente as orientações do plano\n");
        body.append("• Realize os exercícios com a frequência recomendada\n");
        body.append("• Em caso de dúvidas ou desconforto, entre em contato com seu terapeuta\n");
        body.append("• Acompanhe seu progresso através do sistema\n\n");

        body.append("Estamos torcendo pela sua recuperação!\n\n");
        body.append("Equipe RehabAI\n");
        body.append("──────────────────────────────────────────\n");
        body.append("Este é um e-mail automático. Por favor, não responda.\n");

        return body.toString();
    }
}

