package com.atdev.paystubs_api.config.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParamAuthorizationManagerTest {

    private ParamAuthorizationManager authorizationManager;

    @Mock
    private Supplier<Authentication> authenticationSupplier;

    @Mock
    private RequestAuthorizationContext context;

    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        authorizationManager = new ParamAuthorizationManager();
        authorizationManager.setCredentials("testuser", "testpass");

        request = new MockHttpServletRequest();
        when(context.getRequest()).thenReturn(request);
    }

    @Test
    void shouldGrantAccessWithValidCredentials() {
        // Given
        request.setParameter("credentials", "testuser+testpass");

        // When
        AuthorizationDecision decision = authorizationManager.check(authenticationSupplier, context);

        // Then
        assertThat(decision.isGranted()).isTrue();
    }

    @Test
    void shouldDenyAccessWithInvalidCredentials() {
        // Given
        request.setParameter("credentials", "wronguser+wrongpass");

        // When
        AuthorizationDecision decision = authorizationManager.check(authenticationSupplier, context);

        // Then
        assertThat(decision.isGranted()).isFalse();
    }

    @Test
    void shouldDenyAccessWithInvalidUsername() {
        // Given
        request.setParameter("credentials", "wronguser+testpass");

        // When
        AuthorizationDecision decision = authorizationManager.check(authenticationSupplier, context);

        // Then
        assertThat(decision.isGranted()).isFalse();
    }

    @Test
    void shouldDenyAccessWithInvalidPassword() {
        // Given
        request.setParameter("credentials", "testuser+wrongpass");

        // When
        AuthorizationDecision decision = authorizationManager.check(authenticationSupplier, context);

        // Then
        assertThat(decision.isGranted()).isFalse();
    }

    @Test
    void shouldDenyAccessWithMissingCredentials() {
        // Given - no credentials parameter set

        // When
        AuthorizationDecision decision = authorizationManager.check(authenticationSupplier, context);

        // Then
        assertThat(decision.isGranted()).isFalse();
    }

    @Test
    void shouldDenyAccessWithNullCredentials() {
        // Given
        request.setParameter("credentials", (String) null);

        // When
        AuthorizationDecision decision = authorizationManager.check(authenticationSupplier, context);

        // Then
        assertThat(decision.isGranted()).isFalse();
    }

    @Test
    void shouldDenyAccessWithEmptyCredentials() {
        // Given
        request.setParameter("credentials", "");

        // When
        AuthorizationDecision decision = authorizationManager.check(authenticationSupplier, context);

        // Then
        assertThat(decision.isGranted()).isFalse();
    }

    @Test
    void shouldDenyAccessWithWrongSeparator() {
        // Given - using colon instead of plus
        request.setParameter("credentials", "testuser:testpass");

        // When
        AuthorizationDecision decision = authorizationManager.check(authenticationSupplier, context);

        // Then
        assertThat(decision.isGranted()).isFalse();
    }

    @Test
    void shouldHandleCredentialsWithSpecialCharacters() {
        // Given
        authorizationManager.setCredentials("user@email.com", "p@ss!w0rd#123");
        request.setParameter("credentials", "user@email.com+p@ss!w0rd#123");

        // When
        AuthorizationDecision decision = authorizationManager.check(authenticationSupplier, context);

        // Then
        assertThat(decision.isGranted()).isTrue();
    }

    @Test
    void shouldBeCaseSensitive() {
        // Given - uppercase username
        request.setParameter("credentials", "TESTUSER+testpass");

        // When
        AuthorizationDecision decision = authorizationManager.check(authenticationSupplier, context);

        // Then
        assertThat(decision.isGranted()).isFalse();
    }

    @Test
    void shouldBeCaseSensitiveForPassword() {
        // Given - uppercase password
        request.setParameter("credentials", "testuser+TESTPASS");

        // When
        AuthorizationDecision decision = authorizationManager.check(authenticationSupplier, context);

        // Then
        assertThat(decision.isGranted()).isFalse();
    }

    @Test
    void shouldHandleCredentialsUpdate() {
        // Given - initial credentials
        request.setParameter("credentials", "testuser+testpass");
        AuthorizationDecision firstDecision = authorizationManager.check(authenticationSupplier, context);

        // When - update credentials
        authorizationManager.setCredentials("newuser", "newpass");
        request.setParameter("credentials", "newuser+newpass");
        AuthorizationDecision secondDecision = authorizationManager.check(authenticationSupplier, context);

        // Then
        assertThat(firstDecision.isGranted()).isTrue();
        assertThat(secondDecision.isGranted()).isTrue();

        // Old credentials should no longer work
        request.setParameter("credentials", "testuser+testpass");
        AuthorizationDecision thirdDecision = authorizationManager.check(authenticationSupplier, context);
        assertThat(thirdDecision.isGranted()).isFalse();
    }

    @Test
    void shouldDenyAccessWhenCredentialsNotSet() {
        // Given - new instance without credentials set
        ParamAuthorizationManager newManager = new ParamAuthorizationManager();
        request.setParameter("credentials", "anyuser+anypass");

        // When
        AuthorizationDecision decision = newManager.check(authenticationSupplier, context);

        // Then
        assertThat(decision.isGranted()).isFalse();
    }

    @Test
    void shouldHandleWhitespaceInCredentials() {
        // Given - credentials with whitespace
        request.setParameter("credentials", "testuser +testpass");

        // When
        AuthorizationDecision decision = authorizationManager.check(authenticationSupplier, context);

        // Then
        assertThat(decision.isGranted()).isFalse();
    }

    @Test
    void shouldHandleMultiplePlusSigns() {
        // Given - password contains plus sign
        authorizationManager.setCredentials("user", "pass+word");
        request.setParameter("credentials", "user+pass+word");

        // When
        AuthorizationDecision decision = authorizationManager.check(authenticationSupplier, context);

        // Then
        assertThat(decision.isGranted()).isTrue();
    }

    @Test
    void shouldHandleEmptyUsername() {
        // Given
        authorizationManager.setCredentials("", "password");
        request.setParameter("credentials", "+password");

        // When
        AuthorizationDecision decision = authorizationManager.check(authenticationSupplier, context);

        // Then
        assertThat(decision.isGranted()).isTrue();
    }

    @Test
    void shouldHandleEmptyPassword() {
        // Given
        authorizationManager.setCredentials("username", "");
        request.setParameter("credentials", "username+");

        // When
        AuthorizationDecision decision = authorizationManager.check(authenticationSupplier, context);

        // Then
        assertThat(decision.isGranted()).isTrue();
    }
}
