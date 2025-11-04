package io.codibase.server.features.search;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class AiCodeSearchService {
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    public List<Map<String, Object>> semanticSearch(String query, String repoId) {
        // Use PilotCodi's embeddings for semantic code search
        Map<String, Object> request = new HashMap<>();
        request.put("query", query);
        request.put("repository", repoId);
        
        Map<String, Object> response = restTemplate.postForObject(
            "http://pilotcodi:8081/v1/search",
            request,
            Map.class
        );
        
        return (List<Map<String, Object>>) response.get("results");
    }
    
    public String explainCode(String code, String context) {
        Map<String, Object> request = new HashMap<>();
        request.put("complexity", "simple");
        request.put("prompt", "Explain this code in simple terms:\n\n" + code);
        
        Map<String, Object> response = restTemplate.postForObject(
            "http://pilotcodi:8081/v1/chat",
            request,
            Map.class
        );
        
        return response.get("content").toString();
    }
}
