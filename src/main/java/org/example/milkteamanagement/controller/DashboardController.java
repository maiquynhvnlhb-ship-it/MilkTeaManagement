package org.example.milkteamanagement.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        model.addAttribute("username", authentication != null ? authentication.getName() : "Khách");
        String role = "ANONYMOUS";
        if (authentication != null) {
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                role = authority.getAuthority();
                break;
            }
        }
        model.addAttribute("role", role);
        return "dashboard";
    }
}


