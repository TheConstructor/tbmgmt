package de.uni_muenster.cs.comsys.tbmgmt.web.controller;

import de.uni_muenster.cs.comsys.tbmgmt.core.config.FileConfig;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.experiment.ExperimentDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.User;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.Experiment;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.ExperimentFile;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.ExperimentNodeGroup;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.action.ExperimentAction;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.action.ExperimentActionBlock;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.variable.ExperimentVariable;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.variable.ExperimentVariableValue;
import de.uni_muenster.cs.comsys.tbmgmt.core.utils.TbmgmtUtil;
import de.uni_muenster.cs.comsys.tbmgmt.web.support.TbmgmtWebUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.webflow.core.FlowException;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.core.collection.ParameterMap;
import org.springframework.webflow.execution.FlowExecutionOutcome;
import org.springframework.webflow.execution.repository.NoSuchFlowExecutionException;
import org.springframework.webflow.mvc.servlet.AbstractFlowHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Created by matthias on 04.04.15.
 */
public class EditExperimentFlowHandler extends AbstractFlowHandler {

    private static final Logger LOG = LoggerFactory.getLogger(EditExperimentFlowHandler.class);
    private static final Consumer<Object> NO_POST_STEP = x -> {
    };
    private static final Predicate<Object> NO_ADDITIONAL_CHECK = x -> true;

    @Autowired
    private ExperimentDao experimentDao;

    @Autowired
    private FileConfig fileConfig;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Override
    public String getFlowId() {
        return "editExperiment";
    }

    @Override
    public MutableAttributeMap<Object> createExecutionInputMap(final HttpServletRequest request) {
        return super.createExecutionInputMap(request);
    }

    @Override
    @Transactional
    public String handleExecutionOutcome(final FlowExecutionOutcome outcome, final HttpServletRequest request,
                                         final HttpServletResponse response) {

        if ("store".equals(outcome.getId())) {
            // experimentDao.merge(outcome.getOutput().get("experiment", Experiment.class));
        }
        return "/experiments/";
    }

    @Override
    public String handleException(final FlowException e, final HttpServletRequest request,
                                  final HttpServletResponse response) {
        if (e instanceof NoSuchFlowExecutionException) {
            return null;
        } else {
            throw e;
        }
    }

    /**
     * Separate method to be called by flow-expression so we are in the right persistence-context and lazy loading, ...
     * works
     */
    public Experiment provideEntity(final String id, final String copy) {
        final Authentication authentication = TbmgmtWebUtils.getAuthenticatedAuthentication(authenticationManager);
        TbmgmtWebUtils.isFullyAuthenticated(authentication, null);
        if (!StringUtils.isBlank(id)) {
            try {
                final Experiment experiment = experimentDao.find(Long.parseLong(id));
                if (experiment != null) {
                    if (!experiment.getState().allowsEdits() || StringUtils.equalsIgnoreCase("true", copy)) {
                        return setCreatorToCurrentUser(authentication, experiment.createCopy());
                    } else {
                        TbmgmtWebUtils.isAdminOrCreator(authentication, experiment,
                                ExperimentController.USER_CREATED_EXPERIMENT_VOTER);
                        return experiment;
                    }
                } else {
                    LOG.info(String.format("Received unknown experiment-ID \"%s\"", id));
                }
            } catch (final NumberFormatException e) {
                LOG.info("Received invalid experiment-ID", e);
            }
        }
        return setCreatorToCurrentUser(authentication, new Experiment());
    }

    public Experiment setCreatorToCurrentUser(final Experiment experiment) {
        return setCreatorToCurrentUser(TbmgmtWebUtils.getAuthenticatedAuthentication(authenticationManager),
                experiment);
    }

    private Experiment setCreatorToCurrentUser(final Authentication authentication, final Experiment experiment) {
        if (!(authentication.getPrincipal() instanceof User)) {
            throw new InsufficientAuthenticationException("Could not retrieve authenticated User-object");
        }
        final User user = (User) authentication.getPrincipal();
        experiment.setCreatorId(user.getId());
        return experiment;
    }

