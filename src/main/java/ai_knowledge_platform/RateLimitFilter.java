package ai_knowledge_platform;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;

@Component
public class RateLimitFilter implements Filter {

    private final StringRedisTemplate redisTemplate;

    public RateLimitFilter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void doFilter(ServletRequest request,
            ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();

        // Apply rate limiting only on AI APIs
        if (path.startsWith("/ai")) {

            String clientIp = httpRequest.getRemoteAddr();
            String key = "rate_limit:" + clientIp;

            Long count = redisTemplate.opsForValue().increment(key);

            if (count != null && count == 1) {
                redisTemplate.expire(key, Duration.ofMinutes(1));
            }

            if (count != null && count > 5) {
                httpResponse.setStatus(429);
                httpResponse.getWriter().write("Too many requests. Please try again after 1 minute.");
                return;
            }
        }

        chain.doFilter(request, response);
    }
}