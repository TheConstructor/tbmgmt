package de.uni_muenster.cs.comsys.tbmgmt.web.support;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 Created by matthias on 07.10.15.
 */
public class UsernameCachingAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    public static final String LAST_USERNAME_KEY = "SPRING_SECURITY_LAST_USERNAME";

    private final String usernameParameter;

    public UsernameCachingAuthenticationFailureHandler(String defaultFailureUrl, final String usernameParameter) {
        super(defaultFailureUrl);
        this.usernameParameter = usernameParameter;
    }

    @Override
    public void onAuthenticationFailure(final HttpServletRequest request,
            final HttpServletResponse response,
            final AuthenticationException exception) throws IOException, ServletException {

        String username = request.getParameter(usernameParameter);
        if (username != null) {
            if (isUseForward()) {
                request.setAttribute(LAST_USERNAME_KEY, username);
            } else {
                HttpSession session = request.getSession(false);

                if (session != null || isAllowSessionCreation()) {
                    request.getSession().setAttribute(LAST_USERNAME_KEY,
                            username);
                }
            }
        }

        super.onAuthenticationFailure(request, response, exception);
    }
}
