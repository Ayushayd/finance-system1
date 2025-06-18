package com.personalfinance.finance_system.repository;

import com.personalfinance.finance_system.model.Limit;
import com.personalfinance.finance_system.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LimitRepository extends JpaRepository<Limit, Long> {
//    Optional<Limit> findByUserId(Long userId);
    Optional<Limit> findByUser(User user);
}