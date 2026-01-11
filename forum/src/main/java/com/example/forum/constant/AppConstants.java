package com.example.forum.constant;

public final class AppConstants {

    private AppConstants(){}

    // Redis key prefix
    public static final String REVOKED_USER_KEY = "revoked_user:";
    public static final String REVOKED_DEVICE_KEY = "revoked_device:";
    public static final String PREFIX_FAIL_COUNT = "login:fail:";
    public static final String PREFIX_LOCK_LOGIN = "login:locked:";
    public static final String PREFIX_TEMP_2FA = "tempt:2af:";
    public static final String BLACKLIST_KEY="bl_token:";
    public static final String PREFIX_BLACKLIST_REFRESH_TOKEN ="bl_refresh:";

    // otp redis prefix
    public static final String PREFIX_VERIFICATION_OTP = "verification:opt:";
    public static final String PREFIX_VERIFICATION_ATTEMPT ="attempts:verification:otp:";
    public static final String PREFIX_RESET_TOKEN="password_reset_token:";

    // redis value
    public static final String REDIS_VALUE_REVOKED ="revoked";
    public static final String REDIS_VALUE_LOGOUT ="logout";
    public static final String REDIS_VALUE_LOCKED="locked";

    // user role
    public static final String ROLE_USER ="ROLE_USER";
    public static final String ROLE_ADMIN ="ROLE_ADMIN";

    public static final int OTP_GENERATION_BOUND = 1000000; // Để random ra 6 số
    public static final int INITIAL_ATTEMPT_VALUE = 1;

    public static final String DATE_TIME_FORMAT ="dd-MM-yyyy HH:mm:ss";



}
