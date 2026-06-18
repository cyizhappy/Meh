package rw.gov.erp.payroll.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for creating or updating employment (professional info)
 */
@Data
public class EmploymentRequest {

    @NotBlank(message = "Department is required")
    private String department;

    @NotBlank(message = "Position is required")
    private String position;

    @NotNull(message = "Base salary is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Salary must be greater than 0")
    private BigDecimal baseSalary;

    /**
     * Status: ACTIVE or INACTIVE
     * Only ACTIVE employees are included in payroll
     */
    @NotBlank(message = "Status is required (ACTIVE or INACTIVE)")
    private String status;

    @NotNull(message = "Joining date is required")
    private LocalDate joiningDate;

    @NotNull(message = "Employee ID is required")
    private Long employeeId;
}
