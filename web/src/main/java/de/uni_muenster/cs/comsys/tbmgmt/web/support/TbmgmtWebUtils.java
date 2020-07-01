package de.uni_muenster.cs.comsys.tbmgmt.web.support;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.access.vote.UnanimousBased;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.thymeleaf.spring5.util.DetailedError;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 Created by matthias on 08.11.15.
 */
public class TbmgmtWebUtils {

    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_USER = "ROLE_USER";
    public static final AccessDecisionManager AUTHENTICATED_VOTER_MANAGER =
            new UnanimousBased(Collections.singletonList(new AuthenticatedVoter()));

    public static void isFullyAuthenticated(final Authentication authenticated, final Object object)
            throws AccessDeniedException, InsufficientAuthenticationException {
        AUTHENTICATED_VOTER_MANAGER.decide(authenticated, object,
                SecurityConfig.createList(AuthenticatedVoter.IS_AUTHENTICATED_FULLY));
    }

    public static <T> void isAdminOrCreator(final Authentication authenticated, final T object,
                                            final CallbackBasedAccessDecisionVoter<T> creatorVoter) {
        final AccessDecisionManager accessDecisionManager =
                new AffirmativeBased(Arrays.asList(new RoleVoter(), creatorVoter));
        accessDecisionManager.decide(authenticated, object, SecurityConfig.createList(ROLE_ADMIN));
    }

    public static Authentication getAuthenticatedAuthentication(final AuthenticationManager authenticationManager) {
        final SecurityContext securityContext = SecurityContextHolder.getContext();
        if (securityContext == null) {
            throw new AuthenticationCredentialsNotFoundException("SecurityContext is missing");
        }

        final Authentication authentication = securityContext.getAuthentication();
        if (authentication == null) {
            throw new AuthenticationCredentialsNotFoundException("Authentication was null");
        }
        if (authentication.isAuthenticated()) {
            return authentication;
        }

        final Authentication authenticated = authenticationManager.authenticate(authentication);
        securityContext.setAuthentication(authentication);
        return authenticated;
    }

    public static Authentication getAuthentication(final AuthenticationManager authenticationManager) {
        try {
            return getAuthenticatedAuthentication(authenticationManager);
        } catch (final AuthenticationException ignored) {
            return null;
        }
    }

    public static void setHeaders(final HttpServletResponse response, final HttpHeaders httpHeaders) {
        for (final Map.Entry<String, List<String>> entry : httpHeaders.entrySet()) {
            final ListIterator<String> iterator = entry.getValue().listIterator();
            if (iterator.hasNext()) {
                response.setHeader(entry.getKey(), iterator.next());
                while (iterator.hasNext()) {
                    response.addHeader(entry.getKey(), iterator.next());
                }
            }
        }
    }

    // May not be static, access by Thymeleaf!
    @SuppressWarnings("MethodMayBeStatic")
    public boolean containsFieldPrefix(final List<DetailedError> detailedErrors, final String... prefixes) {
        for (final DetailedError detailedError : detailedErrors) {
            if (StringUtils.startsWithAny(detailedError.getFieldName(), prefixes)) {
                return true;
            }
        }
        return false;
    }

    // May not be static, access by Thymeleaf!
    @SuppressWarnings("MethodMayBeStatic")
    public boolean containsFieldNotPrefixed(final List<DetailedError> detailedErrors, final String... prefixes) {
        for (final DetailedError detailedError : detailedErrors) {
            if (!StringUtils.startsWithAny(detailedError.getFieldName(), prefixes)) {
                return true;
            }
        }
        return false;
    }
}
