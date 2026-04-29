package ai_knowledge_platform;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/ai")
public class AiController {

    private final KnowledgeRepository knowledgeRepo;

    // ✅ Constructor injection
    public AiController(KnowledgeRepository knowledgeRepo) {
        this.knowledgeRepo = knowledgeRepo;
    }

    private String findRelevantContent(String content, String question) {

        String[] paragraphs = content.split("\\n\\s*\\n");

        String[] keywords = question.toLowerCase().split("\\s+");

        StringBuilder relevantText = new StringBuilder();

        for (String paragraph : paragraphs) {
            String lowerParagraph = paragraph.toLowerCase();

            for (String keyword : keywords) {
                if (keyword.length() > 3 && lowerParagraph.contains(keyword)) {
                    relevantText.append(paragraph).append("\n\n");
                    break;
                }
            }

            if (relevantText.length() > 4000) {
                break;
            }
        }

        if (relevantText.length() == 0) {
            return content.length() > 4000 ? content.substring(0, 4000) : content;
        }

        return relevantText.toString();
    }

    // ✅ Existing API (general AI)
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

    // 🔥 NEW API (AI + your document)
    @PostMapping("/ask-from-document")
    public String askFromDocument(@RequestBody DocumentQuestionRequest request) {

        // 1. Get document from DB
        Knowledge knowledge = knowledgeRepo.findById(request.getKnowledgeId())
                .orElseThrow(() -> new RuntimeException("Document not found"));

        String content = knowledge.getContent();

        if (content == null || content.isBlank()) {
            throw new RuntimeException("No content found for this document");
        }

        // ⚠️ Limit content (important for small models)
        String relevantContent = findRelevantContent(content, request.getQuestion());

        // 2. Build prompt
        String prompt = """
                You are an AI assistant. Answer the user's question using ONLY the document content below.

                Document Content:
                %s

                Question:
                %s

                Answer:
                """.formatted(relevantContent, request.getQuestion());

        // 3. Call Ollama
        String ollamaUrl = "http://localhost:11434/api/generate";

        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> body = Map.of(
                "model", "llama3.2:1b",
                "prompt", prompt,
                "stream", false);

        Map response = restTemplate.postForObject(ollamaUrl, body, Map.class);

        return response.get("response").toString();
    }
}