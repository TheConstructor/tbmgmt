package de.uni_muenster.cs.comsys.tbmgmt.experiment_control.support;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.MustacheException;
import com.samskivert.mustache.Template;
import de.uni_muenster.cs.comsys.tbmgmt.core.utils.TbmgmtUtil;
import org.springframework.boot.autoconfigure.mustache.MustacheResourceTemplateLoader;

import java.nio.file.Path;
import java.util.Map;

/**
 * Created by matthias on 17.02.16.
 */
public final class MustacheUtils {
    public static Template getTemplate(final Mustache.Compiler compiler,
                                       final MustacheResourceTemplateLoader templateLoader, final String templateName) {
        try {
            return compiler.compile(templateLoader.getTemplate(templateName));
        } catch (final MustacheException e) {
            throw new IllegalStateException("Could not compile " + templateName + "-template", e);
        } catch (final Exception e) {
            throw new IllegalStateException("Could not load " + templateName + "-template", e);
        }
    }

    /**
     * Replaces the contents of a file with the provided template.
     *
     * @return {@code true}, if the file was changed
     */
    public static boolean renderTemplateToPath(final Template template, final Map<String, ?> context,
                                               final Path targetPath) {
        final String result;
        try {
            result = template.execute(context);
        } catch (final MustacheException e) {
            throw new IllegalStateException("Could not render template for " + targetPath, e);
        }
        return TbmgmtUtil.ensureFileContains(targetPath, result);
    }
}
