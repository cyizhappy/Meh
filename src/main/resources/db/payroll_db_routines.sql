-- =============================================================================
-- Government of Rwanda ERP - Payroll Management System
-- DATABASE ROUTINES (Task 5)
-- =============================================================================
-- Run these SQL scripts in your PostgreSQL database AFTER the application
-- has started once (so JPA creates all tables automatically).
--
-- This file contains:
--   1. Trigger Function  – fires after each payslip is updated to PAID
--   2. Trigger           – attaches the function to the payslips table
--   3. Stored Procedure  – called by ADMIN to approve payroll
-- =============================================================================


-- =============================================================================
-- STEP 1: CREATE THE TRIGGER FUNCTION
-- =============================================================================
-- This function fires AFTER a row in the payslips table is updated to PAID.
-- It:
--   a) Fetches the employee's first name, employee code, net salary, month, year
--   b) Inserts a formatted notification message into the messages table
-- =============================================================================

CREATE OR REPLACE FUNCTION fn_notify_employee_on_payslip_paid()
RETURNS TRIGGER AS $$
DECLARE
    v_first_name     VARCHAR(100);
    v_last_name      VARCHAR(100);
    v_employee_id    BIGINT;
    v_employee_code  VARCHAR(50);
    v_net_salary     NUMERIC(15,2);
    v_month_name     VARCHAR(20);
    v_year           INTEGER;
    v_message        TEXT;
    v_month_year     VARCHAR(30);
    v_institution    VARCHAR(100) := 'Rwanda Coding Academy (RCA)';
BEGIN
    -- Only fire when status changes from PENDING to PAID
    IF NEW.status = 'PAID' AND OLD.status = 'PENDING' THEN

        -- Get employee details via employment record
        SELECT
            e.first_name,
            e.last_name,
            e.id,
            emp.employee_code,
            NEW.net_salary,
            NEW.year
        INTO
            v_first_name,
            v_last_name,
            v_employee_id,
            v_employee_code,
            v_net_salary,
            v_year
        FROM employment emp
        JOIN employees e ON e.id = emp.employee_id
        WHERE emp.id = NEW.employment_id;

        -- Convert month number to month name
        v_month_name := TO_CHAR(TO_DATE(NEW.month::TEXT, 'MM'), 'MONTH');
        v_month_name := TRIM(v_month_name);
        v_month_year := UPPER(v_month_name) || '-' || v_year::TEXT;

        -- Build the notification message
        v_message := 'Dear ' || v_first_name ||
                     ' Your salary of ' || v_month_year ||
                     ' from ' || v_institution ||
                     ' ' || TO_CHAR(v_net_salary, 'FM999,999,999.00') || ' FRW' ||
                     ' has been credited to your ' || v_employee_code ||
                     ' account Successfully.';

        -- Insert into messages table
        INSERT INTO messages (employee_id, message, month_year, created_at)
        VALUES (v_employee_id, v_message, v_month_year, NOW());

        RAISE NOTICE 'Message sent to employee %: %', v_first_name, v_message;

    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


-- =============================================================================
-- STEP 2: ATTACH THE TRIGGER TO THE PAYSLIPS TABLE
-- =============================================================================
-- Fires AFTER each row UPDATE on payslips table
-- =============================================================================

DROP TRIGGER IF EXISTS trg_notify_employee_payslip_paid ON payslips;

CREATE TRIGGER trg_notify_employee_payslip_paid
    AFTER UPDATE OF status ON payslips
    FOR EACH ROW
    EXECUTE FUNCTION fn_notify_employee_on_payslip_paid();


-- =============================================================================
-- STEP 3: STORED PROCEDURE - APPROVE PAYROLL
-- =============================================================================
-- Called when ADMIN approves payroll for a given month and year.
-- Updates all PENDING payslips → PAID (which triggers the above trigger).
--
-- Usage: CALL sp_approve_payroll(12, 2024);
-- =============================================================================

CREATE OR REPLACE PROCEDURE sp_approve_payroll(
    p_month INTEGER,
    p_year  INTEGER
)
LANGUAGE plpgsql AS $$
DECLARE
    v_count INTEGER;
BEGIN
    -- Count pending payslips for this period
    SELECT COUNT(*) INTO v_count
    FROM payslips
    WHERE month = p_month AND year = p_year AND status = 'PENDING';

    IF v_count = 0 THEN
        RAISE EXCEPTION 'No pending payslips found for month % and year %. '
                        'Payroll may not have been generated or was already approved.',
                        p_month, p_year;
    END IF;

    -- Update all PENDING payslips to PAID
    -- This triggers fn_notify_employee_on_payslip_paid for each row
    UPDATE payslips
    SET status = 'PAID'
    WHERE month = p_month
      AND year  = p_year
      AND status = 'PENDING';

    RAISE NOTICE '✅ Payroll approved for %/%. % payslip(s) marked as PAID. '
                 'Notification messages inserted into messages table.',
                 p_month, p_year, v_count;
END;
$$;


-- =============================================================================
-- STEP 4: VERIFICATION QUERIES (optional - run to test)
-- =============================================================================

-- View all messages:
-- SELECT m.id, e.first_name, e.last_name, m.month_year, m.message, m.created_at
-- FROM messages m
-- JOIN employees e ON e.id = m.employee_id
-- ORDER BY m.created_at DESC;

-- View payslip status after approval:
-- SELECT p.id, emp.employee_code, e.first_name, e.last_name,
--        p.month, p.year, p.net_salary, p.status
-- FROM payslips p
-- JOIN employment emp ON emp.id = p.employment_id
-- JOIN employees e ON e.id = emp.employee_id
-- ORDER BY p.year, p.month;

-- Manually call the stored procedure (alternative to API):
-- CALL sp_approve_payroll(12, 2024);
