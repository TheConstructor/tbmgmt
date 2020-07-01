package de.uni_muenster.cs.comsys.tbmgmt.web;

import de.uni_muenster.cs.comsys.tbmgmt.web.config.WebFlowConfig;
import de.uni_muenster.cs.comsys.tbmgmt.web.config.WebMvcConfig;
import de.uni_muenster.cs.comsys.tbmgmt.web.config.WebMvcSecurityConfig;
import de.uni_muenster.cs.comsys.tbmgmt.web.controller.EditExperimentFlowHandler;
import de.uni_muenster.cs.comsys.tbmgmt.web.controller.ViewExperimentFlowHandler;
import de.uni_muenster.cs.comsys.tbmgmt.web.controller.admin.EditEvaluationScriptFlowHandler;
import de.uni_muenster.cs.comsys.tbmgmt.web.controller.admin.EditInterfaceTypeFlowHandler;
import de.uni_muenster.cs.comsys.tbmgmt.web.controller.admin.EditNodeFlowHandler;
import de.uni_muenster.cs.comsys.tbmgmt.web.controller.admin.EditNodeTypeFlowHandler;
import de.uni_muenster.cs.comsys.tbmgmt.web.controller.admin.EditTagFlowHandler;
import de.uni_muenster.cs.comsys.tbmgmt.web.controller.admin.EditTestbedFlowHandler;
import de.uni_muenster.cs.comsys.tbmgmt.web.support.InstantViewRenderer;
import de.uni_muenster.cs.comsys.tbmgmt.web.support.WebJarUrlUtil;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.webjars.WebJarAssetLocator;

/**
 * Created by matthias on 15.02.2015.
 */
@Configuration
@Import({WebMvcConfig.class, WebFlowConfig.class, WebMvcSecurityConfig.class})
public class WebSpringConfig {

    @Bean
    public WebJarUrlUtil webJarUrlUtil(final WebJarAssetLocator webJarAssetLocator,
                                       final ResourcePatternResolver resourcePatternResolver) {
        return new WebJarUrlUtil(webJarAssetLocator, resourcePatternResolver);
    }

    @Bean
    public WebJarAssetLocator webJarAssetLocator() {
        return new WebJarAssetLocator();
    }

    @Bean
    public DefaultErrorAttributes errorAttributes() {
        return new DefaultErrorAttributes();
    }

    @Bean(name = "editExperiment")
    public EditExperimentFlowHandler editExperimentFlowHandler() {
        return new EditExperimentFlowHandler();
    }

    @Bean(name = "viewExperiment")
    public ViewExperimentFlowHandler viewExperimentFlowHandler() {
        return new ViewExperimentFlowHandler();
    }

    // first name is view-mapping, second enables access from SpEL
    @Bean(name = {"admin/editEvaluationScript", "admin_editEvaluationScript"})
    public EditEvaluationScriptFlowHandler editEvaluationScript() {
        return new EditEvaluationScriptFlowHandler();
    }

    // first name is view-mapping, second enables access from SpEL
    @Bean(name = {"admin/editInterfaceType", "admin_editInterfaceType"})
    public EditInterfaceTypeFlowHandler editInterfaceType() {
        return new EditInterfaceTypeFlowHandler();
    }

    // first name is view-mapping, second enables access from SpEL
    @Bean(name = {"admin/editNode", "admin_editNode"})
    public EditNodeFlowHandler editNodeFlowHandler() {
        return new EditNodeFlowHandler();
    }

    // first name is view-mapping, second enables access from SpEL
    @Bean(name = {"admin/editNodeType", "admin_editNodeType"})
    public EditNodeTypeFlowHandler editNodeType() {
        return new EditNodeTypeFlowHandler();
    }

    // first name is view-mapping, second enables access from SpEL
    @Bean(name = {"admin/editTag", "admin_editTag"})
    public EditTagFlowHandler editTag() {
        return new EditTagFlowHandler();
    }

    // first name is view-mapping, second enables access from SpEL
    @Bean(name = {"admin/editTestbed", "admin_editTestbed"})
    public EditTestbedFlowHandler editTestbed() {
        return new EditTestbedFlowHandler();
    }

    @Bean
    public InstantViewRenderer instantViewRenderer() {
        return new InstantViewRenderer();
    }
}
