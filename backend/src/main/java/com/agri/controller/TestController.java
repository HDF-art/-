package com.agri.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试控制器
 */
@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
}
