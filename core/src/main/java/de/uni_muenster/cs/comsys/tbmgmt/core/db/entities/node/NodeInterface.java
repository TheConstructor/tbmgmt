package de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.GeneratedIdEntity;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.userTypes.Inet4UserType;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.userTypes.Inet6UserType;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.userTypes.MacAddressUserType;
import de.uni_muenster.cs.comsys.tbmgmt.core.model.MacAddress;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.Type;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.net.Inet4Address;
import java.net.Inet6Address;

/**
 Created by matthias on 07.10.15.
 */
@Entity
public class NodeInterface extends GeneratedIdEntity {
    private Node node;
    private String name;
    private InterfaceType type; // Entity wired, wireless, virtual
    private MacAddress macAddress;
    private Inet4Address ipv4Address;
    private Inet6Address ipv6Address;
    private boolean controlledOverThisConnection;

    public NodeInterface() {
    }

    @ManyToOne(optional = false)
    public Node getNode() {
        return node;
    }

    public void setNode(final Node node) {
        this.node = node;
    }

    @Basic
    @Column(columnDefinition = MacAddressUserType.PG_TYPE_STRING)
    @Type(type = MacAddressUserType.TYPE_STRING)
    public MacAddress getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(final MacAddress macAddress) {
        this.macAddress = macAddress;
    }

    @Basic
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @ManyToOne(optional = false)
    public InterfaceType getType() {
        return type;
    }

    public void setType(InterfaceType type) {
        this.type = type;
    }

    @Basic
    @Column(columnDefinition = Inet4UserType.PG_TYPE_STRING)
    @Type(type = Inet4UserType.TYPE_STRING)
    public Inet4Address getIpv4Address() {
        return ipv4Address;
    }

    public void setIpv4Address(final Inet4Address ipv4Address) {
        this.ipv4Address = ipv4Address;
    }

    @Basic
    @Column(columnDefinition = Inet6UserType.PG_TYPE_STRING)
    @Type(type = Inet6UserType.TYPE_STRING)
    public Inet6Address getIpv6Address() {
        return ipv6Address;
    }

    public void setIpv6Address(final Inet6Address ipv6Address) {
        this.ipv6Address = ipv6Address;
    }

    @Basic
    public boolean isControlledOverThisConnection() {
        return controlledOverThisConnection;
    }

    public void setControlledOverThisConnection(final boolean controlledOverThisConnection) {
        this.controlledOverThisConnection = controlledOverThisConnection;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString())
                                        .append("node", node)
                                        .append("name", name)
                                        .append("macAddress", macAddress)
                                        .append("ipv4Address", ipv4Address)
                                        .append("ipv6Address", ipv6Address)
                                        .append("controlledOverThisConnection", controlledOverThisConnection)
                                        .toString();
    }
}
