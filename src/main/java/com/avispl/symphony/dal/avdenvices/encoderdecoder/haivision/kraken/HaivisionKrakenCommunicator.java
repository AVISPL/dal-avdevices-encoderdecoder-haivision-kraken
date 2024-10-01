/*
 *  Copyright (c) 2024 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.avdenvices.encoderdecoder.haivision.kraken;


import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.avispl.symphony.api.dal.error.ResourceNotReachableException;
import com.avispl.symphony.dal.avdenvices.encoderdecoder.haivision.kraken.common.HaivisionCommand;
import com.avispl.symphony.dal.avdenvices.encoderdecoder.haivision.kraken.common.HaivisionConstant;
import com.avispl.symphony.dal.avdenvices.encoderdecoder.haivision.kraken.common.PingMode;
import com.avispl.symphony.dal.avdenvices.encoderdecoder.haivision.kraken.common.metric.*;
import com.avispl.symphony.dal.avdenvices.encoderdecoder.haivision.kraken.common.metric.childSystem.SystemGPU;
import com.avispl.symphony.dal.avdenvices.encoderdecoder.haivision.kraken.common.metric.childSystem.SystemLoad;
import com.avispl.symphony.dal.util.StringUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.CollectionUtils;


import com.avispl.symphony.api.dal.control.Controller;
import com.avispl.symphony.api.dal.dto.control.ControllableProperty;
import com.avispl.symphony.api.dal.dto.monitor.ExtendedStatistics;
import com.avispl.symphony.api.dal.dto.monitor.Statistics;
import com.avispl.symphony.api.dal.monitor.Monitorable;
import com.avispl.symphony.dal.communicator.RestCommunicator;

import javax.security.auth.login.FailedLoginException;


/**
 * HaivisionKrakenCommunicator Adapter
 *
 * Supported features are:
 * Monitoring for System Information and Stream information
 *
 * Monitoring Capabilities:
 * Haivision Kraken Video Transcoder version 1.0.0
 *
 * CurrentTime
 * Uptime
 * Version
 * License
 * Network
 * NetworkInterface
 * Service
 * Streams
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 8/15/2024
 * @since 1.0.0
 */
public class HaivisionKrakenCommunicator extends RestCommunicator implements Monitorable, Controller {

	/**
	 * store authentication information
	 */
	private String authenticationCookie = HaivisionConstant.EMPTY;

	/**
	 * ReentrantLock to prevent telnet session is closed when adapter is retrieving statistics from the device.
	 */
	private final ReentrantLock reentrantLock = new ReentrantLock();

	/**
	 * ObjectMapper instance used for converting between
	 * Java objects and JSON representations.
	 */
	private final ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * A set containing system info.
	 */
	private Set<String> allSystemInfoSet = new HashSet<>();

	/**
	 * A set containing stream info.
	 */
	private Set<String> allStreamNameSet = new HashSet<>();

	/**
	 * A set containing network info.
	 */
	private Set<String> allNetworkSet = new HashSet<>();

	/**
	 * A set containing system gpu info.
	 */
	private Set<String> allSystemGPUSet = new HashSet<>();

	/**
	 * Store previous/current ExtendedStatistics
	 */
	private ExtendedStatistics localExtendedStatistics;

	/**
	 * isEmergencyDelivery to check if control flow is trigger
	 */
	private boolean isEmergencyDelivery;

	/**
	 * A cache that maps route names to their corresponding values.
	 */
	private final Map<String, String> cacheValue = new HashMap<>();

	/**
	 * ping mode
	 */
	private PingMode pingMode = PingMode.ICMP;

	/**
	 * Retrieves {@link #pingMode}
	 *
	 * @return value of {@link #pingMode}
	 */
	public PingMode getPingMode() {
		return pingMode;
	}

	/**
	 * Sets {@link #pingMode} value
	 *
	 * @param pingMode new value of {@link #pingMode}
	 */
	public void setPingMode(PingMode pingMode) {
		this.pingMode = pingMode;
	}

