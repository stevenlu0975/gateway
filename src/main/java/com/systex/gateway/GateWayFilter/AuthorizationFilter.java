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

import java.util.List;
import java.util.Map;

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

//        log.info("USER_ID {}",claims.get("USER_ID"));
//        String role = (String) claims.get("ROLE");
//        log.info("ROLE {}",role);
//        return role != null && role.equals("MASTER");
        // 1. 獲取 realm_access
        Map<String, Object> realmAccess = (Map<String, Object>) claims.get("realm_access");
        if(realmAccess==null){
            return false;
        }
        List<String> realmRoles = (List<String>) realmAccess.get("roles");

        if(realmRoles==null || !realmRoles.contains("uploadpdf")){
            return false;
        }
        System.out.println("Realm Roles: " + realmRoles);

        // 2. 獲取 resource_access
        Map<String, Object> resourceAccess = (Map<String, Object>) claims.get("resource_access");
        if(resourceAccess==null){
            return false;
        }
        // 針對特定的 client (如 myclient)
        Map<String, Object> myClientRoles = (Map<String, Object>) resourceAccess.get("myclient");
        if(myClientRoles == null){
            return false;
        }
        List<String> clientRoles = (List<String>) myClientRoles.get("roles");
        if(clientRoles == null){
            return false;
        }
        System.out.println("MyClient Roles: " + clientRoles);
        if(clientRoles.contains("uploadpdf")){
            return true;
        }

        return false;
    }

    private Claims isValidToken(String token) {
        // 模擬 JWT 驗證，實際情況應該是解析並驗證 token
        Claims claims;
        try {
            if(token==null) {
                throw new Exception("no token");
            }
//            claims = JwtUtil.parseJWT(JwtUtil.KEY, token);
            claims = JwtUtil.parseJWT(JwtUtil.loadPublicKey(JwtUtil.KEY), token);
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
