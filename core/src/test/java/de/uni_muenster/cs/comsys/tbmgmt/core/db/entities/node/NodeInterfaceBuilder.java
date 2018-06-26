package de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node;

import de.uni_muenster.cs.comsys.tbmgmt.core.model.MacAddress;
import de.uni_muenster.cs.comsys.tbmgmt.core.utils.formatter.Inet4AddressFormatter;
import de.uni_muenster.cs.comsys.tbmgmt.core.utils.formatter.Inet6AddressFormatter;
import de.uni_muenster.cs.comsys.tbmgmt.core.utils.formatter.MacAddressFormatter;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.text.ParseException;

/**
 * Created by matthias on 29.02.16.
 */
public class NodeInterfaceBuilder {
    private static final MacAddressFormatter MAC_ADDRESS_FORMATTER = new MacAddressFormatter();
    private static final Inet4AddressFormatter INET_4_ADDRESS_FORMATTER = new Inet4AddressFormatter();
    private static final Inet6AddressFormatter INET_6_ADDRESS_FORMATTER = new Inet6AddressFormatter();
    private final NodeInterface nodeInterface;

    public NodeInterfaceBuilder() {
        nodeInterface = new NodeInterface();
    }

    public NodeInterfaceBuilder node(final Node node) {
        nodeInterface.setNode(node);
        return this;
    }

    public NodeInterfaceBuilder name(final String name) {
        nodeInterface.setName(name);
        return this;
    }

    public NodeInterfaceBuilder type(final InterfaceType type) {
        nodeInterface.setType(type);
        return this;
    }

    public NodeInterfaceBuilder macAddress(final MacAddress macAddress) {
        nodeInterface.setMacAddress(macAddress);
        return this;
    }

    public NodeInterfaceBuilder macAddress(final String macAddress) throws ParseException {
        nodeInterface.setMacAddress(MAC_ADDRESS_FORMATTER.parse(macAddress, null));
        return this;
    }

    public NodeInterfaceBuilder ipv4Address(final Inet4Address ipv4Address) {
        nodeInterface.setIpv4Address(ipv4Address);
        return this;
    }

    public NodeInterfaceBuilder ipv4Address(final String ipv4Address) throws ParseException {
        nodeInterface.setIpv4Address(INET_4_ADDRESS_FORMATTER.parse(ipv4Address, null));
        return this;
    }

    public NodeInterfaceBuilder ipv6Address(final Inet6Address ipv6Address) {
        nodeInterface.setIpv6Address(ipv6Address);
        return this;
    }

    public NodeInterfaceBuilder ipv6Address(final String ipv6Address) throws ParseException {
        nodeInterface.setIpv6Address(INET_6_ADDRESS_FORMATTER.parse(ipv6Address, null));
        return this;
    }

    public NodeInterfaceBuilder controlledOverThisConnection(final boolean controlledOverThisConnection) {
        nodeInterface.setControlledOverThisConnection(controlledOverThisConnection);
        return this;
    }

    public NodeInterface build() {
        return nodeInterface;
    }
}
