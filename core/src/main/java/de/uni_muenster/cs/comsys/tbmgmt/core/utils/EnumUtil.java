package de.uni_muenster.cs.comsys.tbmgmt.core.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by matthias on 14.03.15.
 */
public final class EnumUtil {

    public static <E extends Enum<E>> Map<String, E> getNameValueMap(final Class<E> clazz) {
        return getValueMap(E::name, clazz);
    }

    public static <E extends Enum<E>, K> Map<K, E> getValueMap(final Function<E, K> selector, final Class<E> clazz) {
        final HashMap<K, E> map = new HashMap<>();
        for (final E value : clazz.getEnumConstants()) {
            map.put(selector.apply(value), value);
        }
        return Collections.unmodifiableMap(map);
    }

    public static <E extends Enum<E>> String nullsafeGetName(final E value) {
        return value == null ? null : value.name();
    }
}
