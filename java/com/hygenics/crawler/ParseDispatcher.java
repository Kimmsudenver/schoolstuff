package com.hygenics.crawler;

import mjson.Json;

import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;


import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hygenics.jdbc.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

import regex2.Regex;

import javax.annotation.*;

/**
 * Sets up and parses for single and multiple regex patterns
 * 
 * This class creates a dispatcher that may be useful when used across
 * multiple machines as well. Due to the limited systems we have. The dispatcher
 * forks out to different processes, don't think Reactor pattern here.
 * 
 * Overall ~22.4 seconds for ~12000 posts, parsing with ~1700 regex operations, 
 * ~1000 pulls. ~500 milliseconds for the ~1700 regex operations @ 99% cpu.
 * 
 * Don't do more than 10-15 splits. I swamped the system at 20-30 and it hates me for
 * a while. (Super user death error and now no response for a bit).
 * 
 * You can specify a primary key in this step if desired. The primary key is usesful in pre-processing data
 *for duplicate and other matching before being sent out (statistically and comparatively removing duplicates 
 *as opposed to grouping and comparing which is an improvement over the existing tool since these sources
 *are incredibly unclean). 
 * 
 * @author aevans
 */


public class ParseDispatcher{
	
	private boolean cascade=true;
	private boolean truncate=true;
	
	private boolean test=false;
	private String key;
	private String fkey;
	private String fkeyref;
	
	private String extracondition;
	private String notnull;
	private int pullsize=100;
	private int loops=0;
	private int waitloops=1;
	
	private int SPLITSIZE=100;
	private final Logger log=LoggerFactory.getLogger(MainApp.class);

	private int termtime=500;
	private int qnum=5;
	private jdbcconn jdbc;
	private int sqlnum=1;
	
	private String schema;
	private int procnum=2;
	
	private String cannotcontain;
	private String mustcontain;
	private String replacementPattern;
	private String pullid;
	private String select;
	private String column;
	private String post;
	
	private int commit_size=100;
	
	private boolean getHash=true;
	
	private String imageRegex;
	private String imageprefix;
	private String imagesuffix;
	
	private ArrayList<String> pages=new ArrayList<String>();
	
	//these shouldn't be too large
	private Map<String,String> singlepats=new HashMap<String, String>();
	private Map<String,Map<String,String>> multipats=new HashMap<String,Map<String,String>>();
	private Map<String,Map<String,String>> loopedpats=new HashMap<String,Map<String,String>>();
	
	private getDAOTemplate template;
	private int offset;
	
	
	public ParseDispatcher()
	{
		
	}
	
	
	

	public boolean isCascade() {
		return cascade;
	}




	public void setCascade(boolean cascade) {
		this.cascade = cascade;
	}




	public boolean isTruncate() {
		return truncate;
	}




	public void setTruncate(boolean truncate) {
		this.truncate = truncate;
	}




	public boolean isGetHash() {
		return getHash;
	}




	public void setGetHash(boolean getHash) {
		this.getHash = getHash;
	}




	public boolean isTest() {
		return test;
	}


	public void setTest(boolean test) {
		this.test = test;
	}




	private class SplitPost extends RecursiveAction
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 5942536165467154211L;
		private final getDAOTemplate template;
		private ArrayList<String> json;
		
		public SplitPost(final getDAOTemplate template,final ArrayList<String> json){
			this.json=json;
			this.template=template;
		}
		
