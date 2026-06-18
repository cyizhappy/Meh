package rw.gov.erp.payroll.enums;

/**
 * Payslip status lifecycle: PENDING → PAID
 * PENDING: Payroll generated but not yet approved by ADMIN
 * PAID: Approved by ADMIN; trigger fires and inserts message
 */
public enum PayslipStatus {
    PENDING,
    PAID
}
