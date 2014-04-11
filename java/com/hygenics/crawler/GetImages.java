package com.hygenics.crawler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import mjson.Json;

import com.hygenics.exceptions.MissingPropertyException;
import com.hygenics.exceptions.RepetitiveErrorException;
import com.hygenics.html.html_grab;
import com.hygenics.imaging.DownloadImage;
import com.hygenics.crawlerobjects.PullObject;

import javax.annotation.*;

/**
 * This class pulls images from a mapping and puts them into a table using the basic
 * jdbcconn (there is only one query needed).If an image requires a cookie, then allow for the 
 * images to be grabbed with the individual pages using the appropriate command.
 * 
 * An offenderhash and image link are required. The offenderhash is used in the metadata. You can
 * specify that the offenderhash be a foreign key constraint which may help in data deduplication.
 * Otherwise, stay away from constraints.
 * 
 * @author aevans
 *
 */
public class GetImages {
	
	private final Logger log=LoggerFactory.getLogger(MainApp.class);
	
	private boolean truncate=true;
	private boolean cascade=true;
	

	private boolean key=false;
	private String keyref;
	private String cannotcontain;
	private int nonimage=0;
	private boolean addimagenumber=false;
	private Map<String,String> headerMap;
	private int timeout=1000;
	private int SUCCESSIVEFAILURES=10;
	private int fails=0;
	
	
	private String extracondition;
	
	private double factor=1.25;
	private boolean average=false;
	private boolean addstamp=false;
	private boolean setMeta=true;
	
	private boolean proxyssl;
	private int WAIT=4;
	private boolean proxy;
	private String proxyport;
	private String proxyhost;
	private String proxypass;
	private String proxyuser;
	
	private PullObject current;
	private boolean SSL;
	private String fpath;
	private PullObject head;
	private getDAOTemplate template;
	private String select;
	private String column;
	private String idcolumn;
	private String imageprefix;
	private String pullid;
	private String imagesufix;
	private String targettable;

	private Map<String,String> imagedata;
	private Map<String,Map<String,String>> pullsq;

	private int commit_size=100;
	private int offset=0;
	
	public GetImages()
	{
		
	}
	

	
	
	public boolean isTruncate() {
		return truncate;
	}




	public void setTruncate(boolean truncate) {
		this.truncate = truncate;
	}




	public boolean isCascade() {
		return cascade;
	}




	public void setCascade(boolean cascade) {
		this.cascade = cascade;
	}




	public boolean isKey() {
		return key;
	}





	public void setKey(boolean key) {
		this.key = key;
	}





	public String getKeyref() {
		return keyref;
	}





	public void setKeyref(String keyref) {
		this.keyref = keyref;
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




	/**
	 * Check whether to add an image number to the end to ensure that images are not erased
	 * if multiple images exist for a single image url
	 * Learn to Swim
	 * @return
	 */
	public boolean isAddimagenumber() {
		return addimagenumber;
	}




	/**
	 * Sepcify whether to add an image number at the end of the url prior to the type spec to ensure that 
	 * images are not overridden for multiple offenders
	 * @param addimagenumber
	 */
	public void setAddimagenumber(boolean addimagenumber) {
		this.addimagenumber = addimagenumber;
	}





	public double getFactor() {
		return factor;
	}




	public void setFactor(double factor) {
		this.factor = factor;
	}




	public boolean isAverage() {
		return average;
	}




	public void setAverage(boolean average) {
		this.average = average;
	}




	public Map<String, String> getHeaderMap() {
		return headerMap;
	}




	public void setHeaderMap(Map<String, String> headerMap) {
		this.headerMap = headerMap;
	}




	public boolean isSetMeta() {
		return setMeta;
	}




	public void setSetMeta(boolean setMeta) {
		this.setMeta = setMeta;
	}




	public String getPullid() {
		return pullid;
	}




	public void setPullid(String pullid) {
		this.pullid = pullid;
	}




	public boolean isAddstamp() {
		return addstamp;
	}



	public void setAddstamp(boolean addstamp) {
		this.addstamp = addstamp;
	}



	public int getSUCCESSIVEFAILURES() {
		return SUCCESSIVEFAILURES;
	}



	public void setSUCCESSIVEFAILURES(int sUCCESSIVEFAILURES) {
		SUCCESSIVEFAILURES = sUCCESSIVEFAILURES;
	}



	public int getTimeout() {
		return timeout;
	}



	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}



	public int getWAIT() {
		return WAIT;
	}





	public void setWAIT(int wAIT) {
		WAIT = wAIT;
	}





	public boolean isProxyssl() {
		return proxyssl;
	}




