package com.tma.job_fusion_backend.commons;

public final class ErrorCode {

    public static final String EMAIL_ALREADY_EXISTS = "email_already_exists";
    public static final String USER_NOT_FOUND = "user_not_found";
    public static final String INVALID_TOKEN = "invalid_token";
    public static final String INVALID_EMAIL = "wrong_email";
    public static final String INVALID_PASSWORD = "wrong_password";
    public static final String ACCESS_DENIED = "access_denied";
    public static final String INACTIVE_USER = "user_account_is_not_active";
    public static final String INVALID_JOB_POSTING = "must_fill_number_or_choose_unlimited";
    public static final String INVALID_STAFF_ACCOUNT = "must_fill_number_or_choose_unlimited";
    public static final String INTERNAL_SERVER_ERROR = "an_unexpected_error_occurred_please_try_again_later.";
    public static final String DUPLICATE_PASSWORD = "old_password_can_not_be_the_same_with_new_password";
    public static final String EXPIRED_OTP = "otp_has_expired_please_request_a_new_one";
    public static final String PLAN_NOT_FOUND = "plan_not_found";
    public static final String ROLE_NOT_FOUND = "role_not_found";
    public static final String TENANT_NOT_FOUND = "tenant_not_found";
    public static final String PLAN_ALREADY_EXISTS = "plan_already_exists";
    public static final String MAX_STAFF_LIMIT_REACHED = "max_staff_limit_reached";
    public static final String STAFF_ALREADY_ACTIVE = "staff_already_active_or_disabled";
    public static final String TENANT_INACTIVE = "tenant_is_inactive";
    public static final String MAX_JOB_POSTING_LIMIT_REACHED = "max_job_posting_limit_reached";
    public static final String JOB_POSTING_NOT_FOUND = "job_posting_not_found";
    public static final String JOB_TITLE_ALREADY_EXISTS = "job_title_already_exists";
    public static final String INVALID_SALARY_RANGE = "salary_max_must_be_greater_than_or_equal_to_salary_min";
    public static final String COMPANY_NAME_ALREADY_EXISTS = "company_name_already_exists";
    public static final String JOB_CRITERIA_NOT_FOUND = "job_criteria_not_found";
    public static final String INVALID_JOB_CRITERIA = "invalid_job_criteria";
    public static final String INVALID_CRITERION_NAME = "criterion_name_is_required";
    public static final String INVALID_CRITERION_WEIGHT = "weight_must_be_positive";
    public static final String DUPLICATE_CRITERION_NAME = "criterion_name_must_be_unique";
    public static final String INVALID_TOTAL_WEIGHT = "total_weight_must_be_exactly_100";
    public static final String DOMAIN_ALREADY_EXISTS = "domain_already_exists";
}
