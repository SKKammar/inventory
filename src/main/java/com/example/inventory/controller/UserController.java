package com.example.inventory.controller;

import com.example.inventory.dto.SignupRequestDTO;
import com.example.inventory.dto.UserResponseDTO;
import com.example.inventory.dto.ApiResponseDTO;
import com.example.inventory.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User management endpoints")
@CrossOrigin(origins = "*", maxAge = 3600)
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    /**
     * Get user by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieve user details by ID")
    public ResponseEntity<ApiResponseDTO<UserResponseDTO>> getUserById(@PathVariable Long id) {
        logger.info("Fetching user with ID: {}", id);

        UserResponseDTO user = userService.getUserById(id);

        ApiResponseDTO<UserResponseDTO> response = ApiResponseDTO.<UserResponseDTO>builder()
                .success(true)
                .message("User retrieved successfully")
                .data(user)
                .statusCode(200)
                .timestamp(System.currentTimeMillis())
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get user by username
     */
    @GetMapping("/username/{username}")
    @Operation(summary = "Get user by username", description = "Retrieve user details by username")
    public ResponseEntity<ApiResponseDTO<UserResponseDTO>> getUserByUsername(@PathVariable String username) {
        logger.info("Fetching user with username: {}", username);

        UserResponseDTO user = userService.getUserByUsername(username);

        ApiResponseDTO<UserResponseDTO> response = ApiResponseDTO.<UserResponseDTO>builder()
                .success(true)
                .message("User retrieved successfully")
                .data(user)
                .statusCode(200)
                .timestamp(System.currentTimeMillis())
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Update user
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Update user information")
    public ResponseEntity<ApiResponseDTO<UserResponseDTO>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody SignupRequestDTO updateRequest) {
        logger.info("Updating user with ID: {}", id);

        UserResponseDTO user = userService.updateUser(id, updateRequest);

        ApiResponseDTO<UserResponseDTO> response = ApiResponseDTO.<UserResponseDTO>builder()
                .success(true)
                .message("User updated successfully")
                .data(user)
                .statusCode(200)
                .timestamp(System.currentTimeMillis())
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Delete user (Admin only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete user", description = "Delete a user (Admin only)")
    public ResponseEntity<ApiResponseDTO<?>> deleteUser(@PathVariable Long id) {
        logger.info("Deleting user with ID: {}", id);

        userService.deleteUser(id);

        ApiResponseDTO<?> response = ApiResponseDTO.builder()
                .success(true)
                .message("User deleted successfully")
                .statusCode(200)
                .timestamp(System.currentTimeMillis())
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}