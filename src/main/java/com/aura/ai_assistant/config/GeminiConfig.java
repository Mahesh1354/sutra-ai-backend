package com.aura.ai_assistant.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class GeminiConfig {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.model:gemini-1.5-flash-002}")
    private String model;

    @Value("${gemini.temperature:0.7}")
    private double temperature;

    @Value("${gemini.max-tokens:2048}")
    private int maxTokens;

    @Value("${gemini.api.version:v1beta}")
    private String apiVersion;

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new org.springframework.web.client.DefaultResponseErrorHandler());
        return restTemplate;
    }

    // Getters
    public String getApiKey() {
        return apiKey;
    }

    public String getModel() {
        return model;
    }

    public double getTemperature() {
        return temperature;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public String getFullApiUrl() {
        return String.format("https://generativelanguage.googleapis.com/%s/models/%s:generateContent",
                apiVersion, model);
    }
}