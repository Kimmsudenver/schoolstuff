package com.hygenics.gdocs;

import java.util.ArrayList;
import java.util.HashMap;

public class Spreadsheet {
	
	private String url;
	private String sheet_name;
	private HashMap<String, ArrayList<String>> rowmap;
	private String password;
	private String user;
	
	
	public Spreadsheet()
	{
		
	}

	
	
	public String getSheet_name() {
		return sheet_name;
	}



	public void setSheet_name(String sheet_name) {
		this.sheet_name = sheet_name;
	}



	public String getUrl() {
		return url;
	}


	public void setUrl(String url) {
		this.url = url;
	}


	public HashMap<String, ArrayList<String>> getRowmap() {
		return rowmap;
	}


	public void setRowmap(HashMap<String, ArrayList<String>> rowmap) {
		this.rowmap = rowmap;
	}


	public String getPassword() {
		return password;
	}


	public void setPassword(String password) {
		this.password = password;
	}


	public String getUser() {
		return user;
	}


	public void setUser(String user) {
		this.user = user;
	}
	
	
}
