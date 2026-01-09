package com.example.forum.constant;

public class MessageConstants {
    private MessageConstants() {}

    // authentication and authorization
    public static final String LOGIN_REQUIRED ="Action can be done when logged in";
    public static final String LOGIN_FAILED = "Invalid username or password.";
    public static final String UNAUTHORIZED = "Unauthorized access. Please login specifically.";
    public static final String FORBIDDEN = "You do not have permission to access this resource.";

    // account status
    public static final String ACCOUNT_LOCKED = "Your account is locked, please contact supports.";
    public static final String ACCOUNT_LOCKED_DUE_TO_OVER_ATTEMPTS = "Your account has been locked due to multiple failed login attempts.";
    public static final String ACCOUNT_DISABLED = "Your account has been disabled. Please contact support.";
    public static final String ACCOUNT_NOT_VERIFIED ="Your account is not verified.";

    // token refresh/access
    public static final String TOKEN_INVALID = "Invalid or expired token.";
    public static final String TOKEN_EXPIRED = "Token has expired. Please refresh your token.";
    public static final String REQUIRE_TWO_FACTOR_AUTHENTICATION ="Two-factor authentication is required";
    public static final String MISSING_REFRESH_TOKEN ="Refresh Token is missing!";
    public static final String REFRESH_TOKEN_INVALID_REVOKED ="Refresh Token has been revoked (Logout)";
    public static final String REFRESH_TOKEN_INVALID ="Invalid Refresh Token";
    public static final String REFRESH_TOKEN_EXPIRED ="Refresh Token Expired.";


    // user & password
    public static final String USER_NOT_FOUND = "User not found.";
    public static final String EMAIL_ALREADY_EXISTS = "Email address is already in use.";
    public static final String USERNAME_ALREADY_EXISTS = "Username is already taken.";
    public static final String OLD_PASSWORD_INCORRECT = "The old password provided is incorrect.";
    public static final String ROLE_NOT_FOUND ="Role not found.";
    public static final String PASSWORD_REQUIRED = "Password is required.";

    // 2fa
    public static final String TWO_FACTOR_REQUIRED = "Two-factor authentication is required.";
    public static final String CODE_2FA_EXPIRED_TRY_AGAIN ="Your code is expired or unavailable. Please retake enable/ setup step.";
    public static final String TWO_FACTOR_NOT_ENABLED ="2FA is not enabled for this user.";

    // otp
    public static final String OTP_SENT_SUCCESS = "OTP has been sent to your email.";
    public static final String OTP_INVALID = "Invalid or expired OTP.";
    public static final String WRONG_OTP_CODE = "Wrong code, please re-enter.";
    public static final String OTP_LIMIT_REACHED = "Maximum OTP attempts reached. Please request a new one.";
    public static final String EMAIL_SEND_FAILED = "Failed to send email. Please try again.";
    public static final String INVALID_RESET_PASSWORD_TOKEN ="Invalid or expired reset password token";


    // == Business logic ==
    // post
    public static final String POST_NOT_FOUND = "Post not found.";
    public static final String NO_PERMISSION_EDIT_POST ="You are not have permission to edit this post";

    // device
    public static final String DEVICE_NOT_FOUND ="Device not found.";

    // comment
    public static final String COMMENT_NOT_FOUND = "Comment not found.";
    public static final String EDIT_OWN_COMMENT ="You can only edit your own comment!";
    public static final String PARENT_COMMENT_NOT_FOUND ="Parent comment not found.";

    // follow
    public static final String CANT_FOLLOW_YOURSELF ="You can not follow yourself!";
    public static final String ALREADY_FOLLOWED= "You already followed this user.";
    public static final String HAVE_NOT_FOLLOW ="You have not followed this user.";
    public static final String USER_HAVE_NOT_FOLLOW ="This user is not following you.";

    // notification
    public static final String NOTIFICATION_NOT_FOUND="Notification not found.";

    // email
    public static final String SUBJECT_OTP_MAIL = "Your verification code (OTP)";
    public static final String SUBJECT_NEW_DEVICE_LOGIN = "Security Alert: New Login Detected";
}

