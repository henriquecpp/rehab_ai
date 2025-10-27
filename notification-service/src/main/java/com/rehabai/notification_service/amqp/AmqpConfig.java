package com.rehabai.notification_service.amqp;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class AmqpConfig {

    @Bean
    public DirectExchange fileEventsExchange(@Value("${amqp.fileExchange:file.events}") String name) {
        return new DirectExchange(name, true, false);
    }

    @Bean
    public Queue notificationFileUploadedQueue(@Value("${amqp.notificationQueue:notification.file.uploaded}") String q) {
        return new Queue(q, true);
    }

    @Bean
    public Binding notificationBinding(Queue notificationFileUploadedQueue, DirectExchange fileEventsExchange,
                                       @Value("${amqp.routingKeyUploaded:file.uploaded}") String routingKey) {
        return BindingBuilder.bind(notificationFileUploadedQueue).to(fileEventsExchange).with(routingKey);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
