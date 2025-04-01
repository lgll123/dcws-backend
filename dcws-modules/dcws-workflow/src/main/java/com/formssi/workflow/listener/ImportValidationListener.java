package com.formssi.workflow.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.formssi.common.core.exception.ServiceException;
import com.formssi.workflow.domain.RowError;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ImportValidationListener<T> extends AnalysisEventListener<T> {

    private final List<RowError> errors = new ArrayList<>();
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Override
    public void invoke(T data, AnalysisContext context) {
        Set<ConstraintViolation<T>> violations = validator.validate(data);
        if (!violations.isEmpty())  {
            int rowNum = context.readRowHolder().getRowIndex()  + 1; // 转1-based行号
            errors.add(new  RowError(rowNum, violations.iterator().next().getMessage()));
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        if (!errors.isEmpty())  {
            throw new ServiceException(
                    errors.stream()
                            .map(e -> "第" + e.getRowNum()  + "行: " + e.getMessage())
                            .collect(Collectors.joining(" ；\n")));
        }
    }
}
