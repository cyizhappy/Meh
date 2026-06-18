package rw.gov.erp.payroll.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rw.gov.erp.payroll.entity.Employment;
import rw.gov.erp.payroll.enums.EmploymentStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmploymentRepository extends JpaRepository<Employment, Long> {
    Optional<Employment> findByEmployeeId(Long employeeId);
    Optional<Employment> findByEmployeeCode(String employeeCode);
    Optional<Employment> findByEmployeeEmail(String email);
    boolean existsByEmployeeCode(String employeeCode);
    List<Employment> findByStatus(EmploymentStatus status);
}
