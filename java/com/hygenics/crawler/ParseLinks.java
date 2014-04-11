package com.hygenics.crawler;


import java.util.concurrent.*;
import java.util.regex.Pattern;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import com.hygenics.jdbc.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;


/**
 * This Step's sole responsibility is to parse links using multithreading
 * 
 * Processor results in case of need
 * Fork Join Pool: 70% max (20% normal) -- max speed
 * Custom Thread Pool: 66% max (20% normal)
 * Executor Service: 50% max (20% normal) -- min speed
 */
public class ParseLinks {
	private static final Logger log=LoggerFactory.getLogger(MainApp.class);

	private String idcolumn="offenderhash";
	private String cannotcontain;
	private String mustcontain;
	
	//boolean to turn off hash checking
	private boolean linkcheck=true;
	
	private int termtime=500;
	private int numprocs=2;
	
	private String extracondition;
	private String replacementPattern;
	private String token;
	
	private boolean unique=false;
	
	//collection for removing duplicates
	private Collection<String> linkset=new HashSet<String>();
	private int id=0;
	private boolean genhash=false;
	private String table;
	private String select;
	private int commit_size=100000;
	private int pullsize=100;
	private String linkRegex;
	private String linkFlags;
	private getDAOTemplate template;
	private Map<String,String> pages=new HashMap<String,String>();
	private Map<String,String> links=new HashMap<String,String>();
	private String column;
	private String pullid;
	private String user;
	private String password;
	private jdbcconn jdbc=null;
	private String url;
	private int offset=0;

	
	
	public ParseLinks()
	{
		
	}


	
	
	public boolean isUnique() {
		return unique;
	}




	public void setUnique(boolean unique) {
		this.unique = unique;
	}




	public String getIdcolumn() {
		return idcolumn;
	}



	public void setIdcolumn(String idcolumn) {
		this.idcolumn = idcolumn;
	}



	public String getCannotcontain() {
		return cannotcontain;
	}



	public void setCannotcontain(String cannotcontain) {
		this.cannotcontain = cannotcontain;
	}



	public String getMustcontain() {
		return mustcontain;
	}



	public void setMustcontain(String mustcontain) {
		this.mustcontain = mustcontain;
	}



	public int getPullsize() {
		return pullsize;
	}




	public void setPullsize(int pullsize) {
		this.pullsize = pullsize;
	}




	public String getExtracondition() {
		return extracondition;
	}



	public void setExtracondition(String extracondition) {
		this.extracondition = extracondition;
	}



	/**
	 * Returns the replacement pattern to be used on a split link
	 * @return
	 */
	public String getReplacementPattern() {
		return replacementPattern;
	}


	/**
	 * Gets the replacement Pattern to be used on a split link
	 * @param replacementPattern
	 */
	public void setReplacementPattern(String replacementPattern) {
		this.replacementPattern = replacementPattern;
	}



	/**
	 * Gets the number of processes to be set when splitting a series of links	
	 * 
	 * @return
	 */
	public int getNumprocs() {
		return numprocs;
	}

	

	/**
	 * Gets the termination time in ms to wait for a link splitting process to finish. Default is 500 milliseconds.	 
	 * @return
	 */
	public int getTermtime() {
		return termtime;
	}

	/**
	 * Sets the termination time in ms to wait for a link splitting process to finish. Default is 500 milliseconds.	 
	 * @return
	 */
	public void setTermtime(int termtime) {
		this.termtime = termtime;
	}


	/**
	 * Sets the number of processes to be set when splitting a series of links	
	 * 
	 * @return
	 */
	public void setNumprocs(int numprocs) {
		this.numprocs = numprocs;
	}




	/**
	 * Get the token to split the link column value from the database on. If this is null, then no splitting will be done.
	 * If no token is specified, no attempt to split will be made.
	 * 
	 * @return
	 */
	public String getToken() {
		return token;
	}



	/**
	 * Set the token to split the link column value from the database on. If this is null, then no splitting will be done.
	 * If no token is specified, no attempt to split will be made.
	 * 
	 * @return
	 */
	public void setToken(String token) {
		this.token = token;
	}


	
	
	public boolean isGenhash() {
		return genhash;
	}



	public void setGenhash(boolean genhash) {
		this.genhash = genhash;
	}



	public String getPullid() {
		return pullid;
	}



	public void setPullid(String pullid) {
		this.pullid = pullid;
	}



