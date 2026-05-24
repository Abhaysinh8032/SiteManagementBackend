package com.abhay.management.site.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Stored as the principal in Spring Security's SecurityContext after JWT validation.
 * Accessible in controllers via @AuthenticationPrincipal EmployeePrincipal.
 */
@Getter
@AllArgsConstructor
public class EmployeePrincipal {


	private String userId;
	private String employeeId;
	private String role;
}
