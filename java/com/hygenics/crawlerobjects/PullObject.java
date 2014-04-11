package com.hygenics.crawlerobjects;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Map;


/**
 * Pull Object
 * @author aevans
 *
 */
public class PullObject implements Cloneable {

	//this is a hash of booleans that saves memory (rotate right is used to store the hash)
	private long hashbool=0;
	
	//url manipulations
	private String urlmanips;
	private String redirectinjection;
	private String getbeforeposturl;
	
	//PullObject Basics
	private String forceredirect;
	private String forcesecondredirect;
	private String getLinkRegex=null;
	private String url=null;
	private Map<String, String> headers;
	private Map<String, String> parameters;
	private Map<String,String> regexparams;
	private PullObject next=null;
	
	//loop url params
	private String loopURLManips;
	
	//loopregex maps
	private Map<String,String> iteratedloopregexparams;
	private Map<String,String> compactloopregexparams;
	private Map<String,String> iteratedloopparams;
	
	//FOR Captcha
	private String captchaRegex;
	private String captchaParam;
	private String captchaname;
	private String captchahash;
	private String hash;
	private String captchaPrefix;
	
	//for images if a link must be directly followed
	private String imagename;
	private String imageprefix;
	private String imagesuffix;
	private String ifpath;
	private String imageregexurls;
	private String iparamname;
	private String imageparams;
	private String imageheaders;
	
	//For Redirect
	private String redirect_regex;
	private String redirect_prefix;

	//For SSL
	private String host=null;
	private String authority=null;
	
	//Timeouts
	private int timeout=0;

	private int perPage=0;
	
	//LOOP BASICS
	private String loop_reg=null;
	private String loopparam=null;
	private String loopredirect;
	private String loopurl=null;
	
	//JSON STRINGS FOR LOOP
	private String loopheaders=null;
	private String loopparams=null;
	private String loopregexheaders=null;
	private String loopregexparams=null;

	//LINK REGEX
	private String indilinkreplace=null;
	private String indilinkprefix=null;
	private String indilinksuffix=null;
	
	private int commitsize=50;

	/**
	 * Empty Constructor
	 */
	public PullObject()
	{
	
	}
	

	
	public String getImageparams() {
		return imageparams;
	}




	public void setImageparams(String imageparams) {
		this.imageparams = imageparams;
	}




	public String getImageheaders() {
		return imageheaders;
	}




	public void setImageheaders(String imageheaders) {
		this.imageheaders = imageheaders;
	}




	public String getIparamname() {
		return iparamname;
	}




	public void setIparamname(String iparamname) {
		this.iparamname = iparamname;
	}




	public String getImagesuffix() {
		return imagesuffix;
	}





	public void setImagesuffix(String imagesuffix) {
		this.imagesuffix = imagesuffix;
	}





	public String getIfpath() {
		return ifpath;
	}





	public void setIfpath(String ifpath) {
		this.ifpath = ifpath;
	}





	public String getImageregexurls() {
		return imageregexurls;
	}





	public void setImageregexurls(String imageregexurls) {
		this.imageregexurls = imageregexurls;
	}





	public String getForcesecondredirect() {
		return forcesecondredirect;
	}





	public void setForcesecondredirect(String forcesecondredirect) {
		this.forcesecondredirect = forcesecondredirect;
	}





	public String getGetbeforeposturl() {
		return getbeforeposturl;
	}





	public void setGetbeforeposturl(String getbeforeposturl) {
		this.getbeforeposturl = getbeforeposturl;
	}





	public Map<String, String> getIteratedloopparams() {
		return iteratedloopparams;
	}





	public void setIteratedloopparams(Map<String, String> iteratedloopparams) {
		this.iteratedloopparams = iteratedloopparams;
	}





	public String getLoopredirect() {
		return loopredirect;
	}





	public void setLoopredirect(String loopredirect) {
		this.loopredirect = loopredirect;
	}