	public void setProxyssl(boolean proxyssl) {
		this.proxyssl = proxyssl;
	}




	public String getProxyport() {
		return proxyport;
	}




	public void setProxyport(String proxyport) {
		this.proxyport = proxyport;
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




	public String getProxypass() {
		return proxypass;
	}




	public void setProxypass(String proxypass) {
		this.proxypass = proxypass;
	}




	public String getProxyuser() {
		return proxyuser;
	}




	public void setProxyuser(String proxyuser) {
		this.proxyuser = proxyuser;
	}




	public Map<String, Map<String, String>> getPullsq() {
		return pullsq;
	}




	public void setPullsq(Map<String, Map<String, String>> pullsq) {
		this.pullsq = pullsq;
	}




	public String getTargettable() {
		return targettable;
	}




	public void setTargettable(String targettable) {
		this.targettable = targettable;
	}




	public int getOffset() {
		return offset;
	}



	public void setOffset(int offset) {
		this.offset = offset;
	}



	public boolean isSSL() {
		return SSL;
	}



	public void setSSL(boolean sSL) {
		SSL = sSL;
	}



	public String getFpath() {
		return fpath;
	}



	public void setFpath(String fpath) {
		this.fpath = fpath.replaceAll("&amp;","");
	}



	public getDAOTemplate getTemplate() {
		return template;
	}


	@Required
	@Resource(name="getDAOTemplate")
	public void setTemplate(getDAOTemplate template) {
		this.template = template;
	}



	public String getSelect() {
		return select;
	}



	public void setSelect(String select) {
		this.select = select;
	}



	public String getColumn() {
		return column;
	}



	public void setColumn(String column) {
		this.column = column;
	}



	public String getIdcolumn() {
		return idcolumn;
	}



	public void setIdcolumn(String idcolumn) {
		this.idcolumn = idcolumn;
	}



	public String getImageprefix() {
		return imageprefix;
	}



	public void setImageprefix(String imageprefix) {
		this.imageprefix = imageprefix;
	}



	public String getImagesufix() {
		return imagesufix;
	}



	public void setImagesufix(String imagesufix) {
		this.imagesufix = imagesufix;
	}



	public Map<String, String> getImagedata() {
		return imagedata;
	}



	public void setImagedata(Map<String, String> imagedata) {
		this.imagedata = imagedata;
	}





	public int getCommit_size() {
		return commit_size;
	}



	public void setCommit_size(int commit_size) {
		this.commit_size = commit_size;
	}



	public void addtoDB(ArrayList<String> imgnames)
	{
		
		this.template.postJsonDatawithTable(imgnames);
	}
	
	public void getfromDB(String condition)
	{
		imagedata=this.template.getData((select+condition), idcolumn, column,false);
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
	public void CreateQueue()
	{
			//TODO instantiate the pull objects: 1-object per completely separate request (loops and searchterms causing loops limit number)
			String jsonString;
			head=null;
			current=null;
			Set<String> keys=pullsq.keySet();
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
					if(pullsq != null)
					{
						keys2=pullsq.get(s).keySet();
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
					Map<String,String> tempmap=pullsq.get(s);
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
					basics=pullsq.get(s);
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
					current.setHeaders(pullsq.get(s));
				}
				else if(s.toLowerCase().contains("parameters")==true & s.trim().toLowerCase().contains("addparameters")== false & s.trim().toLowerCase().contains("loopparameters")==false) 
				{
					current.setParameters(pullsq.get(s));
				}
				else if(s.toLowerCase().contains("loopheaders")==true)
				{
					basics=pullsq.get(s);
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
					basics=pullsq.get(s);
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
					basics=pullsq.get(s);
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
					basics=pullsq.get(s);
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
	
	public void checkTable()
	{
		String query="CREATE ";
		String[] tablearr=targettable.split("\\.");
		
		if(template.checkSchema(tablearr[0])==false)
		{
			template.createSchema(tablearr[0]);
		}
		
		
		if(template.checkTable(targettable, tablearr[0])==false)
		{
			if(key ==false){
				query+="TABLE "+targettable+" (imageid SERIAL PRIMARY KEY NOT NULL, imagepath text, offenderhash text,dbpath text)";
			}
			else{
				query+="TABLE "+targettable+" (imageid SERIAL PRIMARY KEY NOT NULL, imagepath text, offenderhash text FOREIGN KEY REFERENCES"+keyref+",dbpath text)";
			}
			template.execute(query);
		}
		else if(truncate){
			query="TRUNCATE "+targettable;
			
			if(cascade){
				query+=" CASCADE";
			}
			
			template.execute(query);
		}
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
	
	public void run()
	{
		//setup the pull
		String cookies=null;
		String htmlstr=null;
		PullObject po=head;
		String[] headers;
		String[] values;
		Map<String,String> params;
		ArrayList<String> imgnames=new ArrayList<String>(commit_size);
		html_grab get = null;
		ArrayList<String> names=new ArrayList<String>();
		ArrayList<String> vals=new ArrayList<String>();
		Map<String,String> temp;
		Map<String,Json> jtemp;
		
		//check to see if the targettable exists
		checkTable();
		
		if(pullsq != null)
		{
			CreateQueue();
			get=new html_grab();
		}

		String url;
		
		DownloadImage down=new DownloadImage();
		down.setTimeout(timeout);
		
		String condition=" WHERE "+pullid+" >= "+Integer.toString(offset)+" AND "+pullid+" <= "+Integer.toString(offset+commit_size);
		
		if(extracondition != null){
			condition+=" "+extracondition;
		}
		
		getfromDB(condition);
		int j=0;
		String json=null;
		int id=0;
		String idstr="";
		
		while(imagedata.size()>0)
		{
			id++;
			
			if(addimagenumber)
			{
				idstr="_PullId_"+Integer.toString(id);
			}
			
			Set<String> keys=imagedata.keySet();
			for(String k: keys)
			{
				url=imagedata.get(k).replaceAll("&amp;", "&");
			
				if(imageprefix != null)
				{
					url=imageprefix.trim()+url.trim();
				}
			
				if(imagesufix != null)
				{
					url+=imagesufix.trim();
				}
			
				down.setUrl(url);
			
				if(pullsq != null & get != null)
				{
					current=head;
					/*pull the individual pages*/
					if(current != null)
					{
						get.reset_cookies();
						
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
								for(String z:keys)
								{
									names.add(z);
									vals.add(z);
								}
								
								if(current.getLoopregexparams() != null)
								{
									jtemp=Json.read(current.getLoopregexparams()).asJsonMap();
									keys=temp.keySet();
									
									for(String z:keys)
									{

										if(jtemp.get(z).isString())
										{
											names.add(z);
											vals.add(jtemp.get(z).asString());
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
							
							for(String z: keys)
							{
								names.add(z);
								vals.add(z);
							}
							
							if(current.getLoopregexheaders() != null)
							{
								jtemp=Json.read(current.getLoopregexparams()).asJsonMap();
								keys=temp.keySet();
								
								for(String z:keys)
								{

									if(jtemp.get(z).isString())
									{
										names.add(z);
										vals.add(jtemp.get(z).asString());
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
								try{
									Thread.sleep((int)(Math.random()*WAIT));
								}catch(InterruptedException e)
								{
										e.printStackTrace();
								}
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
							
							current=current.getNext();
						}
					}
				}
				
				if(head != null)
				{
					po=head;
					
					while(po.getNext()!=null){
						po=po.getNext();
					}
					
					if(get != null)
					{
						down.set_fpath(fpath+k+idstr+".jpg");
						json="{\"table\":\""+targettable+"\",\"imagepath\":\""+k+idstr+".jpg\",\"offenderhash\":\""+k+"\",\"dbpath\":\""+fpath.replaceAll("\\\\","_")+"\"}";
						imgnames.add(json);
						params=po.getHeaders();
						headers=new String[(params.size()+1)];
						values=new String[(params.size()+1)];
						int i=0;
				
						for(String s:params.keySet())
						{
							headers[i]=s.trim();
							values[i]=params.get(s.trim());
							i++;
						}
				
						headers[i]="Cookie";
						values[i]=get.cookiegrab();
			
						try{
							Thread.sleep((int)(Math.random()*WAIT));
						}catch(InterruptedException e)
						{
								e.printStackTrace();
						}
						if(SSL==false)
						{
							if(down.get_url().trim().length()>0)
							{
								down.download_as_html(headers, values);
								
								if(average ==true)
								{
									down.reaverage(factor);
								}
								
								if(down.get_ibytes()!= null)
								{
									
									if(cannotcontain == null)
									{
										fails=0;
										down.setMetaData(("Offenderhash:"+k+"|Date:"+Calendar.getInstance().getTime().toString()));
									
										if(this.addstamp)
										{
											down.save((down.get_fpath().replaceAll(".jpg","")+Double.toString(Calendar.getInstance().getTimeInMillis()*(Math.random()/3)).replaceAll("\\.","")+".jpg").trim());
										}
										else
										{
											down.save(down.get_fpath());
										}
									}
									else if(down.get_ibytes_string().toLowerCase().contains(cannotcontain.trim())==false)
									{
										fails=0;
										down.setMetaData(("Offenderhash:"+k+"|Date:"+Calendar.getInstance().getTime().toString()));
									
										if(this.addstamp)
										{
											down.save((down.get_fpath().replaceAll(".jpg","")+Double.toString(Calendar.getInstance().getTimeInMillis()*(Math.random()/3)).replaceAll("\\.","")+".jpg").trim());
										}
										else
										{
											down.save(down.get_fpath());
										}
									}
									else
									{
										nonimage++;
										log.info("IMAGE PULL RETURNED NON-IMAGE "+nonimage);
									}
								}
								else
								{
									fails++;
									log.info("ERROR: Image Does Not Exist @ "+down.get_url());
									
									if(fails==SUCCESSIVEFAILURES)
									{
										try{
											throw new RepetitiveErrorException();
										}
										catch(RepetitiveErrorException e){
											e.printStackTrace();
											break;
										}
									}
								}
							}
						}
						else
						{
							if(down.get_url().trim().length()>0)
							{
								log.info("SSL URL "+url);
								if(imageprefix != null && imagedata.get(k).contains(imageprefix)==false)
								{
									down.download_ssl(headers, values, (imageprefix.trim()+imagedata.get(k).trim()).replaceAll("amp;|&amp;","&"));
								}
								else{
									down.download_ssl(headers, values, imagedata.get(k));
								}
								
								
								if(average==true)
								{
									down.reaverage(factor);
								}
								
								if(down.get_ibytes() != null)
								{
									if(cannotcontain==null)
									{
										fails=0;
										down.setMetaData(("Offenderhash:"+k+"|Date:"+Calendar.getInstance().getTime().toString()));
									
										if(this.addstamp)
										{
											down.save((down.get_fpath().replaceAll(".jpg","")+Double.toString(Calendar.getInstance().getTimeInMillis()*(Math.random()/3)).replaceAll("\\.","")+".jpg").trim());
										}
										else
										{
											log.info("Fpath: "+down.get_fpath());
											down.save(down.get_fpath());
										}
									}
									else if(down.get_ibytes_string().toLowerCase().contains(cannotcontain.trim())==false)
									{
										fails=0;
										down.setMetaData(("Offenderhash:"+k+"|Date:"+Calendar.getInstance().getTime().toString()));
									
										if(this.addstamp)
										{
											down.save((down.get_fpath().replaceAll(".jpg","")+Double.toString(Calendar.getInstance().getTimeInMillis()*(Math.random()/3)).replaceAll("\\.","")+".jpg").trim());
										}
										else
										{
											down.save(down.get_fpath());
										}
									}
									else{
										nonimage++;
										log.info("IMAGE PULL RETURNED NON-IMAGE "+nonimage);
									}
								}
								else
								{
									fails++;
									log.info("ERROR: Image Does Not Exist @ "+down.get_url());
									
									if(fails==SUCCESSIVEFAILURES)
									{
										try{
											throw new RepetitiveErrorException();
										}
										catch(RepetitiveErrorException e){
											e.printStackTrace();
											break;
										}
									}
								}
							}
						}
					}
					else
					{
						try{
							throw new MissingPropertyException("Pull Map or pull variable not specified");
						}catch(MissingPropertyException e)
						{
							e.printStackTrace();
						}
					}
				}
				else if(headerMap != null)
				{
					String[] headernames=new String[headerMap.size()];
					String[] headervals=new String[headerMap.size()];
					int i=0;
					
					for(String key: headerMap.keySet())
					{
						headernames[i]=key.trim();
						headervals[i]=headerMap.get(key).trim();
					}

					
					
					
					json="{\"table\":\""+targettable+"\",\"imagepath\":\""+k+idstr+".jpg\",\"offenderhash\":\""+k+"\",\"dbpath\":\""+fpath.replace("\\", "\\\\")+"\"}";
		
					imgnames.add(json);
					down.set_fpath(fpath+k+idstr+".jpg");
					
					if(imageprefix != null && url.contains(imageprefix)==false)
					{
						down.setUrl((imageprefix.trim()+url.trim()).replaceAll("amp;|&amp;","&"));
					}
					else{
						down.setUrl(url.replaceAll("amp;|&amp;","&"));
					}
					
					if(down.get_url().trim().length()>0 & headernames.length>0 & headervals.length>0)
					{
						try{
							Thread.sleep((int)(Math.random()*WAIT));
						}catch(InterruptedException e)
						{
								e.printStackTrace();
						}
						down.download_as_html(headernames, headervals);
						
						if( down.get_ibytes()!= null)
						{
							
							
							fails=0;
							log.info(down.get_fpath());
							if(setMeta==true)
							{
								down.setMetaData(("Offenderhash:"+k+"|Date:"+Calendar.getInstance().getTime().toString()));
							}
							
							if(average==true)
							{
								down.reaverage(factor);
							}
							
							down.save_bytes(down.get_fpath());
						}
						else
						{
							fails++;
							log.info("ERROR: Image Does Not Exist @ "+down.get_url());
							
							if(fails==SUCCESSIVEFAILURES)
							{
								try{
									throw new RepetitiveErrorException();
								}
								catch(RepetitiveErrorException e){
									e.printStackTrace();
									break;
								}
							}
						}
					}
				}
				else
				{
					if(proxy)
					{
						if( proxyport != null & proxyhost != null)
						{
							down.setProxy(proxyhost, proxyport, proxyssl, proxyuser, proxypass);
						}
					}
					
					url=imagedata.get(k);
					
					if(imageprefix != null)
					{
						url=imageprefix+url;
					}
					
					if(imagesufix != null)
					{
						url+=imagesufix;
					}
					
					json="{\"table\":\""+targettable+"\",\"imagepath\":\""+k+idstr+".jpg\",\"offenderhash\":\""+k+"\",\"dbpath\":\""+fpath.replaceAll("\\\\", "")+"\"}";
					imgnames.add(json);
					down.set_fpath(fpath+k+idstr+".jpg");	
					if(imageprefix != null && url.contains(imageprefix)==false)
					{
						down.setUrl((imageprefix.trim()+url.trim()).replaceAll("amp;|&amp;","&"));
					}
					else{
						down.setUrl(url.replaceAll("amp;|&amp;","&"));
					}
					
					if(down.get_url().trim().length()>0)
					{
						try{
							Thread.sleep((int)(Math.random()*WAIT));
						}catch(InterruptedException e)
						{
								e.printStackTrace();
						}
						down.download();
						
						if(average==true)
						{
							down.reaverage(factor);
						}
						
						if( down.get_ibytes()!= null)
						{
							
							if(cannotcontain==null)
							{
								fails=0;
								log.info(down.get_fpath());
								if(setMeta==true)
								{
									down.setMetaData(("Offenderhash:"+k+"|Date:"+Calendar.getInstance().getTime().toString()));
								}
							
								down.save_bytes(down.get_fpath());
							}
							else if(down.get_ibytes_string().toLowerCase().contains(cannotcontain.trim())==false)
							{
								fails=0;
								log.info(down.get_fpath());
								if(setMeta==true)
								{
									down.setMetaData(("Offenderhash:"+k+"|Date:"+Calendar.getInstance().getTime().toString()));
								}
							
								down.save_bytes(down.get_fpath());
							}
							else{
								nonimage++;
								log.info("IMAGE PULL RETURNED NON-IMAGE "+nonimage);
							}
						}
						else
						{
							fails++;
							log.info("ERROR: Image Does Not Exist @ "+down.get_url());
							
							if(fails==SUCCESSIVEFAILURES)
							{
								try{
									throw new RepetitiveErrorException();
								}
								catch(RepetitiveErrorException e){
									e.printStackTrace();
									break;
								}
							}
						}
					}
					
				}
			 
				if(imgnames.size()>=commit_size)
				{
					log.info("Adding to DB @ "+Calendar.getInstance().getTime().toString());
					addtoDB(imgnames);
					imgnames.clear();
					log.info("Posted to DB @ "+Calendar.getInstance().getTime().toString());
				}
				
				id++;
				
				if(addimagenumber)
				{
					idstr="_PullId_"+Integer.toString(id);
				}
			}
			//get the next round of images
			if(imgnames.size()>0)
			{
				log.info("Adding to DB @ "+Calendar.getInstance().getTime().toString());
				addtoDB(imgnames);
				imgnames.clear();
				log.info("Posted to DB @ "+Calendar.getInstance().getTime().toString());
			}
			
			imagedata.clear();
			j++;
			
			try{
				Thread.sleep((int)(Math.random()*10)*WAIT);
			}catch(InterruptedException e)
			{
					e.printStackTrace();
			}
			
			log.info("Getting More Image Paths @ "+Calendar.getInstance().getTime().toString());
			condition=" WHERE "+pullid+" >= "+Integer.toString(offset+(commit_size*j))+" AND "+pullid+" <= "+Integer.toString(offset+(commit_size*(j+1)));
			
			if(extracondition != null){
				condition+=extracondition;
			}
			
			log.info(condition);
			getfromDB(condition);
			log.info("Image Paths Obtained @ "+Calendar.getInstance().getTime().toString());
		}
	}

}
