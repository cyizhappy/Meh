package rw.gov.erp.payroll.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * Request DTO for user registration
 */
@Data
public class RegisterRequest {
    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    /** Role: ADMIN or EMPLOYEE */
    @NotBlank(message = "Role is required")
    private String role;
}
