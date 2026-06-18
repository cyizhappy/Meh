package rw.gov.erp.payroll.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
    @PostMapping
    public ResponseEntity<ApiResponse<Employment>> createEmployment(
            @Valid @RequestBody EmploymentRequest request) {
        Employment employment = employmentService.createEmployment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Employment record created successfully", employment));
    }

    @Operation(summary = "Get all employment records")
    @GetMapping
    public ResponseEntity<ApiResponse<List<Employment>>> getAllEmployments() {
        return ResponseEntity.ok(ApiResponse.success("Employment records retrieved",
                employmentService.getAllEmployments()));
    }

    @Operation(summary = "Get active employment records only (used in payroll)")
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<Employment>>> getActiveEmployments() {
        return ResponseEntity.ok(ApiResponse.success("Active employment records retrieved",
                employmentService.getActiveEmployments()));
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

    @Operation(summary = "Update employment record (ADMIN only)")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Employment>> updateEmployment(
            @PathVariable Long id,
            @Valid @RequestBody EmploymentRequest request) {
        Employment employment = employmentService.updateEmployment(id, request);
        return ResponseEntity.ok(ApiResponse.success("Employment updated successfully", employment));
    }

    @Operation(summary = "Delete employment record (ADMIN only)")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEmployment(@PathVariable Long id) {
        employmentService.deleteEmployment(id);
        return ResponseEntity.ok(ApiResponse.success("Employment deleted successfully", null));
    }
}
