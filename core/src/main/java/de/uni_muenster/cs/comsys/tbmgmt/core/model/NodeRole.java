package de.uni_muenster.cs.comsys.tbmgmt.core.model;

import de.uni_muenster.cs.comsys.tbmgmt.core.utils.EnumUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * Created by matthias on 10.03.15.
 */
public enum NodeRole {
    SERVER("Server"), SERVANT("Servant"), CLIENT("Client");

    private static final Map<String, NodeRole> nameValueMap = EnumUtil.getNameValueMap(NodeRole.class);

    private final String displayName;

    NodeRole(String displayName) {
        this.displayName = displayName;
    }

    public static NodeRole fromName(final String name) {
        return nameValueMap.get(StringUtils.upperCase(name));
    }

    public String getDisplayName() {
        return displayName;
    }
}
