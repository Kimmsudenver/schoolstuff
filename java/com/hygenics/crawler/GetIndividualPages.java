package com.hygenics.crawler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;




import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




import javax.annotation.*;

import org.springframework.beans.factory.annotation.Required;

import mjson.Json;







import com.hygenics.html.html_grab;
import com.hygenics.crawlerobjects.IndividualPage;
import com.hygenics.crawlerobjects.PullObject;

/**
 * The purpose of this code is to Get Individual Pages. This page uses the getDAOTemplate.
 * The class works a little differently then the get pages class. It gets individual urls and
 * then runs a loop where specified. The basic object for the pull list is, however, saved.
 * The individual pull nodes can still be discarded.
 * 
 * @author asevans
 */


public class GetIndividualPages {

	private static final Logger log=LoggerFactory.getLogger(MainApp.class);
	
	private String extracondition;
	private String key;
	
	private boolean genHash=true;
	private String pullid="linkid";
	private int indipageid=1;

	private ArrayList<String> linksarr=new ArrayList<String>();
	private String split=":\\*:";
	private int commit_size=100;
	private ArrayList<IndividualPage> ip=new ArrayList<IndividualPage>();
	private IndividualPage pagehead;
	private PullObject current;
	private String linkprefix;
	private int WAIT=4;
	
	private boolean test=false;
	private boolean linkasparam=false;
	private String linkasparamlink;
	private String linkasparamname;
	
	private String linkreplace;
	private String linktoreplace;
	private String linksuffix;
	private String query;
	private PullObject head;
	private String idcolumn;
	private String table;
	private String post_column;
	private String column;
	private Map<String,String> pages;
	private int offset=0;

	private Map<String,Map<String,String>> indiMap;
	
	
	
	//the DAO parser
	private getDAOTemplate gdt;
	private boolean proxy;
	private String proxyhost;
	private String proxyuser;
	private String proxyport;
	private String proxypass;
	
	public GetIndividualPages()
	{
		
	}
	
	
	
	

	public String getExtracondition() {
		return extracondition;
	}





	public void setExtracondition(String extracondition) {
		this.extracondition = extracondition;
	}





	public String getKey() {
		return key;
	}



	public void setKey(String key) {
		this.key = key;
	}



	public boolean isGenHash() {
		return genHash;
	}





	public void setGenHash(boolean genHash) {
		this.genHash = genHash;
	}





	public String getPullid() {
		return pullid;
	}






	public void setPullid(String pullid) {
		this.pullid = pullid;
	}






	public boolean isTest() {
		return test;
	}






	public void setTest(boolean test) {
		this.test = test;
	}






	public String getLinkasparamname() {
		return linkasparamname;
	}




	public void setLinkasparamname(String linkasparamname) {
		this.linkasparamname = linkasparamname;
	}




	public String getLinkasparamlink() {
		return linkasparamlink;
	}






	public void setLinkasparamlink(String linkasparamlink) {
		this.linkasparamlink = linkasparamlink;
	}






	public boolean isLinkasparam() {
		return linkasparam;
	}




	public void setLinkasparam(boolean linkasparam) {
		this.linkasparam = linkasparam;
	}




	public int getWAIT() {
		return WAIT;
	}



	public void setWAIT(int wAIT) {
		WAIT = wAIT;
	}



	public boolean isProxy() {
		return proxy;
	}






	public void setProxy(boolean proxy) {
		this.proxy = proxy;
	}






	public String getProxyhost() {
		return proxyhost;
	}






	public void setProxyhost(String proxyhost) {
		this.proxyhost = proxyhost;
	}






	public String getProxyuser() {
		return proxyuser;
	}






	public void setProxyuser(String proxyuser) {
		this.proxyuser = proxyuser;
	}






	public String getProxyport() {
		return proxyport;
	}






	public void setProxyport(String proxyport) {
		this.proxyport = proxyport;
	}






	public String getProxypass() {
		return proxypass;
	}






	public void setProxypass(String proxypass) {
		this.proxypass = proxypass;
	}






	public String getLinktoreplace() {
		return linktoreplace;
	}





	public void setLinktoreplace(String linktoreplace) {
		this.linktoreplace = linktoreplace;
	}





	public String getSplit() {
		return split;
	}




	public void setSplit(String split) {
		this.split = split;
	}




	public int getCommit_size() {
		return commit_size;
	}



	public void setCommit_size(int commit_size) {
		this.commit_size = commit_size;
	}



	public String getLinkreplace() {
		return linkreplace;
	}



	public void setLinkreplace(String linkreplace) {
		this.linkreplace = linkreplace;
	}



