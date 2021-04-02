package de.uni_muenster.cs.comsys.tbmgmt.web.controller;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.EvaluationScriptDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.experiment.ExperimentDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.experiment.ExperimentFileDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.experiment.IdleExperimentDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.EvaluationScript;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.Tag_;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.User;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.Experiment;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.ExperimentFile;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.Experiment_;
import de.uni_muenster.cs.comsys.tbmgmt.core.des_cript.DesCriptDatabaseHelper;
import de.uni_muenster.cs.comsys.tbmgmt.core.des_cript.DesCriptWriter;
import de.uni_muenster.cs.comsys.tbmgmt.core.model.ExperimentState;
import de.uni_muenster.cs.comsys.tbmgmt.core.utils.TbmgmtUtil;
import de.uni_muenster.cs.comsys.tbmgmt.web.model.Pagination;
import de.uni_muenster.cs.comsys.tbmgmt.web.support.CallbackBasedAccessDecisionVoter;
import de.uni_muenster.cs.comsys.tbmgmt.web.support.InstantViewRenderer;
import de.uni_muenster.cs.comsys.tbmgmt.web.support.TbmgmtWebUtils;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;

/**
 * Created by matthias on 30.10.15.
 */
@Controller
@RequestMapping("/experiments")
public class ExperimentController {

    public static final CallbackBasedAccessDecisionVoter<Experiment> USER_CREATED_EXPERIMENT_VOTER =
            CallbackBasedAccessDecisionVoter.createUserBased(Experiment.class,
                    (user, exp) -> exp.getCreatorId() != null && Objects.equals(user.getId(), exp.getCreatorId()));
    @Autowired
    private ExperimentDao experimentDao;

    @Autowired
    private IdleExperimentDao idleExperimentDao;

    @Autowired
    private ExperimentFileDao experimentFileDao;

    @Autowired
    private EvaluationScriptDao evaluationScriptDao;

    @Autowired
    private InstantViewRenderer instantViewRenderer;

    @Autowired
    private TransactionTemplate readOnlyTransactionTemplate;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private DesCriptWriter desCriptWriter;

    @Autowired
    private DesCriptDatabaseHelper desCriptDatabaseHelper;

