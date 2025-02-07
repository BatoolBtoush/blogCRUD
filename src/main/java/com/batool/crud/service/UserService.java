package com.batool.crud.service;

import com.batool.crud.entity.Role;
import com.batool.crud.entity.User;
import com.batool.crud.repo.UserRepo;
import com.batool.crud.util.Hasher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepo userRepo;

    public User createUser(User user) {
        if (userRepo.existsByEmail(user.getEmail().toLowerCase())) {
            throw new IllegalArgumentException("Email is already in use");
        }
        user.setFullName(user.getFullName());
        user.setEmail(user.getEmail().toLowerCase());
        String salt = Hasher.getSalt();
        user.setSalt(salt);
        user.setPassword(Hasher.hashPasswordWithSalt(user.getPassword(), salt));
        user.setDateOfBirth(user.getDateOfBirth());
        user.setRole(user.getRole());
        return userRepo.save(user);
    }

    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    public User getUserById(Long id) {
        return userRepo.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }


    public User updateUserById(Long id, User updatedUser) {
        User existentUser = getUserById(id);
        if (existentUser == null) {
            throw new UsernameNotFoundException("User not found");
        }
        if (updatedUser.getFullName() != null && !updatedUser.getFullName().isEmpty()) {
            existentUser.setFullName(updatedUser.getFullName());
        }
        if (updatedUser.getEmail() != null && !updatedUser.getEmail().isEmpty()) {
            existentUser.setEmail(updatedUser.getEmail());
        }
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            String salt = Hasher.getSalt();
            existentUser.setSalt(salt);
            existentUser.setPassword(Hasher.hashPasswordWithSalt(updatedUser.getPassword(), salt));
        }
        if (updatedUser.getDateOfBirth() != null) {
            existentUser.setDateOfBirth(updatedUser.getDateOfBirth());
        }
        if (updatedUser.getRole() != null) {
            existentUser.setRole(updatedUser.getRole());
        }
        return userRepo.save(existentUser);
    }


    public User updateUserByEmail(String email, User updatedUser) throws UsernameNotFoundException {
        User existentUser = userRepo.findByEmail(email.toLowerCase());
        if (existentUser == null) {
            throw new UsernameNotFoundException("User not found");
        }
        if (updatedUser.getFullName() != null && !updatedUser.getFullName().isEmpty()) {
            existentUser.setFullName(updatedUser.getFullName());
        }
        if (updatedUser.getEmail() != null && !updatedUser.getEmail().isEmpty()) {
            existentUser.setEmail(updatedUser.getEmail());
        }
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            String salt = Hasher.getSalt();
            existentUser.setSalt(salt);
            existentUser.setPassword(Hasher.hashPasswordWithSalt(updatedUser.getPassword(), salt));
        }
        if (updatedUser.getDateOfBirth() != null) {
            existentUser.setDateOfBirth(updatedUser.getDateOfBirth());
        }
        if (updatedUser.getRole() != null) {
            existentUser.setRole(updatedUser.getRole());
        }
        return userRepo.save(existentUser);
    }

    public void deleteUserById(Long id) {
        User existentUser = getUserById(id);
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

