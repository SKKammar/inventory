package com.example.inventory.service;

import com.example.inventory.dto.SignupRequestDTO;
import com.example.inventory.dto.UserResponseDTO;
import com.example.inventory.entity.Role;
import com.example.inventory.entity.User;
import com.example.inventory.exception.DuplicateResourceException;
import com.example.inventory.exception.ResourceNotFoundException;
import com.example.inventory.repository.RoleRepository;
import com.example.inventory.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    public UserResponseDTO registerUser(SignupRequestDTO req) {
        if (userRepository.existsByUsername(req.getUsername()))
            throw new DuplicateResourceException("User", "username", req.getUsername());
        if (userRepository.existsByEmail(req.getEmail()))
            throw new DuplicateResourceException("User", "email", req.getEmail());

        Role role = roleRepository.findByName("ROLE_CUSTOMER")
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", "ROLE_CUSTOMER"));

        User user = User.builder()
                .username(req.getUsername()).email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .firstName(req.getFirstName()).lastName(req.getLastName())
                .phoneNumber(req.getPhoneNumber()).address(req.getAddress())
                .isActive(true).roles(new HashSet<>(Collections.singletonList(role)))
                .build();

        return toDTO(userRepository.save(user));
    }

    public UserResponseDTO getUserById(Long id) {
        return toDTO(userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id)));
    }

    public UserResponseDTO getUserByUsername(String username) {
        return toDTO(userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username)));
    }

    public UserResponseDTO updateUser(Long id, SignupRequestDTO req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (!user.getEmail().equals(req.getEmail()) && userRepository.existsByEmail(req.getEmail()))
            throw new DuplicateResourceException("User", "email", req.getEmail());

        user.setFirstName(req.getFirstName()); user.setLastName(req.getLastName());
        user.setEmail(req.getEmail()); user.setPhoneNumber(req.getPhoneNumber());
        user.setAddress(req.getAddress());
        return toDTO(userRepository.save(user));
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        userRepository.delete(user);
    }

    public boolean userExistsByUsername(String username) { return userRepository.existsByUsername(username); }
    public boolean userExistsByEmail(String email) { return userRepository.existsByEmail(email); }

    private UserResponseDTO toDTO(User u) {
        return UserResponseDTO.builder()
                .id(u.getId()).username(u.getUsername()).email(u.getEmail())
                .firstName(u.getFirstName()).lastName(u.getLastName())
                .phoneNumber(u.getPhoneNumber()).address(u.getAddress())
                .isActive(u.getIsActive())
                .roles(u.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .build();
    }
}
