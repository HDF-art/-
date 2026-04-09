package com.agri.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 简单的测试控制器
 */
@RestController
@RequestMapping("/hello")
public class HelloController {
    
    private static final Logger logger = LoggerFactory.getLogger(HelloController.class);
    
    /**
     * 简单的hello接口
     * @return hello响应
     */
    @GetMapping("/world")
    public String helloWorld() {
        logger.info("Hello World接口被调用");
        return "Hello World!";
    }
}
