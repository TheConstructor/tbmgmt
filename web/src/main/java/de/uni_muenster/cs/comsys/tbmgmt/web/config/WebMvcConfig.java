package de.uni_muenster.cs.comsys.tbmgmt.web.config;

import de.uni_muenster.cs.comsys.tbmgmt.core.CoreSpringConfig;
import de.uni_muenster.cs.comsys.tbmgmt.core.config.FileConfig;
import de.uni_muenster.cs.comsys.tbmgmt.web.controller.GreetingController;
import de.uni_muenster.cs.comsys.tbmgmt.web.support.TbmgmtWebUtils;
import de.uni_muenster.cs.comsys.tbmgmt.web.support.WebJarUrlUtil;
import nz.net.ultraq.thymeleaf.LayoutDialect;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.Ordered;
import org.springframework.format.FormatterRegistrar;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.CacheControl;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.http.converter.xml.MarshallingHttpMessageConverter;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.web.servlet.resource.CssLinkResourceTransformer;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;
import org.thymeleaf.dialect.IDialect;
import org.thymeleaf.extras.conditionalcomments.dialect.ConditionalCommentsDialect;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import org.thymeleaf.extras.springsecurity4.dialect.SpringSecurityDialect;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.spring4.dialect.SpringStandardDialect;
import org.thymeleaf.spring4.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring4.view.AjaxThymeleafViewResolver;
import org.thymeleaf.spring4.view.FlowAjaxThymeleafView;
import org.thymeleaf.templatemode.TemplateMode;

import javax.servlet.MultipartConfigElement;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by matthias on 14.02.2015.
 */
@Configuration
@EnableWebMvc
@ComponentScan(basePackageClasses = GreetingController.class)
@Import({WebFlowConfig.class, CoreSpringConfig.class})
@EnableConfigurationProperties
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private Jaxb2Marshaller jaxb2Marshaller;

    @Autowired
    private FormatterRegistrar coreFormatterRegistrar;
    
    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void addFormatters(final FormatterRegistry registry) {
        coreFormatterRegistrar.registerFormatters(registry);
    }

    @Override
    public void extendMessageConverters(final List<HttpMessageConverter<?>> converters) {
        converters.add(0, new MarshallingHttpMessageConverter(jaxb2Marshaller, jaxb2Marshaller));
        final Jaxb2RootElementHttpMessageConverter jaxb2RootElementHttpMessageConverter = new
                Jaxb2RootElementHttpMessageConverter();
    }

    @Override
    public void addViewControllers(final ViewControllerRegistry registry) {
        registry.addRedirectViewController("/", "/greeting");
        registry.addViewController("/login");
        registry.addViewController("/logoutSuccess");
    }

    @Override
    public void configureViewResolvers(final ViewResolverRegistry registry) {
        // registry.viewResolver(thymeleafViewResolver());
    }

    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {

        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addTransformer(new CssLinkResourceTransformer());
        registry.addResourceHandler("/resources/**")
                .addResourceLocations("classpath:/META-INF/web-resources/")
                .resourceChain(true)
                .addTransformer(new CssLinkResourceTransformer());

        // URLs of WebJars contain versions and content is checksumed -> no Transformer, but Cache-Headers
        registry.addResourceHandler(WebJarUrlUtil.WEBJARS_URL_PREFIX + "/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/")
                .setCacheControl(CacheControl.maxAge(90, TimeUnit.DAYS).cachePublic())
                .resourceChain(true);
    }

    @Override
    public void configureDefaultServletHandling(final DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

    @Bean
    public AjaxThymeleafViewResolver thymeleafViewResolver() {
        final AjaxThymeleafViewResolver viewResolver = new AjaxThymeleafViewResolver();
        // Output-Encoding
        viewResolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        viewResolver.setViewClass(FlowAjaxThymeleafView.class);
        viewResolver.setTemplateEngine(templateEngine());
        return viewResolver;
    }

    @Bean
    public SpringTemplateEngine templateEngine() {

        final Set<IDialect> dialects = new LinkedHashSet<>();
        dialects.add(new LayoutDialect());
        dialects.add(new SpringStandardDialect());
        dialects.add(new SpringSecurityDialect());
        dialects.add(new ConditionalCommentsDialect());
        dialects.add(new Java8TimeDialect());

        final SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(defaultTemplateResolver());
        templateEngine.setAdditionalDialects(dialects);
        return templateEngine;
    }

    @Bean
    public SpringResourceTemplateResolver defaultTemplateResolver() {
        final SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
        // Template-File-Encoding
        templateResolver.setApplicationContext(applicationContext);
        templateResolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        templateResolver.setPrefix("/WEB-INF/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        return templateResolver;
    }

    @Bean
    public ResourceBundleMessageSource messageSource() {
        final ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        return messageSource;
    }

    @Bean
    public ContentNegotiatingViewResolver viewResolver(final BeanFactory beanFactory) {
        final ContentNegotiatingViewResolver resolver = new ContentNegotiatingViewResolver();
        resolver.setContentNegotiationManager(beanFactory.getBean(ContentNegotiationManager.class));
        // ContentNegotiatingViewResolver uses all the other view resolvers to locate
        // a view so it should have a high precedence
        resolver.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return resolver;
    }

    @Bean(name = DispatcherServlet.LOCALE_RESOLVER_BEAN_NAME)
    public SessionLocaleResolver localeResolver() {
        return new SessionLocaleResolver();
    }

    @Bean
    public MultipartConfigElement multipartConfigElement(final MultipartProperties multipartProperties) {
        return multipartProperties.createMultipartConfig();
    }

    @Bean
    public MultipartProperties multipartProperties(final FileConfig fileConfig) {
        final MultipartProperties multipartProperties = new MultipartProperties();
        multipartProperties.setLocation(fileConfig.getUploadTempPath().toString());
        multipartProperties.setMaxFileSize("10MB");
        multipartProperties.setMaxRequestSize("20MB");
        return multipartProperties;
    }

    @Bean(name = DispatcherServlet.MULTIPART_RESOLVER_BEAN_NAME)
    public StandardServletMultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

    @Bean
    public TbmgmtWebUtils tbmgmtWebUtils() {
        return new TbmgmtWebUtils();
    }
}
