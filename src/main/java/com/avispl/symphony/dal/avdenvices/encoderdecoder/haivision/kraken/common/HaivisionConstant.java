/*
 *  Copyright (c) 2024 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.avdenvices.encoderdecoder.haivision.kraken.common;

/**
 * Enum representing various the constant.
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 9/19/2024
 * @since 1.0.0
 */
public class HaivisionConstant {
	public static final String AUTHENTICATION_PARAM = "{\"username\":\"%s\", \"password\":\"%s\"}";
	public static final String NONE = "None";
	public static final String EMPTY = "";
	public static final String SPACE = " ";
	public static final String COLON = ":";
	public static final String HASH = "#";
	public static final String TRUE = "true";
	public static final String FALSE = "false";
	public static final String COOKIE = "Cookie";
	public static final String SET_COOKIE = "Set-Cookie";
	public static final String NETWORK_INTERFACE = "network_interfaces";
	public static final String NETWORK = "Network: ";
	public static final String UUID = "uuid";
	public static final String ERROR = "error";
	public static final String RESULT = "result";
	public static final String NAME = "name";
	public static final String STREAM_LIST = "stream_list";
	public static final String HTTPS = "https://";
	public static final String NICS = "nics";
	public static final String INDEX = "index";
	public static final String DATA = "data";
	public static final String METADATA_LIST = "metadata_list";


	/**
	 * Token timeout is 1 hour ( 60 minutes), as this case reserve 5 minutes to make sure we never failed because of the timeout
	 */
	public static final long TIMEOUT = 55;
}