	public String getIdcolumn() {
		return idcolumn;
	}



	public void setIdcolumn(String idcolumn) {
		this.idcolumn = idcolumn;
	}



	public int getOffset() {
		return offset;
	}



	public void setOffset(int offset) {
		this.offset = offset;
	}



	public String getLinkprefix() {
		return linkprefix;
	}



	public void setLinkprefix(String linkprefix) {
		this.linkprefix = linkprefix;
	}



	public String getLinksuffix() {
		return linksuffix;
	}



	public void setLinksuffix(String linksuffix) {
		this.linksuffix = linksuffix;
	}



	public String getColumn() {
		return column;
	}

	public void setColumn(String column) {
		this.column = column;
	}

	@Required
	@Resource(name="getDAOTemplate")
	public void setGdt(getDAOTemplate gdt) {
		this.gdt = gdt;
	}
	
	
	public String getQuery() {
		return query;
	}

	@Required
	public void setQuery(String query) {
		this.query = query;
	}

	
	public String getTable() {
		return table;
	}

	@Required
	public void setTable(String table) {
		this.table = table;
	}

	public String getPost_column() {
		return post_column;
	}

	public void setPost_column(String post_column) {
		this.post_column = post_column;
	}

	
	public Map<String, Map<String, String>> getIndiMap() {
		return indiMap;
	}

	@Required
	@Resource(name="p:indimap")
	public void setMap(Map<String, Map<String, String>> indiMap) {
		this.indiMap = indiMap;
	}

	
	private void getFromDB(String select)
	{
		pages=(genHash)?gdt.getData(select,idcolumn,column,false):gdt.getData(select,idcolumn,column,true);
		log.info(""+pages.size());
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
		
		GenandSetHash gsh=new GenandSetHash();
		gsh.setHash(Long.toString(h));
		gsh.setTemplate(this.gdt);
		
		return gsh.run();
	}
	
	private void posttoDB()
	{
		
		if(pagehead != null)
		{
			if(genHash){
				gdt.BatchUpdateIndi(table,new String[]{"html","link","pullid","datestamp","offenderhash"}, ip,indipageid,true);
			}
			else{
				gdt.BatchUpdateIndi(table,new String[]{"html","link","pullid","datestamp","offenderhash"}, ip,indipageid,false);
			}
		}
		indipageid=indipageid+ip.size();
		
	}
	
	private String[] listtoArray(ArrayList<String> inlist)
	{
		if(inlist.size()>0)
		{
			String[] out=new String[inlist.size()];
			
			for(int i=0;i<inlist.size();i++)
			{
				out[i]=inlist.get(i);
			}
			return out;
		}
		return null;
	}
	
	
	/**
	 *Sets the Pull Queue
	 * @param obj
	 */
	public void setQueueObject(PullObject obj)
	{
		//TODO set a queue object
		if(head==null)
		{
			head=obj;
			current=head;
		}
		else
		{
			current=head;
			while(current.hasNext() != false)
			{
				current=current.getNext();
			}
			
			current.setNext(obj);
			current=current.getNext();
		}
	}
	
	
	
