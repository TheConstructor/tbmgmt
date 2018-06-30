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
import de.uni_muenster.cs.comsys.tbmgmt.core.des_cript.DesCriptReader;
import de.uni_muenster.cs.comsys.tbmgmt.core.des_cript.DesCriptUtil;
import de.uni_muenster.cs.comsys.tbmgmt.core.des_cript.EvaluationScriptResolver;
import de.uni_muenster.cs.comsys.tbmgmt.core.des_cript.NodeNameResolver;
import de.uni_muenster.cs.comsys.tbmgmt.core.model.ExecutionMode;
import de.uni_muenster.cs.comsys.tbmgmt.core.model.NodeRole;
import de.uni_muenster.cs.comsys.tbmgmt.core.model.VariableValueType;
import de.uni_muenster.cs.comsys.tbmgmt.core.schema.*;
import de.uni_muenster.cs.comsys.tbmgmt.core.utils.formatter.InstantFormatter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Service;
import org.springframework.util.InvalidMimeTypeException;
import org.springframework.util.MimeType;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.bind.JAXBElement;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 Created by matthias on 10.03.15.
 */
@Service
public class DesCriptReaderImpl implements DesCriptReader {

    private static final Logger LOG = Logger.getLogger(DesCriptReaderImpl.class.getName());

    private final Jaxb2Marshaller  jaxb2Marshaller;
    private final InstantFormatter instantFormatter;
    private final NodeNameResolver nodeNameResolver;
    private final EvaluationScriptResolver evaluationScriptResolver;

    @Autowired
    public DesCriptReaderImpl(final Jaxb2Marshaller jaxb2Marshaller, final InstantFormatter instantFormatter,
                              final NodeNameResolver nodeNameResolver,
                              final EvaluationScriptResolver evaluationScriptResolver) {
        this.jaxb2Marshaller = jaxb2Marshaller;
        this.instantFormatter = instantFormatter;
        this.nodeNameResolver = nodeNameResolver;
        this.evaluationScriptResolver = evaluationScriptResolver;
    }

    @Override
    public Experiment read(final MultipartFile desCriptFile) throws IOException {
        if (desCriptFile == null || desCriptFile.isEmpty()) {
            return new Experiment();
        }

        // In the unlikely event there is a content-type header containing charset information
        final Charset charset = charsetFromContentType(desCriptFile.getContentType());
        if (charset != null) {
            try (InputStreamReader reader = new InputStreamReader(desCriptFile.getInputStream(), charset)) {
                return read(new StreamSource(reader));
            }
        }

        // normal case
        try (InputStream inputStream = desCriptFile.getInputStream()) {
            return this.read(new StreamSource(inputStream));
        }
    }

    private Charset charsetFromContentType(final String contentType) {
        if (contentType == null) {
            return null;
        }
        try {
            return MimeType.valueOf(contentType).getCharset();
        } catch (final InvalidMimeTypeException e) {
            LOG.log(Level.FINE, "Could not parse " + contentType + " to MimeType", e);
            return null;
        }
    }

    private void read(final Experiment experiment, final GeneralType generalType) {
        experiment.setName(generalType.getName());
        experiment.setDescription(generalType.getDescription());
        try {
            experiment.setStartTime(instantFormatter.parse(generalType.getStart_Time(), Locale.getDefault()));
        } catch (final ParseException e) {
            throw new IllegalArgumentException("Could not parse start_time", e);
        }
        if (StringUtils.isNotBlank(generalType.getDuration())) {
            experiment.setDuration(Duration.ofSeconds(Long.valueOf(generalType.getDuration())));
        }
        experiment.setReplications(generalType.getIterations());
        experiment.setPauseBetweenReplications(Duration.ofSeconds(generalType.getIteration_Pause()));
        experiment.setSampleInterval(Duration.ofSeconds(generalType.getSample_Interval()));
        experiment.setRestartNodes(generalType.isRestart_Nodes());
        experiment.setLockTestbed(generalType.isLock_Testbed());
        read(experiment, generalType.getGroups());
        if (generalType.getFiles() != null) {
            read(experiment, generalType.getFiles());
        }
    }

