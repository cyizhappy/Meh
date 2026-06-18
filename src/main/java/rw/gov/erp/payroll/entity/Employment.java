package rw.gov.erp.payroll.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import rw.gov.erp.payroll.enums.EmploymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Employment entity – stores professional/employment information.
 * Task 1: employeeCode, department, position, baseSalary, status, joiningDate
 * Note: salary here is the BASE salary used for deduction computation.
 */
@Entity
@Table(name = "employment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique employee code (e.g., EMP-001) */
    @Column(nullable = false, unique = true)
    private String employeeCode;

    @NotBlank(message = "Department is required")
    @Column(nullable = false)
    private String department;

    @NotBlank(message = "Position is required")
    @Column(nullable = false)
    private String position;

    /**
     * Base salary used for payroll computation.
     * Gross = baseSalary + house(14%) + transport(14%)
     * Net   = baseSalary - (tax + pension + medical + others)
     */
    @NotNull(message = "Base salary is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Salary must be greater than 0")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal baseSalary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmploymentStatus status;

    @NotNull(message = "Joining date is required")
    @Column(nullable = false)
    private LocalDate joiningDate;

    /** One-to-one relationship: each employment record belongs to one employee */
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", nullable = false, unique = true)
    private Employee employee;
}
