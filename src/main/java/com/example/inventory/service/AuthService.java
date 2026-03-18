package com.example.inventory.service;

import com.example.inventory.dto.AuthRequestDTO;
import com.example.inventory.dto.AuthResponseDTO;
import com.example.inventory.dto.SignupRequestDTO;
import com.example.inventory.dto.UserResponseDTO;
import com.example.inventory.entity.RefreshToken;
import com.example.inventory.entity.User;
import com.example.inventory.exception.InvalidCredentialsException;
import com.example.inventory.exception.InvalidTokenException;
import com.example.inventory.exception.ResourceNotFoundException;
import com.example.inventory.repository.RefreshTokenRepository;
import com.example.inventory.repository.UserRepository;
import com.example.inventory.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserService userService;

    private static final long REFRESH_TOKEN_VALIDITY_DAYS = 7;

    /**
     * Authenticate user and generate tokens
     */
    public AuthResponseDTO authenticateUser(AuthRequestDTO loginRequest) {
        logger.info("Authenticating user: {}", loginRequest.getUsername());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            String accessToken = jwtTokenProvider.generateTokenFromAuthentication(authentication);

            User user = userRepository.findByUsername(loginRequest.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "username", loginRequest.getUsername()));

            RefreshToken refreshToken = createRefreshToken(user);

            UserResponseDTO userResponseDTO = userService.getUserByUsername(loginRequest.getUsername());

            logger.info("User authenticated successfully: {}", loginRequest.getUsername());

            return AuthResponseDTO.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken.getToken())
                    .tokenType("Bearer")
                    .expiresIn(3600L) // 1 hour
                    .user(userResponseDTO)
                    .build();

        } catch (Exception ex) {
            logger.error("Authentication failed for user: {}", loginRequest.getUsername());
            throw new InvalidCredentialsException("Invalid username or password");
        }
    }

    /**
     * Register new user
     */
    public AuthResponseDTO registerUser(SignupRequestDTO signupRequest) {
        logger.info("Registering new user: {}", signupRequest.getUsername());

        UserResponseDTO userResponseDTO = userService.registerUser(signupRequest);

        User user = userRepository.findByUsername(signupRequest.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", signupRequest.getUsername()));

        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toList());

        String accessToken = jwtTokenProvider.generateToken(user.getUsername(), roles);
        RefreshToken refreshToken = createRefreshToken(user);

        logger.info("User registered and authenticated: {}", signupRequest.getUsername());

        return AuthResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(3600L)
                .user(userResponseDTO)
                .build();
    }

    /**
     * Refresh access token using refresh token
     */
    public AuthResponseDTO refreshAccessToken(String refreshTokenStr) {
        logger.info("Refreshing access token");

        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenStr)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new InvalidTokenException("Refresh token has expired");
        }

        User user = refreshToken.getUser();
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toList());

        String newAccessToken = jwtTokenProvider.generateToken(user.getUsername(), roles);

        logger.info("Access token refreshed successfully for user: {}", user.getUsername());

        return AuthResponseDTO.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshTokenStr)
                .tokenType("Bearer")
                .expiresIn(3600L)
                .build();
    }

    /**
     * Logout user
     */
    public void logout(String refreshTokenStr) {
        logger.info("Logging out user");

        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenStr)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        refreshTokenRepository.delete(refreshToken);
        logger.info("User logged out successfully");
    }

    /**
     * Create refresh token
     */
    private RefreshToken createRefreshToken(User user) {
        // Delete existing refresh token for this user
        refreshTokenRepository.findByUser(user).ifPresent(refreshTokenRepository::delete);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(java.util.UUID.randomUUID().toString())
                .user(user)
                .expiryDate(LocalDateTime.now().plusDays(REFRESH_TOKEN_VALIDITY_DAYS))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }
}