package rw.gov.erp.payroll.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.gov.erp.payroll.dto.response.PayslipResponse;
import rw.gov.erp.payroll.entity.Deduction;
import rw.gov.erp.payroll.entity.Employment;
import rw.gov.erp.payroll.entity.Payslip;
import rw.gov.erp.payroll.enums.EmploymentStatus;
import rw.gov.erp.payroll.enums.PayslipStatus;
import rw.gov.erp.payroll.exception.BadRequestException;
import rw.gov.erp.payroll.exception.ResourceNotFoundException;
import rw.gov.erp.payroll.repository.DeductionRepository;
import rw.gov.erp.payroll.repository.EmploymentRepository;
import rw.gov.erp.payroll.repository.PayslipRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Month;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Task 4 – Payroll Management Service
 *
 * COMPUTATION RULES:
 *   Gross  = baseSalary + (baseSalary * 14/100) + (baseSalary * 14/100)
 *   Net    = baseSalary - (tax + pension + medical + others)
 *
 * EXAMPLE (baseSalary = 700,000):
 *   House        = 700,000 * 14% = 98,000
 *   Transport    = 700,000 * 14% = 98,000
 *   Gross        = 700,000 + 98,000 + 98,000 = 896,000
 *
 *   Tax          = 700,000 * 30% = 210,000
 *   Pension      = 700,000 *  6% =  42,000
 *   Medical      = 700,000 *  5% =  35,000
 *   Others       = 700,000 *  5% =  35,000
 *   TotalDeduct  = 322,000
 *   Net          = 700,000 - 322,000 = 378,000
 *
 * Task 5: ADMIN approves payroll → all PENDING payslips become PAID →
 *         PostgreSQL trigger fires → messages table populated automatically.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PayrollService {

    private final EmploymentRepository employmentRepository;
    private final PayslipRepository payslipRepository;
    private final DeductionRepository deductionRepository;

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    /**
     * ADMIN generates payroll for a given month and year.
     * Only ACTIVE employees are included.
     * Duplicate payroll for same employee/month/year is prevented.
     *
     * @param month 1-12
     * @param year  e.g. 2024
     * @return list of generated payslips
     */
    @Transactional
    public List<PayslipResponse> generatePayroll(Integer month, Integer year) {
        validatePeriod(month, year);

        // Load all required deduction percentages
        BigDecimal taxRate       = getDeductionRate("EmployeeTax");
        BigDecimal pensionRate   = getDeductionRate("Pension");
        BigDecimal medicalRate   = getDeductionRate("MedicalInsurance");
        BigDecimal othersRate    = getDeductionRate("Others");
        BigDecimal houseRate     = getDeductionRate("House");
        BigDecimal transportRate = getDeductionRate("Transport");

        // Get all ACTIVE employees
        List<Employment> activeEmployments = employmentRepository.findByStatus(EmploymentStatus.ACTIVE);

        if (activeEmployments.isEmpty()) {
            throw new BadRequestException("No active employees found to generate payroll");
        }

        List<Payslip> generatedPayslips = activeEmployments.stream()
                .filter(employment -> {
                    // Skip if payslip already exists for this period (prevent duplicates)
                    boolean exists = payslipRepository.existsByEmploymentIdAndMonthAndYear(
                            employment.getId(), month, year);
                    if (exists) {
                        log.warn("Payslip already exists for employee {} for {}/{}. Skipping.",
                                employment.getEmployeeCode(), month, year);
                    }
                    return !exists;
                })
                .map(employment -> computePayslip(employment, month, year,
                        taxRate, pensionRate, medicalRate, othersRate, houseRate, transportRate))
                .collect(Collectors.toList());

        if (generatedPayslips.isEmpty()) {
            throw new BadRequestException(
                    "Payroll for " + month + "/" + year + " has already been generated for all active employees");
        }

        List<Payslip> saved = payslipRepository.saveAll(generatedPayslips);
        log.info("Payroll generated for {}/{}: {} payslips created", month, year, saved.size());

        return saved.stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * ADMIN approves payroll for a given month and year.
     * Updates all PENDING payslips to PAID status.
     * The PostgreSQL trigger then fires and inserts messages automatically.
     *
     * @param month 1-12
     * @param year  e.g. 2024
     * @return list of approved payslips
     */
    @Transactional
    public List<PayslipResponse> approvePayroll(Integer month, Integer year) {
        validatePeriod(month, year);

        // Call the stored procedure to approve the payroll
        try {
            payslipRepository.approvePayrollProcedure(month, year);
        } catch (Exception e) {
            log.error("Error executing stored procedure sp_approve_payroll: ", e);
            throw new BadRequestException("Failed to approve payroll via database procedure: " + e.getMessage());
        }

        // Fetch the approved payslips to return them
        List<Payslip> approved = payslipRepository.findByMonthAndYear(month, year);
        log.info("Payroll approved via stored procedure for {}/{}: {} payslips marked PAID", month, year, approved.size());

        return approved.stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * Get all payslips for a specific period (ADMIN view)
     */
    public List<PayslipResponse> getPayrollByPeriod(Integer month, Integer year) {
        validatePeriod(month, year);
        return payslipRepository.findByMonthAndYear(month, year)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * Get payslips for a specific employee (Employee view - secured)
     */
    public List<PayslipResponse> getPayslipsByEmployment(Long employmentId) {
        Employment employment = employmentRepository.findById(employmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Employment not found with id: " + employmentId));

        org.springframework.security.core.Authentication auth = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (!isAdmin && !auth.getName().equalsIgnoreCase(employment.getEmployee().getEmail())) {
                throw new org.springframework.security.access.AccessDeniedException("You are not authorized to view these payslips");
            }
        }

        return payslipRepository.findByEmploymentId(employmentId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * Get logged-in user's own payslips
     */
    public List<PayslipResponse> getMyPayslips(String email) {
        Employment employment = employmentRepository.findByEmployeeEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No employment profile found for email: " + email));
        return payslipRepository.findByEmploymentId(employment.getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * Get a specific payslip by ID (secured)
     */
    public PayslipResponse getPayslipById(Long id) {
        Payslip payslip = payslipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payslip not found with id: " + id));

        org.springframework.security.core.Authentication auth = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (!isAdmin && !auth.getName().equalsIgnoreCase(payslip.getEmployment().getEmployee().getEmail())) {
                throw new org.springframework.security.access.AccessDeniedException("You are not authorized to view this payslip");
            }
        }

        return toResponse(payslip);
    }

    // ─────────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────────

    /**
     * Core computation logic for a single employee's payslip
     */
    private Payslip computePayslip(Employment employment, Integer month, Integer year,
                                    BigDecimal taxRate, BigDecimal pensionRate,
                                    BigDecimal medicalRate, BigDecimal othersRate,
                                    BigDecimal houseRate, BigDecimal transportRate) {

        BigDecimal base = employment.getBaseSalary();

        // ── Benefits (added to gross) ──────────────────────────
        BigDecimal houseAmt     = percentOf(base, houseRate);
        BigDecimal transportAmt = percentOf(base, transportRate);

        // Gross = base + house + transport
        BigDecimal gross = base.add(houseAmt).add(transportAmt);

        // ── Deductions (subtracted from base for net) ──────────
        BigDecimal taxAmt     = percentOf(base, taxRate);
        BigDecimal pensionAmt = percentOf(base, pensionRate);
        BigDecimal medicalAmt = percentOf(base, medicalRate);
        BigDecimal othersAmt  = percentOf(base, othersRate);

        BigDecimal totalDeductions = taxAmt.add(pensionAmt).add(medicalAmt).add(othersAmt);

        // Safety check: deductions must not exceed gross salary
        if (totalDeductions.compareTo(gross) > 0) {
            throw new BadRequestException(
                    "Total deductions exceed gross salary for employee: "
                    + employment.getEmployeeCode());
        }

        // Net = base - totalDeductions
        BigDecimal net = base.subtract(totalDeductions);

        return Payslip.builder()
                .employment(employment)
                .baseSalary(base)
                .houseAmount(houseAmt)
                .transportAmount(transportAmt)
                .grossSalary(gross)
                .employeeTax(taxAmt)
                .pension(pensionAmt)
                .medicalInsurance(medicalAmt)
                .otherDeductions(othersAmt)
                .totalDeductions(totalDeductions)
                .netSalary(net)
                .month(month)
                .year(year)
                .status(PayslipStatus.PENDING)
                .build();
    }

    /** Helper: compute percentage of a value, scaled to 2 decimal places */
    private BigDecimal percentOf(BigDecimal amount, BigDecimal rate) {
        return amount.multiply(rate)
                .divide(HUNDRED, 2, RoundingMode.HALF_UP);
    }

    /** Get deduction rate by name from DB */
    private BigDecimal getDeductionRate(String name) {
        return deductionRepository.findByDeductionNameIgnoreCase(name)
                .map(Deduction::getPercentage)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Deduction not configured: " + name +
                        ". Please ensure all 6 deductions are set up before generating payroll."));
    }

    /** Validate month and year ranges */
    private void validatePeriod(Integer month, Integer year) {
        if (month < 1 || month > 12) {
            throw new BadRequestException("Invalid month: " + month + ". Must be 1-12");
        }
        if (year < 2000 || year > 2100) {
            throw new BadRequestException("Invalid year: " + year);
        }
    }

    /** Map Payslip entity to PayslipResponse DTO */
    private PayslipResponse toResponse(Payslip p) {
        Employment emp = p.getEmployment();
        String fullName = emp.getEmployee().getFirstName() + " " + emp.getEmployee().getLastName();
        String monthName = Month.of(p.getMonth()).name();

        return PayslipResponse.builder()
                .id(p.getId())
                .employeeCode(emp.getEmployeeCode())
                .employeeFullName(fullName)
                .department(emp.getDepartment())
                .position(emp.getPosition())
                .month(p.getMonth())
                .year(p.getYear())
                .monthName(monthName)
                .baseSalary(p.getBaseSalary())
                .houseAmount(p.getHouseAmount())
                .transportAmount(p.getTransportAmount())
                .grossSalary(p.getGrossSalary())
                .employeeTax(p.getEmployeeTax())
                .pension(p.getPension())
                .medicalInsurance(p.getMedicalInsurance())
                .otherDeductions(p.getOtherDeductions())
                .totalDeductions(p.getTotalDeductions())
                .netSalary(p.getNetSalary())
                .status(p.getStatus())
                .build();
    }
}
