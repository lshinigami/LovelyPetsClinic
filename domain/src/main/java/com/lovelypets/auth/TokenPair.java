package com.lovelypets.auth;

public record TokenPair(Token accessToken, Token refreshToken) {}
