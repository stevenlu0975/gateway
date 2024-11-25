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
            Claims claims=isValidToken(authorizationHeader);
            // 驗證 JWT Token（此處需根據你的實際驗證邏輯實現）
            if (claims==null) {
                DataBuffer buffer = GateWayResponseHelper.parseToDataBuffer(HttpStatus.UNAUTHORIZED,HttpStatus.UNAUTHORIZED.value(), Config.TOKEN_UNAUTHORIZED,exchange.getResponse());
                return exchange.getResponse().writeWith(Mono.just(buffer));
            }
            if(!isMaster(claims)){
                DataBuffer buffer = GateWayResponseHelper.parseToDataBuffer(HttpStatus.FORBIDDEN,HttpStatus.FORBIDDEN.value(), Config.TOKEN_FORBIDDEN,exchange.getResponse());
                return exchange.getResponse().writeWith(Mono.just(buffer));
            }
            log.info("Authorization header is invalid\n{}", authorizationHeader);
            // 如果 Token 有效，繼續處理請求
            return chain.filter(exchange);
        };
    }
    private boolean isMaster(Claims claims){
        log.info("USER_ID {}",claims.get("USER_ID"));
        String role = (String) claims.get("ROLE");
        log.info("ROLE {}",role);
        return role != null && role.equals("MASTER");
    }

    private Claims isValidToken(String token) {
        // 模擬 JWT 驗證，實際情況應該是解析並驗證 token
        Claims claims;
        try {
            if(token==null) {
                throw new Exception("no token");
            }
            claims = JwtUtil.parseJWT(JwtUtil.KEY, token);
        }
        catch (ExpiredJwtException e) {
            log.error(e.getMessage());
            return null;
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }

        return claims;  // 替換成實際的驗證邏輯
    }

    public static class Config {
        private HttpStatus statusCode = HttpStatus.TOO_MANY_REQUESTS; // 默認為 HTTP 429 Too Many Requests
        private static final String TOKEN_UNAUTHORIZED = "Token Unauthorized";
        private static final String TOKEN_FORBIDDEN = "Token Invalid";
        public HttpStatus getStatusCode() {
            return statusCode;
        }

        public void setStatusCode(HttpStatus statusCode) {
            this.statusCode = statusCode;
        }
    }

}
