package ai_knowledge_platform;

import com.cloudinary.Cloudinary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(Map.of(
                "cloud_name", "ddppxoqoo",
                "api_key", "661273998978157",
                "api_secret", "FR4C7onZRhzt3DL4m3rh-lb3CHM"));
    }
}