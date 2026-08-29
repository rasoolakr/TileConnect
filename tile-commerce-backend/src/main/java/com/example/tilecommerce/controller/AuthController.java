package com.example.tilecommerce.controller;

import com.example.tilecommerce.dto.AuthDtos.*;
import com.example.tilecommerce.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService auth;

    /** Normal customer registration. */
    @PostMapping("/register")
    public LoginResponse register(@Valid @RequestBody RegisterRequest r) {
        return auth.register(r);
    }

    /** Shop-owner registration: creates User + Shop + default Address atomically. */
    @PostMapping("/shop-register")
    public LoginResponse registerShop(@Valid @RequestBody ShopRegisterRequest r) {
        return auth.registerShop(r);
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest r) {
        return auth.login(r);
    }
}
