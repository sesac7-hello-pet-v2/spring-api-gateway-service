package hello.pet.springapigatewayservice;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements Filter {

    private final JwtProvider jwtProvider;
    private final PublicPathMatcher publicPathMatcher;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;

//        // CORS 헤더를 먼저 추가 (401 에러 시에도 CORS 헤더가 포함되도록)
//        String origin = httpRequest.getHeader("Origin");
//        if (origin != null && (origin.equals("https://cotask.shop") ||
//                               origin.equals("http://cotask.shop") ||
//                               origin.equals("http://localhost:3000"))) {
//            httpResponse.setHeader("Access-Control-Allow-Origin", origin);
//            httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
//            httpResponse.setHeader("Access-Control-Allow-Headers", "*");
//            httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
//            httpResponse.setHeader("Access-Control-Max-Age", "3600");
//        }

        String path = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();

        log.info("JWT Filter - Path: {}, Method: {}", path, method);

		// 공개 경로 처리 - 로그인한 경우 사용자 정보를 추가로 전달
        if (publicPathMatcher.isPublicPath(path, method)) {
			log.info("JWT Filter: Public path detected");

			// 공개 경로이지만 JWT 토큰이 있는 경우 사용자 정보를 전달
			String token = jwtProvider.extractTokenFromRequest(httpRequest);
			if (token != null && jwtProvider.validateToken(token)) {
				Long userId = jwtProvider.getUserIdFromToken(token);
				String role = jwtProvider.getRoleFromToken(token);
				if (userId != null) {
					CustomHttpServletRequestWrapper requestWrapper = new CustomHttpServletRequestWrapper(httpRequest,
						userId, role);
					log.info("JWT Filter: Public path with valid token, adding X-User-Id: {}", userId);
					filterChain.doFilter(requestWrapper, httpResponse);
					return;
				}
			}

			log.info("JWT Filter: Public path without valid token, allowing request");
            filterChain.doFilter(httpRequest, httpResponse);
            return;
        }

        // JWT 토큰 검증
        String token = jwtProvider.extractTokenFromRequest(httpRequest);
        if (token == null || !jwtProvider.validateToken(token)) {
            log.info("JWT Filter: Invalid or missing token");
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.getWriter().write("{\"error\":\"Unauthorized\"}");
            return;
        }

        // JWT에서 사용자 ID 추출하여 헤더에 추가
        Long userId = jwtProvider.getUserIdFromToken(token);
        String role = jwtProvider.getRoleFromToken(token);
        if (userId != null) {
            CustomHttpServletRequestWrapper requestWrapper = new CustomHttpServletRequestWrapper(httpRequest, userId, role);
            log.info("JWT Filter: Valid token, adding X-User-Id: {}", userId);
            log.info("JWT Filter: Valid token, adding X-Role: {}", role);
            filterChain.doFilter(requestWrapper, httpResponse);
        } else {
            log.info("JWT Filter: Unable to extract userId from token");
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.getWriter().write("{\"error\":\"Invalid token claims\"}");
        }


    }

    private static class CustomHttpServletRequestWrapper extends jakarta.servlet.http.HttpServletRequestWrapper {

        private final Long userId;
        private final String role;

        public CustomHttpServletRequestWrapper(HttpServletRequest request, Long userId, String role) {
            super(request);
            this.userId = userId;
            this.role = role;
        }

        @Override
        public String getHeader(String name) {
            if ("X-User-Id".equals(name)) {
                return String.valueOf(userId);
            }
            if ("X-User-Role".equals(name)) {
                return role;
            }
            return super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            Set<String> headerNames = new HashSet<>();
            Enumeration<String> originalNames = super.getHeaderNames();
            while (originalNames.hasMoreElements()) {
                headerNames.add(originalNames.nextElement());
            }
            headerNames.add("X-User-Id");
            headerNames.add("X-User-Role");
            return Collections.enumeration(headerNames);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            if ("X-User-Id".equals(name)) {
                return Collections.enumeration(Collections.singletonList(String.valueOf(userId)));
            }
            if ("X-User-Role".equals(name)) {
                return Collections.enumeration(Collections.singletonList(role));
            }
            return super.getHeaders(name);
        }
    }
}
