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
}