	public String getLoopURLManips() {
		return loopURLManips;
	}





	public void setLoopURLManips(String loopURLManips) {
		this.loopURLManips = loopURLManips;
	}





	public String getRedirectinjection() {
		return redirectinjection;
	}





	public void setRedirectinjection(String redirectinjection) {
		this.redirectinjection = redirectinjection;
	}





	public String getUrlmanips() {
		return urlmanips;
	}






	public void setUrlmanips(String urlmanips) {
		this.urlmanips = urlmanips;
	}






	public Map<String, String> getRegexparams() {
		return regexparams;
	}




	public void setRegexparams(Map<String, String> regexparams) {
		this.regexparams = regexparams;
	}




	public String getForceredirect() {
		return forceredirect;
	}




	public void setForceredirect(String forceredirect) {
		this.forceredirect = forceredirect;
	}


	


	public Map<String, String> getIteratedloopregexparams() {
		return iteratedloopregexparams;
	}




	public void setIteratedloopregexparams(
			Map<String, String> iteratedloopregexparams) {
		this.iteratedloopregexparams = iteratedloopregexparams;
	}




	public Map<String, String> getCompactloopregexparams() {
		return compactloopregexparams;
	}




	public void setCompactloopregexparams(Map<String, String> compactloopregexparams) {
		this.compactloopregexparams = compactloopregexparams;
	}




	public String getCaptchaPrefix() {
		return captchaPrefix;
	}




	public void setCaptchaPrefix(String captchaPrefix) {
		this.captchaPrefix = captchaPrefix;
	}




	public String getImagename() {
		return imagename;
	}




	public void setImagename(String imagename) {
		this.imagename = imagename;
	}




	public String getImageprefix() {
		return imageprefix;
	}




	public void setImageprefix(String imageprefix) {
		this.imageprefix = imageprefix;
	}




	public String getHash() {
		return hash;
	}




	public void setHash(String hash) {
		this.hash = hash;
	}




	public String getCaptchaname() {
		return captchaname;
	}




	public void setCaptchaname(String captchaname) {
		this.captchaname = captchaname;
	}




	public String getCaptchahash() {
		return captchahash;
	}




	public void setCaptchahash(String captchahash) {
		this.captchahash = captchahash;
	}




	public String getLoopurl() {
		return loopurl;
	}




	public void setLoopurl(String loopurl) {
		this.loopurl = loopurl;
	}




	public String getLoopparam() {
		return loopparam;
	}




	public void setLoopparam(String loopparam) {
		this.loopparam = loopparam;
	}




	public long getHashbool() {
		return hashbool;
	}




	public void setHashbool(long hashbool) {
		this.hashbool = hashbool;
	}




	public String getLoopregexparams() {
		return loopregexparams;
	}



	public void setLoopregexparams(String loopregexparams) {
		this.loopregexparams = loopregexparams;
	}



	public String getLoopregexheaders() {
		return loopregexheaders;
	}



	public String getCaptchaParam() {
		return captchaParam;
	}



	public void setCaptchaParam(String captchaParam) {
		this.captchaParam = captchaParam;
	}



	public String getCaptchaRegex() {
		return captchaRegex;
	}



	public void setCaptchaRegex(String captchaRegex) {
		this.captchaRegex = captchaRegex;
	}



