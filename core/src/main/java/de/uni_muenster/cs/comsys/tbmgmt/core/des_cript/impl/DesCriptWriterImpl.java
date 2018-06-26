package de.uni_muenster.cs.comsys.tbmgmt.core.des_cript.impl;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.Experiment;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.ExperimentFile;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.ExperimentNodeGroup;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.action.ExperimentAction;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.action.ExperimentActionBlock;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.variable.ExperimentVariable;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.variable.ExperimentVariableRange;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.variable.ExperimentVariableValue;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.Node;
import de.uni_muenster.cs.comsys.tbmgmt.core.des_cript.DesCriptUtil;
import de.uni_muenster.cs.comsys.tbmgmt.core.des_cript.DesCriptWriter;
import de.uni_muenster.cs.comsys.tbmgmt.core.schema.ActionBlockType;
import de.uni_muenster.cs.comsys.tbmgmt.core.schema.ActionType;
import de.uni_muenster.cs.comsys.tbmgmt.core.schema.ActionsType;
import de.uni_muenster.cs.comsys.tbmgmt.core.schema.ArrayValues;
import de.uni_muenster.cs.comsys.tbmgmt.core.schema.ExperimentType;
import de.uni_muenster.cs.comsys.tbmgmt.core.schema.FileType;
import de.uni_muenster.cs.comsys.tbmgmt.core.schema.FilesType;
import de.uni_muenster.cs.comsys.tbmgmt.core.schema.GeneralType;
import de.uni_muenster.cs.comsys.tbmgmt.core.schema.GroupType;
import de.uni_muenster.cs.comsys.tbmgmt.core.schema.GroupsType;
import de.uni_muenster.cs.comsys.tbmgmt.core.schema.MembersType;
import de.uni_muenster.cs.comsys.tbmgmt.core.schema.NodeType;
import de.uni_muenster.cs.comsys.tbmgmt.core.schema.ObjectFactory;
import de.uni_muenster.cs.comsys.tbmgmt.core.schema.ResultsType;
import de.uni_muenster.cs.comsys.tbmgmt.core.schema.VariableBlockType;
import de.uni_muenster.cs.comsys.tbmgmt.core.schema.VariableRange;
import de.uni_muenster.cs.comsys.tbmgmt.core.schema.VariableType;
import de.uni_muenster.cs.comsys.tbmgmt.core.utils.formatter.InstantFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Service;

import javax.xml.transform.Result;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

/**
 Created by matthias on 10.03.15.
 */
@Service
public class DesCriptWriterImpl implements DesCriptWriter {
    public static final String DES_SCRIPT_VERSION = "2";
    public static final String DES_CRIPT_GENERATOR = "TBMGMT";

    public static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();

    private static final Logger LOG = Logger.getLogger(DesCriptWriterImpl.class.getName());

    private final Jaxb2Marshaller jaxb2Marshaller;
    private final InstantFormatter instantFormatter;

    @Autowired
    public DesCriptWriterImpl(final Jaxb2Marshaller jaxb2Marshaller, final InstantFormatter instantFormatter) {
        this.jaxb2Marshaller = jaxb2Marshaller;
        this.instantFormatter = instantFormatter;
    }

    @Override
    public void write(final Experiment experiment, final Result result) {
        write(experiment, null, result);
    }

    @Override
    public void write(final Experiment experiment, final ResultsType results, final Result result) {
        final ExperimentType experimentType = write(experiment, results);
        jaxb2Marshaller.marshal(OBJECT_FACTORY.createExperiment(experimentType), result);
    }

    @Override
    public ExperimentType write(final Experiment experiment) {
        return write(experiment, (ResultsType) null);
    }

    @Override
    public ExperimentType write(final Experiment experiment, final ResultsType results) {
        final ExperimentType experimentType = OBJECT_FACTORY.createExperimentType();
        experimentType.setVersion(DES_SCRIPT_VERSION);
        experimentType.setGenerator(DES_CRIPT_GENERATOR);
        experimentType.setGeneral(writeGeneralType(experiment));
        experimentType.setActions(writeActionsType(experiment));
        experimentType.setResults(results);
        return experimentType;
    }

    private GeneralType writeGeneralType(final Experiment experiment) {
        final GeneralType generalType = OBJECT_FACTORY.createGeneralType();
        generalType.setName(experiment.getName());
        generalType.setDescription(experiment.getDescription());
        generalType.setStart_Time(instantFormatter.print(experiment.getStartTime(), Locale.getDefault()));
        if (experiment.getDuration() != null) {
            generalType.setDuration(String.valueOf(experiment.getDuration().getSeconds()));
        }
        generalType.setIterations(experiment.getReplications());
        if (experiment.getPauseBetweenReplications() != null) {
            generalType.setIteration_Pause(experiment.getPauseBetweenReplications().getSeconds());
        }
        if (experiment.getSampleInterval() != null) {
            generalType.setSample_Interval(experiment.getSampleInterval().getSeconds());
        }
        generalType.setRestart_Nodes(experiment.isRestartNodes());
        generalType.setLock_Testbed(experiment.isLockTestbed());
        generalType.setGroups(writeGroupsType(experiment.getNodeGroups()));
        if (experiment.getFiles() != null && !experiment.getFiles().isEmpty()) {
            generalType.setFiles(writeFilesType(experiment.getFiles()));
        }
        return generalType;
    }

