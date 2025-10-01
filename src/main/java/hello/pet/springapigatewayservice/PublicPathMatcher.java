package hello.pet.springapigatewayservice;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PublicPathMatcher {

    private static final List<String> PUBLIC_PATHS_ALL_METHODS = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/logout",
            "/api/v1/auth/refresh",
            "/api/v1/users/exist"
    );

    private static final Map<String, List<String>> PUBLIC_PATHS_BY_METHOD = new HashMap<>();

    static {
        PUBLIC_PATHS_BY_METHOD.put("POST", List.of("/api/v1/users"));
    }

    public boolean isPublicPath(String path, String method) {
        // OPTIONS 요청은 항상 허용
        if ("OPTIONS".equals(method)) {
            return true;
        }

        // 모든 메서드에서 공개인 경로 체크
        if (PUBLIC_PATHS_ALL_METHODS.contains(path)) {
            return true;
        }

        // 특정 메서드에서만 공개인 경로 체크
        return PUBLIC_PATHS_BY_METHOD.getOrDefault(method, List.of()).contains(path);
    }
}