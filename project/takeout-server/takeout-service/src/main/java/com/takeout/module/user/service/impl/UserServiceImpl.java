package com.takeout.module.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.takeout.common.constant.CommonConstant;
import com.takeout.common.constant.UserRoleEnum;
import com.takeout.common.exception.BusinessException;
import com.takeout.common.util.JwtUtil;
import com.takeout.module.delivery.entity.Delivery;
import com.takeout.module.delivery.entity.Rider;
import com.takeout.module.delivery.mapper.DeliveryMapper;
import com.takeout.module.delivery.mapper.RiderMapper;
import com.takeout.module.dish.entity.CartItem;
import com.takeout.module.dish.entity.Category;
import com.takeout.module.dish.entity.Dish;
import com.takeout.module.dish.mapper.CartItemMapper;
import com.takeout.module.dish.mapper.CategoryMapper;
import com.takeout.module.dish.mapper.DishMapper;
import com.takeout.module.merchant.entity.Merchant;
import com.takeout.module.merchant.mapper.MerchantMapper;
import com.takeout.module.order.entity.Order;
import com.takeout.module.order.entity.OrderItem;
import com.takeout.module.order.mapper.OrderItemMapper;
import com.takeout.module.order.mapper.OrderMapper;
import com.takeout.module.user.entity.Address;
import com.takeout.module.user.entity.Notification;
import com.takeout.module.user.entity.OperationLog;
import com.takeout.module.user.mapper.AddressMapper;
import com.takeout.module.user.mapper.NotificationMapper;
import com.takeout.module.user.mapper.OperationLogMapper;
import com.takeout.module.user.dto.LoginDTO;
import com.takeout.module.user.dto.RegisterDTO;
import com.takeout.module.user.dto.UpdatePasswordDTO;
import com.takeout.module.user.dto.UserQueryDTO;
import com.takeout.module.user.entity.Customer;
import com.takeout.module.user.entity.User;
import com.takeout.module.user.mapper.CustomerMapper;
import com.takeout.module.user.mapper.UserMapper;
import com.takeout.module.user.service.UserService;
import com.takeout.module.user.vo.LoginVO;
import com.takeout.module.user.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final CustomerMapper customerMapper;
    private final MerchantMapper merchantMapper;
    private final RiderMapper riderMapper;
    private final AddressMapper addressMapper;
    private final DishMapper dishMapper;
    private final CategoryMapper categoryMapper;
    private final CartItemMapper cartItemMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final DeliveryMapper deliveryMapper;
    private final OperationLogMapper operationLogMapper;
    private final NotificationMapper notificationMapper;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void register(RegisterDTO dto) {
        if (!UserRoleEnum.isValid(dto.getRole())) {
            throw new BusinessException(CommonConstant.PARAM_ERROR, "无效的用户身份");
        }
        // 调用存储过程完成注册（原子操作）
        jdbcTemplate.update("CALL sp_register_user(?,?,?,?,?,?,?,?,@uid)",
                dto.getPhone(), passwordEncoder.encode(dto.getPassword()), dto.getRole(),
                dto.getName(), dto.getProvince(), dto.getCity(), dto.getDistrict(), dto.getAddressDetail());
        log.info("用户注册成功: phone={}, role={}", dto.getPhone(), dto.getRole());
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        // 查询用户
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, dto.getPhone()));
        if (user == null) {
            throw new BusinessException(CommonConstant.PASSWORD_ERROR, "手机号或密码错误");
        }

        // 校验密码
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(CommonConstant.PASSWORD_ERROR, "手机号或密码错误");
        }

        // 校验账号状态
        if ("禁用".equals(user.getStatus())) {
            throw new BusinessException(CommonConstant.ACCOUNT_DISABLED, "账号已被禁用");
        }

        // 生成 Token
        String token = JwtUtil.generate(user.getId(), user.getRole());

        // 获取用户名
        String name = getUserName(user);

        log.info("用户登录成功: phone={}, userId={}, role={}", dto.getPhone(), user.getId(), user.getRole());
        return LoginVO.builder()
                .token(token)
                .userId(user.getId())
                .role(user.getRole())
                .name(name)
                .build();
    }

    @Override
    public void logout(String token) {
        long remaining = JwtUtil.getRemainingTime(token);
        if (remaining > 0) {
            String key = CommonConstant.TOKEN_BLACKLIST_PREFIX + token;
            redisTemplate.opsForValue().set(key, "1", Duration.ofMillis(remaining));
        }
    }

    @Override
    public UserInfoVO getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(CommonConstant.NOT_FOUND, "用户不存在");
        }
        return UserInfoVO.builder()
                .id(user.getId())
                .phone(user.getPhone())
                .role(user.getRole())
                .status(user.getStatus())
                .name(getUserName(user))
                .createTime(user.getCreateTime())
                .build();
    }

    @Override
    public UserInfoVO updateProfile(Long userId, Map<String, String> fields) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException(CommonConstant.NOT_FOUND, "用户不存在");

        if (fields.containsKey("name")) {
            String newName = fields.get("name");
            switch (user.getRole()) {
                case "CUSTOMER" -> { Customer c = customerMapper.selectById(userId); c.setName(newName); customerMapper.updateById(c); }
                case "MERCHANT" -> { Merchant m = merchantMapper.selectById(userId); m.setName(newName); merchantMapper.updateById(m); }
                case "RIDER" -> { Rider r = riderMapper.selectById(userId); r.setName(newName); riderMapper.updateById(r); }
            }
        }
        if (fields.containsKey("phone")) {
            String newPhone = fields.get("phone");
            user.setPhone(newPhone);
            userMapper.updateById(user);
        }
        return getCurrentUser(userId);
    }

    @Override
    public void updatePassword(Long userId, UpdatePasswordDTO dto) {
        User user = userMapper.selectById(userId);
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException(CommonConstant.PASSWORD_ERROR, "原密码错误");
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userMapper.updateById(user);
    }

    @Override
    public Page<UserInfoVO> listUsers(UserQueryDTO dto) {
        // 使用 View v_user_summary 直接查询
        long offset = (long) (dto.getPage() - 1) * dto.getSize();
        String phone = (dto.getPhone() != null && !dto.getPhone().isEmpty()) ? dto.getPhone() : null;
        String role = (dto.getRole() != null && !dto.getRole().isEmpty()) ? dto.getRole() : null;
        String status = (dto.getStatus() != null && !dto.getStatus().isEmpty()) ? dto.getStatus() : null;

        List<Map<String, Object>> rows = userMapper.selectUserSummaries(phone, role, status, offset, (long) dto.getSize());
        long total = userMapper.countUserSummaries(phone, role, status);

        List<UserInfoVO> vos = rows.stream().map(row -> UserInfoVO.builder()
                .id(Long.valueOf(row.get("id").toString()))
                .phone((String) row.get("phone"))
                .role((String) row.get("role"))
                .status((String) row.get("status"))
                .name((String) row.get("display_name"))
                .createTime((java.time.LocalDateTime) row.get("create_time"))
                .build()).collect(java.util.stream.Collectors.toList());

        Page<UserInfoVO> page = new Page<>(dto.getPage(), dto.getSize(), total);
        page.setRecords(vos);
        return page;
    }

    @Override
    public void toggleUserStatus(Long userId, String status) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(CommonConstant.NOT_FOUND, "用户不存在");
        }
        user.setStatus(status);
        userMapper.updateById(user);
    }

    @Override
    public void resetPassword(Long userId, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(CommonConstant.NOT_FOUND, "用户不存在");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void createUser(RegisterDTO dto) {
        // 直接复用注册逻辑
        register(dto);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(CommonConstant.NOT_FOUND, "用户不存在");
        }
        if ("ADMIN".equals(user.getRole())) {
            throw new BusinessException("不能删除管理员账号");
        }

        // 调用存储过程安全删除（订单数据保留）
        jdbcTemplate.update("CALL sp_delete_user(?)", userId);
        log.info("管理员删除用户: userId={}, role={}", userId, user.getRole());
    }

    /**
     * 获取用户名（根据角色从不同子表获取）
     */
    private String getUserName(User user) {
        return switch (user.getRole()) {
            case "CUSTOMER" -> {
                Customer c = customerMapper.selectById(user.getId());
                yield c != null ? c.getName() : user.getPhone();
            }
            case "MERCHANT" -> {
                Merchant m = merchantMapper.selectById(user.getId());
                yield m != null ? m.getName() : user.getPhone();
            }
            case "RIDER" -> {
                Rider r = riderMapper.selectById(user.getId());
                yield r != null ? r.getName() : user.getPhone();
            }
            default -> "管理员";
        };
    }
}
