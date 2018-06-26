package de.uni_muenster.cs.comsys.tbmgmt.web.support;

import org.springframework.binding.validation.ValidationContext;
import org.springframework.webflow.validation.ValidationHelper;

/**
 * Created by matthias on 26.02.16.
 */
public interface Validateable {
    /**
     * For top-level models in web-flows this will be called by
     * {@link ValidationHelper#invokeDefaultValidateMethod(java.lang.Object)}, for {@link Validateable}-objects
     * within them they need to handle delegation.
     *
     * @see PrefixedValidationContext
     */
    void validate(ValidationContext context);
}
