package rw.gov.erp.payroll.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import rw.gov.erp.payroll.enums.PayslipStatus;

import java.math.BigDecimal;

/**
 * Response DTO for a computed payslip - shown to employee or manager
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayslipResponse {

    private Long id;

    // Employee info
    private String employeeCode;
    private String employeeFullName;
    private String department;
    private String position;

    // Period
    private Integer month;
    private Integer year;
    private String monthName;  // e.g. "DECEMBER"

    // Salary breakdown
    private BigDecimal baseSalary;
    private BigDecimal houseAmount;       // 14% of base (benefit)
    private BigDecimal transportAmount;   // 14% of base (benefit)
    private BigDecimal grossSalary;       // base + house + transport

    // Deductions
    private BigDecimal employeeTax;       // 30%
    private BigDecimal pension;           // 6%
    private BigDecimal medicalInsurance;  // 5%
    private BigDecimal otherDeductions;   // 5%
    private BigDecimal totalDeductions;

    // Net
    private BigDecimal netSalary;

    // Status
    private PayslipStatus status;
}
