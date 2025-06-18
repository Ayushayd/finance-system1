package com.personalfinance.finance_system.service.impl.integration_test;

import com.personalfinance.finance_system.dto.FinancialReportResponse;
import com.personalfinance.finance_system.model.*;
import com.personalfinance.finance_system.repository.*;
import com.personalfinance.finance_system.service.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class AdminServiceImplIntegrationTest {

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private IncomeRepository incomeRepository;

    @Autowired
    private LimitRepository limitRepository;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setUsername("user1");
        user1.setPassword("password");
        user1.setRole(Role.USER);
        userRepository.save(user1);

        user2 = new User();
        user2.setUsername("user2");
        user2.setPassword("password");
        user2.setRole(Role.USER);
        userRepository.save(user2);

        // Expenses
        expenseRepository.save(new Expense("Food", 50.0, LocalDate.now(), "Lunch", user1));
        expenseRepository.save(new Expense("Travel", 150.0, LocalDate.now(), "Taxi", user1));
        expenseRepository.save(new Expense("Food", 200.0, LocalDate.now(), "Dinner", user2));

        // Incomes
        Income income1 = new Income();
        income1.setAmount(1000.0);
        income1.setSource("Salary");
        income1.setDate(LocalDate.now());
        income1.setUser(user1);
        incomeRepository.save(income1);

        Income income2 = new Income();
        income2.setAmount(2000.0);
        income2.setSource("Freelance");
        income2.setDate(LocalDate.now());
        income2.setUser(user2);
        incomeRepository.save(income2);

        // Expense limits
        limitRepository.save(new Limit(100.0, user1)); // user1 exceeded
        limitRepository.save(new Limit(500.0, user2)); // user2 not exceeded
    }

    @Test
    void testGetOverview() {
        Map<String, Object> overview = adminService.getOverview();

        assertEquals(2, overview.size());

        // Further asserts (optional but recommended)
        assertTrue(overview.containsKey("totalExpense"));
        assertTrue(overview.containsKey("categorySummary"));

        assertEquals(400.0, overview.get("totalExpense")); // Example expected total
        Map<String, Double> categorySummary = (Map<String, Double>) overview.get("categorySummary");
        assertEquals(250.0, categorySummary.get("Food"));
        assertEquals(150.0, categorySummary.get("Travel"));
    }


    @Test
    void testGetAllUsers() {
        List<User> users = adminService.getAllUsers();
        assertTrue(users.size() >= 2);
    }

    @Test
    void testGetUserExpenses() {
        List<Expense> expenses = adminService.getUserExpenses(user1.getId());
        assertEquals(2, expenses.size());
    }

    @Test
    void testGetUsersExceededLimit() {
        List<User> exceededUsers = adminService.getUsersExceededLimit();
        assertEquals(1, exceededUsers.size());
        assertEquals(user1.getId(), exceededUsers.get(0).getId());
    }

    @Test
    void testGetExpensesGroupedByCategory() {
        // Act
        Map<String, Double> categorySummary = adminService.getExpensesGroupedByCategory();

        // Assert
        assertEquals(2, categorySummary.size());  // Assuming you added 2 categories: "Food" and "Travel"

        // Check the actual totals based on your test data
        assertEquals(250.0, categorySummary.get("Food"));   // 50 + 200
        assertEquals(150.0, categorySummary.get("Travel")); // one entry with 150
    }

    @Test
    void testGetSystemFinancialReport() {
        FinancialReportResponse report = adminService.getSystemFinancialReport();
        assertEquals(3000.0, report.getTotalIncome());
        assertEquals(400.0, report.getTotalExpense());
        assertEquals(2600.0, report.getBalance());
        assertEquals(2, report.getExpenseByCategory().size());
    }

    @Test
    void testGetAllExpenses() {
        var expenses = adminService.getAllExpenses();

        assertEquals(3, expenses.size()); // 3 expenses total

        var expense = expenses.get(0);
        assertNotNull(expense.getUserId());
        assertNotNull(expense.getUsername());
        assertTrue(expense.getAmount() > 0);
    }

    @Test
    void testGetAllIncomes() {
        var incomes = adminService.getAllIncomes();

        assertEquals(2, incomes.size());

        var income = incomes.get(0);
        assertNotNull(income.getUserId());
        assertNotNull(income.getUsername());
        assertNotNull(income.getAmount());
        assertNotNull(income.getMonthlyLimit());
    }

    @Test
    void testSetExpenseLimit() {
        double newLimit = 300.0;

        var response = adminService.setExpenseLimit(user1.getId(), newLimit);

        assertEquals(newLimit, response.getMonthlyLimit());

        var updatedLimit = limitRepository.findByUser(user1).orElse(null);
        assertNotNull(updatedLimit);
        assertEquals(newLimit, updatedLimit.getLimitAmount());
    }


}
