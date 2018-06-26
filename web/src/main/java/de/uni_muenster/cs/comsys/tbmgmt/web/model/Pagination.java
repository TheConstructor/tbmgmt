package de.uni_muenster.cs.comsys.tbmgmt.web.model;

import com.google.common.math.IntMath;
import com.google.common.math.LongMath;
import com.google.common.primitives.Ints;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.Dao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.TimestampedEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Created by matthias on 20.11.15.
 */
public class Pagination<E extends TimestampedEntity> {

    private final Dao<E, ?> dao;
    private final Function<Dao<E, ?>, CriteriaQuery<E>> entityQueryProvider;
    private final int perPage;
    private final int pageCount;
    private final int currentPage;
    private final List<Integer> offeredPages;
    private final String initialQuery;

    public Pagination(final Dao<E, ?> dao, final int perPage, final int currentPage, final String initialQuery) {
        this(dao, (cb, r) -> null, (cb, r) -> null, perPage, currentPage, initialQuery);
    }

    public Pagination(final Dao<E, ?> dao, final BiFunction<CriteriaBuilder, Root<E>, List<Order>> orderProvider,
                      final int perPage, final int currentPage, final String initialQuery) {
        this(dao, (cb, r) -> null, orderProvider, perPage, currentPage, initialQuery);
    }

    public Pagination(final Dao<E, ?> dao, final BiFunction<CriteriaBuilder, Root<E>, Predicate> predicateProvider,
                      final BiFunction<CriteriaBuilder, Root<E>, List<Order>> orderProvider, final int perPage,
                      final int currentPage, final String initialQuery) {
        this(dao, (d) -> {
            final CriteriaBuilder cb = d.getCriteriaBuilder();
            final CriteriaQuery<E> cq = cb.createQuery(d.getEntityClass());
            final Root<E> root = cq.from(d.getEntityClass());
            final Predicate predicate = predicateProvider.apply(cb, root);
            if (predicate != null) {
                cq.where(predicate);
            }
            final List<Order> order = orderProvider.apply(cb, root);
            if (order != null) {
                cq.orderBy(order);
            }
            return cq;
        }, (d) -> {
            final CriteriaBuilder cb = d.getCriteriaBuilder();
            final CriteriaQuery<Long> cq = cb.createQuery(Long.class);
            final Root<E> root = cq.from(d.getEntityClass());
            cq.select(cb.count(root));
            final Predicate predicate = predicateProvider.apply(cb, root);
            if (predicate != null) {
                cq.where(predicate);
            }
            return cq;
        }, perPage, currentPage, initialQuery);
    }

    public Pagination(final Dao<E, ?> dao, final Function<Dao<E, ?>, CriteriaQuery<E>> entityQueryProvider,
                      final Function<Dao<E, ?>, CriteriaQuery<Long>> entityCountQueryProvider,
                      final int perPage, final int currentPage, final String initialQuery) {
        this.dao = dao;
        this.entityQueryProvider = entityQueryProvider;
        this.perPage = Math.max(perPage, 1);
        this.initialQuery = initialQuery;
        pageCount = getPageCount(dao, entityCountQueryProvider, this.perPage);
        this.currentPage = dao.validPage(currentPage, pageCount);
        this.offeredPages = new ArrayList<>();
        initOfferedPages();
    }

    private static <E> int getPageCount(final Dao<E, ?> dao,
                                        final Function<Dao<E, ?>, CriteriaQuery<Long>> entityCountQueryProvider,
                                        final int perPage) {
        final Long entityCount = dao.getSingleResult(entityCountQueryProvider.apply(dao));
        return Ints.checkedCast(LongMath.divide(entityCount, perPage, RoundingMode.UP));
    }

    public static String createQueryString(final Map<String, String> params) {
        if (params == null) {
            return createQueryString((MultiValueMap<String, String>) null);
        }
        final LinkedMultiValueMap<String, String> multiParams = new LinkedMultiValueMap<>();
        multiParams.setAll(params);
        return createQueryString(multiParams);
    }

    public static String createQueryString(final MultiValueMap<String, String> params) {
        final String query = UriComponentsBuilder.newInstance().queryParams(params).build().encode().getQuery();
        if (StringUtils.isEmpty(query)) {
            return "";
        }
        return "?" + query;
    }

    /**
     * Creates a range of Pages to skip to. Basic Idea is to provide fewer links the further you move away from the
     * current page, but still provide enough links that you can move to any desired page within O(log(n)) clicks.
     */
    private void initOfferedPages() {
        final TreeSet<Integer> offeredPages = new TreeSet<>();
        offeredPages.add(0);
        offeredPages.add(currentPage);
        if (pageCount > 1) {
            offeredPages.add(pageCount - 1);
        }

        int step = 1;
        int stepCount = 1;
        final int stepMult = 5;
        final int maxDistance = Math.max(currentPage, pageCount - currentPage - 1);
        for (int distance = 1; distance < maxDistance; distance += step) {
            final int left = currentPage - distance;
            final int right = currentPage + distance;
            if (left >= 0) {
                offeredPages.add(left);
            }
            if (right < pageCount) {
                offeredPages.add(right);
            }
            stepCount++;
            if (stepCount >= 4) {
                step = step * stepMult;
                stepCount = 0;
            }
        }

        this.offeredPages.addAll(offeredPages);
    }

    public int getPerPage() {
        return perPage;
    }

    public long getPageCount() {
        return pageCount;
    }

    public long getCurrentPage() {
        return currentPage;
    }

    public List<Integer> getOfferedPages() {
        return offeredPages;
    }

    public List<E> getEntities() {
        final CriteriaQuery<E> cq = entityQueryProvider.apply(dao);
        return dao.getResultList(cq, IntMath.checkedMultiply(currentPage, perPage), perPage);
    }

    public String getInitialQuery() {
        return initialQuery;
    }
}
