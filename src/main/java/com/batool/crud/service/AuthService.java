package com.batool.crud.service;


import com.batool.crud.entity.Role;
import com.batool.crud.entity.User;
import com.batool.crud.repo.UserRepo;
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
    private JwtService jwtService;



    public ResponseEntity<?> registerAdmin(User user){
        user.setEmail(user.getEmail().toLowerCase());
        user.setRole(Role.ROLE_ADMIN);
        user.setPassword(new Hasher(user.getPassword()).getHash());
        userRepo.save(user);

        return new ResponseEntity<>("registration Successful", HttpStatus.OK);
   }

    public ResponseEntity<?> registerContentWriter(User user){
        user.setEmail(user.getEmail().toLowerCase());
        user.setRole(Role.ROLE_CONTENT_WRITER);
        user.setPassword(new Hasher(user.getPassword()).getHash());
        userRepo.save(user);

        return new ResponseEntity<>("registration Successful", HttpStatus.OK);
    }


    public ResponseEntity<?> registerNormalUser(User user){
        user.setEmail(user.getEmail().toLowerCase());
        user.setRole(Role.ROLE_NORMAL);
        user.setPassword(new Hasher(user.getPassword()).getHash());
        userRepo.save(user);

        return new ResponseEntity<>("registration Successful", HttpStatus.OK);
    }

    public ResponseEntity<Map<String, String>> login(User user) {
        Map<String, String> response = new HashMap<>();

        User existantUser = checkDBForUserByEmail(user.getEmail());

        if (existantUser == null) {
            response.put("message", "User not found");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);

        } else {
            if (checkUsersPassword(user)) {
                String accessToken = jwtService.generateToken(existantUser.getEmail(), JwtService.Tokens.ACCESS_TOKEN);
                String refreshToken = jwtService.generateToken(existantUser.getEmail(), JwtService.Tokens.REFRESH_TOKEN);
                response.put("access_token", accessToken);
                response.put("refresh_token", refreshToken);
                return new ResponseEntity<>(response, HttpStatus.OK);

            } else {
                response.put("message", "Incorrect password");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);

            }
        }
    }

    public User checkDBForUserByEmail(String email) {
        return userRepo.findByEmailIgnoreCase(email);
    }

    public boolean checkUsersPassword(User user) {
        String passwordHash = new Hasher(user.getPassword()).getHash();
        return (passwordHash.equals(checkDBForUserByEmail(user.getEmail()).getPassword()));
    }


    public ResponseEntity<Map<String, String>> accessTokenFromRefreshToken(String refreshToken) {
        // Check if the refresh token is valid
        if (jwtService.isValidRefreshToken(refreshToken)) {

            // Retrieve user information from the refresh token
            String userEmail = jwtService.extractEmailFromRefreshTokenJwtService(refreshToken);

            User user = checkDBForUserByEmail(userEmail);

            // Check if the user still exists in the database
            if (user != null) {
                // Generate a new access token
                String newAccessToken = jwtService.generateAccessTokenFromRefreshToken(refreshToken);

                Map<String, String> response = new HashMap<>();
                response.put("access_token", newAccessToken);

                return ResponseEntity.ok(response);
            } else {
                // Handle the case where the user associated with the refresh token has been deleted
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "User associated with the refresh token does not exist");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }
        } else {
            // Handle the case of an invalid refresh token
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid refresh token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }




}
