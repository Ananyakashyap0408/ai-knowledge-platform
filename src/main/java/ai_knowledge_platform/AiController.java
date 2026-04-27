package ai_knowledge_platform;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/ai")
public class AiController {

    @PostMapping("/ask")
    public String askAi(@RequestBody AiRequest request) {

        String ollamaUrl = "http://localhost:11434/api/generate";

        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> body = Map.of(
                "model", "llama3.2:1b",
                "prompt", request.getQuestion(),
                "stream", false);

        Map response = restTemplate.postForObject(ollamaUrl, body, Map.class);

        return response.get("response").toString();
    }
}