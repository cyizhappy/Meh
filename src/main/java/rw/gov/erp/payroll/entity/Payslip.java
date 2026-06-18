package rw.gov.erp.payroll.entity;

import jakarta.persistence.*;
import lombok.*;
import rw.gov.erp.payroll.enums.PayslipStatus;

import java.math.BigDecimal;

/**
 * Payslip entity – stores computed salary details for one employee per month/year.
 * Task 4: Payroll computation result per employee.
 *
 * Computation rules:
 *   grossSalary  = baseSalary + houseAmount + transportAmount
 *   netSalary    = baseSalary - (taxAmount + pensionAmount + medicalAmount + othersAmount)
 *   totalDeductions = taxAmount + pensionAmount + medicalAmount + othersAmount
 *
 * Task 5: When ADMIN approves payroll → status changes to PAID →
 *         PostgreSQL trigger fires → inserts message into messages table
 */
@Entity
@Table(
        name = "payslips",
        uniqueConstraints = {
                // Prevent duplicate payroll: one payslip per employee per month/year
                @UniqueConstraint(
                        name = "uk_payslip_employee_month_year",
                        columnNames = {"employment_id", "month", "year"}
                )
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payslip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employment_id", nullable = false)
    private Employment employment;

    // ── Raw base salary snapshot at time of generation ──
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal baseSalary;

    // ── Benefits (added to gross) ──
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal houseAmount;       // baseSalary * 14%

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal transportAmount;   // baseSalary * 14%

    // ── Gross Salary ──
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal grossSalary;       // baseSalary + house + transport

    // ── Deductions (subtracted from baseSalary for net) ──
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal employeeTax;       // baseSalary * 30%

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal pension;           // baseSalary * 6%

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal medicalInsurance;  // baseSalary * 5%

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal otherDeductions;   // baseSalary * 5%

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalDeductions;   // sum of all deductions

    // ── Net Salary ──
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal netSalary;         // baseSalary - totalDeductions

    // ── Period ──
    @Column(nullable = false)
    private Integer month;  // 1-12

    @Column(nullable = false)
    private Integer year;

    // ── Status (PENDING → PAID when ADMIN approves) ──
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PayslipStatus status;
}
