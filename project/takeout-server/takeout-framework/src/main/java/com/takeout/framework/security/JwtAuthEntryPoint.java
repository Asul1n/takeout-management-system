package com.takeout.framework.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.takeout.common.constant.CommonConstant;
import com.takeout.common.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 未认证处理器 — 返回 JSON 错误信息
 */
@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        Result<Void> result = Result.fail(CommonConstant.TOKEN_INVALID, "请先登录");
        response.getWriter().write(new ObjectMapper().writeValueAsString(result));
    }
}
