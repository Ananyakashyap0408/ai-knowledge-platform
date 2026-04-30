package ai_knowledge_platform;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/ai")
public class AiController {

    private final KnowledgeRepository knowledgeRepo;
    private final ChatHistoryRepository chatRepo;
    private final UserRepository userRepo;

    public AiController(KnowledgeRepository knowledgeRepo,
            ChatHistoryRepository chatRepo,
            UserRepository userRepo) {
        this.knowledgeRepo = knowledgeRepo;
        this.chatRepo = chatRepo;
        this.userRepo = userRepo;
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

    @PostMapping("/ask-from-document")
    public String askFromDocument(@RequestBody DocumentQuestionRequest request) {

        Knowledge knowledge = knowledgeRepo.findById(request.getKnowledgeId())
                .orElseThrow(() -> new RuntimeException("Document not found"));

        User user = userRepo.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String content = knowledge.getContent();

        if (content == null || content.isBlank()) {
            throw new RuntimeException("No content found for this document");
        }

        String relevantContent = findRelevantContent(content, request.getQuestion());

        String prompt = """
                You are an AI assistant. Answer the user's question using ONLY the document content below.

                Document Content:
                %s

                Question:
                %s

                Answer:
                """.formatted(relevantContent, request.getQuestion());

        String ollamaUrl = "http://localhost:11434/api/generate";

        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> body = Map.of(
                "model", "llama3.2:1b",
                "prompt", prompt,
                "stream", false);

        Map response = restTemplate.postForObject(ollamaUrl, body, Map.class);

        String aiAnswer = response.get("response").toString();

        ChatHistory chat = new ChatHistory();
        chat.setQuestion(request.getQuestion());
        chat.setAnswer(aiAnswer);
        chat.setCreatedAt(LocalDateTime.now());
        chat.setUser(user);
        chat.setKnowledge(knowledge);

        chatRepo.save(chat);

        return aiAnswer;
    }
}