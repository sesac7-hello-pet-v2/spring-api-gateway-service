package hello.pet.springapigatewayservice;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class PublicPathMatcher {

    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/api/v1/auth/login",
            "/api/v1/auth/logout",
            "/api/v1/auth/refresh",
            "/api/v1/users",
            "/api/v1/users/exist"
    );

    public boolean isPublicPath(String path, String method) {
        // OPTIONS 요청은 항상 허용
        if ("OPTIONS".equals(method)) {
            return true;
        }

        // 정확한 경로 매칭
        return PUBLIC_PATHS.stream().anyMatch(path::equals);
    }
}