package de.uni_muenster.cs.comsys.tbmgmt.web.model;

import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Collections;
import java.util.Map;

/**
 * Created by matthias on 02.03.16.
 */
public class PaginationTest {

    @Test
    public void testCreateQueryString() throws Exception {
        Assert.assertEquals("", Pagination.createQueryString((MultiValueMap<String, String>) null));
        Assert.assertEquals("", Pagination.createQueryString((Map<String, String>) null));

        Assert.assertEquals("", Pagination.createQueryString(new LinkedMultiValueMap<>(ImmutableMap.of())));
        Assert.assertEquals("", Pagination.createQueryString(ImmutableMap.of()));

        Assert.assertEquals("?q=%26%3D%25a", Pagination.createQueryString(
                new LinkedMultiValueMap<>(ImmutableMap.of("q", Collections.singletonList("&=%a")))));
        Assert.assertEquals("?q=%26%3D%25a", Pagination.createQueryString(ImmutableMap.of("q", "&=%a")));

        Assert.assertEquals("?q=%26%3D%25a&s=sup%C3%A4r", Pagination.createQueryString(new LinkedMultiValueMap<>(
                ImmutableMap.of("q", Collections.singletonList("&=%a"), "s", Collections.singletonList("supär")))));
        Assert.assertEquals("?q=%26%3D%25a&s=sup%C3%A4r",
                Pagination.createQueryString(ImmutableMap.of("q", "&=%a", "s", "supär")));
    }
}