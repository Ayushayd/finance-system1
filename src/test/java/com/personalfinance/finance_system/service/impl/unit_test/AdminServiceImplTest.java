package com.personalfinance.finance_system.service.impl.unit_test;

import com.personalfinance.finance_system.dto.ExpenseLimitResponse;
import com.personalfinance.finance_system.dto.ExpenseResponse;
import com.personalfinance.finance_system.dto.FinancialReportResponse;
import com.personalfinance.finance_system.dto.IncomeWithUserLimitDTO;
import com.personalfinance.finance_system.exception.ResourceNotFoundException;
import com.personalfinance.finance_system.model.*;
import com.personalfinance.finance_system.repository.ExpenseRepository;
import com.personalfinance.finance_system.repository.IncomeRepository;
import com.personalfinance.finance_system.repository.LimitRepository;
import com.personalfinance.finance_system.repository.UserRepository;
import com.personalfinance.finance_system.service.impl.AdminServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private LimitRepository limitRepository;

    @Mock
    private IncomeRepository incomeRepository;

    @InjectMocks
    private AdminServiceImpl adminService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getOverview_shouldReturnTotalExpenseAndCategorySummary() {
        Expense e1 = new Expense();
        e1.setAmount(100.0);
        e1.setCategory("Food");

        Expense e2 = new Expense();
        e2.setAmount(200.0);
        e2.setCategory("Transport");

        when(expenseRepository.findAll()).thenReturn(List.of(e1, e2));

        Map<String, Object> overview = adminService.getOverview();

        assertEquals(300.0, (Double) overview.get("totalExpense"));
        Map<String, Double> categorySummary = (Map<String, Double>) overview.get("categorySummary");
        assertEquals(100.0, categorySummary.get("Food"));
        assertEquals(200.0, categorySummary.get("Transport"));
    }

    @Test
    void getAllUsers_shouldReturnListOfUsers() {
        List<User> users = List.of(new User(), new User());
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = adminService.getAllUsers();
        assertEquals(2, result.size());
    }

    @Test
    void getUserExpenses_shouldReturnUserExpenses() {
        User user = new User();
        user.setId(1L);

        Expense e = new Expense();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(expenseRepository.findByUser(user)).thenReturn(List.of(e));

        List<Expense> result = adminService.getUserExpenses(1L);
        assertEquals(1, result.size());
    }

    @Test
    void getUserExpenses_userNotFound_shouldThrowException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adminService.getUserExpenses(1L));
    }

    @Test
    void getUsersExceededLimit_shouldReturnUsersWithExceededExpenses() {
        User user = new User();
        user.setId(1L);

        Limit limit = new Limit();
        limit.setUser(user);
        limit.setLimitAmount(100.0);

        Expense expense = new Expense();
        expense.setAmount(150.0);

        when(limitRepository.findAll()).thenReturn(List.of(limit));
        when(expenseRepository.findByUserAndDateBetween(eq(user), any(), any()))
                .thenReturn(List.of(expense));

        List<User> result = adminService.getUsersExceededLimit();
        assertEquals(1, result.size());
        assertEquals(user, result.get(0));
    }

    @Test
    void getExpensesGroupedByCategory_shouldReturnMapOfCategorySums() {
        Expense e1 = new Expense();
        e1.setCategory("Food");
        e1.setAmount(50.0);

        Expense e2 = new Expense();
        e2.setCategory("Food");
        e2.setAmount(70.0);

        Expense e3 = new Expense();
        e3.setCategory("Travel");
        e3.setAmount(30.0);

        when(expenseRepository.findAll()).thenReturn(List.of(e1, e2, e3));

        Map<String, Double> result = adminService.getExpensesGroupedByCategory();
        assertEquals(2, result.size());
        assertEquals(120.0, result.get("Food"));
        assertEquals(30.0, result.get("Travel"));
    }

    @Test
    void getSystemFinancialReport_shouldReturnCorrectData() {
        Income i1 = new Income();
        i1.setAmount(1000.0);
        Income i2 = new Income();
        i2.setAmount(500.0);

        Expense e1 = new Expense();
        e1.setAmount(300.0);
        e1.setCategory("Utilities");
        Expense e2 = new Expense();
        e2.setAmount(200.0);
        e2.setCategory("Food");

        when(incomeRepository.findAll()).thenReturn(List.of(i1, i2));
        when(expenseRepository.findAll()).thenReturn(List.of(e1, e2));

        FinancialReportResponse report = adminService.getSystemFinancialReport();

        assertEquals(1500.0, report.getTotalIncome());
        assertEquals(500.0, report.getTotalExpense());
        assertEquals(1000.0, report.getBalance());
        assertEquals(2, report.getExpenseByCategory().size());
        assertEquals(300.0, report.getExpenseByCategory().get("Utilities"));
    }

    @Test
    void getAllExpenses_shouldReturnOnlyUserExpenses() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setRole(Role.USER);

        Expense e = new Expense();
        e.setId(101L);
        e.setAmount(100.0);
        e.setDescription("Lunch");
        e.setCategory("Food");
        e.setDate(LocalDate.now());
        e.setUser(user);

        when(expenseRepository.findAll()).thenReturn(List.of(e));

        List<ExpenseResponse> result = adminService.getAllExpenses();

        assertEquals(1, result.size());
        ExpenseResponse response = result.get(0);
        assertEquals("testuser", response.getUsername());
        assertEquals("Lunch", response.getDescription());
    }

    @Test
    void getAllIncomes_shouldReturnIncomeWithUserAndLimit() {
        User user = new User();
        user.setId(1L);
        user.setUsername("john");

        Income income = new Income();
        income.setId(100L);
        income.setAmount(1000.0);
        income.setSource("Job");
        income.setDate(LocalDate.now());
        income.setUser(user);

        Limit limit = new Limit();
        limit.setUser(user);
        limit.setLimitAmount(500.0);

        when(incomeRepository.findAll()).thenReturn(List.of(income));
        when(limitRepository.findByUser(user)).thenReturn(Optional.of(limit));

        List<IncomeWithUserLimitDTO> result = adminService.getAllIncomes();

        assertEquals(1, result.size());
        assertEquals(500.0, result.get(0).getMonthlyLimit());
        assertEquals("john", result.get(0).getUsername());
    }

    @Test
    void getAllIncomes_shouldReturnNullForMissingLimit() {
        User user = new User();
        user.setId(1L);
        user.setUsername("john");

        Income income = new Income();
        income.setId(100L);
        income.setAmount(1000.0);
        income.setSource("Freelance");
        income.setDate(LocalDate.now());
        income.setUser(user);

        when(incomeRepository.findAll()).thenReturn(List.of(income));
        when(limitRepository.findByUser(user)).thenReturn(Optional.empty());

        List<IncomeWithUserLimitDTO> result = adminService.getAllIncomes();

        assertEquals(1, result.size());
        assertNull(result.get(0).getMonthlyLimit());
    }

    @Test
    void setExpenseLimit_shouldUpdateExistingLimit() {
        User user = new User();
        user.setId(1L);

        Limit limit = new Limit();
        limit.setUser(user);
        limit.setLimitAmount(200.0);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(limitRepository.findByUser(user)).thenReturn(Optional.of(limit));

        ExpenseLimitResponse response = adminService.setExpenseLimit(1L, 500.0);

        assertEquals(500.0, response.getMonthlyLimit());
        verify(limitRepository).save(limit);
    }

    @Test
    void setExpenseLimit_shouldCreateNewLimitIfNotExists() {
        User user = new User();
        user.setId(2L);

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(limitRepository.findByUser(user)).thenReturn(Optional.empty());

        ExpenseLimitResponse response = adminService.setExpenseLimit(2L, 300.0);

        assertEquals(300.0, response.getMonthlyLimit());
        verify(limitRepository).save(any(Limit.class));
    }

    @Test
    void setExpenseLimit_userNotFound_shouldThrowException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adminService.setExpenseLimit(1L, 100.0));
    }


}
