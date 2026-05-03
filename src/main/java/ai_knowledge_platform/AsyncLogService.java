package ai_knowledge_platform;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AsyncLogService {

    @Async
    public void logAiActivity(String message) {
        System.out.println("ASYNC LOG: " + message);
    }
}