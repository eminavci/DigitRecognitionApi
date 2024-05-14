package com.mldm.rest.controllers;

import com.mldm.core.exception.CoreException;
import com.mldm.rest.common.ApiException;
import com.mldm.rest.common.ApiResponse;
import com.mldm.rest.common.ApiResponse.STATUS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
@EnableWebMvc
public class ExceptionHandlerController {

	Logger logger = LoggerFactory.getLogger(ExceptionHandlerController.class);

	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<ApiResponse> handleException1(HttpServletRequest req, RuntimeException exception){
		logger.error("Runtime Error : ", exception);
		return new ResponseEntity<ApiResponse>(
									new ApiResponse(STATUS.ERROR, new ApiException(exception).getAsResponse()), 
									HttpStatus.INTERNAL_SERVER_ERROR); 
	}
	
	@ExceptionHandler({Exception.class, CoreException.class, ApiException.class})
	public ResponseEntity<ApiResponse> handleException2(HttpServletRequest req, Exception exception){
		logger.error("Error : ", exception);
		ApiException ae;
		if(exception instanceof ApiException)
			ae = (ApiException) exception;
		else if(exception instanceof CoreException)
			ae = new ApiException(((CoreException)exception).getMsg(), ((CoreException)exception).getMsgType());
		else
			ae = new ApiException(exception);
		if(ae.getErrMsg() == null || ae.getErrMsg().length() < 1)
			ae.setErrMsg(ae.getMessage());
		return new ResponseEntity<ApiResponse>(
									new ApiResponse(STATUS.ERROR, ae.getAsResponse()), 
									HttpStatus.INTERNAL_SERVER_ERROR); 
	}
	

}
