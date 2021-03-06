package org.stagemonitor.core.rest;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RestClientTest {

	@Test
	public void testGetDashboardForElasticsearch() throws Exception {
	 	String expected = "{\"user\":\"guest\",\"group\":\"guest\",\"title\":\"Test\",\"tags\":[\"jdbc\",\"db\"],\"dashboard\":\"{\\\"title\\\":\\\"Test\\\",\\\"editable\\\":true,\\\"failover\\\":false,\\\"panel_hints\\\":true,\\\"style\\\":\\\"dark\\\",\\\"refresh\\\":\\\"1m\\\",\\\"tags\\\":[\\\"jdbc\\\",\\\"db\\\"],\\\"timezone\\\":\\\"browser\\\"}\"}";
		assertEquals(expected, RestClient.getDashboardForElasticsearch("Test Dashboard.json").toString());
	}

	@Test
	public void testGetMajorMinorVersionFromFullVersionString() {
		assertEquals("123.456", RestClient.getMajorMinorVersionFromFullVersionString("123.456.789-SNAPSHOT"));
	}
}