	/**
	 * Creates the queue for getting to the page that grabs the remaining pages
	 */
	@PostConstruct
	public void CreateQueue()
	{
			//TODO instantiate the pull objects: 1-object per completely separate request (loops and searchterms causing loops limit number)
			String jsonString;
			head=null;
			current=null;
			Set<String> keys=indiMap.keySet();
			Set<String> keys2=null;
			PullObject obj=null;
			ArrayList<String> temp;
			Map<String,String> basics;
			
			for(String s: keys)
			{
				
				if(s.toLowerCase().contains("searchTableheaders")==true | s.toLowerCase().contains("searchTableparams")==true)
				{
					//add search headers and parameters that were from a SQL table
					/*Booleans: a long value is set and bits are set to represent true. Pretty sure that java gets the bit by >> n|n-1 times
					* and then check output bit or mask ~ ROR using BigInteger.isBitSet(n) (unsigned int is 2^32 [SE8 update could further limit memory usage]
					*0:getLinks
					*1:SSL
					*2:Get Viewstate
					*3:Get EventValidation
					*4:Get Server Faces
					*5:Captcha Present
					*6:Reset Cookies
					*7:Critical Page (do not delete)
					*8:Add Url
					*9: OPEN NOTHING YET
					*10: GET Page (set) | POST page (not set)
					*11: OPEN NOTHING YET
					*12: Use Search Terms
					*13: Search Parameters Present
					*14: Search Headers Present
					*15: Loop on Integer (default is String)
					*16: Perform Loop
					*17: Loop Parameters Present
					*18: Loop Get Req (set) | Loop Post Req (not set)
					*/
					if(gdt != null)
					{
						keys2=indiMap.get(s).keySet();
						temp=new ArrayList<String>();
						
						for(String k:keys2)
						{
							if(k.trim().compareTo("table") != 0)
							{
								temp.add(k.trim());
							}
						}
						
						Map<String,ArrayList<String>> tempmap=new HashMap<String,ArrayList<String>>();
						keys2=tempmap.keySet();
						Iterator<String> it=keys2.iterator();
						String key=null;
						
						//add clones to the queue with a clone for each searchterm (populates multiple columns)
						for(int j=0;j<tempmap.get(it.next()).size();j++)
						{
							try {
								//create clone
								PullObject po=(PullObject) obj.clone();
								it=keys2.iterator();
							
								//add headers for the row (j)
								while(it.hasNext())
								{
									key=it.next();
								
							
										if(s.toLowerCase().contains("searchTableheaders"))
										{
											po.addHeader(key, tempmap.get(key).get(j));
										}
										else
										{
											po.addParameter(key, tempmap.get(key).get(j));
										}
								}
								
								//add the deep clone to the queue
								setQueueObject(po);
							} catch (CloneNotSupportedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
				else if(s.toLowerCase().contains("addheaders")==true | s.toLowerCase().contains("addparams")==true)
				{
					//TODO add extra headers
					Map<String,String> tempmap=indiMap.get(s);
					keys2=tempmap.keySet();
					
					
					if(obj != null)
					{
						for(String k: keys2)
						{
							if(s.contains("headers"))
							{
								obj.addHeader(k, tempmap.get(k));
							}
							else
							{
								obj.addParameter(k, tempmap.get(k));
							}
						}
					}
					
				}
				else
				{
				
				//add everything else
				if(s.toLowerCase().contains("get")==true | s.toLowerCase().contains("post")==true)
				{
					//create the clone object
					obj=new PullObject();
					setQueueObject(obj);
					
					
					if(s.toLowerCase().contains("get")==true)
					{
						current.setBoolState(10);
					}
					
					//set the object values
					basics=indiMap.get(s);
					keys2=basics.keySet();
					
					for(String k: keys2)
					{
			
						if(k.compareTo("url")==0)
						{
							current.setUrl(basics.get(k));
						}
						else if(k.compareTo("loop")==0)
						{
							if(basics.get(k).contains("true"))
							{
								current.setBoolState(16);
							}
						}
						else if(k.compareTo("loopInt")==0)
						{
							if(basics.get(k).contains("true"))
							{
								current.setBoolState(15);
							}
						}
						else if(s.toLowerCase().contains("critical")==true)
						{
							if(basics.get(k).toLowerCase().contains("true"))
							{
								current.setBoolState(7);
							}
						}
						else if(s.toLowerCase().contains("captchaRegex")==true)
						{
							current.setCaptchaParam(basics.get(k));
						}
						else if(s.toLowerCase().contains("captchaParam")==true)
						{
							current.setCaptchaParam(basics.get(k));
						}
						else if(k.compareTo("add")==0)
						{
							if(basics.get(k).toLowerCase().contains("true"))
							{
								current.setBoolState(8);
							}
						}
						else if(k.toLowerCase().contains("searchheaders"))
						{
							if(basics.get(k).toLowerCase().contains("true"))
							{
								current.setBoolState(13);
							}
						}
						else if(k.toLowerCase().contains("searchparams"))
						{
							if(basics.get(k).toLowerCase().contains("true"))
							{
								current.setBoolState(14);
							}
						}
						else if(k.compareTo("critical")==0)
						{
							if(basics.get(k).toLowerCase().trim().contains("true"))
							{
								current.setBoolState(7);
							}
						}
						else if(k.compareTo("useterms")==0)
						{
							if(basics.get(k).compareTo("true")==0)
							{
								current.setBoolState(12);
							}
						}
						else if(k.compareTo("ssl")==0)
						{
							if(basics.get(k).toLowerCase().trim().compareTo("true")==0)
							{
								current.setBoolState(1);
							}

						}
						else if(k.compareTo("eval")==0)
						{
							if(basics.get(k).toLowerCase().trim().compareTo("true")==0)
							{
								current.setBoolState(3);
							}
						}
						else if(k.compareTo("resetCookies")==0)
						{
							if(basics.get(k).toLowerCase().trim().compareTo("true")==0)
							{
								current.setBoolState(6);
							}

						}
						else if(k.compareTo("loopreg")==0)
						{
							current.setLoop_reg(basics.get(k).trim());
						}
						else if(k.toLowerCase().trim().compareTo("host")==0)
						{
							current.setHost(basics.get(k).trim());
						}
						else if(k.toLowerCase().trim().compareTo("authority")==0)
						{
							current.setAuthority(basics.get(k).trim());
						}
						else if(k.compareTo("timeout")==0)
						{
							current.setTimeout(Integer.parseInt(basics.get(k).trim()));
						}
						else if(k.compareTo("perPage")==0)
						{
							current.setPerPage(Integer.parseInt(basics.get(k).trim()));
						}
						else if(k.contains("viewstate"))
						{
							if(basics.get(k).toLowerCase().trim().contains("true"))
							{
								current.setBoolState(2);
							}
						}
						else if(k.compareTo("serverfaces")==0)
						{
							if(basics.get(k).toLowerCase().trim().compareTo("true")==0)
							{
								current.setBoolState(4);
							}
						}
						else if(k.compareTo("captcha")==0)
						{
							if(basics.get(k).toLowerCase().trim().compareTo("true")==0)
							{
								current.setBoolState(5);
							}
						}
						else if(k.compareTo("lowercase")==0)
						{
							current.setCommitsize(Integer.parseInt(basics.get(k)));
						}
						else if(k.compareTo("getLinks")==0)
						{
							if(basics.get(k).toLowerCase().contains("true"))
							{
									current.setBoolState(0);
							}
						}
						else if(k.toLowerCase().compareTo("forceredirect")==0)
						{
								current.setForceredirect(basics.get(k));
						}
						else if(k.toLowerCase().compareTo("forcesecondredirect")==0)
						{
								current.setForcesecondredirect(basics.get(k));
						}
						else if(k.toLowerCase().compareTo("redirect")==0)
						{
							current.setRedirect_regex(basics.get(k));
						}
						else if(k.toLowerCase().compareTo("redirectprefix")==0)
						{
							current.setRedirect_prefix(basics.get(k).trim());
						}
						else if(k.compareTo("indiLinkRegex")==0)
						{
							current.setGetLinkRegex(basics.get(k).trim());
						}
						else if(k.toLowerCase().trim().contains("indilinkreplace"))
						{
							current.setIndilinkreplace(basics.get(k).trim());
						}
						else if(k.toLowerCase().trim().contains("indilinkprefix"))
						{
							current.setIndilinkprefix(basics.get(k).trim());
						}
						else if(k.toLowerCase().trim().contains("indilinksuffix"))
						{
							current.setIndilinksuffix(basics.get(k).trim());
						}
						else if(k.toLowerCase().contains("loopparams"))
						{
							if(basics.get(k).contains("true"))
							{
								current.setBoolState(17);
							}
						}
						else if(k.toLowerCase().contains("lpparamval"))
						{
							current.setLoopparam(basics.get(k));
						}
						else if(k.toLowerCase().contains("looptype"))
						{
							if(basics.get(k).toLowerCase().contains("get"))
							{
								current.setBoolState(18);
							}
						}
						else if(k.toLowerCase().contains("loopurl"))
						{
							//this is the base url, the parameters are used to allow the loop parameter to be added lazily (usually works as long as all params are in)
							current.setLoopurl(basics.get(k));
						}
					}
					
				}
				else if(s.toLowerCase().contains("headers")==true & s.trim().toLowerCase().contains("addheaders")==false & s.trim().toLowerCase().contains("loopheaders")==false)
				{
					current.setHeaders(indiMap.get(s));
				}
				else if(s.toLowerCase().contains("parameters")==true & s.trim().toLowerCase().contains("addparameters")== false & s.trim().toLowerCase().contains("loopparameters")==false) 
				{
					current.setParameters(indiMap.get(s));
				}
				else if(s.toLowerCase().contains("loopheaders")==true)
				{
					basics=indiMap.get(s);
					keys=basics.keySet();
					jsonString=null;
					
					for(String k: keys)
					{
						jsonString=(jsonString==null)?"{\""+k+"\":\""+basics.get(k)+"\"":jsonString+",\""+k+"\":\""+basics.get(k)+"\"";
					}
					
					if(jsonString != null)
					{
						jsonString+=",}";
					}
					
					current.setLoopheaders(jsonString);
				}
				else if(s.toLowerCase().contains("loopregexheaders")==true)
				{
					//encode regex headers in a json-ish string to save memory
					basics=indiMap.get(s);
					keys=basics.keySet();
					jsonString=null;
					
					for(String k: keys)
					{
						jsonString=(jsonString==null)?"{\""+k+"\":\""+basics.get(k)+"\"":jsonString+",\""+k+"\":\""+basics.get(k)+"\"";
					}
					
					if(jsonString != null)
					{
						jsonString+=",}";
					}

					current.setLoopregexheaders(jsonString);
				}
				else if(s.toLowerCase().contains("loopparameters"))
				{
					//encode loop parameters in a json-ish String to save memory
					basics=indiMap.get(s);
					keys=basics.keySet();
					jsonString=null;
					
					for(String k: keys)
					{
						jsonString=(jsonString==null)?"{\""+k+"\":\""+basics.get(k)+"\"":jsonString+",\""+k+"\":\""+basics.get(k)+"\"";
					}
					
					if(jsonString != null)
					{
						jsonString+=",}";
					}

					current.setLoopparams(jsonString);
				}
				else if(s.toLowerCase().contains("loopregexparams"))
				{
					//encode loop regex parameters in json-ish string to save memory
					basics=indiMap.get(s);
					keys=basics.keySet();
					jsonString=null;
					
					for(String k: keys)
					{
						jsonString=(jsonString==null)?"{\""+k+"\":\""+basics.get(k)+"\"":jsonString+",\""+k+"\":\""+basics.get(k)+"\"";
					}
					
					if(jsonString != null)
					{
						jsonString+=",}";
					}

					current.setLoopregexparams(jsonString);
				}
			
			}
			}

	}
	
	
	/**
	 * Checks and creates table and schema as necessary. Fails fatally if malformed data
	 * is presented. The table is {@table}.
	 */
	@PostConstruct
	public void checkTable()
	{
		String[] tablearr=table.split("\\.");
		String sql=null;
		
		if(this.gdt.checkSchema(tablearr[0])==false)
		{
			sql="CREATE SCHEMA "+tablearr[0];
			this.gdt.execute(sql);
		}
		
		if(this.gdt.checkTable(table, tablearr[0])==false)
		{
			if(key ==null){
				sql="CREATE TABLE "+table+"(indid SERIAL PRIMARY KEY NOT NULL, html text, link text, pullid text, datestamp text,offenderhash text)";
			}
			else{
				sql="CREATE TABLE "+table+"(indid SERIAL UNIQUE NOT NULL,"+key+" text PRIMARY KEY";
				
				if(key.compareTo("html") != 0){
					sql+=",html text";
				}
				else if(key.compareTo("link") != 0){
					sql+=",link text";
				}
				else if(key.compareTo("pullid")!= 0){
					sql+=",pullid text";
				}
				else if(key.compareTo("datestamp") != 0){
					sql+=", datestamp text";
				}
				else if(key.compareTo("offenderhash")!=0){
					sql+=", offenderhash text";
				}
				
				sql+=")";
			}
			this.gdt.execute(sql);
		}
	}
	
	
	/**
	 *Controls the pulling of each page
	 */
	public void pull()
	{
		//vars
		
		ArrayList<String> ulist=new ArrayList<String>();
		Map<String,String> temp;
		int j=1;
		Set<String> keys;
		ArrayList<String> names=new ArrayList<String>();
		ArrayList<String> vals=new ArrayList<String>();
		String htmlstr=null;
		String cookies=null;
		Map<String,Json> jtemp;
		String url=null;
		

		
		current=head;
		
		/*pull the individual pages*/
		if(current != null)
		{
			html_grab get=new html_grab();
			
			//get to the pullable page
			while(current!= null)
			{
				//get the page
				get.set_url(current.getUrl());
				log.info("Going for URL: "+get.getUrl());
				
				
				//set method
				if(current.getBoolState(10))
				{
					get.set_method("GET");
				}
				else{
					get.set_method("POST");
				}
				
				//check for parameters and set if found
				if(current.getBoolState(10)==false)
				{
					names.clear();
					vals.clear();
					temp=current.getParameters();
					keys=temp.keySet();
					
					//add normal parameters [should not need regex parameters here]
					for(String k:keys)
					{
						names.add(k);
						vals.add(k);
					}
					
					if(current.getLoopregexparams() != null)
					{
						jtemp=Json.read(current.getLoopregexparams()).asJsonMap();
						keys=temp.keySet();
						
						for(String k:keys)
						{

							if(jtemp.get(k).isString())
							{
								names.add(k);
								vals.add(jtemp.get(k).asString());
							}
			
						}
					}
					
					if(current.getBoolState(3))
					{
						names.add("__EVENTVALIDATION");
						vals.add(get.get_event_validation());
					}
					
					if(current.getBoolState(4))
					{
						names.add("javax.faces.ViewState");
						vals.add(get.get_server_faces());
					}
					
					if(current.getBoolState(2))
					{
						names.add("__VIEWSTATE");
						vals.add(get.get_viewstate());
					}
					
					
					get.encode_url_params(listtoArray(names), listtoArray(vals));
				}
				
				//set the headers
				names.clear();
				vals.clear();
				
				temp=current.getHeaders();
				keys=temp.keySet();
				
				for(String k: keys)
				{
					names.add(k);
					vals.add(k);
				}
				
				if(current.getLoopregexheaders() != null)
				{
					jtemp=Json.read(current.getLoopregexparams()).asJsonMap();
					keys=temp.keySet();
					
					for(String k:keys)
					{

						if(jtemp.get(k).isString())
						{
							names.add(k);
							vals.add(jtemp.get(k).asString());
						}
		
					}
				}
				
				
				if(current.getBoolState(10)==false)
				{
					names.add("Content-Length");
					vals.add(Integer.toString(get.get_param_size()));
				}
				
				get.set_header_names(listtoArray(names));
				get.set_values(listtoArray(vals));
				
				/*get the pages*/
				//check for ssl
				
				if(proxy)
				{
					if( proxyport != null & proxyhost != null)
					{
						get.setProxy(proxyhost, proxyport, current.getBoolState(1), proxyuser, proxypass);
					}
					else
					{
						if(current.getAuthority() != null)
						{
							get.setProxybySystem(current.getBoolState(1), true);
						}
						else
						{
							get.setProxybySystem(current.getBoolState(1), false);
						}
					}
				}
				
				try{
					Thread.sleep((int)(Math.random()*WAIT));
				}catch(InterruptedException e)
				{
						e.printStackTrace();
				}
				
				if(current.getBoolState(1))
				{
					if(current.getAuthority()!= null| current.getHost()!= null)
					{
						get.set_authority(current.getAuthority());
						get.set_host(current.getHost());
						cookies=get.get_secured();
						htmlstr=get.cookiegrab();
					}
					else
					{
						htmlstr=get.get_SSL();
						cookies=get.get_html();
					}
				}
				else
				{
					cookies=get.get_cookies();
					htmlstr=get.get_html();
				}
				
				if(current.getForceredirect()!= null | current.getRedirect_regex() != null)
				{
					
					get.set_method("GET");
					
					if(current.getRedirect_regex() == null){
						log.info("Setting Redirect");
						get.set_url(current.getForceredirect());
					}
					else{
						log.info("Searching for Redirect");
						Pattern p=Pattern.compile(current.getRedirect_regex());
						Matcher m=p.matcher(htmlstr);
						
						if(m.find()){
							log.info("Found "+m.group());
							if(current.getRedirect_prefix() != null){
								get.set_url(current.getRedirect_prefix().trim()+m.group().trim().replaceAll("amp;",""));
							}
							else{
								get.set_url(m.group().trim());
							}
						}
						else{
							log.info("Could not find redirect");
						}
						
					}
					get.set_url_params(null);
					
					if(current.getBoolState(1))
					{
						if(current.getAuthority()!= null| current.getHost()!= null)
						{
							get.set_authority(current.getAuthority());
							get.set_host(current.getHost());
							cookies=get.get_secured();
							htmlstr=get.cookiegrab();
						}
						else
						{
							htmlstr=get.get_SSL();
							cookies=get.get_html();
						}
					}
					else
					{
						cookies=get.get_cookies();
						htmlstr=get.get_html();
					}
					
					if(current.getForcesecondredirect() != null){
						get.set_url(current.getForcesecondredirect());
						
						if(current.getBoolState(1))
						{
							if(current.getAuthority()!= null| current.getHost()!= null)
							{
								get.set_authority(current.getAuthority());
								get.set_host(current.getHost());
								cookies=get.get_secured();
								htmlstr=get.cookiegrab();
							}
							else
							{
								htmlstr=get.get_SSL();
								cookies=get.get_html();
							}
						}
						else
						{
							cookies=get.get_cookies();
							htmlstr=get.get_html();
						}
					}
					
				}
				
				if(test)
				{
					System.out.println("*******************************************************HTML from "+get.getUrl()+" "+get.get_method()+"************************************************************\n");
					System.out.println(htmlstr+"\n\n\n************************************************************************END********************************************\n");
					System.out.println("\n\nCookies: "+get.cookiegrab());
				
				}
				
				//get the next pull object
				//this is much less complex than for the initial page since all of the pages i have run into beyond the 
				//interface normally just need a cookie
				if(current.hasNext())
				{
					
					//get the next nod
					current=current.getNext();
					
					//reset the method if necessary
					if((get.get_method().toLowerCase().compareTo("get")==0 & current.getBoolState(10)==false) | (get.get_method().toLowerCase().compareTo("post")==0 &current.getBoolState(10)==true))
					{
						
						if(current.getBoolState(10))
						{
							get.set_method("GET");
							get.set_url_params(null);
						}
						else
						{
							get.set_method("POST");
						}
					
						
					}
					
					//check to see if the current object is asking to use the links
					if(current.getUrl().compareTo("link")==0)
					{
						break;	
					}
				}
				else
				{
					current=null;
				}
			}
		
			
			if(current != null)
			{
				
			//set the appropriate parameters
			if(current.getBoolState(10)==false)
			{
				names.clear();
				vals.clear();
				temp=current.getParameters();
				keys=temp.keySet();
				
				for(String k: keys)
				{
					names.add(k);
					vals.add(temp.get(k));
				}
				
				if(current.getLoopregexparams() != null)
				{
					jtemp=Json.read(current.getLoopregexparams()).asJsonMap();
					keys=temp.keySet();
					
					for(String k:keys)
					{

						if(jtemp.get(k).isString())
						{
							names.add(k);
							vals.add(jtemp.get(k).asString());
						}
		
					}
				}
				
				if(current.getBoolState(3))
				{
					names.add("__EVENTVALIDATION");
					vals.add(get.get_event_validation());
				}
				
				if(current.getBoolState(4))
				{
					names.add("javax.faces.ViewState");
					vals.add(get.get_server_faces());
				}
				
				if(current.getBoolState(2))
				{
					names.add("__VIEWSTATE");
					vals.add(get.get_viewstate());
				}
				
				get.encode_url_params(listtoArray(names),listtoArray(vals));
			}
			
			temp=current.getHeaders();
			keys=temp.keySet();
			names.clear();
			vals.clear();
			
			for(String k: keys)
			{
				names.add(k);
				vals.add(temp.get(k));
			}
			

			if(current.getLoopregexheaders() != null)
			{
				jtemp=Json.read(current.getLoopregexparams()).asJsonMap();
				keys=temp.keySet();
					
				for(String k:keys)
				{

					if(jtemp.get(k).isString())
					{
						names.add(k);
						vals.add(jtemp.get(k).asString());
					}
		
				}
			}
			
			if(current.getBoolState(10)==false)
			{
				names.add("Length");
				vals.add(""+get.get_param_size());
			}
			
			get.set_header_names(listtoArray(names));
			get.set_values(listtoArray(vals));			
			
			if(current.getBoolState(1))
			{
				if(current.getAuthority()!= null| current.getHost() != null)
				{
					get.set_authority(current.getAuthority());
					get.set_host(current.getHost());
				}
			}
			
			String condition=" WHERE "+pullid+" >= "+Integer.toString(offset)+" AND "+pullid+" <= "+Integer.toString(offset+commit_size);
			
			if(extracondition != null){
				condition+=" "+extracondition;
			}
			
			log.info(query+condition);
			
			getFromDB(query+condition);
			long numpages=0;
			
			//iterate through the links while they are still being returned
			while(pages.size()>0)
			{
			log.info("Number of Link Containing Pages Found for Iteration "+j+": "+pages.size());
			//suggestion in an attempt to free up memory for consumption	
			Runtime.getRuntime().gc();
			System.gc();
			
			keys=pages.keySet();
			
			//iterate through the links using the current node as the source for getting pages
			for(String k:keys)
			{
				String[] links=pages.get(k).split(split);
				
				for(String l:links)
				{
					if(linksarr.contains(l)==false)
					{
						linksarr.add(l);
						url=l;
						if(linkprefix != null)
						{
							url=linkprefix.trim()+url.trim();
						}
					
						if(linksuffix != null)
						{
							url+=linksuffix.trim();
						}
					
						url=url.replaceAll("&amp;", "&");
					
						if(linktoreplace !=null & linkreplace !=null)
						{
							url.replaceAll(linktoreplace, linkreplace);
						}
					
						ulist.add((k+"~|"+url));
						
					}
				}
			}
			
			for(String item: ulist)
			{
				String[] urlarr=item.split("~\\|");

				if(linkasparam==false)
				{
					get.set_url(urlarr[1]);
				}
				else
				{
					get.set_url(linkasparamlink);
				}
				
				log.info("Going for Link: "+get.getUrl());
				
				htmlstr=null;
				if(proxy)
				{
					if( proxyport != null & proxyhost != null)
					{
						get.setProxy(proxyhost, proxyport, current.getBoolState(1), proxyuser, proxypass);
					}
					else
					{
						if(current.getAuthority() != null)
						{
							get.setProxybySystem(current.getBoolState(1), true);
						}
						else
						{
							get.setProxybySystem(current.getBoolState(1), false);
						}
					}
				}
				
				try{
					Thread.sleep((int)(Math.random()*WAIT));
				}catch(InterruptedException e)
				{
						e.printStackTrace();
				}
				
				//if the link is a parameter, then enter the parameter information
				if(linkasparam==true)
				{
					log.info("URL FOR PARAMETER "+get.getUrl());
					log.info("ADDING PARAMETER "+linkasparamname+" : "+urlarr[1]);
					current.addParameter(linkasparamname, urlarr[1]);
					
					//recreate params
					names.clear();
					vals.clear();
					
					for(String k : current.getParameters().keySet())
					{
						names.add(k);
						vals.add(current.getParameters().get(k));
					}
					
					//set the new parameters
					if(current.getBoolState(3))
					{
						names.add("__EVENTVALIDATION");
						vals.add(get.get_event_validation());
					}
					
					if(current.getBoolState(4))
					{
						names.add("javax.faces.ViewState");
						vals.add(get.get_server_faces());
					}
					
					if(current.getBoolState(2))
					{
						names.add("__VIEWSTATE");
						vals.add(get.get_viewstate());
					}
					
					
					get.encode_url_params(listtoArray(names), listtoArray(vals));
					names.clear();
					vals.clear();
					
				}
				
				//pull the page
				if(current.getBoolState(1))
				{
					if(current.getAuthority() != null | current.getHost() != null)
					{
						get.set_authority(current.getAuthority());
						get.set_host(current.getHost());
						cookies=get.get_secured();
						htmlstr=get.get_html();
					}
					else
					{
						htmlstr=get.get_SSL();
						cookies=get.cookiegrab();
					}
					
				}
				else
				{
					cookies=get.get_cookies();
					htmlstr=get.get_html();
				}
				
				if(htmlstr != null)
				{

					if(htmlstr.trim().length()>0)
					{
						pagehead=new IndividualPage();
						pagehead.setHtml(htmlstr.replaceAll("\t|\r|\r\n|\n|\'|\"", ""));
						
						if(genHash)
						{
							pagehead.setId(urlarr[0]);
						}
						else{
							String ident=urlarr[0].replaceAll("UQUQ\\d+UQUQ","");
							pagehead.setId(ident.trim());
						}
						
						pagehead.setLink(pages.get(urlarr[1]));
						pagehead.setDatestamp(Calendar.getInstance().getTime().toString());
						
						//pagehead.setId();
						
						ip.add(pagehead);
						numpages++;
						
						if(ip.size()>commit_size)
						{
							log.info("Sending to DB @ "+Calendar.getInstance().getTime().toString());
							posttoDB();
							ip.clear();
							pagehead=null;
							System.gc();
							Runtime.getRuntime().gc();
							log.info("Success: Returning to Loop @ "+Calendar.getInstance().getTime().toString());
						}
					}
				}
				
				if(test)
				{
					System.out.println("*******************************************************HTML from "+get.getUrl()+"************************************************************\n");
					System.out.println(htmlstr+"\n\n\n************************************************************************END********************************************\n");
				}
				
				if(linkasparam)
				{
					current.removeParameter(linkasparamname);
				}
			}
			
			
			//post to the database
			log.info("Sending to DB @ "+Calendar.getInstance().getTime().toString());
			posttoDB();
			
			log.info("Success: Initializing new Loop @ "+Calendar.getInstance().getTime().toString());
			ip.clear();
			pagehead=null;
			
			//suggestion to clear out the heavily burdensome nodes (unreliable but probably necessary also done at the beginning of the loop)
			Runtime.getRuntime().gc();
			System.gc();
			pages.clear();
			log.info("Preparing new Statement Iteration "+(j+1));
			//get more results for processing, smaller chunks are returne faster but take more iterations
			condition=" WHERE "+pullid+" >= "+Integer.toString(offset+(commit_size*j))+" AND "+pullid+" <= "+Integer.toString(offset+commit_size+(commit_size*j));
			
			if(extracondition != null){
				condition+=" "+extracondition;
			}
			
			log.info(query+condition);
			
			getFromDB(query+condition);
			j++;
			log.info("Number of New Link Containing Pages Found for Iteration "+j+": "+pages.size());
		}
			log.info("Total Pages Added:"+Long.toString(numpages));
			
		}
		}
		
		
	}

	
}
