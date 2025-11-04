package io.codibase.server.features.ai;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class AiCodeReviewService {
    
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String PILOTCODI_URL = "http://pilotcodi:8081";
    
    public Map<String, Object> reviewPullRequest(String prId, String diff) {
        Map<String, Object> request = new HashMap<>();
        request.put("complexity", "complex");
        request.put("prompt", buildReviewPrompt(diff));
        
        // Call PilotCodi/Claude for review
        Map<String, Object> response = restTemplate.postForObject(
            PILOTCODI_URL + "/v1/chat",
            request,
            Map.class
        );
        
        return parseReviewResponse(response);
    }
    
    private String buildReviewPrompt(String diff) {
        return String.format(
            "Review this code change and provide feedback on:\n" +
            "1. Potential bugs\n" +
            "2. Security vulnerabilities\n" +
            "3. Performance issues\n" +
            "4. Best practices\n\n" +
            "Diff:\n%s", diff
        );
    }
    
    private Map<String, Object> parseReviewResponse(Map<String, Object> response) {
        // Parse AI response into structured feedback
        Map<String, Object> review = new HashMap<>();
        review.put("summary", response.get("content"));
        review.put("suggestions", extractSuggestions(response));
        review.put("severity", calculateSeverity(response));
        return review;
    }
    
    private List<String> extractSuggestions(Map<String, Object> response) {
        // Extract actionable suggestions from AI response
        return new ArrayList<>();
    }
    
    private String calculateSeverity(Map<String, Object> response) {
        // Analyze response for severity level
        return "medium";
    }
}
