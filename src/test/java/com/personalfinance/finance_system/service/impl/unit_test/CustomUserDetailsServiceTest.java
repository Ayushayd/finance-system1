package com.personalfinance.finance_system.service.impl.unit_test;

import com.personalfinance.finance_system.model.Role;
import com.personalfinance.finance_system.model.User;
import com.personalfinance.finance_system.repository.UserRepository;
import com.personalfinance.finance_system.service.impl.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void loadUserByUsername_shouldReturnUserDetails() {
        User user = new User();
        user.setUsername("john");
        user.setPassword("encodedPassword");
        user.setRole(Role.ADMIN);

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));

        UserDetails userDetails = userDetailsService.loadUserByUsername("john");

        assertEquals("john", userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void loadUserByUsername_userNotFound_shouldThrowException() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername("unknown");
        });

        assertEquals("User not found with username: unknown", exception.getMessage());
    }
}
