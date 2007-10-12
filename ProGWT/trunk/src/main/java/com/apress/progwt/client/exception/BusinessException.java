package com.apress.progwt.client.exception;

public class BusinessException extends Exception {

	public BusinessException(String message) {
		super(message);
	}

	public BusinessException(Exception e) {
		super(e);
	}

}
