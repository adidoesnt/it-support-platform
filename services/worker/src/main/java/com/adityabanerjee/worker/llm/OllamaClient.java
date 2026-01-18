package com.adityabanerjee.worker.llm;

import java.util.Map;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

// TODO: Try using the Ollama4J library instead of making HTTP requests

@Component
@Profile("local")
public class OllamaClient implements LlmClient {
    private final String baseUrl;
    private final String model;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    public OllamaClient(@Value("${worker.llm.ollama.baseUrl}") String baseUrl,
            @Value("${worker.llm.ollama.model}") String model) {
        this.baseUrl = baseUrl;
        this.model = model;
    }

    @Override
    public String generateText(String prompt) {
        try {
            String url = String.format("%s/api/generate", baseUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));

            Map<String, Object> requestBody = Map.of("model", model, "prompt", prompt, "stream", false);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new IllegalStateException(String.format("Failed to generate text: %s", response.getBody()));
            }

            String responseBody = response.getBody();

            if (responseBody == null) {
                throw new IllegalStateException("Failed to generate text: response body is null");
            }

            JsonNode json = objectMapper.readTree(responseBody);
            JsonNode out = json.get("response");

            if (out == null || out.isNull()) {
                throw new IllegalStateException("Failed to generate text: response is null");
            } else if (!out.isTextual()) {
                throw new IllegalStateException("Failed to generate text: response is not textual");
            }

            return out.asText();
        } catch (Exception e) {
            throw new IllegalStateException(String.format("Failed to generate text: %s", e.getMessage()), e);
        }
    }
}
