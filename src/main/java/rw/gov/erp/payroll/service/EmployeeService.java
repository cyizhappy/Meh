package rw.gov.erp.payroll.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rw.gov.erp.payroll.dto.request.EmployeeRequest;
import rw.gov.erp.payroll.entity.Employee;
import rw.gov.erp.payroll.exception.BadRequestException;
import rw.gov.erp.payroll.exception.ResourceNotFoundException;
import rw.gov.erp.payroll.repository.EmployeeRepository;

import java.util.List;

/**
 * Task 1 – Employee Management Service
 * Handles CRUD operations for employee personal information
 */
@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public Employee createEmployee(EmployeeRequest request) {
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Employee with email already exists: " + request.getEmail());
        }

        Employee employee = Employee.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .district(request.getDistrict())
                .mobile(request.getMobile())
                .dateOfBirth(request.getDateOfBirth())
                .build();

        return employeeRepository.save(employee);
    }

    public Employee getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        org.springframework.security.core.Authentication auth = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (!isAdmin && !auth.getName().equalsIgnoreCase(employee.getEmail())) {
                throw new org.springframework.security.access.AccessDeniedException("You are not authorized to view this employee profile");
            }
        }
        return employee;
    }

    public Employee getMyProfile(String email) {
        return employeeRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No employee profile found for email: " + email));
    }

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public Employee updateEmployee(Long id, EmployeeRequest request) {
        Employee employee = getEmployeeById(id);

        // Check email uniqueness (excluding self)
        if (!employee.getEmail().equals(request.getEmail())
                && employeeRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already used by another employee: " + request.getEmail());
        }

        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        employee.setDistrict(request.getDistrict());
        employee.setMobile(request.getMobile());
        employee.setDateOfBirth(request.getDateOfBirth());

        return employeeRepository.save(employee);
    }

    public void deleteEmployee(Long id) {
        Employee employee = getEmployeeById(id);
        employeeRepository.delete(employee);
    }
}
