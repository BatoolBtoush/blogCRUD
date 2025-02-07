package com.batool.crud.service;

import com.batool.crud.entity.*;
import com.batool.crud.repo.UserRepo;
import com.batool.crud.util.Hasher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepo userRepo;

    public User createUser(UserCreateDTO userCreateDTO) {
        if (userRepo.existsByEmail(userCreateDTO.getEmail().toLowerCase())) {
            throw new IllegalArgumentException("Email is already in use");
        }
        User user = new User();
        user.setFullName(userCreateDTO.getFullName());
        user.setEmail(userCreateDTO.getEmail().toLowerCase());
        String salt = Hasher.getSalt();
        user.setSalt(salt);
        user.setPassword(Hasher.hashPasswordWithSalt(userCreateDTO.getPassword(), salt));
        user.setDateOfBirth(userCreateDTO.getDateOfBirth());
        user.setRole(userCreateDTO.getRole());
        return userRepo.save(user);
    }

    public List<UserRetrievalDTO> getAllUsers() {
        return userRepo.findAll()
                .stream()
                .map(user -> new UserRetrievalDTO(
                        user.getId(),
                        user.getFullName(),
                        user.getEmail(),
                        user.getDateOfBirth(),
                        user.getRole()
                ))
                .collect(Collectors.toList());
    }

    public UserRetrievalDTO getUserById(Long id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return new UserRetrievalDTO(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getDateOfBirth(),
                user.getRole()
        );
    }

    public UserRetrievalDTO getUserByEmail(String email) {
        User user = userRepo.findByEmail(email.toLowerCase());
        if (user == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");

        return new UserRetrievalDTO(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getDateOfBirth(),
                user.getRole()
        );
    }


    public User updateUserById(Long id, UserUpdateDTO userUpdateDTO) {
        User existentUser = userRepo.findById(id).orElse(null);
        if (existentUser == null) {
            throw new UsernameNotFoundException("User not found");
        }
        if (userUpdateDTO.getFullName() != null && !userUpdateDTO.getFullName().isEmpty()) {
            existentUser.setFullName(userUpdateDTO.getFullName());
        }
        if (userUpdateDTO.getEmail() != null && !userUpdateDTO.getEmail().isEmpty()) {
            String newEmail = userUpdateDTO.getEmail().toLowerCase();
            if (!newEmail.equals(existentUser.getEmail()) && userRepo.existsByEmail(newEmail)) {
                throw new IllegalArgumentException("Email is already in use");
            }
            existentUser.setEmail(userUpdateDTO.getEmail());
        }
        if (userUpdateDTO.getPassword() != null && !userUpdateDTO.getPassword().isEmpty()) {
            String salt = Hasher.getSalt();
            existentUser.setSalt(salt);
            existentUser.setPassword(Hasher.hashPasswordWithSalt(userUpdateDTO.getPassword(), salt));
        }
        if (userUpdateDTO.getDateOfBirth() != null) {
            existentUser.setDateOfBirth(userUpdateDTO.getDateOfBirth());
        }
        if (userUpdateDTO.getRole() != null) {
            existentUser.setRole(userUpdateDTO.getRole());
        }
        return userRepo.save(existentUser);
    }


    public User updateUserByEmail(String email, UserUpdateDTO userUpdateDTO) {
        User existentUser = userRepo.findByEmail(email.toLowerCase());
        if (existentUser == null) {
            throw new UsernameNotFoundException("User not found");
        }
        if (userUpdateDTO.getFullName() != null && !userUpdateDTO.getFullName().isEmpty()) {
            existentUser.setFullName(userUpdateDTO.getFullName());
        }
        if (userUpdateDTO.getEmail() != null && !userUpdateDTO.getEmail().isEmpty()) {
            String newEmail = userUpdateDTO.getEmail().toLowerCase();
            if (!newEmail.equals(existentUser.getEmail()) && userRepo.existsByEmail(newEmail)) {
                throw new IllegalArgumentException("Email is already in use");
            }
            existentUser.setEmail(userUpdateDTO.getEmail());
        }
        if (userUpdateDTO.getPassword() != null && !userUpdateDTO.getPassword().isEmpty()) {
            String salt = Hasher.getSalt();
            existentUser.setSalt(salt);
            existentUser.setPassword(Hasher.hashPasswordWithSalt(userUpdateDTO.getPassword(), salt));
        }
        if (userUpdateDTO.getDateOfBirth() != null) {
            existentUser.setDateOfBirth(userUpdateDTO.getDateOfBirth());
        }
        if (userUpdateDTO.getRole() != null) {
            existentUser.setRole(userUpdateDTO.getRole());
        }
        return userRepo.save(existentUser);
    }

    public void deleteUserById(Long id) {
        User existentUser = userRepo.findById(id).orElse(null);
        if (existentUser == null) {
            throw new UsernameNotFoundException("User not found");
        }
        userRepo.delete(existentUser);
    }

    public void deleteUserByEmail(String email) throws UsernameNotFoundException {
        User existentUser = userRepo.findByEmail(email.toLowerCase());
        if (existentUser == null) {
            throw new UsernameNotFoundException("User not found");
        }
        userRepo.delete(existentUser);
    }
}

