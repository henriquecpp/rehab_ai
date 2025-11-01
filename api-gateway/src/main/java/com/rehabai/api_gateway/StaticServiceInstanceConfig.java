package com.rehabai.api_gateway;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "services")
@Data
public class StaticServiceInstanceConfig {

    private Map<String, ServiceProperties> services = new HashMap<>();

    @Data
    public static class ServiceProperties {
        private int port;
    }
}
