package com.formssi.workflow.common.annotation;


import com.formssi.workflow.listener.FixedValueValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FixedValueValidator.class)
public @interface FixedValues {
    String[] value(); // 允许的固定值集合
    String message() default "输入值不在允许范围内";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