	@Override
	/**
	 * Gets a deep clone of the PullObject, this is used very seldomnly if something is difficult to recreate and hardly any variables
	 * are used
	 */
	public Object clone() throws CloneNotSupportedException
	{
		PullObject clone=(PullObject) super.clone();
		
		clone.loopURLManips=(String) this.loopURLManips;
		clone.redirectinjection=(String) this.redirectinjection;
		clone.urlmanips=(String) this.urlmanips;
		clone.compactloopregexparams=(Map<String,String>) this.compactloopregexparams;
		clone.iteratedloopregexparams=(Map<String,String>) this.iteratedloopregexparams;
		clone.regexparams=(Map<String,String>) this.regexparams;
		clone.captchaPrefix=(String) this.captchaPrefix;
		clone.hash=(String) this.hash;
		clone.captchahash=(String) this.captchahash;
		clone.captchaname=(String) this.captchaname;
		clone.loopparam=(String) this.loopparam;
		clone.redirect_regex=(String)this.redirect_regex;
		clone.redirect_prefix=(String) this.redirect_prefix;
		clone.authority=(String)this.authority;
		clone.commitsize=(int)this.commitsize;
		clone.getLinkRegex=(String) this.getLinkRegex;
		clone.headers=(Map<String, String>)this.headers;
		clone.host=(String)this.host;
		clone.loop_reg=(String)this.loop_reg;
		clone.loopparams=(String)this.loopparams;
		clone.loopheaders=(String)this.loopheaders;
		clone.loopregexheaders=(String)this.loopregexheaders;
		clone.loopregexparams=(String) this.loopregexparams;
		clone.next=(PullObject)this.next;
		clone.parameters=(Map<String,String>)this.parameters;
		clone.perPage=(int)this.perPage;
		clone.timeout=(int) this.timeout;
		clone.url=(String) this.url;
		clone.indilinkreplace=(String) this.indilinkreplace;
		clone.indilinkprefix=(String) this.indilinkprefix;
		clone.indilinksuffix=(String) this.indilinksuffix;
		
		this.setNext(clone);
		
		return clone;
	}
	
	
	
	public String getLoopheaders()
	{
		return loopheaders;
	}
	
	public String getLoopparams() {
		return loopparams;
	}

	public void setLoopparams(String loopparams) {
		this.loopparams = loopparams;
	}

	public void setLoopheaders(String loopheaders) {
		this.loopheaders = loopheaders;
	}

	public void setLoopregexheaders(String loopregexheaders) {
		this.loopregexheaders = loopregexheaders;
	}

	public String getRedirect_prefix() {
		return redirect_prefix;
	}

	public void setRedirect_prefix(String redirect_prefix) {
		this.redirect_prefix = redirect_prefix;
	}

	//redirect regex
	public String getRedirect_regex() {
		return redirect_regex;
	}

	public void setRedirect_regex(String redirect_regex) {
		this.redirect_regex = redirect_regex;
	}

	//uses bit shifting (equivallent of taking >> and moving the bits falling off the end to the highest position)
	public boolean getBoolState(int i)
	{
		return BigInteger.valueOf(hashbool).testBit(i);
	}
	
	public void setBoolState(int i)
	{
		this.hashbool=BigInteger.valueOf(this.hashbool).setBit(i).longValue();
	}
	
	
	public String getIndilinkreplace() {
		return indilinkreplace;
	}

	public void setIndilinkreplace(String indilinkreplace) {
		this.indilinkreplace = indilinkreplace;
	}

	public String getIndilinkprefix() {
		return indilinkprefix;
	}

	public void setIndilinkprefix(String indilinkprefix) {
		this.indilinkprefix = indilinkprefix;
	}

	public String getIndilinksuffix() {
		return indilinksuffix;
	}

	public void setIndilinksuffix(String indilinksuffix) {
		this.indilinksuffix = indilinksuffix;
	}

	/**
	 * Return whether or not to get the link regex
	 * @return getLinkRegex
	 */
	public String getGetLinkRegex() {
		return getLinkRegex;
	}


	/**
	 * Set the Link Regex
	 * @param getLinkRegex
	 */
	public void setGetLinkRegex(String getLinkRegex) {
		this.getLinkRegex = getLinkRegex;
	}

	/**
	 * Get the CommitSize
	 * @return commitsize
	 */
	public int getCommitsize() {
		return commitsize;
	}