    private ActionsType writeActionsType(final Experiment experiment) {
        final ActionsType actionsType = OBJECT_FACTORY.createActionsType();
        {
            final VariableBlockType variableBlockType = OBJECT_FACTORY.createVariableBlockType();
            for (final ExperimentVariable variable : experiment.getVariables()) {
                final VariableType variableType = OBJECT_FACTORY.createVariableType();
                variableType.setName(variable.getName());
                variableType.setType(variable.getType().getDisplayName());
                variableType.setStep(variable.getStepping());

                final ExperimentVariableRange range = variable.getRange();
                if (range != null) {
                    final VariableRange variableRange = OBJECT_FACTORY.createVariableRange();
                    variableRange.setStart(range.getStart());
                    variableRange.setEnd(range.getEnd());
                    variableType.setRange(variableRange);
                }

                final List<ExperimentVariableValue> values = variable.getValues();
                if (values != null && !values.isEmpty()) {
                    final ArrayValues arrayValues = OBJECT_FACTORY.createArrayValues();
                    for (final ExperimentVariableValue variableValue : values) {
                        arrayValues.getValue().add(variableValue.getValue());
                    }
                    variableType.setValues(arrayValues);
                }
                variableBlockType.getVariable().add(variableType);
            }
            actionsType.setVariables(variableBlockType);
        }
        {
            for (final ExperimentActionBlock actionBlock : experiment.getActionBlocks()) {
                final ActionBlockType actionBlockType = OBJECT_FACTORY.createActionBlockType();
                actionBlockType.setId(actionBlock.getSequence());
                actionBlockType.setExecution_Mode(actionBlock.getExecutionMode().getModeId());

                for (final ExperimentAction action : actionBlock.getActions()) {
                    final ActionType actionType = OBJECT_FACTORY.createActionType();
                    actionType.setId(action.getSequence());
                    actionType.setCommand(action.getCommand());
                    actionType.setGroup(action.getTargetedNodeGroup().getName());
                    actionType.setStart_Time(
                            action.getStartOffset() == null ? null : action.getStartOffset().getSeconds());
                    actionType.setDuration(action.getDuration() == null ? null : action.getDuration().getSeconds());
                    actionType.setEvaluation_Script(OBJECT_FACTORY.createActionTypeEvaluation_Script(
                            DesCriptUtil.extractEvaluationScriptParameter(action)));
                    actionType.setEvaluation_Parameter(
                            OBJECT_FACTORY.createActionTypeEvaluation_Parameter(action.getEvaluationParameter()));
                    actionBlockType.getAction().add(actionType);
                }
                actionsType.getAction_Block().add(actionBlockType);
            }
        }
        return actionsType;
    }

    private GroupsType writeGroupsType(final List<ExperimentNodeGroup> nodeGroups) {
        final GroupsType groupsType = OBJECT_FACTORY.createGroupsType();
        if (nodeGroups == null || nodeGroups.isEmpty()) {
            final GroupType groupType = OBJECT_FACTORY.createGroupType();
            groupType.setName("placeholder");
            groupsType.getGroup().add(groupType);
            return groupsType;
        }
        for (final ExperimentNodeGroup nodeGroup : nodeGroups) {
            final GroupType groupType = OBJECT_FACTORY.createGroupType();
            groupType.setName(nodeGroup.getName());
            groupType.setRole(nodeGroup.getRole().getDisplayName());

            final MembersType membersType = OBJECT_FACTORY.createMembersType();
            for (final Node node : nodeGroup.getNodes()) {
                final NodeType nodeType = OBJECT_FACTORY.createNodeType();
                nodeType.setId(node.getName());
                membersType.getNode().add(nodeType);
            }
            groupType.getMembers().add(membersType);
            groupsType.getGroup().add(groupType);
        }
        return groupsType;
    }

    private FilesType writeFilesType(final List<ExperimentFile> files) {
        final FilesType filesType = OBJECT_FACTORY.createFilesType();
        for (final ExperimentFile file : files) {
            final FileType fileType = OBJECT_FACTORY.createFileType();
            fileType.setFilename(file.getFileName());
            fileType.setDescription(file.getDescription());
            fileType.setEval(file.isEval());
            filesType.getFile().add(fileType);
        }
        return filesType;
    }
}
