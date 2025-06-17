package com.personalfinance.finance_system.service;

import com.personalfinance.finance_system.dto.ExpenseLimitRequest;
import com.personalfinance.finance_system.model.Limit;

import java.util.Map;

public interface UserService {

    Map<String, Object> getOverview(String username);

    Map<String, Object> getChartData(String username);

    Limit setLimit(String username, ExpenseLimitRequest request);

    Limit getLimit(String username);
}
