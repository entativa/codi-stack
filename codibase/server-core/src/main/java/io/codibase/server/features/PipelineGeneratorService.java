package io.codibase.server.features.ai;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class PipelineGeneratorService {
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    public String generatePipeline(String projectLanguage, String framework, List<String> requirements) {
        Map<String, Object> request = new HashMap<>();
        request.put("complexity", "complex");
        request.put("prompt", buildPipelinePrompt(projectLanguage, framework, requirements));
        
        Map<String, Object> response = restTemplate.postForObject(
            "http://pilotcodi:8081/v1/chat",
            request,
            Map.class
        );
        
        return extractPipeline(response);
    }
    
    private String buildPipelinePrompt(String lang, String framework, List<String> reqs) {
        return String.format(
            "Generate a complete CI/CD pipeline configuration for:\n" +
            "Language: %s\n" +
            "Framework: %s\n" +
            "Requirements: %s\n\n" +
            "Include: build, test, lint, security scan, and deploy stages.",
            lang, framework, String.join(", ", reqs)
        );
    }
    
    private String extractPipeline(Map<String, Object> response) {
        return response.get("content").toString();
    }
}