    @RequestMapping(value = {"", "/"})
    public void experiments(
            @RequestParam(value = "page", required = false, defaultValue = "0") @Min(0) final int page,
            @RequestParam(value = "perPage", required = false, defaultValue = "20") @Min(1) final int perPage,
            final SearchParameter searchParameter, final HttpServletRequest request, final HttpServletResponse response,
            final Model model) {
        final LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        final SearchParameter effectiveSearchParameter =
                searchParameter == null ? new SearchParameter() : searchParameter;

        // keep parameter for pagination
        setParam(params, "tags", effectiveSearchParameter.getTags());
        setParam(params, "nameQ", effectiveSearchParameter.getNameQ());
        setParam(params, "states", effectiveSearchParameter.getStates());
        setParam(params, "onlyMine", effectiveSearchParameter.isOnlyMine());

        readOnlyTransactionTemplate.execute(transactionStatus -> {
            try {
                model.addAttribute("searchParameter", effectiveSearchParameter);
                final Experiment idleExperiment = idleExperimentDao.getIdleExperiment();
                model.addAttribute("idleExperiment", idleExperiment == null ? null : idleExperiment.getId());
                model.addAttribute("pagination", new Pagination<>(experimentDao, (cb, experimentPath) -> {
                    final List<Predicate> predicates = new ArrayList<>();

                    final List<String> tags = effectiveSearchParameter.getTags();
                    if (tags != null) {
                        for (final String tag : tags) {
                            predicates.add(cb.equal(experimentPath.join(Experiment_.tags).get(Tag_.name), tag));
                        }
                    }

                    final String nameQ = effectiveSearchParameter.getNameQ();
                    if (StringUtils.isNotBlank(nameQ)) {
                        predicates.add(cb.like(cb.lower(experimentPath.get(Experiment_.name)),
                                cb.lower(cb.literal('%' + TbmgmtUtil.escapeLikeString(nameQ) + '%')),
                                TbmgmtUtil.LIKE_ESCAPE_CHAR));
                    }

                    final String states = effectiveSearchParameter.getStates();
                    if (StringUtils.isNotBlank(states)) {
                        switch (states) {
                            case "running": {
                                predicates.add(isRunning(experimentPath));
                                break;
                            }
                            case "scheduled": {
                                predicates.add(isScheduled(experimentPath));
                                break;
                            }
                            case "ran": {
                                predicates.add(inEndState(experimentPath));
                                break;
                            }
                            case "exceptRanOrMine": {
                                final Authentication authentication =
                                        TbmgmtWebUtils.getAuthentication(authenticationManager);
                                if (authentication != null && authentication.getPrincipal() instanceof User) {
                                    final Predicate createdByUser =
                                            createdByUser(cb, experimentPath, ((User) authentication.getPrincipal()));
                                    final Predicate notInEndState = inEndState(experimentPath).not();
                                    predicates.add(cb.or(createdByUser, notInEndState));
                                    break;
                                }
                            }
                            //noinspection fallthrough
                            case "exceptRan": {
                                predicates.add(inEndState(experimentPath).not());
                                break;
                            }
                        }
                    }

                    if (effectiveSearchParameter.isOnlyMine()) {
                        final Authentication authentication = TbmgmtWebUtils.getAuthentication(authenticationManager);
                        if (authentication != null && authentication.getPrincipal() instanceof User) {
                            final Predicate createdByUser =
                                    createdByUser(cb, experimentPath, ((User) authentication.getPrincipal()));
                            predicates.add(createdByUser);
                        }
                    }

                    return !predicates.isEmpty() ? cb.and(predicates.toArray(new Predicate[predicates.size()])) : null;
                }, (cb, r) -> Collections.singletonList(cb.desc(r.get(Experiment_.startTime))), perPage, page,
                        Pagination.createQueryString(params)));
                instantViewRenderer.render(new ModelAndView("experiments", model.asMap()), request, response);
            } catch (final Exception e) {
                throw new IllegalStateException(e);
            }
            return null;
        });
    }

    public static Predicate isScheduled(final Path<Experiment> experimentPath) {
        return experimentPath.get(Experiment_.state).in(ExperimentState.SCHEDULED);
    }

    public static Predicate isRunning(final Path<Experiment> experimentPath) {
        return experimentPath.get(Experiment_.state).in(ExperimentState.getRunningStates());
    }

    private static void setParam(final LinkedMultiValueMap<String, String> params, final String name,
                                 final String value) {
        if (StringUtils.isNotBlank(value)) {
            params.add(name, value);
        } else {
            params.add(name, "");
        }
    }

    private static void setParam(final LinkedMultiValueMap<String, String> params, final String name,
                                 final List<String> value) {
        if (value != null && !value.isEmpty()) {
            params.put(name, value);
        } else {
            params.add("_" + name, "on");
        }
    }

    private static void setParam(final LinkedMultiValueMap<String, String> params, final String name,
                                 final boolean value) {
        if (value) {
            params.add(name, "true");
        } else {
            params.add("_" + name, "on");
        }
    }

    private static Predicate createdByUser(final CriteriaBuilder cb, final Root<Experiment> experimentPath, User user) {
        return cb.equal(experimentPath.get(Experiment_.creatorId), user.getId());
    }

    protected static Predicate inEndState(final Root<Experiment> experimentPath) {
        return experimentPath.get(Experiment_.state).in(ExperimentState.getEndStates());
    }

