package com.videotron.bsi.service.b.constants.logging;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LoggingConstants {

	public static final String MAIN_OPERATION = "mainOperation";
	public static final String MAIN_APPLICATION = "mainApplication";
	public static final String MAIN_CLIENT = "mainClient";
	public static final String MAIN_DURATION = "mainDuration";
	public static final String MAIN_RESPONSE_CODE = "mainResponseCode";
	
	public static final String EXECUTION_UUID = "executionUUID"; 
	
	public static final String X_VL_REMOTE_USER = "x-vl-remote-user";
	
	public static final String PAYLOAD_REQUEST = "requestPayload";
	public static final String PAYLOAD_RESPONSE = "responsePayload";
	public static final String PAYLOAD = "payload";
	
	public static final String START = "Start";
	public static final String END = "End";
	
	public static final String BACKEND_SYSTEM = "backendSystem";
	public static final String BACKEND_PROCESS = "backendProcess";
	public static final String BACKEND_OPERATION = "backendOperation";
	public static final String BACKEND_RESPONSE_CODE = "backendResponseCode";
	public static final String BACKEND_ERROR_CODE = "backendErrorCode";
	public static final String BACKEND_ERROR_MESSAGE = "backendErrorMessage";
	public static final String BACKEND_DURATION = "backendDuration";

	
	
}