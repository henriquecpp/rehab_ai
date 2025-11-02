package com.rehabai.file_service.amqp;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
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
}
