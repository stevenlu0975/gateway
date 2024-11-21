package com.systex.gateway.GateWayFilter;

import com.systex.gateway.model.GateWayResponseHelper;
import com.systex.gateway.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
@Slf4j
@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {


    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            log.info("AuthenticationFilter start");
            Map<String,Object> claims = new HashMap<>();
            claims.put("USER_ID", "12345");
            claims.put("ROLE", "user");

            String token = JwtUtil.createJWT(JwtUtil.KEY, 365*24*60*60*1000, claims);
            log.info("generate token {}", token);
            exchange.getResponse().getHeaders().add("authToken", token);
            DataBuffer buffer = GateWayResponseHelper.parseToDataBuffer(config.getStatusCode(),200, "",exchange.getResponse());
            return exchange.getResponse().writeWith(Mono.just(buffer));
        };
    }

    public static class Config {
        private HttpStatus statusCode = HttpStatus.OK; // 默認為 HTTP 429 Too Many Requests

        public HttpStatus getStatusCode() {
            return statusCode;
        }

        public void setStatusCode(HttpStatus statusCode) {
            this.statusCode = statusCode;
        }
    }

}
