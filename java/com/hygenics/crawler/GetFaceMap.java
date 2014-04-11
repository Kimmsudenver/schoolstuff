package com.hygenics.crawler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hygenics.jdbc.jdbcconn;

import mjson.Json;
import regex2.Regex;

/**
 * 
 * INCOMPLETE PENDING COMPLETION OF PARTS OF THE FACIAL RECOGNITION CLASS (NOT WHOLE THING LUCKILY)
 * 
 * This Step Takes in Images from the Database and Gets a Spline Map of the face that can be used
 * in facial recognition application contexts. This information could potentially be sold, used to 
 * find duplicates, or used in our own applications (facial recognition, camera based individual detection,
 * or something similar)
 * 
 * Small pullsizes and commit sizes are recommended here. This algorithm could take a while even though I 
 * don't use the tripple looped Gabor (it takes about 1/2-2 second(s) to get a recent edge and 1/4-1 second(s) per spline
 * after getting the points around the face)
 * 
 * @author aevans
 *
 */
public class GetFaceMap {

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
	private String idcolumn;
	private String post;
	
	private int commit_size=100;
	
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
	
	public GetFaceMap(){
		
	}
	
	
	
	@Override
	public String toString() {
		return "GetFaceMap [notnull=" + notnull + "]";
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
					else
					{
						//case is that no table is different and the number of values does not differ from the previous ammount
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
	
	
	private class SplitQuery extends RecursiveTask<ArrayList<String>>{

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

		@Override
		protected ArrayList<String> compute() {
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
	private class ParseMultiPage extends RecursiveTask<String>{
		
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
			this.html=html;
			this.offenderhash=offenderhash;
			this.date=date;
			this.regexes=regexes;
		}

		@Override
		protected String compute() {
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
	
	private void genSplines(){
		
	}
	
	public void run()
	{
		genSplines();
	}
}
