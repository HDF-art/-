package com.agri.config;

import com.agri.constant.RoleEnum;
import com.agri.mapper.UserMapper;
import com.agri.model.User;
import com.agri.model.UserPrincipal;
import com.agri.utils.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserMapper userMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;
        Long userId = null;
        String role = null;
        Long farmId = null;

        try {
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                jwt = authorizationHeader.substring(7);
                Map<String, Object> tokenInfo = jwtUtils.validateToken(jwt);
                if (Boolean.TRUE.equals(tokenInfo.get("valid"))) {
                    username = (String) tokenInfo.get("username");
                    Object userIdObj = tokenInfo.get("userId");
                    if (userIdObj instanceof Integer) {
                        userId = ((Integer) userIdObj).longValue();
                    } else if (userIdObj instanceof Long) {
                        userId = (Long) userIdObj;
                    } else if (userIdObj != null) {
                        userId = Long.parseLong(userIdObj.toString());
                    }
                    role = (String) tokenInfo.get("role");
                    
                    if (userId != null) {
                        User user = userMapper.selectById(userId);
                        if (user != null) {
                            farmId = user.getFarmId();
                        }
                    }
                } else if (tokenInfo.containsKey("message")) {
                    throw new JwtException((String) tokenInfo.get("message"));
                }
            }

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserPrincipal principal = new UserPrincipal(userId, username, role, farmId);
                
                String roleName;
                if (role != null && !role.isEmpty()) {
                    if (role.matches("\\d+")) {
                        int roleCode = Integer.parseInt(role);
                        roleName = RoleEnum.fromCode(roleCode).getValue().toUpperCase();
                    } else {
                        roleName = role.toUpperCase();
                    }
                } else {
                    roleName = "USER";
                }
                
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + roleName);
                
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        principal, null, Collections.singletonList(authority));
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            handleAuthenticationException(response, e);
        }
    }

    private void handleAuthenticationException(HttpServletResponse response, Exception e) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> body = new HashMap<>();
        body.put("code", 401);
        body.put("message", "认证失败: " + e.getMessage());
        body.put("path", "");

        new ObjectMapper().writeValue(response.getOutputStream(), body);
    }
}
