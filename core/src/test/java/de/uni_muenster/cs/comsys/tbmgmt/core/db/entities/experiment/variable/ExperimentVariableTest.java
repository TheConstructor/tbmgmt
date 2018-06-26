package de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.variable;

import de.uni_muenster.cs.comsys.tbmgmt.core.model.VariableValueType;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Created by matthias on 28.02.16.
 */
public class ExperimentVariableTest {

    @Test
    public void testSetIterator() throws Exception {
        innerTestSetIterator("one");
        innerTestSetIterator("one", "two", "three");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptySetIterator() {
        innerTestSetIterator();
    }

    private static void innerTestSetIterator(final String... values) {
        final ExperimentVariable variable = new ExperimentVariable();
        variable.setType(VariableValueType.SET);
        variable.setValues(Arrays.stream(values).map(s -> {
            final ExperimentVariableValue value = new ExperimentVariableValue();
            value.setExperimentVariable(variable);
            value.setValue(s);
            return value;
        }).collect(Collectors.toList()));
        variable.generateValueSequence();

        Assert.assertThat(variable, IsIterableContainingInOrder.contains(values));
    }

    @Test
    public void testIntegerIterator() throws Exception {
        innerTestRangeIterator(VariableValueType.INTEGER, "1", "0", "1", "1");
        innerTestRangeIterator(VariableValueType.INTEGER, "1", "2", "3", "1", "3");
        innerTestRangeIterator(VariableValueType.INTEGER, "3", "-2", "1", "3", "1");
    }

    @Test(expected = IllegalStateException.class)
    public void testIntegerIteratorWithoutRange() {
        final ExperimentVariable variable = createRangeVariable(VariableValueType.INTEGER, "1", "0", "1");
        variable.setRange(null);
        variable.iterator();
    }

    @Test(expected = IllegalStateException.class)
    public void testIntegerIteratorWithoutStart() {
        final ExperimentVariable variable = createRangeVariable(VariableValueType.INTEGER, "1", "0", "1");
        variable.getRange().setStart(null);
        variable.iterator();
    }

    @Test(expected = IllegalStateException.class)
    public void testIntegerIteratorWithoutEnd() {
        final ExperimentVariable variable = createRangeVariable(VariableValueType.INTEGER, "1", "0", "1");
        variable.getRange().setEnd(null);
        variable.iterator();
    }

    @Test(expected = IllegalStateException.class)
    public void testIntegerIteratorWithoutStepping() {
        final ExperimentVariable variable = createRangeVariable(VariableValueType.INTEGER, "1", "0", "1");
        variable.setStepping(null);
        variable.iterator();
    }

    @Test
    public void testDoubleIterator() throws Exception {
        innerTestRangeIterator(VariableValueType.DOUBLE, "1.0", "0", "1", "1.0");
        innerTestRangeIterator(VariableValueType.DOUBLE, "1", "2", "3.0", "1", "3");
        innerTestRangeIterator(VariableValueType.DOUBLE, "3", "-2.0", "1", "3", "1.0");
    }

    @Test(expected = IllegalStateException.class)
    public void testIntegerDoubleWithoutRange() {
        final ExperimentVariable variable = createRangeVariable(VariableValueType.DOUBLE, "1", "0", "1");
        variable.setRange(null);
        variable.iterator();
    }

    @Test(expected = IllegalStateException.class)
    public void testDoubleIteratorWithoutStart() {
        final ExperimentVariable variable = createRangeVariable(VariableValueType.DOUBLE, "1", "0", "1");
        variable.getRange().setStart(null);
        variable.iterator();
    }

    @Test(expected = IllegalStateException.class)
    public void testDoubleIteratorWithoutEnd() {
        final ExperimentVariable variable = createRangeVariable(VariableValueType.DOUBLE, "1", "0", "1");
        variable.getRange().setEnd(null);
        variable.iterator();
    }

    @Test(expected = IllegalStateException.class)
    public void testDoubleIteratorWithoutStepping() {
        final ExperimentVariable variable = createRangeVariable(VariableValueType.DOUBLE, "1", "0", "1");
        variable.setStepping(null);
        variable.iterator();
    }

    private static void innerTestRangeIterator(final VariableValueType type, final String start, final String stepping,
                                               final String end, final String... values) {
        final ExperimentVariable variable = createRangeVariable(type, start, stepping, end);

        Assert.assertThat(variable, IsIterableContainingInOrder.contains(values));
    }

    private static ExperimentVariable createRangeVariable(final VariableValueType type, final String start,
                                                          final String stepping, final String end) {
        final ExperimentVariable variable = new ExperimentVariable();
        variable.setType(type);
        final ExperimentVariableRange range = new ExperimentVariableRange();
        range.setExperimentVariable(variable);
        range.setStart(start);
        range.setEnd(end);
        variable.setRange(range);
        variable.setStepping(stepping);
        return variable;
    }
}