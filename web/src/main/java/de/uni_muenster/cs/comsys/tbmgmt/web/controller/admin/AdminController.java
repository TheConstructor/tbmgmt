package de.uni_muenster.cs.comsys.tbmgmt.web.controller.admin;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.EvaluationScriptDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.TagDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.node.InterfaceTypeDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.node.NodeDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.node.NodeTypeDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.node.TestbedDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.EvaluationScript;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.EvaluationScript_;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.Tag;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.Tag_;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.InterfaceType;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.InterfaceType_;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.Node;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.NodeType;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.NodeType_;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.Node_;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.Testbed;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.Testbed_;
import de.uni_muenster.cs.comsys.tbmgmt.web.model.Pagination;
import de.uni_muenster.cs.comsys.tbmgmt.web.support.InstantViewRenderer;
import de.uni_muenster.cs.comsys.tbmgmt.web.support.TbmgmtWebUtils;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;

/**
 * Created by matthias on 30.10.15.
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private EvaluationScriptDao evaluationScriptDao;

    @Autowired
    private InterfaceTypeDao interfaceTypeDao;

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private NodeTypeDao nodeTypeDao;

    @Autowired
    private TagDao tagDao;

    @Autowired
    private TestbedDao testbedDao;

    @Autowired
    private InstantViewRenderer instantViewRenderer;

    @Autowired
    private TransactionTemplate readOnlyTransactionTemplate;

    @RequestMapping(value = {"/evaluationScripts", "/evaluationScripts/"})
    @Secured(TbmgmtWebUtils.ROLE_ADMIN)
    public void evaluationScripts(
            @RequestParam(value = "page", required = false, defaultValue = "0") @Min(0) final int page,
            @RequestParam(value = "perPage", required = false, defaultValue = "20") @Min(1) final int perPage,
            final HttpServletRequest request, final HttpServletResponse response, final Model model) {
        readOnlyTransactionTemplate.execute(transactionStatus -> {
            try {
                model.addAttribute("pagination", new Pagination<>(evaluationScriptDao,
                        (cb, r) -> Collections.singletonList(cb.asc(r.get(EvaluationScript_.fileName))), perPage, page,
                        ""));
                instantViewRenderer.render(new ModelAndView("admin/evaluationScripts", model.asMap()), request,
                        response);
            } catch (final Exception e) {
                throw new IllegalStateException(e);
            }
            return null;
        });
    }

    @RequestMapping(value = "/evaluationScripts/{id}/delete", method = RequestMethod.POST)
    @Secured(TbmgmtWebUtils.ROLE_ADMIN)
    public String deleteEvaluationScript(@PathVariable("id") final long id,
                                         final RedirectAttributes redirectAttributes) {
        evaluationScriptDao.getTransactionTemplate().execute(transactionStatus -> {
            final EvaluationScript evaluationScript = evaluationScriptDao.find(id);
            if (evaluationScript != null) {
                evaluationScriptDao.remove(evaluationScript);
                redirectAttributes.addFlashAttribute("flash_success", "Deleted evaluationScript with id " + id);
            } else {
                redirectAttributes.addFlashAttribute("flash_error", "Can not find evaluationScript with id " + id);
            }
            return null;
        });
        return "redirect:/admin/evaluationScripts";
    }

    @RequestMapping(value = {"/interfaceTypes", "/interfaceTypes/"})
    @Secured(TbmgmtWebUtils.ROLE_ADMIN)
    public void interfaceTypes(
            @RequestParam(value = "page", required = false, defaultValue = "0") @Min(0) final int page,
            @RequestParam(value = "perPage", required = false, defaultValue = "20") @Min(1) final int perPage,
            final HttpServletRequest request, final HttpServletResponse response, final Model model) {
        readOnlyTransactionTemplate.execute(transactionStatus -> {
            try {
                model.addAttribute("pagination", new Pagination<>(interfaceTypeDao,
                        (cb, r) -> Collections.singletonList(cb.asc(r.get(InterfaceType_.name))), perPage, page, ""));
                instantViewRenderer.render(new ModelAndView("admin/interfaceTypes", model.asMap()), request, response);
            } catch (final Exception e) {
                throw new IllegalStateException(e);
            }
            return null;
        });
    }

    @RequestMapping(value = "/interfaceTypes/{id}/delete", method = RequestMethod.POST)
    @Secured(TbmgmtWebUtils.ROLE_ADMIN)
    public String deleteInterfaceType(@PathVariable("id") final long id, final RedirectAttributes redirectAttributes) {
        interfaceTypeDao.getTransactionTemplate().execute(transactionStatus -> {
            final InterfaceType interfaceType = interfaceTypeDao.find(id);
            if (interfaceType != null) {
                interfaceTypeDao.remove(interfaceType);
                redirectAttributes.addFlashAttribute("flash_success", "Deleted interfaceType with id " + id);
            } else {
                redirectAttributes.addFlashAttribute("flash_error", "Can not find interfaceType with id " + id);
            }
            return null;
        });
        return "redirect:/admin/interfaceTypes";
    }

    @RequestMapping(value = {"/nodes", "/nodes/"})
    @Secured(TbmgmtWebUtils.ROLE_ADMIN)
    public void nodes(
            @RequestParam(value = "page", required = false, defaultValue = "0") @Min(0) final int page,
            @RequestParam(value = "perPage", required = false, defaultValue = "20") @Min(1) final int perPage,
            final HttpServletRequest request, final HttpServletResponse response, final Model model) {
        readOnlyTransactionTemplate.execute(transactionStatus -> {
            try {
                model.addAttribute("pagination",
                        new Pagination<>(nodeDao, (cb, r) -> Collections.singletonList(cb.asc(r.get(Node_.name))),
                                perPage, page, ""));
                instantViewRenderer.render(new ModelAndView("admin/nodes", model.asMap()), request, response);
            } catch (final Exception e) {
                throw new IllegalStateException(e);
            }
            return null;
        });
    }

    @RequestMapping(value = "/nodes/{id}/delete", method = RequestMethod.POST)
    @Secured(TbmgmtWebUtils.ROLE_ADMIN)
    public String deleteNode(@PathVariable("id") final long id, final RedirectAttributes redirectAttributes) {
        nodeDao.getTransactionTemplate().execute(transactionStatus -> {
            final Node node = nodeDao.find(id);
            if (node != null) {
                nodeDao.remove(node);
                redirectAttributes.addFlashAttribute("flash_success", "Deleted node with id " + id);
            } else {
                redirectAttributes.addFlashAttribute("flash_error", "Can not find node with id " + id);
            }
            return null;
        });
        return "redirect:/admin/nodes";
    }

    @RequestMapping(value = {"/nodeTypes", "/nodeTypes/"})
    @Secured(TbmgmtWebUtils.ROLE_ADMIN)
    public void nodeTypes(@RequestParam(value = "page", required = false, defaultValue = "0") @Min(0) final int page,
                          @RequestParam(value = "perPage", required = false, defaultValue = "20") @Min(1) final int
                                  perPage,
                          final HttpServletRequest request, final HttpServletResponse response, final Model model) {
        readOnlyTransactionTemplate.execute(transactionStatus -> {
            try {
                model.addAttribute("pagination", new Pagination<>(nodeTypeDao,
                        (cb, r) -> Collections.singletonList(cb.asc(r.get(NodeType_.name))), perPage, page, ""));
                instantViewRenderer.render(new ModelAndView("admin/nodeTypes", model.asMap()), request, response);
            } catch (final Exception e) {
                throw new IllegalStateException(e);
            }
            return null;
        });
    }

    @RequestMapping(value = "/nodeTypes/{id}/delete", method = RequestMethod.POST)
    @Secured(TbmgmtWebUtils.ROLE_ADMIN)
    public String deleteNodeType(@PathVariable("id") final long id, final RedirectAttributes redirectAttributes) {
        nodeTypeDao.getTransactionTemplate().execute(transactionStatus -> {
            final NodeType nodeType = nodeTypeDao.find(id);
            if (nodeType != null) {
                nodeTypeDao.remove(nodeType);
                redirectAttributes.addFlashAttribute("flash_success", "Deleted nodeType with id " + id);
            } else {
                redirectAttributes.addFlashAttribute("flash_error", "Can not find nodeType with id " + id);
            }
            return null;
        });
        return "redirect:/admin/nodeTypes";
    }

    @RequestMapping(value = {"/tags", "/tags/"})
    @Secured(TbmgmtWebUtils.ROLE_ADMIN)
    public void tags(@RequestParam(value = "page", required = false, defaultValue = "0") @Min(0) final int page,
                     @RequestParam(value = "perPage", required = false, defaultValue = "20") @Min(1) final int perPage,
                     final HttpServletRequest request, final HttpServletResponse response, final Model model) {
        readOnlyTransactionTemplate.execute(transactionStatus -> {
            try {
                model.addAttribute("pagination",
                        new Pagination<>(tagDao, (cb, r) -> Collections.singletonList(cb.asc(r.get(Tag_.name))),
                                perPage, page, ""));
                instantViewRenderer.render(new ModelAndView("admin/tags", model.asMap()), request, response);
            } catch (final Exception e) {
                throw new IllegalStateException(e);
            }
            return null;
        });
    }

    @RequestMapping(value = "/tags/{id}/delete", method = RequestMethod.POST)
    @Secured(TbmgmtWebUtils.ROLE_ADMIN)
    public String deleteTag(@PathVariable("id") final long id, final RedirectAttributes redirectAttributes) {
        tagDao.getTransactionTemplate().execute(transactionStatus -> {
            final Tag tag = tagDao.find(id);
            if (tag != null) {
                tagDao.remove(tag);
                redirectAttributes.addFlashAttribute("flash_success", "Deleted tag with id " + id);
            } else {
                redirectAttributes.addFlashAttribute("flash_error", "Can not find tag with id " + id);
            }
            return null;
        });
        return "redirect:/admin/tags";
    }

    @RequestMapping(value = {"/testbeds", "/testbeds/"})
    @Secured(TbmgmtWebUtils.ROLE_ADMIN)
    public void testbeds(@RequestParam(value = "page", required = false, defaultValue = "0") @Min(0) final int page,
                         @RequestParam(value = "perPage", required = false, defaultValue = "20") @Min(1) final int
                                 perPage,
                         final HttpServletRequest request, final HttpServletResponse response, final Model model) {
        readOnlyTransactionTemplate.execute(transactionStatus -> {
            try {
                model.addAttribute("pagination",
                        new Pagination<>(testbedDao, (cb, r) -> Collections.singletonList(cb.asc(r.get(Testbed_.name))),
                                perPage, page, ""));
                instantViewRenderer.render(new ModelAndView("admin/testbeds", model.asMap()), request, response);
            } catch (final Exception e) {
                throw new IllegalStateException(e);
            }
            return null;
        });
    }

    @RequestMapping(value = "/testbeds/{id}/delete", method = RequestMethod.POST)
    @Secured(TbmgmtWebUtils.ROLE_ADMIN)
    public String deleteTestbed(@PathVariable("id") final long id, final RedirectAttributes redirectAttributes) {
        testbedDao.getTransactionTemplate().execute(transactionStatus -> {
            final Testbed testbed = testbedDao.find(id);
            if (testbed != null) {
                testbedDao.remove(testbed);
                redirectAttributes.addFlashAttribute("flash_success", "Deleted testbed with id " + id);
            } else {
                redirectAttributes.addFlashAttribute("flash_error", "Can not find testbed with id " + id);
            }
            return null;
        });
        return "redirect:/admin/testbeds";
    }
}