    public void applyChanges(final Experiment experiment, final ParameterMap parameters) {
        LOG.debug("Received parameters: " + parameters.toString());
        final String applyValue = parameters.get("_eventId_adjust");
        if (applyValue != null) {
            final String[] split = applyValue.split("-");
            switch (split[0]) {
                case "addNG": {
                    addX(experiment, Experiment::getNodeGroups, exp -> {
                        final ExperimentNodeGroup nodeGroup = new ExperimentNodeGroup();
                        nodeGroup.setExperiment(experiment);
                        return nodeGroup;
                    }, NO_POST_STEP);
                    break;
                }
                case "delNG": {
                    delX(experiment, split, Experiment::getNodeGroups, ng -> !experiment.isNodeGroupInUse(ng),
                            NO_POST_STEP);
                    break;
                }
                case "addAB": {
                    addX(experiment, Experiment::getActionBlocks, exp -> {
                        final ExperimentActionBlock actionBlock = new ExperimentActionBlock();
                        actionBlock.setExperiment(experiment);
                        return actionBlock;
                    }, Experiment::generateActionBlockSequence);
                    break;
                }
                case "delAB": {
                    delX(experiment, split, Experiment::getActionBlocks, NO_ADDITIONAL_CHECK,
                            Experiment::generateActionBlockSequence);
                    break;
                }
                case "addA": {
                    addXY(experiment, split, Experiment::getActionBlocks, ExperimentActionBlock::getActions,
                            actionBlock -> {
                                final ExperimentAction action = new ExperimentAction();
                                action.setExperimentActionBlock(actionBlock);
                                return action;
                            }, ExperimentActionBlock::generateActionSequence);
                    break;
                }
                case "delA": {
                    delXY(experiment, split, Experiment::getActionBlocks, ExperimentActionBlock::getActions,
                            NO_ADDITIONAL_CHECK, ExperimentActionBlock::generateActionSequence);
                    break;
                }
                case "addV": {
                    addX(experiment, Experiment::getVariables, exp -> {
                        final ExperimentVariable variable = new ExperimentVariable();
                        variable.setExperiment(experiment);
                        return variable;
                    }, NO_POST_STEP);
                    break;
                }
                case "delV": {
                    delX(experiment, split, Experiment::getVariables, NO_ADDITIONAL_CHECK, NO_POST_STEP);
                    break;
                }
                case "addVV": {
                    addXY(experiment, split, Experiment::getVariables, ExperimentVariable::getValues, variable -> {
                        final ExperimentVariableValue variableValue = new ExperimentVariableValue();
                        variableValue.setExperimentVariable(variable);
                        return variableValue;
                    }, ExperimentVariable::generateValueSequence);
                    break;
                }
                case "delVV": {
                    delXY(experiment, split, Experiment::getVariables, ExperimentVariable::getValues,
                            NO_ADDITIONAL_CHECK, ExperimentVariable::generateValueSequence);
                    break;
                }
                case "delF": {
                    final ExperimentFile experimentFile =
                            delX(experiment, split, Experiment::getFiles, NO_ADDITIONAL_CHECK, NO_POST_STEP);
                    if (experimentFile != null && experimentFile.getId() == null) {
                        final File file = experimentFile.getFile();
                        if (file != null) {
                            if (!file.delete()) {
                                LOG.warn("Could not delete file " + file);
                            }
                        }
                    }
                    break;
                }
            }
        }
    }

    public static <X> X addX(final Experiment experiment, final Function<Experiment, List<? super X>> listGetter,
                             final Function<Experiment, X> constructor, final Consumer<? super Experiment> postStep) {
        final X x = constructor.apply(experiment);
        listGetter.apply(experiment).add(x);
        postStep.accept(experiment);
        return x;
    }

    public static <X> X delX(final Experiment experiment, final String[] split,
                             final Function<Experiment, List<X>> listGetter, final Predicate<? super X> additionalCheck,
                             final Consumer<? super Experiment> postStep) {
        if (split.length < 2) {
            return null;
        }

        final int id;
        try {
            id = Integer.parseUnsignedInt(split[1]);
        } catch (final NumberFormatException e) {
            LOG.info("Could not read number " + split[1], e);
            return null;
        }

        if (id >= 0) {
            final List<X> list = listGetter.apply(experiment);
            if (id < list.size() && additionalCheck.test(list.get(id))) {
                final X x = list.remove(id);
                postStep.accept(experiment);
                return x;
            }
        }
        return null;
    }

    public static <X, Y> Y addXY(final Experiment experiment, final String[] split,
                                 final Function<Experiment, ? extends List<? extends X>> xListGetter,
                                 final Function<X, List<? super Y>> yListGetter, final Function<X, Y> constructor,
                                 final Consumer<? super X> postStep) {
        if (split.length < 2) {
            return null;
        }

        final int id;
        try {
            id = Integer.parseUnsignedInt(split[1]);
        } catch (final NumberFormatException e) {
            LOG.info("Could not read number " + split[1], e);
            return null;
        }

        if (id >= 0) {
            final List<? extends X> xList = xListGetter.apply(experiment);
            if (id < xList.size()) {
                final X x = xList.get(id);
                final Y y = constructor.apply(x);
                yListGetter.apply(x).add(y);
                postStep.accept(x);
                return y;
            }
        }
        return null;
    }

    public static <X, Y> Y delXY(final Experiment experiment, final String[] split,
                                 final Function<Experiment, ? extends List<? extends X>> xListGetter,
                                 final Function<X, List<Y>> yListGetter, final Predicate<? super Y> additionalCheck,
                                 final Consumer<? super X> postStep) {
        if (split.length < 3) {
            return null;
        }

        final int xId;
        try {
            xId = Integer.parseUnsignedInt(split[1]);
        } catch (final NumberFormatException e) {
            LOG.info("Could not read number " + split[1], e);
            return null;
        }
        final int yId;
        try {
            yId = Integer.parseUnsignedInt(split[2]);
        } catch (final NumberFormatException e) {
            LOG.info("Could not read number " + split[2], e);
            return null;
        }

        if (xId >= 0) {
            final List<? extends X> xList = xListGetter.apply(experiment);
            if (xId < xList.size()) {
                final X x = xList.get(xId);
                if (yId >= 0) {
                    final List<Y> yList = yListGetter.apply(x);
                    if (yId < yList.size() && additionalCheck.test(yList.get(yId))) {
                        final Y y = yList.remove(yId);
                        postStep.accept(x);
                        return y;
                    }
                }
            }
        }
        return null;
    }

    public void addFile(final Experiment experiment, final MultipartFile file) {
        final File tempFile;
        try {
            tempFile = TbmgmtUtil.createTempFile(fileConfig.getUploadTempPath()).toFile();
            tempFile.deleteOnExit();
            file.transferTo(tempFile);
        } catch (IllegalStateException | IOException e) {
            throw new IllegalStateException("Could not move uploaded file to temporary directory");
        }
        final ExperimentFile experimentFile = new ExperimentFile();
        experimentFile.setFileName(Paths.get(file.getOriginalFilename()).getFileName().toString());
        experimentFile.setFile(tempFile);
        experimentFile.setExperiment(experiment);
        experiment.getFiles().add(experimentFile);
    }
}
