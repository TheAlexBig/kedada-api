package com.kedada.backend.common.validation;

import java.time.OffsetDateTime;

public interface DateRangeProvider {
    OffsetDateTime startDate();
    OffsetDateTime endDate();
}