	/**
	 * Constructs a new instance of HaivisionKrakenCommunicator.
	 */
	public HaivisionKrakenCommunicator() throws IOException {
		this.setTrustAllCertificates(true);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 *
	 * Check for available devices before retrieving the value
	 * ping latency information to Symphony
	 */
	@Override
	public int ping() throws Exception {
		if (this.pingMode == PingMode.ICMP) {
			return super.ping();
		} else if (this.pingMode == PingMode.TCP) {
			if (isInitialized()) {
				long pingResultTotal = 0L;

				for (int i = 0; i < this.getPingAttempts(); i++) {
					long startTime = System.currentTimeMillis();

					try (Socket puSocketConnection = new Socket(this.host, this.getPort())) {
						puSocketConnection.setSoTimeout(this.getPingTimeout());
						if (puSocketConnection.isConnected()) {
							long pingResult = System.currentTimeMillis() - startTime;
							pingResultTotal += pingResult;
							if (this.logger.isTraceEnabled()) {
								this.logger.trace(String.format("PING OK: Attempt #%s to connect to %s on port %s succeeded in %s ms", i + 1, host, this.getPort(), pingResult));
							}
						} else {
							if (this.logger.isDebugEnabled()) {
								this.logger.debug(String.format("PING DISCONNECTED: Connection to %s did not succeed within the timeout period of %sms", host, this.getPingTimeout()));
							}
							return this.getPingTimeout();
						}
					} catch (SocketTimeoutException | ConnectException tex) {
						throw new RuntimeException("Socket connection timed out", tex);
					} catch (UnknownHostException ex) {
						throw new UnknownHostException(String.format("Connection timed out, UNKNOWN host %s", host));
					} catch (Exception e) {
						if (this.logger.isWarnEnabled()) {
							this.logger.warn(String.format("PING TIMEOUT: Connection to %s did not succeed, UNKNOWN ERROR %s: ", host, e.getMessage()));
						}
						return this.getPingTimeout();
					}
				}
				return Math.max(1, Math.toIntExact(pingResultTotal / this.getPingAttempts()));
			} else {
				throw new IllegalStateException("Cannot use device class without calling init() first");
			}
		} else {
			throw new IllegalArgumentException("Unknown PING Mode: " + pingMode);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void authenticate() throws Exception {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void internalInit() throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("Internal init is called.");
		}
		super.internalInit();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void internalDestroy() {
		if (StringUtils.isNotNullOrEmpty(this.authenticationCookie)) {
			deleteCookieSession();
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Internal destroy is called.");
		}
		localExtendedStatistics = null;
		cacheValue.clear();
		super.internalDestroy();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected HttpHeaders putExtraRequestHeaders(HttpMethod httpMethod, String uri, HttpHeaders headers) throws Exception {
		if (StringUtils.isNotNullOrEmpty(this.authenticationCookie)) {
			headers.add(HaivisionConstant.COOKIE, "DisplayUnsavedWarning=true; Path=/; Secure;");
			headers.add(HaivisionConstant.COOKIE, String.format("id=%s; Path=/; Secure; HttpOnly;", this.authenticationCookie));
		}
		return super.putExtraRequestHeaders(httpMethod, uri, headers);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Statistics> getMultipleStatistics() throws Exception {
		reentrantLock.lock();
		try {
			Map<String, String> stats = new HashMap<>();
			ExtendedStatistics extendedStatistics = new ExtendedStatistics();
			if (!isEmergencyDelivery) {
				if (!isValidCookie()) {
					throw new FailedLoginException(String.format("Failed to login to device: %s.", this.authenticationCookie));
				}
				populateSystemInfo(stats);
				populateNetworkInfo(stats);
				populateLicenseInfo(stats);
				populateStreamsInfo(stats);
				populateSystemGPUInfo(stats);
				populateServiceInfo(stats);
				extendedStatistics.setStatistics(stats);
				localExtendedStatistics = extendedStatistics;
			}
			isEmergencyDelivery = false;
		} finally {
			reentrantLock.unlock();
		}
		return Collections.singletonList(localExtendedStatistics);
	}

	/**
	 * Populates system information into the provided stats map by retrieving data from the system info endpoint.
	 *
	 * @param stats a map to store system information as key-value pairs
	 * @throws ResourceNotReachableException if the system information cannot be retrieved
	 */
	private void populateSystemInfo(Map<String, String> stats) throws Exception{
		try {
			// retrieve data
			JsonNode response = this.doGet(HaivisionCommand.GET_SYSTEM_INFO, JsonNode.class);
			if (response != null && response.has(HaivisionConstant.RESULT) && response.get(HaivisionConstant.RESULT).asBoolean()) {
				allSystemInfoSet.clear();
				for (SystemsEnum item : SystemsEnum.values()) {
					if (response.has(item.getField())) {
						cacheValue.put(item.getName(), response.get(item.getField()).asText());
					}
				}
				// retrieve data network interface of system
				JsonNode networkInterfaces = response.get(HaivisionConstant.NETWORK_INTERFACE);
				if (networkInterfaces != null && networkInterfaces.isObject()) {
					Iterator<String> interfaces = networkInterfaces.fieldNames();
					while (interfaces.hasNext()) {
						String interfaceName = interfaces.next();
						allSystemInfoSet.add(interfaceName);
						JsonNode interfaceData = networkInterfaces.get(interfaceName);
						for (SystemsEnum item : SystemsEnum.values()) {
							if (interfaceData.has(item.getField())) {
								cacheValue.put(interfaceName + HaivisionConstant.HASH + item.getName(), interfaceData.get(item.getField()).asText());
							}
						}
					}
				}
				//populate system
				for (SystemsEnum system : SystemsEnum.values()){
					String systemName = system.getName();
					String value = getDefaultValueForNullData(cacheValue.get(systemName));
					if (system == SystemsEnum.CURRENTTIME) {
						stats.put(systemName, formatMillisecondsToDate(value));
					} else {
						stats.put(systemName, value);
					}
				}
				//populate network interface
				for (String name : allSystemInfoSet){
					for (NetWorkInterfaceBySystemInfoEnum item : NetWorkInterfaceBySystemInfoEnum.values()) {
						String nameProperty = name + HaivisionConstant.HASH + item.getName();
						String value = getDefaultValueForNullData(cacheValue.get(nameProperty));
						stats.put("NetworkInterface: " + uppercaseFirstCharacter(name) + HaivisionConstant.HASH + item.getName(), value);
					}
				}
			}
		} catch (Exception e) {
			throw new ResourceNotReachableException("Error when retrieving system info", e);
		}
	}

	/**
	 * Populates network information into the provided stats map by retrieving data from the network info endpoint.
	 *
	 * @param stats a map to store network information as key-value pairs
	 * @throws ResourceNotReachableException if the network information cannot be retrieved
	 */
	private void populateNetworkInfo(Map<String, String> stats) throws Exception{
		try {
			// Retrieve data
			JsonNode response = this.doGet(HaivisionCommand.GET_NETWORK_INFO, JsonNode.class);
			if (response != null && response.has(HaivisionConstant.NICS) && response.get(HaivisionConstant.NICS).isArray()) {
				allNetworkSet.clear();

				for (JsonNode item : response.get(HaivisionConstant.NICS)) {
					String group = item.get(HaivisionConstant.NAME).asText();
					allNetworkSet.add(group);
					for (NetworkEnum networkEnum : NetworkEnum.values()) {
						if(networkEnum.equals(NetworkEnum.SUBNET_MASK)){
							cacheValue.put(networkEnum.getName(), getDefaultValueForNullData(item.get(networkEnum.getField()).asText()));
						} else {
							cacheValue.put(networkEnum.getName(), response.get(networkEnum.getField()).asText());
						}
					}
				}
				// Populate network
				for (String name : allNetworkSet) {
					for (NetworkEnum item : NetworkEnum.values()) {
						String nameProperty = item.getName();
						String value = getDefaultValueForNullData(cacheValue.get(nameProperty));
						if (item == NetworkEnum.IP_FORWARD) {
							stats.put(HaivisionConstant.NETWORK + uppercaseFirstCharacter(name) + HaivisionConstant.HASH + item.getName(), value.toUpperCase());
						} else {
							stats.put(HaivisionConstant.NETWORK + uppercaseFirstCharacter(name) + HaivisionConstant.HASH + item.getName(), value);
						}
					}
				}
			}
		} catch (Exception e) {
			throw new ResourceNotReachableException("Error when retrieving network info", e);
		}
	}


	/**
	 * Populates system GPUs information into the provided stats map by retrieving data from the system GPUs info endpoint.
	 *
	 * @param stats a map to store system GPUs information as key-value pairs
	 * @throws ResourceNotReachableException if the system GPUs information cannot be retrieved
	 */
	private void populateSystemGPUInfo(Map<String, String> stats) throws Exception{
		try {
			// retrieve data license
			JsonNode response = this.doGet(HaivisionCommand.GET_SYSTEM_GPUS, JsonNode.class);
			if (response != null && response.has(HaivisionConstant.DATA)) {
				allSystemGPUSet.clear();
				JsonNode dataGPU = response.get(HaivisionConstant.DATA);
				if(!dataGPU.get("gpus").isEmpty()){
					for(JsonNode item : response.get("gpus")){
						String gpuName = item.get(HaivisionConstant.NAME).asText();
						String gpuIndex = item.get(HaivisionConstant.INDEX).asText();
						if (Integer.parseInt(gpuIndex) > 0) {
							allSystemGPUSet.add(gpuName + gpuIndex);
						} else {
							allSystemGPUSet.add(gpuName);
						}
						for(SystemGPU systemGPU: SystemGPU.values()){
							cacheValue.put(systemGPU.getName(), item.get(systemGPU.getField()).asText());
						}
						for (SystemLoad systemLoad: SystemLoad.values()){
							cacheValue.put(systemLoad.getName(), dataGPU.get(systemLoad.getField()).asText());
						}
					}
					// populate system gpu
					for (String name : allSystemGPUSet) {
						for (SystemGPU item : SystemGPU.values()) {
							String nameProperty = item.getName();
							String value = getDefaultValueForNullData(cacheValue.get(nameProperty));
							stats.put("System" + uppercaseFirstCharacter(name) + HaivisionConstant.HASH + item.getName(), value);
						}
						for(SystemLoad systemLoad: SystemLoad.values()){
							String nameProperty = systemLoad.getName();
							String value = getDefaultValueForNullData(cacheValue.get(nameProperty));
							stats.put("System" + uppercaseFirstCharacter(name) + HaivisionConstant.HASH + systemLoad.getName(), value);
						}
					}
				} else {
					for(SystemLoad systemLoad: SystemLoad.values()){
						cacheValue.put(systemLoad.getName(), getDefaultValueForNullData(dataGPU.get(systemLoad.getField()).asText()));
					}
					// populate system load
					for (SystemLoad itemLoad : SystemLoad.values()) {
						String nameProperty = itemLoad.getName();
						String value = getDefaultValueForNullData(cacheValue.get(nameProperty));
						stats.put("System" + HaivisionConstant.HASH + itemLoad.getName(), value);
					}
				}
			}
		} catch (Exception e) {
			throw new ResourceNotReachableException("Error when retrieving system GPUs info", e);
		}
	}


	/**
	 * Populates service information into the provided stats map by retrieving data from the service info endpoint.
	 *
	 * @param stats a map to store service information as key-value pairs
	 * @throws ResourceNotReachableException if the service information cannot be retrieved
	 */
	private void populateServiceInfo(Map<String, String> stats) throws Exception{
		try{
			// retrieve data RTSP
			JsonNode responseRTSP = this.doGet(HaivisionCommand.GET_RTSP, JsonNode.class);
			JsonNode responseWebserver = this.doGet(HaivisionCommand.GET_WEBSERVER, JsonNode.class);

			if(responseRTSP != null && responseRTSP.has("rtsp_port")){
				cacheValue.put("RtspServerPort", responseRTSP.get("rtsp_port").asText());
			}

			// populate rtsp
			String valueRTSP = getDefaultValueForNullData(cacheValue.get("RtspServerPort"));
			stats.put("Service" + HaivisionConstant.HASH + "RtspServerPort", valueRTSP);

			// retrieve web server
			if(responseWebserver.has(HaivisionConstant.DATA)){
				JsonNode dataWebServer = responseWebserver.get(HaivisionConstant.DATA).get("listeners");
				if(responseWebserver.has("interfaces")){
					cacheValue.put("WebServerInterfaces", dataWebServer.get("interfaces").asText());
				}
			}
			// populate web server
			String valueWebServer = getDefaultValueForNullData(cacheValue.get("WebServerInterfaces"));
			stats.put("Service" + HaivisionConstant.HASH + "WebServerInterfaces", valueWebServer);

		} catch (Exception e){
			throw new ResourceNotReachableException("Error when retrieving service info", e);
		}
	}


	/**
	 * Populates license information into the provided stats map by retrieving data from the license info endpoint.
	 *
	 * @param stats a map to store license information as key-value pairs
	 * @throws ResourceNotReachableException if the license information cannot be retrieved
	 */
	private void populateLicenseInfo(Map<String, String> stats) throws Exception{
		try {
			// retrieve data license
			JsonNode response = this.doGet(HaivisionCommand.GET_LICENSE_INFO, JsonNode.class);
			if (response != null) {
				for (LicenseEnum licenseEnum : LicenseEnum.values()) {
					if (response.has(licenseEnum.getField())) {
						cacheValue.put(licenseEnum.getName(), response.get(licenseEnum.getField()).asText());
					}
				}
				// populate data license
				for (LicenseEnum item : LicenseEnum.values()) {
					String name = item.getName();
					String value = getDefaultValueForNullData(cacheValue.get(name));
					if (item == LicenseEnum.CREATION_DATE || item == LicenseEnum.EXPIRATION) {
						stats.put("License" + HaivisionConstant.HASH + name, formatMillisecondsToDate(value));
					} else {
						stats.put("License" + HaivisionConstant.HASH + name, value);
					}
				}
			}
		} catch (Exception e) {
			throw new ResourceNotReachableException("Error when retrieving license info", e);
		}
	}


	/**
	 * Populates stream information into the provided stats map by retrieving data from the stream info endpoint.
	 *
	 * @param stats a map to store stream information as key-value pairs
	 * @throws ResourceNotReachableException if the stream information cannot be retrieved
	 */
	private void populateStreamsInfo(Map<String, String> stats) throws Exception{
		try {
			// retrieve data stream
			JsonNode response = this.doGet(HaivisionCommand.GET_ALL_STREAMS, JsonNode.class);
			if (response != null && response.has(HaivisionConstant.STREAM_LIST) && response.get(HaivisionConstant.STREAM_LIST).isArray()) {
				allStreamNameSet.clear();
				for (JsonNode item : response.get(HaivisionConstant.STREAM_LIST)) {
					String group = item.get(HaivisionConstant.NAME).asText();
					allStreamNameSet.add(group);
					for (StreamInfoEnum streamInfoEnum : StreamInfoEnum.values()) {
						if(item.has(streamInfoEnum.getField())){
							if(streamInfoEnum.equals(StreamInfoEnum.METADATA)){
								cacheValue.put(group + HaivisionConstant.HASH + streamInfoEnum.getName(), getDefaultValueForNullData(item.get(streamInfoEnum.getField()).toString()));
							} else {
								cacheValue.put(group + HaivisionConstant.HASH + streamInfoEnum.getName(), getDefaultValueForNullData(item.get(streamInfoEnum.getField()).asText()));
							}
						}
					}
				}
				// populate data stream
				for (String name : allStreamNameSet) {
					for (StreamInfoEnum item : StreamInfoEnum.values()) {
						String nameProperty = name + HaivisionConstant.HASH + item.getName();
						String value = getDefaultValueForNullData(cacheValue.get(nameProperty));
						if(item.equals(StreamInfoEnum.METADATA)){
							populateMetadata(stats, value, name);
						} else {
							stats.put("Stream: " + nameProperty, value);
						}
					}
				}
			}
		} catch (Exception e) {
			throw new ResourceNotReachableException("Error when retrieving stream info", e);
		}
	}


	/**
	 * Populates metadata information into the provided stats map by retrieving data from the metadata info endpoint.
	 *
	 * @param stats a map to store metadata information as key-value pairs
	 * @throws ResourceNotReachableException if the metadata information cannot be retrieved
	 */
	private void populateMetadata(Map<String, String> stats, String jsonString, String name) {
		if (jsonString.equalsIgnoreCase(HaivisionConstant.NONE)) {
			return;
		}
		try {
			JsonNode node = objectMapper.readTree(jsonString);
			if (!node.isArray()) {
				return;
			}
			JsonNode responseMetadata = this.doGet(HaivisionCommand.GET_METADATA, JsonNode.class);
			if (responseMetadata != null && responseMetadata.has(HaivisionConstant.METADATA_LIST) && responseMetadata.get(HaivisionConstant.METADATA_LIST).isArray()) {
				// Loop through the metadata UUIDs from the original object
				for (JsonNode metadataUuidNode : node) {
					String metadataUuid = metadataUuidNode.asText();
					for (JsonNode metadataItem : responseMetadata.get(HaivisionConstant.METADATA_LIST)) {
						String metadataID = metadataItem.get(HaivisionConstant.UUID).asText();
						if (metadataUuid.equals(metadataID)) {
							String metadataName = metadataItem.get(HaivisionConstant.NAME).asText();
							stats.put("Stream: " + name + HaivisionConstant.HASH + "Metadata", metadataName);
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error while populating the metadata Info", e);
		}
	}


	/**
	 * {@inheritDoc}
   */
	@Override
	public void controlProperty(ControllableProperty controllableProperty) throws Exception {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void controlProperties(List<ControllableProperty> controllableProperties) throws Exception {
		if (CollectionUtils.isEmpty(controllableProperties)) {
			throw new IllegalArgumentException("ControllableProperties can not be null or empty");
		}
		for (ControllableProperty p : controllableProperties) {
			try {
				controlProperty(p);
			} catch (Exception e) {
				logger.error(String.format("Error when control property %s", p.getProperty()), e);
			}
		}
	}

	/**
	 * Authenticates the user by sending a login request and retrieves the session cookie or ID.
	 *
	 * @return {@code true} if the session is successfully retrieved, {@code false} otherwise.
	 * @throws ResourceNotReachableException if an error occurs during the login process.
	 */
	private boolean getCookieSession() {
		try {
			HttpClient client = this.obtainHttpClient(true);
			HttpPost httpPost = new HttpPost(buildDeviceFullPath(HaivisionCommand.API_LOGIN));

			String jsonPayload = String.format(HaivisionConstant.AUTHENTICATION_PARAM, this.getLogin(), this.getPassword());
			httpPost.setEntity(new StringEntity(jsonPayload, ContentType.APPLICATION_JSON));

			HttpResponse response = client.execute(httpPost);
			int statusCode = response.getStatusLine().getStatusCode();

			if (statusCode == 403) {
				this.authenticationCookie = EntityUtils.toString(response.getEntity());
				return false;
			}
			if (statusCode == 204) {
				Header[] headers = response.getHeaders(HaivisionConstant.SET_COOKIE);
				if (headers.length == 0) {
					String responseBody = EntityUtils.toString(response.getEntity());
					String sessionId = extractSessionId(responseBody);
					if (sessionId != null) {
						this.authenticationCookie = "id=" + sessionId;
						System.out.println("Session ID retrieved: " + sessionId);
					} else {
						System.out.println("Session ID not found in the response body.");
						this.authenticationCookie = HaivisionConstant.EMPTY;
						return false;
					}
				} else {
					String authenticationCookies = Arrays.stream(headers)
							.map(header -> extractUUIDFromCookie(header.getValue()))
							.collect(Collectors.joining(""));

					this.authenticationCookie = authenticationCookies.isEmpty() ? HaivisionConstant.EMPTY : authenticationCookies;
					System.out.println("Authentication Cookies: " + this.authenticationCookie);
				}
			}
		} catch (Exception e) {
			this.authenticationCookie = HaivisionConstant.EMPTY;
			throw new ResourceNotReachableException("An error occurred when attempting to send a login request to the device", e);
		}
		return true;
	}

	/**
	 * check value is null or empty
	 *
	 * @param value input value
	 * @return value after checking
	 */
	private String getDefaultValueForNullData(String value) {
		return StringUtils.isNotNullOrEmpty(value) && !"null".equalsIgnoreCase(value) ? value : HaivisionConstant.NONE;
	}

	/**
	 * Extracts the session ID from the JSON response body.
	 * Parses the JSON string to retrieve the value of the "id" field.
	 * Returns the session ID if present, or {@code null} if not found or if parsing fails.
	 *
	 * @param responseBody JSON string containing the session information.
	 * @return The session ID, or {@code null} if not found or on error.
	 */
	private String extractSessionId(String responseBody) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode rootNode = mapper.readTree(responseBody);
			return rootNode.path("id").asText(null);
		} catch (Exception e) {
			logger.error("Error parsing session ID from response:", e);
			return null;
		}
	}


	/**
	 * Get cookie value
	 *
	 * @param cookie cookie value
	 */
	private String extractUUIDFromCookie(String cookie) {
		//Pattern pattern = Pattern.compile("id=([^;]+)");
		Pattern uuidPattern = Pattern.compile("[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}");
		Matcher matcher = uuidPattern.matcher(cookie);

		return matcher.find() ? matcher.group() : "";
	}


	/**
	 * Formats a string representing milliseconds into a date string in the format "MMM d, yyyy, h:mm a GMT".
	 *
	 * @param inputValue the string representing milliseconds.
	 * @return the formatted date string, or a default value if the input is "NONE" or invalid.
	 */
	private String formatMillisecondsToDate(String inputValue) {
		if (inputValue.equals(HaivisionConstant.NONE)) {
			return inputValue;
		}
		try {
			long milliseconds = Long.parseLong(inputValue);
			Date date = new Date(milliseconds);
			SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy, h:mm a");
			dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
			return dateFormat.format(date);
		} catch (Exception e) {
			logger.error("Error when convert date data", e);
			return HaivisionConstant.NONE;
		}
	}

	/**
	 * @param path url of the request
	 * @return String full path of the device
	 */
	private String buildDeviceFullPath(String path) {
		Objects.requireNonNull(path);
		return HaivisionConstant.HTTPS
				+ getHost()
				+ HaivisionConstant.COLON
				+ getPort()
				+ path;
	}

	/**
	 * Send POST request to retrieve cookie session
	 *
	 * @return boolean
	 */
	private boolean isValidCookie() throws Exception {
		boolean isAuthenticate = false;
		try {
			isAuthenticate = getCookieSession();

		} catch (ResourceNotReachableException e) {
			throw new ResourceNotReachableException("Failed to send login request to device", e);
		} catch (Exception e) {
			logger.error("Failed to retrieve cookie session", e);
		}
		return isAuthenticate;
	}

	/**
	 * capitalize the first character of the string
	 *
	 * @param input input string
	 * @return string after fix
	 */
	private String uppercaseFirstCharacter(String input) {
		char firstChar = input.charAt(0);
		return Character.toUpperCase(firstChar) + input.substring(1);
	}

	/**
	 * Deletes the current authentication cookie session from the server.
	 * This method sends a request to the server to delete the current session ID.
	 * After deleting the session, the local authentication cookie is reset to an empty value.
	 */
	private void deleteCookieSession() {
		try {
			HttpClient client = this.obtainHttpClient(true);
			HttpGet httpGet = new HttpGet(buildDeviceFullPath(HaivisionCommand.API_LOGOUT));
			httpGet.setHeader(HaivisionConstant.COOKIE, "DisplayUnsavedWarning=true; Path=/; Secure;");
			httpGet.setHeader(HaivisionConstant.COOKIE, String.format("id=%s; Path=/; Secure; HttpOnly;", this.authenticationCookie));
			HttpResponse response = client.execute(httpGet);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == 204) {
				logger.info("Session deleted successfully. No content returned.");
				logger.info("Delete session ID " + this.authenticationCookie);
			} else {
				logger.info("Unexpected status code while deleting session: " + statusCode);
			}
		} catch (Exception e) {
			logger.error("Error while deleting session ID " + this.authenticationCookie, e);
		} finally {
			// Clear the authentication cookie
			this.authenticationCookie = HaivisionConstant.EMPTY;
		}
}}
