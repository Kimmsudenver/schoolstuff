package com.hygenics.crawlerclient;
/**
 * 
 * NOT POJO: For Getting the Message from the Server
 * 
 * 
 * @author aevans
 */

public class MessageObject {
	

	private String message;
	private boolean ALLOW_ACCESS=false;
	
	public MessageObject()
	{
		
	}

	public boolean isALLOW_ACCESS() {
		return ALLOW_ACCESS;
	}



	public void setALLOW_ACCESS(boolean aLLOW_ACCESS) {
		ALLOW_ACCESS = aLLOW_ACCESS;
	}
	
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public void resetMessage()
	{
		message=null;
	}
	

}
