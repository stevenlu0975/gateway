package com.systex.gateway.GateWayFilter;

import com.systex.gateway.model.GateWayResponseHelper;
import com.systex.gateway.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.logging.Logger;
@Slf4j
@Component
public class AuthorizationFilter extends AbstractGatewayFilterFactory<AuthorizationFilter.Config> {


    public AuthorizationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // 從請求中提取 Authorization 標頭
            String authorizationHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
            log.info("Authorization header: " + authorizationHeader);
            if (authorizationHeader == null) {
                log.info("Authorization header is null");
                DataBuffer buffer = GateWayResponseHelper.parseToDataBuffer(HttpStatus.UNAUTHORIZED,HttpStatus.UNAUTHORIZED.value(), Config.NO_AUTHORIZATION_HEADER,exchange.getResponse());
                return exchange.getResponse().writeWith(Mono.just(buffer));
            }
            // 驗證 JWT Token（此處需根據你的實際驗證邏輯實現）
            if (!isValidToken(authorizationHeader)) {
                DataBuffer buffer = GateWayResponseHelper.parseToDataBuffer(HttpStatus.UNAUTHORIZED,HttpStatus.UNAUTHORIZED.value(), Config.TOKEN_INVALID,exchange.getResponse());
                return exchange.getResponse().writeWith(Mono.just(buffer));
            }
            log.info("Authorization header is invalid\n{}", authorizationHeader);
            // 如果 Token 有效，繼續處理請求
            return chain.filter(exchange);
        };
    }
    // todo 5455
    private boolean isValidToken(String token) {
        // 模擬 JWT 驗證，實際情況應該是解析並驗證 token
        try {
            if(token==null) {
                throw new Exception("no token");
            }
            Claims claims = JwtUtil.parseJWT(JwtUtil.KEY, token);
            if(claims==null) {
                throw new Exception("invalid token");
            }
            log.info("USER_ID {}",claims.get("USER_ID"));
        }
        catch (ExpiredJwtException e) {
            log.error(e.getMessage());
            return false;
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }

        return true;  // 替換成實際的驗證邏輯
    }

    public static class Config {
        private HttpStatus statusCode = HttpStatus.TOO_MANY_REQUESTS; // 默認為 HTTP 429 Too Many Requests
        private static final String NO_AUTHORIZATION_HEADER = "NO Authorization Header";
        private static final String TOKEN_INVALID = "Token Invalid";
        public HttpStatus getStatusCode() {
            return statusCode;
        }

        public void setStatusCode(HttpStatus statusCode) {
            this.statusCode = statusCode;
        }
    }

}
