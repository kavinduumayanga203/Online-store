package com.store.controller;

import com.store.entity.User;
import com.store.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 1. Show Login Page
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    // 2. Show Registration Page
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    // 3. Process Registration
    @PostMapping("/saveUser")
    public String saveUser(@RequestParam String email,
            @RequestParam String password,
            @RequestParam String role,
            org.springframework.ui.Model model) {

        // 1. Check if email already exists
        if (userRepository.findByEmail(email).isPresent()) {
            model.addAttribute("error", "Email already registered. Please login or use a different email.");
            return "register";
        }

        // Create new user object
        User u = new User();
        u.setEmail(email);
        // CRITICAL: Encrypt password before saving!
        u.setPassword(passwordEncoder.encode(password));
        u.setRole(role);

        userRepository.save(u);

        return "redirect:/login?success";
    }
}
