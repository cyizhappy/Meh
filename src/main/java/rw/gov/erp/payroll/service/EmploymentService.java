package rw.gov.erp.payroll.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rw.gov.erp.payroll.dto.request.EmploymentRequest;
import rw.gov.erp.payroll.entity.Employee;
import rw.gov.erp.payroll.entity.Employment;
import rw.gov.erp.payroll.enums.EmploymentStatus;
import rw.gov.erp.payroll.exception.BadRequestException;
import rw.gov.erp.payroll.exception.ResourceNotFoundException;
import rw.gov.erp.payroll.repository.EmployeeRepository;
import rw.gov.erp.payroll.repository.EmploymentRepository;

import java.util.List;

/**
 * Task 1 – Employment Management Service
 * Handles CRUD operations for employee professional/employment information
 * Auto-generates unique employee codes (e.g., EMP-00001)
 */
@Service
@RequiredArgsConstructor
public class EmploymentService {

    private final EmploymentRepository employmentRepository;
    private final EmployeeRepository employeeRepository;

    public Employment createEmployment(EmploymentRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found with id: " + request.getEmployeeId()));

        // Check if employee already has employment record
        if (employmentRepository.findByEmployeeId(request.getEmployeeId()).isPresent()) {
            throw new BadRequestException("Employment record already exists for employee id: "
                    + request.getEmployeeId());
        }

        EmploymentStatus status;
        try {
            status = EmploymentStatus.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status. Must be ACTIVE or INACTIVE");
        }

        String employeeCode = generateEmployeeCode();

        Employment employment = Employment.builder()
                .employeeCode(employeeCode)
                .department(request.getDepartment())
                .position(request.getPosition())
                .baseSalary(request.getBaseSalary())
                .status(status)
                .joiningDate(request.getJoiningDate())
                .employee(employee)
                .build();

        return employmentRepository.save(employment);
    }

    public Employment getEmploymentById(Long id) {
        return employmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employment not found with id: " + id));
    }

    public Employment getEmploymentByEmployeeId(Long employeeId) {
        return employmentRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employment record not found for employee id: " + employeeId));
    }

    public List<Employment> getAllEmployments() {
        return employmentRepository.findAll();
    }

    public List<Employment> getActiveEmployments() {
        return employmentRepository.findByStatus(EmploymentStatus.ACTIVE);
    }

    public Employment updateEmployment(Long id, EmploymentRequest request) {
        Employment employment = getEmploymentById(id);

        EmploymentStatus status;
        try {
            status = EmploymentStatus.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status. Must be ACTIVE or INACTIVE");
        }

        employment.setDepartment(request.getDepartment());
        employment.setPosition(request.getPosition());
        employment.setBaseSalary(request.getBaseSalary());
        employment.setStatus(status);
        employment.setJoiningDate(request.getJoiningDate());

        return employmentRepository.save(employment);
    }

    public void deleteEmployment(Long id) {
        Employment employment = getEmploymentById(id);
        employmentRepository.delete(employment);
    }

    /**
     * Auto-generates unique employee code in format EMP-XXXXX
     * e.g., EMP-00001, EMP-00002, etc.
     */
    private String generateEmployeeCode() {
        long count = employmentRepository.count() + 1;
        return String.format("EMP-%05d", count);
    }
}
