package com.abhay.management.site.enums;

public enum UserStatus {
    PENDING,    // Registered, awaiting admin approval
    ACTIVE,     // Approved, can log in fully
    REJECTED,   // Rejected by admin
    INACTIVE    // Deactivated
}
