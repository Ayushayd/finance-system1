package com.personalfinance.finance_system.service.impl.integration_test;

import com.personalfinance.finance_system.model.Role;
import com.personalfinance.finance_system.model.User;
import com.personalfinance.finance_system.repository.UserRepository;
import com.personalfinance.finance_system.service.impl.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional  // Rollback after each test
class CustomUserDetailsServiceIntegrationTest {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private UserRepository userRepository;

    private User savedUser;

    @BeforeEach
    void setUp() {
        // Clear repository and add a test user
        userRepository.deleteAll();

        User user = new User();
        user.setUsername("integrationUser");
        user.setPassword("encodedPassword");
        user.setRole(Role.USER);
        savedUser = userRepository.save(user);
    }

    @Test
    void loadUserByUsername_shouldReturnUserDetails() {
        UserDetails userDetails = userDetailsService.loadUserByUsername("integrationUser");

        assertNotNull(userDetails);
        assertEquals("integrationUser", userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void loadUserByUsername_userNotFound_shouldThrowException() {
        assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername("nonexistentUser");
        });
    }
}
