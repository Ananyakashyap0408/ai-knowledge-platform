package ai_knowledge_platform;

import jakarta.persistence.*;

@Entity
@Table(name = "knowledge")
public class Knowledge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String content;

    // 🔗 Many Knowledge → One User
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Getters & Setters
    public Long getId() { return id; }

    public String getTitle() { return title; }

    public String getContent() { return content; }

    public User getUser() { return user; }

    public void setId(Long id) { this.id = id; }

    public void setTitle(String title) { this.title = title; }

    public void setContent(String content) { this.content = content; }

    public void setUser(User user) { this.user = user; }
}