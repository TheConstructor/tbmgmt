package de.uni_muenster.cs.comsys.tbmgmt.web.support;

import org.springframework.binding.message.DefaultMessageResolver;
import org.springframework.binding.message.Severity;

import java.text.MessageFormat;

/**
 * Created by matthias on 25.02.16.
 */
public class MessageFormatMessageResolver extends DefaultMessageResolver {
    public MessageFormatMessageResolver(final String source, final String[] codes, final Severity severity,
                                        final Object[] args) {
        this(source, codes, severity, args, getDefaultText(codes));
    }

    public MessageFormatMessageResolver(final String source, final String[] codes, final Severity severity,
                                        final Object[] args, final String defaultText) {
        super(source, codes, severity, args, defaultText);
    }

    public static String getDefaultText(final String[] codes) {
        return "Could not find " + codes[0];
    }

    @Override
    protected String postProcessMessageText(final String text) {
        return new MessageFormat(text).format(getArguments());
    }
}
