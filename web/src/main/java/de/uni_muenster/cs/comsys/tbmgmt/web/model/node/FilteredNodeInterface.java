package de.uni_muenster.cs.comsys.tbmgmt.web.model.node;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.node.InterfaceTypeDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.InterfaceType;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.Node;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.NodeInterface;
import de.uni_muenster.cs.comsys.tbmgmt.core.model.MacAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.net.Inet4Address;
import java.net.Inet6Address;

/**
 Created by matthias on 21.10.15.
 */
@Configurable
public class FilteredNodeInterface implements Serializable {
    @Autowired
    private transient InterfaceTypeDao interfaceTypeDao;

    private final Node          node;
    private final NodeInterface nodeInterface;

    public FilteredNodeInterface(final Node node, final NodeInterface nodeInterface) {
        this.node = node;
        this.nodeInterface = nodeInterface;
    }

    @NotBlank
    public String getName() {
        return nodeInterface.getName();
    }

    public void setName(final String name) {
        nodeInterface.setName(name);
    }

    @NotNull
    public String getType() {
        final InterfaceType type = nodeInterface.getType();
        if (type != null) {
            return type.getName();
        } else {
            return null;
        }
    }

    public void setType(final String type) {
        if (type == null) {
            nodeInterface.setType(null);
        } else {
            nodeInterface.setType(interfaceTypeDao.getByName(type));
        }
    }

    public MacAddress getMacAddress() {
        return nodeInterface.getMacAddress();
    }

    public void setMacAddress(final MacAddress macAddress) {
        nodeInterface.setMacAddress(macAddress);
    }

    public Inet4Address getIpv4Address() {
        return nodeInterface.getIpv4Address();
    }

    public void setIpv4Address(final Inet4Address ipv4Address) {
        nodeInterface.setIpv4Address(ipv4Address);
    }

    public Inet6Address getIpv6Address() {
        return nodeInterface.getIpv6Address();
    }

    public void setIpv6Address(final Inet6Address ipv6Address) {
        nodeInterface.setIpv6Address(ipv6Address);
    }

    public boolean isControlledOverThisConnection() {
        return nodeInterface.isControlledOverThisConnection();
    }

    public void setControlledOverThisConnection(final boolean controlledOverThisConnection) {
        nodeInterface.setControlledOverThisConnection(controlledOverThisConnection);
    }
}
