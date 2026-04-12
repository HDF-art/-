package com.agri.service.impl;

import com.agri.dto.LoginDTO;
import com.agri.dto.LoginResponseDTO;
import com.agri.dto.UserInfoDTO;
import com.agri.mapper.FarmMapper;
import com.agri.mapper.UserMapper;
import com.agri.model.User;
import com.agri.service.SmsService;
import com.agri.service.UserService;
import com.agri.utils.JwtUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FarmMapper farmMapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private SmsService smsService;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    @Override
    public LoginResponseDTO login(LoginDTO loginDTO) {
        // 根据用户名查询用户
        User user = userMapper.findByUsername(loginDTO.getUsername());
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 校验密码
        boolean passwordMatches = passwordEncoder.matches(loginDTO.getPassword(), user.getPassword());
        System.out.println("Password matches: " + passwordMatches);
        if (!passwordMatches) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 校验用户状态
        if (user.getStatus() != 1) {
            throw new RuntimeException("用户已被禁用");
        }

        // 生成token
        String token = jwtUtils.generateToken(user.getUsername(), user.getId(), user.getRole().toString());

        // 构建响应
        LoginResponseDTO responseDTO = new LoginResponseDTO();
        responseDTO.setToken(token);
        responseDTO.setExpiresIn(jwtExpiration);
        responseDTO.setUserInfo(getUserInfo(user.getId()));

        return responseDTO;
    }

    @Override
    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    @Override
    public UserInfoDTO getUserInfo(Long userId) {
        User user = getById(userId);
        if (user == null) {
            return null;
        }

        UserInfoDTO userInfoDTO = new UserInfoDTO();
        BeanUtils.copyProperties(user, userInfoDTO);

        // 设置角色名称
        switch (user.getRole()) {
            case 1: userInfoDTO.setRoleName("一级管理员"); break;
            case 2: userInfoDTO.setRoleName("二级管理员"); break;
            case 3: userInfoDTO.setRoleName("普通用户"); break;
        }

        // 设置农场名称
        if (user.getFarmId() != null) {
            com.agri.model.Farm farm = farmMapper.selectById(user.getFarmId());
            if (farm != null) {
                userInfoDTO.setFarmName(farm.getName());
            }
        }

        return userInfoDTO;
    }

    @Override
    public boolean createUser(User user) {
        // 检查用户名是否已存在
        if (userMapper.findByUsername(user.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus(1); // 默认启用
        user.setCreatedAt(java.time.LocalDateTime.now());
        user.setUpdatedAt(java.time.LocalDateTime.now());

        return save(user);
    }

    @Override
    public boolean updateUser(User user) {
        // 如果修改密码，需要重新加密
        if (StringUtils.hasText(user.getPassword()) && !user.getPassword().startsWith("$2a$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        user.setUpdatedAt(java.time.LocalDateTime.now());
        return updateById(user);
    }

    @Override
    public boolean deleteUser(Long userId) {
        return removeById(userId);
    }

    @Override
    public LoginResponseDTO phoneLogin(String phone, String code) {
        // 验证验证码
        if (!smsService.verifyCode(phone, code)) {
            throw new RuntimeException("验证码错误或已过期");
        }

        // 根据手机号查询用户
        User user = userMapper.findByPhone(phone);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 校验用户状态
        if (user.getStatus() != 1) {
            throw new RuntimeException("用户已被禁用");
        }

        // 生成token
        String token = jwtUtils.generateToken(user.getUsername(), user.getId(), user.getRole().toString());

        // 构建响应
        LoginResponseDTO responseDTO = new LoginResponseDTO();
        responseDTO.setToken(token);
        responseDTO.setExpiresIn(jwtExpiration);
        responseDTO.setUserInfo(getUserInfo(user.getId()));

        return responseDTO;
    }

    @Override
    public boolean resetPassword(String phone, String code, String newPassword) {
        // 验证验证码
        if (!smsService.verifyCode(phone, code)) {
            throw new RuntimeException("验证码错误或已过期");
        }

        // 根据手机号查询用户
        User user = userMapper.findByPhone(phone);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 加密新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(java.time.LocalDateTime.now());

        return updateById(user);
    }

    @Override
    public User findByPhone(String phone) {
        return userMapper.findByPhone(phone);
    }

    @Override
    public void registerUser(User user) {
        user.setCreatedAt(java.time.LocalDateTime.now());
        user.setStatus(1);
        save(user);
    }

    public User findByEmail(String email) {
        return userMapper.findByEmail(email);
    }

    @Override
    public Long registerAdmin2Pending(User user, String organization) {
        // 检查邮箱是否已注册（包括待审核）
        User existingUser = userMapper.findByEmail(user.getEmail());
        if (existingUser != null) {
            throw new RuntimeException("该邮箱已被注册或正在审核中");
        }
        
        // 创建待审核用户
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(2); // 二级管理员
        user.setStatus(0); // 待审核状态
        user.setAuditStatus(0); // 0:待审核
        user.setOrganization(organization);
        user.setCreatedAt(java.time.LocalDateTime.now());
        user.setUpdatedAt(java.time.LocalDateTime.now());
        
        save(user);
        return user.getId();
    }
    
    @Override
    public boolean auditAdmin2(Long userId, Integer auditStatus) {
        User user = getById(userId);
        if (user == null || user.getRole() != 2) {
            return false;
        }
        
        user.setAuditStatus(auditStatus);
        if (auditStatus == 1) {
            // 审核通过，启用用户
            user.setStatus(1);
        } else if (auditStatus == 2) {
            // 审核拒绝，禁用用户
            user.setStatus(0);
        }
        user.setUpdatedAt(java.time.LocalDateTime.now());
        
        return updateById(user);
    }
    
    @Override
    public java.util.List<User> getPendingAuditUsers() {
        return userMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User>()
                .eq("role", 2)
                .eq("audit_status", 0)
                .orderByDesc("created_at")
        );
    }
    
    @Override
    public java.util.List<User> getProcessedAuditUsers() {
        return userMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User>()
                .eq("role", 2)
                .in("audit_status", 1, 2)
                .orderByDesc("created_at")
        );
    }
}
