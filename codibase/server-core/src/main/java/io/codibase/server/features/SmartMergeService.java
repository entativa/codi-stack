package io.codibase.server.features.ai;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class SmartMergeService {
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    public Map<String, Object> resolveConflicts(String baseCode, String theirCode, String ourCode) {
        Map<String, Object> request = new HashMap<>();
        request.put("complexity", "complex");
        request.put("prompt", buildMergePrompt(baseCode, theirCode, ourCode));
        
        Map<String, Object> response = restTemplate.postForObject(
            "http://pilotcodi:8081/v1/chat",
            request,
            Map.class
        );
        
        return Map.of(
            "resolved", true,
            "mergedCode", extractMergedCode(response),
            "explanation", response.get("content")
        );
    }
    
    private String buildMergePrompt(String base, String theirs, String ours) {
        return String.format(
            "Resolve this merge conflict by combining the best of both changes:\n\n" +
            "Base version:\n%s\n\n" +
            "Their changes:\n%s\n\n" +
            "Our changes:\n%s\n\n" +
            "Provide the merged code and explain your reasoning.", 
            base, theirs, ours
        );
    }
    
    private String extractMergedCode(Map<String, Object> response) {
        // Extract code from AI response
        return response.get("content").toString();
    }
}
