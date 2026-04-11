package com.agri.controller;

import com.agri.dto.LoginDTO;
import com.agri.dto.LoginResponseDTO;
import com.agri.dto.PhoneLoginDTO;
import com.agri.dto.ResetPasswordDTO;
import com.agri.dto.SendCodeDTO;
import com.agri.dto.UserInfoDTO;
import com.agri.model.User;
import com.agri.service.EmailCodeService;
import com.agri.service.SmsService;
import com.agri.service.UserService;
import com.agri.utils.JwtUtils;
import com.agri.utils.ResponseUtils;
import com.agri.utils.FileUploadValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;

@RestController
@RequestMapping("/users")
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    private static final long MAX_AVATAR_SIZE = 2 * 1024 * 1024;
    private static final java.util.Set<String> ALLOWED_AVATAR_TYPES = java.util.Set.of("image/jpeg", "image/png", "image/gif");

    @Autowired
    private UserService userService;
    
    @Autowired
    private SmsService smsService;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    @Autowired
    private EmailCodeService emailCodeService;
    
    @Autowired
    private FileUploadValidator fileUploadValidator;
    
    @Value("${upload.image-path:/home/ubuntu/农业大数据联合建模平台/upload/images/}")
    private String uploadDirBase;

    @GetMapping("/hello")
    public String hello() {
        logger.info("UserController的hello接口被调用");
        return "UserController的hello接口正常工作";
    }
    
    @PostMapping("/login")
    public ResponseUtils.ApiResponse<LoginResponseDTO> login(@RequestBody LoginDTO loginDTO) {
        logger.info("登录接口被调用，用户名：{}", loginDTO.getUsername());
        try {
            LoginResponseDTO result = userService.login(loginDTO);
            return ResponseUtils.success(result);
        } catch (Exception e) {
            return ResponseUtils.error(400, "登录失败");
        }
    }

    @GetMapping("/info")
    public ResponseUtils.ApiResponse<UserInfoDTO> getCurrentUserInfo(HttpServletRequest request) {
        try {
            Long userId = getAuthenticatedUserId(request);
            if (userId == null) {
                return ResponseUtils.error(401, "未授权访问");
            }
            
            UserInfoDTO result = userService.getUserInfo(userId);
            if (result == null) {
                return ResponseUtils.error(404, "用户不存在");
            }
            return ResponseUtils.success(result);
        } catch (SecurityException e) {
            logger.warn("认证失败: {}", e.getMessage());
            return ResponseUtils.error(401, "认证失败，请重新登录");
        } catch (Exception e) {
            logger.error("获取用户信息失败", e);
            return ResponseUtils.error(500, "获取用户信息失败");
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN1') or hasRole('ADMIN2') or #id == authentication.principal.id")
    public ResponseUtils.ApiResponse<UserInfoDTO> getUserInfo(@PathVariable("id") Long userId) {
        try {
            UserInfoDTO result = userService.getUserInfo(userId);
            if (result == null) {
                return ResponseUtils.error(404, "用户不存在");
            }
            return ResponseUtils.success(result);
        } catch (Exception e) {
            logger.error("获取用户信息失败", e);
            return ResponseUtils.error(500, "获取用户信息失败");
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN1')")
    public ResponseUtils.ApiResponse<String> createUser(@RequestBody User user) {
        try {
            boolean result = userService.createUser(user);
            return result ? ResponseUtils.success("创建成功") : ResponseUtils.error(500, "创建失败");
        } catch (Exception e) {
            logger.error("创建用户失败", e);
            return ResponseUtils.error(500, "创建用户失败");
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN1')")
    public ResponseUtils.ApiResponse<String> updateUser(@PathVariable("id") Long userId, @RequestBody Map<String, Object> userData) {
        try {
            User user = userService.getById(userId);
            if (user == null) {
                return ResponseUtils.error(404, "用户不存在");
            }
            
            if (userData.containsKey("email")) {
                user.setEmail((String) userData.get("email"));
            }
            if (userData.containsKey("phone")) {
                user.setPhone((String) userData.get("phone"));
            }
            if (userData.containsKey("organization")) {
                user.setOrganization((String) userData.get("organization"));
            }
            if (userData.containsKey("status")) {
                user.setStatus((Integer) userData.get("status"));
            }
            
            user.setUpdatedAt(java.time.LocalDateTime.now());
            userService.updateById(user);
            return ResponseUtils.success("更新成功");
        } catch (Exception e) {
            logger.error("更新用户信息失败", e);
            return ResponseUtils.error(500, "更新失败");
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN1')")
    public ResponseUtils.ApiResponse<String> deleteUser(@PathVariable("id") Long userId) {
        try {
            User user = userService.getById(userId);
            if (user == null) {
                return ResponseUtils.error(404, "用户不存在");
            }
            
            if (user.getRole() != null && user.getRole() == 1) {
                return ResponseUtils.error(403, "无法删除一级管理员账户");
            }
            
            userService.removeById(userId);
            return ResponseUtils.success("删除成功");
        } catch (Exception e) {
            logger.error("删除用户失败", e);
            return ResponseUtils.error(500, "删除失败");
        }
    }
    
    @PutMapping("/profile")
    public ResponseUtils.ApiResponse<String> updateCurrentUserInfo(HttpServletRequest request, @RequestBody Map<String, Object> userData) {
        try {
            Long userId = getAuthenticatedUserId(request);
            if (userId == null) {
                return ResponseUtils.error(401, "未授权访问");
            }
            
            User currentUser = userService.getById(userId);
            if (currentUser == null) {
                return ResponseUtils.error(404, "用户不存在");
            }
            
            if (userData.containsKey("email")) {
                currentUser.setEmail((String) userData.get("email"));
            }
            if (userData.containsKey("phone")) {
                currentUser.setPhone((String) userData.get("phone"));
            }
            if (userData.containsKey("name")) {
                currentUser.setName((String) userData.get("name"));
            }
            if (userData.containsKey("organization")) {
                currentUser.setOrganization((String) userData.get("organization"));
            }
            
            currentUser.setUpdatedAt(java.time.LocalDateTime.now());
            boolean result = userService.updateById(currentUser);
            
            return result ? ResponseUtils.success("用户信息更新成功") : ResponseUtils.error(500, "用户信息更新失败");
        } catch (SecurityException e) {
            logger.warn("认证失败: {}", e.getMessage());
            return ResponseUtils.error(401, "认证失败，请重新登录");
        } catch (Exception e) {
            logger.error("更新用户信息失败: {}", e.getMessage());
            return ResponseUtils.error(500, "更新用户信息失败");
        }
    }
    
    @PutMapping("/password")
    public ResponseUtils.ApiResponse<String> changePassword(HttpServletRequest request, @RequestBody Map<String, String> passwordDTO) {
        try {
            Long userId = getAuthenticatedUserId(request);
            if (userId == null) {
                return ResponseUtils.error(401, "未授权访问");
            }
            
            User currentUser = userService.getById(userId);
            if (currentUser == null) {
                return ResponseUtils.error(404, "用户不存在");
            }
            
            String currentPassword = passwordDTO.get("currentPassword");
            String newPassword = passwordDTO.get("newPassword");
            
            if (currentPassword == null || newPassword == null) {
                return ResponseUtils.error(400, "密码信息不完整");
            }
            
            if (newPassword.length() < 6 || newPassword.length() > 32) {
                return ResponseUtils.error(400, "新密码长度必须在6-32位之间");
            }
            
            if (!passwordEncoder.matches(currentPassword, currentUser.getPassword())) {
                return ResponseUtils.error(400, "当前密码错误");
            }
            
            currentUser.setPassword(passwordEncoder.encode(newPassword));
            currentUser.setUpdatedAt(java.time.LocalDateTime.now());
            boolean result = userService.updateUser(currentUser);
            
            return result ? ResponseUtils.success("密码修改成功") : ResponseUtils.error(500, "密码修改失败");
        } catch (SecurityException e) {
            logger.warn("认证失败: {}", e.getMessage());
            return ResponseUtils.error(401, "认证失败，请重新登录");
        } catch (Exception e) {
            logger.error("修改密码失败: {}", e.getMessage());
            return ResponseUtils.error(500, "修改密码失败");
        }
    }
    
    @PostMapping("/upload-avatar")
    public ResponseUtils.ApiResponse<Map<String, String>> uploadAvatar(HttpServletRequest request, @RequestParam("file") MultipartFile file) {
        logger.info("【头像上传】接收到头像上传请求");
        try {
            Long userId = getAuthenticatedUserId(request);
            if (userId == null) {
                return ResponseUtils.error(401, "未授权访问");
            }
            
            if (file.isEmpty()) {
                logger.warn("【头像上传】上传文件为空");
                return ResponseUtils.error(400, "上传文件不能为空");
            }
            
            if (file.getSize() > MAX_AVATAR_SIZE) {
                logger.warn("【头像上传】文件大小超出限制: {} bytes", file.getSize());
                return ResponseUtils.error(400, "图片大小不能超过2MB");
            }
            
            String detectedType = detectImageType(file.getInputStream());
            if (detectedType == null) {
                logger.warn("【头像上传】无法识别的文件类型");
                return ResponseUtils.error(400, "不支持的文件类型，只支持JPG/PNG/GIF格式");
            }
            
            String fileExtension;
            switch (detectedType) {
                case "jpeg":
                    fileExtension = ".jpg";
                    break;
                case "png":
                    fileExtension = ".png";
                    break;
                case "gif":
                    fileExtension = ".gif";
                    break;
                default:
                    return ResponseUtils.error(400, "不支持的文件类型");
            }
            
            String newFileName = UUID.randomUUID().toString() + fileExtension;
            
            String uploadDir = "/home/ubuntu/农业大数据联合建模平台/upload/avatars";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
                logger.info("【头像上传】创建上传目录: {}", uploadDir);
            }
            
            File destFile = new File(dir, newFileName);
            file.transferTo(destFile);
            logger.info("【头像上传】文件已保存: {}", destFile.getAbsolutePath());
            
            String fileUrl = "/upload/avatars/" + newFileName;
            
            User user = userService.getById(userId);
            if (user != null) {
                user.setAvatar(fileUrl);
                user.setUpdatedAt(java.time.LocalDateTime.now());
                userService.updateById(user);
                logger.info("【头像上传】用户头像已更新, userId={}, avatar={}", userId, fileUrl);
            }
            
            Map<String, String> result = new HashMap<>();
            result.put("url", fileUrl);
            result.put("fileName", newFileName);
            
            logger.info("【头像上传】头像上传成功，返回URL: {}", fileUrl);
            return ResponseUtils.success(result);
        } catch (SecurityException e) {
            logger.warn("认证失败: {}", e.getMessage());
            return ResponseUtils.error(401, "认证失败，请重新登录");
        } catch (IOException e) {
            logger.error("【头像上传】头像上传IO异常: ", e);
            return ResponseUtils.error(500, "头像上传失败");
        } catch (Exception e) {
            logger.error("【头像上传】头像上传其他异常: ", e);
            return ResponseUtils.error(500, "头像上传失败");
        }
    }
    
    @PostMapping("/send-code")
    public ResponseUtils.ApiResponse<String> sendCode(@RequestBody SendCodeDTO sendCodeDTO) {
        logger.info("发送验证码接口被调用，手机号：{}", sendCodeDTO.getPhone());
        try {
            boolean result = smsService.sendCode(sendCodeDTO.getPhone(), null);
            return result ? ResponseUtils.success("验证码发送成功") : ResponseUtils.error(500, "验证码发送失败");
        } catch (Exception e) {
            return ResponseUtils.error(400, "验证码发送失败");
        }
    }
    
    @PostMapping("/phone-login")
    public ResponseUtils.ApiResponse<LoginResponseDTO> phoneLogin(@RequestBody PhoneLoginDTO phoneLoginDTO) {
        logger.info("手机验证码登录接口被调用，手机号：{}", phoneLoginDTO.getPhone());
        try {
            LoginResponseDTO result = userService.phoneLogin(phoneLoginDTO.getPhone(), phoneLoginDTO.getCode());
            return ResponseUtils.success(result);
        } catch (Exception e) {
            return ResponseUtils.error(400, "登录失败");
        }
    }
    
    @PostMapping("/reset-password")
    public ResponseUtils.ApiResponse<String> resetPassword(@RequestBody ResetPasswordDTO resetPasswordDTO) {
        logger.info("找回密码接口被调用，手机号：{}", resetPasswordDTO.getPhone());
        try {
            boolean result = userService.resetPassword(resetPasswordDTO.getPhone(), resetPasswordDTO.getCode(), resetPasswordDTO.getNewPassword());
            return result ? ResponseUtils.success("密码重置成功") : ResponseUtils.error(500, "密码重置失败");
        } catch (Exception e) {
            return ResponseUtils.error(400, "密码重置失败");
        }
    }
    
    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN1')")
    public ResponseUtils.ApiResponse<String> updateUserRole(@PathVariable("id") Long userId, @RequestBody Map<String, String> roleData) {
        try {
            String newRole = roleData.get("role");
            if (newRole == null || (!newRole.equals("admin1") && !newRole.equals("admin2") && !newRole.equals("user"))) {
                return ResponseUtils.error(400, "无效的角色类型");
            }
            
            User user = userService.getById(userId);
            if (user == null) {
                return ResponseUtils.error(404, "用户不存在");
            }
            
            int roleValue;
            switch (newRole) {
                case "admin1":
                    roleValue = 1;
                    break;
                case "admin2":
                    roleValue = 2;
                    break;
                default:
                    roleValue = 3;
            }
            
            user.setRole(roleValue);
            user.setUpdatedAt(java.time.LocalDateTime.now());
            userService.updateById(user);
            
            logger.info("用户角色已更新: userId={}, newRole={}", userId, newRole);
            return ResponseUtils.success("角色更新成功");
        } catch (Exception e) {
            logger.error("更新用户角色失败", e);
            return ResponseUtils.error(500, "更新角色失败");
        }
    }
    
    private Long getAuthenticatedUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return null;
        }
        
        token = token.substring(7);
        try {
            return jwtUtils.getUserIdFromToken(token);
        } catch (Exception e) {
            logger.warn("Token解析失败: {}", e.getMessage());
            return null;
        }
    }
    
    private String detectImageType(InputStream inputStream) throws IOException {
        byte[] header = new byte[8];
        int bytesRead = inputStream.read(header);
        if (bytesRead < 8) {
            return null;
        }
        
        if (header[0] == (byte) 0xFF && header[1] == (byte) 0xD8 && header[2] == (byte) 0xFF) {
            return "jpeg";
        }
        
        if (header[0] == (byte) 0x89 && header[1] == (byte) 0x50 && 
            header[2] == (byte) 0x4E && header[3] == (byte) 0x47) {
            return "png";
        }
        
        if (header[0] == (byte) 0x47 && header[1] == (byte) 0x49 && 
            header[2] == (byte) 0x46 && header[3] == (byte) 0x38) {
            return "gif";
        }
        
        return null;
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN1') or hasRole('ADMIN2')")
    public ResponseUtils.ApiResponse<Map<String, Object>> getUserList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String role) {
        try {
            List<User> allUsers = userService.list();
            
            List<User> filteredUsers = allUsers.stream()
                .filter(u -> {
                    if (u.getId() == 1 || "admin".equals(u.getUsername())) {
                        return false;
                    }
                    boolean match = true;
                    if (username != null && !username.isEmpty()) {
                        match = u.getUsername() != null && u.getUsername().contains(username);
                    }
                    if (role != null && !role.isEmpty()) {
                        int roleInt = "admin1".equals(role) ? 1 : ("admin2".equals(role) ? 2 : 3);
                        match = match && u.getRole() != null && u.getRole() == roleInt;
                    }
                    return match;
                })
                .collect(java.util.stream.Collectors.toList());
            
            int total = filteredUsers.size();
            int start = (page - 1) * size;
            int end = Math.min(start + size, total);
            
            List<Map<String, Object>> userList = new java.util.ArrayList<>();
            for (int i = start; i < end && i < filteredUsers.size(); i++) {
                User u = filteredUsers.get(i);
                Map<String, Object> userMap = new java.util.HashMap<>();
                userMap.put("id", u.getId());
                userMap.put("username", u.getUsername());
                userMap.put("email", u.getEmail());
                userMap.put("phone", u.getPhone());
                userMap.put("organization", u.getOrganization());
                userMap.put("role", u.getRole());
                userMap.put("roleName", getRoleName(u.getRole()));
                userMap.put("status", u.getStatus());
                userMap.put("createdAt", u.getCreatedAt());
                userList.add(userMap);
            }
            
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("list", userList);
            result.put("total", total);
            result.put("page", page);
            result.put("size", size);
            
            return ResponseUtils.success(result);
        } catch (Exception e) {
            logger.error("获取用户列表失败", e);
            return ResponseUtils.error(500, "获取用户列表失败");
        }
    }
    
    private String getRoleName(Integer role) {
        if (role == null) return "未知";
        switch (role) {
            case 1: return "一级管理员";
            case 2: return "二级管理员";
            case 3: return "普通用户";
            default: return "未知";
        }
    }
}
