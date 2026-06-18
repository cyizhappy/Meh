package rw.gov.erp.payroll.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger / OpenAPI configuration for the ERP Payroll System
 * Access at: http://localhost:8080/swagger-ui.html
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI erPayrollOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Government of Rwanda ERP - Payroll Management System API")
                        .description("""
                                ## ERP Payroll System API
                                
                                This API manages the Payroll and Employee Management System for the Government of Rwanda.
                                
                                ### Features:
                                - **Task 1**: Employee & Employment Management
                                - **Task 2**: Deduction & Tax Management (Tax 30%, Pension 6%, Medical 5%, Others 5%, House 14%, Transport 14%)
                                - **Task 4**: Payroll Generation & Approval with salary computation
                                - **Task 5**: DB Trigger-based messaging on payroll approval
                                
                                ### Authentication:
                                Use the `/api/auth/login` endpoint to get your JWT token, then click 'Authorize' and enter: `Bearer <your-token>`
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Rwanda Coding Academy (RCA)")
                                .email("admin@rca.ac.rw")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter your JWT token")));
    }
}
