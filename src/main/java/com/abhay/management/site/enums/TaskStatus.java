package com.abhay.management.site.enums;

public enum TaskStatus {
    PENDING,
    IN_PROGRESS,
    REVIEW_REQUESTED,  // Worker submits for admin review before marking complete
    COMPLETED,
    ON_HOLD
}
