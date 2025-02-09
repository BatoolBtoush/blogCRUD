package com.batool.crud.services;

import com.batool.crud.customexceptions.EmailAlreadyExistsException;
import com.batool.crud.customexceptions.UserNotFoundException;
import com.batool.crud.dtos.UserCreationDTO;
import com.batool.crud.dtos.UserRetrievalDTO;
import com.batool.crud.dtos.UserUpdateDTO;
import com.batool.crud.entities.*;
import com.batool.crud.repos.NewsRepo;
import com.batool.crud.repos.UserRepo;
import com.batool.crud.util.Hasher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private NewsRepo newsRepo;

    public void createUser(UserCreationDTO userCreationDTO) {
        if (userRepo.existsByEmail(userCreationDTO.getEmail().toLowerCase())) {
            throw new EmailAlreadyExistsException("Email is already in use");
        }
        User user = new User();
        user.setFullName(userCreationDTO.getFullName());
        user.setEmail(userCreationDTO.getEmail().toLowerCase());
        String salt = Hasher.getSalt();
        user.setSalt(salt);
        user.setPassword(Hasher.hashPasswordWithSalt(userCreationDTO.getPassword(), salt));
        user.setDateOfBirth(userCreationDTO.getDateOfBirth());
        user.setRole(userCreationDTO.getRole());
        userRepo.save(user);
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
                .orElseThrow(() -> new UserNotFoundException("User not found"));

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
            throw new UserNotFoundException("User with email " + email + " not found");

        return new UserRetrievalDTO(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getDateOfBirth(),
                user.getRole()
        );
    }


    public void updateUserById(Long id, UserUpdateDTO userUpdateDTO) {
        User existentUser = userRepo.findById(id).orElse(null);
        if (existentUser == null) {
            throw new UserNotFoundException("User not found");
        }
        if (userUpdateDTO.getFullName() != null && !userUpdateDTO.getFullName().isEmpty()) {
            existentUser.setFullName(userUpdateDTO.getFullName());
        }
        if (userUpdateDTO.getEmail() != null && !userUpdateDTO.getEmail().isEmpty()) {
            String newEmail = userUpdateDTO.getEmail().toLowerCase();
            if (!newEmail.equals(existentUser.getEmail()) && userRepo.existsByEmail(newEmail)) {
                throw new EmailAlreadyExistsException("Email is already in use");
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
        userRepo.save(existentUser);
    }


    public void updateUserByEmail(String email, UserUpdateDTO userUpdateDTO) {
        User existentUser = userRepo.findByEmail(email.toLowerCase());
        if (existentUser == null) {
            throw new UserNotFoundException("User not found");
        }
        if (userUpdateDTO.getFullName() != null && !userUpdateDTO.getFullName().isEmpty()) {
            existentUser.setFullName(userUpdateDTO.getFullName());
        }
        if (userUpdateDTO.getEmail() != null && !userUpdateDTO.getEmail().isEmpty()) {
            String newEmail = userUpdateDTO.getEmail().toLowerCase();
            if (!newEmail.equals(existentUser.getEmail()) && userRepo.existsByEmail(newEmail)) {
                throw new EmailAlreadyExistsException("Email is already in use");
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
        userRepo.save(existentUser);
    }

    public void deleteUserById(Long id) {
        User existentUser = userRepo.findById(id).orElse(null);
        if (existentUser == null) {
            throw new UserNotFoundException("User not found");
        }
        List<News> associatedNews = newsRepo.findByCreatedBy(existentUser);
        if (!associatedNews.isEmpty()) {
            throw new IllegalStateException("Cannot delete user with news associated with them.");
        }
        userRepo.delete(existentUser);
    }
}

