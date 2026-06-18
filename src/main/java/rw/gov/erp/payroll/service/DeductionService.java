package rw.gov.erp.payroll.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rw.gov.erp.payroll.dto.request.DeductionRequest;
import rw.gov.erp.payroll.entity.Deduction;
import rw.gov.erp.payroll.exception.BadRequestException;
import rw.gov.erp.payroll.exception.ResourceNotFoundException;
import rw.gov.erp.payroll.repository.DeductionRepository;

import java.util.List;

/**
 * Task 2 – Deduction Management Service
 * Manages deduction types and their percentages.
 *
 * Default deductions (seeded via DataSeeder):
 *   1. EmployeeTax      → 30%
 *   2. Pension          → 6%
 *   3. MedicalInsurance → 5%
 *   4. Others           → 5%
 *   5. House            → 14%
 *   6. Transport        → 14%
 */
@Service
@RequiredArgsConstructor
public class DeductionService {

    private final DeductionRepository deductionRepository;

    public Deduction createDeduction(DeductionRequest request) {
        if (deductionRepository.existsByDeductionNameIgnoreCase(request.getDeductionName())) {
            throw new BadRequestException("Deduction already exists: " + request.getDeductionName());
        }

        Deduction deduction = Deduction.builder()
                .deductionName(request.getDeductionName())
                .percentage(request.getPercentage())
                .build();

        return deductionRepository.save(deduction);
    }

    public Deduction getDeductionById(Long id) {
        return deductionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deduction not found with id: " + id));
    }

    public Deduction getDeductionByName(String name) {
        return deductionRepository.findByDeductionNameIgnoreCase(name)
                .orElseThrow(() -> new ResourceNotFoundException("Deduction not found: " + name));
    }

    public List<Deduction> getAllDeductions() {
        return deductionRepository.findAll();
    }

    public Deduction updateDeduction(Long id, DeductionRequest request) {
        Deduction deduction = getDeductionById(id);

        // Check name uniqueness (excluding self)
        if (!deduction.getDeductionName().equalsIgnoreCase(request.getDeductionName())
                && deductionRepository.existsByDeductionNameIgnoreCase(request.getDeductionName())) {
            throw new BadRequestException("Deduction name already exists: " + request.getDeductionName());
        }

        deduction.setDeductionName(request.getDeductionName());
        deduction.setPercentage(request.getPercentage());

        return deductionRepository.save(deduction);
    }

    public void deleteDeduction(Long id) {
        Deduction deduction = getDeductionById(id);
        deductionRepository.delete(deduction);
    }
}
