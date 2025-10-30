package hello.pet.springapigatewayservice;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Custom Gateway Filter to handle multipart/form-data requests properly
 * This filter preserves the Content-Type header with boundary information
 */
@Component
public class MultipartGatewayFilter extends AbstractGatewayFilterFactory<MultipartGatewayFilter.Config> implements Ordered {

    public MultipartGatewayFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            HttpHeaders headers = request.getHeaders();
            MediaType contentType = headers.getContentType();

            // Check if this is a multipart request
            if (contentType != null && contentType.includes(MediaType.MULTIPART_FORM_DATA)) {
                // Preserve the original Content-Type with boundary
                String originalContentType = headers.getFirst(HttpHeaders.CONTENT_TYPE);

                ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header(HttpHeaders.CONTENT_TYPE, originalContentType)
                    .build();

                ServerWebExchange mutatedExchange = exchange.mutate()
                    .request(mutatedRequest)
                    .build();

                return chain.filter(mutatedExchange);
            }

            return chain.filter(exchange);
        };
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    public static class Config {
        // Configuration properties if needed
    }
}