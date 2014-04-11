package com.hygenics.exceptions;

public class UserInputNotObtainedException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 123823246609637914L;

	public UserInputNotObtainedException()
	{
		super("User Input Could not Be Found!");
	}
	
	public UserInputNotObtainedException(String e)
	{
		super("User Input Could not Be Found! \n"+e);
	}
	
}
