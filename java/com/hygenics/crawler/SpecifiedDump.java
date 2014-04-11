package com.hygenics.crawler;

import java.util.Calendar;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import javax.annotation.*;

import com.hygenics.crawler.CommandDump.ToFile;
import com.hygenics.exceptions.SQLMalformedException;
import com.hygenics.jdbc.jdbcconn;

/**
 * Takes in a series of attributes and dumps them based on their specifications.
 * This is useful for maintaining common schemas.
 * 
 * Input is a Map<table<Map<attribute type/table name, attr/table name>>
 * 
 * Types of keys are 
 * 	  the name of the table as a string combined with the filename as table | filename
 *    attr - common attr as String
 * 
 * Specifications for attributes
 *    distinct-for concacting distinct part of query
 *    not0-for specifiying that the length must be greater than 0 in the WHERE clause
 *    group-for grouping the attribute
 *    not null-for specifying that the attr cannot be null
 * 
 * @author asevans
 *
 */
public class SpecifiedDump {
	
	private Logger log=LoggerFactory.getLogger(MainApp.class);
	private String delimiter="|";
	private String url;
	private String user;
	private String pass;
	private Map<String,Map<String,String>> tables;
	private getDAOTemplate template;
	private String extracondition;
	
	public SpecifiedDump(){
		
	}
	
	
	
	public String getExtracondition() {
		return extracondition;
	}



	public void setExtracondition(String extracondition) {
		this.extracondition = extracondition;
	}



	@Required
	@Resource(name="getDAOTemplate")
	public void setGetDAOTemplate(getDAOTemplate template){
		this.template=template;
	}
	

