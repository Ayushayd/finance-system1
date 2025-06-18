package com.personalfinance.finance_system.service.impl.integration_test;

import com.personalfinance.finance_system.dto.*;
import com.personalfinance.finance_system.exception.ResourceNotFoundException;
import com.personalfinance.finance_system.model.*;
import com.personalfinance.finance_system.repository.*;
import com.personalfinance.finance_system.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private IncomeRepository incomeRepository;

    @Autowired
    private LimitRepository limitRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Clean up all repositories before each test
        limitRepository.deleteAll();
        expenseRepository.deleteAll();
        incomeRepository.deleteAll();
        userRepository.deleteAll();

        // Create and save a test user
        testUser = new User();
        testUser.setUsername("integrationTestUser");
        testUser.setPassword("encodedPass");
        testUser.setRole(Role.USER);
        testUser = userRepository.save(testUser);
    }

    @Test
    void getOverview_shouldReturnCorrectSummary() {
        // Add expenses for user
        Expense e1 = new Expense(null, 100.0, LocalDate.now(), "desc1", testUser);
        Expense e2 = new Expense(null, 200.0, LocalDate.now(), "desc2", testUser);
        e1.setCategory("Food");
        e2.setCategory("Travel");
        expenseRepository.save(e1);
        expenseRepository.save(e2);

        Map<String, Object> overview = userService.getOverview(testUser.getUsername());

        assertNotNull(overview);
        assertEquals(300.0, (Double) overview.get("totalExpense"));
        Map<String, Double> categorySummary = (Map<String, Double>) overview.get("categorySummary");
        assertEquals(2, categorySummary.size());
        assertEquals(100.0, categorySummary.get("Food"));
        assertEquals(200.0, categorySummary.get("Travel"));
    }

    @Test
    void getChartData_shouldReturnMonthlyExpense() {
        Expense e1 = new Expense(null, 50.0, LocalDate.of(2025, 6, 1), "desc1", testUser);
        Expense e2 = new Expense(null, 150.0, LocalDate.of(2025, 6, 15), "desc2", testUser);
        Expense e3 = new Expense(null, 200.0, LocalDate.of(2025, 5, 10), "desc3", testUser);
        expenseRepository.save(e1);
        expenseRepository.save(e2);
        expenseRepository.save(e3);

        Map<String, Object> chartData = userService.getChartData(testUser.getUsername());

        assertNotNull(chartData);
        Map<String, Double> monthlyExpense = (Map<String, Double>) chartData.get("monthlyExpense");

        assertEquals(2, monthlyExpense.size());
        assertEquals(200.0, monthlyExpense.get("2025-05"));
        assertEquals(200.0, monthlyExpense.get("2025-06"), 0.001);  // 50 + 150
    }

    @Test
    void setLimit_and_getLimit_shouldWorkCorrectly() {
        ExpenseLimitRequest limitRequest = new ExpenseLimitRequest();
        limitRequest.setMonthlyLimit(500.0);

        Limit savedLimit = userService.setLimit(testUser.getUsername(), limitRequest);

        assertNotNull(savedLimit);
        assertEquals(500.0, savedLimit.getLimitAmount());
        assertEquals(testUser.getId(), savedLimit.getUser().getId());

        ExpenseLimitResponse fetchedLimit = userService.getLimit(testUser.getUsername());
        assertEquals(500.0, fetchedLimit.getMonthlyLimit());
    }

    @Test
    void getLimit_whenLimitNotFound_shouldThrowException() {
        assertThrows(ResourceNotFoundException.class, () -> userService.getLimit(testUser.getUsername()));
    }

    @Test
    void addExpense_and_getAllExpenses_shouldWork() {
        ExpenseRequest expenseRequest = new ExpenseRequest();
        expenseRequest.setAmount(123.45);
        expenseRequest.setCategory("Health");
        expenseRequest.setDate(LocalDate.now());
        expenseRequest.setDescription("Doctor appointment");

        Expense savedExpense = userService.addExpense(testUser.getUsername(), expenseRequest);
        assertNotNull(savedExpense.getId());
        assertEquals(123.45, savedExpense.getAmount());
        assertEquals("Health", savedExpense.getCategory());

        List<ExpenseResponse> expenses = userService.getAllExpenses(testUser.getUsername());
        assertFalse(expenses.isEmpty());
        assertEquals(1, expenses.size());
        assertEquals("Health", expenses.get(0).getCategory());  // optional deeper check

    }

    @Test
    void addIncome_and_getFinancialReport_shouldWork() {
        // Add income
        IncomeRequest incomeRequest = new IncomeRequest();
        incomeRequest.setAmount(1000.0);
        incomeRequest.setSource("Salary");
        incomeRequest.setDate(LocalDate.now());
        Income income = userService.addIncome(testUser.getUsername(), incomeRequest);

        // Add expense
        ExpenseRequest expenseRequest = new ExpenseRequest();
        expenseRequest.setAmount(200.0);
        expenseRequest.setCategory("Bills");
        expenseRequest.setDate(LocalDate.now());
        expenseRequest.setDescription("Electricity bill");
        userService.addExpense(testUser.getUsername(), expenseRequest);

        FinancialReportResponse report = userService.getFinancialReport(testUser.getUsername());

        assertEquals(1000.0, report.getTotalIncome());
        assertEquals(200.0, report.getTotalExpense());
        assertEquals(800.0, report.getBalance());

        Map<String, Double> categorySummary = report.getExpenseByCategory();
        assertEquals(1, categorySummary.size());
        assertEquals(200.0, categorySummary.get("Bills"));
    }

    @Test
    void getOverview_whenUserNotFound_shouldThrowException() {
        assertThrows(ResourceNotFoundException.class, () -> userService.getOverview("nonExistingUser"));
    }

    @Test
    void addExpense_whenUserNotFound_shouldThrowException() {
        ExpenseRequest expenseRequest = new ExpenseRequest();
        expenseRequest.setAmount(50.0);
        expenseRequest.setCategory("Misc");
        expenseRequest.setDate(LocalDate.now());
        expenseRequest.setDescription("Misc expense");

        assertThrows(ResourceNotFoundException.class, () -> userService.addExpense("invalidUser", expenseRequest));
    }

    @Test
    void addIncome_and_getIncome_shouldReturnIncomeList() {
        IncomeRequest request = new IncomeRequest();
        request.setAmount(1200.0);
        request.setDate(LocalDate.of(2025, 6, 1));
        request.setSource("Freelance");

        userService.addIncome(testUser.getUsername(), request);

        List<IncomeResponseDTO> incomes = userService.getIncome(testUser.getUsername());
        assertEquals(1, incomes.size());
        assertEquals("Freelance", incomes.get(0).getSource());
    }

    @Test
    void updateIncome_shouldUpdateCorrectly() {
        IncomeRequest req = new IncomeRequest(1000.0, "Bonus", LocalDate.now());
        Income income = userService.addIncome(testUser.getUsername(), req);

        IncomeRequest update = new IncomeRequest(1100.0, "Updated Bonus", LocalDate.now());
        IncomeResponseDTO updated = userService.updateIncome(testUser.getUsername(), income.getId(), update);

        assertEquals(1100.0, updated.getAmount());
        assertEquals("Updated Bonus", updated.getSource());
    }

    @Test
    void updateIncome_withWrongUser_shouldThrowException() {
        Income income = userService.addIncome(testUser.getUsername(),
                new IncomeRequest(500.0, "Gift", LocalDate.now()));

        assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateIncome("invalidUser", income.getId(), new IncomeRequest(600.0, "Edited", LocalDate.now()));
        });
    }

    @Test
    void deleteIncome_shouldRemoveIncome() {
        Income income = userService.addIncome(testUser.getUsername(), new IncomeRequest(200.0, "Side Job", LocalDate.now()));
        userService.deleteIncome(testUser.getUsername(), income.getId());

        List<IncomeResponseDTO> incomes = userService.getIncome(testUser.getUsername());
        assertTrue(incomes.isEmpty());
    }

    @Test
    void deleteIncome_withInvalidUser_shouldThrowException() {
        Income income = userService.addIncome(testUser.getUsername(), new IncomeRequest(300.0, "Gift", LocalDate.now()));

        assertThrows(ResourceNotFoundException.class, () -> {
            userService.deleteIncome("invalidUser", income.getId());
        });
    }

}
