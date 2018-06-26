package de.uni_muenster.cs.comsys.tbmgmt.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

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

    @Override
    public String getErrorPath() {
        return "/error";
    }

    // Error page
    @RequestMapping("/error")
    public String error(HttpServletRequest request, Model model) {
        ServletRequestAttributes requestAttributes = new ServletRequestAttributes(request);
        model.addAllAttributes(errorAttributes.getErrorAttributes(requestAttributes, true));

        Object errorCode = request.getAttribute("javax.servlet.error.status_code");
        model.addAttribute("errorCode", errorCode);
        Throwable throwable = (Throwable) request.getAttribute("javax.servlet.error.exception");
        final String errorMessage;
        if (throwable != null) {
            errorMessage = throwable.getMessage();
        } else {
            errorMessage = String.valueOf(request.getAttribute("javax.servlet.error.message"));
        }
        model.addAttribute("errorMessage", errorMessage);
        final String errorRequestUri = String.valueOf(request.getAttribute("javax.servlet.error.request_uri"));
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
