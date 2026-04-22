package ai_knowledge_platform;

import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/knowledge")
public class KnowledgeController {

    private final KnowledgeRepository repo;
    private final UserRepository userRepo;

    public KnowledgeController(KnowledgeRepository repo, UserRepository userRepo) {
        this.repo = repo;
        this.userRepo = userRepo;
    }

    // ✅ CREATE Knowledge for a User
    @PostMapping("/user/{userId}")
    public Knowledge createKnowledge(@PathVariable Long userId,
            @Valid @RequestBody Knowledge knowledge) {

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        knowledge.setUser(user);

        return repo.save(knowledge);
    }

    @GetMapping
    public List<Knowledge> getAllKnowledge() {
        return repo.findAll();
    }

    // ✅ GET ALL Knowledge BY ID
    @GetMapping("/{id}")
    public Knowledge getKnowledgeById(@PathVariable Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Knowledge not found with id: " + id));
    }

    @GetMapping("/user/{userId}")
    public List<Knowledge> getKnowledgeByUser(@PathVariable Long userId) {

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return user.getKnowledgeList();
    }

    @PutMapping("/{id}")
    public Knowledge updateKnowledge(@PathVariable Long id,
            @Valid @RequestBody Knowledge knowledge) {

        Knowledge existing = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Knowledge not found with id: " + id));

        existing.setTitle(knowledge.getTitle());
        existing.setContent(knowledge.getContent());

        return repo.save(existing);
    }

    @DeleteMapping("/{id}")
    public String deleteKnowledge(@PathVariable Long id) {

        Knowledge existing = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Knowledge not found with id: " + id));

        repo.delete(existing);

        return "Knowledge deleted successfully with id: " + id;
    }
}