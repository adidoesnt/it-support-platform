package com.adityabanerjee.worker.classifier;

import org.springframework.stereotype.Component;

import com.adityabanerjee.worker.llm.LlmClient;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class LlmIncidentClassifier implements IncidentClassifier {
    private final LlmClient llmClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LlmIncidentClassifier(LlmClient llmClient) {
        this.llmClient = llmClient;
    }

    private String extractFirstJsonObject(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start == -1 || end == -1 || end <= start) {
            throw new IllegalArgumentException(String.format("No JSON object found in response: %s", text));
        }
        return text.substring(start, end + 1);
    }

    public ClassificationResult classifyIncident(String incidentDescription) {
        try {
            String prompt = buildPrompt(incidentDescription);

            String rawResponse = llmClient.generateText(prompt);
            String jsonOnly = extractFirstJsonObject(rawResponse);

            ClassificationResult classificationResult = objectMapper.readValue(jsonOnly, ClassificationResult.class);

            // Validate the classification result
            // If the values are not in the enums, an exception will be thrown
            IncidentCategory.valueOf(classificationResult.category());
            IncidentPriority.valueOf(classificationResult.priority());

            // Validate the summary
            if (classificationResult.summary() == null || classificationResult.summary().isBlank()) {
                throw new IllegalArgumentException("No summary generated");
            }

            return classificationResult;
        } catch (Exception e) {
            System.out.println(String.format("[WARNING] Error classifying incident: %s", e.getMessage()));

            // Classification is best effort, so we return a default result
            return new ClassificationResult(
                    IncidentCategory.OTHER.name(),
                    IncidentPriority.P3.name(),
                    "Unknown incident");
        }
    }

    private String buildPrompt(String incidentDescription) {
        String categoryOptions = java.util.Arrays.stream(IncidentCategory.values())
                .map(Enum::name)
                .collect(java.util.stream.Collectors.joining(", "));
        String priorityOptions = java.util.Arrays.stream(IncidentPriority.values())
                .map(Enum::name)
                .collect(java.util.stream.Collectors.joining(", "));

        return String.format("""
                You are an IT incident classifier.

                Classify the following incident description.

                Rules:
                - category must be one of: %s
                - priority must be one of: %s
                - summary must be a single sentence
                - Return ONLY valid JSON
                - Do not include explanations or extra text

                Incident description:
                \"%s\"

                Return JSON in this exact format:
                {
                  "category": "...",
                  "priority": "...",
                  "summary": "..."
                }

                DO NOT INCLUDE ANY EXTRA TEXT OR COMMENTS IN YOUR RESPONSE.
                """, categoryOptions, priorityOptions, incidentDescription);
    }
}