    @RequestMapping(value = "/{id}/delete", method = RequestMethod.POST)
    public RedirectView deleteExperiment(@PathVariable("id") final long id,
                                         final RedirectAttributes redirectAttributes) {
        final Authentication authenticated = TbmgmtWebUtils.getAuthenticatedAuthentication(authenticationManager);
        experimentDao.getTransactionTemplate().execute(transactionStatus -> {
            final Experiment experiment = experimentDao.find(id);
            if (experiment != null) {
                if (!experiment.getState().isRunning()) {
                    TbmgmtWebUtils.isFullyAuthenticated(authenticated, experiment);
                    TbmgmtWebUtils.isAdminOrCreator(authenticated, experiment, USER_CREATED_EXPERIMENT_VOTER);
                    experimentDao.remove(experiment);
                    redirectAttributes.addFlashAttribute("flash_success", "Deleted experiment with id " + id);
                } else {
                    redirectAttributes.addFlashAttribute("flash_warning",
                            "Can not delete a running experiment. Cancel it first");
                }
            } else {
                redirectAttributes.addFlashAttribute("flash_error", "Can not find experiment with id " + id);
            }
            return null;
        });
        return new RedirectView("/experiments/", true);
    }

    @RequestMapping(value = "/{id}/cancel", method = RequestMethod.POST)
    public RedirectView cancelExperiment(@PathVariable("id") final long id,
                                         final RedirectAttributes redirectAttributes) {
        final Authentication authenticated = TbmgmtWebUtils.getAuthenticatedAuthentication(authenticationManager);
        experimentDao.getTransactionTemplate().execute(transactionStatus -> {
            final Experiment experiment = experimentDao.find(id);
            if (experiment != null) {
                if (experiment.getState().isRunning()) {
                    TbmgmtWebUtils.isFullyAuthenticated(authenticated, experiment);
                    TbmgmtWebUtils.isAdminOrCreator(authenticated, experiment, USER_CREATED_EXPERIMENT_VOTER);
                    experimentDao.immediatelySetStateIfNotEnded(experiment, ExperimentState.CANCELATION_REQUESTED);
                } else {
                    redirectAttributes.addFlashAttribute("flash_warning", "Can not cancel a not running experiment.");
                }
            } else {
                redirectAttributes.addFlashAttribute("flash_error", "Can not find experiment with id " + id);
            }
            return null;
        });
        return new RedirectView("/experiments/", true);
    }

    @RequestMapping(value = "/{id}/export", method = RequestMethod.GET, produces = MediaType.TEXT_XML_VALUE)
    public String exportExperiment(@PathVariable("id") final long id, final RedirectAttributes redirectAttributes,
                                   final HttpServletResponse response) {
        final Authentication authenticated = TbmgmtWebUtils.getAuthenticatedAuthentication(authenticationManager);
        return experimentDao.getTransactionTemplate().execute(transactionStatus -> {
            transactionStatus.setRollbackOnly();
            final Experiment experiment = experimentDao.find(id);
            if (experiment != null) {
                TbmgmtWebUtils.isFullyAuthenticated(authenticated, experiment);
                return DesCriptRenderAction.marshall(desCriptWriter, experiment,
                        desCriptDatabaseHelper.loadResultsFromDatabase(experiment), response, () -> null, e -> {
                            redirectAttributes.addFlashAttribute("flash_error",
                                    "Could not render DEScript: " + e.getMessage());
                            return "redirect:/experiments/";
                        });
            } else {
                redirectAttributes.addFlashAttribute("flash_error", "Can not find experiment with id " + id);
                return "redirect:/experiments/";
            }
        });
    }

    @RequestMapping(value = "/{id}/setIdleExperiment", method = RequestMethod.POST)
    @Secured(TbmgmtWebUtils.ROLE_ADMIN)
    public String setIdleExperiment(@PathVariable("id") final long id, final RedirectAttributes redirectAttributes) {
        idleExperimentDao.getTransactionTemplate().execute(transactionStatus -> {
            final Experiment experiment = experimentDao.find(id);
            if (experiment != null) {
                idleExperimentDao.setIdleExperiment(experiment);
                redirectAttributes.addFlashAttribute("flash_success",
                        "Idle experiment is now experiment with id " + id);
            } else {
                redirectAttributes.addFlashAttribute("flash_error",
                        "Could not set idle experiment to experiment with id " + id);
            }
            return null;
        });
        return "redirect:/experiments/";
    }

