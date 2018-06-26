package de.uni_muenster.cs.comsys.tbmgmt.web.support;

import org.apache.commons.lang3.StringUtils;
import org.springframework.binding.message.Message;
import org.springframework.binding.message.MessageContext;
import org.springframework.binding.message.MessageCriteria;
import org.springframework.binding.message.MessageResolver;
import org.springframework.binding.validation.ValidationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

/**
 * Created by matthias on 25.02.16.
 */
public class PrefixedValidationContext implements ValidationContext {
    private final ValidationContext context;
    private final String prefix;

    public PrefixedValidationContext(final ValidationContext context, final String prefix) {
        this.context = context;
        this.prefix = prefix;
    }

    public static <T extends Validateable> void validateListWithPrefix(final String listName, final List<T> list,
                                                                       final ValidationContext context) {
        if (list == null) {
            return;
        }
        for (final ListIterator<T> listIterator = list.listIterator(); listIterator.hasNext(); ) {
            final String prefix = listName + "[" + listIterator.nextIndex() + "].";
            listIterator.next().validate(new PrefixedValidationContext(context, prefix));
        }
    }

    @Override
    public MessageContext getMessageContext() {
        return new MessageContext() {
            @Override
            public Message[] getAllMessages() {
                return context
                        .getMessageContext()
                        .getMessagesByCriteria(
                                message -> !(message.getSource() instanceof String) || StringUtils.startsWith(
                                        (String) message.getSource(), prefix));
            }

            @Override
            public Message[] getMessagesBySource(final Object source) {
                return context.getMessageContext().getMessagesBySource(prefix + source);
            }

            @Override
            public Message[] getMessagesByCriteria(final MessageCriteria criteria) {
                return Arrays.stream(context.getMessageContext().getMessagesByCriteria(message -> {
                    final Message unprefixedMessage = getUnprefixedMessage(prefix, message);
                    return criteria.test(unprefixedMessage);
                })).map(m -> getUnprefixedMessage(prefix, m)).toArray(Message[]::new);
            }

            @Override
            public boolean hasErrorMessages() {
                return context.getMessageContext().hasErrorMessages();
            }

            @Override
            public void addMessage(final MessageResolver messageResolver) {
                if (messageResolver instanceof MessageSourceResolvable) {
                    //noinspection unchecked
                    context.getMessageContext().addMessage(new ResolvableMessageResolver(prefix, messageResolver));
                } else {
                    context
                            .getMessageContext()
                            .addMessage((messageSource, locale) -> getPrefixedMessage(prefix,
                                    messageResolver.resolveMessage(messageSource, locale)));
                }
            }

            @Override
            public void clearMessages() {
                context.getMessageContext().clearMessages();
            }
        };
    }

    @Override
    public Principal getUserPrincipal() {
        return context.getUserPrincipal();
    }

    @Override
    public String getUserEvent() {
        return context.getUserEvent();
    }

    @Override
    public Object getUserValue(final String property) {
        return context.getUserValue(prefix + property);
    }

    public static Message getPrefixedMessage(final String prefix, final Message message) {
        final Object source = message.getSource();
        final Object prefixedSource = source instanceof String ? prefix + source : source;
        return new Message(prefixedSource, message.getText(), message.getSeverity());
    }

    public static Message getUnprefixedMessage(final String prefix, final Message message) {
        final Object source = message.getSource();
        final Object unprefixedSource =
                source instanceof String ? StringUtils.removeStart((String) source, prefix) : source;
        return new Message(unprefixedSource, message.getText(), message.getSeverity());
    }

    private static class ResolvableMessageResolver<R extends MessageResolver & MessageSourceResolvable>
            implements MessageResolver, MessageSourceResolvable {
        private final String prefix;
        private final R messageResolver;

        public ResolvableMessageResolver(final String prefix, final R messageResolver) {
            this.prefix = prefix;
            this.messageResolver = messageResolver;
        }

        @Override
        public Message resolveMessage(final MessageSource messageSource, final Locale locale) {
            return getPrefixedMessage(prefix, messageResolver.resolveMessage(messageSource, locale));
        }

        @Override
        public String[] getCodes() {
            return messageResolver.getCodes();
        }

        @Override
        public Object[] getArguments() {
            return messageResolver.getArguments();
        }

        @Override
        public String getDefaultMessage() {
            return messageResolver.getDefaultMessage();
        }
    }
}
