package com.hygenics.crawler;

import java.util.Calendar;

/**
 * Generates the Hash Linkage for Individuals across the database.
 * This code does not link people across the databases. Instead
 * 
 * @author aevans
 */
public class GenandSetHash {
	
	//hash obtained
	private getDAOTemplate template;
	private String hash;
	
	public GenandSetHash(){
		
	}
	
	
	

	public String getHash() {
		return hash;
	}




	public void setHash(String hash) {
		this.hash = hash;
	}




	public getDAOTemplate getTemplate() {
		return template;
	}



	public void setTemplate(getDAOTemplate template) {
		this.template = template;
	}
	
	/**
	 * Appends the unique id to the hash
	 */
	private void appendID(){
		
		int id=template.getCount("data.hashes");
		id++;
		hash=Integer.toString(id)+hash;
	}

	
	private void addHash(){
		String sql="INSERT INTO data.hashes (hash) VALUES("+hash+")";
		template.execute(sql);
	}
	
	
	public String run(){
		
		if(hash != null)
		{
			appendID();
			addHash();
		}else{
			try{
				throw new NullPointerException("No Hash Specified");
			}catch(NullPointerException e){
				e.printStackTrace();
			}
		}
		
		return hash;
	}

}
