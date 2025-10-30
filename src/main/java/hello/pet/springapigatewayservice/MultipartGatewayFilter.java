package hello.pet.springapigatewayservice;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom Filter to handle multipart/form-data requests properly in Spring Cloud Gateway MVC
 * This filter preserves the Content-Type header with boundary information
 */
@Component
public class MultipartGatewayFilter implements Filter, Ordered {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            String contentType = httpRequest.getContentType();

            // Check if this is a multipart request
            if (contentType != null && contentType.startsWith(MediaType.MULTIPART_FORM_DATA_VALUE)) {
                // Wrap the request to preserve headers
                HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(httpRequest) {
                    private final Map<String, String> customHeaders = new HashMap<>();

                    {
                        // Preserve the original Content-Type with boundary
                        customHeaders.put("Content-Type", contentType);
                    }

                    @Override
                    public String getHeader(String name) {
                        String header = customHeaders.get(name);
                        return (header != null) ? header : super.getHeader(name);
                    }

                    @Override
                    public Enumeration<String> getHeaders(String name) {
                        String header = customHeaders.get(name);
                        if (header != null) {
                            return Collections.enumeration(Collections.singletonList(header));
                        }
                        return super.getHeaders(name);
                    }

                    @Override
                    public String getContentType() {
                        return contentType;
                    }
                };

                chain.doFilter(wrapper, response);
            } else {
                chain.doFilter(request, response);
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}