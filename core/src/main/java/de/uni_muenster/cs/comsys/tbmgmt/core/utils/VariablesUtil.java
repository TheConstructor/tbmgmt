package de.uni_muenster.cs.comsys.tbmgmt.core.utils;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.ExperimentNodeGroup;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.Node;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.NodeInterface;
import de.uni_muenster.cs.comsys.tbmgmt.core.model.MacAddress;
import org.apache.commons.lang3.StringUtils;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by matthias on 28.02.16.
 */
public final class VariablesUtil {
    public static String replaceVariables(final String input, final Map<String, String> variableMap) {
        if (input == null || input.isEmpty() || variableMap == null || variableMap.isEmpty()) {
            return input;
        }
        final String[] variablePlaceholders = new String[variableMap.size()];
        final String[] variableReplacements = new String[variableMap.size()];
        int i = 0;
        for (final Map.Entry<String, String> variable : variableMap.entrySet()) {
            variablePlaceholders[i] = "{" + variable.getKey() + "}";
            variableReplacements[i] = variable.getValue();
            i++;
        }
        return StringUtils.replaceEach(input, variablePlaceholders, variableReplacements);
    }

    public static Map<String, List<String>> extractNodeGroupReferences(final String input,
                                                                       final Function<String, ExperimentNodeGroup>
                                                                               nodeGroupResolver) {
        final Map<String, List<String>> nodeGroupReferences = new HashMap<>();
        for (int startPos = input.indexOf('{'); startPos >= 0; startPos = input.indexOf('{', startPos + 1)) {

            final int endPos = input.indexOf('}', startPos + 1);
            if (endPos >= 0) {
                final String fullReference = input.substring(startPos + 1, endPos);
                int endOfName = fullReference.length();
                ExperimentNodeGroup nodeGroup = null;
                while (endOfName > 0
                        && (nodeGroup = nodeGroupResolver.apply(fullReference.substring(0, endOfName))) == null) {
                    endOfName = fullReference.lastIndexOf('.', endOfName - 1);
                }
                if (nodeGroup != null) {
                    final String postfix = fullReference.substring(endOfName);
                    final int interfaceSeparator = postfix.indexOf('.', 1);
                    final String interfaceName;
                    final String addressType;
                    if (interfaceSeparator > 0) {
                        interfaceName = postfix.substring(1, interfaceSeparator);
                        addressType = postfix.substring(interfaceSeparator + 1);
                    } else {
                        interfaceName = StringUtils.removeStart(postfix, ".");
                        addressType = "";
                    }
                    final Stream<Node> nodeStream = nodeGroup.getNodes().stream();
                    final Stream<NodeInterface> nodeInterfaceStream = filterInterfaces(nodeStream, interfaceName);
                    final Stream<String> addressStream = getInterfaceAddresses(nodeInterfaceStream, addressType);
                    final List<String> values = addressStream.collect(Collectors.toList());
                    nodeGroupReferences.put(fullReference, values);
                    startPos = endPos;
                }
            }
        }
        return nodeGroupReferences;
    }

    private static Stream<NodeInterface> filterInterfaces(final Stream<Node> nodeStream,
                                                          final String interfaceNameOrType) {
        final Stream<NodeInterface> nodeInterfaceStream;
        if (interfaceNameOrType.startsWith("*")) {
            final String interfaceType = interfaceNameOrType.substring(1);
            nodeInterfaceStream = nodeStream.flatMap(node -> node
                    .getInterfaces()
                    .stream()
                    .filter(nodeInterface -> StringUtils.equalsIgnoreCase(nodeInterface.getType().getName(),
                            interfaceType)));
        } else if (StringUtils.isNotBlank(interfaceNameOrType)) {
            nodeInterfaceStream = nodeStream.flatMap(node -> node
                    .getInterfaces()
                    .stream()
                    .filter(nodeInterface -> StringUtils.equalsIgnoreCase(nodeInterface.getName(),
                            interfaceNameOrType)));
        } else {
            nodeInterfaceStream = nodeStream.flatMap(node -> node
                    .getInterfaces()
                    .stream()
                    .filter(nodeInterface -> !nodeInterface.isControlledOverThisConnection()));
        }
        return nodeInterfaceStream;
    }

    private static Stream<String> getInterfaceAddresses(final Stream<NodeInterface> nodeInterfaceStream,
                                                        final String addressType) {
        final Stream<String> addressStream;
        switch (addressType) {
            case "mac":
                addressStream = nodeInterfaceStream
                        .map(NodeInterface::getMacAddress)
                        .filter(a -> a != null)
                        .map(MacAddress::getDashedString);
                break;
            case "ipv6":
                addressStream = nodeInterfaceStream
                        .map(NodeInterface::getIpv6Address)
                        .filter(a -> a != null)
                        .map(Inet6Address::getHostAddress);
                break;
            case "":
            case "ipv4":
            default:
                addressStream = nodeInterfaceStream
                        .map(NodeInterface::getIpv4Address)
                        .filter(a -> a != null)
                        .map(Inet4Address::getHostAddress);
                break;
        }
        return addressStream;
    }
}
