package com.rehabai.plan_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String PLAN_EVENTS_EXCHANGE = "plan.events";
    public static final String PLAN_APPROVED_QUEUE = "notification.plan.approved";
    public static final String PLAN_APPROVED_ROUTING_KEY = "plan.approved";

    @Bean
    public TopicExchange planEventsExchange() {
        return new TopicExchange(PLAN_EVENTS_EXCHANGE, true, false);
    }

    @Bean
    public Queue planApprovedQueue() {
        return new Queue(PLAN_APPROVED_QUEUE, true);
    }

    @Bean
    public Binding planApprovedBinding(Queue planApprovedQueue, TopicExchange planEventsExchange) {
        return BindingBuilder.bind(planApprovedQueue).to(planEventsExchange).with(PLAN_APPROVED_ROUTING_KEY);
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

