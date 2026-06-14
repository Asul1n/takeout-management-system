package com.takeout.framework.security;

import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

/**
 * 用户详情 — SecurityContext 中的认证主体
 */
@Getter
public class UserDetailsImpl {

    private final Long userId;
    private final String role;

    public UserDetailsImpl(Long userId, String role) {
        this.userId = userId;
        this.role = role;
    }

    public boolean hasRole(String role) {
        return this.role.equals(role);
    }

    /**
     * 获取 GrantedAuthority 列表 — Spring Security 要求 ROLE_ 前缀
     */
    public List<SimpleGrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }
}
