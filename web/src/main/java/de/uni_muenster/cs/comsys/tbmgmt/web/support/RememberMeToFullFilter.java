package de.uni_muenster.cs.comsys.tbmgmt.web.support;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.util.ThrowableAnalyzer;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Created by matthias on 09.01.16.
 */
@Configurable
public class RememberMeToFullFilter implements Filter, BeanFactoryAware {
    private final HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
    private final AuthenticationTrustResolver authenticationTrustResolver =
            new AuthenticationTrustResolverImpl();
    private final ThrowableAnalyzer throwableAnalyzer = new ThrowableAnalyzer();
    @Autowired
    private BeanFactory beanFactory;

    public RememberMeToFullFilter() {
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
        } catch (Exception e) {
            Throwable[] causeChain = throwableAnalyzer.determineCauseChain(e);
            AccessDeniedException accessDeniedException =
                    (AccessDeniedException) throwableAnalyzer.getFirstThrowableOfType(AccessDeniedException.class,
                            causeChain);
            if (accessDeniedException != null) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authenticationTrustResolver.isRememberMe(authentication)) {
                    HttpServletRequest httpServletRequest = (HttpServletRequest) request;
                    HttpServletResponse httpServletResponse = (HttpServletResponse) response;
                    HttpSession session = httpServletRequest.getSession();

                    requestCache.saveRequest(httpServletRequest, httpServletResponse);

                    if (authentication.getPrincipal() instanceof UserDetails) {
                        session.setAttribute(UsernameCachingAuthenticationFailureHandler.LAST_USERNAME_KEY,
                                ((UserDetails) authentication.getPrincipal()).getUsername());
                    }
                    AuthenticationEntryPoint authenticationEntryPoint =
                            beanFactory.getBean(AuthenticationEntryPoint.class);
                    authenticationEntryPoint.commence(httpServletRequest, httpServletResponse,
                            new InsufficientAuthenticationException("RememberMe found"));
                    return;
                }
            }
            throw e;
        }
    }

    @Override
    public void destroy() {

    }
}
