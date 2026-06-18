package rw.gov.erp.payroll.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rw.gov.erp.payroll.entity.Message;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    /** Get all messages for a specific employee */
    List<Message> findByEmployeeId(Long employeeId);

    /** Get all messages for a given month-year period */
    List<Message> findByMonthYear(String monthYear);
}
