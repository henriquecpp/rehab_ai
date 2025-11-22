package com.rehabai.prescription_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String PATIENT_DATA_REQUEST_QUEUE = "patient.data.request";
    public static final String PATIENT_DATA_RESPONSE_QUEUE = "patient.data.response";
    public static final String EXCHANGE = "rehab.exchange";

    @Bean
    public Queue patientDataRequestQueue() {
        return new Queue(PATIENT_DATA_REQUEST_QUEUE, true);
    }

    @Bean
    public Queue patientDataResponseQueue() {
        return new Queue(PATIENT_DATA_RESPONSE_QUEUE, true);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Binding requestBinding(Queue patientDataRequestQueue, TopicExchange exchange) {
        return BindingBuilder.bind(patientDataRequestQueue).to(exchange).with("patient.data.request");
    }

    @Bean
    public Binding responseBinding(Queue patientDataResponseQueue, TopicExchange exchange) {
        return BindingBuilder.bind(patientDataResponseQueue).to(exchange).with("patient.data.response");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }
}

