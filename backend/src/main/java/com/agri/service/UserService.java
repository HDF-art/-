package com.agri.service;
import com.agri.dto.LoginDTO;
import com.agri.dto.LoginResponseDTO;
import com.agri.dto.UserInfoDTO;
import com.agri.model.User;
import com.baomidou.mybatisplus.extension.service.IService;
/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {
    /**
     * 用户登录
     * @param loginDTO 登录信息
     * @return 登录响应
     */
    LoginResponseDTO login(LoginDTO loginDTO);
    /**
     * 根据用户名获取用户
     * @param username 用户名
     * @return 用户信息
     */
    User findByUsername(String username);
    /**
     * 获取用户信息
     * @param userId 用户 ID
     * @return 用户信息 DTO
     */
    UserInfoDTO getUserInfo(Long userId);
    /**
     * 创建用户
     * @param user 用户信息
     * @return 创建结果
     */
    boolean createUser(User user);
    /**
     * 更新用户
     * @param user 用户信息
     * @return 更新结果
     */
    boolean updateUser(User user);
    /**
     * 删除用户
     * @param userId 用户 ID
     * @return 删除结果
     */
    boolean deleteUser(Long userId);
    /**
     * 手机验证码登录
     * @param phone 手机号
     * @param code 验证码
     * @return 登录响应
     */
    LoginResponseDTO phoneLogin(String phone, String code);
    /**
     * 找回密码
     * @param phone 手机号
     * @param code 验证码
     * @param newPassword 新密码
     * @return 是否成功
     */
    boolean resetPassword(String phone, String code, String newPassword);
    /**
     * 根据手机号查找用户
     * @param phone 手机号
     * @return 用户
     */
    User findByPhone(String phone);
    
    /**
     * 根据邮箱查找用户
     * @param email 邮箱
     * @return 用户
     */
    User findByEmail(String email);
    /**
     * 注册用户
     * @param user 用户信息
     */
    void registerUser(User user);
    
    /**
     * 注册二级管理员（待审核）
     * @param user 用户信息
     * @param organization 单位名称
     * @return 用户 ID
     */
    Long registerAdmin2Pending(User user, String organization);
    
    /**
     * 审核二级管理员
     * @param userId 用户 ID
     * @param auditStatus 审核状态（1:通过，2:拒绝）
     * @return 是否成功
     */
    boolean auditAdmin2(Long userId, Integer auditStatus);
    
    /**
     * 获取待审核的二级管理员列表
     * @return 待审核用户列表
     */
    java.util.List<User> getPendingAuditUsers();
    
    /**
     * 获取已处理的审核用户列表
     * @return 已处理用户列表
     */
    java.util.List<User> getProcessedAuditUsers();
}
