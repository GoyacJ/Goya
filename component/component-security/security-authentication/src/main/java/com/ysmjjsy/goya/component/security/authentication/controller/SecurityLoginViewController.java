package com.ysmjjsy.goya.component.security.authentication.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * <p>最小登录页入口</p>
 *
 * @author goya
 * @since 2026/2/10
 */
@Controller
public class SecurityLoginViewController {

    @GetMapping("/security/login")
    public String login() {
        return "security/login";
    }
}
