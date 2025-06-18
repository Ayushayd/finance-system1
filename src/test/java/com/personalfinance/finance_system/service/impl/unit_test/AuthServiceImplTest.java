package com.personalfinance.finance_system.service.impl.unit_test;

import com.personalfinance.finance_system.dto.AuthRequest;
import com.personalfinance.finance_system.dto.AuthResponse;
import com.personalfinance.finance_system.dto.RegisterRequest;
import com.personalfinance.finance_system.model.Role;
import com.personalfinance.finance_system.model.User;
import com.personalfinance.finance_system.repository.UserRepository;
import com.personalfinance.finance_system.security.JwtService;
import com.personalfinance.finance_system.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceImplTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private AuthenticationManager authenticationManager;
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtService = mock(JwtService.class);
        authenticationManager = mock(AuthenticationManager.class);

        authService = new AuthServiceImpl(userRepository, passwordEncoder, jwtService, authenticationManager);
    }

    @Test
    void register_shouldReturnToken() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("mockuser");
        request.setPassword("mockpass");
        request.setRole("ADMIN");

        when(passwordEncoder.encode("mockpass")).thenReturn("encodedpass");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(any(User.class))).thenReturn("mock-token");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("mock-token", response.getToken());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void login_shouldReturnToken() {
        AuthRequest request = new AuthRequest();
        request.setUsername("mockuser");
        request.setPassword("mockpass");

        User user = new User();
        user.setUsername("mockuser");
        user.setPassword("encodedpass");
        user.setRole(Role.USER);

        when(userRepository.findByUsername("mockuser")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("mock-token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mock-token", response.getToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void register_shouldAssignUserRoleWhenNotAdmin() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("user1");
        request.setPassword("password");
        request.setEmail("user1@example.com");
        request.setRole("user"); // lowercase, should map to USER role

        when(passwordEncoder.encode("password")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(any(User.class))).thenReturn("mock-token");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("mock-token", response.getToken());
        assertEquals("USER", response.getRole());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void login_shouldThrowExceptionWhenUserNotFound() {
        AuthRequest request = new AuthRequest();
        request.setUsername("nonexistent");
        request.setPassword("password");

        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.login(request));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void login_shouldThrowExceptionOnInvalidCredentials() {
        AuthRequest request = new AuthRequest();
        request.setUsername("mockuser");
        request.setPassword("wrongpass");

        User user = new User();
        user.setUsername("mockuser");

        when(userRepository.findByUsername("mockuser")).thenReturn(Optional.of(user));
        doThrow(new RuntimeException("Bad credentials")).when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.login(request));
        assertEquals("Bad credentials", exception.getMessage());
    }

}
