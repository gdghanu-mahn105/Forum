package com.example.forum.core.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class EmailLoggingAspect {

    @Pointcut(value = "execution(* com.example.forum.service.EmailService.*(..))")
    public void emailServiceMethods(){}

    @Before("emailServiceMethods()")
    public void logBefore(JoinPoint joinPoint){
        log.info("[Email] Initiating email request: '{}' with arguments: {}",
                joinPoint.getSignature().getName(),
                Arrays.toString(joinPoint.getArgs()
                ));
    }

    @AfterReturning("emailServiceMethods()")
    public void logSuccess(JoinPoint joinPoint) {
        log.info("[EMAIL] Email request processed successfully: '{}'",
                joinPoint.getSignature().getName());
    }

    @AfterThrowing(pointcut = "emailServiceMethods()", throwing = "ex")
    public void logError(JoinPoint joinPoint, Exception ex){
        log.error("[EMAIL] Failed to process email request: '{}'. Error reason: {}",
                joinPoint.getSignature().getName(),
                ex.getMessage());
    }
}
