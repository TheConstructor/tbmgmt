package de.uni_muenster.cs.comsys.tbmgmt.web.config;

import org.springframework.binding.convert.service.DefaultConversionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.webflow.config.AbstractFlowConfiguration;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.executor.FlowExecutor;
import org.springframework.webflow.mvc.builder.MvcViewFactoryCreator;
import org.springframework.webflow.mvc.servlet.FlowHandlerAdapter;
import org.springframework.webflow.mvc.servlet.FlowHandlerMapping;
import org.springframework.webflow.security.SecurityFlowExecutionListener;
import org.thymeleaf.spring5.webflow.view.AjaxThymeleafViewResolver;

import javax.persistence.EntityManagerFactory;
import java.util.Arrays;

/**
 Created by matthias on 15.02.2015.
 */
@Configuration
public class WebFlowConfig extends AbstractFlowConfiguration {

    @Bean
    public FlowHandlerMapping flowHandlerMapping(final FlowDefinitionRegistry flowRegistry) {
        final FlowHandlerMapping handlerMapping = new FlowHandlerMapping();
        handlerMapping.setOrder(-1);
        handlerMapping.setFlowRegistry(flowRegistry);
        return handlerMapping;
    }

    @Bean
    public FlowDefinitionRegistry flowRegistry(final FlowBuilderServices flowBuilderServices) {
        return getFlowDefinitionRegistryBuilder(flowBuilderServices)
                .setBasePath("/templates")
                .addFlowLocationPattern("/**/*-flow.xml").build();
    }

    @Bean
    public FlowBuilderServices flowBuilderServices(final MvcViewFactoryCreator mvcViewFactoryCreator,
                                                   final FormattingConversionService mvcConversionService) {
        return getFlowBuilderServicesBuilder()
                .setConversionService(new DefaultConversionService(mvcConversionService))
                .setViewFactoryCreator(mvcViewFactoryCreator)
                .setValidator(validator())
                .setDevelopmentMode(true)
                .build();
    }

    @Bean
    public LocalValidatorFactoryBean validator() {
        return new LocalValidatorFactoryBean();
    }

    @Bean
    public MvcViewFactoryCreator mvcViewFactoryCreator(final AjaxThymeleafViewResolver thymeleafViewResolver) {
        final MvcViewFactoryCreator factoryCreator = new MvcViewFactoryCreator();
        factoryCreator.setViewResolvers(Arrays.asList(thymeleafViewResolver));
        factoryCreator.setUseSpringBeanBinding(true);
        return factoryCreator;
    }

    @Bean
    public FlowHandlerAdapter flowHandlerAdapter(final FlowExecutor flowExecutor) {
        final FlowHandlerAdapter handlerAdapter = new FlowHandlerAdapter();
        handlerAdapter.setFlowExecutor(flowExecutor);
        handlerAdapter.setSaveOutputToFlashScopeOnRedirect(true);
        return handlerAdapter;
    }

    @Bean
    public FlowExecutor flowExecutor(final FlowDefinitionRegistry flowRegistry,
                                     final EntityManagerFactory entityManagerFactory,
                                     final PlatformTransactionManager transactionManager) {
        return getFlowExecutorBuilder(flowRegistry)
                .addFlowExecutionListener(new SecurityFlowExecutionListener())
                // as per https://jira.spring.io/browse/SWF-1525 JpaFlowExecutionListener leaks DB-connections.
                // we use jpaProperties.put(AvailableSettings.ENABLE_LAZY_LOAD_NO_TRANS, "true") in DbSpringConfig as
                // a work-around for this
                //.addFlowExecutionListener(new JpaFlowExecutionListener(entityManagerFactory, transactionManager))
                .build();
    }
}