	public boolean isLinkcheck() {
		return linkcheck;
	}



	public void setLinkcheck(boolean linkcheck) {
		this.linkcheck = linkcheck;
	}



	public int getOffset() {
		return offset;
	}







	public void setOffset(int offset) {
		this.offset = offset;
	}







	public String getTable() {
		return table;
	}







	public void setTable(String table) {
		this.table = table;
	}







	public String getUser() {
		return user;
	}







	public void setUser(String user) {
		this.user = user;
	}







	public String getPassword() {
		return password;
	}







	public void setPassword(String password) {
		this.password = password;
	}







	public String getUrl() {
		return url;
	}







	public void setUrl(String url) {
		this.url = url;
	}







	public String getColumn() {
		return column;
	}






	public void setColumn(String column) {
		this.column = column;
	}






	public String getpullid() {
		return pullid;
	}






	public void setpullid(String pullid) {
		this.pullid = pullid;
	}



	public String getSelect() {
		return select;
	}






	public void setSelect(String select) {
		this.select = select;
	}






	public String getLinkFlags() {
		return linkFlags;
	}




	public void setLinkFlags(String linkflags) {
		this.linkFlags = linkflags;
	}




	public int getCommit_size() {
		return commit_size;
	}




	public void setCommit_size(int commit_size) {
		this.commit_size = commit_size;
	}




	public String getLinkRegex() {
		return linkRegex;
	}



    @Required
	public void setLinkRegex(String linkRegex) {
		this.linkRegex = linkRegex;
	}


	public getDAOTemplate getTemplate() {
		return template;
	}



	/**
	 * Set the DAO Template
	 * @return
	 */
	@Required
	@Resource(name="getDAOTemplate")
	public void setTemplate(getDAOTemplate template) {
		this.template = template;
	}


	/**
	 * Generates a Unique hash specific to the pull
	 * 
	 * @param id
	 * @return
	 */
	private String genHash(String id)
	{
		
		long h=0;
		String d=id+Long.toString(Calendar.getInstance().getTimeInMillis());
		
		for(int i=0;i<d.length();i++)
		{
			h+=(d.charAt(i)*(10*i));
		}
		
		return Long.toString(h);
	}


	public void getFromDB(String select,String column, String idcolumn)
	{	
		pages=template.getData(select,idcolumn,column,unique);
		
		//html pages can be quite large so its good to keep gc on its toes
		Runtime.getRuntime().gc();
	}
	
	public void sendtoDB(Map<String,String> links)
	{	
		String query="INSERT INTO "+table+"("+idcolumn+",link) VALUES(";
		Set<String> keys=links.keySet();
		String out=null;
		
		if(password != null & url != null)
		{
			if(jdbc == null)
			{	
				
				jdbc=new jdbcconn();
				
				jdbc.connectAgain(url, user, password);
			}
			
			if(jdbc != null)
			{
				for(String k: keys)
				{
					id++;
					if(genhash==true)
					{
						out=query+"'"+genHash(Integer.toString(id))+"','"+k+"')";
						
						if(k != null)
							jdbc.addtoBatch(out);
					}
					else
					{
						if(token ==null)
						{
							out=query+"'"+links.get(k).replaceAll("UQUQ\\d+UQUQ","")+"','"+k+"')";
							
							if(k != null)
								jdbc.addtoBatch(out);
						}
						else
						{
							for(String linkstr : k.split(token))
							{
								out=query+"'"+links.get(k).replaceAll("UQUQ\\d+UQUQ","")+"','"+linkstr+"')";
										
								if(linkstr != null)
									jdbc.addtoBatch(out);
							}
						}
					}
					
				}
			jdbc.executeBatch();
			jdbc.clearBatch();
			}
		}
	
	}
	
	/**
	 * Check and create tables where necessary
	 */
	public void checkTable()
	{
		String[] tablearr=table.split("\\.");
		
		log.info("Checking for Schema "+tablearr[0]+" and Table "+tablearr[1]);
		String sql=null;
		
		if(this.template.checkSchema(tablearr[0])==false)
		{
			log.info("Creating Schema");
			sql="CREATE SCHEMA "+tablearr[0];
			this.template.execute(sql);
		}
		
		if(this.template.checkTable(table, tablearr[0])==false)
		{
			log.info("Creating Table");
			sql="CREATE TABLE "+table+"(id SERIAL PRIMARY KEY NOT NULL, link text, "+idcolumn+" text)";
			this.template.execute(sql);
		}
	}
	
