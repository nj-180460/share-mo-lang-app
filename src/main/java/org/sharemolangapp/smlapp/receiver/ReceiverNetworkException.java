package org.sharemolangapp.smlapp.receiver;



public class ReceiverNetworkException extends Exception{

	
	private final String message; 
	
	public ReceiverNetworkException(String message) {
		super(message);
		this.message = message;
	}
	
	
	private String errMessage() {
		return message + ": " + getCause();
	}
	
	
	@Override
	public String toString() {
		return errMessage();
	}
	
}
