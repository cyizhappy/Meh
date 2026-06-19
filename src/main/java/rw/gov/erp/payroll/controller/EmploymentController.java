package rw.gov.erp.payroll.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import rw.gov.erp.payroll.dto.request.EmploymentRequest;
import rw.gov.erp.payroll.dto.response.ApiResponse;
import rw.gov.erp.payroll.entity.Employment;
import rw.gov.erp.payroll.service.EmploymentService;

import java.util.List;

/**
 * Task 1 – Employment Management Controller
 * Handles professional/employment information (department, position, salary, status)
 */
@RestController
@RequestMapping("/api/employment")
@RequiredArgsConstructor
@Tag(name = "Employment Management", description = "Task 1 - Manage employment/professional information")
@SecurityRequirement(name = "bearerAuth")
public class EmploymentController {

    private final EmploymentService employmentService;

    @Operation(summary = "Create employment record for an employee (ADMIN only)")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<Employment>> createEmployment(
            @Valid @RequestBody EmploymentRequest request) {
        Employment employment = employmentService.createEmployment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Employment record created successfully", employment));
    }

    @Operation(summary = "Get all employment records (ADMIN only)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<Employment>>> getAllEmployments() {
        return ResponseEntity.ok(ApiResponse.success("Employment records retrieved",
                employmentService.getAllEmployments()));
    }
 
    @Operation(summary = "Get active employment records only (used in payroll) (ADMIN only)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<Employment>>> getActiveEmployments() {
        return ResponseEntity.ok(ApiResponse.success("Active employment records retrieved",
                employmentService.getActiveEmployments()));
    }

    @Operation(
        summary = "Get PENDING employment records (ADMIN only)",
        description = "Lists employees who self-registered and are waiting for admin approval. " +
                      "Admin should review, assign department/position/salary, then call PUT /approve/{id}."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<Employment>>> getPendingEmployments() {
        return ResponseEntity.ok(ApiResponse.success("Pending employment records retrieved",
                employmentService.getPendingEmployments()));
    }

    @Operation(summary = "Get employment record by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Employment>> getEmployment(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Employment retrieved",
                employmentService.getEmploymentById(id)));
    }

    @Operation(summary = "Get employment record by employee ID")
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponse<Employment>> getByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(ApiResponse.success("Employment retrieved",
                employmentService.getEmploymentByEmployeeId(employeeId)));
    }

    @Operation(
        summary = "Approve a PENDING employee (ADMIN only)",
        description = "Changes employment status from PENDING → ACTIVE. " +
                      "The admin should first update the employee's department, position, and salary " +
                      "via PUT /api/employment/{id}, then approve them here."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/approve/{id}")
    public ResponseEntity<ApiResponse<Employment>> approveEmployment(@PathVariable Long id) {
        Employment employment = employmentService.approveEmployment(id);
        return ResponseEntity.ok(ApiResponse.success("Employee approved successfully! Status changed to ACTIVE.", employment));
    }

    @Operation(summary = "Update employment record (ADMIN only)")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Employment>> updateEmployment(
            @PathVariable Long id,
            @Valid @RequestBody EmploymentRequest request) {
        Employment employment = employmentService.updateEmployment(id, request);
        return ResponseEntity.ok(ApiResponse.success("Employment updated successfully", employment));
    }

    @Operation(summary = "Delete employment record (ADMIN only)")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEmployment(@PathVariable Long id) {
        employmentService.deleteEmployment(id);
        return ResponseEntity.ok(ApiResponse.success("Employment deleted successfully", null));
    }
}
