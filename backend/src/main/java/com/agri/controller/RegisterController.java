package com.agri.controller;

import com.agri.model.User;
import com.agri.service.UserService;
import com.agri.service.EmailCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.*;

import com.agri.utils.ResponseUtils;

@RestController
@RequestMapping("")
public class RegisterController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailCodeService emailCodeService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * 发送注册验证码
     */
    @PostMapping("/register/send-code")
    public ResponseUtils.ApiResponse<String> sendRegisterCode(@RequestBody Map<String, String> params) {
        String email = params.get("email");

        try {
            // 检查邮箱是否已注册或正在审核
            User existingUser = userService.findByEmail(email);
            if (existingUser != null) {
                if (existingUser.getAuditStatus() != null && existingUser.getAuditStatus() == 0) {
                    return ResponseUtils.error(400, "该邮箱的注册申请正在审核中");
                }
                return ResponseUtils.error(400, "该邮箱已被注册");
            }

            // 发送验证码
            boolean sent = emailCodeService.sendCode(email);
            if (sent) {
                return ResponseUtils.success("验证码已发送到邮箱");
            } else {
                return ResponseUtils.error(500, "发送失败，请稍后重试");
            }
        } catch (Exception e) {
            return ResponseUtils.error(500, "发送失败：" + e.getMessage());
        }
    }

    /**
     * 注册用户（普通用户）
     */
    @PostMapping("/register/user")
    public ResponseUtils.ApiResponse<Long> registerUser(@RequestBody Map<String, Object> params) {
        try {
            String email = (String) params.get("email");
            String code = (String) params.get("code");
            String username = (String) params.get("username");
            String password = (String) params.get("password");

            // 验证邮箱验证码
            if (!emailCodeService.verifyCode(email, code)) {
                return ResponseUtils.error(400, "验证码错误或已过期");
            }

            // 检查邮箱是否已注册
            User existingUser = userService.findByEmail(email);
            if (existingUser != null) {
                return ResponseUtils.error(400, "该邮箱已被注册");
            }

            // 检查用户名是否已存在
            if (userService.findByUsername(username) != null) {
                return ResponseUtils.error(400, "用户名已存在");
            }

            // 创建用户（普通用户 role=3）
            User user = new User();
            user.setUsername(username);
            user.setPassword(password); // 会在 service 中加密
            user.setEmail(email);
            user.setRole(3); // 普通用户
            user.setStatus(1); // 直接启用

            userService.createUser(user);
            
            // 重新查询获取完整信息
            User createdUser = userService.findByUsername(username);

            return ResponseUtils.success(createdUser.getId());

        } catch (Exception e) {
            return ResponseUtils.error(500, "注册失败：" + e.getMessage());
        }
    }

    /**
     * 注册二级管理员（待审核）
     */
    @PostMapping("/register/admin2")
    public ResponseUtils.ApiResponse<Long> registerAdmin2(@RequestBody Map<String, Object> params) {
        try {
            String email = (String) params.get("email");
            String code = (String) params.get("code");
            String username = (String) params.get("username");
            String password = (String) params.get("password");
            String organization = (String) params.get("organization");

            // 验证邮箱验证码
            if (!emailCodeService.verifyCode(email, code)) {
                return ResponseUtils.error(400, "验证码错误或已过期");
            }

            // 检查邮箱是否已注册或正在审核
            User existingUser = userService.findByEmail(email);
            if (existingUser != null) {
                if (existingUser.getAuditStatus() != null && existingUser.getAuditStatus() == 0) {
                    return ResponseUtils.error(400, "该邮箱的注册申请正在审核中");
                }
                return ResponseUtils.error(400, "该邮箱已被注册");
            }

            // 检查用户名是否已存在
            if (userService.findByUsername(username) != null) {
                return ResponseUtils.error(400, "用户名已存在");
            }

            // 创建待审核的二级管理员
            User user = new User();
            user.setUsername(username);
            user.setPassword(password);
            user.setEmail(email);
            user.setName(username); // 默认使用用户名作为昵称

            Long userId = userService.registerAdmin2Pending(user, organization);

            // TODO: 发送审核通知邮件给一级管理员
            // TODO: 发送申请提交邮件给申请人

            return ResponseUtils.success(userId);

        } catch (Exception e) {
            return ResponseUtils.error(500, "注册失败：" + e.getMessage());
        }
    }
}
