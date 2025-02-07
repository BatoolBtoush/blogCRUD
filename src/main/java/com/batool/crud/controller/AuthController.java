package com.batool.crud.controller;

import com.batool.crud.entity.LoginRequestDTO;
import com.batool.crud.entity.RegistrationRequestDTO;
import com.batool.crud.entity.User;
import com.batool.crud.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<?> registerAdmin(@Valid @RequestBody RegistrationRequestDTO registrationRequestDTO) {
        User user = authService.registerAdmin(registrationRequestDTO);
        return new ResponseEntity<>("Registration of "+ user.getFullName()+ " is successful", HttpStatus.OK);
    }

    @PostMapping("/register-content-writer")
    public ResponseEntity<?> registerContentWriter(@Valid @RequestBody RegistrationRequestDTO registrationRequestDTO) {
        User user = authService.registerContentWriter(registrationRequestDTO);
        return new ResponseEntity<>("Registration of "+ user.getFullName()+ " is successful", HttpStatus.OK);

    }

    @PostMapping("/register-normal-user")
    public ResponseEntity<?> registerNormalUser(@Valid @RequestBody RegistrationRequestDTO registrationRequestDTO) {
        User user = authService.registerNormalUser(registrationRequestDTO);
        return new ResponseEntity<>("Registration of "+ user.getFullName()+ " is successful", HttpStatus.OK);

    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequestDTO loginRequest) {
        return authService.login(loginRequest);
    }


    @PostMapping("/generate-access-token-from-refresh-token")
    public ResponseEntity<Map<String, String>> accessTokenFromRefreshToken(@RequestBody Map<String, String> refreshTokenRequest) {
        String refreshToken = refreshTokenRequest.get("refresh_token");
        return authService.accessTokenFromRefreshToken(refreshToken);
    }



}
