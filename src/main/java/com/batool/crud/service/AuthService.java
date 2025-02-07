package com.batool.crud.service;


import com.batool.crud.entity.LoginRequestDTO;
import com.batool.crud.entity.RegistrationRequestDTO;
import com.batool.crud.entity.Role;
import com.batool.crud.entity.User;
import com.batool.crud.repo.UserRepo;
import com.batool.crud.security.JwtTokenUtil;
import com.batool.crud.util.Hasher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;



    public User registerAdmin(RegistrationRequestDTO registrationRequestDTO){
        if (userRepo.existsByEmail(registrationRequestDTO.getEmail().toLowerCase())) {
            throw new IllegalArgumentException("Email is already in use");
        }

        User user = new User();
        user.setFullName(registrationRequestDTO.getFullName());
        user.setEmail(registrationRequestDTO.getEmail().toLowerCase());
        String salt = Hasher.getSalt();
        user.setSalt(salt);
        user.setPassword(Hasher.hashPasswordWithSalt(registrationRequestDTO.getPassword(), salt));
        user.setDateOfBirth(registrationRequestDTO.getDateOfBirth());
        user.setRole(Role.ROLE_ADMIN);
        userRepo.save(user);

        return user;
   }

    public User registerContentWriter(RegistrationRequestDTO registrationRequestDTO){
        if (userRepo.existsByEmail(registrationRequestDTO.getEmail().toLowerCase())) {
            throw new IllegalArgumentException("Email is already in use");
        }

        User user = new User();
        user.setFullName(registrationRequestDTO.getFullName());
        user.setEmail(registrationRequestDTO.getEmail().toLowerCase());
        String salt = Hasher.getSalt();
        user.setSalt(salt);
        user.setPassword(Hasher.hashPasswordWithSalt(registrationRequestDTO.getPassword(), salt));
        user.setDateOfBirth(registrationRequestDTO.getDateOfBirth());
        user.setRole(Role.ROLE_CONTENT_WRITER);
        userRepo.save(user);

        return user;
    }


    public User registerNormalUser(RegistrationRequestDTO registrationRequestDTO){
        if (userRepo.existsByEmail(registrationRequestDTO.getEmail().toLowerCase())) {
            throw new IllegalArgumentException("Email is already in use");
        }

        User user = new User();
        user.setFullName(registrationRequestDTO.getFullName());
        user.setEmail(registrationRequestDTO.getEmail().toLowerCase());
        String salt = Hasher.getSalt();
        user.setSalt(salt);
        user.setPassword(Hasher.hashPasswordWithSalt(registrationRequestDTO.getPassword(), salt));
        user.setDateOfBirth(registrationRequestDTO.getDateOfBirth());
        user.setRole(Role.ROLE_NORMAL);
        userRepo.save(user);

        return user;
    }

public ResponseEntity<Map<String, String>> login(LoginRequestDTO loginRequest) {
    Map<String, String> response = new HashMap<>();
    User existentUser = userRepo.findByEmail(loginRequest.getEmail().toLowerCase());

    if (existentUser == null) {
        response.put("message", "User not found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    if (!checkUsersPassword(loginRequest)) {
        response.put("message", "Incorrect password");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    String accessToken = jwtTokenUtil.generateToken(existentUser.getEmail(), JwtTokenUtil.Tokens.ACCESS_TOKEN);
    String refreshToken = jwtTokenUtil.generateToken(existentUser.getEmail(), JwtTokenUtil.Tokens.REFRESH_TOKEN);

    response.put("access_token", accessToken);
    response.put("refresh_token", refreshToken);

    return ResponseEntity.ok(response);
}

    public User checkDBForUserByEmail(String email) {
        return userRepo.findByEmailIgnoreCase(email);
    }

    public boolean checkUsersPassword(LoginRequestDTO loginRequest) {
        User associatedUser = userRepo.findByEmail(loginRequest.getEmail().toLowerCase());
        if (associatedUser == null) {
            return false;
        }
        String salt = associatedUser.getSalt();
        String passwordHash = Hasher.hashPasswordWithSalt(loginRequest.getPassword(), salt);
        return passwordHash.equals(associatedUser.getPassword());
    }



    public ResponseEntity<Map<String, String>> accessTokenFromRefreshToken(String refreshToken) {
        if (jwtTokenUtil.isValidRefreshToken(refreshToken)) {
            String userEmail = jwtTokenUtil.getEmailFromToken(refreshToken);

            User user = checkDBForUserByEmail(userEmail);
            if (user != null) {
                String newAccessToken = jwtTokenUtil.generateAccessTokenFromRefreshToken(refreshToken);

                Map<String, String> response = new HashMap<>();
                response.put("access_token", newAccessToken);

                return ResponseEntity.ok(response);
            } else {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "User associated with the refresh token does not exist");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }
        } else {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid refresh token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

}
