/*
 * Copyright (c) 2026 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.avdenvices.encoderdecoder.haivision.kraken;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.avispl.symphony.dal.avdenvices.encoderdecoder.haivision.kraken.common.HaivisionConstant;
import com.avispl.symphony.dal.avdenvices.encoderdecoder.haivision.kraken.common.metric.SystemsEnum;
import com.avispl.symphony.dal.avdenvices.encoderdecoder.haivision.kraken.common.metric.childSystem.SystemLoad;

/**
 * Uptime (Main group) and System#SystemCPULoad(%) are reported in extended properties as in 1.0.0 (SYAL-2292).
 */
@Tag("Mock")
public class HaivisionKrakenStatisticsTest {

	private static final String CPU_KEY = HaivisionConstant.SYSTEM + HaivisionConstant.HASH + SystemLoad.SYS_CPU_LOAD.getName();
	private static final String SYSTEM_UPTIME_KEY = HaivisionConstant.SYSTEM + HaivisionConstant.HASH + SystemLoad.SYS_UP_TIME.getName();

	private void populateGenerateStatistics(Map<String, String> stats) throws Exception {
		Method method = HaivisionKrakenCommunicator.class.getDeclaredMethod("populateGenerateStatistics", Map.class);
		method.setAccessible(true);
		method.invoke(new HaivisionKrakenCommunicator(), stats);
	}

	@Test
	void testUptimeInMainGroup() {
		Assertions.assertTrue(Arrays.stream(SystemsEnum.values()).anyMatch(item -> "Uptime".equals(item.getName()) && "uptime".equals(item.getField())));
	}

	@Test
	void testSystemCpuLoadKeptInExtendedProperties() throws Exception {
		Map<String, String> stats = new HashMap<>();
		stats.put(CPU_KEY, "12.5");
		stats.put(SYSTEM_UPTIME_KEY, "3600");
		populateGenerateStatistics(stats);
		Assertions.assertEquals("12.5", stats.get(CPU_KEY));
	}

	@Test
	void testNoneValuesDoNotFail() throws Exception {
		Map<String, String> stats = new HashMap<>();
		stats.put(CPU_KEY, HaivisionConstant.NONE);
		stats.put(SYSTEM_UPTIME_KEY, HaivisionConstant.NONE);
		Assertions.assertDoesNotThrow(() -> populateGenerateStatistics(stats));
	}
}
