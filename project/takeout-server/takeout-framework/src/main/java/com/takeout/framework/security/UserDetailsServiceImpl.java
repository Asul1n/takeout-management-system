package com.takeout.framework.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 用户详情加载服务
 * <p>
 * JWT 认证模式下，用户信息直接从 Token 中提取，
 * 此 Service 作为 Spring Security 体系中的占位实现。
 */
@Slf4j
@Service
public class UserDetailsServiceImpl {
    // 用户详情通过 JwtAuthFilter 从 Token 中直接解析，无需再从数据库加载
}
