package com.tma.job_fusion_backend.commons;

public final class EndpointConstant {

    public static final String ENDPOINT_PREFIX = "/api";

    // Base paths without prefix
    public static final String ENDPOINT_AUTH_BASE = "/auth";
    public static final String ENDPOINT_PLAN_BASE = "/plan";
    public static final String ENDPOINT_TENANT_BASE = "/tenant";
    public static final String ENDPOINT_USER_BASE = "/user";
    public static final String ENDPOINT_DASHBOARD_BASE = "/dashboard";
    public static final String ENDPOINT_STAFF_BASE = "/staff";

    // Core endpoints with /api
    public static final String ENDPOINT_AUTH = ENDPOINT_PREFIX + ENDPOINT_AUTH_BASE;
    public static final String ENDPOINT_PLAN = ENDPOINT_PREFIX + ENDPOINT_PLAN_BASE;
    public static final String ENDPOINT_TENANT = ENDPOINT_PREFIX + ENDPOINT_TENANT_BASE;
    public static final String ENDPOINT_USER = ENDPOINT_PREFIX + ENDPOINT_USER_BASE;
    public static final String ENDPOINT_DASHBOARD = ENDPOINT_PREFIX + ENDPOINT_DASHBOARD_BASE;

    // Authentication methods
    public static final String ENDPOINT_SIGNIN_BASE = "/signin";
    public static final String ENDPOINT_SIGNIN = ENDPOINT_AUTH + ENDPOINT_SIGNIN_BASE;

    public static final String ENDPOINT_SIGNUP_BASE = "/signup";
    public static final String ENDPOINT_SIGNUP = ENDPOINT_AUTH + ENDPOINT_SIGNUP_BASE;

    public static final String ENDPOINT_FORGOT_PASSWORD_BASE = "/forgot-password";
    public static final String ENDPOINT_FORGOT_PASSWORD = ENDPOINT_AUTH + ENDPOINT_FORGOT_PASSWORD_BASE;

    public static final String ENDPOINT_RESET_PASSWORD_BASE = "/reset-password";
    public static final String ENDPOINT_RESET_PASSWORD = ENDPOINT_AUTH + ENDPOINT_RESET_PASSWORD_BASE;

    public static final String ENDPOINT_CHECK_OTP_BASE = "/check-otp";
    public static final String ENDPOINT_CHECK_OTP = ENDPOINT_AUTH + ENDPOINT_CHECK_OTP_BASE;

    public static final String ENDPOINT_REFRESH_BASE = "/refresh-token";
    public static final String ENDPOINT_REFRESH = ENDPOINT_AUTH + ENDPOINT_REFRESH_BASE;

    public static final String ENDPOINT_LOGOUT_BASE = "/logout";
    public static final String ENDPOINT_LOGOUT = ENDPOINT_AUTH + ENDPOINT_LOGOUT_BASE;

    public static final String ENDPOINT_CHANGE_PASSWORD_BASE = "/change-password";
    public static final String ENDPOINT_CHANGE_PASSWORD = ENDPOINT_AUTH + ENDPOINT_CHANGE_PASSWORD_BASE;

    public static final String ENDPOINT_ACTIVATE_BASE = "/activate";
    public static final String ENDPOINT_ACTIVATE = ENDPOINT_AUTH + ENDPOINT_ACTIVATE_BASE;

    // Utility & Common sub-paths
    public static final String ENDPOINT_LIST = "/list";
    public static final String ENDPOINT_ID = "/{id}";
    public static final String ENDPOINT_STATS = "/stats";
    public static final String ENDPOINT_STATS_TENANT = ENDPOINT_STATS + ENDPOINT_TENANT_BASE;
    public static final String ENDPOINT_STAFF_LIST = ENDPOINT_STAFF_BASE + ENDPOINT_LIST;
    public static final String ENDPOINT_STAFF_ID = ENDPOINT_STAFF_BASE + ENDPOINT_ID;
}
