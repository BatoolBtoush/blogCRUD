package com.batool.crud.services;


import com.batool.crud.customexceptions.EmailAlreadyExistsException;
import com.batool.crud.customexceptions.InvalidRefreshTokenException;
import com.batool.crud.customexceptions.UserNotFoundException;
import com.batool.crud.dtos.LoginRequestDTO;
import com.batool.crud.dtos.RegistrationRequestDTO;
import com.batool.crud.enums.Role;
import com.batool.crud.entities.User;
import com.batool.crud.repos.UserRepo;
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
        String email = registrationRequestDTO.getEmail().toLowerCase();

        if (userRepo.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email is already in use");
        }

        User user = new User();
        user.setFullName(registrationRequestDTO.getFullName());
        user.setEmail(registrationRequestDTO.getEmail().toLowerCase());
        String salt = Hasher.getSalt();
        user.setSalt(salt);
        user.setPassword(Hasher.hashPasswordWithSalt(registrationRequestDTO.getPassword(), salt));
        user.setDateOfBirth(registrationRequestDTO.getDateOfBirth());
        user.setRole(Role.ROLE_ADMIN);
        return userRepo.save(user);
   }

    public User registerContentWriter(RegistrationRequestDTO registrationRequestDTO){
        String email = registrationRequestDTO.getEmail().toLowerCase();

        if (userRepo.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email is already in use");
        }

        User user = new User();
        user.setFullName(registrationRequestDTO.getFullName());
        user.setEmail(registrationRequestDTO.getEmail().toLowerCase());
        String salt = Hasher.getSalt();
        user.setSalt(salt);
        user.setPassword(Hasher.hashPasswordWithSalt(registrationRequestDTO.getPassword(), salt));
        user.setDateOfBirth(registrationRequestDTO.getDateOfBirth());
        user.setRole(Role.ROLE_CONTENT_WRITER);
        return userRepo.save(user);
    }


    public User registerNormalUser(RegistrationRequestDTO registrationRequestDTO){
        String email = registrationRequestDTO.getEmail().toLowerCase();

        if (userRepo.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email is already in use");
        }

        User user = new User();
        user.setFullName(registrationRequestDTO.getFullName());
        user.setEmail(registrationRequestDTO.getEmail().toLowerCase());
        String salt = Hasher.getSalt();
        user.setSalt(salt);
        user.setPassword(Hasher.hashPasswordWithSalt(registrationRequestDTO.getPassword(), salt));
        user.setDateOfBirth(registrationRequestDTO.getDateOfBirth());
        user.setRole(Role.ROLE_NORMAL);
        return userRepo.save(user);
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
        if (!jwtTokenUtil.isValidRefreshToken(refreshToken)) {
            throw new InvalidRefreshTokenException("Refresh token is invalid or has expired");
        }
        String userEmail = jwtTokenUtil.getEmailFromToken(refreshToken);
        User user = checkDBForUserByEmail(userEmail);

        if (user == null) {
            throw new UserNotFoundException("User associated with the refresh token does not exist");
        }

        String newAccessToken = jwtTokenUtil.generateAccessTokenFromRefreshToken(refreshToken);

        Map<String, String> response = new HashMap<>();
        response.put("access_token", newAccessToken);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
