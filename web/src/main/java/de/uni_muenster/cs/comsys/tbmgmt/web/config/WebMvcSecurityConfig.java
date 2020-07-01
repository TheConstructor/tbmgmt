package de.uni_muenster.cs.comsys.tbmgmt.web.config;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.UserDao;
import de.uni_muenster.cs.comsys.tbmgmt.web.support.RememberMeToFullFilter;
import de.uni_muenster.cs.comsys.tbmgmt.web.support.UsernameCachingAuthenticationFailureHandler;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.authentication.LdapAuthenticator;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.NestedLdapAuthoritiesPopulator;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

import java.security.SecureRandom;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
public class WebMvcSecurityConfig extends WebSecurityConfigurerAdapter implements BeanFactoryAware {

    private BeanFactory beanFactory;

    @Autowired
    private UserDao userDao;

    @Autowired
    private LdapConfig ldapConfig;

    @Bean
    public LdapConfig ldapConfig() {
        return new LdapConfig();
    }

    @Override
    public void setBeanFactory(final BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(
                authenticationProvider(ldapAuthenticator(contextSource()), ldapAuthoritiesPopulator(contextSource())));
    }

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        final String usernameParameter = UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_USERNAME_KEY;
        http.formLogin().withObjectPostProcessor(new ObjectPostProcessor<AuthenticationEntryPoint>() {
            @Override
            public <O extends AuthenticationEntryPoint> O postProcess(final O object) {
                // unchecked cast as we're otherwise unable to autowire de.uni_muenster.cs.comsys.tbmgmt.web
                // .controller.ErrorControllerImpl and should fail anyway
                ((SingletonBeanRegistry) beanFactory).registerSingleton("formLoginAuthenticationEntryPoint", object);
                return object;
            }
        })
                .loginPage("/login")
                .usernameParameter(usernameParameter)
                .defaultSuccessUrl("/")
                .failureHandler(
                        new UsernameCachingAuthenticationFailureHandler("/login?login_error=1", usernameParameter));

        http.logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/logoutSuccess");

        http.rememberMe().userDetailsService(userDao);

        http.addFilterAfter(new RememberMeToFullFilter(), ExceptionTranslationFilter.class);
        
        http.csrf().csrfTokenRepository(new HttpSessionCsrfTokenRepository());
    }

    @Bean
    public DefaultSpringSecurityContextSource contextSource() {
        return new DefaultSpringSecurityContextSource(ldapConfig.getProviderUrl());
    }

    @Bean
    public BindAuthenticator ldapAuthenticator(final BaseLdapPathContextSource contextSource) {
        final BindAuthenticator bindAuthenticator = new BindAuthenticator(contextSource);
        bindAuthenticator.setUserDnPatterns(new String[]{ldapConfig.getUserDnPattern()});
        return bindAuthenticator;
    }

    @Bean
    public AuthenticationProvider authenticationProvider(final LdapAuthenticator ldapAuthenticator,
                                                         final LdapAuthoritiesPopulator ldapAuthoritiesPopulator) {
        final LdapAuthenticationProvider ldapAuthenticationProvider =
                new LdapAuthenticationProvider(ldapAuthenticator, ldapAuthoritiesPopulator);
        ldapAuthenticationProvider.setUserDetailsContextMapper(getUserDetailsContextMapper());
        return ldapAuthenticationProvider;
    }

    @Bean
    public DbBackedLdapUserDetailsMapper getUserDetailsContextMapper() {
        return new DbBackedLdapUserDetailsMapper();
    }

    @Bean
    public NestedLdapAuthoritiesPopulator ldapAuthoritiesPopulator(final ContextSource contextSource) {
        return new NestedLdapAuthoritiesPopulator(contextSource, ldapConfig.getGroupSearchBase());
    }

    @Bean
    public SecureRandom secureRandom() {
        return new SecureRandom();
    }
    
    @Override
    @Bean(name = BeanIds.AUTHENTICATION_MANAGER)
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}
