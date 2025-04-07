package com.formssi.workflow.listener;

import com.formssi.workflow.common.annotation.FixedValues;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class FixedValueValidator implements ConstraintValidator<FixedValues, String> {
    private Set<String> allowedValues;

    @Override
    public void initialize(FixedValues constraintAnnotation) {
        this.allowedValues  = new HashSet<>(Arrays.asList(constraintAnnotation.value()));

    }

//    @Override
//    public void initialize(FixedValues constraintAnnotation) {
//        // 若注解的字段，改成维护在字典表，可将校验的值改成实时查询数据库字典表
//        this.allowedValues = repository.findAllowedValues();
//    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // 允许空值（如需非空校验需配合@NotNull）
        return value == null || allowedValues.contains(value.trim());
    }
}
