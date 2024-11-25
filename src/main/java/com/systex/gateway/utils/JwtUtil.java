package com.systex.gateway.utils;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class JwtUtil {
//	public static final String KEY="systex";
	public static final String KEY="MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAzI+VmlAtc/hyGv+QxoBz3E6b2wHFZ7FCYRjwXVw9+thaj82V/+l1D5s5KlkE75rz9P6sqZvnz9GyHl6jl8KF+hhVXbUeGQ9XyCkJbHdvj7RFq6+8ISopK0SpFDhvKAz2g8IaOPODiL7mwTEFeTm100Nf77ZuRLS2kjTiQ0jiljguvQaX0QgNDrJioCrJbr4MnlQwGG2K09pIywQsi3K+VHPCHqPDfBFoihhW5g/+FaRQEjRwcZJoR0AZxIf1XEy9YWo1Q1Gwo5tIWcWLm5BqjLl32WGCB4BSLNiSiVCmYd4ogNvmGUyhxWssilDDcLOOO43/+d3nVa0CYZ5pN/gxCwIDAQAB";
    /**
     * 生成jwt
     * 使用Hs256算法, key使用固定字串
     *
     * @param secretKey jwt秘鑰
     * @param ttlMillis jwt timeout(毫秒)
     * @param claims    设置的信息
     * @return
     */
    public static String createJWT(String secretKey, long ttlMillis, Map<String, Object> claims) {
        // 指定簽名的時候使用的簽名算法，也就是header那部分
//        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.RS256;
        // 生成JWT的時間
        long expMillis = System.currentTimeMillis() + ttlMillis;
        Date exp = new Date(expMillis);

        // 設置jwt的body
        JwtBuilder builder = Jwts.builder()
                // 如果有私有聲明，一定要先設置這個自己創建的私有的聲明，這個是給builder的claim賦值，一但寫在標準的聲明賦值之後，就是覆蓋了那些標準的聲明的
                .setClaims(claims)
                // 設置簽名使用的簽名算法和簽名使用的秘鑰
                .signWith(signatureAlgorithm, secretKey.getBytes(StandardCharsets.UTF_8))
                // 設置過期時間
                .setExpiration(exp);
        return builder.compact();
    }
    /**
     * Token解密
     *
     * @param secretKey jwt key
     * @param token     加密後的token
     * @return
     */
    public static Claims parseJWT(String secretKey, String token) {
        // 得到DefaultJwtParser
        Claims claims = Jwts.parser()
                // 設置簽名的秘鑰
                .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
                // 設置需要解析的jwt
                .parseClaimsJws(token).getBody();
        return claims;
    }

    public static Claims parseJWT(PublicKey publicKey, String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(publicKey)
                .parseClaimsJws(token).getBody();
        return claims;
    }

    public static PublicKey loadPublicKey(String publicKeyStr) throws Exception {
        String publicKeyPEM = publicKeyStr
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] keyBytes = Base64.getDecoder().decode(publicKeyPEM);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }
}
