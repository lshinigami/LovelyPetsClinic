package com.lovelypets.auth.exception;

public class AccountNotVerifiedException extends RuntimeException {
    public AccountNotVerifiedException() {
        super("Account is not verified. Please confirm your email via OTP.");
    }
}
