package rw.gov.erp.payroll.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rw.gov.erp.payroll.entity.Payslip;
import rw.gov.erp.payroll.enums.PayslipStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface PayslipRepository extends JpaRepository<Payslip, Long> {

    /** Check for duplicate payroll per employee per month/year */
    boolean existsByEmploymentIdAndMonthAndYear(Long employmentId, Integer month, Integer year);

    /** Find a specific payslip for an employee in a given period */
    Optional<Payslip> findByEmploymentIdAndMonthAndYear(Long employmentId, Integer month, Integer year);

    /** Get all payslips for a given month/year (used during approval) */
    List<Payslip> findByMonthAndYear(Integer month, Integer year);

    /** Get all payslips for a specific employee (to view payslip history) */
    List<Payslip> findByEmploymentId(Long employmentId);

    /** Get pending payslips for a month/year (before approval) */
    List<Payslip> findByMonthAndYearAndStatus(Integer month, Integer year, PayslipStatus status);

    /** Call the PostgreSQL stored procedure to approve payroll (Task 5 requirement) */
    @Modifying
    @Query(value = "CALL sp_approve_payroll(:month, :year)", nativeQuery = true)
    void approvePayrollProcedure(@Param("month") Integer month, @Param("year") Integer year);
}

