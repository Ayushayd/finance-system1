package com.personalfinance.finance_system.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("execution(* com.personalfinance.finance_system.controller..*(..)) || execution(* com.personalfinance.finance_system.service..*(..))")

    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object proceed = joinPoint.proceed();
        long duration = System.currentTimeMillis() - start;

        logger.info(joinPoint.getSignature() + " executed in " + duration + "ms");
        logger.debug("{} executed in {} ms", joinPoint.getSignature(), duration);

        return proceed;
    }
}
