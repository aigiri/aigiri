package io.kutumbini.auth.security;

public interface ISecurityUserService {

    String validatePasswordResetToken(long id, String token);

}
