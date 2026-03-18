package com.example.inventory.service;

import com.example.inventory.dto.SignupRequestDTO;
import com.example.inventory.dto.UserResponseDTO;
import com.example.inventory.entity.Role;
import com.example.inventory.entity.User;
import com.example.inventory.exception.DuplicateResourceException;
import com.example.inventory.exception.ResourceNotFoundException;
import com.example.inventory.repository.RoleRepository;
import com.example.inventory.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ModelMapper modelMapper;

    /**
     * Register new user
     */
    public UserResponseDTO registerUser(SignupRequestDTO signupRequest) {
        logger.info("Registering new user: {}", signupRequest.getUsername());

        // Check if username already exists
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            throw new DuplicateResourceException("User", "username", signupRequest.getUsername());
        }

        // Check if email already exists
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new DuplicateResourceException("User", "email", signupRequest.getEmail());
        }

        // Create new user
        User user = User.builder()
                .username(signupRequest.getUsername())
                .email(signupRequest.getEmail())
                .password(passwordEncoder.encode(signupRequest.getPassword()))
                .firstName(signupRequest.getFirstName())
                .lastName(signupRequest.getLastName())
                .phoneNumber(signupRequest.getPhoneNumber())
                .address(signupRequest.getAddress())
                .isActive(true)
                .build();

        // Assign default CUSTOMER role
        Role customerRole = roleRepository.findByName("ROLE_CUSTOMER")
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", "ROLE_CUSTOMER"));

        user.setRoles(new HashSet<>(Collections.singletonList(customerRole)));

        User savedUser = userRepository.save(user);
        logger.info("User registered successfully: {}", savedUser.getId());

        return convertToResponseDTO(savedUser);
    }

    /**
     * Get user by ID
     */
    public UserResponseDTO getUserById(Long userId) {
        logger.info("Fetching user with ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return convertToResponseDTO(user);
    }

    /**
     * Get user by username
     */
    public UserResponseDTO getUserByUsername(String username) {
        logger.info("Fetching user with username: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        return convertToResponseDTO(user);
    }

    /**
     * Update user
     */
    public UserResponseDTO updateUser(Long userId, SignupRequestDTO updateRequest) {
        logger.info("Updating user with ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Check if email is being changed and if it already exists
        if (!user.getEmail().equals(updateRequest.getEmail()) &&
                userRepository.existsByEmail(updateRequest.getEmail())) {
            throw new DuplicateResourceException("User", "email", updateRequest.getEmail());
        }

        user.setFirstName(updateRequest.getFirstName());
        user.setLastName(updateRequest.getLastName());
        user.setEmail(updateRequest.getEmail());
        user.setPhoneNumber(updateRequest.getPhoneNumber());
        user.setAddress(updateRequest.getAddress());

        User updatedUser = userRepository.save(user);
        logger.info("User updated successfully: {}", userId);

        return convertToResponseDTO(updatedUser);
    }

    /**
     * Delete user
     */
    public void deleteUser(Long userId) {
        logger.info("Deleting user with ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        userRepository.delete(user);
        logger.info("User deleted successfully: {}", userId);
    }

    /**
     * Check if user exists by username
     */
    public boolean userExistsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Check if user exists by email
     */
    public boolean userExistsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Convert User entity to UserResponseDTO
     */
    private UserResponseDTO convertToResponseDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .address(user.getAddress())
                .isActive(user.getIsActive())
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet()))
                .build();
    }
}