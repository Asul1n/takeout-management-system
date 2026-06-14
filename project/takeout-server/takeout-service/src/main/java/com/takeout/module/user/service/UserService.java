package com.takeout.module.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.takeout.module.user.dto.LoginDTO;
import com.takeout.module.user.dto.RegisterDTO;
import com.takeout.module.user.dto.UpdatePasswordDTO;
import com.takeout.module.user.dto.UserQueryDTO;
import com.takeout.module.user.vo.LoginVO;
import com.takeout.module.user.vo.UserInfoVO;

public interface UserService {

    /** 用户注册 */
    void register(RegisterDTO dto);

    /** 用户登录 */
    LoginVO login(LoginDTO dto);

    /** 用户登出 */
    void logout(String token);

    /** 获取当前用户信息 */
    UserInfoVO getCurrentUser(Long userId);

    /** 修改个人信息 */
    UserInfoVO updateProfile(Long userId, java.util.Map<String, String> fields);

    /** 修改密码 */
    void updatePassword(Long userId, UpdatePasswordDTO dto);

    /** 管理员：用户分页列表 */
    Page<UserInfoVO> listUsers(UserQueryDTO dto);

    /** 管理员：启用/禁用用户 */
    void toggleUserStatus(Long userId, String status);

    /** 管理员：重置用户密码 */
    void resetPassword(Long userId, String newPassword);

    /** 管理员：创建用户（含角色子表） */
    void createUser(RegisterDTO dto);

    /** 管理员：删除用户（含角色子表） */
    void deleteUser(Long userId);
}
