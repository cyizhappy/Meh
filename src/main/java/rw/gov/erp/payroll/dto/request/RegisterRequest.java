package rw.gov.erp.payroll.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

/**
 * Request DTO for user registration.
 *
 * When someone registers via /api/auth/register, the system creates:
 *   1. A User account (login credentials)
 *   2. An Employee profile (personal information)
 *   3. An Employment record with status PENDING (awaiting admin approval)
 *
 * The admin can later approve the employee by updating their employment
 * status from PENDING → ACTIVE and assigning department/position/salary.
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "First name is required")
    @Schema(description = "Employee first name", example = "Jean")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Schema(description = "Employee last name", example = "Uwimana")
    private String lastName;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    @Schema(description = "Email address (used for login)", example = "jean.uwimana@rca.ac.rw")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @Schema(description = "Login password (min 6 characters)", example = "securePass123")
    private String password;

    // ── Employee personal information ──────────────────────────

    @NotBlank(message = "District is required")
    @Schema(description = "District of residence", example = "Gasabo")
    private String district;

    @NotBlank(message = "Mobile number is required")
    @Schema(description = "Mobile phone number", example = "+250788123456")
    private String mobile;

    @NotNull(message = "Date of birth is required")
    @Schema(description = "Date of birth (YYYY-MM-DD)", example = "1998-05-15")
    private LocalDate dateOfBirth;

    @Schema(
            description = "User role. Ignored for anonymous registration (always coerced to EMPLOYEE). " +
                          "Only an authenticated ADMIN can assign ADMIN role when creating accounts.",
            example = "EMPLOYEE",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String role;
}
