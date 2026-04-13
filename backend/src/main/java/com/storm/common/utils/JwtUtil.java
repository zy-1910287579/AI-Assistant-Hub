// JwtUtil.java
package com.storm.common.utils;

import com.storm.common.enums.ErrorCode;
import com.storm.common.exception.BusinessException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {
    
    // 使用密钥，实际项目中应从配置文件读取
    private static final String SECRET_KEY = "storm_secret_key_for_jwt_token_generation_and_validation";
    
    // Token过期时间：2小时
    private static final long ACCESS_TOKEN_EXPIRATION = 2 * 60 * 60 * 1000L;
    
    // Refresh Token过期时间：7天
    private static final long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000L;
    
    /**
     * 生成访问令牌(Access Token)
     */
    public String generateAccessToken(String subject, Map<String, Object> claims) {
        return createToken(subject, claims, ACCESS_TOKEN_EXPIRATION);
    }
    
    /**
     * 生成刷新令牌(Refresh Token)
     */
    public String generateRefreshToken(String subject, Map<String, Object> claims) {
        return createToken(subject, claims, REFRESH_TOKEN_EXPIRATION);
    }
    
    /**
     * 创建令牌
     */
    private String createToken(String subject, Map<String, Object> claims, long expirationTime) {
        Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * 解析令牌
     */
    public Claims parseToken(String token) {
        try {
            Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "登录信息过期,请重新登录!");
        }
    }
    
    /**
     * 验证令牌是否有效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
    
    /**
     * 从令牌中获取用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }
}