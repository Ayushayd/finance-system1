package com.personalfinance.finance_system.service.impl.integration_test;

import com.personalfinance.finance_system.dto.ExpenseRequest;
import com.personalfinance.finance_system.dto.ExpenseResponse;
import com.personalfinance.finance_system.exception.ResourceNotFoundException;
import com.personalfinance.finance_system.model.Expense;
import com.personalfinance.finance_system.model.User;
import com.personalfinance.finance_system.model.Role;
import com.personalfinance.finance_system.repository.ExpenseRepository;
import com.personalfinance.finance_system.repository.UserRepository;
import com.personalfinance.finance_system.service.ExpenseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ExpenseServiceImplIntegrationTest {

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    private User testUser;

    @BeforeEach
    void setup() {
        expenseRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("password");
        testUser.setRole(Role.USER);
        testUser = userRepository.save(testUser);
    }

    @Test
    void addExpense_shouldAddAndReturnExpenseResponse() {
        ExpenseRequest request = new ExpenseRequest();
        request.setAmount(50.0);
        request.setCategory("Food");
        request.setDate(LocalDate.now());
        request.setDescription("Lunch");

        ExpenseResponse response = expenseService.addExpense(testUser.getUsername(), request);

        assertNotNull(response.getId());
        assertEquals(50.0, response.getAmount());
        assertEquals("Food", response.getCategory());
        assertEquals("Lunch", response.getDescription());
    }

    @Test
    void getUserExpenses_shouldReturnAllExpensesForUser() {
        Expense e1 = new Expense(null, 20.0, LocalDate.now(), "Coffee", testUser);
        Expense e2 = new Expense(null, 30.0, LocalDate.now(), "Snacks", testUser);
        expenseRepository.save(e1);
        expenseRepository.save(e2);

        List<ExpenseResponse> expenses = expenseService.getUserExpenses(testUser.getUsername());

        assertEquals(2, expenses.size());
        assertTrue(expenses.stream().anyMatch(e -> e.getDescription().equals("Coffee")));
        assertTrue(expenses.stream().anyMatch(e -> e.getDescription().equals("Snacks")));
    }

    @Test
    void getExpenseById_shouldReturnCorrectExpense() {
        Expense expense = new Expense("Groceries", 75.0, LocalDate.now(), "Big Bazaar", testUser);
        expense = expenseRepository.save(expense);

        ExpenseResponse response = expenseService.getExpenseById(testUser.getUsername(), expense.getId());

        assertEquals(expense.getId(), response.getId());
        assertEquals(75.0, response.getAmount());
        assertEquals("Groceries", response.getCategory());
    }


    @Test
    void updateExpense_shouldModifyAndReturnUpdatedExpense() {
        Expense expense = new Expense(null, 100.0, LocalDate.now(), "Shopping", testUser);
        expense = expenseRepository.save(expense);

        ExpenseRequest updateRequest = new ExpenseRequest();
        updateRequest.setAmount(120.0);
        updateRequest.setCategory("Shopping");
        updateRequest.setDate(LocalDate.now());
        updateRequest.setDescription("Updated Shopping");

        ExpenseResponse updated = expenseService.updateExpense(testUser.getUsername(), expense.getId(), updateRequest);

        assertEquals(expense.getId(), updated.getId());
        assertEquals(120.0, updated.getAmount());
        assertEquals("Updated Shopping", updated.getDescription());
    }

    @Test
    void deleteExpense_shouldRemoveExpense() {
        Expense expense = new Expense(null, 40.0, LocalDate.now(), "Taxi", testUser);
        expense = expenseRepository.save(expense);

        expenseService.deleteExpense(testUser.getUsername(), expense.getId());

        assertFalse(expenseRepository.findById(expense.getId()).isPresent());
    }

    @Test
    void getExpensesByCategory_shouldReturnFilteredExpenses() {
        Expense e1 = new Expense(null, 10.0, LocalDate.now(), "Breakfast", testUser);
        e1.setCategory("Food");
        Expense e2 = new Expense(null, 25.0, LocalDate.now(), "Dinner", testUser);
        e2.setCategory("Food");
        Expense e3 = new Expense(null, 15.0, LocalDate.now(), "Bus", testUser);
        e3.setCategory("Travel");
        expenseRepository.save(e1);
        expenseRepository.save(e2);
        expenseRepository.save(e3);

        List<ExpenseResponse> foodExpenses = expenseService.getExpensesByCategory(testUser.getUsername(), "Food");

        assertEquals(2, foodExpenses.size());
        assertTrue(foodExpenses.stream().allMatch(e -> e.getCategory().equals("Food")));
    }

    @Test
    void methods_shouldThrowException_whenUserNotFound() {
        ExpenseRequest request = new ExpenseRequest();
        request.setAmount(10.0);
        request.setCategory("Misc");
        request.setDate(LocalDate.now());
        request.setDescription("Test");

        String invalidUsername = "invalidUser";

        assertThrows(ResourceNotFoundException.class, () -> expenseService.addExpense(invalidUsername, request));
        assertThrows(ResourceNotFoundException.class, () -> expenseService.getUserExpenses(invalidUsername));
        assertThrows(ResourceNotFoundException.class, () -> expenseService.getExpenseById(invalidUsername, 1L));
        assertThrows(ResourceNotFoundException.class, () -> expenseService.updateExpense(invalidUsername, 1L, request));
        assertThrows(ResourceNotFoundException.class, () -> expenseService.deleteExpense(invalidUsername, 1L));
        assertThrows(ResourceNotFoundException.class, () -> expenseService.getExpensesByCategory(invalidUsername, "Food"));
    }

    @Test
    void getExpenseById_shouldThrowException_whenExpenseNotFoundOrAccessDenied() {
        Expense e = new Expense(null, 100.0, LocalDate.now(), "Lunch", testUser);
        e = expenseRepository.save(e);

        User otherUser = new User();
        otherUser.setUsername("otheruser");
        otherUser.setPassword("pass");
        otherUser.setRole(Role.USER);
        otherUser = userRepository.save(otherUser);

        // Try to get expense of testUser with otherUser username
        User finalOtherUser = otherUser;
        Expense finalE = e;
        assertThrows(ResourceNotFoundException.class, () -> expenseService.getExpenseById(finalOtherUser.getUsername(), finalE.getId()));
    }

    @Test
    void updateExpense_shouldThrowException_whenExpenseNotFoundOrAccessDenied() {
        User otherUser = new User();
        otherUser.setUsername("otheruser");
        otherUser.setPassword("pass");
        otherUser.setRole(Role.USER);
        otherUser = userRepository.save(otherUser);

        ExpenseRequest updateRequest = new ExpenseRequest();
        updateRequest.setAmount(10.0);
        updateRequest.setCategory("Misc");
        updateRequest.setDate(LocalDate.now());
        updateRequest.setDescription("Test");

        User finalOtherUser = otherUser;
        assertThrows(ResourceNotFoundException.class,
                () -> expenseService.updateExpense(finalOtherUser.getUsername(), 999L, updateRequest));
    }

    @Test
    void deleteExpense_shouldThrowException_whenExpenseNotFoundOrAccessDenied() {
        User otherUser = new User();
        otherUser.setUsername("otheruser");
        otherUser.setPassword("pass");
        otherUser.setRole(Role.USER);
        otherUser = userRepository.save(otherUser);

        User finalOtherUser = otherUser;
        assertThrows(ResourceNotFoundException.class,
                () -> expenseService.deleteExpense(finalOtherUser.getUsername(), 999L));
    }
}
