package com.personalfinance.finance_system.service.impl.unit_test;

import com.personalfinance.finance_system.dto.*;
import com.personalfinance.finance_system.exception.ResourceNotFoundException;
import com.personalfinance.finance_system.model.*;
import com.personalfinance.finance_system.repository.*;
import com.personalfinance.finance_system.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock private UserRepository userRepository;
    @Mock private ExpenseRepository expenseRepository;
    @Mock private IncomeRepository incomeRepository;
    @Mock private LimitRepository limitRepository;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
    }

    @Test
    void testGetOverview() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        List<Expense> expenses = List.of(
                new Expense("Food", 100.0, LocalDate.now(), "Lunch", user),
                new Expense("Travel", 200.0, LocalDate.now(), "Bus", user),
                new Expense("Food", 50.0, LocalDate.now(), "Dinner", user)
        );

        when(expenseRepository.findByUser(user)).thenReturn(expenses);

        Map<String, Object> result = userService.getOverview("testuser");

        assertEquals(350.0, result.get("totalExpense"));
        Map<String, Double> categorySummary = (Map<String, Double>) result.get("categorySummary");
        assertEquals(150.0, categorySummary.get("Food"));
        assertEquals(200.0, categorySummary.get("Travel"));
    }

    @Test
    void testGetChartData() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        LocalDate date = LocalDate.of(2024, 6, 1);
        List<Expense> expenses = List.of(
                new Expense("Food", 100.0, date, "Lunch", user),
                new Expense("Travel", 200.0, date, "Train", user)
        );

        when(expenseRepository.findByUser(user)).thenReturn(expenses);

        Map<String, Object> result = userService.getChartData("testuser");

        Map<String, Double> monthlyExpense = (Map<String, Double>) result.get("monthlyExpense");
        assertEquals(300.0, monthlyExpense.get("2024-06"));
    }

    @Test
    void testSetLimit_NewLimit() {
        ExpenseLimitRequest request = new ExpenseLimitRequest();
        request.setMonthlyLimit(1000.0);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(limitRepository.findByUser(user)).thenReturn(Optional.empty());

        Limit savedLimit = new Limit();
        savedLimit.setLimitAmount(1000.0);
        savedLimit.setUser(user);

        when(limitRepository.save(any(Limit.class))).thenReturn(savedLimit);

        Limit result = userService.setLimit("testuser", request);
        assertEquals(1000.0, result.getLimitAmount());
    }

    @Test
    void testGetLimit_Exists() {
        Limit limit = new Limit();
        limit.setUser(user);
        limit.setLimitAmount(1500.0);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(limitRepository.findByUser(user)).thenReturn(Optional.of(limit));

        ExpenseLimitResponse result = userService.getLimit("testuser");

        assertEquals(1500.0, result.getMonthlyLimit());
    }


    @Test
    void testAddExpense() {
        ExpenseRequest request = new ExpenseRequest();
        request.setAmount(250.0);
        request.setCategory("Food");
        request.setDate(LocalDate.now());
        request.setDescription("Dinner");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        Expense savedExpense = new Expense("Food", 250.0, request.getDate(), "Dinner", user);

        when(expenseRepository.save(any(Expense.class))).thenReturn(savedExpense);

        Expense result = userService.addExpense("testuser", request);

        assertEquals(250.0, result.getAmount());
        assertEquals("Food", result.getCategory());
    }

    @Test
    void testAddIncome() {
        IncomeRequest request = new IncomeRequest(5000.0, "Salary", LocalDate.now());

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        Income savedIncome = new Income(
                request.getAmount(),
                request.getSource(),
                request.getDate(),
                user
        );

        when(incomeRepository.save(any(Income.class))).thenReturn(savedIncome);

        Income result = userService.addIncome("testuser", request);

        assertEquals(5000.0, result.getAmount());
        assertEquals("Salary", result.getSource());
    }



    @Test
    void testGetAllExpenses() {
        List<Expense> expenses = List.of(
                new Expense("Food", 100.0, LocalDate.now(), "Lunch", user)
        );

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(expenseRepository.findByUser(user)).thenReturn(expenses);

        List<ExpenseResponse> result = userService.getAllExpenses("testuser");
        assertEquals(1, result.size());
    }

    @Test
    void testGetFinancialReport() {
        List<Expense> expenses = List.of(
                new Expense("Food", 100.0, LocalDate.now(), "Lunch", user),
                new Expense("Travel", 150.0, LocalDate.now(), "Bus", user)
        );

        Income income = new Income(1000.0, "Job", LocalDate.now(), user);
        List<Income> incomes = List.of(income);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(expenseRepository.findByUser(user)).thenReturn(expenses);
        when(incomeRepository.findByUser(user)).thenReturn(incomes);

        FinancialReportResponse report = userService.getFinancialReport("testuser");

        assertEquals(1000.0, report.getTotalIncome());
        assertEquals(250.0, report.getTotalExpense());
        assertEquals(750.0, report.getBalance());
        assertEquals(100.0, report.getExpenseByCategory().get("Food"));
    }

    @Test
    void testGetLimit_NotFound() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(limitRepository.findByUser(user)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getLimit("testuser"));
    }

    @Test
    void testUpdateIncome_Unauthorized() {
        Income income = new Income(1000.0, "Bonus", LocalDate.now(), new User());
        income.setId(1L);
        income.getUser().setUsername("anotheruser");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(incomeRepository.findById(1L)).thenReturn(Optional.of(income));

        IncomeRequest request = new IncomeRequest(2000.0, "Bonus", LocalDate.now());

        assertThrows(ResourceNotFoundException.class, () ->
                userService.updateIncome("testuser", 1L, request));
    }

    @Test
    void testUpdateIncome_Success() {
        Income income = new Income(1000.0, "Freelance", LocalDate.of(2024, 5, 20), user);
        income.setId(1L);

        IncomeRequest request = new IncomeRequest(2000.0, "Job", LocalDate.of(2024, 6, 1));

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(incomeRepository.findById(1L)).thenReturn(Optional.of(income));
        when(incomeRepository.save(any(Income.class))).thenReturn(income);

        IncomeResponseDTO result = userService.updateIncome("testuser", 1L, request);

        assertEquals(2000.0, result.getAmount());
        assertEquals("Job", result.getSource());
    }

    @Test
    void testDeleteIncome_Unauthorized() {
        Income income = new Income(1000.0, "Job", LocalDate.now(), new User());
        income.setId(1L);
        income.getUser().setUsername("otheruser");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(incomeRepository.findById(1L)).thenReturn(Optional.of(income));

        assertThrows(ResourceNotFoundException.class, () -> userService.deleteIncome("testuser", 1L));
    }

    @Test
    void testDeleteIncome_Success() {
        Income income = new Income(1000.0, "Salary", LocalDate.now(), user);
        income.setId(1L);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(incomeRepository.findById(1L)).thenReturn(Optional.of(income));

        userService.deleteIncome("testuser", 1L);

        verify(incomeRepository, times(1)).delete(income);
    }

    @Test
    void testGetIncome_Empty() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(incomeRepository.findByUser(user)).thenReturn(Collections.emptyList());

        List<IncomeResponseDTO> result = userService.getIncome("testuser");
        assertTrue(result.isEmpty());
    }

}