	public String getDelimiter() {
		return delimiter;
	}



	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}



	public String getUrl() {
		return url;
	}



	public void setUrl(String url) {
		this.url = url;
	}



	public String getUser() {
		return user;
	}



	public void setUser(String user) {
		this.user = user;
	}



	public String getPass() {
		return pass;
	}



	public void setPass(String pass) {
		this.pass = pass;
	}



	
	
	public Map<String, Map<String, String>> getTables() {
		return tables;
	}


	/**
	 * Set the tables and attributes
	 * @param tables
	 */
	public void setTables(Map<String, Map<String, String>> tables) {
		this.tables = tables;
	}





	protected class ToFile extends RecursiveAction
	{
		private final String sql;
		private final String file;
		
		public ToFile(final String sql,final String file)
		{
			this.sql=sql;
			this.file=file;
		}
		
		public void compute()
		{
			jdbcconn conn=new jdbcconn(url,user,pass);
			conn.CopyOut(sql.trim(),(file.trim()+Calendar.getInstance().getTime().toString().trim().replaceAll(":|\\s", "")+".txt").trim());
		}
	}


	/**
	 *Runs the Dump
	 */
	public void run()
	{
		
		ForkJoinPool fjp=new ForkJoinPool(Runtime.getRuntime().availableProcessors());
		
		for(String tf:tables.keySet()){
			String[] split=tf.split("\\|");
			log.info("Dumping for "+split[0]);
			String schema=null;
			try{
				schema=split[0].split("\\.")[0];
			}catch(IndexOutOfBoundsException e){
				try{
					throw new SQLMalformedException("FATAL ERROR: Table name "+split[0]+" malformed");
				}
				catch(SQLMalformedException e2){
					e2.printStackTrace();
					System.exit(-1);
				}
			}
			
			log.info("Checking  table: "+split[0]+ "&& schema: "+schema);
			if(template.checkTable(split[0], schema)){
				Set<String> keys=tables.get(tf).keySet();
				String sql;
				String select="SELECT ";
				String distinct=null;
				String attrs=null;
				String where=null;
				String group=null;
				String order=null;
			
			
				/**
				 *    SET THE ATTRIBUTES WHICH CAN BE SPECIFIED WITH
				 *    distinct-for concacting distinct part of query
				 *    not0-for specifiying that the length must be greater than 0 in the WHERE clause
				 *    group-for grouping the attribute
				 *    not null-for specifying that the attr cannot be null
				 *    orderby-for specifying our one order attr
				 */
				for(String k:keys)
				{
					if(k.toLowerCase().contains("distinct")){
						distinct=(distinct==null)?"distinct on("+tables.get(tf).get(k).replaceAll("\\sas.*",""):distinct+","+tables.get(tf).get(k).replaceAll("\\sas.*","");
					}
				
				
					if(k.toLowerCase().contains("group")){
						group=(group==null)?"GROUP BY "+tables.get(tf).get(k).replaceAll("\\sas.*",""):group+","+tables.get(tf).get(k).replaceAll("\\sas.*","");
					}
				
					if(k.toLowerCase().contains("not0")){
						if(k.contains("not0OR")){
							where=(where==null)?"WHERE length("+tables.get(tf).get(k).replaceAll("\\sas.*","")+") >0 ":where+"OR length("+tables.get(tf).get(k).replaceAll("\\sas.*","")+")";
						}
						else
						{
							where=(where==null)?"WHERE length("+tables.get(tf).get(k).replaceAll("\\sas.*","")+") >0 ":where+"AND length("+tables.get(tf).get(k).replaceAll("\\sas.*","")+")";
						}
					}
				
					if(k.toLowerCase().contains("notnull")){
						if(k.toLowerCase().contains("notnullor")){
							where=(where==null)?"WHERE "+tables.get(tf).get(k).replaceAll("\\sas.*","")+" IS NOT NULL":where+" OR "+tables.get(tf).get(k).replaceAll("\\sas.*","")+" IS NOT NULL";
						}
						else{
							where=(where==null)?"WHERE "+tables.get(tf).get(k).replaceAll("\\sas.*","")+" IS NOT NULL":where+" AND "+tables.get(tf).get(k).replaceAll("\\sas.*","")+" IS NOT NULL";
						}
					}
				
					if(k.toLowerCase().contains("order")){
						if(k.toLowerCase().contains("orderdesc")){
							order=(order==null)?"ORDER BY "+tables.get(tf).get(k).replaceAll("\\sas.*","")+" ASC":order;
						}
						else{
							order=(order==null)?"ORDER BY "+tables.get(tf).get(k).replaceAll("\\sas.*","")+" DESC":order;
						}
					}
				
					if(k.toLowerCase().contains("attr")){
						attrs=(attrs==null)?tables.get(tf).get(k):attrs+","+tables.get(tf).get(k);
					}
				}
			
				select=(distinct==null)?select:select.trim()+" "+distinct.trim()+")";
				select+=" "+attrs.trim();
				select+=" FROM "+split[0].trim();
				select=(where==null)?select:select.trim()+" "+where.trim();
				select=(group==null)?select:select.trim()+" "+group.trim();
				select=(order==null)?select:select.trim()+" "+order.trim();
				
				if(extracondition != null){
					select+=(select.contains(" WHERE ")==true)?" AND"+extracondition:" WHERE "+extracondition;
				}
				
				select=select.trim();
			
				log.info("Dump Select Command: "+select);
			
				sql="COPY  ("+select+") TO STDOUT DELIMITER '"+delimiter.trim()+"'  CSV HEADER";
				fjp.execute(new ToFile(sql,split[1].trim()));
			
				select="SELECT ";
				distinct=null;
				attrs=null;
				where=null;
				group=null;
				order=null;
			}
			else{
				try{
					throw new SQLMalformedException("WARNING: Table "+split[0]+" is missing");
				}catch(SQLMalformedException e){
					e.printStackTrace();
				}
			}
		}
		
		try {
			fjp.awaitTermination(60000, TimeUnit.MILLISECONDS);
			fjp.shutdown();
		} catch (InterruptedException e) {
				// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	
}
