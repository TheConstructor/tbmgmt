package de.uni_muenster.cs.comsys.tbmgmt.web.support;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.User;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;

import java.util.Collection;
import java.util.function.BiFunction;

/**
 * Created by matthias on 04.01.16.
 */
public class CallbackBasedAccessDecisionVoter<T> implements AccessDecisionVoter<T> {
    private final Class<T> clazz;
    private final BiFunction<Authentication, T, Boolean> callback;

    public CallbackBasedAccessDecisionVoter(Class<T> clazz,
                                            BiFunction<Authentication, T, Boolean> callback) {
        this.clazz = clazz;
        this.callback = callback;
    }

    public static <T> CallbackBasedAccessDecisionVoter<T> createUserBased(Class<T> clazz,
                                                                          BiFunction<User, T, Boolean> callback) {
        return new CallbackBasedAccessDecisionVoter<>(clazz, ((authentication, t) -> {
            if (authentication.getPrincipal() instanceof User) {
                User user = (User) authentication.getPrincipal();
                return callback.apply(user, t);
            }
            return false;
        }));
    }

    @Override
    public boolean supports(ConfigAttribute attribute) {
        return false;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return this.clazz.isAssignableFrom(clazz);
    }

    @Override
    public int vote(Authentication authentication, T object, Collection<ConfigAttribute> attributes) {
        return callback.apply(authentication, object) ? ACCESS_GRANTED : ACCESS_DENIED;
    }
}
