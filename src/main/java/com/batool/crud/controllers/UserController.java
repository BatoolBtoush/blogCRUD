package com.batool.crud.controllers;

import com.batool.crud.customexceptions.EmailAlreadyExistsException;
import com.batool.crud.customexceptions.UserNotFoundException;
import com.batool.crud.entities.User;
import com.batool.crud.dtos.UserCreationDTO;
import com.batool.crud.dtos.UserRetrievalDTO;
import com.batool.crud.dtos.UserUpdateDTO;
import com.batool.crud.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/create")
    public ResponseEntity<Map<String, String>> createUser(@Valid @RequestBody UserCreationDTO userCreationDTO) {
        Map<String, String> response = new HashMap<>();
        try {
            userService.createUser(userCreationDTO);
            response.put("message", "User created successfully");
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllUsers() {
        try {
            List<UserRetrievalDTO> allUsers = userService.getAllUsers();
            return new ResponseEntity<>(allUsers, HttpStatus.OK);
        } catch (DataAccessException e) {
            return new ResponseEntity<>("An error occurred while fetching users", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-by-id/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            UserRetrievalDTO user = userService.getUserById(id);
            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (DataAccessException e) {
            return new ResponseEntity<>("An error occurred while fetching users", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-by-email/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
        try {
            UserRetrievalDTO user = userService.getUserByEmail(email);
            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (DataAccessException e) {
            return new ResponseEntity<>("An error occurred while fetching users", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/update-by-id/{id}")
    public ResponseEntity<String> updateUserById(@PathVariable Long id,
                                                 @Valid @RequestBody UserUpdateDTO userUpdateDTO) {
        userService.updateUserById(id, userUpdateDTO);
        return new ResponseEntity<>("User updated successfully", HttpStatus.OK);

    }

    @PutMapping("/update-by-email/{email}")
    public ResponseEntity<String> updateUserByEmail(@PathVariable String email,
                                                    @Valid @RequestBody UserUpdateDTO userUpdateDTO) {
        userService.updateUserByEmail(email, userUpdateDTO);
        return new ResponseEntity<>("User updated successfully", HttpStatus.OK);

    }

    @DeleteMapping("/delete-by-id/{id}")
    public ResponseEntity<String> deleteUserById(@PathVariable Long id) {

        userService.deleteUserById(id);
        return new ResponseEntity<>("User deleted successfully", HttpStatus.OK);

    }
}
