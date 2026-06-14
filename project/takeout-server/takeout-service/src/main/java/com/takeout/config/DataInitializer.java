package com.takeout.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.takeout.module.user.entity.User;
import com.takeout.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 系统初始化 — 创建默认管理员账号
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /** 默认管理员手机号 */
    private static final String ADMIN_PHONE = "13800000000";
    /** 默认管理员密码 */
    private static final String ADMIN_PASSWORD = "123456";

    @Override
    public void run(String... args) {
        // 检查管理员账号是否已存在
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getPhone, ADMIN_PHONE));
        if (count > 0) {
            log.info("管理员账号已存在，跳过初始化");
            return;
        }

        // 创建默认管理员
        User admin = new User();
        admin.setPhone(ADMIN_PHONE);
        admin.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
        admin.setRole("ADMIN");
        admin.setStatus("正常");
        userMapper.insert(admin);

        log.info("============================================");
        log.info("  默认管理员账号已创建");
        log.info("  手机号: {}", ADMIN_PHONE);
        log.info("  密码: {}", ADMIN_PASSWORD);
        log.info("============================================");
    }
}
