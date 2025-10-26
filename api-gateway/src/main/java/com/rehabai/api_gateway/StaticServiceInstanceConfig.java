package com.rehabai.api_gateway;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "services")
public class StaticServiceInstanceConfig {

    private Map<String, ServiceProperties> services = new HashMap<>();

    public Map<String, ServiceProperties> getServices() {
        return services;
    }

    public void setServices(Map<String, ServiceProperties> services) {
        this.services = services;
    }

    public static class ServiceProperties {
        private int port;

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }
    }
}
