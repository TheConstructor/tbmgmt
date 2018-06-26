package de.uni_muenster.cs.comsys.tbmgmt.core.model;

import de.uni_muenster.cs.comsys.tbmgmt.core.utils.EnumUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * Created by matthias on 05.12.15.
 */
public enum VariableValueType {
    SET("set"), INTEGER("integer"), DOUBLE("double");

    private static final Map<String, VariableValueType> nameValueMap =
            EnumUtil.getNameValueMap(VariableValueType.class);

    private final String displayName;


    VariableValueType(final String displayName) {
        this.displayName = displayName;
    }

    public static VariableValueType fromName(final String name) {
        return nameValueMap.get(StringUtils.upperCase(name));
    }

    public String getDisplayName() {
        return displayName;
    }
}
