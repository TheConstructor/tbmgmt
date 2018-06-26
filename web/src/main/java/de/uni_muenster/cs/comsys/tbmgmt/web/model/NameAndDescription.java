package de.uni_muenster.cs.comsys.tbmgmt.web.model;

import java.util.Comparator;

/**
 * Created by matthias on 16.03.16.
 */
public class NameAndDescription implements Comparable<NameAndDescription> {

    public static final Comparator<String> COMPARATOR = Comparator.nullsFirst(Comparator.naturalOrder());

    private final String name;
    private final String description;

    public NameAndDescription(final String name, final String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public int compareTo(final NameAndDescription o) {
        final int i = COMPARATOR.compare(name, o.getName());
        if (i != 0) {
            return i;
        }
        return COMPARATOR.compare(description, o.getDescription());
    }
}
