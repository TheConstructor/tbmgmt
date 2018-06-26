package de.uni_muenster.cs.comsys.tbmgmt.web.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Locale;

/**
 * Based upon {@link org.springframework.web.servlet.DispatcherServlet}
 */
public class InstantViewRenderer {

    private static final Logger logger = LoggerFactory.getLogger(InstantViewRenderer.class);

    @Autowired
    private LocaleResolver localeResolver;

    @Autowired
    private List<ViewResolver> viewResolvers;

    /**
     * Render the given ModelAndView.
     * <p>This is the last stage in handling a request. It may involve resolving the view by name.
     *
     * @param mv       the ModelAndView to render
     * @param request  current HTTP servlet request
     * @param response current HTTP servlet response
     * @throws ServletException if view is missing or cannot be resolved
     * @throws Exception        if there's a problem rendering the view
     */
    public void render(final ModelAndView mv, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        // Determine locale for request and apply it to the response.
        final Locale locale = this.localeResolver.resolveLocale(request);
        response.setLocale(locale);

        final View view;
        if (mv.isReference()) {
            // We need to resolve the view name.
            view = resolveViewName(mv.getViewName(), locale);
            if (view == null) {
                throw new ServletException("Could not resolve view with name '" + mv.getViewName() + "'");
            }
        } else {
            // No need to lookup: the ModelAndView object contains the actual View object.
            view = mv.getView();
            if (view == null) {
                throw new ServletException("ModelAndView [" + mv + "] neither contains a view name nor a View object ");
            }
        }

        // Delegate to the View object for rendering.
        if (logger.isDebugEnabled()) {
            logger.debug("Rendering view [" + view + "] in InstantViewRenderer");
        }
        try {
            view.render(mv.getModel(), request, response);
        } catch (final Exception ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("Error rendering view [" + view + "] in InstantViewRenderer", ex);
            }
            throw ex;
        }
    }

    /**
     * Resolve the given view name into a View object (to be rendered).
     * <p>The default implementations asks all ViewResolvers of this dispatcher.
     * Can be overridden for custom resolution strategies, potentially based on
     * specific model attributes or request parameters.
     *
     * @param viewName the name of the view to resolve
     * @param locale   the current locale
     * @return the View object, or {@code null} if none found
     * @throws Exception if the view cannot be resolved
     *                   (typically in case of problems creating an actual View object)
     * @see ViewResolver#resolveViewName
     */
    protected View resolveViewName(final String viewName, final Locale locale) throws Exception {

        for (final ViewResolver viewResolver : this.viewResolvers) {
            final View view = viewResolver.resolveViewName(viewName, locale);
            if (view != null) {
                return view;
            }
        }
        return null;
    }
}