    private void read(final Experiment experiment, final ActionsType actionsType) {
        {
            final ArrayList<ExperimentVariable> variables = new ArrayList<>();
            final VariableBlockType variableBlockType = actionsType.getVariables();
            if (variableBlockType != null) {
                for (final VariableType variableType : variableBlockType.getVariable()) {
                    final ExperimentVariable experimentVariable = new ExperimentVariable();
                    experimentVariable.setExperiment(experiment);
                    experimentVariable.setName(variableType.getName());
                    experimentVariable.setType(VariableValueType.fromName(variableType.getType()));
                    experimentVariable.setStepping(variableType.getStep());

                    final VariableRange range = variableType.getRange();
                    if (range != null) {
                        final ExperimentVariableRange variableRange = new ExperimentVariableRange();
                        variableRange.setExperimentVariable(experimentVariable);
                        variableRange.setStart(range.getStart());
                        variableRange.setEnd(range.getEnd());
                        experimentVariable.setRange(variableRange);
                    }

                    final ArrayValues values = variableType.getValues();
                    if (values != null) {
                        final ArrayList<ExperimentVariableValue> variableValues = new ArrayList<>();
                        for (final String s : values.getValue()) {
                            final ExperimentVariableValue variableValue = new ExperimentVariableValue();
                            variableValue.setExperimentVariable(experimentVariable);
                            variableValue.setValue(s);
                            variableValues.add(variableValue);
                        }
                        experimentVariable.setValues(variableValues);
                    }
                    variables.add(experimentVariable);
                }
            }
            experiment.setVariables(variables);
        }
        {
            final ArrayList<ExperimentActionBlock> actionBlocks = new ArrayList<>();
            for (final ActionBlockType actionBlockType : actionsType.getAction_Block()) {
                final ExperimentActionBlock actionBlock = new ExperimentActionBlock();
                actionBlock.setExperiment(experiment);
                actionBlock.setSequence(actionBlockType.getId());
                actionBlock.setExecutionMode(ExecutionMode.getByModeId(actionBlockType.getExecution_Mode()));
                final ArrayList<ExperimentAction> actions = new ArrayList<>();
                for (final ActionType actionType : actionBlockType.getAction()) {
                    final ExperimentAction action = new ExperimentAction();
                    action.setExperimentActionBlock(actionBlock);
                    action.setSequence(actionType.getId());
                    action.setCommand(actionType.getCommand());
                    action.setTargetedNodeGroup(experiment.getNodeGroupByName(actionType.getGroup()));
                    if (actionType.getStart_Time() != null) {
                        action.setStartOffset(Duration.ofSeconds(actionType.getStart_Time()));
                    }
                    if (actionType.getDuration() != null) {
                        action.setDuration(Duration.ofSeconds(actionType.getDuration()));
                    }
                    final JAXBElement<String> evaluation_script = actionType.getEvaluation_Script();
                    DesCriptUtil.extractEvaluationScript(evaluationScriptResolver, Collections.emptyList(), action,
                            evaluation_script != null ? evaluation_script.getValue() : null);
                    final JAXBElement<String> evaluation_parameter = actionType.getEvaluation_Parameter();
                    action.setEvaluationParameter(
                            evaluation_parameter != null ? evaluation_parameter.getValue() : null);
                    actions.add(action);
                }
                actionBlock.setActions(actions);
                actionBlocks.add(actionBlock);
            }
            experiment.setActionBlocks(actionBlocks);
        }
    }

    private void read(final Experiment experiment, final GroupsType groupsType) {
        final ArrayList<ExperimentNodeGroup> experimentNodeGroups = new ArrayList<>();
        if (groupsType.getGroup() != null) {
            for (final GroupType groupType : groupsType.getGroup()) {
                final ExperimentNodeGroup experimentNodeGroup = new ExperimentNodeGroup();
                experimentNodeGroup.setExperiment(experiment);
                experimentNodeGroup.setName(groupType.getName());
                experimentNodeGroup.setRole(NodeRole.fromName(groupType.getRole()));
                final ArrayList<Node> nodes = new ArrayList<>();
                for (final MembersType membersType : groupType.getMembers()) {
                    for (final NodeType nodeType : membersType.getNode()) {
                        try {
                            nodes.add(nodeNameResolver.getNodeByName(nodeType.getId()));
                        } catch (NoSuchElementException | EmptyResultDataAccessException e) {
                            LOG.log(Level.WARNING,
                                    "Could not resolve node with name " + nodeType.getId() + " while reading DES-Cript",
                                    e);
                        }
                    }
                }
                experimentNodeGroup.setNodes(nodes);
                experimentNodeGroups.add(experimentNodeGroup);
            }
        }
        experiment.setNodeGroups(experimentNodeGroups);
    }

    private void read(final Experiment experiment, final FilesType filesType) {
        final ArrayList<ExperimentFile> experimentFiles = new ArrayList<>();
        for (final FileType fileType : filesType.getFile()) {
            final ExperimentFile experimentFile = new ExperimentFile();
            experimentFile.setExperiment(experiment);
            experimentFile.setFileName(fileType.getFilename());
            experimentFile.setDescription(fileType.getDescription());
            experimentFile.setEval(fileType.isEval());
        }
        experiment.setFiles(experimentFiles);
    }

    @Override
    public Experiment read(final Source source) {
        final Object desCript = jaxb2Marshaller.unmarshal(source);
        if (desCript instanceof ExperimentType) {
            return read((ExperimentType) desCript);
        } else {
            throw new IllegalArgumentException("Unmarshalling returned unknown object " + desCript);
        }
    }

    @Override
    public Experiment read(final ExperimentType experimentType) {
        final Experiment experiment = new Experiment();
        read(experiment, experimentType.getGeneral());
        read(experiment, experimentType.getActions());
        return experiment;
    }
}
