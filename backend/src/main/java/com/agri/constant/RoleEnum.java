package com.agri.constant;

import lombok.Getter;

@Getter
public enum RoleEnum {
    ADMIN1(1, "admin1", "一级管理员"),
    ADMIN2(2, "admin2", "二级管理员"),
    USER(3, "user", "普通用户");

    private final int code;
    private final String value;
    private final String description;

    RoleEnum(int code, String value, String description) {
        this.code = code;
        this.value = value;
        this.description = description;
    }

    public static RoleEnum fromCode(Integer code) {
        if (code == null) return USER;
        for (RoleEnum role : RoleEnum.values()) {
            if (role.getCode() == code) {
                return role;
            }
        }
        return USER;
    }

    public static RoleEnum fromValue(String value) {
        if (value == null) return USER;
        for (RoleEnum role : RoleEnum.values()) {
            if (role.getValue().equalsIgnoreCase(value)) {
                return role;
            }
        }
        return USER;
    }
}
