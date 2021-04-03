package de.uni_muenster.cs.comsys.tbmgmt.web.model.node;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.node.NodeTypeDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.node.TestbedDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.Node;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.NodeType;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.Testbed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 Created by matthias on 21.10.15.
 */
@Configurable
public class FilteredNode implements Serializable {
    @Autowired
    private transient NodeTypeDao nodeTypeDao;

    @Autowired
    private transient TestbedDao testbedDao;

    private final Node node;

    public FilteredNode(final Node node) {
        this.node = node;
    }

    public Long getId() {
        return node.getId();
    }

    @NotNull
    @NotBlank
    public String getName() {
        return node.getName();
    }

    public void setName(final String name) {
        node.setName(name);
    }

    public String getDescription() {
        return node.getDescription();
    }

    public void setDescription(final String description) {
        node.setDescription(description);
    }

    public String getBuilding() {
        return node.getBuilding();
    }

    public void setBuilding(String building) {
        node.setBuilding(building);
    }

    public String getLevel() {
        return node.getLevel();
    }

    public void setLevel(String level) {
        node.setLevel(level);
    }

    public String getRoom() {
        return node.getRoom();
    }

    public void setRoom(String room) {
        node.setRoom(room);
    }

    public BigDecimal getLocationX() {
        return node.getLocationX();
    }

    public void setLocationX(BigDecimal locationX) {
        node.setLocationX(locationX);
    }

    public BigDecimal getLocationY() {
        return node.getLocationY();
    }

    public void setLocationY(BigDecimal locationY) {
        node.setLocationY(locationY);
    }

    public BigDecimal getLocationZ() {
        return node.getLocationZ();
    }

    public void setLocationZ(BigDecimal locationZ) {
        node.setLocationZ(locationZ);
    }

    @NotNull
    public String getType() {
        NodeType nodeType = node.getType();
        if (nodeType != null) {
            return nodeType.getName();
        } else {
            return null;
        }
    }

    public void setType(String type) {
        if (type == null) {
            node.setType(null);
        } else {
            node.setType(nodeTypeDao.getByName(type));
        }
    }

    @NotNull
    public String getTestbed() {
        Testbed testbed = node.getTestbed();
        if (testbed != null) {
            return testbed.getName();
        } else {
            return null;
        }
    }

    public void setTestbed(String testbed) {
        if (testbed == null) {
            node.setTestbed(null);
        } else {
            node.setTestbed(testbedDao.getByName(testbed));
        }
    }

    public String getImageFolder() {
        return node.getImageFolder();
    }

    public void setImageFolder(String imageFolder) {
        node.setImageFolder(imageFolder);
    }

    public String getKernelName() {
        return node.getKernelName();
    }

    public void setKernelName(String kernelName) {
        node.setKernelName(kernelName);
    }

    public String getSwitchName() {
        return node.getSwitchName();
    }

    public void setSwitchName(String switchName) {
        node.setSwitchName(switchName);
    }

    @NotNull
    @NotEmpty
    @Valid
    public List<FilteredNodeInterface> getInterfaces() {
        if (node.getInterfaces() == null) {
            node.setInterfaces(new ArrayList<>());
        }
        return Collections.unmodifiableList(node.getInterfaces().stream()
                .map((nodeInterface -> new FilteredNodeInterface(node, nodeInterface
                )))
                .collect(Collectors.toList()));
    }
}
