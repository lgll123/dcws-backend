package com.formssi.chorflow.config;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * 切面 打印日志
 *
 * @author lijun
 * @date 2024.12.26
 */
@Slf4j
@Aspect
public class ExecutionTimeAOP {

  public static Map<String, Long> executionTimeMap = new HashMap<>();

  @Around("execution(* com.formssi.chorflow..*.*(..))")
  public Object measureMethodExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
    long startTime = System.nanoTime();
    Object result = joinPoint.proceed();
    long endTime = System.nanoTime();

    // 将纳秒差值转换为微秒
    long microseconds = (endTime - startTime) / 1000;
    String methodName =
        joinPoint.getSignature().getDeclaringTypeName() + joinPoint.getSignature().getName();
    log.info("{} 方法执行时间为: {} μs", methodName, microseconds);
    executionTimeMap.put(methodName, microseconds);
    return result;
  }
}