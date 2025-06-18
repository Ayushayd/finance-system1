package com.personalfinance.finance_system.service.impl.integration_test;

import com.personalfinance.finance_system.dto.AuthRequest;
import com.personalfinance.finance_system.dto.AuthResponse;
import com.personalfinance.finance_system.dto.RegisterRequest;
import com.personalfinance.finance_system.model.User;
import com.personalfinance.finance_system.repository.UserRepository;
import com.personalfinance.finance_system.security.JwtService;
import com.personalfinance.finance_system.service.AuthService;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthServiceImplIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Test
    void register_shouldSaveUserAndReturnToken() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("testpass");
        request.setEmail("testuser@example.com");
        request.setRole("USER");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertNotNull(response.getToken());

        User user = userRepository.findByUsername("testuser").orElse(null);
        assertNotNull(user);
        assertEquals("testuser", user.getUsername());
        assertEquals("testuser@example.com", user.getEmail());
        assertTrue(passwordEncoder.matches("testpass", user.getPassword()));
        assertEquals("USER", user.getRole().name());
    }


    @Test
    void login_shouldAuthenticateAndReturnToken() {
        // First register a user
        User user = new User();
        user.setUsername("loginuser");
        user.setPassword(passwordEncoder.encode("loginpass"));
        user.setRole(com.personalfinance.finance_system.model.Role.USER);
        userRepository.save(user);

        AuthRequest request = new AuthRequest();
        request.setUsername("loginuser");
        request.setPassword("loginpass");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertNotNull(response.getToken());
    }

    @Test
    void login_withWrongPassword_shouldThrowException() {
        User user = new User();
        user.setUsername("baduser");
        user.setPassword(passwordEncoder.encode("correctpass"));
        user.setRole(com.personalfinance.finance_system.model.Role.USER);
        userRepository.save(user);

        AuthRequest request = new AuthRequest();
        request.setUsername("baduser");
        request.setPassword("wrongpass");

        assertThrows(Exception.class, () -> authService.login(request));
    }
}
