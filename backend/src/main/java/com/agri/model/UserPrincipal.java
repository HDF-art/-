package com.agri.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Spring Security 认证主体对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserPrincipal implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private String role;
    private Long farmId;
}
