package com.systex.gateway.GateWayFilter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.systex.gateway.model.GateWayResponseHelper;
import com.systex.gateway.model.Result;
import com.systex.gateway.utils.JsonUtil;
import com.systex.gateway.utils.JwtUtil;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitFilter extends AbstractGatewayFilterFactory<RateLimitFilter.Config> {

    private static final int MAX_REQUESTS_PER_MINUTE = 5;
    private final Map<String, RateLimiter> limiters = new ConcurrentHashMap<>();

    public RateLimitFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String clientIp = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
            RateLimiter rateLimiter = limiters.computeIfAbsent(clientIp, k -> new RateLimiter());

            if (rateLimiter.allowRequest()) {
                return chain.filter(exchange);
            } else {
                exchange.getResponse().setStatusCode(config.getStatusCode());
                DataBuffer buffer = GateWayResponseHelper.parseToDataBuffer(config.getStatusCode(),429,Config.TOO_MANY_REQUEST,exchange.getResponse());
                return exchange.getResponse().writeWith(Mono.just(buffer));
            }
        };
    }

    public static class Config {
        private HttpStatus statusCode = HttpStatus.TOO_MANY_REQUESTS; // 默認為 HTTP 429 Too Many Requests
        private static final String TOO_MANY_REQUEST="Too many requests, please try again later.";
        public HttpStatus getStatusCode() {
            return statusCode;
        }

        public void setStatusCode(HttpStatus statusCode) {
            this.statusCode = statusCode;
        }
    }

    private static class RateLimiter {
        private final AtomicInteger requestCount = new AtomicInteger(0);
        private Instant windowStart = Instant.now();

        public synchronized boolean allowRequest() {
            Instant now = Instant.now();
            if (now.isAfter(windowStart.plusSeconds(60))) {
                windowStart = now;
                requestCount.set(0);
            }
            return requestCount.incrementAndGet() <= MAX_REQUESTS_PER_MINUTE;
        }
    }
}
