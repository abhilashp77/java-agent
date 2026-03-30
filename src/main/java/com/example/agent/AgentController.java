package com.example.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class AgentController {

    private final DatabaseAgent databaseAgent;
    private final ObjectMapper objectMapper;

    public AgentController(DatabaseAgent databaseAgent, ObjectMapper objectMapper) {
        this.databaseAgent = databaseAgent;
        this.objectMapper = objectMapper;
    }

    @PostMapping
    public Object askQuestion(@RequestBody Map<String, String> request) {
        String question = request.get("question");
        if (question == null || question.isBlank()) {
            return Map.of("error", "Question is required");
        }

        String rawResponse = databaseAgent.chat(question);

        try {
            // Parse the LLM's JSON response
            return objectMapper.readValue(rawResponse, Object.class);
        } catch (Exception e) {
            // If parsing fails, return the raw response string
            return Map.of(
                "steps", "Failed to parse JSON response",
                "answer", rawResponse
            );
        }
    }
}

