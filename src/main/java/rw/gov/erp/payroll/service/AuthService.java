package rw.gov.erp.payroll.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.gov.erp.payroll.dto.request.LoginRequest;
import rw.gov.erp.payroll.dto.request.RegisterRequest;
import rw.gov.erp.payroll.dto.response.AuthResponse;
import rw.gov.erp.payroll.entity.Employee;
import rw.gov.erp.payroll.entity.Employment;
import rw.gov.erp.payroll.entity.User;
import rw.gov.erp.payroll.enums.EmploymentStatus;
import rw.gov.erp.payroll.enums.Role;
import rw.gov.erp.payroll.exception.BadRequestException;
import rw.gov.erp.payroll.repository.EmployeeRepository;
import rw.gov.erp.payroll.repository.EmploymentRepository;
import rw.gov.erp.payroll.repository.UserRepository;
import rw.gov.erp.payroll.security.CustomUserDetailsService;
import rw.gov.erp.payroll.security.JwtUtil;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Handles user registration and login with JWT generation.
 *
 * REGISTRATION FLOW:
 *   1. Creates a User account (credentials + role)
 *   2. Creates an Employee profile (personal information)
 *   3. Creates an Employment record with status PENDING
 *      → The employee is registered but CANNOT receive payroll yet
 *   4. Admin reviews and approves: updates Employment status PENDING → ACTIVE
 *      and assigns department, position, salary
 *   5. Once ACTIVE, the employee is included in payroll generation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final EmploymentRepository employmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;

    /**
     * Register a new user.
     *
     * For anonymous (public) callers:
     *   - Role is forced to EMPLOYEE regardless of what they pass
     *   - An Employee profile is created from the personal info fields
     *   - An Employment record is created with status PENDING (awaiting admin approval)
     *
     * For authenticated ADMIN callers:
     *   - Can assign any role (ADMIN or EMPLOYEE)
     *   - Employee + Employment records are still created if role is EMPLOYEE
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // ── 1. Validate uniqueness ─────────────────────────────
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered: " + request.getEmail());
        }

        // ── 2. Determine role ──────────────────────────────────
        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();

        boolean isAdmin = auth != null && auth.isAuthenticated() && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Role role;
        if (isAdmin && request.getRole() != null) {
            try {
                role = Role.valueOf(request.getRole().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid role. Must be ADMIN or EMPLOYEE");
            }
        } else {
            // Force EMPLOYEE for anonymous/public registration
            role = Role.EMPLOYEE;
        }

        // ── 3. Create User (login credentials) ────────────────
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();

        userRepository.save(user);
        log.info("User account created: {} (role: {})", user.getEmail(), role);

        // ── 4. Create Employee profile (personal info) ────────
        //    Only if an employee with this email doesn't already exist
        //    (admin may have pre-created the employee record)
        if (!employeeRepository.existsByEmail(request.getEmail())) {
            Employee employee = Employee.builder()
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .email(request.getEmail())
                    .district(request.getDistrict())
                    .mobile(request.getMobile())
                    .dateOfBirth(request.getDateOfBirth())
                    .build();

            employeeRepository.save(employee);
            log.info("Employee profile created for: {} {}", employee.getFirstName(), employee.getLastName());

            // ── 5. Create Employment record with PENDING status ───
            //    Auto-generate employee code
            long count = employmentRepository.count() + 1;
            String employeeCode = String.format("EMP-%05d", count);

            Employment employment = Employment.builder()
                    .employeeCode(employeeCode)
                    .department("Unassigned")
                    .position("Unassigned")
                    .baseSalary(BigDecimal.ZERO)
                    .status(EmploymentStatus.PENDING)
                    .joiningDate(LocalDate.now())
                    .employee(employee)
                    .build();

            employmentRepository.save(employment);
            log.info("Employment record created for {} with status PENDING (code: {}). " +
                     "Awaiting admin approval.", employee.getEmail(), employeeCode);
        }

        // ── 6. Generate JWT and return response ────────────────
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtUtil.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().name())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("User not found"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtUtil.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().name())
                .build();
    }
}
