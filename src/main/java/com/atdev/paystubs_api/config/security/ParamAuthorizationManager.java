package com.atdev.paystubs_api.config.security;

import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import java.util.Objects;
import java.util.function.Supplier;

public class ParamAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {
    private String user;
    private String password;

    public void setCredentials(String user, String password) {
        this.user = user;
        this.password = password;
    }

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext context) {
        var request = context.getRequest();

        String credentials = request.getParameter("credentials");
        String expected = user + "+" + password;

        boolean valid =
                Objects.equals(credentials, expected);

        // true = allow, false = deny (403)
        return new AuthorizationDecision(valid);
    }
}