		@Override
		protected void compute() {
			// TODO Auto-generated method stub
			String val=null;
			String table=null;
			String sql=null;
			ArrayList<String> outlist=new ArrayList<String>();
			int numvals=0;
			
			if(this.json != null){

				for(String str: this.json)
				{
					Map<String, Json> jmap=Json.read(str).asJsonMap();
					
					if(table==null)
					{
						//CASE IS THAT no table has been added
						
						Set<String>keys=jmap.keySet();
						Iterator<String>it=keys.iterator();
						String fname=null;
						
						//create statement (a repeat due to comparison against a null condition [just to be careful null is used])
						table=jmap.get("table").asString().trim();
						sql="INSERT INTO "+table+" (";
						val="VALUES(";
						
		
						//hopefully a smaller loop saves time
						for(int vals=0;vals<keys.size();vals++)
						{
							fname=it.next();
							
							if(fname.compareTo("table") != 0)
							{
								if(vals>0)
								{
									val+=",";
									sql+=",";
								}
							
								val+="?";
								sql+=fname;
							}
						}
						
						numvals=keys.size();
						sql+=")";
						val+=")";
						
						sql=sql+" "+val;
						
						if(str.trim().compareTo("NO DATA")!= 0){
							if(notnull != null)
							{
								if(jmap.get(notnull.trim()).asString().trim().length()>0)
								{
									outlist.add(str);
								}
							}
							else
							{
								outlist.add(str);
							}
						}
					}
					else if(table.compareTo(jmap.get("table").asString().trim())!=0 | jmap.size()>numvals |jmap.size()<numvals)
					{
						//case is that table is different or the number of values differs which would cause sql to throw an error
						Set<String>keys=jmap.keySet();
						Iterator<String>it=keys.iterator();
						String fname=null;
						
						//send current data if the waiting list is greater than 0
						if(outlist.size()>0)
						{
							this.template.postJsonData(sql, outlist);
							outlist=new ArrayList<String>();
						}
						
						//reset information
						table=jmap.get("table").asString().trim();
						sql="INSERT INTO "+table+" (";
						val="VALUES(";
						
						//hopefully a smaller loop saves time
						for(int vals=0;vals<keys.size();vals++)
						{
							fname=it.next();
							
							if(fname.compareTo("table")!=0)
							{
								if(vals>0)
								{
									val+=",";
									sql+=",";
								}
							
								val+="?";
								sql+=fname;
							}
						}
						
						sql+=")";
						val+=")";
						
						numvals=keys.size();
						
						sql=sql+" "+val;
						if(str.trim().compareTo("NO DATA")!= 0){
							
							if(notnull != null)
							{
								if(jmap.get(notnull.trim()).asString().trim().length()>0)
								{
									outlist.add(str);
								}
							}
							else
							{
								outlist.add(str);
							}
						}
					}
					else
					{
						//case is that no table is different and the number of values does not differ from the previous ammount
						
						if(str.trim().compareTo("NO DATA")!= 0){
							
						
							if(notnull != null)
							{
								if(jmap.get(notnull.trim()).asString().trim().length()>0)
								{
									outlist.add(str);
								}
							}
							else
							{
								outlist.add(str);
							}
						}
					}
					
					jmap=null;
				}
				
				//send remaining strings to db
				if(outlist.size()>0)
				{
					this.template.postJsonData(sql, outlist);
				}
				
				sql=null;
				val=null;
				outlist=null;
				json=null;
			}
		}
		
	}
	
	
	private class SplitQuery implements Callable<ArrayList<String>>{

		/**
		 * 
		 */
		private static final long serialVersionUID = -8185035798235011678L;
		private final getDAOTemplate template;
		private String sql;
		
		private SplitQuery(final getDAOTemplate template, final String sql)
		{
			this.template=template;
			this.sql=sql;
		}

		
		public ArrayList<String> call() {
			// TODO Auto-generated method stub
			ArrayList<String> results=template.getJsonData(sql);
			return results;
		}
	}
	
	/**
	 * Computes Regexes @ 100% CPU for Multiple Result Regexes and
	 * returns a json string with the result.
	 * @author aevans
	 *
	 */
	private class ParseMultiPage implements Callable<String>{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -2269985121520139450L;
		
		private String html;
		private String offenderhash;
		private String date;
		private Map<String,String> regexes;
		private String table;
		private String replacementPattern;
		
		public ParseMultiPage(final String replacementPattern,final String table,final String html,final String offenderhash,final String date,final Map<String,String> regexes)
		{
			super();
			this.replacementPattern=replacementPattern;
			this.table=table;
			this.html=html.replaceAll("\t|\r|\r\n|\n", "");
			this.offenderhash=offenderhash;
			this.date=date;
			this.regexes=regexes;
		}


		public String call() {
			// TODO Auto-generated method stub
			String json=null;
			String[] results;
			Regex reg=new Regex();
			
			Set<String> set=this.regexes.keySet();
			
			for(String s: set)
			{
				reg.setPattern(regexes.get(s));
				results=reg.multi_regex(this.html);
				
				if(results != null)
				{
					for(int i=0;i<results.length;i++)
					{
						json=(json==null)?"{\""+s+"\":\""+results[i]+"\",\"table\":\""+table+"\",\"offenderhash\":\""+offenderhash+"\",\"date\":\""+date+"\"}":json+"~"+"{\""+s+"\":\""+results[i]+"\",\"table\":\""+table+"\",\"offenderhash\":\""+offenderhash+"\",\"date\":\""+date+"\"}";
					}
				}
				else
				{
					json="No Data";
				}
			}
			
			if(json != null)
			{
				
				if(this.replacementPattern!= null)
				{
					json=json.replaceAll(replacementPattern," ").trim();
				}
			}
			else
			{
				json="No Data";
			}
			
			html=null;
			regexes=null;
			return json;
		}
	
	}

	/**
	 * Computes Regexes @ 100% CPU for single result regexes and returns a 
	 * json string with the result.
	 * @author aevans
	 *
	 */
	private class ParsePage implements Callable<String> {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 2486584810895316538L;
		private final String html;
		private final String offenderhash;
		private final String date;
		private final String table;
		private final String replacementPattern;

		private Map<String,String> regex;
		
		public ParsePage(final String replacementPattern,final String table,final String inhtml,final Map<String,String> regex,final String date,final String offenderhash)
		{
			this.replacementPattern=replacementPattern;
			this.table=table;
			this.offenderhash=offenderhash;
			this.html=inhtml.replaceAll("\t|\r|\r\n|\n", "");
			this.regex=regex;
			this.date=date;
		}
		
		
		public String call(){
			// TODO Auto-generated method stub
			String json=null;
			String result;
			Regex reg=new Regex();
			Set<String> set=this.regex.keySet();
			
			for(String r: set)
			{
				reg.setPattern(this.regex.get(r));
				result=reg.single_regex(this.html);
				
				if(result != null & r.compareTo("table") != 0& r.compareTo("offenderhash") != 0& r.compareTo("date") != 0)
				{
					json=(json==null)?"{\""+r.replaceAll(replacementPattern," ").trim()+"\":\""+result.replaceAll(replacementPattern," ").trim()+"\"":json+",\""+r.replaceAll(replacementPattern," ").trim()+"\":\""+result.replaceAll(replacementPattern," ").trim()+"\"";
				}
				else if(r.compareTo("table") != 0& r.compareTo("offenderhash") != 0& r.compareTo("date") != 0)
				{
					json=(json==null)?"{\""+r.replaceAll(replacementPattern," ").trim()+"\":\"\"":json.trim()+",\""+r.replaceAll(replacementPattern," ").trim()+"\":\"\"";
				}
			}
			
			if(json != null)
			{
				json+=",\"table\":\""+this.table+"\",\"date\":\""+this.date+"\",\"offenderhash\":\""+this.offenderhash+"\"}";

					json=json.trim();

			}
			else
			{
				json="No Data";
			}
			regex=null;
			return json;
		}
	}
	
	
	/**
	 * This class is used to perform a "looping regex" where elements 
	 * are broke down to a piece of the page and then further parsed. It is 
	 * still a bit faster than getting every tag and then regexing for certain information.
	 * Really, I tested it 
	 * 
	 * Comparison of operations:
	 * XML:toXML-->compile -->parse, -->compile-->parse-->check condition-->replace-->json
	 * v. 
	 * REGEX:split reg-->compile--> parse-->compile-->parse-->replace-->json
	 * 
	 * 
	 * @author aevans
	 *
	 */
	private class LoopRegex implements Callable<String>{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 7604436478239646264L;
		private String html;
		private String offenderhash;
		private String date;
		private String table;
		private String replacementPattern;
		private Map<String,String> patterns;
		private boolean test;
		

		public LoopRegex(final String html,final String offenderhash,final String date,final String table,final String replacementPattern,final Map<String,String> patterns, final boolean test){
			super();
			this.html = html.replaceAll("\t|\r|\r\n|\n", "");
			this.offenderhash = offenderhash;
			this.date = date;
			this.table = table;
			this.replacementPattern = replacementPattern;
			this.patterns=patterns;
			this.test=test;
		}


		public String call(){
			// TODO Auto-generated method stub
			String json=null;
			Regex reg=new Regex();
			Set<String> keys=patterns.keySet();
			String result=this.html;
			String[] results=null;
			Boolean mustcontain=true;
			String secondres=null;
			String stringified=null;
			Map<String,String> resultsmap=new HashMap<String,String>();
			
			
			if(keys.contains("mustcontain")){
				mustcontain=this.html.contains(patterns.get("mustcontain"));
			}
			
			
			if(mustcontain){
			
				if(test){
					System.out.println("//////////////////////HTML////////////////////////\n"+this.html+"\n///////////////////////////////END///////////////////////////\n\n");
				}
				
				for(String k:keys)
				{
					reg.setPattern(patterns.get(k));
				
				
					if(k.contains("narrow"))
					{
						result=reg.single_regex(this.html);
					}
					else if(k.contains("PsingleP"))
					{
						if(result != null)
						{

							secondres=reg.single_regex(result);

							if(secondres != null)
							{

								resultsmap.put(k.replaceAll("PsingleP",""),secondres.trim());
								secondres=null;
							}
						}
					}
					else if(k.contains("mustcontain")==false)
					{
						if(result != null)
						{

							results=reg.multi_regex(result.replaceAll("\t|\r|\r\n", ""));
						
							
							if(results != null)
							{
								for(String s: results)
								{
									stringified=(stringified==null)?s:stringified+"|"+s;						
								}
							
								resultsmap.put(k, stringified);
								stringified=null;
								results=null;
							}
						
						}
					}
				}
			
				if(resultsmap.size() > 0)
				{
					for(String k: resultsmap.keySet())
					{
						json=(json==null)?"{\""+k+"\":\""+resultsmap.get(k).replaceAll(replacementPattern, " ")+"\"":json+",\""+k+"\":\""+resultsmap.get(k).replaceAll(replacementPattern, " ")+"\"";
					}
				}
			
				if(json ==null)
				{
					json="No Data";
				}
				else
				{
					json+=",\"table\":\""+this.table+"\",\"date\":\""+this.date+"\",\"offenderhash\":\""+this.offenderhash+"\"}";
					json=json.trim();
				}
			
				this.patterns=null;
				this.html=null;
				stringified=null;
				secondres=null;
				resultsmap=null;
			
				}
				else{
					json="No Data";
				}
			
			return json;
		}
		
	}
	
	
	
	
	public String getFkey() {
		return fkey;
	}


	public void setFkey(String fkey) {
		this.fkey = fkey;
	}





	public String getFkeyref() {
		return fkeyref;
	}





	public void setFkeyref(String fkeyref) {
		this.fkeyref = fkeyref;
	}





	public String getKey() {
		return key;
	}





	public void setKey(String key) {
		this.key = key;
	}




	public String getExtracondition() {
		return extracondition;
	}





	public void setExtracondition(String extracondition) {
		this.extracondition = extracondition;
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





	public int getLoops() {
		return loops;
	}




	public void setLoops(int loops) {
		this.loops = loops;
	}




	public int getWaitloops() {
		return waitloops;
	}




	public void setWaitloops(int waitloops) {
		this.waitloops = waitloops;
	}




	public int getTermtime() {
		return termtime;
	}




	public void setTermtime(int termtime) {
		this.termtime = termtime;
	}




	public int getSPLITSIZE() {
		return SPLITSIZE;
	}




	public void setSPLITSIZE(int sPLITSIZE) {
		SPLITSIZE = sPLITSIZE;
	}




	public int getSqlnum() {
		return sqlnum;
	}




	public void setSqlnum(int sqlnum) {
		this.sqlnum = sqlnum;
	}




	public int getProcnum() {
		return procnum;
	}



	public void setProcnum(int procnum) {
		this.procnum = procnum;
	}



	public int getQnum() {
		return qnum;
	}





	public void setQnum(int qnum) {
		this.qnum = qnum;
	}

	
	

	public String getNotnull() {
		return notnull;
	}





	public void setNotnull(String notnull) {
		this.notnull = notnull;
	}





	public int getPullsize() {
		return pullsize;
	}





	public void setPullsize(int pullsize) {
		this.pullsize = pullsize;
	}





	public Map<String, Map<String, String>> getLoopedpats() {
		return loopedpats;
	}





	public void setLoopedpats(Map<String, Map<String, String>> loopedpats) {
		this.loopedpats = loopedpats;
	}





	public ArrayList<String> getPages() {
		return pages;
	}





	public void setPages(ArrayList<String> pages) {
		this.pages = pages;
	}





	public String getSchema() {
		return schema;
	}




	@Required
	public void setSchema(String schema) {
		this.schema = schema;
	}


	public String getReplacementPattern() {
		return replacementPattern;
	}





	public void setReplacementPattern(String replacementPattern) {
		this.replacementPattern = replacementPattern;
	}





	public String getPost() {
		return post;
	}





	public void setPost(String post) {
		this.post = post;
	}





	public jdbcconn getJdbc() {
		return jdbc;
	}





	public void setJdbc(jdbcconn jdbc) {
		this.jdbc = jdbc;
	}





	public String getPullid() {
		return pullid;
	}




	@Required
	public void setPullid(String pullid) {
		this.pullid = pullid;
	}





	public String getSelect() {
		return select;
	}




	@Required
	public void setSelect(String select) {
		this.select = select;
	}





	public String getColumn() {
		return column;
	}





	public void setColumn(String column) {
		this.column = column;
	}


	public int getCommit_size() {
		return commit_size;
	}





	public void setCommit_size(int commit_size) {
		this.commit_size = commit_size;
	}




	public String getImageRegex() {
		return imageRegex;
	}





	public void setImageRegex(String imageRegex) {
		this.imageRegex = imageRegex;
	}





	public String getImageprefix() {
		return imageprefix;
	}





	public void setImageprefix(String imageprefix) {
		this.imageprefix = imageprefix;
	}





	public String getImagesuffix() {
		return imagesuffix;
	}





	public void setImagesuffix(String imagesuffix) {
		this.imagesuffix = imagesuffix;
	}





	public Map<String, String> getSinglepats() {
		return singlepats;
	}





	public void setSinglepats(Map<String, String> singlepats) {
		this.singlepats = singlepats;
	}




	public Map<String, Map<String, String>> getMultipats() {
		return multipats;
	}




	public void setMultipats(Map<String, Map<String, String>> multipats) {
		this.multipats = multipats;
	}




	public getDAOTemplate getTemplate() {
		return template;
	}




	@Autowired
	@Required
	@Resource(name="getDAOTemplate")
	public void setTemplate(getDAOTemplate template) {
		this.template = template;
	}





	public int getOffset() {
		return offset;
	}




	@Required
	public void setOffset(int offset) {
		this.offset = offset;
	}


	public Logger getLog() {
		return log;
	}


	private void getFromDb(String condition)
	{
		pages=template.getJsonData((select+condition));
		
	}
	
	private void sendToDb(ArrayList<String> json,boolean split)
	{
		if(json.size()>0)
			log.info("Records to Add: "+json.size());

		if(split)
		{
			
			ForkJoinPool f2=new ForkJoinPool((Runtime.getRuntime().availableProcessors()+((int)Math.ceil(procnum*sqlnum))));
			ArrayList<String> l;
			int size=(int)Math.ceil(json.size()/qnum);
			for(int conn=0;conn<qnum;conn++)
			{
				l=new ArrayList<String>();
				if(((conn+1)*size)<json.size())
				{
					l.addAll(json.subList((conn*size),((conn+1)*size)));

				}
				else
				{
					l.addAll(json.subList((conn*size),(json.size()-1)));
					f2.execute(new SplitPost(template,l));
					
					break;
				}
				
				f2.execute(new SplitPost(template,l));
			}
			
			
			try {
				f2.awaitTermination(termtime, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	
			
			f2.shutdown();
			
			
			int incrementor=0;
			
			while(f2.isShutdown()==false)
			{
				incrementor++;
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				log.info("Shutting Down"+incrementor);
			}
			
			l=null;
			f2=null;
			
		}
		else
		{
			for(String j: json)
			{
				this.template.postSingleJson(j);
			}
		}
		
	}
	
	public String genHash(String id)
	{
		
		long h=0;
		String d=id+Long.toString(Calendar.getInstance().getTimeInMillis());
		
		for(int i=0;i<d.length();i++)
		{
			h+=(d.charAt(i)*(10*i));
		}
		
		GenandSetHash gsh=new GenandSetHash();
		gsh.setHash(Long.toString(h));
		gsh.setTemplate(this.template);
		
		return gsh.run();
	}
	
	public void createTables()
	{
		Set<String> keys=null;
		String sql=null;
		String[] tablearr;
		String vals=null;
		if(singlepats != null & schema != null)
		{

			if(singlepats.containsKey("table"))
			{
				tablearr=singlepats.get("table").trim().split("\\.");
				if(this.template.checkTable(singlepats.get("table"), tablearr[0])==false)
				{
					sql="CREATE TABLE "+singlepats.get("table")+"(";
				
					keys=singlepats.keySet();
						
					if(keys.contains("id")==false)
					{
						if(key != null){
							vals="id SERIAL UNIQUE NOT NULL";
						}
						else{
							key="";
							fkey="";
							vals="id SERIAL PRIMARY KEY NOT NULL";
						}
					}

					for(String k: keys)
					{
						if(k.compareTo("table")!=0)
						{
							if(k.compareTo(key)==0)
							{
								vals+=(vals==null)?k+" text PRIMARY KEY":","+k+"text";
							}
							else if(k.compareTo(fkey)==0){
								vals+=(vals==null)?k+" text FOREIGN KEY "+fkeyref:","+k+" text FOREIGN KEY "+fkeyref;
							}
							else{
								vals=(vals==null)?k+" text":vals+","+k+" text";
							}
						}
					}
				
				
					sql+=vals+",date text,offenderhash text)";
				
					this.template.execute(sql);
				}
				else if(truncate){
					
					sql="TRUNCATE "+singlepats.get("table");
					
					if(cascade){
						sql+=" CASCADE";
					}
					log.info("Truncating with: "+sql);
					template.execute(sql);
				}
				
			}
			
		}
		
		if(multipats != null & schema != null)
		{
			keys=multipats.keySet();
			
			for(String table:keys)
			{
				if(this.template.checkTable(table, schema)==false)
				{
					this.template.createTable(table, schema, multipats.get(table).keySet(),key,fkey,fkeyref);	
				}
				else if(truncate){
					sql="TRUNCATE "+table;
					
					if(cascade){
						sql+=" CASCADE";
					}
					log.info("Truncating with: "+sql);
					template.execute(sql);
				}
			}
		}
		
		if(loopedpats != null & schema != null)
		{
			
			keys=loopedpats.keySet();
			
			for(String table: keys)
			{
				if(this.template.checkTable(table, schema)==false)
				{
					this.template.createTable(table, schema, loopedpats.get(table).keySet(),key,fkey,fkeyref);
				}
				else if(truncate){
					sql="TRUNCATE "+table;
					
					if(cascade){
						sql+=" CASCADE";
					}
					log.info("Truncating with: "+sql);
					template.execute(sql);
				}
			}
		}
	}

	/**
	 *Fork/Join Pool Solution Maximizes Speed. JSon increases ease of use
	 * 
	 */
	public void run()
	{
		
		String add=null;
		
		Set<Callable<String>> collect=new HashSet<Callable<String>>();
		List<Future<String>> futures;
		
		List<Future<ArrayList<String>>> qfutures;
		Set<Callable<ArrayList<String>>> qcollect=new HashSet<Callable<ArrayList<String>>>(4);
		
		ForkJoinPool fjp = new ForkJoinPool((int) Math.ceil(Runtime.getRuntime().availableProcessors()*procnum));
		
		ArrayList<String> parsedrows=new ArrayList<String>();
		
		//List<Future<String>> 
		//get the singular patterns from the chunk of data
		log.info("Starting Clock and Parsing @"+Calendar.getInstance().getTime().toString());
		long t=Calendar.getInstance().getTimeInMillis();
		Map<String,Json> jmap;
		String condition=null;
		
		if(schema != null)
		{
			createTables();
		}
		

		//attempt to query the database from multiple threads
		for(int conn=1;conn<=qnum;conn++)
		{
			condition=" WHERE "+pullid+" >= "+Integer.toString(offset+(((Integer)Math.round(pullsize/qnum))*(conn-1)))+" AND "+pullid+" <= "+Integer.toString(offset+(((Integer)Math.round(pullsize/qnum))*conn));
			
			if(extracondition != null)
			{
				condition+=" "+extracondition.trim();
			}
			
			qcollect.add(new SplitQuery(template,(select+condition)));
			log.info("Fetching "+select+condition);
		}
		
		
		qfutures=fjp.invokeAll(qcollect);
		
		for(Future<ArrayList<String>> f: qfutures)
		{
			try{

				ArrayList<String> test=f.get();
				if(test != null)
				{
					if(test.size()>0)
					{
						pages.addAll(test);
					}
				}
			
				if(f.isDone()==false)
				{
					f.cancel(true);
				}
			
				f=null;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		qcollect=new HashSet<Callable<ArrayList<String>>>(4);
		qfutures=null;

		
		log.info("Performing Regex");
		int i=0;
		int j=1;
		int records=0;
		
		while(pages.size()>0)
		{
			log.info("Currently Active Threads: "+Thread.activeCount());
			log.info("Pages Found in the Iteration "+j+": "+pages.size());

			if(fjp.isShutdown())
			{
				fjp=new ForkJoinPool(Runtime.getRuntime().availableProcessors()*procnum);
			}
			
			i=0;

			
			if(singlepats != null)
			{
				
			log.info("Found Singlepats");
			for(String row:pages)
			{	
				
				String str=row;
				str=str.replaceAll("\t|\r|\r\n|\n","");
				jmap=Json.read(str).asJsonMap();
				
				if(singlepats.containsKey("table"))
				{
					if(fjp.isShutdown())
					{
						fjp=new ForkJoinPool((Runtime.getRuntime().availableProcessors()*procnum));
					}
					
					if(jmap.get(column)!=null)
					{
						
						if(test){
							System.out.println("//////////////////////HTML////////////////////////\n"+jmap.get(column).asString()+"\n///////////////////////////////END///////////////////////////\n\n");
						}
						
						if(mustcontain != null)
						{
							if(jmap.get(column).asString().contains(mustcontain))
							{
								if(cannotcontain != null)
								{
									if(jmap.get(column).asString().contains(cannotcontain)==false)
									collect.add(new ParsePage(replacementPattern,singlepats.get("table"),jmap.get(column).asString().replaceAll("\\s\\s", " "),singlepats, Calendar.getInstance().getTime().toString(), jmap.get("offenderhash").asString()));
								}
								else
								{
									collect.add(new ParsePage(replacementPattern,singlepats.get("table"),jmap.get(column).asString().replaceAll("\\s\\s", " "),singlepats, Calendar.getInstance().getTime().toString(), jmap.get("offenderhash").asString()));
								}
							}
						}
						else if(cannotcontain != null)
						{
							if(jmap.get(column).asString().contains(cannotcontain)==false)
							{
								collect.add(new ParsePage(replacementPattern,singlepats.get("table"),jmap.get(column).asString().replaceAll("\\s\\s", " "),singlepats, Calendar.getInstance().getTime().toString(), jmap.get("offenderhash").asString()));
							}
						}
						else
						{
							collect.add(new ParsePage(replacementPattern,singlepats.get("table"),jmap.get(column).asString().replaceAll("\\s\\s", " "),singlepats, Calendar.getInstance().getTime().toString(), jmap.get("offenderhash").asString()));
						}
					}
				}
				i++;
				
				if(((i%commit_size)==0 & i != 0) | i==pages.size() |pages.size()==1 & singlepats != null)
				{
					log.info("Getting Regex Results");
				
					log.info("Getting Tasks");
					
					futures=fjp.invokeAll(collect);
					
					 int w=0;
					
					while(fjp.getActiveThreadCount()>0){
						w++;
					}
					
					log.info("Waited for "+w+" cycles");

					for(Future<String> r:futures)
					{
						try {
						
							add=r.get();
							if(add.contains("No Data")==false)
							{
								parsedrows.add(add);
							}
							
							add=null;

						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (ExecutionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
					futures=null;
					collect=new HashSet<Callable<String>>();
				
					if(parsedrows.size()>=commit_size)
					{
						
						if(parsedrows.size()>=SPLITSIZE)
						{
							sendToDb(parsedrows,true);
						}
						else
						{
							sendToDb(parsedrows,false);
						}
						
						parsedrows=new ArrayList<String>();
					}
				
					
					//hint to the gc in case it actually pays off (think if i were a gambling man)
					System.gc();
					Runtime.getRuntime().gc();
				}
				
				
			}
			}
			
			//looped patterns
			//perform any looped regular expressions
			if(loopedpats!= null)
			{
				log.info("Looped Patterns Found");
				if(fjp.isShutdown())
				{
					fjp=new ForkJoinPool(Runtime.getRuntime().availableProcessors()*procnum);
				}
				
				for(String row: pages)
				{
					
					
					for(String k:loopedpats.keySet())
					{
						if(fjp.isShutdown())
						{
							fjp=new ForkJoinPool(Runtime.getRuntime().availableProcessors()*procnum);
						}
						jmap=Json.read(row).asJsonMap();
						
						if(jmap.get(column) != null){
							
							
							if(mustcontain != null)
							{
								if(jmap.get(column).asString().contains(mustcontain))
								{
									if(cannotcontain != null)
									{
										if(jmap.get(column).asString().contains(cannotcontain)==false)
										{
											//final String html,final String offenderhash,final String date,final String table,final String replacementPattern,final Map<String,String> patterns
											collect.add(new LoopRegex(jmap.get(column).asString().replaceAll("\\s\\s", " "),jmap.get("offenderhash").asString(),Calendar.getInstance().getTime().toString(),k,replacementPattern,loopedpats.get(k),test));
										}
									}
									else
									{
										//final String html,final String offenderhash,final String date,final String table,final String replacementPattern,final Map<String,String> patterns
										collect.add(new LoopRegex(jmap.get(column).asString().replaceAll("\\s\\s", " "),jmap.get("offenderhash").asString(),Calendar.getInstance().getTime().toString(),k,replacementPattern,loopedpats.get(k),test));
									}
								}
							}
							else if(cannotcontain != null)
							{
								if(jmap.get(column).asString().contains(cannotcontain)==false)
								{
									//final String html,final String offenderhash,final String date,final String table,final String replacementPattern,final Map<String,String> patterns
									collect.add(new LoopRegex(jmap.get(column).asString().replaceAll("\\s\\s", " "),jmap.get("offenderhash").asString(),Calendar.getInstance().getTime().toString(),k,replacementPattern,loopedpats.get(k),test));
								}
							}
							else
							{
								//final String html,final String offenderhash,final String date,final String table,final String replacementPattern,final Map<String,String> patterns
								collect.add(new LoopRegex(jmap.get(column).asString().replaceAll("\\s\\s", " "),jmap.get("offenderhash").asString(),Calendar.getInstance().getTime().toString(),k,replacementPattern,loopedpats.get(k),test));
							}
							jmap.remove(k);
						}
						i++;
						if(((i%commit_size)==0 & i != 0) || (i%(pages.size()-1))==0 || pages.size()==1)
						{
				
							futures=fjp.invokeAll(collect);
					
							 int w=0;
								
								while(fjp.getActiveThreadCount()>0){
									w++;
								}
								log.info("Waited "+w+" Cycles");
								
								
							for(Future<String> r:futures)
							{
								try {
									add=r.get();
									if(add.contains("No Data")==false)
									{
										for(String toarr: add.split("~"))
										{
											parsedrows.add(toarr);
										}
									}
									
									if(r.isDone()==false)
									{
										r.cancel(true);
									}
									add=null;
									
									
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (ExecutionException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							
							futures=null;
							collect=new HashSet<Callable<String>>();
							
							if(parsedrows.size()>=commit_size)
							{
								if(parsedrows.size()>=SPLITSIZE)
								{
									sendToDb(parsedrows,true);
								}
								else
								{
									sendToDb(parsedrows,false);
								}
								parsedrows=new ArrayList<String>();
							}
							
							//hint to the gc in case it actually pays off
							System.gc();
							Runtime.getRuntime().gc();
						}
					}
				
					if(parsedrows.size()>=0)
					{
						if(parsedrows.size()>=SPLITSIZE)
						{
							sendToDb(parsedrows,true);
						}
						else
						{
							sendToDb(parsedrows,false);
						}

						parsedrows=new ArrayList<String>();
					}
				}
			}
			
			
			if(parsedrows.size()>0)
			{
				
				if(parsedrows.size()>=SPLITSIZE)
				{
					sendToDb(parsedrows,true);
				}
				else
				{
					sendToDb(parsedrows,false);
				}
				
				parsedrows=new ArrayList<String>();
			}
			
			
			
			//multiple patterns
			if(multipats!= null)
			{
				
				//parse multiple pages for the run
				for(String row: pages)
				{
					
					for(String k:multipats.keySet())
					{
						if(fjp.isShutdown())
						{
							
							fjp=new ForkJoinPool(Runtime.getRuntime().availableProcessors());
						}
						
						jmap=Json.read(row).asJsonMap();
						
						
						if(jmap.get(column) != null )
						{
							if(test){
								System.out.println("//////////////////////HTML////////////////////////\n"+jmap.get(column).asString()+"\n///////////////////////////////END///////////////////////////\n\n");
							}
							
							if(mustcontain != null)
							{
								if(jmap.get(column).asString().contains(mustcontain))
								{
									if(cannotcontain != null)
									{
										if(jmap.get(column).asString().contains(cannotcontain)==false)
										{
											//final String replacementPattern,final String table,final String html,final String offenderhash,final String date,final Map<String,String> regexes
											collect.add(new ParseMultiPage(replacementPattern,k,jmap.get(column).asString().replaceAll("\\s\\s", " "),jmap.get("offenderhash").asString(), Calendar.getInstance().getTime().toString(), multipats.get(k)));	
										}
									}
									else
									{
										//final String replacementPattern,final String table,final String html,final String offenderhash,final String date,final Map<String,String> regexes
										collect.add(new ParseMultiPage(replacementPattern,k,jmap.get(column).asString(),jmap.get("offenderhash").asString().replaceAll("\\s\\s", " "), Calendar.getInstance().getTime().toString(), multipats.get(k)));	
									}
								}
							}
							else if(cannotcontain != null)
							{
								if(jmap.get(column).asString().contains(cannotcontain)==false)
								{
									//final String replacementPattern,final String table,final String html,final String offenderhash,final String date,final Map<String,String> regexes
									collect.add(new ParseMultiPage(replacementPattern,k,jmap.get(column).asString().replaceAll("\\s\\s", " "),jmap.get("offenderhash").asString(), Calendar.getInstance().getTime().toString(), multipats.get(k)));	
								}
								
							}
							else
							{
								//final String replacementPattern,final String table,final String html,final String offenderhash,final String date,final Map<String,String> regexes
								collect.add(new ParseMultiPage(replacementPattern,k,jmap.get(column).asString().replaceAll("\\s\\s", " "),jmap.get("offenderhash").asString(), Calendar.getInstance().getTime().toString(), multipats.get(k)));	
							}
						}
						
						i++;
						if(((i%commit_size)==0 & i != 0) | i==pages.size() |pages.size()==1 & multipats != null)
						{
						
							futures=fjp.invokeAll(collect);
					
							 int w=0;
								
								while(fjp.getActiveThreadCount()>0){
									w++;
								}
								log.info("Waited "+w+" Cycles");
							
							for(Future<String> r:futures)
							{
								try {
									add=r.get();
									
									if(add.contains("No Data")==false)
									{
										
										for(String js: add.split("~"))
										{
											parsedrows.add(js);
										}
									}
									add=null;
									
									if(r.isDone()==false)
									{
										r.cancel(true);
									}
									r=null;
									
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (ExecutionException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							
							futures=null;
							collect=new HashSet<Callable<String>>();
							
							if(parsedrows.size()>=commit_size)
							{
								
								if(parsedrows.size()>=SPLITSIZE)
								{
									sendToDb(parsedrows,true);
								}
								else
								{
									sendToDb(parsedrows,false);
								}
								parsedrows=new ArrayList<String>();
							}
					
							
							//hint to the gc in case it actually pays off
							System.gc();
							Runtime.getRuntime().gc();
						}
					}
					
					

				}
			}
			
			if(parsedrows.size()>0)
			{
				
				if(parsedrows.size()>=SPLITSIZE)
				{
					sendToDb(parsedrows,true);
				}
				else
				{
					sendToDb(parsedrows,false);
				}
				
				parsedrows=new ArrayList<String>();
			}
			//add to record count
			records+=i;
			i=0;
			
			
			
			//commit remaining
			log.info("REMAINING ROWS TO COMMIT "+parsedrows.size());
			log.info("Rows Left"+parsedrows.size());
			if(parsedrows.size()>0)
			{
				
				
				if(parsedrows.size()>=SPLITSIZE)
				{
					sendToDb(parsedrows,true);
				}
				else
				{
					sendToDb(parsedrows,false);
				}
				
	
				parsedrows=new ArrayList<String>();
			}
		
			records+=i;
			i=0;

			
			//pull from the database
			log.info("Pulling from Db @"+Calendar.getInstance().getTime().toString());
			pages=new ArrayList<String>();
			if(fjp.isShutdown())
			{
				fjp=new ForkJoinPool((Runtime.getRuntime().availableProcessors()*procnum));
			}
			
			//attempt to query the database from multiple threads
			for(int conn=1;conn<=qnum;conn++)
			{
				//change condition
				condition=" WHERE "+pullid+" >= "+Integer.toString((offset)+pullsize*j+(((Integer)Math.round(pullsize/qnum))*(conn-1)))+" AND "+pullid+" <= "+Integer.toString((offset)+pullsize*j+(((Integer)Math.round(pullsize/qnum))*conn));
				
				if(extracondition != null)
				{
					condition+=" "+extracondition.trim();
				}
				
				log.info(select+condition);
				qcollect.add(new SplitQuery(template,(select+condition)));
			}
			
			qfutures=fjp.invokeAll(qcollect);
			
			for(Future<ArrayList<String>> f: qfutures)
			{
				try {

					ArrayList<String> test=f.get();
					if(test != null)
					{
						if(test.size()>0)
						{
							pages.addAll(test);
						}
					}
					
					
				
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			

			qfutures=null;
			qcollect=new HashSet<Callable<ArrayList<String>>>();
			
			log.info("Finished Pulling from Db @"+Calendar.getInstance().getTime().toString());
			
			log.info("Memory Remaining: "+Runtime.getRuntime().freeMemory());
			if(Runtime.getRuntime().freeMemory()<500000 | ((loops%waitloops)==0 & waitloops != 1))
			{
				log.info("Paused Free Memory Left: "+Runtime.getRuntime().freeMemory());
				System.gc();
				Runtime.getRuntime().gc();
				
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				while(Runtime.getRuntime().freeMemory()<500000)
				{
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				log.info("Restart Free Memory Left: "+Runtime.getRuntime().freeMemory());
			
			}
			
			
			j++;
		}
		
		log.info("Shutting Down Fork Join Pool");
		if(fjp.isShutdown()==false){
			fjp.shutdownNow();
		}
		
		log.info("Complete @"+Calendar.getInstance().getTime().toString());
		log.info("Total Runtime(seconds): "+Double.toString((double)(Calendar.getInstance().getTimeInMillis()-t)/1000));
		log.info("Total Regex Tasks Completed(groupings of regexes): "+records);
		
		//hint to the gc in case it actually pays off
		System.gc();
		Runtime.getRuntime().gc();
	}

}