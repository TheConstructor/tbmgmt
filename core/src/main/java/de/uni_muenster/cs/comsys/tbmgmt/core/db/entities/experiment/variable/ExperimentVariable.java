package de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.variable;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.GeneratedIdEntity;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.Experiment;
import de.uni_muenster.cs.comsys.tbmgmt.core.model.VariableValueType;
import de.uni_muenster.cs.comsys.tbmgmt.core.utils.TriFunction;
import de.uni_muenster.cs.comsys.tbmgmt.core.utils.iterator.BigDecimalRangeIterator;
import de.uni_muenster.cs.comsys.tbmgmt.core.utils.iterator.BigIntegerRangeIterator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Transient;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

/**
 * Created by matthias on 14.03.15.
 */
@Entity
public class ExperimentVariable extends GeneratedIdEntity implements Iterable<String> {
    private Experiment experiment;
    private String name;
    private VariableValueType type;
    private String stepping;
    private ExperimentVariableRange range;
    private List<ExperimentVariableValue> values = new ArrayList<>();

    @ManyToOne(optional = false)
    public Experiment getExperiment() {
        return experiment;
    }

    public void setExperiment(final Experiment experiment) {
        this.experiment = experiment;
    }

    @Basic
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Basic
    @Enumerated(EnumType.STRING)
    public VariableValueType getType() {
        return type;
    }

    public void setType(final VariableValueType type) {
        this.type = type;
    }

    @Basic
    public String getStepping() {
        return stepping;
    }

    public void setStepping(final String stepping) {
        this.stepping = stepping;
    }

    @OneToOne(mappedBy = "experimentVariable", optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
    public ExperimentVariableRange getRange() {
        return range;
    }

    public void setRange(final ExperimentVariableRange range) {
        this.range = range;
    }

    @OneToMany(mappedBy = "experimentVariable", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy(value = "sequence asc")
    @PrimaryKeyJoinColumn
    public List<ExperimentVariableValue> getValues() {
        return values;
    }

    public void setValues(final List<ExperimentVariableValue> values) {
        this.values = values;
    }

    @Transient
    public void generateValueSequence() {
        BigInteger current = BigInteger.ZERO;
        for (final ExperimentVariableValue value : getValues()) {
            value.setSequence(current);
            current = current.add(BigInteger.ONE);
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString())
                                        .append("name", name)
                                        .append("type", type)
                                        .append("stepping", stepping)
                                        .append("range", range)
                                        .append("values", values)
                                        .toString();
    }

    public ExperimentVariable createCopy(final Experiment experiment) {
        final ExperimentVariable experimentVariable = new ExperimentVariable();
        experimentVariable.setExperiment(experiment);
        experimentVariable.setName(getName());
        experimentVariable.setType(getType());
        experimentVariable.setStepping(getStepping());
        final ExperimentVariableRange range = getRange();
        experimentVariable.setRange(range == null ? null : range.createCopy(experimentVariable));
        getValues().stream().map((experimentVariableValue) -> experimentVariableValue.createCopy(experimentVariable))
                .forEachOrdered(experimentVariable.getValues()::add);
        return experimentVariable;
    }

    @Override
    public Iterator<String> iterator() {
        switch (type) {
            case SET:
                return new Iterator<String>() {
                    private final Iterator<ExperimentVariableValue> iterator = values.iterator();

                    @Override
                    public boolean hasNext() {
                        return iterator.hasNext();
                    }

                    @Override
                    public String next() {
                        final ExperimentVariableValue next = iterator.next();
                        return next == null ? null : next.getValue();
                    }
                };
            case INTEGER:
                return createRangeIterator(BigInteger.ZERO, BigInteger::new, BigIntegerRangeIterator::new);
            case DOUBLE:
                return createRangeIterator(BigDecimal.ZERO, BigDecimal::new, BigDecimalRangeIterator::new);
        }
        throw new IllegalStateException("Unknown type: " + type);
    }

    @Transient
    private <N extends Number & Comparable<? super N>, I extends Iterator<String>> I createRangeIterator(final N zero,
                                                                                                         final
                                                                                                         Function<String, N> valueConstructor,
                                                                                                         final
                                                                                                             TriFunction<N, N, N, I> iteratorConstructor) {
        final ExperimentVariableRange range = getRange();
        if (range == null || StringUtils.isBlank(range.getStart()) || StringUtils.isBlank(range.getEnd())) {
            throw new IllegalStateException("Type: " + type + " requires a range");
        }
        if (StringUtils.isBlank(stepping)) {
            throw new IllegalStateException("Type: " + type + " requires a stepping");
        }

        final N startValue = valueConstructor.apply(range.getStart());
        final N steppingValue = valueConstructor.apply(stepping);
        final N endValue = valueConstructor.apply(range.getEnd());

        return iteratorConstructor.apply(startValue, steppingValue, endValue);
    }
}
