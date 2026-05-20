package com.kedada.backend.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidDateRangeValidator implements ConstraintValidator<ValidDateRange, DateRangeProvider> {

    @Override
    public boolean isValid(DateRangeProvider value, ConstraintValidatorContext context) {
        if (value == null || value.startDate() == null || value.endDate() == null) {
            return true;
        }
        return value.endDate().isAfter(value.startDate());
    }
}
