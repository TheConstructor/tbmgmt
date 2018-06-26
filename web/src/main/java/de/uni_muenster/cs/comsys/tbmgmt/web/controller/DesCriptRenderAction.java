package de.uni_muenster.cs.comsys.tbmgmt.web.controller;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.Experiment;
import de.uni_muenster.cs.comsys.tbmgmt.core.des_cript.DesCriptSpringConfig;
import de.uni_muenster.cs.comsys.tbmgmt.core.des_cript.DesCriptWriter;
import de.uni_muenster.cs.comsys.tbmgmt.core.schema.ResultsType;
import de.uni_muenster.cs.comsys.tbmgmt.core.utils.TbmgmtUtil;
import de.uni_muenster.cs.comsys.tbmgmt.web.support.TbmgmtWebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.context.ExternalContext;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.xml.transform.StringResult;

import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by matthias on 20.05.15.
 */
@Controller
public class DesCriptRenderAction extends AbstractAction {

    private static final Logger LOG = LoggerFactory.getLogger(DesCriptRenderAction.class);
    public static final MediaType MEDIA_TYPE = new MediaType("text", "xml", DesCriptSpringConfig.DES_CRIPT_ENCODING);

    @Autowired
    private DesCriptWriter desCriptWriter;

    @Override

    protected Event doExecute(final RequestContext context) throws Exception {
        final Experiment experiment =
                context.getFlowScope().get(context.getAttributes().getString("flowVariable"), Experiment.class);
        if (experiment == null) {
            return error();
        }
        final ExternalContext externalContext = context.getExternalContext();
        return marshall(desCriptWriter, experiment, (HttpServletResponse) externalContext.getNativeResponse(),
                () -> {
                    externalContext.recordResponseComplete();
                    return success();
                }, exception -> {
                    LOG.error("Could not output DEScript", exception);
                    return error(exception);
                });
    }

    public static <T> T marshall(final DesCriptWriter desCriptWriter, final Experiment experiment,
                                 final HttpServletResponse response, final Supplier<?
            extends T> onSuccess, final Function<Exception, ? extends T> onException) {
        try {
            // Marshal early so we can handle exceptions gracefully
            final StringResult result = new StringResult();
            desCriptWriter.write(experiment, result);

            applyHeader(experiment, response);
            try (OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream(),
                    DesCriptSpringConfig.DES_CRIPT_ENCODING)) {
                writer.write(result.toString());
            }

            return onSuccess.get();
        } catch (final IOException e) {
            return onException.apply(e);
        }
    }

    public static <T> T marshall(final DesCriptWriter desCriptWriter, final Experiment experiment,
                                 final ResultsType resultsType, final HttpServletResponse response, final Supplier<?
            extends T> onSuccess, final Function<Exception, ? extends T> onException) {
        try {
            applyHeader(experiment, response);

            try (OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream(),
                    DesCriptSpringConfig.DES_CRIPT_ENCODING)) {

                // Write directly so we don't need mem to store all result strings
                desCriptWriter.write(experiment, resultsType, new StreamResult(writer));
            }

            return onSuccess.get();
        } catch (final IOException e) {
            return onException.apply(e);
        }
    }

    protected static void applyHeader(final Experiment experiment, final HttpServletResponse response) {
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MEDIA_TYPE);
        httpHeaders.set(HttpHeaders.CONTENT_DISPOSITION,
                TbmgmtUtil.asContentDisposition(experiment.getName() + "-DEScript.xml"));
        TbmgmtWebUtils.setHeaders(response, httpHeaders);
    }
}
