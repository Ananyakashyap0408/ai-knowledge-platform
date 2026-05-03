package ai_knowledge_platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AiKnowledgePlatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiKnowledgePlatformApplication.class, args);
	}
}