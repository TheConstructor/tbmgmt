package de.uni_muenster.cs.comsys.tbmgmt.web.controller;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.TagDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.Tag;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.Tag_;
import de.uni_muenster.cs.comsys.tbmgmt.core.utils.TbmgmtUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

/**
 * Created by matthias on 30.10.15.
 */
@Controller
@RequestMapping("/tags")
public class TagsController {

    @Autowired
    private TagDao tagDao;

    @RequestMapping(value = "/json", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public
    @ResponseBody
    List<Tag> tagsJson(@RequestParam(value = "q", required = false, defaultValue = "") final String q) {
        final CriteriaBuilder cb = tagDao.getCriteriaBuilder();
        final CriteriaQuery<Tag> query = cb.createQuery(Tag.class);
        final Root<Tag> tagRoot = query.from(Tag.class);
        if (StringUtils.isNotBlank(q)) {
            query.where(cb.like(cb.lower(tagRoot.get(Tag_.name)),
                    cb.lower(cb.literal("%" + TbmgmtUtil.escapeLikeString(q) + "%")), TbmgmtUtil.LIKE_ESCAPE_CHAR));
        }
        return tagDao.getResultList(query, 0, 20);
    }
}
