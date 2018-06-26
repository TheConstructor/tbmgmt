package de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.GeneratedIdEntity;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Transient;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Models individual Nodes which can participate in experiments.
 */
@Entity
public class Node extends GeneratedIdEntity {
    private String name;
    private String description;
    private String building;
    private String level;
    private String room;
    private BigDecimal locationX;
    private BigDecimal locationY;
    private BigDecimal locationZ;
    private NodeType type;
    private Testbed testbed;
    private String imageFolder;
    private String kernelName;
    private String switchName;
    private List<NodeInterface> interfaces = new ArrayList<>();

    @Basic
    @Column(unique = true)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Basic
    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    /**
     * Displayed as sub-text when selecting nodes
     */
    @Transient
    public String getAdditionalInfo() {
        final StringBuilder sb = new StringBuilder();
        final String building = getBuilding();
        final String description = getDescription();
        if (StringUtils.isNotBlank(building)) {
            sb.append(building);
            final String room = getRoom();
            if (StringUtils.isNotBlank(room)) {
                sb.append("/").append(room);
            }
            if (StringUtils.isNotBlank(description)) {
                sb.append(": ");
            }
        }
        if (StringUtils.isNotBlank(description)) {
            sb.append(description);
        }
        return sb.toString();
    }

    @OneToMany(mappedBy = "node", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("name asc")
    public List<NodeInterface> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(final List<NodeInterface> interfaces) {
        this.interfaces = interfaces;
    }

    @Basic
    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    @Basic
    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    @Basic
    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    @Basic
    public BigDecimal getLocationX() {
        return locationX;
    }

    public void setLocationX(BigDecimal locationX) {
        this.locationX = locationX;
    }

    @Basic
    public BigDecimal getLocationY() {
        return locationY;
    }

    public void setLocationY(BigDecimal locationY) {
        this.locationY = locationY;
    }

    @Basic
    public BigDecimal getLocationZ() {
        return locationZ;
    }

    public void setLocationZ(BigDecimal locationZ) {
        this.locationZ = locationZ;
    }

    @ManyToOne(optional = false)
    public NodeType getType() {
        return type;
    }

    public void setType(NodeType type) {
        this.type = type;
    }

    @ManyToOne(optional = false)
    public Testbed getTestbed() {
        return testbed;
    }

    public void setTestbed(Testbed testbed) {
        this.testbed = testbed;
    }

    @Basic
    public String getImageFolder() {
        return imageFolder;
    }

    public void setImageFolder(String imageFolder) {
        this.imageFolder = imageFolder;
    }

    @Basic
    public String getKernelName() {
        return kernelName;
    }

    public void setKernelName(String kernelName) {
        this.kernelName = kernelName;
    }

    @Basic
    public String getSwitchName() {
        return switchName;
    }

    public void setSwitchName(String switchName) {
        this.switchName = switchName;
    }

    @Transient
    public String getInterfaceShortlist() {
        return getInterfaces().stream().map(i -> {
            StringBuilder out = new StringBuilder();
            out.append(i.getName()).append("=");
            if (i.getMacAddress() != null) {
                out.append(i.getMacAddress());
            } else if (i.getIpv6Address() != null) {
                out.append(i.getIpv6Address().getHostAddress());
            } else if (i.getIpv4Address() != null) {
                out.append(i.getIpv4Address().getHostAddress());
            } else {
                out.append("{}");
            }
            return out.toString();
        }).collect(Collectors.joining(", "));
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("name", name)
                .append("building", building)
                .append("level", level)
                .append("room", room)
                .append("locationX", locationX)
                .append("locationY", locationY)
                .append("locationZ", locationZ)
                .append("type", type)
                .append("testbed", testbed)
                .append("imageFolder", imageFolder)
                .append("kernelName", kernelName)
                .append("switchName", switchName)
                .append("interfaces", interfaces)
                .append("interfaceShortlist", getInterfaceShortlist())
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Node)) {
            return false;
        }

        Node node = (Node) o;

        return new EqualsBuilder().append(getName(), node.getName()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getName()).toHashCode();
    }
}
