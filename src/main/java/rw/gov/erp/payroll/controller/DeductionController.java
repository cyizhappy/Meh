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
import rw.gov.erp.payroll.dto.request.DeductionRequest;
import rw.gov.erp.payroll.dto.response.ApiResponse;
import rw.gov.erp.payroll.entity.Deduction;
import rw.gov.erp.payroll.service.DeductionService;

import java.util.List;

/**
 * Task 2 – Deduction Management Controller (ADMIN only)
 * Manage deduction types and their percentages
 */
@RestController
@RequestMapping("/api/deductions")
@RequiredArgsConstructor
@Tag(name = "Deduction Management", description = "Task 2 - Manage deduction types and percentages (ADMIN only)")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class DeductionController {

    private final DeductionService deductionService;

    @Operation(summary = "Get all deductions")
    @GetMapping
    public ResponseEntity<ApiResponse<List<Deduction>>> getAllDeductions() {
        return ResponseEntity.ok(ApiResponse.success("Deductions retrieved", deductionService.getAllDeductions()));
    }

    @Operation(summary = "Get deduction by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Deduction>> getDeduction(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Deduction retrieved", deductionService.getDeductionById(id)));
    }

    @Operation(summary = "Update a deduction percentage")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Deduction>> updateDeduction(
            @PathVariable Long id,
            @Valid @RequestBody DeductionRequest request) {
        Deduction deduction = deductionService.updateDeduction(id, request);
        return ResponseEntity.ok(ApiResponse.success("Deduction updated successfully", deduction));
    }
}
