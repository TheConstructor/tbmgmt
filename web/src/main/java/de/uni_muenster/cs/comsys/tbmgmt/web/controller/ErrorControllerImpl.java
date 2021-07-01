package de.uni_muenster.cs.comsys.tbmgmt.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;

/**
 * Created by matthias on 17.02.2015.
 */
@Controller
public class ErrorControllerImpl implements ErrorController {

    public static final Logger LOG = LoggerFactory.getLogger(ErrorControllerImpl.class);

    private final ErrorAttributes errorAttributes;

    @Autowired
    public ErrorControllerImpl(ErrorAttributes errorAttributes) {
        Assert.notNull(errorAttributes, "ErrorAttributes must not be null");
        this.errorAttributes = errorAttributes;
    }

    // Error page
    @RequestMapping("/error")
    public String error(WebRequest request, Model model) {
        model.addAllAttributes(
                errorAttributes.getErrorAttributes(request,
                        ErrorAttributeOptions.of(
                                ErrorAttributeOptions.Include.values())));
    
        Object errorCode = request.getAttribute("javax.servlet.error.status_code", RequestAttributes.SCOPE_REQUEST);
        model.addAttribute("errorCode", errorCode);
        Throwable throwable = (Throwable) request
                .getAttribute("javax.servlet.error.exception", RequestAttributes.SCOPE_REQUEST);
        final String errorMessage;
        if (throwable != null) {
            errorMessage = throwable.getMessage();
        } else {
            errorMessage = String
                    .valueOf(request.getAttribute("javax.servlet.error.message", RequestAttributes.SCOPE_REQUEST));
        }
        model.addAttribute("errorMessage", errorMessage);
        final String errorRequestUri = String
                .valueOf(request.getAttribute("javax.servlet.error.request_uri", RequestAttributes.SCOPE_REQUEST));
        model.addAttribute("requestUri", errorRequestUri);
        String logMessage = "Got error code " + errorCode + " while processing " + errorRequestUri;
        if (throwable != null) {
            LOG.error(logMessage, throwable);
        } else {
            LOG.error(logMessage + ": " + errorMessage);
        }
        return "errorView";
    }
}
