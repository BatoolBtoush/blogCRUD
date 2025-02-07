package com.batool.crud.controller;

import com.batool.crud.entity.User;
import com.batool.crud.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Map;


@RestController
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/register-admin")
    public ResponseEntity<?> registerAdmin(@Valid @RequestBody User user) {
        return authService.registerAdmin(user);
    }

    @PostMapping("/register-content-writer")
    public ResponseEntity<?> registerContentWriter(@Valid @RequestBody User user) {
        return authService.registerContentWriter(user);
    }

    @PostMapping("/register-normal-user")
    public ResponseEntity<?> registerNormalUser(@Valid @RequestBody User user) {
        return authService.registerNormalUser(user);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody User user) {
        return authService.login(user);
    }


    @PostMapping("/generate-access-token-from-refresh-token")
    public ResponseEntity<Map<String, String>> accessTokenFromRefreshToken(@RequestBody Map<String, String> refreshTokenRequest) {
        String refreshToken = refreshTokenRequest.get("refresh_token");
        return authService.accessTokenFromRefreshToken(refreshToken);
    }



}
