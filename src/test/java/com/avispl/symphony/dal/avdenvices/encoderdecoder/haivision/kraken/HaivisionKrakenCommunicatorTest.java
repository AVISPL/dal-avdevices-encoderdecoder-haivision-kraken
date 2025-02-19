/*
 *  Copyright (c) 2024 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.avdenvices.encoderdecoder.haivision.kraken;


import java.util.Map;

import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.avispl.symphony.api.dal.dto.monitor.ExtendedStatistics;

import javax.security.auth.login.FailedLoginException;

public class HaivisionKrakenCommunicatorTest {
	private HaivisionKrakenCommunicator haivisionKrakenCommunicator;
	private ExtendedStatistics extendedStatistic;

	@BeforeEach
	void setUp() throws Exception {
		haivisionKrakenCommunicator = new HaivisionKrakenCommunicator();
		haivisionKrakenCommunicator.setTrustAllCertificates(true);
		haivisionKrakenCommunicator.setHost("");
		haivisionKrakenCommunicator.setLogin("");
		haivisionKrakenCommunicator.setPassword("");
		haivisionKrakenCommunicator.setPort(443);
		haivisionKrakenCommunicator.init();
		haivisionKrakenCommunicator.connect();
	}

	@AfterEach
	void destroy() throws Exception {
		haivisionKrakenCommunicator.disconnect();
		haivisionKrakenCommunicator.destroy();
	}

	@Test
	void testLoginSuccess() throws Exception {
		haivisionKrakenCommunicator.getMultipleStatistics();
	}

	@Test
	void testFailedLogin() throws Exception {
		haivisionKrakenCommunicator.destroy();
		haivisionKrakenCommunicator.setPassword("Invalid-password");
		haivisionKrakenCommunicator.init();
		haivisionKrakenCommunicator.connect();
		Assert.assertThrows(FailedLoginException.class, () -> {
			haivisionKrakenCommunicator.getMultipleStatistics();
		});
	}

	@Test
	void testGetAllStream() throws Exception{
		extendedStatistic = (ExtendedStatistics) haivisionKrakenCommunicator.getMultipleStatistics().get(0);
		Map<String, String> statistics = extendedStatistic.getStatistics();
		System.out.println(statistics);
		Assert.assertEquals(96, statistics.size());
	}
@Test
void testSessionExist() throws Exception {
	while (true) {
		extendedStatistic = (ExtendedStatistics) haivisionKrakenCommunicator.getMultipleStatistics().get(0);
		Map<String, String> statistics = extendedStatistic.getStatistics();
		Assert.assertEquals(96, statistics.size());
		System.out.println("Statistics size is correct. Waiting 60 seconds before next check...");
		Thread.sleep(60000);
	}
}


}