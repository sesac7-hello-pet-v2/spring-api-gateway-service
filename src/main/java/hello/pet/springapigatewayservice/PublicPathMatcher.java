package hello.pet.springapigatewayservice;

import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PublicPathMatcher {

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final List<String> PUBLIC_PATHS_ALL_METHODS = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/logout",
            "/api/v1/auth/refresh",
            "/api/v1/users/exist",
            "/actuator/health"
    );

    private static final Map<String, List<String>> PUBLIC_PATHS_BY_METHOD = new HashMap<>();

    static {
        // POST - 회원가입
        PUBLIC_PATHS_BY_METHOD.put("POST", List.of("/api/v1/users"));

        // GET - 공개 조회 API
        PUBLIC_PATHS_BY_METHOD.put("GET", List.of(
                "/api/v1/announcements",           // 공지사항 목록
                "/api/v1/announcements/**",        // 공지사항 상세
                "/api/posts",                       // 게시글 목록
                "/api/posts/**",                    // 게시글 상세
                "/api/v1/pets",                     // 펫 목록
                "/api/v1/pets/**"                   // 펫 상세
        ));
    }

    public boolean isPublicPath(String path, String method) {
        // OPTIONS 요청은 항상 허용
        if ("OPTIONS".equals(method)) {
            return true;
        }

        // 모든 메서드에서 공개인 경로 체크 (패턴 매칭)
        for (String publicPath : PUBLIC_PATHS_ALL_METHODS) {
            if (pathMatcher.match(publicPath, path)) {
                return true;
            }
        }

        // 특정 메서드에서만 공개인 경로 체크 (패턴 매칭)
        List<String> methodSpecificPaths = PUBLIC_PATHS_BY_METHOD.getOrDefault(method, List.of());
        for (String publicPath : methodSpecificPaths) {
            if (pathMatcher.match(publicPath, path)) {
                return true;
            }
        }

        return false;
    }
}