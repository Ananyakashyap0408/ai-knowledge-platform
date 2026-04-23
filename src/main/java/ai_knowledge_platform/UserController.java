package ai_knowledge_platform;

import org.springframework.web.bind.annotation.*;
import java.util.List;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository repo;

    public UserController(UserRepository repo) {
        this.repo = repo;
    }

    // ✅ CREATE
    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        return repo.save(user);
    }

    @PostMapping("/signup")
    public User signup(@Valid @RequestBody User user) {

        // Check if email already exists
        if (repo.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        return repo.save(user);
    }

    // ✅ GET ALL
    @GetMapping
    public List<User> getUsers() {
        return repo.findAll();
    }

    // ✅ GET BY ID
    @GetMapping("/{id}")
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