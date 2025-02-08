package com.batool.crud.controllers;

import com.batool.crud.entities.User;
import com.batool.crud.dtos.UserCreationDTO;
import com.batool.crud.dtos.UserRetrievalDTO;
import com.batool.crud.dtos.UserUpdateDTO;
import com.batool.crud.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            User createdUser = userService.createUser(userCreationDTO);
            response.put("message", "User created successfully");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/get-all")
    public ResponseEntity<List<UserRetrievalDTO>> getAllUsers() {
        List<UserRetrievalDTO> allUsers = userService.getAllUsers();
        return ResponseEntity.ok(allUsers);
    }

    @GetMapping("/get-by-id/{id}")
    public ResponseEntity<UserRetrievalDTO> getUserById(@PathVariable Long id) {
        UserRetrievalDTO user =  userService.getUserById(id);
        return ResponseEntity.ok(user);

    }

    @GetMapping("/get-by-email/{email}")
    public ResponseEntity<UserRetrievalDTO> getUserByEmail(@PathVariable String email) {
        UserRetrievalDTO user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/update-by-id/{id}")
    public ResponseEntity<String> updateUserById(@PathVariable Long id,
                                                 @Valid @RequestBody UserUpdateDTO userUpdateDTO) {
        try {
            User updatedUser = userService.updateUserById(id, userUpdateDTO);
            return new ResponseEntity<>("User updated successfully", HttpStatus.OK);
        } catch (UsernameNotFoundException e) {
            return new ResponseEntity<>("User not found with id: " + id, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to update user: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/update-by-email/{email}")
    public ResponseEntity<String> updateUserByEmail(@PathVariable String email,
                                                    @Valid @RequestBody UserUpdateDTO userUpdateDTO) {
        try {
            User updatedUser = userService.updateUserByEmail(email, userUpdateDTO);
            return new ResponseEntity<>("User updated successfully", HttpStatus.OK);
        } catch (UsernameNotFoundException e) {
            return new ResponseEntity<>("User not found with email: " + email, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to update user: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete-by-id/{id}")
    public ResponseEntity<String> deleteUserById(@PathVariable Long id) {
        try {
            userService.deleteUserById(id);
            return new ResponseEntity<>("User deleted successfully", HttpStatus.OK);
        } catch (UsernameNotFoundException e) {
            return new ResponseEntity<>("User not found with id: " + id, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to delete user: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete-by-email/{email}")
    public ResponseEntity<String> deleteUserByEmail(@PathVariable String email) {
        try {
            userService.deleteUserByEmail(email);
            return new ResponseEntity<>("User deleted successfully", HttpStatus.OK);
        } catch (UsernameNotFoundException e) {
            return new ResponseEntity<>("User not found with email: " + email, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to delete user: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
