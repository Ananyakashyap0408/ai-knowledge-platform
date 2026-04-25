package ai_knowledge_platform;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;

import com.cloudinary.Cloudinary;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository repo;
    private final Cloudinary cloudinary;
    private final KnowledgeRepository knowledgeRepo;

    // ✅ SINGLE constructor (FIXED)
    public UserController(UserRepository repo,
            Cloudinary cloudinary,
            KnowledgeRepository knowledgeRepo) {
        this.repo = repo;
        this.cloudinary = cloudinary;
        this.knowledgeRepo = knowledgeRepo;
    }

    // ✅ SIGNUP
    @PostMapping("/signup")
    public User signup(@Valid @RequestBody User user) {

        if (repo.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        return repo.save(user);
    }

    // ✅ LOGIN
    @PostMapping("/login")
    public String login(@RequestBody User user) {

        User existingUser = repo.findByEmail(user.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!existingUser.getPassword().equals(user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return JwtUtil.generateToken(existingUser.getEmail());
    }

    // ✅ UPLOAD FILE (Cloudinary + DB)
    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file,
            @RequestParam("userId") Long userId) {
        try {

            // 1. Upload to Cloudinary
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    Map.of("resource_type", "auto"));

            String fileUrl = uploadResult.get("secure_url").toString();

            // 2. Get user
            User user = repo.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // 3. Save in DB
            Knowledge knowledge = new Knowledge();
            knowledge.setFileName(file.getOriginalFilename());
            knowledge.setFileUrl(fileUrl);
            knowledge.setUser(user);

            knowledgeRepo.save(knowledge);

            return "File uploaded & saved: " + fileUrl;

        } catch (Exception e) {
            e.printStackTrace();
            return "Upload failed: " + e.getMessage();
        }
    }

    // ✅ GET ALL USERS
    @GetMapping
    public List<User> getUsers() {
        return repo.findAll();
    }

    @GetMapping("/files")
    public List<Knowledge> getAllFiles() {
        return knowledgeRepo.findAll();
    }

    // ✅ GET BY ID
    @GetMapping("/{id:\\d+}")
    public User getUserById(@PathVariable Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    // ✅ UPDATE
    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @Valid @RequestBody User user) {
        User existingUser = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        existingUser.setName(user.getName());
        existingUser.setEmail(user.getEmail());

        return repo.save(existingUser);
    }

    // ✅ DELETE
    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable Long id) {
        User existingUser = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        repo.delete(existingUser);
        return "User deleted successfully with id: " + id;
    }
}