package com.personalfinance.finance_system.repository;

import com.personalfinance.finance_system.model.Income;
import com.personalfinance.finance_system.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncomeRepository extends JpaRepository<Income, Long> {

    List<Income> findByUser(User user);

}
