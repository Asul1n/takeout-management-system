package com.takeout.common.util;

import cn.hutool.core.date.DateUtil;
import com.takeout.common.constant.CommonConstant;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类 — 签发、校验、解析 Token
 */
public class JwtUtil {

    private static final String SECRET = "Takeout2026SecretKeyForJWTTokenGenerationAndValidation!!";
    private static final long EXPIRATION = 24 * 60 * 60 * 1000L; // 24小时

    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    /**
     * 生成 JWT Token
     *
     * @param userId 用户ID
     * @param role   用户角色
     * @return JWT 字符串
     */
    public static String generate(Long userId, String role) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + EXPIRATION);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("userId", userId)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(KEY)
                .compact();
    }

    /**
     * 解析 Token 中的 Claims
     */
    public static Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从 Token 中获取用户ID
     */
    public static Long getUserId(String token) {
        try {
            return parseClaims(token).get("userId", Long.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从 Token 中获取用户角色
     */
    public static String getRole(String token) {
        try {
            return parseClaims(token).get("role", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 校验 Token 是否有效
     */
    public static boolean validate(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 校验 Token 是否过期
     */
    public static boolean isExpired(String token) {
        try {
            parseClaims(token);
            return false;
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 获取 Token 剩余有效时间（毫秒）
     */
    public static long getRemainingTime(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getExpiration().getTime() - System.currentTimeMillis();
        } catch (Exception e) {
            return 0;
        }
    }
}
