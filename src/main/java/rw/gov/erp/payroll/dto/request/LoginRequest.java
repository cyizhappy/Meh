package rw.gov.erp.payroll.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * Request DTO for login
 */
@Data
public class LoginRequest {
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;
}
