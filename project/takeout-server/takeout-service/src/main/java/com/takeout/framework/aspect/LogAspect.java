package com.takeout.framework.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.takeout.framework.security.UserDetailsImpl;
import com.takeout.module.user.entity.OperationLog;
import com.takeout.module.user.mapper.OperationLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;
import java.util.Set;

/**
 * 操作日志 AOP 切面 — 控制台输出 + 写入 operation_log 表
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    private final ObjectMapper objectMapper;
    private final OperationLogMapper operationLogMapper;

    /** 需要写入数据库的关键操作方法 */
    private static final Set<String> AUDIT_METHODS = Set.of(
        "login", "register", "createUser", "deleteUser",
        "toggleUserStatus", "resetPassword",
        "audit", "apply",
        "submit", "accept", "prepareComplete", "cancel", "confirm",
        "acceptDelivery", "pickup", "deliver",
        "add", "delete", "toggleStatus", "updateStock"
    );

    @Pointcut("execution(public * com.takeout.module.*.controller.*.*(..))")
    public void controllerLog() {}

    @Around("controllerLog()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();

        UserDetailsImpl user = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getPrincipal)
                .filter(p -> p instanceof UserDetailsImpl)
                .map(p -> (UserDetailsImpl) p)
                .orElse(null);

        String userId = user != null ? String.valueOf(user.getUserId()) : "anonymous";
        String userRole = user != null ? user.getRole() : null;
        String ip = Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(attrs -> attrs instanceof ServletRequestAttributes)
                .map(attrs -> ((ServletRequestAttributes) attrs).getRequest())
                .map(HttpServletRequest::getRemoteAddr).orElse("unknown");

        log.info("[操作日志] 用户:{} | IP:{} | {}.{}", userId, ip, className, methodName);

        Object result = joinPoint.proceed();
        long elapsed = System.currentTimeMillis() - start;

        // 写入 operation_log 表（login 等匿名操作也需要记录）
        if (AUDIT_METHODS.contains(methodName)) {
            try {
                OperationLog opLog = new OperationLog();
                opLog.setUserId(user != null ? user.getUserId() : null);
                opLog.setUserRole(user != null ? user.getRole() : null);
                opLog.setOperation(mapOp(methodName));
                opLog.setTargetType(mapTarget(className));
                opLog.setDetail(className.substring(className.lastIndexOf('.') + 1) + "." + methodName + " (" + elapsed + "ms)");
                opLog.setIp(ip);
                operationLogMapper.insert(opLog);
            } catch (Exception e) {
                log.warn("操作日志DB写入失败: {}", e.getMessage());
            }
        }

        return result;
    }

    private String mapOp(String m) {
        return switch (m) {
            case "login" -> "LOGIN";
            case "register", "createUser" -> "REGISTER";
            case "deleteUser" -> "USER_DELETE";
            case "toggleUserStatus" -> "USER_STATUS_CHANGE";
            case "resetPassword" -> "PASSWORD_RESET";
            case "audit" -> "MERCHANT_AUDIT";
            case "apply" -> "MERCHANT_APPLY";
            case "submit" -> "SUBMIT_ORDER";
            case "accept", "acceptDelivery" -> "ACCEPT_ORDER";
            case "prepareComplete" -> "ORDER_PREPARED";
            case "cancel" -> "CANCEL_ORDER";
            case "confirm" -> "CONFIRM_RECEIPT";
            case "pickup", "deliver" -> "UPDATE_DELIVERY_STATUS";
            default -> m.toUpperCase();
        };
    }

    private String mapTarget(String c) {
        if (c.contains("Auth")) return "USER";
        if (c.contains("User") || c.contains("Admin")) return "USER";
        if (c.contains("Merchant")) return "MERCHANT";
        if (c.contains("Dish") || c.contains("Cart") || c.contains("File") || c.contains("Category")) return "DISH";
        if (c.contains("Order")) return "ORDER";
        if (c.contains("Delivery")) return "DELIVERY";
        return null;
    }
}
