package de.uni_muenster.cs.comsys.tbmgmt.web.controller;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.experiment.ExperimentDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.Experiment;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.Experiment_;
import de.uni_muenster.cs.comsys.tbmgmt.web.support.InstantViewRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.BiFunction;

@Controller
public class GreetingController {

    public static final Logger LOG = LoggerFactory.getLogger(GreetingController.class);

    @Autowired
    private ExperimentDao experimentDao;

    @Autowired
    private TransactionTemplate readOnlyTransactionTemplate;

    @Autowired
    private InstantViewRenderer instantViewRenderer;

    @RequestMapping("/greeting")
    public void greeting(final HttpServletRequest request, final HttpServletResponse response, final Model model) {
        readOnlyTransactionTemplate.execute(transactionStatus -> {
            try {
                final CriteriaBuilder cb = experimentDao.getCriteriaBuilder();

                final List<Experiment> runningExperiments =
                        getExperiments(cb, (experiment, startTime) -> ExperimentController.isRunning(experiment));

                final Instant scheduleCutOff = Instant.now().plus(6, ChronoUnit.HOURS);
                final List<Experiment> scheduledExperiments = getExperiments(cb,
                        (experiment, startTime) -> cb.and(ExperimentController.isScheduled(experiment),
                                cb.lessThan(startTime, scheduleCutOff)));

                model.addAttribute("runningExperiments", runningExperiments);
                model.addAttribute("scheduleCutOff", scheduleCutOff);
                model.addAttribute("scheduledExperiments", scheduledExperiments);

                instantViewRenderer.render(new ModelAndView("greeting", model.asMap()), request, response);
            } catch (final Exception e) {
                throw new IllegalStateException(e);
            }
            return null;
        });
    }

    public List<Experiment> getExperiments(final CriteriaBuilder cb,
                                           final BiFunction<Root<Experiment>, Path<Instant>, Predicate>
                                                   predicateFunction) {
        final CriteriaQuery<Experiment> query = cb.createQuery(Experiment.class);

        final Root<Experiment> experiment = query.from(Experiment.class);

        final Path<Instant> startTime = experiment.get(Experiment_.startTime);
        query.where(predicateFunction.apply(experiment, startTime));

        query.orderBy(cb.asc(startTime));

        return experimentDao.getResultList(query);
    }
}
