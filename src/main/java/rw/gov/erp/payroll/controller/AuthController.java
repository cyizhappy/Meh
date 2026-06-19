package rw.gov.erp.payroll.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.gov.erp.payroll.dto.request.LoginRequest;
import rw.gov.erp.payroll.dto.request.RegisterRequest;
import rw.gov.erp.payroll.dto.response.ApiResponse;
import rw.gov.erp.payroll.dto.response.AuthResponse;
import rw.gov.erp.payroll.service.AuthService;

/**
 * Authentication endpoints – publicly accessible (no JWT required)
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Login and Register are public. Default ADMIN is pre-seeded (admin@rca.ac.rw / admin123)")
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "Register a new employee (Public endpoint)",
            description = """
                    **Self-Registration Flow:**
                    1. Creates a **User** account (login credentials, forced to EMPLOYEE role)
                    2. Creates an **Employee** profile (personal information)
                    3. Creates an **Employment** record with status **PENDING**
                    
                    The registered employee can immediately log in and view their profile via `/api/employees/me`,
                    but they will NOT appear in payroll until an admin approves them.
                    
                    **Admin Approval:** The admin updates the employment record (assigns department, position, salary)
                    and changes the status from PENDING → ACTIVE via `PUT /api/employment/{id}`.
                    
                    **Admin Registration:** If an authenticated ADMIN calls this endpoint, they can optionally
                    assign the ADMIN role. Employee + Employment records are still auto-created.
                    """
    )
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully. Employment status: PENDING (awaiting admin approval)", response));
    }

    @Operation(summary = "Login and receive JWT token")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }
}
