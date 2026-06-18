package rw.gov.erp.payroll.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import rw.gov.erp.payroll.entity.Deduction;
import rw.gov.erp.payroll.entity.User;
import rw.gov.erp.payroll.enums.Role;
import rw.gov.erp.payroll.repository.DeductionRepository;
import rw.gov.erp.payroll.repository.UserRepository;

import java.math.BigDecimal;

/**
 * Data Seeder – runs on application startup.
 * Seeds:
 *   1. Default ADMIN user
 *   2. All 6 required deductions (Task 2)
 *
 * This ensures the system is ready to use immediately after startup.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DeductionRepository deductionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedAdminUser();
        seedDeductions();
    }

    /**
     * Seed default ADMIN user
     * Email: admin@rca.ac.rw | Password: admin123
     */
    private void seedAdminUser() {
        if (!userRepository.existsByEmail("admin@rca.ac.rw")) {
            User admin = User.builder()
                    .firstName("System")
                    .lastName("Admin")
                    .email("admin@rca.ac.rw")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .build();
            userRepository.save(admin);
            log.info("✅ Default ADMIN user created: admin@rca.ac.rw / admin123");
        }
    }

    /**
     * Task 2: Seed the 6 required deductions with correct percentages
     *
     * No  | Name              | Percentage
     * ----|-------------------|----------
     * 1   | EmployeeTax       | 30%
     * 2   | Pension           | 6%
     * 3   | MedicalInsurance  | 5%
     * 4   | Others            | 5%
     * 5   | House             | 14%
     * 6   | Transport         | 14%
     */
    private void seedDeductions() {
        seedDeduction("EmployeeTax",      new BigDecimal("30"));
        seedDeduction("Pension",          new BigDecimal("6"));
        seedDeduction("MedicalInsurance", new BigDecimal("5"));
        seedDeduction("Others",           new BigDecimal("5"));
        seedDeduction("House",            new BigDecimal("14"));
        seedDeduction("Transport",        new BigDecimal("14"));
    }

    private void seedDeduction(String name, BigDecimal percentage) {
        if (!deductionRepository.existsByDeductionNameIgnoreCase(name)) {
            Deduction d = Deduction.builder()
                    .deductionName(name)
                    .percentage(percentage)
                    .build();
            deductionRepository.save(d);
            log.info("✅ Deduction seeded: {} → {}%", name, percentage);
        }
    }
}
