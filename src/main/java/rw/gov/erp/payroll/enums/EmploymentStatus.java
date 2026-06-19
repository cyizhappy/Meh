package rw.gov.erp.payroll.enums;

/**
 * Employment status:
 *   PENDING  – Self-registered employee awaiting admin approval
 *   ACTIVE   – Approved by admin, included in payroll generation
 *   INACTIVE – Deactivated, excluded from payroll
 */
public enum EmploymentStatus {
    PENDING,
    ACTIVE,
    INACTIVE
}
