package rw.gov.erp.payroll;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Government of Rwanda ERP System
 * Payroll & Employee Management System
 *
 * Entry point for the Spring Boot application.
 *
 * After startup:
 *   - Swagger UI: http://localhost:8080/swagger-ui.html
 *   - Default ADMIN: admin@rca.ac.rw / admin123
 *   - All 6 deductions are auto-seeded
 *
 * Remember to run the SQL file after first startup:
 *   src/main/resources/db/payroll_db_routines.sql
 */
@SpringBootApplication
public class PayrollApplication {
    public static void main(String[] args) {
        SpringApplication.run(PayrollApplication.class, args);
    }
}
