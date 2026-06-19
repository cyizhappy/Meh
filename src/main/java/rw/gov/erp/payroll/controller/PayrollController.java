package rw.gov.erp.payroll.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import rw.gov.erp.payroll.dto.response.ApiResponse;
import rw.gov.erp.payroll.dto.response.PayslipResponse;
import rw.gov.erp.payroll.service.PayrollService;

import java.util.List;

/**
 * Task 4 – Payroll Management Controller
 *
 * POST /api/payroll/generate/{month}/{year}  → ADMIN generates payroll
 * POST /api/payroll/approve/{month}/{year}   → ADMIN approves (triggers DB messages)
 * GET  /api/payroll/{month}/{year}           → ADMIN views all payslips for period
 * GET  /api/payroll/payslip/{id}             → View specific payslip
 * GET  /api/payroll/employee/{employmentId}  → Employee views their payslips
 */
@RestController
@RequestMapping("/api/payroll")
@RequiredArgsConstructor
@Tag(name = "Payroll Management", description = "Task 4 - Generate, approve and view payroll (payslips)")
@SecurityRequirement(name = "bearerAuth")
public class PayrollController {

    private final PayrollService payrollService;

    @Operation(
        summary = "Generate payroll for a month/year (ADMIN only)",
        description = "Computes gross, net salary and deductions for all ACTIVE employees. " +
                      "Prevents duplicates for the same employee/month/year."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/generate/{month}/{year}")
    public ResponseEntity<ApiResponse<List<PayslipResponse>>> generatePayroll(
            @PathVariable Integer month,
            @PathVariable Integer year) {
        List<PayslipResponse> payslips = payrollService.generatePayroll(month, year);
        return ResponseEntity.ok(ApiResponse.success(
                "Payroll generated successfully for " + month + "/" + year +
                ". Total payslips: " + payslips.size(), payslips));
    }

    @Operation(
        summary = "Approve payroll for a month/year (ADMIN only)",
        description = "Updates all PENDING payslips to PAID status. " +
                      "The PostgreSQL trigger fires automatically and inserts notification messages " +
                      "into the messages table for each employee."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/approve/{month}/{year}")
    public ResponseEntity<ApiResponse<List<PayslipResponse>>> approvePayroll(
            @PathVariable Integer month,
            @PathVariable Integer year) {
        List<PayslipResponse> payslips = payrollService.approvePayroll(month, year);
        return ResponseEntity.ok(ApiResponse.success(
                "Payroll approved for " + month + "/" + year +
                ". Employees have been notified via messages table.", payslips));
    }

    @Operation(summary = "Get all payslips for a specific month/year (ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{month}/{year}")
    public ResponseEntity<ApiResponse<List<PayslipResponse>>> getPayrollByPeriod(
            @PathVariable Integer month,
            @PathVariable Integer year) {
        return ResponseEntity.ok(ApiResponse.success("Payroll retrieved",
                payrollService.getPayrollByPeriod(month, year)));
    }

    @Operation(summary = "Get a specific payslip by ID")
    @GetMapping("/payslip/{id}")
    public ResponseEntity<ApiResponse<PayslipResponse>> getPayslipById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Payslip retrieved",
                payrollService.getPayslipById(id)));
    }

    @Operation(summary = "Get currently logged-in employee's payslips")
    @GetMapping("/my-payslips")
    public ResponseEntity<ApiResponse<List<PayslipResponse>>> getMyPayslips() {
        org.springframework.security.core.Authentication auth = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        List<PayslipResponse> payslips = payrollService.getMyPayslips(auth.getName());
        return ResponseEntity.ok(ApiResponse.success("Your payslips retrieved", payslips));
    }

    @Operation(summary = "Get all payslips for a specific employment record (Employee view - secured)")
    @GetMapping("/employee/{employmentId}")
    public ResponseEntity<ApiResponse<List<PayslipResponse>>> getEmployeePayslips(
            @PathVariable Long employmentId) {
        return ResponseEntity.ok(ApiResponse.success("Employee payslips retrieved",
                payrollService.getPayslipsByEmployment(employmentId)));
    }
}
