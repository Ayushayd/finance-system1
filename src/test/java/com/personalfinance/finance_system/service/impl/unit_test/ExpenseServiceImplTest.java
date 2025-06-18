package com.personalfinance.finance_system.service.impl.unit_test;

import com.personalfinance.finance_system.dto.ExpenseRequest;
import com.personalfinance.finance_system.dto.ExpenseResponse;
import com.personalfinance.finance_system.exception.ResourceNotFoundException;
import com.personalfinance.finance_system.model.Expense;
import com.personalfinance.finance_system.model.User;
import com.personalfinance.finance_system.repository.ExpenseRepository;
import com.personalfinance.finance_system.repository.UserRepository;
import com.personalfinance.finance_system.service.impl.ExpenseServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExpenseServiceImplTest {

    private ExpenseRepository expenseRepository;
    private UserRepository userRepository;
    private ExpenseServiceImpl expenseService;

    @BeforeEach
    void setUp() {
        expenseRepository = mock(ExpenseRepository.class);
        userRepository = mock(UserRepository.class);
        expenseService = new ExpenseServiceImpl(expenseRepository, userRepository);
    }

    @Test
    void addExpense_ShouldReturnExpenseResponse() {
        String username = "john";
        User user = new User();
        user.setId(1L);

        ExpenseRequest request = new ExpenseRequest();
        request.setAmount(100.0);
        request.setCategory("Food");
        request.setDate(LocalDate.now());
        request.setDescription("Lunch");

        Expense savedExpense = new Expense();
        savedExpense.setId(1L);
        savedExpense.setUser(user);
        savedExpense.setAmount(request.getAmount());
        savedExpense.setCategory(request.getCategory());
        savedExpense.setDate(request.getDate());
        savedExpense.setDescription(request.getDescription());

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(expenseRepository.save(any(Expense.class))).thenReturn(savedExpense);

        ExpenseResponse response = expenseService.addExpense(username, request);

        assertEquals(1L, response.getId());
        assertEquals("Food", response.getCategory());
        assertEquals(100.0, response.getAmount());
        verify(userRepository).findByUsername(username);
        verify(expenseRepository).save(any(Expense.class));
    }

    @Test
    void getUserExpenses_ShouldReturnListOfResponses() {
        String username = "john";
        User user = new User();
        user.setId(1L);

        Expense expense = new Expense();
        expense.setId(1L);
        expense.setAmount(50.0);
        expense.setCategory("Transport");
        expense.setDate(LocalDate.now());
        expense.setDescription("Taxi");
        expense.setUser(user);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(expenseRepository.findByUserId(user.getId())).thenReturn(List.of(expense));

        List<ExpenseResponse> responses = expenseService.getUserExpenses(username);

        assertEquals(1, responses.size());
        assertEquals("Transport", responses.get(0).getCategory());
    }

    @Test
    void getExpenseById_ShouldReturnCorrectExpense() {
        String username = "john";
        Long id = 1L;
        User user = new User();
        user.setId(1L);

        Expense expense = new Expense();
        expense.setId(id);
        expense.setUser(user);
        expense.setAmount(80.0);
        expense.setCategory("Bills");
        expense.setDate(LocalDate.now());
        expense.setDescription("Electricity");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(expenseRepository.findById(id)).thenReturn(Optional.of(expense));

        ExpenseResponse response = expenseService.getExpenseById(username, id);

        assertEquals(id, response.getId());
        assertEquals("Bills", response.getCategory());
    }

    @Test
    void updateExpense_ShouldUpdateAndReturnUpdatedExpense() {
        String username = "john";
        Long id = 1L;
        User user = new User();
        user.setId(1L);

        Expense existingExpense = new Expense();
        existingExpense.setId(id);
        existingExpense.setUser(user);
        existingExpense.setAmount(30.0);
        existingExpense.setCategory("Snacks");

        ExpenseRequest updateRequest = new ExpenseRequest();
        updateRequest.setAmount(45.0);
        updateRequest.setCategory("Snacks");
        updateRequest.setDate(LocalDate.now());
        updateRequest.setDescription("Evening Snacks");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(expenseRepository.findById(id)).thenReturn(Optional.of(existingExpense));
        when(expenseRepository.save(any(Expense.class))).thenReturn(existingExpense);

        ExpenseResponse response = expenseService.updateExpense(username, id, updateRequest);

        assertEquals(45.0, response.getAmount());
        assertEquals("Evening Snacks", response.getDescription());
    }

    @Test
    void deleteExpense_ShouldCallDeleteMethod() {
        String username = "john";
        Long id = 1L;
        User user = new User();
        user.setId(1L);

        Expense expense = new Expense();
        expense.setId(id);
        expense.setUser(user);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(expenseRepository.findById(id)).thenReturn(Optional.of(expense));

        expenseService.deleteExpense(username, id);

        verify(expenseRepository).delete(expense);
    }

    @Test
    void getExpensesByCategory_ShouldReturnFilteredExpenses() {
        String username = "john";
        String category = "Groceries";
        User user = new User();
        user.setId(1L);

        Expense expense = new Expense();
        expense.setId(1L);
        expense.setUser(user);
        expense.setCategory(category);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(expenseRepository.findByUserIdAndCategory(user.getId(), category))
            .thenReturn(List.of(expense));

        List<ExpenseResponse> responses = expenseService.getExpensesByCategory(username, category);

        assertEquals(1, responses.size());
        assertEquals(category, responses.get(0).getCategory());
    }

    @Test
    void addExpense_shouldThrowException_whenUserNotFound() {
        when(userRepository.findByUsername("invalid")).thenReturn(Optional.empty());

        ExpenseRequest request = new ExpenseRequest();
        assertThrows(ResourceNotFoundException.class, () ->
                expenseService.addExpense("invalid", request)
        );
    }

    @Test
    void getExpenseById_shouldThrowException_whenExpenseNotOwnedByUser() {
        String username = "john";
        Long id = 1L;

        User user = new User(); user.setId(1L);
        User anotherUser = new User(); anotherUser.setId(2L);

        Expense expense = new Expense();
        expense.setId(id);
        expense.setUser(anotherUser);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(expenseRepository.findById(id)).thenReturn(Optional.of(expense));

        assertThrows(ResourceNotFoundException.class, () ->
                expenseService.getExpenseById(username, id)
        );
    }



}
