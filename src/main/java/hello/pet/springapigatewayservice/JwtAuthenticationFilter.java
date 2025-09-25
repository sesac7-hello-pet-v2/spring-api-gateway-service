package hello.pet.springapigatewayservice;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.io.IOException;

@Slf4j
@Component
public class JwtAuthenticationFilter implements Filter {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;

        String path = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();

        log.info("JWT Filter - Path: {}, Method: {}", path, method);

        // 공개 경로는 인증 없이 통과
        if (isPublicPath(path, method)) {
            System.out.println("JWT Filter: Public path, allowing request");
            filterChain.doFilter(httpRequest, httpResponse);
            return;
        }

        // JWT 토큰 검증
        String token = getTokenFromRequest(httpRequest);
        if (token == null || !validateToken(token)) {
            log.info("JWT Filter: Invalid or missing token");
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.getWriter().write("{\"error\":\"Unauthorized\"}");
            return;
        }
    }

    private boolean isPublicPath(String path, String method) {
        return path.equals("/api/users/login") ||
                "OPTIONS".equals(method);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
