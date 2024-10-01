/*
 *  Copyright (c) 2024 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.avdenvices.encoderdecoder.haivision.kraken.common;

/**
 * Enum representing various the command.
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 9/16/2024
 * @since 1.0.0
 */
public class HaivisionCommand {
	public final static String API_LOGIN = "/apis/v2/login";
	public final static String API_LOGOUT = "/apis/v2/logout";
	public final static String GET_SYSTEM_INFO ="apis/v2/systeminfo";
	public final static String GET_NETWORK_INFO = "apis/v2/system/network";
	public final static String GET_LICENSE_INFO = "apis/v2/license";
	public final static String GET_ALL_STREAMS = "apis/v2/streams";
	public final static String GET_METADATA = "apis/v2/metadata";
	public final static String GET_SYSTEM_GPUS = "apis/v2/system/load";
	public final static String GET_RTSP = "apis/v2/system/services/rtspserver";
	public final static String GET_WEBSERVER = "apis/v2/system/services/webserver";

}