	/**
	 * The driving method any future main should call this
	 */
	public void parse()
	{
		int id=0;
		//check table
		checkTable();
		
		int j=1;
		
		//perform parsing
		if(linkRegex != null)
		{
			log.info("Starting Parse @ "+Calendar.getInstance().getTime().toString());
			String condition=" WHERE "+pullid+" >= "+Integer.toString(offset)+" AND "+pullid+" <= "+Integer.toString((pullsize+offset));
			
			if(extracondition != null)
			{
				condition+=" "+extracondition.trim();
			}
			
			log.info((select.trim()+condition).trim());
			getFromDB((select.trim()+condition).trim(),column,idcolumn);
			Set<String> keys;
			ArrayList<ForkJoinTask<String[]>> callables=new ArrayList<ForkJoinTask<String[]>>(); 
			log.info("FOUND "+pages.size()+" PAGES");
			
			if(linkFlags != null)
			{
				linkRegex="(?"+linkFlags+")"+linkRegex;
			}
		
			Pattern p=Pattern.compile(linkRegex);
			long t=Calendar.getInstance().getTimeInMillis();
			while(pages.size()>0)
			{
				id++;
				
				//attempt to ensure all garbage is collected. this is a memory intensive app so paranoia is king
				Runtime.getRuntime().gc();
				System.gc();
			  
				//implement multithreaded regex parsing
				String[] kv=null;
				keys=pages.keySet();

				ForkJoinPool fjp=new ForkJoinPool((2*Runtime.getRuntime().availableProcessors()));
				boolean proceed=true;
				
				for(String k: keys)
				{
					if(pages.get(k) != null){
						
						if(mustcontain != null)
						{
							if(pages.get(k).contains(mustcontain)==false)
							{
							proceed=false;
							}
						}
						
						if(cannotcontain != null)  
						{		
							if(pages.get(k).contains(cannotcontain)==true)
							{
								proceed=false;
							}
						}	
						
						if(proceed){
							
							if(genhash==false){
								callables.add(fjp.submit((new ParseLinkReg(p,pages.get(k),k))));
							}
							else{
								callables.add(fjp.submit((new ParseLinkReg(p,pages.get(k),genHash(Integer.toString(id))))));
							}
						}else{
							proceed=true;
						}
					}
				}
			
				if(callables.size()>0)
				{
					try{
						fjp.awaitTermination(termtime, TimeUnit.MILLISECONDS);
						fjp.shutdown();
						
						while(fjp.isTerminated()==false)
						{
							try{
								Thread.sleep(5);
							}catch(InterruptedException e)
							{
								e.printStackTrace();
							}
						}
						
				
						for(ForkJoinTask<String[]> f:callables)
						{
							kv=f.get();
					
							if(kv != null)
							{
								
								if(kv[0] != null & kv[1]!= null)
								{
									if(linkcheck)
									{
										//for all pages the decision to work on small batches was made to increase speed
										if(linkset.contains(kv[0])==false)
										{
											linkset.add(kv[1]);
											links.put(kv[1], kv[0]);
										}
									}
									else
									{
										links.put(kv[1], kv[0]);
									}
								}
							}
							kv=null;
							f.cancel(true);
						}
						callables.clear();

					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ExecutionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
			
			
			//send to db 
			if(links.size()>0)
			{
			  sendtoDB(links);
			 
			}
  
			//clean out lists
			  pages.clear();
			  links.clear();
			  
			 //attempt to ensure all garbage is collected. this is a memory intensive app so paranoia is king
			  Runtime.getRuntime().gc();
			  System.gc();
			  
			  
			  condition=" WHERE "+pullid+" > "+Integer.toString(offset+(pullsize*j))+" AND "+pullid+" < "+Integer.toString(offset+(pullsize*j)+pullsize);
			  
			  if(extracondition != null)
			  {
				condition+=" "+extracondition.trim();
			  }
			  
			  log.info(condition);
			  getFromDB((select.trim()+condition).trim(),column,idcolumn);	
			 j++;
			
		
	 }
			log.info(Long.toString(Calendar.getInstance().getTimeInMillis()-t));
	}
	else
	{
		try{
			
			throw new NullPointerException("No Regex Provided");
			
		}catch(NullPointerException e)
		{
			e.printStackTrace();
		}
	}
			
		
	}
	
}
