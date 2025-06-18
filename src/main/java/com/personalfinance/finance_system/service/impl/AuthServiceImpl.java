package com.personalfinance.finance_system.service.impl;

import com.personalfinance.finance_system.dto.AuthRequest;
import com.personalfinance.finance_system.dto.AuthResponse;
import com.personalfinance.finance_system.dto.RegisterRequest;
import com.personalfinance.finance_system.model.Role;
import com.personalfinance.finance_system.model.User;
import com.personalfinance.finance_system.repository.UserRepository;
import com.personalfinance.finance_system.security.JwtService;
import com.personalfinance.finance_system.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());

        if ("ADMIN".equalsIgnoreCase(request.getRole())) {
            user.setRole(Role.ADMIN);
        } else {
            user.setRole(Role.USER);
        }

        userRepository.save(user);

        String token = jwtService.generateToken(user);

        return new AuthResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getRole().name(),
                user.getEmail()
        );
    }


    @Override
    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        String token = jwtService.generateToken(user);

        return new AuthResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getRole().name(),
                user.getEmail()
        );
    }

}