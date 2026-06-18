package rw.gov.erp.payroll.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rw.gov.erp.payroll.entity.Message;
import rw.gov.erp.payroll.entity.Employee;
import rw.gov.erp.payroll.exception.ResourceNotFoundException;
import rw.gov.erp.payroll.repository.EmployeeRepository;
import rw.gov.erp.payroll.repository.MessageRepository;

import java.util.List;

/**
 * Task 5 – Message Service
 * Messages are inserted automatically by the PostgreSQL trigger when payroll is approved.
 * This service only provides READ access to those messages.
 */
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final EmployeeRepository employeeRepository;

    /** Get all messages (ADMIN view) */
    public List<Message> getAllMessages() {
        return messageRepository.findAll();
    }

    /** Get messages for a specific employee (secured) */
    public List<Message> getMessagesByEmployee(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        org.springframework.security.core.Authentication auth = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (!isAdmin && !auth.getName().equalsIgnoreCase(employee.getEmail())) {
                throw new org.springframework.security.access.AccessDeniedException("You are not authorized to view these messages");
            }
        }
        return messageRepository.findByEmployeeId(employeeId);
    }

    /** Get logged-in user's own messages */
    public List<Message> getMyMessages(String email) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No employee profile found for email: " + email));
        return messageRepository.findByEmployeeId(employee.getId());
    }

    /** Get messages for a specific month-year period */
    public List<Message> getMessagesByMonthYear(String monthYear) {
        return messageRepository.findByMonthYear(monthYear);
    }
}