    @RequestMapping(value = "/{id}/unsetIdleExperiment", method = RequestMethod.POST)
    @Secured(TbmgmtWebUtils.ROLE_ADMIN)
    public String unsetIdleExperiment(@PathVariable("id") final long id, final RedirectAttributes redirectAttributes) {
        idleExperimentDao.getTransactionTemplate().execute(transactionStatus -> {
            final Experiment experiment = idleExperimentDao.getIdleExperiment();
            if (experiment == null) {
                redirectAttributes.addFlashAttribute("flash_warning", "Currently no idle experiment is set");
            } else if (experiment.getId() == id) {
                idleExperimentDao.setIdleExperiment(null);
                redirectAttributes.addFlashAttribute("flash_success",
                        "No idle experiment set. Was experiment with id " + id);
            } else {
                redirectAttributes.addFlashAttribute("flash_warning",
                        "Actual idle experiment is experiment with id " + experiment.getId());
            }
            return null;
        });
        return "redirect:/experiments/";
    }

    @RequestMapping(value = "/{id}/file/{fileId}", method = RequestMethod.GET)
    @Secured(TbmgmtWebUtils.ROLE_USER)
    public String downloadFile(@PathVariable("id") final long id, @PathVariable("fileId") final long fileId,
                               final HttpServletResponse response, final RedirectAttributes redirectAttributes) {
        final ExperimentFile experimentFile = experimentFileDao.find(fileId);
        if (experimentFile != null && Objects.equals(experimentFile.getExperiment().getId(), id)) {
            if (sendFile(response, experimentFile.getFile(), experimentFile.getFileName(), redirectAttributes)) {
                return null;
            }
        } else {
            redirectAttributes.addFlashAttribute("flash_error", "Could not find this file");
        }
        return "redirect:/experiments/";
    }

    @RequestMapping(value = "/evaluationScript/{id}", method = RequestMethod.GET)
    @Secured(TbmgmtWebUtils.ROLE_USER)
    public String downloadEvaluationScript(@PathVariable("id") final long id, final HttpServletResponse response,
                                           final RedirectAttributes redirectAttributes) {
        final EvaluationScript evaluationScript = evaluationScriptDao.find(id);
        if (evaluationScript != null) {
            if (sendFile(response, evaluationScript.getFile(), evaluationScript.getFileName(), redirectAttributes)) {
                return null;
            }
        } else {
            redirectAttributes.addFlashAttribute("flash_error", "Could not find this file");
        }
        return "redirect:/experiments/";
    }

    protected static boolean sendFile(final HttpServletResponse response, final File file, final String fileName,
                                      final RedirectAttributes redirectAttributes) {
        if (file.canRead()) {
            final HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            httpHeaders.set(HttpHeaders.CONTENT_DISPOSITION, TbmgmtUtil.asContentDisposition(fileName));
            httpHeaders.setContentLength(file.length());
            TbmgmtWebUtils.setHeaders(response, httpHeaders);

            try (final FileInputStream inputStream = new FileInputStream(file);
                 final ServletOutputStream outputStream = response.getOutputStream()) {
                IOUtils.copy(inputStream, outputStream);
                outputStream.flush();
                return true;
            } catch (final IOException e) {
                redirectAttributes.addFlashAttribute("flash_error", "I/O-Error: " + e);
            }
        } else {
            redirectAttributes.addFlashAttribute("flash_error", "File found in database, but not on disk");
        }
        return false;
    }

    public static class SearchParameter implements Serializable {
        private List<String> tags;
        private String nameQ;
        private String states = "exceptRanOrMine";
        private boolean onlyMine = false;

        public List<String> getTags() {
            return tags;
        }

        public void setTags(final List<String> tags) {
            this.tags = tags;
        }

        public String getNameQ() {
            return nameQ;
        }

        public void setNameQ(final String nameQ) {
            this.nameQ = nameQ;
        }

        public String getStates() {
            return states;
        }

        public void setStates(final String states) {
            this.states = states;
        }

        public boolean isOnlyMine() {
            return onlyMine;
        }

        public void setOnlyMine(final boolean onlyMine) {
            this.onlyMine = onlyMine;
        }
    }
}
