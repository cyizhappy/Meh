package rw.gov.erp.payroll.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Deduction entity – stores each deduction type and its percentage.
 * Task 2: Required deductions:
 *   1. Employee Tax      → 30%
 *   2. Pension           → 6%
 *   3. Medical Insurance → 5%
 *   4. Others            → 5%
 *   5. House             → 14%  (benefit added to gross)
 *   6. Transport         → 14%  (benefit added to gross)
 */
@Entity
@Table(name = "deductions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Deduction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Deduction name e.g. "EmployeeTax", "Pension", "MedicalInsurance" */
    @NotBlank(message = "Deduction name is required")
    @Column(nullable = false, unique = true)
    private String deductionName;

    /**
     * Percentage as a decimal value (e.g. 30 for 30%)
     * Must be between 0 and 100
     */
    @NotNull(message = "Percentage is required")
    @DecimalMin(value = "0.0", message = "Percentage cannot be negative")
    @DecimalMax(value = "100.0", message = "Percentage cannot exceed 100")
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal percentage;
}
