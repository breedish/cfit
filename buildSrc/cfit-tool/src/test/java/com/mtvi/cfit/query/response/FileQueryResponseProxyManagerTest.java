package com.mtvi.cfit.query.response;

import com.mtvi.cfit.query.definition.QueryDefinition;
import junit.framework.Assert;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import java.io.File;

/**
 * Class FileQueryResponseProxyManagerTest.
 *
 * @author zenind
 */
public class FileQueryResponseProxyManagerTest {

    private final ToFileQueryResponseManager manager = new ToFileQueryResponseManager();

    @Test
    public void testToFileConversion() throws Exception {
        QueryResponse expected = buildResponse();

        File tmp = File.createTempFile("prefix1", "suffix1");
        manager.save(expected, tmp);

        QueryResponse actual = manager.load(tmp);
        Assert.assertEquals(expected.getUrl(), actual.getUrl());
        Assert.assertEquals(expected.getContent(), actual.getContent());
        Assert.assertEquals(expected.getStatus(), actual.getStatus());
        Assert.assertEquals(expected.getTime(), actual.getTime());
        Assert.assertEquals(tmp.getName(), actual.getDefinition().getId());
    }

    @Test
    public void testOldFormatConversion() throws Exception {
        QueryResponse response = manager.load(new File(getClass().getResource("/jsonpath-responses/oldformat.response").getPath()));
        Assert.assertNotNull(response);
        Assert.assertEquals(
            "http://shared-crx-001.1515.mtvi.com:12042/jp/gametrailers.com?&q=%7B%22select%22%3A%7B%22mtvi%3Aid%22%3A1%7D%2C%22vars%22%3A%7B%7D%2C%22where%22%3A%7B%22byTypeAnd3LinkParamsAndExcludeItemsOrderByOriginalPublishDate%22%3A%5B%5B%22Standard%3AVideo%22%5D%2C%20%22285420ba-45d2-49e7-9a01-f8bb8ebdfec6%22%2C%20%5B%22c069590c-e6a7-11e0-9921-a4badb23230a%22%5D%2C%20%5B%226ce327fe-05c1-4ece-9bb2-eab68764cd06%22%5D%2C%20%5B%5D%5D%7D%2C%22start%22%3A0%2C%22rows%22%3A1%2C%22omitNumFound%22%3Atrue%2C%22debug%22%3A%7B%20%20%7D%7D&stage=live&filterSchedules=true&dateFormat=UTC&indent=true&plugin.timeTravel=2013-07-30T04:00:00.000Z&timeTravel=2013-07-30T04:00:00.000Z",
            response.getUrl()
        );
        Assert.assertTrue(StringUtils.isNotBlank(response.getContent()));
    }

    private QueryResponse buildResponse() throws Exception {
        QueryResponse response = manager.load(new File(getClass().getResource("/jsonpath-responses/oldformat.response").getPath()));
        return new QueryResponse(new QueryDefinition("tmp_file_01", response.getUrl()), response.getUrl(), response.getContent(), 500, 222);
    }
}