	/**
	 * Set the commit size
	 * @param commitsize
	 */
	public void setCommitsize(int commitsize) {
		this.commitsize = commitsize;
	}


	
	/**
	 * Get the Number of Results Per Loop Page
	 * @return perPage
	 */
	public int getPerPage() {
		return perPage;
	}


	/**
	 * Set the Number of Results Per Loop Page
	 * @param perPage
	 */
	public void setPerPage(int perPage) {
		this.perPage = perPage;
	}



	/**
	 * Get the Timeout
	 * @return timeout
	 */
	public int getTimeout() {
		return timeout;
	}


	/**
	 * Set the Timeout
	 * @param timeout
	 * @return timeout
	 */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}



	/**
	 * Get Host
	 * @return host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Set Host
	 * @param host
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * Get Authority
	 * @return authority
	 */
	public String getAuthority() {
		return authority;
	}

	/**
	 * Set Authority
	 * 
	 * @param authority
	 */
	public void setAuthority(String authority) {
		this.authority = authority;
	}
	
	/**
	 * Get Loop Regex
	 * @return
	 */
	public String getLoop_reg() {
		return loop_reg;
	}
	
	public void setLoop_reg(String reg)
	{
		loop_reg=reg;
	}
	
	
	/**
	 * Get Next node
	 * @return next
	 */
	public PullObject getNext() {
		return next;
	}

	/**
	 * Set Next Object
	 * @param next
	 */
	public void setNext(PullObject next) {
		this.next = next;
	}
	

	/**
	 * Get the Url
	 * @return
	 */
	public String getUrl() {
		return url;
	}

	
	/**
	 * Set the URL
	 * @param url
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * Get the Headers
	 * @return
	 */
	public Map<String,String> getHeaders() {
		return headers;
	}

	/**
	 * Set the Headers
	 * @param map
	 */
	public void setHeaders(Map<String, String> map) {
		this.headers = map;
	}

	
	/**
	 * Get the Parameters
	 * @return parameters
	 */
	public Map<String,String> getParameters() {
		return parameters;
	}

	/**
	 * Set the Parameters
	 * @param map
	 */
	public void setParameters(Map<String, String> map) {
		this.parameters = map;
	}
	
	
	/**
	 * Check for Next (returns a boolean)
	 * @return boolean
	 */
	public Boolean hasNext()
	{
		if(this.next != null)
		{
			return true;
		}
		
		return false;
	}

	/**
	 * Add a single header
	 * @param header
	 * @param value
	 */
	public void addHeader(String header,String value)
	{
		if(this.headers != null)
		{
			this.headers.put(header, value);
		}
	}
	
	/**
	 * Adds a single parameter
	 * @param parameter
	 * @param value
	 */
	public void addParameter(String parameter, String value)
	{
		if(this.parameters != null)
		{
			this.parameters.put(parameter, value);
		}
	}
	
	/**
	 * Remove a parameter
	 * @param parameter
	 */
	public void removeParameter(String parameter)
	{
		if(this.parameters != null)
		{
			if(this.parameters.containsKey(parameter))
			{
				this.parameters.remove(parameter);
			}
		}
	}
	
	/**
	 * Takes in a header mapping and set new headers
	 */
	public void addHeaders(ArrayList<String> headers, ArrayList<String> vals)
	{
		if(this.headers != null)
		{
			for(int i=0;i<headers.size();i++)
			{
				this.headers.put(headers.get(i), vals.get(i));
			}
		}
	}
	
	
	public void removeHeader(String header)
	{
		if(this.headers != null)
		{
			if(this.headers.containsKey(header))
			{
				this.headers.remove(header);
			}
		}
	}
	
	
	/**
	 * Takes in header parameters and adds them to the parameters
	 */
	public void addParameters(ArrayList<String> parameters, ArrayList<String> vals)
	{
		if(this.parameters != null)
		{
			for(int i=0;i<parameters.size();i++)
			{
				this.parameters.put(parameters.get(i), vals.get(i));
			}
		}
	}

}

