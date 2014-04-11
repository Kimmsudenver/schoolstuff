package com.hygenics.crawler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Time;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.HashSet;



import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import javax.annotation.*;

import mjson.Json;

import com.hygenics.exceptions.BadRegex;
import com.hygenics.exceptions.MismatchException;
import com.hygenics.exceptions.NoClassSpecified;
import com.hygenics.exceptions.NoHeaderException;
import com.hygenics.exceptions.NoParameterException;
import com.hygenics.exceptions.UserInputNotObtainedException;
import com.hygenics.html.*;
import com.hygenics.imaging.DownloadImage;
import com.hygenics.sftp.SFTP;
import com.hygenics.sort.Quicksort;
import com.hygenics.crawlerobjects.PostObjects;
import com.hygenics.crawlerobjects.PullObject;

/**
 * 
 * Pulls pages. This is the most Object intensive code becuase of its capabilities 
 * and attempt at usability
 * 
 * The code can:
 * 
 * A. pull SSL in multiple ways that allow for authentication (no all accepting agents) (2 ways)
 * B. maintain cookies
 * C. keep track of viewstates, event validation, event targets, server faces, and other tech that you regex
 * D. loop through results
 * E. add search terms 
 * F. accept user input that comes from a file 
 * G. Query a table to get multiple columns of input (join or simple where conditions)
 * H. Perform the typical Query for Results (POST and GET)
 * I. Add headers
 * J. Extract headers from a page and add them to the POST and Get Commands
 * 
 * Returns a list of pages specified as added to the list (looped pages are automatically added unless specified but others are not)
 * 
 * See the xml manual for clues on driving this class.
 * 
 * About 100-200 lines of code per feature 
 * 
 * Saves 200+ lines per crawler (200*n lines for number of pages)
 * 
 * For added security and abstraction, please do not hardcode any passwords, usernames, directories, and urls
 * 
 * 
 * @author asevans
 *
 */
public class GetPages{
	
	//stats
	private int pageforsearch=0;
	private int averagesearchlength = 0;
	
	private int totalpages=0;
	private int totalsearchlength=0;
	
	//other vars
	private boolean refreshstats=true;
	private boolean additionalviewstate=true;
	private boolean additionaleval=true;
	private ArrayList<String> ijsons;
	private html_grab get;
	
	private String persistantparamregex;
	private String persistantparam;
	private String persistantname;
	
	private boolean persistparams=false;
	private boolean incolumn=false;
	
	private String imagetable;
	
	private String key;
	
	private String beanname;

	private boolean encodeparam=true;
	
	private Map<String,String> captchaheaders;
	
	private Map<String,String> captcharedirectheaders;
	
	private String secondaryrespstring;
	
	private String additionalnarrow;
	private String additionalrenarrow;
	private int additionalmaxpos;
	private String additionaliterparams;
	
	private String recaptcharefreshurl;
	private String recaptcharedirecturlpre;
	private String recaptcharedirecturl;
	private String recaptchaparam;
	private String recaptchachallengename;
	private String recaptchaurlregex;
	
	private String captcharedirectregex;
	private String captcharedirectprefix;
	
	private boolean addextraurl=false;
	
	private boolean useaddparams;
	private String additionalredirectreplace;
	private String additionalredirectprefix;
	private String additionalredirectsuffix;
	private String additionalredirect;
	private String additionalparamurl;
	private Map<String,String> additionalparams;
	private Map<String,String> additionalregexparams;
	private String additionalurlmanips;
	
	private String looplinkpre;
	private String looplinkpost;
	
	private String cannotcontain="~|error|~";
	private String additionalnotcontain="viewstate MAC failed";
	private String additionalparamname;
	private String additionalpre;
	private String additionalpost;
	private String additionalurls;
	private String mustcontain="~none~";
	private boolean addparam=false;
	private boolean test=false;
	private String sftpdir;
	private String sftppass;
	private String sftpuser;
	private String sftpurl;
	
	private int WAIT=4;
	private boolean proxy=false;
	private String searchparamname;
	private static Logger log=LoggerFactory.getLogger(MainApp.class);
	private String proxyuser;
	private String proxypass; 
	private String proxyport;
	private String proxyhost;
	private String cresponse;
	private int pnum=0;
	private long timeout=5000;
	private getDAOTemplate gdt=null;
	private Map<String,String> loopparams;
	private ArrayList<String> terms;
	private ArrayList<String> names;
	private String loopparam;
	private String loopheader;
	private boolean loopterms;
	
	private String searchcolumn;
	private String searchsql;
	private String secondarysearchsql;
	private String secondarysearchparamname;
	private String secondarysearchcolumn;
	private String extracondition;
	
	private ArrayList<String> searchterms=new ArrayList<String>();
	private ArrayList<String> secondarysearchterms=new ArrayList<String>();
	
	private ArrayList<PostObjects> html;
	
	
	private String table=null;
	private int commit_size=100;
	
	private PullObject head;
	private PullObject current;
	private ArrayList<String> keys;
	

	private getDAOTemplate jdbcposter;
	

	private Map<String,Map<String,String>> pulls;
	
	public GetPages()
	{
		
	}
	
	
	
	
	public boolean isRefreshstats() {
		return refreshstats;
	}




	public void setRefreshstats(boolean refreshstats) {
		this.refreshstats = refreshstats;
	}




	public String getPersistantname() {
		return persistantname;
	}




	public void setPersistantname(String persistantname) {
		this.persistantname = persistantname;
	}




	public String getPersistantparamregex() {
		return persistantparamregex;
	}




	public void setPersistantparamregex(String persistantparamregex) {
		this.persistantparamregex = persistantparamregex;
	}




	public String getPersistantparam() {
		return persistantparam;
	}




	public void setPersistantparam(String persistantparam) {
		this.persistantparam = persistantparam;
	}




	public String getImagetable() {
		return imagetable;
	}





	public void setImagetable(String imagetable) {
		this.imagetable = imagetable;
	}





	public boolean isPersistparams() {
		return persistparams;
	}





	public void setPersistparams(boolean persistparams) {
		this.persistparams = persistparams;
	}





	public boolean isAdditionalviewstate() {
		return additionalviewstate;
	}





	public void setAdditionalviewstate(boolean additionalviewstate) {
		this.additionalviewstate = additionalviewstate;
	}





	public boolean isAdditionaleval() {
		return additionaleval;
	}





	public void setAdditionaleval(boolean additionaleval) {
		this.additionaleval = additionaleval;
	}





	public boolean isIncolumn() {
		return incolumn;
	}



	public void setIncolumn(boolean incolumn) {
		this.incolumn = incolumn;
	}



	public String getKey() {
		return key;
	}



	public void setKey(String key) {
		this.key = key;
	}



	public String getSecondaryrespstring() {
		return secondaryrespstring;
	}




	public void setSecondaryrespstring(String secondaryrespstring) {
		this.secondaryrespstring = secondaryrespstring;
	}




	public String getBeanname() {
		return beanname;
	}



	public void setBeanname(String beanname) {
		this.beanname = beanname;
	}



	public String getSecondarysearchcolumn() {
		return secondarysearchcolumn;
	}



	public void setSecondarysearchcolumn(String secondarysearchcolumn) {
		this.secondarysearchcolumn = secondarysearchcolumn;
	}



	public String getExtracondition() {
		return extracondition;
	}



	public void setExtracondition(String extracondition) {
		this.extracondition = extracondition;
	}



	public String getSecondarysearchsql() {
		return secondarysearchsql;
	}



	public void setSecondarysearchsql(String secondarysearchsql) {
		this.secondarysearchsql = secondarysearchsql;
	}



	public String getSecondarysearchparamname() {
		return secondarysearchparamname;
	}



	public void setSecondarysearchparamname(String secondarysearchparamname) {
		this.secondarysearchparamname = secondarysearchparamname;
	}



	public ArrayList<String> getSecondarysearchterms() {
		return secondarysearchterms;
	}



	public void setSecondarysearchterms(ArrayList<String> secondarysearchterms) {
		this.secondarysearchterms = secondarysearchterms;
	}



	/**
	 * Get the DAO template
	 * @return jdbcposter
	 */
	public getDAOTemplate getJdbcposter() {
		return jdbcposter;
	}
	
	
	/**
	 * Set the DAO template
	 * @param jdbcposter
	 */
	@Required
	@Resource(name="getDAOTemplate")
	public void setJdbcposter(getDAOTemplate jdbcposter) {
		this.jdbcposter = jdbcposter;
	}

	
	
	
	/**true
	 * Set Pull Mapping
	 * @param inmap
	 * 
	 */
	@Required
	public void setPulls(Map<String,Map<String,String>> inmap)
	{
		pulls=inmap;
	}
	
	/**
	 * get pull mapping
	 * @return pulls
	 */
	public Map<String,Map<String,String>> getPulls()
	{
		return pulls;
		
	}

	public boolean isAddextraurl() {
		return addextraurl;
	}

	public void setAddextraurl(boolean addextraurl) {
		this.addextraurl = addextraurl;
	}
	
	public boolean isEncodeparam() {
		return encodeparam;
	}

	public void setEncodeparam(boolean encodeparam) {
		this.encodeparam = encodeparam;
	}

	public String getAdditionalnarrow() {
		return additionalnarrow;
	}

	public void setAdditionalnarrow(String additionalnarrow) {
		this.additionalnarrow = additionalnarrow;
	}
	
	public String getAdditionalrenarrow() {
		return additionalrenarrow;
	}




	public void setAdditionalrenarrow(String additionalrenarrow) {
		this.additionalrenarrow = additionalrenarrow;
	}




	public int getAdditionalmaxpos() {
		return additionalmaxpos;
	}




	public void setAdditionalmaxpos(int additionalmaxpos) {
		this.additionalmaxpos = additionalmaxpos;
	}




	public String getAdditionaliterparams() {
		return additionaliterparams;
	}




	public void setAdditionaliterparams(String additionaliterparams) {
		this.additionaliterparams = additionaliterparams;
	}




	public String getRecaptcharefreshurl() {
		return recaptcharefreshurl;
	}



	public void setRecaptcharefreshurl(String recaptcharefreshurl) {
		this.recaptcharefreshurl = recaptcharefreshurl;
	}



	public String getRecaptcharedirecturlpre() {
		return recaptcharedirecturlpre;
	}




	public void setRecaptcharedirecturlpre(String recaptcharedirecturlpre) {
		this.recaptcharedirecturlpre = recaptcharedirecturlpre;
	}




	public String getRecaptcharedirecturl() {
		return recaptcharedirecturl;
	}


	public void setRecaptcharedirecturl(String recaptcharedirecturl) {
		this.recaptcharedirecturl = recaptcharedirecturl;
	}




	public String getRecaptchaparam() {
		return recaptchaparam;
	}




	public void setRecaptchaparam(String recaptchaparam) {
		this.recaptchaparam = recaptchaparam;
	}




	public String getRecaptchachallengename() {
		return recaptchachallengename;
	}



	public void setRecaptchachallengename(String recaptchachallengename) {
		this.recaptchachallengename = recaptchachallengename;
	}



	public String getRecaptchaurlregex() {
		return recaptchaurlregex;
	}



	public void setRecaptchaurlregex(String recaptchaurlregex) {
		this.recaptchaurlregex = recaptchaurlregex;
	}



	public Map<String, String> getCaptchaheaders() {
		return captchaheaders;
	}





	public void setCaptchaheaders(Map<String, String> captchaheaders) {
		this.captchaheaders = captchaheaders;
	}





	public Map<String, String> getCaptcharedirectheaders() {
		return captcharedirectheaders;
	}





	public void setCaptcharedirectheaders(Map<String, String> captcharedirectheaders) {
		this.captcharedirectheaders = captcharedirectheaders;
	}





	public String getCaptcharedirectregex() {
		return captcharedirectregex;
	}





	public void setCaptcharedirectregex(String captcharedirectregex) {
		this.captcharedirectregex = captcharedirectregex;
	}





	public String getCaptcharedirectprefix() {
		return captcharedirectprefix;
	}





	public void setCaptcharedirectprefix(String captcharedirectprefix) {
		this.captcharedirectprefix = captcharedirectprefix;
	}





	public boolean isUseaddparams() {
		return useaddparams;
	}





	public void setUseaddparams(boolean useaddparams) {
		this.useaddparams = useaddparams;
	}





	public String getAdditionalredirectreplace() {
		return additionalredirectreplace;
	}





	public void setAdditionalredirectreplace(String additionalredirectreplace) {
		this.additionalredirectreplace = additionalredirectreplace;
	}





	public String getAdditionalredirectprefix() {
		return additionalredirectprefix;
	}





	public void setAdditionalredirectprefix(String additionalredirectprefix) {
		this.additionalredirectprefix = additionalredirectprefix;
	}





	public String getAdditionalredirectsuffix() {
		return additionalredirectsuffix;
	}





	public void setAdditionalredirectsuffix(String additionalredirectsuffix) {
		this.additionalredirectsuffix = additionalredirectsuffix;
	}





	public String getAdditionalredirect() {
		return additionalredirect;
	}



	public void setAdditionalredirect(String additionalredirect) {
		this.additionalredirect = additionalredirect;
	}



	public String getAdditionalparamurl() {
		return additionalparamurl;
	}


	public void setAdditionalparamurl(String additionalparamurl) {
		this.additionalparamurl = additionalparamurl;
	}



	public String getAdditionalurlmanips() {
		return additionalurlmanips;
	}






	public void setAdditionalurlmanips(String additionalurlmanips) {
		this.additionalurlmanips = additionalurlmanips;
	}






	public String getLooplinkpre() {
		return looplinkpre;
	}





	public void setLooplinkpre(String looplinkpre) {
		this.looplinkpre = looplinkpre;
	}





	public String getLooplinkpost() {
		return looplinkpost;
	}





	public void setLooplinkpost(String looplinkpost) {
		this.looplinkpost = looplinkpost;
	}





	public String getCannotcontain() {
		return cannotcontain;
	}





	public void setCannotcontain(String cannotcontain) {
		this.cannotcontain = cannotcontain;
	}





	public String getAdditionalnotcontain() {
		return additionalnotcontain;
	}




	public void setAdditionalnotcontain(String additionalnotcontain) {
		this.additionalnotcontain = additionalnotcontain;
	}




	public String getAdditionalparamname() {
		return additionalparamname;
	}






	public void setAdditionalparamname(String additionalparamname) {
		this.additionalparamname = additionalparamname;
	}






	public Map<String, String> getAdditionalparams() {
		return additionalparams;
	}





	public void setAdditionalparams(Map<String, String> additionalparams) {
		this.additionalparams = additionalparams;
	}





	public Map<String, String> getAdditionalregexparams() {
		return additionalregexparams;
	}





	public void setAdditionalregexparams(Map<String, String> additionalregexparams) {
		this.additionalregexparams = additionalregexparams;
	}





	public String getAdditionalpre() {
		return additionalpre;
	}




	public void setAdditionalpre(String additionalpre) {
		this.additionalpre = additionalpre;
	}




	public String getAdditionalpost() {
		return additionalpost;
	}




	public void setAdditionalpost(String additionalpost) {
		this.additionalpost = additionalpost;
	}




	public String getAdditionalurls() {
		return additionalurls;
	}





	public void setAdditionalurls(String additionalurls) {
		this.additionalurls = additionalurls;
	}





	public String getMustcontain() {
		return mustcontain;
	}



	public void setMustcontain(String mustcontain) {
		this.mustcontain = mustcontain;
	}



	public boolean isTest() {
		return test;
	}


	public void setTest(boolean test) {
		this.test = test;
	}





	public String getSftpdir() {
		return sftpdir;
	}




	public void setSftpdir(String sftpdir) {
		this.sftpdir = new String(sftpdir.getBytes(),0,sftpdir.length());
	}




	public String getSftppass() {
		return sftppass;
	}




	public void setSftppass(String sftppass) {
		this.sftppass = new String(sftppass.getBytes(),0,sftppass.length());
	}




	public String getSftpuser() {
		return sftpuser;
	}




	public void setSftpuser(String sftpuser) {
		this.sftpuser = new String(sftpuser.getBytes(),0,sftpuser.length());
	}




	public String getSftpurl() {
		return sftpurl;
	}




	public void setSftpurl(String sftpurl) {
		this.sftpurl = sftpurl;
	}




	public int getWAIT() {
		return WAIT;
	}



	public void setWAIT(int wAIT) {
		WAIT = wAIT;
	}



	public String getSearchparamname() {
		return searchparamname;
	}





	public void setSearchparamname(String searchparamname) {
		this.searchparamname = searchparamname;
	}





	public boolean isProxy() {
		return proxy;
	}





	public void setProxy(boolean proxy) {
		this.proxy = proxy;
	}





	public String getProxyuser() {
		return proxyuser;
	}




	public void setProxyuser(String proxyuser) {
		this.proxyuser = proxyuser;
	}




	public String getProxypass() {
		return proxypass;
	}




	public void setProxypass(String proxypass) {
		this.proxypass = proxypass;
	}




	public String getProxyport() {
		return proxyport;
	}




	public void setProxyport(String proxyport) {
		this.proxyport = proxyport;
	}




	public String getProxyhost() {
		return proxyhost;
	}




	public void setProxyhost(String proxyhost) {
		this.proxyhost = proxyhost;
	}




	public String getSearchcolumn() {
		return searchcolumn;
	}

	
	public void setSearchcolumn(String searchcolumn) {
		this.searchcolumn = searchcolumn;
	}






	public String getSearchsql() {
		return searchsql;
	}






	public void setSearchsql(String searchsql) {
		this.searchsql = searchsql;
	}






	public ArrayList<String> getTerms() {
		return terms;
	}






	public void setTerms(ArrayList<String> terms) {
		this.terms = terms;
	}






	public ArrayList<String> getNames() {
		return names;
	}






	public void setNames(ArrayList<String> names) {
		this.names = names;
	}






	public long getTimeout() {
		return timeout;
	}




	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}




	public void setGdt(getDAOTemplate gdt) {
		this.gdt = gdt;
	}
	
	
	
	public void setLoopparams(Map<String,String> loopparams)
	{
		this.loopparams=loopparams;
	}
	
	public Map<String,String> getLoopparams()
	{
		return this.loopparams;
	}
	




	public String getLoopparam() {
		return loopparam;
	}




	public void setLoopparam(String loopparam) {
		this.loopparam = loopparam;
	}




	public String getLoopheader() {
		return loopheader;
	}




	public void setLoopheader(String loopheader) {
		this.loopheader = loopheader;
	}




	public boolean isLoopterms() {
		return loopterms;
	}




	public void setLoopterms(boolean loopterms) {
		this.loopterms = loopterms;
	}




	/**
	 * Get Table
	 * @return table
	 */
	public String getTable() {
		return table;
	}



	/**
	 * Set the Table
	 * @param table
	 */
	public void setTable(String table) {
		this.table = table;
	}



	/**
	 * Get the Commit Size
	 * @return commit size
	 */
	public int getCommit_size() {
		return commit_size;
	}



	/**
	 * Set the Commit Size
	 * @param commit_size
	 */
	public void setCommit_size(int commit_size) {
		this.commit_size = commit_size;
	}



	/**
	 * Get the Keys
	 * @return keys
	 */
	public ArrayList<String> getKeys() {
		return keys;
	}



	/**
	 * Set the Keys
	 * @param keys
	 */
	public void setKeys(ArrayList<String> keys) {
		this.keys = keys;
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
		gsh.setTemplate(this.jdbcposter);
		
		return gsh.run();
	}
	
	
	/**
	 * Add everything to the database via postobjects
	 * @param html
	 */
	public void addtoDB(ArrayList<PostObjects> inhtml)
	{
		//TODO add to DB via DAO--> commit size should be set to avoid Stack Overflow
		
		//check for null and collect stats
		for(int i=0;i<inhtml.size();i++){
			if(inhtml.get(i)==null)
			{
				inhtml.remove(i);
			}
			else{
				totalpages++;
				totalsearchlength+=inhtml.get(i).getHtml().length();
			}
		}
		
		//bacth update
		jdbcposter.BatchUpdatePostObjects(table,new String[]{"html","link","root","datestamp","offenderhash","additionalhtml"},inhtml);
		
		
		if(ijsons != null){
			if(ijsons.size()>0){
				jdbcposter.postJsonDatawithTable(ijsons);
				ijsons=new ArrayList<String>();
			}
		}
		
		Runtime.getRuntime().gc();
		System.gc();
	}
	
	/**
	 * Replace Escape Characters from XML 
	 * @param regex
	 * @return
	 */
	public String repEscape(String regex)
	{
		//TODO Escapse regex strings
		regex=regex.replaceAll("&quot;", "\"");
		regex=regex.replaceAll("&lt;","<");
		regex=regex.replaceAll("&gt;",">");
		regex=regex.replaceAll("&amp;", "&");
		regex=regex.replaceAll("&apos;","'");
		regex=regex.replaceAll("&#34;", "\"");
		regex=regex.replaceAll("&#60;","<");
		regex=regex.replaceAll("&#62;",">");
		regex=regex.replaceAll("&#38;", "&");
		regex=regex.replaceAll("&#39;","'");
		regex=regex.replaceAll("%3f", "?");
		regex=regex.replaceFirst("%3d","=");
		regex=regex.replaceAll("%26", "&");
		regex=regex.replaceAll("&amp", "&");
		regex=regex.replaceAll("&%3D","=");
		regex=regex.replaceAll("%2f", "/");
		
		return regex;
		
	}
	
	
	/**
	 * Encode a url
	 * 
	 * @param url
	 * @return
	 */
	public String repEncode(String url)
	{
		//TODO replace common url encodings
		url=url.replaceAll("&amp;", "&");
		url=url.replaceAll("%2f", "/");
		return url;
	}
	
	
	/**
	 * Turns and arraylist into a string array
	 * @param inlist
	 * @return return_arr
	 */
	public String[] arrListtoStringArr(ArrayList<String> inlist)
	{
		//TODO turn an efficient arraylist into a crappy string[]: remnants of pentaho
		String[] return_arr=new String[inlist.size()];
		
		for(int i=0;i<inlist.size();i++)
		{
			return_arr[i]=inlist.get(i).replaceAll("~", "");;
		}
		
		return return_arr;
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
			current.setNext(null);
		}
	}
	
	
	public void addAdditionalToArray(boolean incolumn,String additionalhtml,int pageid, String htmlstr,String ohash,String link){
		//TODO add additional html to array
				
				//iteratestats
				pageforsearch++;
				averagesearchlength+=additionalhtml.length();
				
				PostObjects obj;
				if(incolumn){
					obj=new PostObjects();
					obj.setHtml(additionalhtml.replaceAll("\t|;|\r|\n|%|\r\n|\"|'|$|\"", ""));
					obj.setLink(link);
					obj.setRoot(current.getUrl());
					obj.setDatestamp(DateFormat.getInstance().format(Calendar.getInstance().getTime()));
					obj.setId(ohash);
					obj.setAdditionalhtml(htmlstr.replaceAll("\t|;|\r|\n|%|\r\n|\"|'|$|\"", ""));
				}
				else{
					obj=new PostObjects();
					obj.setHtml(additionalhtml.replaceAll("\t|;|\r|\n|%|\r\n|\"|'|$|\"", "")+"~"+htmlstr.replaceAll("\t|;|\r|\n|%|\r\n|\"|'|$|\"", ""));
					obj.setLink(link);
					obj.setRoot(current.getUrl());
					obj.setDatestamp(DateFormat.getInstance().format(Calendar.getInstance().getTime()));
					obj.setId(ohash);
					obj.setAdditionalhtml("");
				}
				
				if((htmlstr.contains(mustcontain)==true | mustcontain.compareTo("~none~")==0) & htmlstr.contains(cannotcontain)==false)
				{
					html.add(obj);
					log.info("Added Page.");
				}
				else{
					log.info("PAGE NOT ADDED: Page Contained Unallowed Content or Did not Contain Appropriate Content.");
				}
			
			
					
				if(html.size()>commit_size){
					log.info("Adding to DB @ "+Calendar.getInstance().getTime().toString());
					
					//remove any nulls
				
					
					addtoDB(html);
					html.clear();
					log.info("Finished Adding to DB @ "+Calendar.getInstance().getTime().toString());
				}
					
	}
	
	
	public void timeout()
	{
		//TODO timeout between pages based on the timeout seed
		try{
			Thread.sleep((long)(timeout*Math.random()));
		}catch(InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Get the Terms from the Database
	 */
	public void pullTerms(boolean primary)
	{
		//TODO get the searchterms 
		if(primary){
			searchterms=this.jdbcposter.getArrayList(searchsql,searchcolumn);
		}
		else{
			secondarysearchterms=this.jdbcposter.getArrayList(secondarysearchsql, secondarysearchcolumn);
		}
	}
	
	/**
	 * Generate a CaptchaId for the SQL captchas table and successes table
	 * 
	 * @return captcha
	 */
	private String genCaptchaIdSQL()
	{
		long l=10L;
		return Long.toString((l*((int)Math.ceil(Math.random())))+Calendar.getInstance().getTimeInMillis());
	}
	
	/**
	 * Sets up the Queue from the Set Values and the Map 
	 * 
	 * Booleans (32 possible): a long value is set and bits are set to represent true. Pretty sure that java gets the bit by >> n|n-1 times
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
	 *20: LoopRegex parameters must be set
	 *21: Redirect Injection before url (if redirectinjection is not null)		
	 */
	@PostConstruct
	public void createQueue()
	{
		//TODO instantiate the pull objects: 1-object per completely separate request (loops and searchterms causing loops limit number)
		
		if(beanname != null)
			log.info("Instantiating Pulls Mapping for "+beanname);
		
		String jsonString;
		head=null;
		current=null;
		Set<String> keys=pulls.keySet();
		Set<String> keys2=null;
		PullObject obj=null;
		ArrayList<String> temp;
		Map<String,String> basics;
		
		for(String s: keys)
		{
			
			
			if(s.toLowerCase().contains("searchTableheaders")==true | s.toLowerCase().contains("searchTableparams")==true)
			{
				//add search headers and parameters that were from a SQL table
				
				if(gdt != null)
				{
					keys2=pulls.get(s).keySet();
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
				Map<String,String> tempmap=pulls.get(s);
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
				basics=pulls.get(s);
				keys2=basics.keySet();
				
				for(String k: keys2)
				{
		
					if(k.compareTo("url")==0)
					{
						current.setUrl(basics.get(k));
					}
					else if(k.compareTo("switchmethodonadditional")==0){
						if(basics.get(k).compareTo("true")==0){
							current.setBoolState(33);
						}
					}
					else if(k.compareTo("loopmaxpage")==0){
						if(basics.get(k).compareTo("true")==0){
							current.setBoolState(32);
						}
					}
					else if(k.compareTo("ifpath")==0){
						current.setIfpath(basics.get(k));
					}
					else if(k.compareTo("imageregexurls")==0){
						current.setImageregexurls(basics.get(k));
					}
					else if(k.compareTo("imagesuffix")==0){
						current.setImagesuffix(basics.get(k));
					}
					else if(k.compareTo("imageprefix")==0){
						current.setImageprefix(basics.get(k));
					}
					else if(k.compareTo("iheaders")==0){
						current.setImageheaders(basics.get(k));
					}
					else if(k.compareTo("iparams")==0){
						current.setImageparams(basics.get(k));
					}
					else if(k.compareTo("addparaminloop")==0){
						if(basics.get(k).compareTo("true")==0){
							current.setBoolState(28);
						}
					}
					else if(k.compareTo("addparambeforeloop")==0){
						if(basics.get(k).trim().compareTo("true")==0)
						{
							current.setBoolState(27);
						}
					}
					else if(k.compareTo("forcesecondredirect")==0){
						current.setForcesecondredirect(basics.get(k));
					}
					else if(k.trim().compareTo("getbeforepost")==0)
					{
						current.setBoolState(26);
					}
					else if(k.trim().compareTo("additionalbeforeloop")==0){
						current.setBoolState(29);
					}
					else if(k.toLowerCase().contains("loopurlmanips")==true)
					{
						current.setLoopURLManips(basics.get(k));
					}
					else if(k.compareTo("urlmanips")==0)
					{
						current.setUrlmanips(basics.get(k));
					}
					else if(k.toLowerCase().compareTo("redirectinjection")==0)
					{
						current.setRedirectinjection(basics.get(k));
					}
					else if(k.toLowerCase().compareTo("rinjectprior")==0)
					{
						if(basics.get(k).compareTo("true")==0)
						{
							current.setBoolState(21);
						}
					}
					else if(k.toLowerCase().compareTo("rinject")==0)
					{
						if(basics.get(k).compareTo("true")==0)
						{
							current.setBoolState(22);
						}
					}
					else if(k.compareTo("forceredirect")==0)
					{
						current.setForceredirect(basics.get(k));
					}
					else if(k.compareTo("captchahash")==0)
					{
						current.setCaptchahash(basics.get(k));						
					}
					else if(k.compareTo("captchaparam")==0)
					{
						current.setCaptchaParam(basics.get(k));
					}
					else if(k.compareTo("hash")==0)
					{
						current.setHash(basics.get(k));
					}
					else if(k.compareTo("captchaprefix")==0)
					{
						current.setCaptchaPrefix(basics.get(k));
					}
					else if(k.compareTo("captchaname")==0)
					{
						current.setCaptchaname(basics.get(k));
					}
					else if(k.compareTo("persistParam")==0){
						if(basics.get(k).compareTo("true")==0){
							current.setBoolState(30);
						}
					}
					else if(k.compareTo("loop")==0)
					{
					if(basics.get(k).contains("true"))
						{
							current.setBoolState(16);
						}
					}
					else if(k.toLowerCase().compareTo("loopint")==0)
					{
						if(basics.get(k).contains("true"))
						{
							current.setBoolState(15);
						}
					}
					else if(k.toLowerCase().contains("critical")==true)
					{
						if(basics.get(k).toLowerCase().contains("true"))
						{
							current.setBoolState(7);
						}
					}
					else if(k.toLowerCase().contains("captcharegex")==true)
					{
						current.setCaptchaRegex(basics.get(k));
					}
					else if(k.toLowerCase().contains("captchaparam")==true)
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
					else if(k.toLowerCase().contains("resetonrevert"))
					{
						if(basics.get(k).contains("true"))
						{
							current.setBoolState(24);
						}
					}
					else if(k.toLowerCase().contains("revertsearchtohead"))
					{
						if(basics.get(k).toLowerCase().contains("true"))
						{
							current.setBoolState(23);
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
					else if(k.compareTo("addtermbeforeloop")==0)
					{
						if(basics.get(k).toLowerCase().trim().compareTo("true")==0)
						{
							current.setBoolState(25);
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
					else if(k.toLowerCase().compareTo("perpage")==0)
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
								current.setBoolState(1);
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
					else if(k.toLowerCase().compareTo("indilinkregex")==0)
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
				current.setHeaders(pulls.get(s));
			}
			else if(s.toLowerCase().contains("parameters")==true & s.toLowerCase().contains("regexparameters")==false & s.trim().toLowerCase().contains("addparameters")== false & s.trim().toLowerCase().contains("loopparameters")==false) 
			{
				current.setParameters(pulls.get(s));
			}
			else if(s.toLowerCase().contains("regexparameters")==true)
			{
				current.setRegexparams(pulls.get(s));
			}
			else if(s.toLowerCase().contains("iteratedloopparams"))
			{
				current.setIteratedloopparams(pulls.get(s));
			}
			else if(s.toLowerCase().contains("loopheaders")==true)
			{
				basics=pulls.get(s);
				keys=basics.keySet();
				jsonString=null;
				
				for(String k: keys)
				{
					jsonString=(jsonString==null)?"{\""+k+"\":\""+basics.get(k)+"\"":jsonString+",\""+k+"\":\""+basics.get(k)+"\"";
				}
				
				
				if(jsonString != null)
				{
					jsonString+="}";
				}
				
				current.setLoopheaders(jsonString);
			}
			else if(s.toLowerCase().contains("loopregexheaders")==true)
			{
				//encode regex headers in a json-ish string to save memory
				basics=pulls.get(s);
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
				basics=pulls.get(s);
				keys=basics.keySet();
				jsonString=null;
				
				for(String k: keys)
				{
					jsonString=(jsonString==null)?"{\""+k+"\":\""+basics.get(k)+"\"":jsonString+",\""+k+"\":\""+basics.get(k)+"\"";
				}
				
				if(jsonString != null)
				{
					jsonString+="}";
				}
				
				current.setLoopparams(jsonString);
			}
			else if(s.toLowerCase().contains("iteratedloopregexparams"))
			{
				current.setIteratedloopregexparams(pulls.get(s));
			}
			else if(s.toLowerCase().contains("compactloopregexparams"))
			{
				current.setCompactloopregexparams(pulls.get(s));
			}
			else if(s.toLowerCase().contains("loopregexparams"))
			{
				//encode loop regex parameters in json-ish string to save memory
				basics=pulls.get(s);
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
		
		if(beanname != null)
			log.info("Finished Instantiating Pulls Mapping for "+beanname);
	}
	
	
	/**
	 * Set the proxy if requested
	 */
	private void setPullProxy()
	{
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
	}
	
	
	private String getPages(html_grab grab)
	{
		String htmlstr=null;
		String cookies=null;
		
		//perform the get or post and add to the appropriate section
		
		try{
			Thread.sleep((int)(Math.random()*WAIT));
		}catch(InterruptedException e)
		{
				e.printStackTrace();
		}

		if(grab.getUrl().contains("https:") & current.getAuthority() != null)
		{
			cookies=grab.get_secured();
			htmlstr=grab.get_html();
		}
		else if(grab.getUrl().contains("https:") & (current.getAuthority()==null))
		{
		
			htmlstr=grab.get_SSL();
			cookies=grab.get_cookies();
		}
		else
		{
			cookies=grab.get_cookies();
			htmlstr=grab.get_html();
		}
		
		return htmlstr;
	}
	
	/**
	 * Pull a webpage
	 * @return
	 */
	private String getPages()
	{
		String htmlstr=null;
		String cookies=null;
		
		//perform the get or post and add to the appropriate section
		
		try{
			Thread.sleep((int)(Math.random()*WAIT));
		}catch(InterruptedException e)
		{
				e.printStackTrace();
		}

		if(get.getUrl().contains("https:") & current.getAuthority() != null)
		{
			cookies=get.get_secured();
			htmlstr=get.get_html();
		}
		else if(get.getUrl().contains("https:") & (current.getAuthority()==null))
		{
		
			htmlstr=get.get_SSL();
			cookies=get.get_cookies();
		}
		else
		{
			cookies=get.get_cookies();
			htmlstr=get.get_html();
		}
		
		return htmlstr;
	}

	
	private void checkTable()
	{
		if(beanname != null)
			log.info("Checking Table for "+beanname);
		
		String[] tablearr=table.split("\\.");
		String sql=null;
		
		if(this.jdbcposter.checkSchema(tablearr[0]) ==false)
		{
			sql="CREATE SCHEMA "+tablearr[0];
			this.jdbcposter.execute(sql);
		}
		
		if(this.jdbcposter.checkTable(table, tablearr[0])==false)
		{

				if(key ==null){
					sql="CREATE TABLE "+table+"(pullid SERIAL PRIMARY KEY NOT NULL,html text, link text, root text, datestamp text,offenderhash text, additionalhtml text)";
				}
				else{
					sql="CREATE TABLE "+table+"(pullid SERIAL UNIQUE NOT NULL";
					
					if(key.compareTo("html") != 0){
						sql+=",html text";
					}
					else{
						sql+=","+key+" text PRIMARY KEY";
					}
					
					if(key.compareTo("link") != 0){
						sql+=",link text";
					}
					else{
						sql+=","+key+" text PRIMARY KEY";
					}
					
					if(key.compareTo("root")!= 0){
						sql+=",root text";
					}else{
						sql+=","+key+" text PRIMARY KEY";
					}
					 
					if(key.compareTo("datestamp") != 0){
						sql+=", datestamp text";
					}
					else{
						sql+=","+key+" text PRIMARY KEY";
					}
					
					
					if(key.compareTo("offenderhash")!= 0){
						sql+=", offenderhash text";
					}
					else{
						sql+=","+key+" text PRIMARY KEY";
					}
				
					if(key.compareTo("additionalhtml")!=0){
						sql+=", additionalhtml text";
					}
					else{
						sql+=","+key+" text PRIMARY KEY";
					}
					
					sql+=")";
			}
			this.jdbcposter.execute(sql);

			//check for the image table
			if(imagetable != null){
				
				if(this.jdbcposter.checkTable(imagetable, tablearr[0])==false){
					sql="Create Table "+imagetable+" imagepath text, dbpath text, offenderhash text, image_path text";
					
					this.jdbcposter.execute(sql);
				}
			}
			
			//check for the stats table
			if(this.jdbcposter.checkTable((tablearr[0].trim()+".searchstats").trim(), tablearr[0])==false){
				//create stats table if not existing
				sql="CREATE TABLE "+tablearr[0].trim()+".searchstats (term TEXT, pages INTEGER, avglength DOUBLE PRECISION, timestamp TEXT)";
				
				this.jdbcposter.execute(sql);
			}
			else if(refreshstats){
				//refresh stats table if exists
				log.info("Creating a Stats Table for SearchTerms and a Total. If no terms exist, only expect a total to be posted at the end. \n Be Warned, that total is for all pages added to the DB!");
				sql="DELETE FROM "+tablearr[0].trim()+".searchstats";
				this.jdbcposter.execute(sql);
			}
		}
		
		if(beanname != null){
			log.info("Searchterms attained for "+beanname);
		}
	}
	
	/**
	 * Get the Search terms Just after bean instantiation
	 */
	private void getSearchTerms(){
		//TODO call method to get searchterms (this was originally in the pulls method)
		
		/*Print beanname if given for logging purposes, PostConstruct ensures that the method is called just after instantiation so probably 
		*before programmatic access. I will continue to look for a way to use bean aware to get the bean id from inside the 
		class being instantiated. Singletons are static and consumer-producer is well known, so wtf. I think that spring uses threading/parallelism to instantiate things and I'd like it to stay that way.
		*/
		if(beanname != null){
			log.info("Checking for Searchterms for "+beanname);
		}
		
		
		//get the search terms
		if(searchsql != null)
		{
			if(beanname != null)
				log.info("Getting Search Terms for "+beanname);
				
			pullTerms(true);
					
					
			if(secondarysearchsql != null){
				if(beanname != null)
					log.info("Getting Secondary Search Terms for "+beanname);
				
				pullTerms(false);
				
				if(searchterms ==null){
					try{
						throw new NullPointerException("Searchterms are Missing!");
					}catch(NullPointerException e){
						e.printStackTrace();
					}
				}
				
				
				//perform some error checking as a precaution
				if(secondarysearchterms.size()>0 & searchterms != null){
					if(searchterms.size() != secondarysearchterms.size()){
						try{
							throw new MismatchException("Searchterm and Secondary Search Term Sizes Do Not Match!");
						}catch(MismatchException e){
							e.printStackTrace();
							System.exit(-1);
						}
					}
				}else{
					try{
							throw new NullPointerException("The secondary searchterms are missing!");

					}catch(NullPointerException e){
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	/**
	 * Manipulate the url
	 * 
	 * @param manips -manipulations as a Json String
	 * @param inurl - the input url
	 * @param htmlstr -the htmlstr
	 * @param searchterm -the searchterm
	 * @param i -iteration
	 * @return
	 */
	public String getUrlManips(String manips, String inurl,String htmlstr,String searchterm,int i,String secondarysearchterm)
	{
		String url=inurl;
		Map<String,Json> mp=Json.read(manips).asJsonMap();
	
		if(mp.keySet().size()>0)
		{
		
			for(String fix: mp.keySet())
			{
				if(fix.toLowerCase().contains("persistantparam")){
					url+=fix.replace("persistantparam", "")+"="+persistantparam;
				}
				else if(fix.toLowerCase().contains("REPTERM")){
					url=url.replaceAll("PSEARCHTERM",searchterm);
					url=url.replaceAll("SSEARCHTERM", secondarysearchterm);
				}
				else if(fix.toLowerCase().contains("replace")==false & fix.toLowerCase().contains("TOTALREG") & fix.toLowerCase().contains("add")==false)
				{
					Pattern p=Pattern.compile(mp.get(fix).asString());
					Matcher m=p.matcher(htmlstr.replaceAll("\t|\r|\r\n",""));
				
					if(fix.toLowerCase().contains("prefix"))
					{
						//add a suffix if the htmlstr is not null
						if(m.find())
						{
							url=m.group().trim()+url.trim();
						}
						else
						{
							try{
								throw new BadRegex("Bad Regex for URL Manipulation:\n");
							}catch(BadRegex e)
							{
								e.printStackTrace();
							}
						}
					}
					else if(fix.toLowerCase().contains("suffix"))
					{
						//add a prefix string if the htmlstr is not null
						if(m.find())
						{
							url+=m.group().trim();
						}
						else
						{
							try{
								throw new BadRegex("Bad Regex for URL Manipulation:\n");
							}catch(BadRegex e)
							{
								e.printStackTrace();
							}
						}
				
					}
				
				}
				else if(fix.toLowerCase().contains("searchterm") | fix.toLowerCase().contains("secondarysearchterm"))
				{
					//if the url manipulation is a searchterm, add the searchterm
					//parameter name is the map value
					if(searchterm != null){
						url+=mp.get(fix).asString().trim()+"=";
						url+=(fix.toLowerCase().contains("secondarysearchterm"))?searchterm:secondarysearchterm;
					}
				}
				else if(fix.contains("REGEX"))
				{
					//if the url manipulation is a regular expression, add the expression
					//parameter name needs to replace any REGEX appendings
					Pattern p2=Pattern.compile(mp.get(fix).asString().replaceAll("REGEX",""));
					Matcher m2=p2.matcher(htmlstr.replaceAll("\t|\r|\r\n|\n|\"",""));
				
					if(m2.find())
					{
						url+=fix.replaceAll("REGEX","")+"="+m2.group().trim().replaceAll("<.*?>","");
					}
					else{
						
						try{
							throw new BadRegex("Bad Regex in URL Manipulation. Regex was: "+mp.get(fix).asString());
						}catch(BadRegex e)
						{
							e.printStackTrace();
						}
					}
				
				}
				else if(fix.contains("add"))
				{
					//add a paramater string (multiple ways are provided for more
					//intuitive use
					url+=mp.get(fix).asString().trim();
				}
				else if(fix.contains("encodeadd"))
				{
					//add an encoded connector
					url+=encode(mp.get(fix).asString().trim());
				}
				else if(fix.contains("connector"))
				{
					//add an unencoded connector
					url+=mp.get(fix).asString().trim();
				}
				else if(fix.contains("encodeconnector"))
				{
					//encode and add a connector
					url+=encode(mp.get(fix).asString().trim());
				}
				else if(fix.contains("replace"))
				{
					//replace anything in the entire url
					String[] reps=mp.get(fix).asString().split("~");
					url.replaceAll(reps[0], reps[1]);
				}
				else if(fix.contains("ITERABLE"))
				{
					//add the integer as the loop param. in the case that words are before the iterable,
					//use itarable prior or iterablepost. this is a special case for the loop
					url+=fix.replaceAll("ITERABLE","")+"="+Integer.toString(i);
				}
				else if(fix.contains("ITERABLEPRIOR")){
					url+=fix.replaceAll("ITERABLE","")+"="+Integer.toString(i)+mp.get(fix).asString();
				}
				else if(fix.contains("ITERABLEPOST"))
				{
					url+=fix.replaceAll("ITERABLE","")+"="+mp.get(fix).asString()+Integer.toString(i);
				}
				else
				{
					//this is assumed to be a parameter to be added with value as 
					//the name and value as the value with an = sign separating the values
					url+=fix+"="+mp.get(fix).asString();
				}
			}
			mp=null;
			
		}
		else
		{
			try{
				throw new NullPointerException("Missing URL Manipulations or HTML:\n");
			}catch(NullPointerException e)
			{
				e.printStackTrace();
			}
		}
		
		return url;
	}
	
	
	private void printTestHtml(String htmlstr,String URL){
		System.out.println("*******************************************************Loop HTML from "+URL+"************************************************************\n");
		System.out.println(htmlstr+"\n\n\n************************************************************************END********************************************\n");

	}
	
	
	/**
	 * URL Encode string elements
	 * @param str
	 * @return
	 */
	private String encode(String str)
	{
		try {
			return URLEncoder.encode(str, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return str;
	}
	
	public void getImages(PullObject incurrent,String searchterm,String offenderhash,String inhtml){
		//TODO get images
		
		if(ijsons==null){
			ijsons=new ArrayList<String>();
		}
		
		log.info("Adding Images");
		//get parameters and headers
		String params=current.getImageparams();
		String headers=current.getImageheaders();
		String regex=current.getImageregexurls();
		String json;
		
		Pattern p=Pattern.compile(current.getImageregexurls());
		Matcher m=p.matcher(inhtml);
		
		int id=0;
		String url;
		
		if(m.find()){
			log.info("Image found: "+m.group());
			DownloadImage down=new DownloadImage();
			
			//get the url
			url=m.group();
			
			//iter and set params
			Set<String> keys;
			String[] names=null;
			String[] vals=null;
			
			String[] parnames=null;
			String[] parvals=null;
			
			//set any params
			if(current.getImageparams() != null){
				Map<String,Json> jmap=Json.read(current.getImageparams()).asJsonMap();
			
				keys=jmap.keySet();
				parnames=(current.getIparamname() != null)?new String[(keys.size()+1)]:new String[keys.size()];
				parvals=(current.getIparamname()!= null)?new String[(keys.size()+1)]:new String[keys.size()];
				
				int i=0;
				for(String k: keys){
					parnames[i]=k;
					parvals[i]=jmap.get(k).asString();
					i++;
				}
				
				if(current.getIparamname() != null){
					parnames[i]=current.getIparamname();
					parvals[i]=searchterm;
				}
				
			
			}
		
			//set any headers
			if(current.getImageheaders() != null){
				Map<String,Json> jmap=Json.read(current.getImageheaders()).asJsonMap();
				
				keys=jmap.keySet();
				
				names=new String[keys.size()];
				vals=new String[keys.size()];
				int i=0;
				for(String k: keys){
					names[i]=k;
					vals[i]=jmap.get(k).asString();
					i++;
				}
				
				
				down.set_header_names(names);
				down.set_values(vals);
			}
			
			while(url != null){
				if(current.getImageprefix() != null){
					url=current.getImageprefix()+url;
				}

				if(current.getImagesuffix() != null){
					url+=current.getImagesuffix();
				}
				
				
				log.info("Image URL is "+url);
				down.setUrl(url);
				down.set_fpath(current.getIfpath());
				down.setCookies(get.cookiegrab());
				
				
				if(url.contains("https:")==true){
					if(parnames != null){
						down.download_ssl_post(names, vals, parnames, parvals, url);
					}
					else{
						down.download_ssl(names, vals,url);
					}
				}
				else{
					if(parnames != null){
						down.download_html_post(names, vals, parnames, parvals, url);
					}
					else{
						down.download_as_html(names, vals);
					}
				}
					
				
				if(down.get_ibytes() != null){
					
					if(down.get_ibytes_string().trim().length()>0 & down.get_ibytes_string().trim().toLowerCase().compareTo("null")!=0){
						if(id>0){
							down.save(current.getIfpath()+offenderhash+"_"+id+".jpg");
							json="{\"table\":\""+table+"\",\"imagepath\":\""+offenderhash+"_"+id+".jpg\",\"offenderhash\":\""+offenderhash+"\",\"dbpath\":\""+current.getIfpath().replace("\\", "\\\\")+"\"}";
						
						}
						else{
							log.info(current.getIfpath()+offenderhash+".jpg");
							down.save(current.getIfpath()+offenderhash+".jpg");
							json="{\"table\":\""+imagetable+"\",\"imagepath\":\""+offenderhash+".jpg\",\"offenderhash\":\""+offenderhash+"\",\"dbpath\":\""+current.getIfpath().replace("\\", "\\\\")+"\"}";
						}
						id++;
						ijsons.add(json);
						log.info("Added");
					}
					else{
						log.warn("WARNING: Image was not writeable. Image Path: "+url);
					}
				}
				else{
					log.warn("WARNING: Image was not writeable. Image Path: "+url);
				}
				
				
				if(m.find()){
					url=m.group();
				}
				else{
					url=null;
				}
			}
		}
	}
	
	/**
	 * Handle the Page Pull (method acting like driver)
	 * 
	 * Steps in the pull method()
	 * 
	 * 1. initialize variables
	 * 2. while loop
	 * 		a. set node attributes
	 * 			i. set basic page pull data not related to parameters and values
	 * 			ii. manipulate url parameters
	 *  		iii. set url
	 *      b. set parameters
	 *      c. set values
	 *      d. proxy check
	 *      e. get page 
	 *      f. check and get redirect
	 *      ******g. if specified: get pages and images if specified
	 *      h. add page to post object if specified as critical
	 *      i. loop and post from loop as specified
	 *         i. set parameters
	 *         ii. get pages
	 *         iii. if previous captcha is found set to original captcha node and break the loop (searchterm is preserved)
	 *         ******iv. if specified: get pages and images
	 *                    -post to db if necessary  
	 *         v. see if links should be added
	 *         vi. post to db if necessary 
	 *         vii. increment and delete current searchterm
	 *      j. check for next node and delete current node if necessary 
	 *      k. change method 
	 *      l. reset cookies if necessary
	 *      m. get the captcha from the current page if specified 
	 *      		-captcha params
	 *      n. increment
	 * 3. post remaining pages
	 * 
	 * 
	 * NOTE: the log is maintained as to the current position in the loop. 
	 * WARNING: the dependency for this method is the hygenics.crawler.html class last gauged at 5500 lines of java.nio and java.net network calls
	 */
	public void pull()
	{
		//TODO pull the pages using the created queue
		log.info("Starting the Pull @"+Calendar.getInstance().getTime().toString());
		
		
		checkTable();
		getSearchTerms();
		String useurl=null;
		String searchterm=null;
		Quicksort<String> qs=new Quicksort<String>();
		String secondarysearchterm=null;
		int pageid=0;
		String redirectinjection = null;
		String prevurl;
		byte[] imgbytes=null;
		String captchaurl=null;
		HashSet<String> links=new HashSet<String>();
		String htmlstr=null;
		String url;
		int total=0;
		int i=0;
		String cookies=null;
		PostObjects obj=null;
		html=new ArrayList<PostObjects>();
		Map<String,String> pars=null;
		ArrayList<String> names=new ArrayList<String>();
		ArrayList<String> values=new ArrayList<String>();
		Set<String> keys=null;
		Iterator<String> it=null;
		String val=null;
		Map<String,Json> mp;
		String additionalhtml = null;
		
		
		
		
		log.info("Initializing Html Service for "+head.getUrl());
		//get head of queue
		current=head;
		
		//get a grab object 
		get=new html_grab();

		//reset the cookie manager
		get.reset_cookies();
		
		//statistics for the pull
		int searchsize=searchterms.size();
	
		
		int searchnodenum=0;
		
		//Iterate across the pull script and get the pages
		log.info("Iterating");
		while(current != null)
		{
			//search for a persistant param
			if(persistantparamregex != null & htmlstr != null){
				Pattern p2=Pattern.compile(persistantparamregex);
				Matcher m2=p2.matcher(htmlstr);
				
				if(m2.find()){
					persistantparam=m2.group();
				}
			}
			
			//project getting so big I would need to take time to read through
			//the whole thing. Instead, this will serve as a check
			if(current.getBoolState(12)){
				if(searchsize>0){
					log.info("Searchterms (Primary and Secondary) Size "+searchterms.size()+" Percent Complete: "+Double.toString((1-(double)(searchterms.size()/searchsize))*100));
				}
				log.info("Checking Searchterms");
				if(searchterms.size()==0)
				{
					log.info("Searchterms Complete @ "+Calendar.getInstance().getTime().toString()+" | "+Calendar.getInstance().getTimeInMillis());
					if(current.getNext()==null){
						break;
					}
					else
					{
						current=current.getNext();
					}
				}
				else if(searchterms.size()==searchsize & current.getBoolState(27)){
					searchterm=searchterms.get(0);
					if(current.getBoolState(10)==false){
						current.addParameter(searchparamname, searchterm);
						
						if(secondarysearchparamname != null){
							log.info("Secondary term is "+secondarysearchterms.size());
							current.addParameter(secondarysearchparamname, secondarysearchterm);
						}
					}
					log.info("Term is "+searchterm);
				}
			}
			
			pageid++;
			//timeout for a designated ammount of time
			timeout();
			
			if(current.getUrl() == null)
			{
				break;
			}
			
			log.info("Current URL: "+current.getUrl());
			//add the possible search term
			useurl=current.getUrl();
			
			//if a get is requested prior to a post, perform the get request
			if(current.getBoolState(26))
			{
				log.info("GETTING A PAGE PRIOR TO A POST");
				get.set_method("GET");
				get.set_url_params(null);
				
				if(current.getGetbeforeposturl()==null)
				{
					get.set_url(useurl);
				}
				else
				{
					get.set_url(current.getGetbeforeposturl());
				}
				
				//perform the get or post and add to the appropriate section
				if(proxy)
					setPullProxy();
				
				htmlstr=getPages();
				cookies=get.cookiegrab();
				
				//print obtained url if test is set
				if(test)
				{
					printTestHtml(htmlstr,get.getUrl());
				}
			}

			//check for post condition and whether there are parameters
			if(current.getBoolState(10)==false)
			{
				if(current.getParameters()==null)
				{
					try{
						throw new NoParameterException();
					}catch(NoParameterException e)
					{
						e.printStackTrace();
						System.exit(-1);
					}
				}
			}
			
			//check for error
			if(current.getHeaders()==null)
			{
				try{
					throw new NoHeaderException();
				}catch(NoHeaderException e)
				{
					e.printStackTrace();
					System.exit(-1);
				}
			}
			
			
			if(((current.getBoolState(13)==true & current.getBoolState(23)==false) | (addparam==true & current.getBoolState(13)==true)) & searchterms.size()>0)
			{
				//perform a direct addition to a url in the case that a search term is not to be encoded (this was a late addition sorry)
				log.info("Adding Search Term to URL");
				log.info("Search Terms Left "+(searchterms.size()-1));
				
				if(searchterms.size()>0)
				{
					if(searchterms.get(0) != null){
						log.info("Current Search Term is "+searchterms.get(0).trim());
					}
				}
				
				if(current.getUrl().contains(searchparamname))
				{
					//search paramname was found
					String repstring="(?<="+searchparamname+"=).*";
					useurl=current.getUrl().replaceAll(repstring, searchterms.get((0)));
					
					if(secondarysearchterms != null){
						if(secondarysearchterms.size()>0){
							log.info("Adding Secondary Search Term");
							useurl=useurl.replaceAll(secondaryrespstring, secondarysearchterms.get((0)));
						}
					}
				}
				else if(encodeparam==false){
					//user specified not to encode the parameter with &searchparamname=searchterm
					log.info("Not Encoding Parameter. Adding Term.");
					if(current.getUrl().contains("SEARCHTERM") & searchterms.size()>0){
						useurl=current.getUrl().replaceAll("SEARCHTERM",searchterms.get(0).trim());
					}
					else if(searchterms.size()>0){
						useurl=current.getUrl().replaceAll(searchterm,searchterms.get(0).trim());
					}
					
					if(secondarysearchterms.size()>0){
						log.info("Adding Secondary Search Term");
						
						if(useurl.contains("SEARCHSECONDTERM")){
							useurl=useurl.replaceAll("SEARCHSECONDTERM",secondarysearchterms.get(0).trim());
						}
						else{
							useurl=useurl.replaceAll(searchterm,secondarysearchterms.get(0).trim());
						}
					}
				}
				else
				{
					//otherwise encode to url in &param=value
					if(searchterms.size()>0){
						useurl=current.getUrl()+"&"+encode(searchparamname)+"="+encode(searchterms.get(0).trim());
					}
					
					if(secondarysearchterms.size()>0){
						log.info("Adding Secondary Search Term");
						useurl=useurl+"&"+encode(secondarysearchparamname)+"="+encode(secondarysearchterms.get(0).trim());
					}
				}
				
				//remove the secondary searchterm and set it as the current secondary term
				if(secondarysearchterms.size()>0){
					secondarysearchterm=secondarysearchterms.get(0).trim();
					secondarysearchterms.remove(0);
				}
				
				//remove the primary searchternm and set it as the current term
				searchterm=searchterms.get(0).trim();
				searchterms.remove(0);
			}
			else if(current.getBoolState(13)==false & current.getBoolState(23)==false & current.getBoolState(25)==true)
			{
				//add a search parameter
				log.info("ADDING SEARCH PARAMETER "+searchterms.get(0).trim());
				
				if(searchparamname != null & searchterms.size()>0 & encodeparam==true)
				{
					if(searchterm==null){
						useurl=current.getUrl().replaceAll("SEARCHTERM",searchterms.get(0).trim());
					}
					else{
						useurl=current.getUrl().replaceAll(searchterm,searchterms.get(0).trim());
					}
					
					if(searchterms.size()>0){
						searchterm=searchterms.get(0).trim();
					}
						
					//add a secondary searcterm if requested
					if(secondarysearchterms.size()>0)
					{
						secondarysearchterm=secondarysearchterms.get(0).trim();
						log.info("Adding Secondary Term "+secondarysearchterm);
						if(secondarysearchterm != null){
							useurl=useurl.replaceAll("SEARCHSECONDTERM", secondarysearchterm);
						}
						else{
							useurl=useurl.replaceAll(secondarysearchterm,secondarysearchterm);
						}
					}
				}
				else if(searchparamname != null & searchterms.size()>0 & encodeparam==false){
					
					if(searchterms.size()>0){
						searchterm=searchterms.get(0).trim();
					}
					
					useurl=current.getUrl().replaceAll("SEARCHTERM",searchterms.get(0).trim());
					
					if(secondarysearchterms.size()>0){
						secondarysearchterm=secondarysearchterms.get(0).trim();
						useurl=useurl.replaceAll("SEARCHSECONDTERM",secondarysearchterm);
					}
				}
				else
				{
					try{
						throw new NullPointerException("No Search Term for Adding. Program has not entered the loop");					
					}catch(NullPointerException e){
						e.printStackTrace();
					}
				}
				
				//if a request to add a secondary searchterm was received, remove the current term
				if(secondarysearchterms.size()>0)
				{
					secondarysearchterms.remove(0);
				}
				
				searchterms.remove(0);
			}
			
			//GET PREVIOUS URL
			prevurl=get.getUrl();
			
			//PREPARE URL
			url=useurl;
			
			
			//manipulate the url with regex or by adding encoded or unencoded parameters/connectors
			if(current.getUrlmanips() != null)
			{
				log.info("Manipulating URL");
				if(htmlstr != null)
				{
					if(secondarysearchterm != null){
						url=getUrlManips(current.getUrlmanips(), url, htmlstr, searchterm, pageid,secondarysearchterm);
					}
					else{
						url=getUrlManips(current.getUrlmanips(), url, htmlstr, searchterm, pageid,null);
					}
				}
			}
			
			
			//set the redirect injection before setting the url
			if(current.getRedirectinjection() != null & current.getBoolState(21)==true)
			{
				if(current.getRedirectinjection().contains("regexinject:"))
				{
					Pattern p=Pattern.compile(current.getRedirectinjection().replaceAll("regexinject:",""));
					Matcher m=p.matcher(htmlstr);
				
					if(m.find())
					{
						redirectinjection=m.group();
					}
				}
				else
				{
					redirectinjection=current.getRedirectinjection();
				}
			}
			
			//set url
			if(url != null)
			{
				
				
				log.info("Final URL to use is "+url);
				get.set_url(url);
			}
			else{
				log.info("Stuck on old url "+url);
			}
			
			//setting the redirect injection after setting the new url
			if(current.getRedirectinjection() != null & current.getBoolState(21)==false)
			{
				Pattern p=Pattern.compile(current.getRedirectinjection());
				Matcher m=p.matcher(htmlstr);
				
				if(m.find())
				{
					redirectinjection=m.group();
				}
			}
			
			//set the type
			if(current.getBoolState(10))
			{
				get.set_method("GET");
			}
			else
			{
				get.set_method("POST");
			}
			
			
			
			//check for post parameters
			if(get.get_method().compareTo("POST")==0)
			{
				//reset the names and parameters
				names.clear();
				values.clear();
				
				//set parameters
				pars=current.getParameters();
				
				keys=pars.keySet();
				
				it=keys.iterator();

				
				while(it.hasNext())
				{
					val=it.next().replaceAll("~", "");
					names.add(val);
					values.add(pars.get(val));
				}
				
				if(current.getBoolState(27)==true & current.getBoolState(12)==true){
					
					log.info("Adding Param "+searchterms.get(0).trim());
					if(searchparamname != null & names.contains(searchparamname)==false){
						
						if(searchterm ==null){
							names.add(searchparamname);
							values.add(searchterms.get(0).trim());
							searchterm=searchterms.get(0);
						}
						else{
							names.add(searchparamname);
							values.add(searchterm);
						}
					
						if(secondarysearchparamname != null){
							log.info("Adding Secondary Param "+secondarysearchterms.get(0).trim());
							
							if(secondarysearchterm == null){
								names.add(secondarysearchparamname);
								values.add(secondarysearchterms.get(0).trim());
							}
							else
							{
								names.add(secondarysearchparamname);
								values.add(secondarysearchterm);
							}
						}
					}
					
				}
				
				if(current.getBoolState(3))
				{
					log.info("ADDING EVENT VALIDATION CODE");
					if(get.get_html().contains("__EVENTVALIDATION"))
					{
						names.add("__EVENTVALIDATION");
						values.add(get.get_event_validation());
					}
					else
					{
						log.info("MISSING EVENT VALIDATION");
					}
				}
				
				if(current.getBoolState(2))
				{
					log.info("ADDING VIEWSTATE");
					if(get.get_html().contains("__VIEWSTATE"))
					{
						names.add("__VIEWSTATE");
						values.add(get.get_viewstate());
					}
					else
					{
						log.info("MISSING VIEWSTATE");
					}
				}
				
				
				if(current.getBoolState(4))
				{
					if(get.get_html().contains("javax.faces.ViewState"))
					{
						names.add("javax.faces.ViewState");
						values.add(get.get_server_faces());
					}
					else
					{
						log.info("MISSING SERVER FACES VIEWSTATE");
					}
				}
				
				//add any captcha params
				if(recaptchachallengename != null & recaptchaparam != null)
				{
					names.add(recaptchachallengename);
					values.add(recaptchaparam);
				}
				
				//grab any regular expression based parameters
				if(current.getRegexparams() !=null)
				{
					log.info("ADDING REGEX PARAMETERS");
					for(String param: current.getRegexparams().keySet())
					{
						log.info("Param: "+param+" Regex:"+current.getRegexparams().get(param));
						//do not move inside of the find segment to ensure less intrusive behavior
						
						if(param.contains("ALLREGES"))
						{
							Pattern p=Pattern.compile(current.getRegexparams().get(param));
							Matcher m=p.matcher(htmlstr);
							int f=1;
							
							while(m.find()){	
								log.info("ADDING REGEX PARAM: "+param+" VALUE:"+m.group()+" NUM: "+f);
								names.add(param.replaceAll("ALLREGES","").trim());
								values.add(m.group().trim());
								f++;
							}
						}
						else{
							names.add(param.trim());
						
						
							Pattern p=Pattern.compile(current.getRegexparams().get(param));
							Matcher m=p.matcher(htmlstr);
						
							if(m.find())
							{	
								log.info("ADDING REGEX PARAM: "+param+" VALUE:"+m.group());
								values.add(m.group().trim());
							}
							else
							{
								try{
									log.info("ERROR WITH REGEX PARAM");
									throw new BadRegex("Cannot Find Regex Parameter! \n");
								}catch(BadRegex e)
								{
									e.printStackTrace();
									System.exit(-1);
								}
							}
						}
					}
				}
				

				//encode
				get.set_url_params(null);
				get.encode_url_params(arrListtoStringArr(names),arrListtoStringArr(values));
				
				if(url.contains("REPsearchtermREP")){
					log.info("Attempting to Replace Term in URL");
					
		
					if(searchterm !=null){
						log.info("Replacing for term: "+searchterm);
						url=url.replaceAll("REPsearchtermREP", searchterm);
					}
					if(searchterms.size()>0){
						log.info("Replacing for term in array: "+searchterms.get(0));
						url=url.replaceAll("REPsearchtermREP", searchterms.get(0));
					}
					else if(current.getParameters().get(searchparamname)!=null){
						log.info("Getting for Parameter: "+current.getParameters().get(searchparamname).trim());
						url=url.replaceAll("REPsearchtermREP", current.getParameters().get(searchparamname).trim());
					}
					get.set_url(url);
					log.info("Replaced Term in Url As well");
					log.info("New URL is: "+url);
				}
				
				log.info("NEW PARAMS: "+get.get_url_params());
				recaptchaparam=null;
			}
			
			//reset the names and parameters
			names.clear();
			values.clear();
			
			//set headers if necessary 
			pars=current.getHeaders();
			
			//get keyset
			keys=pars.keySet();
			//iterate across the keyset
			it=keys.iterator();
			
			val=null;

			while(it.hasNext())
			{
				
				val=it.next().replaceAll("~","");
				
				names.add(val);
			
				
				if(pars.get(val).contains("addprevurl"))
				{
					values.add(prevurl);
				}
				else
				{
					values.add(pars.get(val));
				}
			}
			
			
			
			
			
			if(get.get_method().compareTo("POST")==0)
			{
				log.info("PARAMS: "+get.get_url_params());
				names.add("Content-Length");
				values.add(""+get.get_param_size());
			}
			
		
			
			if(get.cookiegrab() != null)
			{
				names.add("Cookie");
				values.add(cookies);
			}
			
			//set headers
			get.set_header_names(arrListtoStringArr(names));
			get.set_values(arrListtoStringArr(values));
			
			if(current.getTimeout() != 0)
			{
				get.set_timeout(current.getTimeout());
			}
			
			
			//perform the get or post and add to the appropriate section
			if(proxy)
			{
				setPullProxy();
			}
			
			try{
				Thread.sleep((int)(Math.random()*WAIT));
			}catch(InterruptedException e)
			{
					e.printStackTrace();
			}
			
			if(current.getBoolState(1)==true & (current.getAuthority()==null & current.getHost() == null))
			{
				htmlstr=get.get_SSL();
				cookies=get.cookiegrab();
				
				if(persistantparamregex != null){
					Pattern p2=Pattern.compile(persistantparamregex);
					Matcher m2=p2.matcher(htmlstr);
					
					if(m2.find()){
						persistantparam=m2.group();
					}
				}
				
				//redirect if necessary
				if(current.getForceredirect() != null)
				{
					if(test){
						printTestHtml(htmlstr, get.getUrl());
					}
					
					if(current.getForceredirect().contains("REPsearchtermREP"))
					{
						//check for an injection in the redirect
						if(current.getBoolState(22))
						{
							if(searchterms.size()>0){
								
							if(searchterms.get(0) != null){
							if(current.getBoolState(21))
							{
								get.set_url((redirectinjection.trim()+current.getForceredirect().replaceAll("REPsearchtermREP", searchterms.get(0).trim())));
							
								if(secondarysearchterms.size()>0){
									get.set_url((redirectinjection.trim()+current.getForceredirect().replaceAll("REPsecondarysearchtermREP", secondarysearchterms.get(0).trim())));
								}
							}
							else
							{
								get.set_url((current.getForceredirect().replaceAll("REPsearchtermREP", searchterms.get(0).trim()).trim()+redirectinjection.trim()));
								
								if(secondarysearchterms.size()>0){
									get.set_url((current.getForceredirect().replaceAll("REPsecondarysearchtermREP", secondarysearchterms.get(0).trim())));
								}
							}
							
							}
							
							}
							
						}
						else
						{
							if(searchterms.size()>0)
							{
								
								if(searchterms.get(0) != null){
									get.set_url(current.getForceredirect().replaceAll("REPsearchtermREP", searchterms.get(0).trim()));
							
									if(secondarysearchterms.size()>0){
										get.set_url((current.getForceredirect().replaceAll("REPsecondarysearchtermREP", secondarysearchterms.get(0).trim())));
									}
								}
							}
						}
					}
					else
					{
						get.set_url(current.getForceredirect());
					}
					
					get.set_method("GET");
					get.set_url_params(null);
					
					if(get.getUrl().indexOf("https:")==0)
					{
						htmlstr=get.get_SSL();
						cookies=get.cookiegrab();
					}
					else{
						cookies=get.get_cookies();
						htmlstr=get.get_html();
					}
					
					if(test){
						printTestHtml(htmlstr,get.getUrl());
					}
					
					if(current.getForcesecondredirect() != null)
					{
						log.info("Chaining to a Second Redirect at "+current.getForcesecondredirect());
						
						//replace a search term if it exists
						if(current.getForcesecondredirect().contains("REPsearchtermREP"))
						{
							log.info("TERM IS "+searchterms.get(0).trim()+" "+searchterms.size());
							if(searchterms.size()>0)
							{
								
								if(searchterms.get(0) != null){
									get.set_url(current.getForcesecondredirect().replaceAll("REPsearchtermREP", searchterms.get(0).trim()));
							
									if(secondarysearchterms.size()>0){
										get.set_url(current.getForcesecondredirect().replaceAll("REPsecondarysearchtermREP", secondarysearchterms.get(0).trim()));
									}
								}
							}
						}
						else{
							get.set_url(current.getForcesecondredirect());
						}
						
						htmlstr=get.get_SSL();
						cookies=get.cookiegrab();
						
						if(persistantparamregex != null){
							Pattern p2=Pattern.compile(persistantparamregex);
							Matcher m2=p2.matcher(htmlstr);
							
							if(m2.find()){
								persistantparam=m2.group();
							}
						}
						
						if(test){
							printTestHtml(htmlstr,get.getUrl());
						}
					}
				}
			}
			else if(current.getBoolState(1)==true & (current.getAuthority() != null | current.getHost() != null))
			{
		
				get.set_authority(current.getAuthority());
				get.set_host(current.getHost());
				
				cookies=get.get_secured();
				htmlstr=get.get_html();
				
				if(persistantparamregex != null){
					Pattern p2=Pattern.compile(persistantparamregex);
					Matcher m2=p2.matcher(htmlstr);
					
					if(m2.find()){
						persistantparam=m2.group();
					}
				}
				
				//redirect if necessary: only performs a GET request (running low on memory)
				if(current.getForceredirect() != null)
				{
					//check for an injection in the redirect
					
					if(searchterms.contains("REPsearchtermREP"))
					{
						if(current.getBoolState(22))
						{
							if(current.getBoolState(21))
							{
								get.set_url((redirectinjection.trim()+current.getForceredirect().replaceAll("REPsearchtermREP", searchterms.get(0).trim())));
								
								//add a secondary term if requested
								if(secondarysearchterms.size()>0){
									if(searchterms.size()>0)
									{
										
										if(searchterms.get(0) != null){
											get.set_url((redirectinjection.trim()+current.getForceredirect().replaceAll("REPsecondarysearchtermREP", secondarysearchterms.get(0).trim())));
										}
									}
								}
							}
							else
							{
								get.set_url((current.getForceredirect().replaceAll("REPsearchtermREP", searchterms.get(0).trim()).trim()+redirectinjection.trim()));
								
								if(secondarysearchterms.size()>0){
									if(searchterms.size()>0)
									{
										
										if(searchterms.get(0) != null){
											get.set_url((current.getForceredirect().replaceAll("REPsecondarysearchtermREP", secondarysearchterms.get(0).trim()).trim()+redirectinjection.trim()));
										}
									}
								}
							}
						}
						else
						{
							get.set_url(current.getForceredirect().replaceAll("REPsearchtermREP", searchterms.get(0).trim()));
						
							if(secondarysearchterms.size()>0)
							{
								if(searchterms.size()>0)
								{
									
									if(searchterms.get(0) != null){
										get.set_url(current.getForceredirect().replaceAll("REPsecondarysearchtermREP", secondarysearchterms.get(0).trim()));
									}
								}
							}
						}
					}
					else
					{
						//check for an injection in the redirect
						if(current.getBoolState(22))
						{
							if(current.getBoolState(21))
							{
								get.set_url((redirectinjection.trim()+current.getForceredirect().trim()));
			
							}
							else
							{
								get.set_url((current.getForceredirect().trim()+redirectinjection.trim()));
							}
						}
						else
						{
							get.set_url(current.getForceredirect().trim());
						}
					}
					get.set_method("GET");
					get.set_url_params(null);
					
					if(get.getUrl().indexOf("https:")==0)
					{
						cookies=get.get_secured();
						htmlstr=get.get_html();
					}
					else{
						cookies=get.get_cookies();
						htmlstr=get.get_html();
					}
					
					if(persistantparamregex != null){
						Pattern p2=Pattern.compile(persistantparamregex);
						Matcher m2=p2.matcher(htmlstr);
						
						if(m2.find()){
							persistantparam=m2.group();
						}
					}
					
					if(test){
						printTestHtml(htmlstr,get.getUrl());
					}
					
					if(current.getForcesecondredirect() != null)
					{
						log.info("Chaining to a Second Redirect at "+current.getForcesecondredirect());
						
						//replace a search term if it exists
						if(current.getForcesecondredirect().contains("REPsearchtermREP"))
						{
							log.info("TERM IS "+searchterms.get(0).trim()+" "+searchterms.size());
							get.set_url(current.getForcesecondredirect().replaceAll("REPsearchtermREP", searchterms.get(0).trim()));
							
							//perform for secondary term if requested
							if(secondarysearchterms.size()>0){
								get.set_url(current.getForcesecondredirect().replaceAll("REPsecondarysearchtermREP", secondarysearchterms.get(0).trim()));
							}
						}
						else{
							get.set_url(current.getForcesecondredirect());
						}
						
						
						if(get.getUrl().indexOf("https:")==0)
						{
							cookies=get.get_secured();
							htmlstr=get.get_html();
						}
						else{
							cookies=get.get_cookies();
							htmlstr=get.get_html();
						}
						
						if(persistantparamregex != null){
							Pattern p2=Pattern.compile(persistantparamregex);
							Matcher m2=p2.matcher(htmlstr);
							
							if(m2.find()){
								persistantparam=m2.group();
							}
						}
						
						if(test){
							printTestHtml(htmlstr,get.getUrl());
						}
					}
				}
			}
			else{
				
				cookies=get.get_cookies();
				htmlstr=get.get_html();
				
				if(persistantparamregex != null){
					Pattern p2=Pattern.compile(persistantparamregex);
					Matcher m2=p2.matcher(htmlstr);
					
					if(m2.find()){
						persistantparam=m2.group();
					}
				}
				
				if(test){
					printTestHtml(htmlstr,get.getUrl());
				}
				
				//redirect if necessary
				if(current.getForceredirect() != null)
				{
					log.info("Replacing Search Term");
					if(current.getForceredirect().contains("REPsearchtermREP"))
					{
						//check for an injection in the redirect
						if(current.getBoolState(22))
						{
							if(current.getBoolState(21))
							{
								get.set_url((redirectinjection.trim()+current.getForceredirect().replaceAll("REPsearchtermREP", searchterms.get(0).trim())));
							
								if(secondarysearchterms.size()>0){
									get.set_url((redirectinjection.trim()+current.getForceredirect().replaceAll("REPsecondarysearchtermREP", secondarysearchterms.get(0).trim())));
								}
							}
							else
							{
								get.set_url((current.getForceredirect().replaceAll("REPsearchtermREP", searchterms.get(0).trim()).trim()+redirectinjection.trim()));
								
								if(secondarysearchterms.size()>0){
									get.set_url((current.getForceredirect().replaceAll("REPsecondarysearchtermREP", secondarysearchterms.get(0).trim()).trim()+redirectinjection.trim()));
								}
							}
						}
						else
						{
							get.set_url(current.getForceredirect().replaceAll("REPsearchtermREP", searchterms.get(0).trim()));
						
							if(secondarysearchterms.size()>0){
								get.set_url(current.getForceredirect().replaceAll("REPsecondarysearchtermREP", secondarysearchterms.get(0).trim()));
							}
						}
						
					}
					else
					{
						//check for an injection in the redirect
						if(current.getBoolState(22))
						{
							if(current.getBoolState(21))
							{
								get.set_url((redirectinjection.trim()+current.getForceredirect().trim()));
							}
							else
							{
								get.set_url((current.getForceredirect().trim()+redirectinjection.trim()));
							}
						}
						else
						{
							get.set_url(current.getForceredirect().trim());
						}
					}
					
					log.info("Redirecting to "+get.getUrl());
					get.set_method("GET");
					get.set_url_params(null);
					
					
					cookies=get.get_cookies();
					htmlstr=get.get_html();
					
					if(persistantparamregex != null){
						Pattern p2=Pattern.compile(persistantparamregex);
						Matcher m2=p2.matcher(htmlstr);
						
						if(m2.find()){
							persistantparam=m2.group();
						}
					}

					if(test){
						printTestHtml(htmlstr,get.getUrl());
					}
					
					if(current.getForcesecondredirect() != null)
					{
					
						log.info("Chaining to a Second Redirect at "+current.getForcesecondredirect());
						
						//replace a search term if it exists
						if(current.getForcesecondredirect().contains("REPsearchtermREP"))
						{
							log.info("TERM IS "+searchterms.get(0).trim()+" "+searchterms.size());
							get.set_url(current.getForcesecondredirect().replaceAll("REPsearchtermREP", searchterms.get(0).trim()));
							
							if(secondarysearchterms.size()>0){
								get.set_url(current.getForcesecondredirect().replaceAll("REPsecondarysearchtermREP", secondarysearchterms.get(0).trim()));
							}
						}
						else{
							get.set_url(current.getForcesecondredirect());
						}
						
						
						cookies=get.get_cookies();
						htmlstr=get.get_html();
						
						if(persistantparamregex != null){
							Pattern p2=Pattern.compile(persistantparamregex);
							Matcher m2=p2.matcher(htmlstr);
							
							if(m2.find()){
								persistantparam=m2.group();
							}
						}
						
						if(test){
							printTestHtml(htmlstr,get.getUrl());
						}
					}
				}
			}
			
			if(current.getRedirect_regex()!= null & htmlstr != null)
			{
				log.info("Performing Redirect");
				int runs=0;
				boolean run=true;
				String temp=null;
				
				//redirect on multiple regexes
				while(run)
				{
					Pattern p=null;
					
					if(current.getRedirect_regex().contains("SINGLE:"))
					{
						run=false;
						p=Pattern.compile(repEscape(current.getRedirect_regex().replaceAll("SINGLE:", "")),Pattern.MULTILINE|Pattern.CASE_INSENSITIVE);
						
					}
					else if(current.getRedirect_regex().contains("DOUBLE:"))
					{
						 if(runs==1)
						 {
							 run=false;
						 }
						 
						 p=Pattern.compile(repEscape(current.getRedirect_regex().replaceAll("DOUBLE:", "")),Pattern.MULTILINE|Pattern.CASE_INSENSITIVE);
					}
					else if(current.getRedirect_regex().contains("TRIPLE:"))
					{
						 if(runs==2)
						 {
							 run=false;
						 }
						 
						 p=Pattern.compile(repEscape(current.getRedirect_regex().replaceAll("TRIPLE:", "")),Pattern.MULTILINE|Pattern.CASE_INSENSITIVE);
					}
					else
					{
						 p=Pattern.compile(repEscape(current.getRedirect_regex()),Pattern.MULTILINE|Pattern.CASE_INSENSITIVE);
					}
					
					Matcher m=p.matcher(htmlstr);
				
					if(m.find())
					{
						if(current.getRedirect_prefix() != null)
						{
						temp=current.getRedirect_prefix().trim()+repEscape(m.group().trim());
						}
						else
						{
						temp=repEscape(m.group());
						}
					
						temp=repEscape(temp);
					
						//check for an injection in the redirect
						if(current.getBoolState(22))
						{
							if(current.getBoolState(21))
							{
								temp=redirectinjection.trim()+temp.trim();
							}
							else
							{
								temp=temp.trim();
								temp+=redirectinjection.trim();
							}
						}

						get.set_url(temp);
						get.set_method("GET");
						get.set_url_params(null);
						
						
					    if(proxy)
					    {
							setPullProxy();
						}
					
						if(temp.contains("https:") & current.getAuthority() != null)
						{
							cookies=get.get_secured();
							htmlstr=get.get_html();
						}
						else if(temp.contains("https:") & (current.getAuthority()==null))
						{
						
							htmlstr=get.get_SSL();
							cookies=get.get_cookies();
						}
						else
						{
							cookies=get.get_cookies();
							htmlstr=get.get_html();
						}
						
						if(persistantparamregex != null){
							Pattern p2=Pattern.compile(persistantparamregex);
							Matcher m2=p2.matcher(htmlstr);
							
							if(m2.find()){
								persistantparam=m2.group();
							}
						}
					}	
					else
					{
						run=false;
						
						//if this was the first redirect, then there is a problem and a custom error is thrown containing the stack trace
						if(runs==0)
						{
							try{
								throw new BadRegex("In Redirect: \n");
							}catch(BadRegex e)
							{
								e.printStackTrace();
							}
						}
					}
					temp=null;
					runs++;
				}
				
				if(test){
					this.printTestHtml(htmlstr, get.getUrl());
				}
			}
			
			
			
			additionalhtml=null;
			String originalhtml=htmlstr;
			//not bother trying to integrate adding this in whole: the first if will grab a param string from the page and use it to get a series fo pages
			if(additionaliterparams != null &current.getBoolState(8) & current.getBoolState(29)==true){
				log.info("Getting Additional Pages");
				String tempurl;
				String temphtml=htmlstr;
				Pattern p2;
				Matcher m2;
				HashSet<String> additionalurls=new HashSet<String>();

				
				if(additionalnarrow != null){
					
					log.info("Narrowing");
					p2=Pattern.compile(additionalnarrow);
					m2=p2.matcher(htmlstr.replaceAll("\t|\r|\r\n|\n", ""));
					
					if(m2.find()){
						temphtml=m2.group();
					}
					else{
						log.info("Cannot Narrow Html");
					}
				}
				
				if(temphtml != null & additionalmaxpos>0){
					
					log.info("Max Pos Given");
					
					//get the map
					Map<String,Json> jmap=Json.read(additionaliterparams).asJsonMap();
					
					//get the individual params if specified and create a series of urls from there
					if(additionalrenarrow != null){
						
						log.info("Narrowing Again and then Adding Params");
						
						p2=Pattern.compile(additionalrenarrow);
						m2=p2.matcher(temphtml);
						tempurl=additionalpre;
						String temp;
						int pos=0;
						String[] tmparr;
						
						while(m2.find()){
							
							if(jmap.get(Integer.toString(pos)).asString().compareTo("skip") != 0 & jmap.get(Integer.toString(pos)).asString().contains("ADD")==false){
								tempurl+=(pos==0)?jmap.get(Integer.toString(pos))+"="+m2.group():"&"+jmap.get(Integer.toString(pos))+"="+m2.group();
							}
							
							if(pos == additionalmaxpos){
								
								if(additionalurlmanips != null){
									//perform additional url manipulations
									if(secondarysearchterms.size()>0)
									{
										tempurl=getUrlManips(additionalurlmanips, tempurl, temphtml, searchterms.get(0).trim(), pos,secondarysearchterms.get(0).trim());
									}
									else
									{
										tempurl=getUrlManips(additionalurlmanips, tempurl, temphtml, searchterms.get(0).trim(), pos,null);
									}
								}
								
								if(additionalpost != null){
									tempurl+=additionalpost.trim();
								}
								
								additionalurls.add(tempurl.replaceAll("\"", ""));
								tempurl=additionalpre;
								pos=0;
							}
							else
							{
								pos++;
							}
						}
					}
					else{
						log.info("Adding Params");
						//if the urls are not narrowed further, concact as if each key is connected to its regex value
						//this is useful if one url needs to be concacted from given page parameters
						tempurl=additionalpre;
						int pos=0;
						
						//get the parameters
						for(String header: jmap.keySet()){
							tempurl=(pos==0)?tempurl+header+"=":tempurl+"&"+header+"=";
							
							if(jmap.get(header).asString().contains("ADD")==false)
							{
								//everything is considered to be a regex unless told to just add the parameter
								p2=Pattern.compile(jmap.get(header).asString());
								m2=p2.matcher(temphtml);
							
								if(m2.find()){
									tempurl+=m2.group();
								}
							}
							else if(jmap.get(header).asString().contains("SEARCHTERM") == true)
							{
								//add the searchterm
								tempurl+=searchterms.get(0).trim();
							}
							else{
								//just add a specified term
								tempurl+=jmap.get(header).asString();
							}
							pos++;
						}
						
						//search for the new url in the page
						p2=Pattern.compile(tempurl);
						m2=p2.matcher(temphtml);
					
						while(m2.find()){
							
							if(additionalurlmanips != null){
								if(secondarysearchterms.size()>0)
								{
									tempurl=getUrlManips(additionalurlmanips, tempurl, temphtml, searchterms.get(0).trim(), pos,secondarysearchterms.get(0).trim());
								}
								else
								{
									tempurl=getUrlManips(additionalurlmanips, tempurl, temphtml, searchterms.get(0).trim(), pos,null);
								}
							}
							
							additionalurls.add(m2.group());
						}
					}
					
					//iterate through the new urls to get the additional page
					if(proxy)
					{
						setPullProxy();
					}
					
					for(String addurl: additionalurls){
						log.info("Getting Additional Page for "+addurl);
						get.set_url(addurl);
						get.set_method("GET");
					
						//get the pages
						if(current.getBoolState(1))
						{
							if(get.get_authority() != null | get.get_host() != null)
							{
								cookies=get.get_secured();
								
								additionalhtml=(additionalhtml==null)?get.get_html().replaceAll("~|\t|\r|\r\n|\n"," "):additionalhtml+"~"+get.get_html().replaceAll("~|\t|\r|\r\n|\n"," ");
							}
							else
							{
								additionalhtml=(additionalhtml==null)?get.get_SSL().replaceAll("~|\t|\r|\r\n|\n"," "):additionalhtml+"~"+get.get_SSL().replaceAll("~|\t|\r|\r\n|\n"," ");
								cookies=get.cookiegrab();
							}
						}
						else
						{
							cookies=get.get_cookies();
							additionalhtml=(additionalhtml==null)?get.get_html().replaceAll("~|\t|\r|\r\n|\n"," "):additionalhtml+"~"+get.get_html().replaceAll("~|\t|\r|\r\n|\n"," ");
						}
						
						if(additionalredirect != null){
							log.info("Getting Redirects");
							
							if(test){
								this.printTestHtml(additionalhtml, get.getUrl());
							}
							
							
							Pattern p3=Pattern.compile(additionalredirect);
							Matcher m3=p3.matcher(additionalhtml);
							
							additionalhtml=null;
							
							while(m3.find()){
								
								if(this.additionalredirectprefix != null){
									get.set_url((additionalredirectprefix.trim()+m3.group().trim()));
								}
								else{
									get.set_url(m3.group());
								}
								
								log.info("Redirect: "+get.getUrl());
							
							
								//get the pages
								if(current.getBoolState(1))
								{
									if(get.get_authority() != null | get.get_host() != null)
									{
										cookies=get.get_secured();
										additionalhtml=(additionalhtml==null)?get.get_html().replaceAll("~|\t|\r|\r\n|\n"," "):additionalhtml+"~"+get.get_html().replaceAll("~|\t|\r|\r\n|\n"," ");
									}
									else
									{
									
										additionalhtml=(additionalhtml==null)?get.get_SSL().replaceAll("~|\t|\r|\r\n|\n"," "):additionalhtml+"~"+get.get_SSL().replaceAll("~|\t|\r|\r\n|\n"," ");
										cookies=get.cookiegrab();
									}
								
								}
								else
								{
									cookies=get.get_cookies();
									additionalhtml=(additionalhtml==null)?get.get_html().replaceAll("~|\t|\r|\r\n|\n"," "):additionalhtml+"~"+get.get_html().replaceAll("~|\t|\r|\r\n|\n"," ");
								}
								
								
								
								if(incolumn & additionalhtml != null){
								
									String ohash=genHash(Integer.toString(pageid));
									if(current.getIfpath() != null){
										getImages(current,searchterm,ohash,additionalhtml);
									}
									
									if(test){
										printTestHtml(additionalhtml,get.getUrl());
									}
								    addAdditionalToArray(incolumn,additionalhtml,pageid,htmlstr,ohash,get.getUrl());
									log.info("PAGE FOUND AND ADDED");
									
									additionalhtml=null;
								}
							
							}
							
						
							if(additionalhtml != null){
									
									if(incolumn ==true){
										for(String str: additionalhtml.split("~")){
											String ohash=genHash(Integer.toString(pageid));
											if(current.getIfpath() != null){
												getImages(current,searchterm,ohash,additionalhtml);
											}
										 	addAdditionalToArray(incolumn,additionalhtml,pageid,htmlstr,ohash,get.getUrl());
										}
									}
									else{
										String ohash=genHash(Integer.toString(pageid));
										 addAdditionalToArray(incolumn,additionalhtml,pageid,htmlstr,ohash,get.getUrl());
									}
									additionalhtml =null;
									
							}
							
							
						}
						
						
						
						//something is getting reset: I will look later, adding new node to db
						if(additionalhtml != null){
							
							if(incolumn ==true){
								for(String str: additionalhtml.split("~")){
									String ohash=genHash(Integer.toString(pageid));
									if(current.getIfpath() != null){
										getImages(current,searchterm,ohash,additionalhtml);
									}
								 	addAdditionalToArray(incolumn,additionalhtml,pageid,htmlstr,ohash,get.getUrl());
								}
							}
							else{
								String ohash=genHash(Integer.toString(pageid));
								 addAdditionalToArray(incolumn,additionalhtml,pageid,htmlstr,ohash,get.getUrl());
							}
							additionalhtml =null;
							pnum++;
							log.info("PAGE FOUND AND ADDED");
							
						}
						
						//automatically add to db in case of extremely large additions
						if(html.size()>0){
							log.info("Adding to DB @ "+Calendar.getInstance().getTime().toString());
							addtoDB(html);
							html.clear();
							log.info("Finished Adding to DB @ "+Calendar.getInstance().getTime().toString());
						}
					}
					
				}
				else
				{
					log.info("NO ADDITIONAL PAGES FOUND");
				}
				
				
			}
			else if(current.getBoolState(8) & additionalhtml==null & additionalparams==null & additionalurls != null & additionalparamname == null & current.getBoolState(29)==true)
			{
				log.info("Getting Additional Pages");
				
				Pattern p2=Pattern.compile(additionalurls);
				Matcher m2=p2.matcher(htmlstr);
				timeout();	
				String method=get.get_method();
				String params=null;
				
				if(get.get_method().trim().toLowerCase().compareTo("post")==0)
				{
					params=get.get_url_params();
					get.set_url_params(null);
				}
				
				get.set_method("GET");
				int additional=0;
				while(m2.find())
				{
					additional++;
					
					
					log.info("FOUND ADDITIONAL PAGE");
					
					url=repEscape(m2.group().trim());
					if(additionalpre != null)
					{
						url=additionalpre.trim()+url.trim();
					}
					
					if(additionalpost != null)
					{
						url+=additionalpost.trim();
					}
					
					if(get.getUrl().contains(repEscape(m2.group()).trim())==false)
					{
						//manipulate the url with regex or by adding encoded or unencoded parameters/connectors
						if(current.getUrlmanips() != null)
						{
							if(htmlstr != null)
							{
								if(secondarysearchterms.size()>0)
								{
									url=(searchterm != null)?getUrlManips(current.getUrlmanips(), url, htmlstr, searchterm, pageid,secondarysearchterm):getUrlManips(current.getUrlmanips(), url, htmlstr, searchterms.get(0), pageid,secondarysearchterm);
								}
								else
								{
									url=(searchterm != null)?getUrlManips(current.getUrlmanips(), url, htmlstr, searchterm, pageid,null):getUrlManips(current.getUrlmanips(), url, htmlstr, searchterms.get(0), pageid,null);
								}
							}
						}
					}
					
					log.info("Additional Url: "+repEscape(url));
					get.set_url(repEscape(url));
					timeout();
					if(proxy)
					{
						setPullProxy();
					}
					
					//get the pages
					if(current.getBoolState(1) & get.getUrl().contains("https:"))
					{
						if(get.get_authority() != null | get.get_host() != null)
						{
							cookies=get.get_secured();
							additionalhtml=(additionalhtml==null)?get.get_html().replaceAll("~|\t|\r|\r\n|\n"," "):additionalhtml+"~"+get.get_html().replaceAll("~|\t|\r|\r\n|\n"," ");
						}
						else
						{
							
							additionalhtml=(additionalhtml==null)?get.get_SSL().replaceAll("~|\t|\r|\r\n|\n"," "):additionalhtml+"~"+get.get_SSL().replaceAll("~|\t|\r|\r\n|\n"," ");
							cookies=get.cookiegrab();
						}
						
					}
					else if(get.getUrl().contains("http:"))
					{
						cookies=get.get_cookies();
						additionalhtml=(additionalhtml==null)?get.get_html().replaceAll("~|\t|\r|\r\n|\n"," "):additionalhtml+"~"+get.get_html().replaceAll("~|\t|\r|\r\n|\n"," ");
					}
					else{
						log.info("WARNING: Malformed URL "+get.getUrl());
					}
					
					
					if(additionalredirect != null){
						log.info("Getting Redirects");
						
						if(test){
							this.printTestHtml(additionalhtml, get.getUrl());
						}
						
						Pattern p3=Pattern.compile(additionalredirect);
						Matcher m3=p3.matcher(additionalhtml);
						
						additionalhtml=null;
						
						while(m3.find()){
							
							
							if(this.additionalredirectprefix != null){
								get.set_url((additionalredirectprefix.trim()+m3.group().trim()));
							}
							else{
								get.set_url(m3.group());
							}
							
							log.info("Redirect: "+get.getUrl());
						
						
							//get the pages
							if(current.getBoolState(1))
							{
								if(get.get_authority() != null | get.get_host() != null)
								{
									cookies=get.get_secured();
									additionalhtml=(additionalhtml==null)?get.get_html().replaceAll("~|\t|\r|\r\n|\n"," "):additionalhtml+"~"+get.get_html().replaceAll("~|\t|\r|\r\n|\n"," ");
								}
								else
								{
								
									additionalhtml=(additionalhtml==null)?get.get_SSL().replaceAll("~|\t|\r|\r\n|\n"," "):additionalhtml+"~"+get.get_SSL().replaceAll("~|\t|\r|\r\n|\n"," ");
									cookies=get.cookiegrab();
								}
							
							}
							else
							{
								cookies=get.get_cookies();
								additionalhtml=(additionalhtml==null)?get.get_html().replaceAll("~|\t|\r|\r\n|\n"," "):additionalhtml+"~"+get.get_html().replaceAll("~|\t|\r|\r\n|\n"," ");
							}
						
							if(incolumn & additionalhtml != null){
								
								if(test){
									printTestHtml(additionalhtml,get.getUrl());
								}
								
								if(incolumn ==true){
									for(String str: additionalhtml.split("~")){
										String ohash=genHash(Integer.toString(pageid));
										if(current.getIfpath() != null){
											getImages(current,searchterm,ohash,additionalhtml);
										}
									 	addAdditionalToArray(incolumn,additionalhtml,pageid,htmlstr,ohash,get.getUrl());
									}
								}

								if(html.size()>commit_size)
								{
									log.info("Adding to DB @ "+Calendar.getInstance().getTime().toString());
									addtoDB(html);
									html.clear();
									log.info("Finished Adding to DB @ "+Calendar.getInstance().getTime().toString());
								}
								
								additionalhtml=null;
								pnum++;
								log.info("PAGE FOUND AND ADDED");
							}
						}
						
						if(additionalhtml != null & incolumn==false)
						{
							if(test){
								printTestHtml(additionalhtml,get.getUrl());
							}
							
							String ohash=genHash(Integer.toString(pageid));
							addAdditionalToArray(incolumn,additionalhtml,pageid,htmlstr,ohash,get.getUrl());
							
							if(html.size()>commit_size)
							{
								log.info("Adding to DB @ "+Calendar.getInstance().getTime().toString());
								addtoDB(html);
								html.clear();
								log.info("Finished Adding to DB @ "+Calendar.getInstance().getTime().toString());
							}
							
							additionalhtml=null;
							pnum++;
							log.info("PAGE FOUND AND ADDED");
						}
					}
					else if(incolumn==true & additionalhtml != null){
						
						if(test){
							printTestHtml(additionalhtml,get.getUrl());
						}
						
						if(incolumn ==true){
							for(String str: additionalhtml.split("~")){
								String ohash=genHash(Integer.toString(pageid));
								if(current.getIfpath() != null){
									getImages(current,searchterm,ohash,additionalhtml);
								}
							 	addAdditionalToArray(incolumn,additionalhtml,pageid,htmlstr,ohash,get.getUrl());
							}
						}

						
						if(html.size()>commit_size)
						{
							log.info("Adding to DB @ "+Calendar.getInstance().getTime().toString());
							addtoDB(html);
							html.clear();
							log.info("Finished Adding to DB @ "+Calendar.getInstance().getTime().toString());
						}
						
						additionalhtml=null;
						pnum++;
						log.info("PAGE FOUND AND ADDED");
					}
				}
				
				
				if(html.size()>commit_size)
				{
					log.info("Adding to DB @ "+Calendar.getInstance().getTime().toString());
					addtoDB(html);
					html.clear();
					log.info("Finished Adding to DB @ "+Calendar.getInstance().getTime().toString());
				}
				
				if(get.get_url_params()!= null){
					get.set_method(method.toUpperCase().trim());
					get.set_url_params(params);
				}
			}
			else if(current.getBoolState(8)==true & additionalhtml==null & additionalurls != null & additionalparamname != null)
			{
				log.info("ADDING ADDITIONAL URLS");
				
				//find the specified post param in the page
				if(additionalparams != null | additionalregexparams != null | additionalparamname != null)
				{
					String oldparams=get.get_url_params();
					
					ArrayList<String> paramnames=new ArrayList<String>();
					ArrayList<String> paramvals=new ArrayList<String>();
					
					ArrayList<String> regexnames=new ArrayList<String>();
					ArrayList<String> regexvals=new ArrayList<String>();
					
					//set additional regex parameters
					if(additionalregexparams != null)
					{
						log.info("GETTING REGEX PARAMS");
						for(String iterval: additionalregexparams.keySet())
						{
							regexnames.add(iterval);
							
							Pattern p2=Pattern.compile(additionalregexparams.get(iterval));
							Matcher m2=p2.matcher(htmlstr.replaceAll("\t|;|\r|\r\n|\n"," "));
							
							if(m2.find())
							{
								log.info(m2.group());
								regexvals.add(repEscape(repEscape(m2.group().trim())));
							}
						}
					}
					
					
					//set up additional parameters
					if(additionalparams != null | additionalregexparams != null)
					{
						log.info("Setting Additional Params");
						
						//set up for additional html with post params
						if(additionalparams != null)
						{
							for(String iterval: additionalparams.keySet())
							{
								paramnames.add(iterval);
								paramvals.add(additionalparams.get(iterval));
							}
							
							//add extra parameters
							if(additionaleval)
							{
								paramnames.add("__EVENTVALIDATION");
								paramvals.add(get.get_event_validation());
							}
							
							if(additionalviewstate)
							{
								paramnames.add("__VIEWSTATE");
								paramvals.add(get.get_viewstate());
							}
							
							
							if(current.getBoolState(4))
							{
								paramnames.add("javax.faces.ViewState");
								paramvals.add(get.get_server_faces());
							}
						}
						
						if(additionalregexparams != null & regexnames.size()>0)
						{
							paramnames.addAll(regexnames);
							paramvals.addAll(regexvals);
						}
						
						
					}
			
					///THIS IS WHERE THE REGEX FOR THE ADDITIONAL URLS GOES TO PICK UP EXTRA PARAMETERS 
					//THE URL BECOMES additionalparamurl
					log.info("Searching for the Additional Parameter");
					Pattern p=Pattern.compile(additionalurls);
					Matcher m=p.matcher(htmlstr);
					int j=0;
					
					if(persistantparamregex != null){
						Pattern p2=Pattern.compile(persistantparamregex);
						Matcher m2=p2.matcher(htmlstr);
						
						if(m2.find()){
							persistantparam=m2.group();
						}
					}
				
					
					get.set_method("POST");
					int pages=0;
					
					HashSet<String> newhtml=new HashSet<String>();
				
					while(m.find())
					{
						if(newhtml.contains(m.group())==false){
						newhtml.add(m.group());
						pages++;
						
						log.info("Adding Param "+m.group());
						
						//check to see whether the paramname is the regex value (this happens a lot more than i thought
						if(additionalparamname.contains("regex")==true){
							paramnames.add(repEscape(repEncode(m.group().trim())));
						}
						else{
							paramnames.add(additionalparamname);
						}
						
						if(current.getBoolState(30) & persistantparam != null){
							paramnames.add(persistantname);
							paramvals.add(persistantparam);
						}
						
						paramvals.add(repEscape(repEncode(m.group().trim())));
						get.set_url_params(null);
						
						if(current.getBoolState(30) & persistantparam != null){
							paramnames.add(persistantname);
							paramvals.add(persistantparam);
						}
						
						get.encode_url_params(arrListtoStringArr(paramnames), arrListtoStringArr(paramvals));
						
						log.info("NEW PARAMS: "+get.get_url_params());
				
						if(additionalpre != null | additionalpost != null & additionalparamurl ==null)
						{
							
							if(additionalpre != null)
							{
								url=(url==null)?additionalpre.trim()+url.trim():additionalparamurl+additionalpre.trim()+url.trim();
							}
							
							if(additionalpost != null)
							{
								url=(url==null)?url.trim()+additionalpost.trim():url.trim()+additionalpost.trim();
							}
							
							
							log.info("Setting URL to "+url);
							get.set_url(url);

							if(get.getUrl().contains("SEARCHTERM")){
								url=get.getUrl();
								url.replaceAll("SEARCHTERM", searchterm);
								
								if(url.contains("SECONDARYSEARCHTERM")){
									url=url.replaceAll("SECONDARYSEARCHTERM", secondarysearchterm);
								}
								get.set_url(url);
								log.info("URL: "+url);
							}
						}
						else if(additionalparamurl != null)
						{
							url=additionalparamurl;
							
							if(additionalurlmanips != null)
							{
								if(secondarysearchterms.size()>0)
								{
									url=getUrlManips(additionalurlmanips, additionalparamurl, htmlstr, searchterm, j,secondarysearchterm);
								}
								else
								{
									url=getUrlManips(additionalurlmanips, additionalparamurl, htmlstr, searchterm, j,null);
								}
							}
							
							log.info("Setting URL to "+url);
							get.set_url(url);

							if(get.getUrl().contains("SEARCHTERM")){
								url=get.getUrl();
								url=url.replaceAll("SEARCHTERM", searchterm);
								
								if(url.contains("SECONDARYSEARCHTERM")){
									url=url.replaceAll("SECONDARYSEARCHTERM", secondarysearchterm);
								}
								get.set_url(url);
							}
						}
						j++;
						
						
						
						//get the html
						log.info("Getting Additional URLS from Params");
						timeout();
						if(proxy)
						{
							setPullProxy();
						}
				
						if(get.get_html().contains(additionalnotcontain)==false)
						{
							//get the pages
							
							
							if(current.getBoolState(1))
							{
								if(get.get_authority() != null | get.get_host() != null)
								{
									cookies=get.get_secured();
									
									if(additionalredirect != null)
									{
										get.get_html();
										
										Pattern p3=Pattern.compile(additionalredirect);
										Matcher m3=p3.matcher(get.get_html());
										
										if(m3.find())
										{
											get.set_method("GET");
											url=repEscape(m3.group());
											get.set_url_params(null);
											
											if(additionalredirectprefix != null)
											{
												url=additionalredirectprefix+url;
											}
											
											if(additionalredirectsuffix != null)
											{
												url+=additionalredirectsuffix;
											}
											
											if(additionalredirectreplace != null)
											{
												url=url.replaceAll(additionalredirectreplace,additionalredirectreplace);
											}
											

											
											log.info("Setting URL to "+url);
											get.set_url(url);

											if(get.getUrl().contains("SEARCHTERM")){
												url=get.getUrl();
												url=url.replaceAll("SEARCHTERM", searchterm);
												
												if(url.contains("SECONDARYSEARCHTERM")){
													url=url.replaceAll("SECONDARYSEARCHTERM", secondarysearchterm);
												}
												get.set_url(url);
											}
											cookies=get.get_secured();
										}
									}
									
									String hs=get.get_html();
									String ohash=genHash(Integer.toString(pageid));
									if(current.getIfpath() != null){
										getImages(current,searchterm,ohash,hs);
									}
									
									additionalhtml=(additionalhtml==null)?hs.replaceAll("~|\t|\r|\r\n|\n"," "):additionalhtml+"~"+hs.replaceAll("~|\t|\r|\r\n|\n"," ");
									
									if(incolumn==true | current.getIfpath() != null){

										addAdditionalToArray(incolumn,additionalhtml,pageid,htmlstr,ohash,get.getUrl());
											
												
									   if(html.size()>commit_size){
										   log.info("Adding to DB @ "+Calendar.getInstance().getTime().toString());
											addtoDB(html);
											html.clear();
											log.info("Finished Adding to DB @ "+Calendar.getInstance().getTime().toString());
										}
												
									}

									
							
									cookies=get.cookiegrab();
									if(additionalparams != null)
									{
										get.set_method("POST");
									}
								}
								else
								{
									if(additionalredirect != null)
									{
										log.info("SEARCHING FOR REDIRECT");
										get.get_SSL();
										
										Pattern p3=Pattern.compile(additionalredirect);
										Matcher m3=p3.matcher(get.get_html());
										
										if(m3.find())
										{
											get.set_method("GET");
											get.set_url_params(null);
											
											url=repEscape(m3.group());
											
											if(additionalredirectprefix != null)
											{
												url=additionalredirectprefix+url;
											}
											
											if(additionalredirectsuffix != null)
											{
												url+=additionalredirectsuffix;
											}
											
											if(additionalredirectreplace != null)
											{
												url=url.replaceAll(additionalredirectreplace,additionalredirectreplace);
											}
											
											
											
											log.info("FOUND ADDITIONAL REDIRECT");
											get.set_url(url);
											
											if(get.getUrl().contains("SEARCHTERM")){
												url=get.getUrl();
												url=url.replaceAll("SEARCHTERM", searchterm);
												
												if(url.contains("SECONDARYSEARCHTERM")){
													url=url.replaceAll("SECONDARYSEARCHTERM", secondarysearchterm);
												}
												get.set_url(url);
											}
											
											
											get.get_SSL();
										}
										else
										{
											log.info("ADDITIONAL REDIRECT NOT FOUND");
										}
									}
									
									String hs=get.get_SSL();
									String ohash=genHash(Integer.toString(pageid));
									
									if(current.getIfpath() != null){
										getImages(current, searchterm, ohash, hs);
									}
						
									additionalhtml=(additionalhtml==null)?hs.replaceAll("~|;|\t|\r|\r\n|\n"," "):additionalhtml+"~"+hs.replaceAll("~|\t|\r|\r\n|\n"," ");
									
									if(incolumn==true | current.getIfpath() != null){

										addAdditionalToArray(incolumn,additionalhtml,pageid,htmlstr,ohash,get.getUrl());
											
												
										if(html.size()>commit_size){
												log.info("Adding to DB @ "+Calendar.getInstance().getTime().toString());
												addtoDB(html);
												html.clear();
												log.info("Finished Adding to DB @ "+Calendar.getInstance().getTime().toString());
										}
												
								
										additionalhtml=null;
									
									
									cookies=get.cookiegrab();
									if(additionalparams != null)
									{
										get.set_method("POST");
									}
								}
								}
							}
							else
							{
								if(additionalredirect != null)
								{
									log.info("LOOKING FOR REDIRECT");
									cookies=get.get_cookies();
									
									Pattern p3=Pattern.compile(additionalredirect);
									Matcher m3=p3.matcher(get.get_html());
									
									if(m3.find())
									{
										log.info("FOUND REDIRECT");
										get.set_method("GET");
										get.set_url_params(null);
										
										url=repEscape(m3.group());
										
										if(additionalredirectprefix != null)
										{
											url=additionalredirectprefix+url;
										}
										
										if(additionalredirectsuffix != null)
										{
											url+=additionalredirectsuffix;
										}
										
										if(additionalredirectreplace != null)
										{
											url=url.replaceAll(additionalredirectreplace,additionalredirectreplace);
										}
										
										
										
										log.info("Setting URL to "+url);
										get.set_url(url);

										if(get.getUrl().contains("SEARCHTERM")){
											url=get.getUrl();
											url=url.replaceAll("SEARCHTERM", searchterm);
											
											if(url.contains("SECONDARYSEARCHTERM")){
												url=url.replaceAll("SECONDARYSEARCHTERM", secondarysearchterm);
											}
											get.set_url(url);
										}
										cookies=get.get_cookies();
									}
									else
									{
										log.info("REDIRECT NOT FOUND");
									}
								}
								
								if(get.getUrl().contains("SEARCHTERM")){
									url=get.getUrl();
									url=url.replaceAll("SEARCHTERM", searchterm);
									
									if(url.contains("SECONDARYSEARCHTERM")){
										url=url.replaceAll("SECONDARYSEARCHTERM", secondarysearchterm);
									}
									log.info("Resetting URL to:"+url);
									get.set_url(url);
								}
								
								cookies=get.get_cookies();
								additionalhtml=(additionalhtml==null)?get.get_html().replaceAll("~|\t|\r|\r\n|\n"," "):additionalhtml+"~"+get.get_html().replaceAll("~|\t|\r|\r\n|\n"," ");
								
								if(additionalparams != null)
								{
									get.set_method("POST");
								}
							}
						}
						
						paramnames.remove((paramnames.size()-1));
						paramvals.remove(repEscape(repEncode(m.group().trim())));
						
						//print obtained url if test is set
						if(test)
						{
							printTestHtml(additionalhtml,get.getUrl());
						}
					
					if(additionalhtml==null)
					{
						log.info("NO ADDITIONAL PAGES FOUND");
					}
					else{
						log.info("Additional Pages Found "+pages);
					}
					
					//reset old params from the stored param string
					get.set_url_params(oldparams);
					get.set_html(htmlstr);
					
					if(true){
						printTestHtml(additionalhtml,get.getUrl());
					}
					
					//add the post objects
					if(current.getBoolState(8) & additionalhtml != null)
					{
						
						if(incolumn ==true){
							for(String str: additionalhtml.split("~")){
								String ohash=genHash(Integer.toString(pageid));
								if(current.getIfpath() != null){
									getImages(current,searchterm,ohash,additionalhtml);
								}
							 	addAdditionalToArray(incolumn,additionalhtml,pageid,htmlstr,ohash,get.getUrl());
							}
						}
						else{
							String ohash=genHash(Integer.toString(pageid));
							addAdditionalToArray(incolumn,additionalhtml,pageid,htmlstr,ohash,get.getUrl());
						}

						
						if(html.size()>commit_size){
							log.info("Adding to DB @ "+Calendar.getInstance().getTime().toString());
							addtoDB(html);
							html.clear();
							log.info("Finished Adding to DB @ "+Calendar.getInstance().getTime().toString());
						}
								
							
						additionalhtml=null;
						
					}
				}
				}
				}
			}
			
			//add the post objects
			if(current.getBoolState(8) & additionalhtml != null)
			{
				
				if(test){
					printTestHtml(additionalhtml,get.getUrl());
				}
				

				if(incolumn ==true){
					for(String str: additionalhtml.split("~")){
						String ohash=genHash(Integer.toString(pageid));
						if(current.getIfpath() != null){
							getImages(current,searchterm,ohash,additionalhtml);
						}
					 	addAdditionalToArray(incolumn,additionalhtml,pageid,htmlstr,ohash,get.getUrl());
					}
				}
				else{
					String ohash=genHash(Integer.toString(pageid));
					addAdditionalToArray(incolumn,additionalhtml,pageid,htmlstr,ohash,get.getUrl());
				}				
				
				//automatically add to db in case of extremely large additions
				if(html.size()>0){
					log.info("Adding to DB @ "+Calendar.getInstance().getTime().toString());
					addtoDB(html);
					html.clear();
					log.info("Finished Adding to DB @ "+Calendar.getInstance().getTime().toString());
				}
				
				additionalhtml=null;
				pnum++;
				log.info("PAGE FOUND AND ADDED");
			}
		
			if(html.size()>commit_size)
			{
				log.info("Adding to DB @ "+Calendar.getInstance().getTime().toString());
				addtoDB(html);
				html.clear();
				log.info("Finished Adding to DB @ "+Calendar.getInstance().getTime().toString());
			}
			
			get.set_html(originalhtml);
			
			
			//perform a loop if necessary
			if(current.getBoolState(16))
			{
				log.info("Performing Loop From: "+current.getUrl());
				//check against current method
				if(((get.get_method().compareTo("GET")==0) & current.getBoolState(18)==false) | ((get.get_method().compareTo("POST")==0) & current.getBoolState(18)==true))
				{
					//change the method if necessary
					if(get.get_method().compareTo("POST") ==0)
					{
						get.set_method("POST");
					}
					else
					{
						get.set_method("GET");
						get.set_url_params(null);
					}
				}
				
		
				total=0;
				//get the loop information via regex
				if(current.getBoolState(15))
				{	
					log.info(current.getLoop_reg());
					htmlstr=htmlstr.replaceAll("\t|\r\n|\n|\r","");
					Pattern p=Pattern.compile(current.getLoop_reg(),Pattern.MULTILINE|Pattern.CASE_INSENSITIVE);
					Matcher m=p.matcher(htmlstr);
					
					if(m.find())
					{
						total=Integer.parseInt(m.group().trim())/current.getPerPage();
					}
				}
				else if(current.getBoolState(32)){
					log.info(current.getLoop_reg());
					htmlstr=htmlstr.replaceAll("\t|\r\n|\n|\r","");
					Pattern p=Pattern.compile(current.getLoop_reg(),Pattern.MULTILINE|Pattern.CASE_INSENSITIVE);
					Matcher m=p.matcher(htmlstr);
					
					while(m.find())
					{
						total=Integer.parseInt(m.group().trim());
					}
				
					
					log.info("Total is: "+total);
				}
				
				int currenti=1;
				pars=null;
				i=1;
				Boolean reg=true;
				
				if(current.getIteratedloopparams() != null){
					if(current.getIteratedloopparams().containsKey("offset")){
							i+=Integer.parseInt(current.getIteratedloopparams().get("offset"));
							log.info("Revising Starting position to "+i+" in loop to reflect starting position");
							current.getIteratedloopparams().remove("offset");
					}
				}
				
				log.info("CURRENT TOTAL: "+total);
				
				
				//perform loop
				if(current.getLoop_reg()==null | current.getBoolState(15)==true |current.getBoolState(32)==true)
				{
					reg=false;
				}
				else
				{
					Pattern p=Pattern.compile(current.getLoop_reg());
					Matcher m=p.matcher(htmlstr.replaceAll("\t|\r|\r\n|\n", ""));
					
					if(!m.find()){
						reg=false;
					}
				}
				
				while(reg==true | i<=total)
				{	
					log.info("LOOP: "+searchterm+"| Iteration: "+i+"| Total: "+total);
					
					if((current.getLoopheaders()) != null)
					{					
						//reset the parameters if necessary
						if((current.getLoopparams()) != null  | current.getLoopparam() != null)
						{
							log.info("SETTING LOOP PARAMS");
							names.clear();
							values.clear();
							val=null;
							
							mp=Json.read(current.getLoopparams()).asJsonMap();
							keys=mp.keySet();
							
							for(String k:keys)
							{

								if(mp.get(k).isString())
								{
									if(k.toLowerCase().compareTo("viewstate")!=0 & k.toLowerCase().compareTo("eval")!=0)
									{
										names.add(k);
										values.add(mp.get(k).asString());
									}
								}
								else if(mp.get(k).isNull()){
									if(k.toLowerCase().compareTo("viewstate")!=0 & k.toLowerCase().compareTo("eval")!=0)
									{
										names.add(k);
										values.add("");
									}
								}
								else{
									if(k.toLowerCase().compareTo("viewstate")!=0 & k.toLowerCase().compareTo("eval")!=0)
									{
										names.add(k);
										values.add(mp.get(k).asString());
									}
								}
				
							}
							
							
							//add search parameter if it exists (the encoding is the same for POST and GET)
							if(current.getBoolState(14) & current.getBoolState(28)==true)
							{
								if(searchterm==null){
									log.info("Adding Searchterm "+searchterm+" as Parameter "+searchparamname+" in Loop");
									names.add(searchparamname);
									values.add(searchterm);
								}
								else{
									log.info("Adding Searchterm "+searchterms.get(0)+" as Parameter "+searchparamname+" in Loop");
									names.add(searchparamname);
									values.add(searchterms.get(0));
								}
								
								if(secondarysearchterm != null){
									names.add(secondarysearchparamname);
									values.add(secondarysearchterm);
								}
							}
							
							
							
							//add extra parameters
							if(current.getBoolState(3))
							{
								log.info("ADDING LOOP EVENT VALIDATION");
								if(current.getLoopparams()!= null)
								{
									mp=Json.read(current.getLoopparams()).asJsonMap();
									
									if(mp.containsKey("eval"))
									{
										if(mp.get("eval").asString().compareTo("true")==0)
										{
											names.add("__EVENTVALIDATION");
											values.add(get.get_event_validation());
										}
									}
									else
									{
										names.add("__EVENTVALIDATION");
										values.add(get.get_event_validation());
									}
									mp=null;
								}
								else
								{
									names.add("__EVENTVALIDATION");
									values.add(get.get_event_validation());
								}
							}
							
							
							if(current.getBoolState(2))
							{
								log.info("ADDING LOOP Params");
								if(current.getLoopparams()!= null)
								{
									mp=Json.read(current.getLoopparams()).asJsonMap();
									
									if(mp.containsKey("viewstate"))
									{
										if(mp.get("viewstate").asString().compareTo("true")==0)
										{
											names.add("__VIEWSTATE");
											values.add(get.get_viewstate());
										}
									}
									else
									{
										names.add("__VIEWSTATE");
										values.add(get.get_viewstate());
									}
									mp=null;
								}
								else
								{
									names.add("__VIEWSTATE");
									values.add(get.get_viewstate());
								}
							}
							
							
							if(current.getBoolState(4))
							{
								log.info("ADDING JAVAX VIEWSTATE");
								if(current.getLoopparams()!= null)
								{
									mp=Json.read(current.getLoopparams()).asJsonMap();
									
									if(mp.containsKey("jview"))
									{
										if(mp.get("jview").asString().compareTo("true")==0)
										{
											names.add("javax.faces.ViewState");
											values.add(get.get_server_faces());
										}
									}
									else
									{
										names.add("javax.faces.ViewState");
										values.add(get.get_server_faces());
									}
									mp=null;
								}
								else
								{
									names.add("javax.faces.ViewState");
									values.add(get.get_server_faces());
								}
							}
							
							log.info("ADDING ANY REMAINING LOOP PARAMETERS");
							if(current.getLoopparam()!= null)
							{
								names.add(current.getLoopparam());
								values.add(Integer.toString(i));
							}
							
							//add the loop regex parameters
							if((current.getLoopregexparams())!= null)
							{
								log.info("ADDING LOOP REGEX PARAMS");
								mp=Json.read(current.getLoopregexparams()).asJsonMap();
								keys=mp.keySet();
								
								for(String k:keys)
								{
									log.info("Param is: "+k+" Reg: "+mp.get(k).asString().trim());
									Pattern p=Pattern.compile(mp.get(k).asString().trim());
									Matcher m=p.matcher(htmlstr);
										
									if(m.find()){
										log.info("Param: "+k+" Value: "+m.group());
										names.add(k);
										values.add(m.group());
									}
									else{
										try{
											throw new BadRegex("Regex not found in Loop Regex Parameters.");
										}catch(BadRegex e){
											e.printStackTrace();
										}
									}
								}
								
							}
							
							//add loop param
							if(current.getLoopparam()!= null)
							{
								names.add(current.getLoopparam());
								values.add(Integer.toString(i));
							}
							
							//add iterated params
							if(current.getIteratedloopparams() != null)
							{
								log.info("ADDING ITERATED PARAMS");
								int startnum=0;
								
								if(current.getIteratedloopparams().containsKey("start"))
								{
									startnum=Integer.parseInt(current.getIteratedloopparams().get("start"));
								}
								
								if(i+startnum>total){
									log.info("Too Many Pages Breaking Loop");
									break;
								}
								String addparam=null;
								for(String param: current.getIteratedloopparams().keySet())
								{
									
									
									if(param.compareTo("start")!=0)
									{
										
										names.add(param);

										if(current.getIteratedloopparams().get(param).contains("PRE:"))
										{
											values.add(Integer.toString((i+startnum))+current.getIteratedloopparams().get(param).replaceAll("PRE:",""));
										}
										else if(current.getIteratedloopparams().get(param).contains("POST:"))
										{
											values.add(current.getIteratedloopparams().get(param).replaceAll("POST:","")+Integer.toString((i+startnum)));
										}
										else if(current.getIteratedloopparams().get(param).contains(":MID:"))
										{
											values.add(current.getIteratedloopparams().get(param).replaceAll(":MID:",Integer.toString((i+startnum))));
										}
										else if(current.getIteratedloopparams().get(param).contains(":MUL")){
											addparam=null;
											
											Pattern p2=Pattern.compile(":MUL([0-9]+):ADD([0-9]+)");
											Matcher m2=p2.matcher(current.getIteratedloopparams().get(param));
											
											if(m2.find()){
												
												int mod=Integer.parseInt(m2.group(1));
												int add=Integer.parseInt(m2.group(2));
												
											
												
												addparam=current.getIteratedloopparams().get(param);
												
												addparam=addparam.replaceAll((":MUL"+Integer.toString(mod)+":ADD"+Integer.toString(add)+":"), Integer.toString((i*mod)+add));
												
			
												if(addparam.contains(":WITH0")){
													addparam=(((i*mod)+add)<10)?addparam.replace(":WITH0","0"):addparam.replace(":WITH0","");
												}
												log.info(addparam);
											}
											else{
												try{
													throw new NullPointerException("Iterated Param Not Found in: "+current.getIteratedloopparams().get(param));
												}catch(NullPointerException e){
													e.printStackTrace();
													System.exit(-1);
												}
											}
											
										}
										else if(current.getIteratedloopparams().get(param).contains(":MOD")){
											//IF A MOD IS SPECIFIED as a loop parameter
											addparam=null;
											
											Pattern p2=Pattern.compile(":MOD([0-9]+):ADD([0-9]+)");
											Matcher m2=p2.matcher(current.getIteratedloopparams().get(param));
											
											if(m2.find()){
												
												int mod=Integer.parseInt(m2.group(1));
												int add=Integer.parseInt(m2.group(2));
												
											
												
												addparam=current.getIteratedloopparams().get(param);
												
												if((i>=mod))
												{
													addparam=addparam.replaceAll((":MOD"+Integer.toString(mod)+":ADD"+Integer.toString(add)+":"), Integer.toString((i%mod)+add));
												
													if(addparam.contains(":WITH0")){
														addparam=(((i%mod)+add)<10)?addparam.replace(":WITH0","0"):addparam.replace(":WITH0","");
													}
												}
												else{
													
													if(addparam.contains(":WITH0")){
														addparam=(i<10)?addparam.replace(":WITH0","0"):addparam.replace(":WITH0","");
													}
			
													addparam=addparam.replaceAll((":MOD"+Integer.toString(mod)+":ADD"+Integer.toString(add)+":"), Integer.toString(i));
												}
												
											}
											
										}
										else
										{
											values.add(Integer.toString((i+startnum)));
										}
									
									}
									
									log.info("Param: "+param+" Value: "+addparam);
									if(addparam != null)
									{
										if(test){
											log.info("ADDPARAM: "+addparam);
										}
										values.add(addparam);
									}
									else if(param.trim().compareTo("start") != 0){
										try{
											throw new NullPointerException("No MOD  Found");
										}catch(NullPointerException e){
											e.printStackTrace();
											System.exit(-1);
										}
									}
								}
							}
							
							//add iterated loopregexparams
							if(current.getIteratedloopregexparams() != null)
							{
								log.info("ADDING ITERATED LOOP REGEX PARAMS");
								//get and set the iteration value
								int itval=0;
								for(String param: current.getIteratedloopregexparams().keySet())
								{
									//set the name
									names.add(param);
									
									//set the value
									mp=Json.read(current.getIteratedloopregexparams().get(param)).asJsonMap();
									for(int j=0;j<mp.size();j++)
									{
										itval=(i==1)?(mp.get("start").asInteger()*mp.get("mul").asInteger())*i:mp.get("start").asInteger();
										log.info("Param: "+param+" Value: "+itval);
										values.add(Integer.toString(itval));
									}
								}
							}
							
							
							//add compact loop regex params
							if(current.getCompactloopregexparams() != null)
							{
								log.info("CONCACTING CONCACTABLE LOOP REGEX PARAMS");
								String res=null;
								
								
								
								for(String param : current.getCompactloopregexparams().keySet())
								{
									mp=Json.read(current.getCompactloopregexparams().get(param)).asJsonMap();
									
									Set<String> params=mp.keySet();
									ArrayList<String> sortparams=new ArrayList<String>();
									sortparams.addAll(params);
									
									qs.setTosortlist(sortparams);
									sortparams=qs.sort();
									
									for(String pat: sortparams)
									{
										log.info("Key: "+pat+" Value: "+mp.get(pat).asString());
										if(pat.toLowerCase().contains("reg"))
										{
											
											Pattern p=Pattern.compile(mp.get(pat).asString().trim());
											Matcher m=p.matcher(htmlstr);
										
											if(m.find())
											{
												res=(res==null)?m.group().trim():res+m.group().trim();
											}
											else
											{
												try{
													throw new BadRegex("In Compact Loop Regex Parameter Creation: "+mp.get(pat).asString()+"\n");
												}catch(BadRegex e)
												{
													e.printStackTrace();
													System.exit(-1);
												}
											}
										}
										else
										{
											res=(res==null)?mp.get(pat).asString().trim():res+mp.get(pat).asString().trim();
										}
									}
									log.info("Param: "+param+" Value: "+res);
									
									names.add(param);
									values.add(res);
								}
							}
							
							if(current.getBoolState(30) & persistantparam != null){
								names.add(persistantname);
								values.add(persistantparam);
							}
							
							for(int j=0;j<values.size();j++){
								if(values.get(j)==null){
									if(test){log.info(names.get(i));}
									values.set(j, "");
								}
							}
							
							log.info("ENCODING LOOP PARAMETERS");
							get.set_url_params(null);
							
							get.encode_url_params(arrListtoStringArr(names),arrListtoStringArr(values));
							log.info("Params: "+get.get_url_params());
						}
						
						//clear parameters and values
						names.clear();
						values.clear();
						
						//add the loop headers
						mp=Json.read(current.getLoopheaders()).asJsonMap();
						keys=mp.keySet();
						
						log.info("ADDING LOOP HEADERS");
						for(String k: keys)
						{
							if(mp.get(k).isString())
							{
								names.add(k);
								
								if(mp.get(k).asString().compareTo("PREVURL")==0)
								{
									names.add(get.getUrl());
								}
								else
								{
									values.add(mp.get(k).asString());
								}
							}
						}
						
						
						
						//add the loop regex headers
						if((current.getLoopregexheaders())!= null)
						{
							log.info("ADDING LOOP REGEX HEADERS");
							mp=Json.read(current.getLoopregexheaders()).asJsonMap();
							keys=mp.keySet();
							for(String k: keys)
							{
								//get each key and set of regex Strings
								if(mp.get(k).isString())
								{
									//add name
									names.add(k);
									
									//add regex string to array but not replacement values
									String[] temp=mp.get(val).asString().split("~");
									
									//compile and find regex
									Pattern p=Pattern.compile(repEscape(temp[0]),Pattern.MULTILINE|Pattern.CASE_INSENSITIVE);
									Matcher m=p.matcher(htmlstr);
								
								   if(m.find())
								   {
									
										val=m.group().trim();
									
										if(temp.length>1)
										{
											if(temp.length >2)
											{
												//perform the replacement
												val=val.replaceAll(temp[1],temp[2]);
											}
											else
											{	
												//perform a replacement if only a to replace is given
												val=val.replaceAll(temp[1],"");
											}
										}
									
										values.add(val);
									}
								    else
								    {
								    	try{
											throw new BadRegex("In Loop Regex Headers \n");
										}catch(BadRegex e)
										{
											e.printStackTrace();
										}
								    }
								
								}
							}
						}
						
						//add extras
						if(current.getBoolState(18)==false)
						{
							names.add("Content-Length");
							values.add(""+get.get_param_size());
						}
						
						if(cookies != null)
						{
							names.add("Cookie");
							values.add(cookies);
						}
						
						//set headers
						get.set_header_names(arrListtoStringArr(names));
						get.set_values(arrListtoStringArr(values));
					}
				
					String loopurl=current.getLoopurl();
					if(current.getLoopURLManips() != null)
					{
						
						log.info("MANIPULATING LOOP URL "+loopurl);
						mp=Json.read(current.getLoopURLManips()).asJsonMap();
					
						if(mp.keySet().size()>0)
						{
						
							for(String fix: mp.keySet())
							{
							
								if(fix.toLowerCase().contains("persistantparam")){
									loopurl+=fix.replace("persistantparam", "")+"="+persistantparam;
								}
								else if(fix.toLowerCase().contains("replace")==false & fix.toLowerCase().contains("TOTALREG") & fix.toLowerCase().contains("add")==false)
								{
									Pattern p=Pattern.compile(mp.get(fix).asString());
									Matcher m=p.matcher(htmlstr.replaceAll("\t|\r|\r\n",""));
								
									if(fix.toLowerCase().contains("suffix"))
									{
										//add a suffix if the htmlstr is not null
										if(m.find())
										{
											loopurl=m.group().trim()+loopurl.trim();
										}
										else
										{
											try{
												throw new BadRegex("Bad Regex for URL Manipulation:\n");
											}catch(BadRegex e)
											{
												e.printStackTrace();
											}
										}
									}
									else if(fix.toLowerCase().contains("prefix"))
									{
										//add a prefix string if the htmlstr is not null
										if(m.find())
										{
											loopurl+=m.group().trim();
										}
										else
										{
											try{
												throw new BadRegex("Bad Regex for URL Manipulation:\n");
											}catch(BadRegex e)
											{
												e.printStackTrace();
											}
										}
								
									}
								
								}
								else if(fix.toLowerCase().contains("searchterm"))
								{
									//if the url manipulation is a searchterm, add the searchterm
									//parameter name is the map value
									if(searchterm != null){
										loopurl+=mp.get(fix).asString().trim().replace("searchterm","")+"="+searchterm;
									}
								}
								else if(fix.contains("REGEX"))
								{
									//if the url manipulation is a regular expression, add the expression
									//parameter name needs to replace any REGEX appendings
									Pattern p2=Pattern.compile(mp.get(fix).asString().replaceAll("REGEX",""));
									Matcher m2=p2.matcher(htmlstr.replaceAll("\t|\r|\r\n|\n|\"",""));
									
									if(m2.find())
									{
										loopurl+=fix.replaceAll("REGEX","")+m2.group().trim().replaceAll("<.*?>","");
									}
								
								}
								else if(fix.contains("add"))
								{
									//add a paramater string (multiple ways are provided for more
									//intuitive use
									loopurl+=mp.get(fix).asString().trim();
								}
								else if(fix.contains("encodeadd"))
								{
									//add an encoded connector
									loopurl+=encode(mp.get(fix).asString().trim());
								}
								else if(fix.contains("connector"))
								{
									//add an unencoded connector
									loopurl+=mp.get(fix).asString().trim();
								}
								else if(fix.contains("encodeconnector"))
								{
									//encode and add a connector
									loopurl+=encode(mp.get(fix).asString().trim());
								}
								else if(fix.contains("replace"))
								{
									//replace anything in the entire url
									String[] reps=mp.get(fix).asString().split("~");
									loopurl.replaceAll(reps[0], reps[1]);
								}
								else if(fix.contains("ITERMULWITH0")){
									//add a parameter with a multiplier to the iterated int
									loopurl+=fix.replaceAll("ITERMULWITH0","")+"="+mp.get(fix).asString().trim()+Integer.toString(i*(mp.get(fix).asInteger())).trim();
								}
								else if(fix.contains("ITERMULNO0")){
									//add a parameter with a multiplier to the iterated int
									if(i != 0){
										loopurl+=fix.replaceAll("ITERMULNO0","")+"="+mp.get(fix).asString().trim()+Integer.toString(i*(mp.get(fix).asInteger())).trim();
									}
									else{
										log.info("i was 0 this term");
									}
								}
								else if(fix.contains("ITERADDMULLNO0")){
									Pattern p2=Pattern.compile("(?<=START)\\d+");
									Matcher m2=p2.matcher(fix);
									
									if(m2.find()){
										
										if(i != 0){
											int add=((mp.get(fix).asInteger()*i)+(Integer.parseInt(m2.group())));
											log.info("param is:"+add);
											loopurl+="&"+fix.replaceAll("ITERADDMULLNO0.START"+m2.group().trim(),"").trim()+"="+Integer.toString(add);
										}
										else{
											log.info("Iteration 0, no Parameters Set");
										}
									}
									else{
										log.warn("A start number in the format START\\d+ must be provided!");
									}
								}
								else if(fix.contains("ITERADDMULLWITH0")){
									Pattern p2=Pattern.compile("(?<=START)\\d+");
									Matcher m2=p2.matcher(fix);
									
									if(m2.find()){
										int add=((mp.get(fix).asInteger()*i)+(Integer.parseInt(m2.group())));
										log.info("param is:"+add);
										fix=fix.replace("ITERADDMULLWITH0|","");
										fix=fix.replace("START"+m2.group().trim(),"");
										loopurl+="&"+fix.replaceAll("ITERADDMULLNO0.START"+m2.group().trim(),"").trim()+"="+Integer.toString(add);									}
									else{
										log.warn("A start number in the format START\\d+ must be provided!");
									}
								}
								else if(fix.contains("ITERABLE"))
								{
									//add the integer as the loop param. in the case that words are before the iterable,
									//use itarable prior or iterablepost. this is a special case for the loop
									loopurl+=fix.replaceAll("ITERABLE","")+"="+Integer.toString(i);
								}
								else if(fix.contains("ITERABLEPRIOR")){
									loopurl+=fix.replaceAll("ITERABLE","")+"="+Integer.toString(i)+mp.get(fix).asString();
								}
								else if(fix.contains("ITERABLEPOST"))
								{
									loopurl+=fix.replaceAll("ITERABLE","")+"="+mp.get(fix).asString()+Integer.toString(i);
								}
								else if(fix.contains("REPTERM")){
									log.info("Adding Term: "+searchterm);
									loopurl=(searchterm!=null)?loopurl.replaceAll("SEARCHTERM", searchterm):loopurl.replaceAll("SEARCHTERM", searchterms.get(0));
								}
								else if(fix.contains("REPITER")){
									int add=0;
									
									if(fix.contains("ADD")){
										Pattern p2=Pattern.compile("ADD\\d+");
										Matcher m2=p2.matcher(loopurl);
										
										if(m2.find()){
											add=Integer.parseInt(m2.group());
											loopurl.replaceAll(m2.group(),"");
										}
									}
									
									loopurl=loopurl.replaceAll("REPITER", Integer.toString((i+add)));
								}
								else if(fix.contains("add")){
									loopurl+=mp.get(fix).asString().trim();
								}
								else
								{
									//this is assumed to be a parameter to be added with value as 
									//the name and value as the value with an = sign separating the values
									loopurl+=fix+"="+mp.get(fix).asString();
								}
							}
							mp=null;
							log.info("LOOP URL IS NOW "+loopurl);
							
							if(current.getLoopURLManips() != null & current.getLoopurl().trim().compareTo(loopurl.trim())==0){
								reg=false;
							}
						}
						else
						{
							try{
								throw new NullPointerException("Missing URL Manipulations or HTML:\n");
							}catch(NullPointerException e)
							{
								e.printStackTrace();
							}
						}
					}
					
					
					//set the page url
					if(current.getBoolState(18))
					{
						//get with query parameters
						if(get.get_url_params()!= null)
						{
							
							if(get.get_url_params().length()>0 & persistparams==true){
								log.info("Setting URl to "+loopurl+get.get_url_params().trim());
								get.set_url(loopurl.trim()+get.get_url_params().trim());
							}
							else{
								log.info("Setting URl to "+loopurl);
								get.set_url(loopurl);
							}
						}
						else
						{
							log.info("Setting URl to "+loopurl);
							get.set_url(loopurl.trim());
						}
				
					}
					else
					{
						//straight post
						log.info("Setting URl to "+loopurl);
						get.set_method("POST");
						get.set_url(loopurl.trim());
					}
					
					//set the proxy
					if(proxy)
					{
						setPullProxy();
					}
					
				
					//get the pages
					htmlstr=getPages();
					
					if(current.getLoopredirect()!=null)
					{
						get.set_method("GET");
						get.set_url(current.getLoopredirect());
						
						//get the pages
						htmlstr=getPages();
						
						if(current.getBoolState(18)==false)
						{
							get.set_method("POST");
						}
						
					}
					
					
					//print obtained url if test is set
					if(test)
					{
						printTestHtml(htmlstr,get.getUrl());
					}
					
					if(persistantparamregex != null){
						Pattern p2=Pattern.compile(persistantparamregex);
						Matcher m2=p2.matcher(htmlstr);
						
						if(m2.find()){
							persistantparam=m2.group();
						}
					}
					
					//add to post requests if the page is marked as to add
					if(current.getBoolState(8))
					{
						obj=new PostObjects();
						obj.setHtml(htmlstr.replaceAll("\t|\r|\r\n|\n|$|\"", ""));
						obj.setLink(get.getUrl());
						obj.setRoot(current.getUrl());
						obj.setDatestamp(DateFormat.getInstance().format(Calendar.getInstance().getTime()));
						obj.setId(genHash(Integer.toString(pageid)));
					
						
						if((htmlstr.contains(mustcontain) | mustcontain.compareTo("~none~")==0) & htmlstr.contains(cannotcontain)==false)
						{
							
							log.info("Added Page.");
							
							
							html.add(obj);
						}
						
						pnum++;
					}
					
					//check the size and send to db if the size is too large
					if(html.size()>commit_size)
					{
						log.info("Adding to Db @ "+Calendar.getInstance().getTime().toString());
						this.addtoDB(html);
						html.clear();
						log.info("Posted to Db @ "+Calendar.getInstance().getTime().toString());
					}
					
					//get the links if requested and add those pages if requested
					if(current.getGetLinkRegex() != null)
					{
						int linkid=0;
						log.info("LOOKING FOR LINKS");
						//create the regex links
						Pattern p=Pattern.compile(repEscape(current.getGetLinkRegex()),Pattern.MULTILINE|Pattern.CASE_INSENSITIVE);
						Matcher m=p.matcher(htmlstr.replaceAll("\t|\r|\n|\r\n", ""));

						
						while(m.find())
						{
							links.add(m.group().trim());
						}
						
						
						//get the links
						for(String link:links)
						{
							
							String linkhtml=null;
							
							//set the found url and replace commonly changed characters back
							link=link.replaceAll("&amp", "&");
							link=link.replaceAll("&%3D","=");
							link=link.replaceAll("%2f", "/");
							
							if(current.getIndilinkprefix()!= null)
							{
								link=(current.getIndilinkreplace() != null)?current.getIndilinkprefix().trim()+link.replaceAll(current.getIndilinkreplace(),""):current.getIndilinkprefix().trim()+link.trim();
							}

							if(current.getIndilinksuffix() != null)
							{
								link=(current.getIndilinkreplace() != null)?current.getIndilinksuffix().trim()+link.replaceAll(current.getIndilinkreplace(),""):current.getIndilinksuffix().trim()+link.trim();
							}
							
							if(!useaddparams){
								log.info("INDIVIDUAL LINK IS: "+link);
								get.set_url(link.trim());
							}
							
							if(useaddparams)
							{
								log.info("Going for: "+link);
								url=additionalparamurl;
								
								ArrayList<String> paramnames=new ArrayList<String>();
								ArrayList<String> paramvals=new ArrayList<String>();
								
								ArrayList<String> regexnames=new ArrayList<String>();
								ArrayList<String> regexvals=new ArrayList<String>();
								
								//find the specified post param in the page
								if(additionalparams != null | additionalregexparams != null)
								{
									
									
									//set additional regex parameters
									if(additionalregexparams != null)
									{
										log.info("GETTING REGEX PARAMS");
										for(String iterval: additionalregexparams.keySet())
										{
											regexnames.add(iterval);
											
											Pattern p2=Pattern.compile(additionalregexparams.get(iterval));
											Matcher m2=p2.matcher(htmlstr.replaceAll("\t|\r|\r\n|\n"," "));
											
											if(m2.find())
											{
												log.info(m2.group());
												regexvals.add(repEscape(repEscape(m2.group().trim())));
											}
										}
									}
									
									if(current.getBoolState(30) & persistantparam != null){
										paramnames.add(persistantname);
										paramvals.add(persistantparam);
									}
						
									//set up additional parameters
									if(additionalparams != null | additionalregexparams != null)
									{
										
										//set up for additional html with post params
										if(additionalparams != null)
										{
											for(String iterval: additionalparams.keySet())
											{
												paramnames.add(iterval);
												paramvals.add(additionalparams.get(iterval));
											}
											
											//add extra parameters
											if(additionaleval)
											{
												paramnames.add("__EVENTVALIDATION");
												paramvals.add(get.get_event_validation());
											}
											
											if(additionalviewstate)
											{
												paramnames.add("__VIEWSTATE");
												paramvals.add(get.get_viewstate());
											}
											
											
											if(current.getBoolState(4))
											{
												paramnames.add("javax.faces.ViewState");
												paramvals.add(get.get_server_faces());
											}
										}
										
										if(additionalregexparams != null & regexnames.size()>0)
										{
											paramnames.addAll(regexnames);
											paramvals.addAll(regexvals);
										}
										
										
									}

								}
								
								if(additionalparamname != null){
									//get params
									if(additionalparamname.contains("regex")){
										paramnames.add(repEscape(link));
									}else{
										paramnames.add(additionalparamname);
									}
									
									paramvals.add(repEscape(link));
								}
								
								if(paramnames != null){
									
									if(paramnames.size()>0){
										get.set_method("POST");
										get.set_url_params(null);
										get.encode_url_params(arrListtoStringArr(paramnames), arrListtoStringArr(paramvals));
										log.info("NEW PARAMS: "+get.get_url_params());
										
									}
									
								}
						
								if((additionalpre != null | additionalpost != null) & additionalparamurl ==null)
								{
									
									if(additionalpre != null)
									{
										url=(url==null)?additionalpre.trim()+url.trim():additionalparamurl+additionalpre.trim()+url.trim();
									}
									
									if(additionalpost != null)
									{
										url=(url==null)?url.trim()+additionalpost.trim():url.trim()+additionalpost.trim();
									}
									log.info("Setting URL to "+url);
									get.set_url(url);
								}
								else if(additionalparamurl != null)
								{
									url=additionalparamurl;
									
									if(additionalurlmanips != null)
									{										
										if(secondarysearchterm != null){
											url=getUrlManips(additionalurlmanips, additionalparamurl, htmlstr, searchterm, i,secondarysearchterm);
										}
										else{
											url=getUrlManips(additionalurlmanips, additionalparamurl, htmlstr, searchterm, i,null);
										}
									
									}
									log.info("Setting URL to "+url);
									get.set_url(url);
								}
								
								//encode url params
								
								log.info("Getting Additional URLS from Params");
							
								//get html
								timeout();
								if(proxy)
								{
									setPullProxy();
								}
						
								if(get.get_html().contains(additionalnotcontain)==false )
								{
									//get the pages
									
									
									if(current.getBoolState(1))
									{
										if(get.get_authority() != null | get.get_host() != null)
										{
											cookies=get.get_secured();
											
											if(additionalredirect != null)
											{
												get.get_html();
												
												Pattern p3=Pattern.compile(additionalredirect);
												Matcher m3=p3.matcher(get.get_html());
												
												if(m3.find())
												{
													get.set_method("GET");
													url=repEscape(m3.group());
													get.set_url_params(null);
													
													if(additionalredirectprefix != null)
													{
														url=additionalredirectprefix+url;
													}
													
													if(additionalredirectsuffix != null)
													{
														url+=additionalredirectsuffix;
													}
													
													if(additionalredirectreplace != null)
													{
														url=url.replaceAll(additionalredirectreplace,additionalredirectreplace);
													}
													log.info("Setting URL to "+url);
													get.set_url(url);
													cookies=get.get_secured();
												}
											}
											
											linkhtml=get.get_html();
											
											if(additionalparams != null)
											{
												get.set_method("POST");
											}
										}
										else
										{
											if(additionalredirect != null)
											{
												log.info("SEARCHING FOR REDIRECT");
												get.get_SSL();
												
												Pattern p3=Pattern.compile(additionalredirect);
												Matcher m3=p3.matcher(get.get_html());
												
												if(m3.find())
												{
													get.set_method("GET");
													get.set_url_params(null);
													
													url=repEscape(m3.group());
													
													if(additionalredirectprefix != null)
													{
														url=additionalredirectprefix+url;
													}
													
													if(additionalredirectsuffix != null)
													{
														url+=additionalredirectsuffix;
													}
													
													if(additionalredirectreplace != null)
													{
														url=url.replaceAll(additionalredirectreplace,additionalredirectreplace);
													}
													
													log.info("FOUND ADDITIONAL REDIRECT");
													get.set_url(url);
												}
												else
												{
													log.info("ADDITIONAL REDIRECT NOT FOUND");
												}
											}
								
											linkhtml=get.get_SSL();
											cookies=get.cookiegrab();
											
											if(additionalparams != null)
											{
												get.set_method("POST");
											}
										}
									}
									else
									{
										log.info("Setting URL to "+url);
										get.set_url(url);
										if(additionalredirect != null)
										{
											log.info("LOOKING FOR REDIRECT");
											cookies=get.get_cookies();
											
											Pattern p3=Pattern.compile(additionalredirect);
											Matcher m3=p3.matcher(get.get_html());
											
											if(m3.find())
											{
												log.info("FOUND REDIRECT");
												get.set_method("GET");
												get.set_url_params(null);
												
												url=repEscape(m3.group());
												
												if(additionalredirectprefix != null)
												{
													url=additionalredirectprefix+url;
												}
												
												if(additionalredirectsuffix != null)
												{
													url+=additionalredirectsuffix;
												}
												
												if(additionalredirectreplace != null)
												{
													url=url.replaceAll(additionalredirectreplace,additionalredirectreplace);
												}
												log.info("Setting URL to "+url);
												get.set_url(url);
											}
											else
											{
												log.info("REDIRECT NOT FOUND");
											}
										}
										
										
										cookies=get.get_cookies();
										linkhtml=get.get_html();
										
										if(additionalparams != null)
										{
											get.set_method("POST");
										}
									}
								}
								
							}
							else
							{
								String params=get.get_url_params();
								if(current.getBoolState(33)){
								
									log.info("Switching Method for Grab");
									if(get.get_method().compareTo("POST")==0){
										get.set_method("GET");
										get.set_url_params(null);
									}
									else{
										get.set_method("POST");
									}
								}
							
								//get the link
								if(proxy)
								{
									setPullProxy();
								}	
							
								linkhtml=getPages();
								
								if(current.getBoolState(33)){
									if(get.get_method().compareTo("POST")==0){
										get.set_method("GET");
									}
									else{
										get.set_method("POST");
										get.set_url_params(params);
									}
								}
							
							}
							
							//print out if desired
							if(test)
							{
								printTestHtml(linkhtml,get.getUrl());
							}
							
							
							String ohash=genHash(Integer.toString(pageid));
							if(current.getIfpath() != null){
								getImages(current,searchterm,ohash,linkhtml);
							}
						
							
							//get the htmlstr
							if(linkhtml != null & i==currenti)
							{
								linkid++;
								
								if(current.getIfpath() != null){
									log.info("Image file path will be: "+current.getIfpath());
								}
								
								if(incolumn){
									for(String str: linkhtml.split("~")){
										ohash=genHash(Integer.toString(pageid));
										if(current.getIfpath() != null){
											getImages(current,searchterm,ohash,str);
										}
										addAdditionalToArray(incolumn,str,pageid,htmlstr,ohash,get.getUrl());
									}
								}
								else{
									ohash=genHash(Integer.toString(pageid));
									addAdditionalToArray(incolumn,linkhtml,pageid,htmlstr,ohash,get.getUrl());
								}

								
										
								//check the size and send to db if the size is too large
								if(html.size()>commit_size)
								{
									log.info("Adding to Db @ "+Calendar.getInstance().getTime().toString());
									this.addtoDB(html);
									html.clear();
									log.info("Posted to Db @ "+Calendar.getInstance().getTime().toString());
								}
							
								linkhtml=null;
								
								pnum++;
							}
							

							
							//check the size and send to db if the size is too large
							if(html.size()>commit_size)
							{
								log.info("Adding to Db @ "+Calendar.getInstance().getTime().toString());
								this.addtoDB(html);
								html.clear();
								log.info("Posted to Db @ "+Calendar.getInstance().getTime().toString());
							}
							
							get.set_html(htmlstr);
						}
						links.clear();
					}
					
					//if there is a capture that fails, get back to this page after submitting the captcha for processing
					
					//if you need to redirect to get the captch, this script will do it using regexes
					if(current.getCaptchaRegex() != null)
					{
						Pattern p;
						Matcher m;
						
						
						
						if(captcharedirectregex != null)
						{
							log.info("Captcha Redirect Detected");
							//redirect 
							String temphtml=null;
							p=Pattern.compile(captcharedirectregex);
							m=p.matcher(htmlstr);
							
							if(m.find())
							{
								log.info("Captcha Redirect Found");
								html_grab g2=new html_grab();
								g2.set_method("GET");
								
								if(captcharedirectprefix != null)
								{
									log.info("Adding Captcha Prefix");
									g2.set_url(captcharedirectprefix.trim()+m.group().trim());
								}
								else
								{
									g2.set_url(m.group().trim());
								}
								
								if(captcharedirectheaders != null)
								{
									log.info("Adding captcha redirect headers");
									String[] headers=new String[captcharedirectheaders.size()];
									String[] headernames=new String[captcharedirectheaders.size()];
									
									Iterator<String> hkeys=captcharedirectheaders.keySet().iterator();
									String tmp;
									for(int j=0;j<captcharedirectheaders.size();j++)
									{
										tmp=hkeys.next();
										headernames[j]=tmp;
										headers[j]=captcharedirectheaders.get(tmp);
									}
									
									g2.set_header_names(headernames);
									g2.setHeaders(headers);
								}
							
								if(proxy){
									setPullProxy();
								}
								
								log.info("Getting Captcha Redirect");
								temphtml=getPages();
							}
							
							log.info("Getting Captcha Redirect");
							
							p=Pattern.compile(repEscape(current.getCaptchaRegex()),Pattern.MULTILINE|Pattern.CASE_INSENSITIVE);
							m=p.matcher(temphtml);
						}
						else{
							log.info("Getting Captcha Redirect");
							p=Pattern.compile(repEscape(current.getCaptchaRegex()),Pattern.MULTILINE|Pattern.CASE_INSENSITIVE);
							m=p.matcher(htmlstr);
						}
						
						if(m.find())
						{
							current=head;
							html.clear();
							while(current.getNext().getCaptchaRegex() == null)
							{
								current=current.getNext();
								
								if(current.hasNext()==false)
								{
									try{
										throw new NoClassSpecified("Regex Node not Found!");
									}catch(NoClassSpecified e)
									{
										e.printStackTrace();
									}
								}
							}
							
						}
					}
					
					
					//check the size and send to db if the size is too large
					if(html.size()>commit_size)
					{
						log.info("Adding to Db @ "+Calendar.getInstance().getTime().toString());
						this.addtoDB(html);
						html.clear();
						log.info("Posted to Db @ "+Calendar.getInstance().getTime().toString());
					}
					
					if(i==currenti)
					{
						i++;
					}
					
					currenti++;
					
					//check if loop should be performed
					if(current.getLoop_reg()==null)
					{
						reg=false;
					}
					else
					{
						log.info(current.getLoop_reg());
						htmlstr=htmlstr.replaceAll("\t|\r\n|\n|\r","");
						Pattern p2=Pattern.compile(current.getLoop_reg(),Pattern.MULTILINE|Pattern.CASE_INSENSITIVE);
						Matcher m2=p2.matcher(htmlstr);
						
						if(!m2.find())
						{
							reg=false;
						}
						else{
							log.info(m2.group());
						}
						
						
					}
				}
				
			}
			
			
			//check to see if links should be obtained and add them
			if(current.getGetLinkRegex()!= null & htmlstr != null)
			{
				
				Pattern p=Pattern.compile(current.getGetLinkRegex().replaceAll("&quot;","\""),Pattern.MULTILINE|Pattern.CASE_INSENSITIVE);
				Matcher m=p.matcher(htmlstr);
				String l=null;
				
				while(m.find())
				{
					//add the links to the arraylist
					if(current.getIndilinkprefix()!= null)
					{
						l=current.getIndilinkprefix();
					}
					l=(l==null)?m.group():l.trim()+m.group().trim();
					if(current.getIndilinksuffix() != null)
					{
						l+=m.group().trim();
					}
					links.add(l);
					l=null;
					
				}
				
				if(current.getBoolState(0))
				{
					//get the links
					for(String link:links)
					{
						//set the found url and replace commonly changed characters back
						link=link.replaceAll("&amp", "&");
						link=link.replaceAll("&%3D","=");
						get.set_url(link.trim());
						
						//get the link
						if(proxy)
						{
							setPullProxy();
						}
						htmlstr=getPages();
						
						String ohash=genHash(Integer.toString(pageid));
						//get the htmlstr
						if(htmlstr != null)
						{
							if(test){
								printTestHtml(htmlstr,get.getUrl());
							}
							
							if(incolumn){
								for(String str: htmlstr.split("~")){
									ohash=genHash(Integer.toString(pageid));
									if(current.getIfpath() != null){
										getImages(current,searchterm,ohash,htmlstr);
									}
									addAdditionalToArray(incolumn,str,pageid,htmlstr,ohash,get.getUrl());
								}
							}
							else{
								ohash=genHash(Integer.toString(pageid));
								addAdditionalToArray(incolumn,htmlstr,pageid,htmlstr,ohash,get.getUrl());
							}
							
							pnum++;
						}
						
						//check the size and send to db if the size is too large
						if(html.size()>commit_size)
						{
							log.info("Adding to Db @ "+Calendar.getInstance().getTime().toString());
							this.addtoDB(html);
							html.clear();
							log.info("Posted to Db @ "+Calendar.getInstance().getTime().toString());
						}
					}
				}
				else
				{
					String ohash=genHash(Integer.toString(pageid));
					//get the htmlstr
					if(htmlstr != null & current.getBoolState(8)==true)
					{
						if(test){
							printTestHtml(htmlstr,get.getUrl());
						}
						
						if(incolumn){
							for(String str: htmlstr.split("~")){
								ohash=genHash(Integer.toString(pageid));
								if(current.getIfpath() != null){
									getImages(current,searchterm,ohash,str);
								}
								addAdditionalToArray(incolumn,str,pageid,htmlstr,ohash,get.getUrl());
							}
						}
						else{
							ohash=genHash(Integer.toString(pageid));
							addAdditionalToArray(incolumn,htmlstr,pageid,htmlstr,ohash,get.getUrl());
						}
						
						if((mustcontain.compareTo("~none~")==0| htmlstr.contains(mustcontain)) & htmlstr.contains(cannotcontain)==false)
						{
							log.info("Added Page.");

							html.add(obj);
						}
						
						pnum++;
					}
					
					//check the size and send to db if the size is too large
					if(html.size()>commit_size)
					{
						log.info("Adding to Db @ "+Calendar.getInstance().getTime().toString());
						this.addtoDB(html);
						html.clear();
						log.info("Posted to Db @ "+Calendar.getInstance().getTime().toString());
					}
				}
			}
			
			if(addextraurl==true & htmlstr != null){
				//get the htmlstr
				if(current.getBoolState(8)==true)
				{
					
					obj=new PostObjects();
					obj.setHtml(htmlstr.replaceAll("\t|\r|\r\n|\n|$|\"", ""));
					obj.setLink(get.getUrl());
					obj.setRoot(current.getUrl());
					obj.setDatestamp(DateFormat.getInstance().format(Calendar.getInstance().getTime()));
					obj.setId(genHash(Integer.toString(pageid)));
					
					if((mustcontain.compareTo("~none~")==0| htmlstr.contains(mustcontain)) & htmlstr.contains(cannotcontain)==false)
					{

						log.info("Added Page.");

						html.add(obj);
					}
					
					pnum++;
				}
				
				//check the size and send to db if the size is too large
				if(html.size()>commit_size)
				{
					log.info("Adding to Db @ "+Calendar.getInstance().getTime().toString());
					this.addtoDB(html);
					html.clear();
					log.info("Posted to Db @ "+Calendar.getInstance().getTime().toString());
				}
			}
			
			//add the page if add is requested, no loop is present, and no additional urls are present
			if(additionalurls==null & current.getBoolState(8)==true & current.getLoop_reg() == null & htmlstr != null)
			{
				log.info("Adding Page");
				
				PostObjects po=new PostObjects();
				po.setHtml(htmlstr.replaceAll("\t|\r|\r\n|\n|$|\"", ""));
				po.setDatestamp(Calendar.getInstance().getTime().toString());
				po.setLink(get.getUrl());
				po.setAdditionalhtml("");
				po.setRoot(get.getUrl());
				po.setId(genHash(Integer.toString(pageid)));
				
				html.add(po);
				
				if(html.size()>commit_size)
				{
					log.info("Adding to Db @ "+Calendar.getInstance().getTime().toString());
					this.addtoDB(html);
					html.clear();
					log.info("Posted to Db @ "+Calendar.getInstance().getTime().toString());
				}
			}
			
			//Post Stats if there was a searchterm
			if(searchterm != null & pageforsearch >0){
				log.info("Posting Stats For a Searchterm. Size: "+pageforsearch);
				String[] schema=table.split("\\.");
				String stats="{\"table\":\""+schema[0].trim()+".searchstats"+"\",\"term\":\""+searchterm+"\",\"pages\":\""+pageforsearch+"\",\"avglength\":\""+Double.toString((averagesearchlength/pageforsearch))+"\",\"timestamp\":\""+Calendar.getInstance().getTime().toString()+"\"}";
				this.jdbcposter.postSingleJson(stats);
			}
			else{
				log.info("Posting Stats For a Searchterm. Size: "+pageforsearch);
				String[] schema=table.split("\\.");
				String stats="{\"table\":\""+schema[0].trim()+".searchstats"+"\",\"term\":\""+searchterm+"\",\"pages\":\""+pageforsearch+"\",\"avglength\":\"0\",\"timestamp\":\""+Calendar.getInstance().getTime().toString()+"\"}";
				this.jdbcposter.postSingleJson(stats);
			}
			
			log.info("Checking for Next Node");
			
			if(current.getParameters() != null)
			{
				if(current.getParameters().containsKey(searchparamname))
				{
					log.info("Getting Page for Searchterm: "+current.getParameters().get(searchparamname));
				}
			}
			
			//get the next node or iterate the search term of the current node
			if((current.getBoolState(23)==true & searchterms.size()>0 & searchnodenum==0)|(current.getBoolState(23)==false))
			{
				addparam=true;
			}
			
			
			if(current.getBoolState(23)==true & searchterms.size()>0 & addparam==false)
			{
				//switch to the current head after setting a search parameter
				log.info("REVERTING TO HEAD");
				
				current.removeParameter(searchparamname);
				
				current=head;
				addparam=true;
				
				//if set to reset cookies on revert, then reset the cookies
				if(current.getBoolState(24))
				{
					get.reset_cookies();
				}
			}
			else if(current.getBoolState(23)==true & addparam==true) 
			{	
				if(head.equals(current))
				{
					current=current.getNext();
				}
				else if(current.getBoolState(12) & searchterms.size()>0 & current != null)
				{
			
				 searchnodenum++;
				 
				 //if the appropriate boolstate is found: switch the add param in case the boolstate requests it--> this may be the case if a certain parameter is being tracked
				 addparam=(current.getBoolState(23))?false:true;
				 
				/*iterate the searchterm since this was set*/
				if(current.getBoolState(14)==true)
				{
				
					/*Set the */
					//set the terms if they exist
					log.info("Searchterms Size: "+searchterms.size());

					if(searchterms.size()>0){
						
						if(searchterms.get(0) != null){
							log.info("Term: "+searchterms.get(0).trim());
						}
					}
					
					if(searchterms.size()>0)
					{
						
						if(searchparamname != null){
							current.removeParameter(searchparamname);
						}
						
						if(secondarysearchparamname != null){
							current.removeParameter(secondarysearchterm);
						}
						
						//set up the current node for the next search term
						if(current.getBoolState(14) & searchterms.get(0) != null)
						{
							//add the new parameter
							log.info("Adding Search Parameters "+searchterms.get(0));
							current.addParameter(searchparamname, searchterms.get(0).trim());
							searchterm=searchterms.remove(0);
							
							if(secondarysearchparamname != null){
								log.info("Adding Secondary Search Parameter");
								current.addParameter(secondarysearchparamname, secondarysearchterms.get(0).trim());
								secondarysearchterms.remove(0);
							}
							
						}
					
					}
					else
					{
						//no command for search addition found or size is 0, iterate to next node
						if(current.hasNext())
						{
							current=current.getNext();
						}
						else
						{
							current=null;
						}
					
						if(searchterms != null & current != null)
						{
							if((searchterms.size()==0  & (current.getBoolState(13)==true|current.getBoolState(14)==true))|(searchterms.size()>0  & (current.getBoolState(13)==false & current.getBoolState(14)==false)))
							{
								current=null;
							}
						}
					}
				
				}
				
				}
				
			}
			else if(current.getBoolState(13)==true | current.getBoolState(14)==true & current.getBoolState(23)==false)
			{
				/*Set the */
				//set the terms if they exists
				log.info("Searchterms Size: "+searchterms.size());
				
				if(searchterms.size()>0){
					if(searchterms.get(0)!= null){
						log.info("Term: "+searchterms.get(0).trim());
					}
				}
				
				
					
				if(searchterms.size()>0)
				{
					if(searchterms.get(0) != null){
						//set up the current node for the next search term
						if(current.getBoolState(14))
						{
							//add the new parameter
							searchterm=searchterms.get(0).trim();
							log.info("Adding Search Parameters "+searchterms.get(0));
							current.addParameter(searchparamname, searchterm);
							searchterms.remove(0);
						
							if(secondarysearchparamname != null){
								log.info("Adding Secondary Search Term");
								secondarysearchterm=secondarysearchterms.get(0).trim();
								current.addParameter(secondarysearchparamname, secondarysearchterm);
							}
						
						}
				
					}
					else
					{
						//no command for search addition found or size is 0, iterate to next node
						if(current.hasNext())
						{
							current=current.getNext();
						}
						else
						{
							current=null;
						}
				
						if(searchterms != null & current != null)
						{
							if((searchterms.size()==0  & (current.getBoolState(13)==true|current.getBoolState(14)==true))|(searchterms.size()>0  & (current.getBoolState(13)==false & current.getBoolState(14)==false)))
							{
								current=null;
							}
						}
					}
				}
				else if(searchterms.size()==1){
					current=null;
				}
				else if(searchterms.size()>1){
					
					while(searchterms.get(0)==null){
						searchterms.remove(0);
					
						if(searchterms.size()==0){
							break;
						}
					}
					
					if(searchterms.size()==0){
						current=null;
					}
					else if(searchterms.get(0)==null){
						current=null;
					}
					else{
						
						//set up the current node for the next search term
						if(current.getBoolState(14))
						{
							//add the new parameter
							searchterm=searchterms.get(0).trim();
							log.info("Adding Search Parameters "+searchterms.get(0));
							current.addParameter(searchparamname, searchterm);
							searchterm=searchterms.remove(0);
						
							if(secondarysearchparamname != null){
								log.info("Adding Secondary Search Term");
								secondarysearchterm=secondarysearchterms.get(0).trim();
								current.addParameter(secondarysearchparamname, secondarysearchterm);
							}
						
						}
						
					}
					
				}
			}
			else if(current.hasNext())
			{

				if(current.getBoolState(7))
				{
					//the current node is marked as critical to the pull

					if(head.getBoolState(7))
					{
						//the head is also marked as critical so don't do anything since non-critical nodes are deleted
						current=current.getNext();	
					}
				}
				else
				{

					//the current node is not critical: delete it and move the previous pointer forward one
					//if the current node is the head set the head one forward
					
					//if non-critical, this node will be removed and will not be restarted if the loop calls for it
					PullObject temp=head;
					
					if(current.equals(head))
					{
						//the current node is the head
						current=current.getNext();
						head.setNext(null);
						head=current;
					}
					else
					{
						//since non critical go to the last node and point it one forward, then grab that next node
						
						while(temp.getNext().equals(current)==false)
						{
							temp=temp.getNext();
						}
						
						temp.setNext(temp.getNext().getNext());
						current=temp.getNext();
					}
				}
			}
			else
			{
				current=null;
			}
				
			if(test)
			{
				printTestHtml(htmlstr,get.getUrl());
			}
			
			if(current != null)
			{
					//reset certain parameters
					if((current.getBoolState(10)==true & get.get_method().compareTo("GET")!= 0)|(current.getBoolState(10)==false & get.get_method().compareTo("POST") !=0))
					{
						//change post/get parameters
						if(current.getBoolState(10)==false)
						{
							get.set_method("POST");
						}
						else 
						{
							get.set_method("GET");
							get.set_url_params(null);
						}
					}
					
					if(current.getBoolState(6)==true)
					{
						//reset cookies
						get.reset_cookies();
					}
					
		
					if(current.getCaptchaRegex() != null)
					{
						
						//if there is a captcha regex, get the captcha and post if found using the contents in the next node
						String temp=null;
						
						Pattern p;
						Matcher m;
						
						if(captcharedirectregex != null | recaptcharedirecturl != null)
						{
							log.info("Captcha Redirect Detected");
							//redirect 
							String temphtml=null;
							
							html_grab g2=new html_grab();
							
							if(captcharedirectregex != null){
								p=Pattern.compile(captcharedirectregex);
								m=p.matcher(htmlstr);
							
								
							
								if(m.find())
								{
									log.info("Captcha Redirect Found at "+m.group());
								
									g2.set_method("GET");
								
									if(captcharedirectprefix != null)
									{
										log.info("Adding Captcha Prefix");
										g2.set_url(captcharedirectprefix.trim()+m.group().trim());
									}
									else
									{
										g2.set_url(m.group().trim());
									}
								
									if(captcharedirectheaders != null)
									{
										log.info("Adding captcha redirect headers");
										String[] headers=new String[captcharedirectheaders.size()];
										String[] headernames=new String[captcharedirectheaders.size()];
									
										Iterator<String> hkeys=captcharedirectheaders.keySet().iterator();
										String tmp;
										for(int j=0;j<captcharedirectheaders.size();j++)
										{
											tmp=hkeys.next();
											headernames[j]=tmp;
											headers[j]=captcharedirectheaders.get(tmp);
										}
									
										g2.setValues(headers);
										g2.set_header_names(headernames);
									
										if(proxy){
											setPullProxy();
										}
									
										log.info("Getting Captcha Redirect");
										if(get.get_cookies()!= null)
										{		
											g2.reset_cookies();
											g2.set_cookies(get.get_cookies());
										}else
										{	
											g2.reset_cookies();
										}
									
										temphtml=getPages(g2);
									}
							
									if(test)
									{
										printTestHtml(temphtml,g2.getUrl());
									}
									
								}
							}
							
							String paramhtml=null;
							
							if(recaptcharedirecturl !=null){
								log.info("FOUND RECAPTCHA REDIRECT REQUEST");
								Pattern u=Pattern.compile(recaptcharedirecturl);
								Matcher um=u.matcher(htmlstr);
								
								if(um.find())
								{
									if(recaptcharedirecturlpre==null)
									{
										g2.set_url(um.group().trim());
									}
									else{
										g2.set_url(recaptcharedirecturlpre.trim()+um.group().trim());
									}
									

									if(proxy){
										setPullProxy();
									}
									
									log.info("Getting Captcha Redirect");
									if(get.get_cookies()!= null)
									{	
										g2.reset_cookies();
										g2.set_cookies(get.get_cookies());
									}else
									{	
										g2.reset_cookies();
									}
									
									if(captcharedirectheaders != null)
									{
										log.info("Adding captcha redirect headers");
										String[] headers=new String[captcharedirectheaders.size()];
										String[] headernames=new String[captcharedirectheaders.size()];
									
										Iterator<String> hkeys=captcharedirectheaders.keySet().iterator();
										String tmp;
										for(int j=0;j<captcharedirectheaders.size();j++)
										{
											tmp=hkeys.next();
											headernames[j]=tmp;
											headers[j]=captcharedirectheaders.get(tmp);
										}
										
										g2.setValues(headers);
										g2.set_header_names(headernames);
									
										if(proxy){
											setPullProxy();
										}
									
										log.info("Getting Captcha Redirect");
										if(get.get_cookies()!= null)
										{		
											g2.reset_cookies();
											g2.set_cookies(get.get_cookies());
										}else
										{	
											g2.reset_cookies();
										}
									
										temphtml=getPages(g2);
									}
								
									
									if(recaptcharefreshurl != null){
										log.info("REDIRECT RECAPTCHA REDIRECT TO GET PARAM");
										u=Pattern.compile(recaptcharefreshurl);
										um=u.matcher(temphtml);
										
										while(um.find()){
											log.info("Found "+um.group());
											g2.set_url(um.group().trim());
											
											if(proxy){
												setPullProxy();
											}
											
											log.info("Getting Recaptcha Redirect");
											if(get.get_cookies()!= null)
											{	
												g2.reset_cookies();
												g2.set_cookies(get.get_cookies());
											}else
											{	
												g2.reset_cookies();
											}
											
											log.info("Getting Redirect for Recaptcha");
											paramhtml=getPages(g2);
											
											um=u.matcher(paramhtml);
										}
										
									}else{
										paramhtml=temphtml;
									}
									
									log.info("Getting Recaptcha Parameter");
									if(paramhtml != null){
										
										u=Pattern.compile(recaptchaurlregex);
										um=u.matcher(paramhtml.trim());
												
										if(um.find())
										{
												recaptchaparam=um.group().trim();
												log.info("FOUND PARAM "+recaptchaparam);
										}else
										{
											log.info("RECAPTCHA PARAMETER NOT FOUND \n");
											
											if(test){
												
												printTestHtml(paramhtml,g2.getUrl());
											}
										}
									}else{
										log.info("RECAPTCHA REDIRECT NOT FOUND \n");
										
										if(test){
											
											printTestHtml(paramhtml,g2.getUrl());
										}
									}
									
								}
								else{
									log.info("COULD NOT FIND RECAPTCHA JSON OBJECT URL");
									
									if(test){
										printTestHtml(htmlstr,g2.getUrl());
									}
								}
							}
							
							if(test){
								if(test)
								{
									printTestHtml(paramhtml,g2.getUrl());
								}
							}
							
							log.info("Getting Captcha Redirect");
							
							p=Pattern.compile(repEscape(current.getCaptchaRegex()),Pattern.MULTILINE|Pattern.CASE_INSENSITIVE);
							m=p.matcher(paramhtml);
							
						
						}
						else{
							p=Pattern.compile(repEscape(current.getCaptchaRegex()),Pattern.MULTILINE|Pattern.CASE_INSENSITIVE);
							m=p.matcher(htmlstr);
						}
						
						if(m.find())
						{
							//delete from the captchaanswers database if there was not a success
							if(captchaurl != null & cresponse != null)
							{
								//delete from answers
								String[] resp=cresponse.split("~");
								log.info("WARNING: Captcha found and Previous Captcha Entered");
								
								String sql="DELETE FROM data.captchaanswers WHERE id='"+resp[1]+"' AND answer='"+resp[0]+"'";
								
								this.jdbcposter.execute(sql);
								
							}
							
							captchaurl=(current.getCaptchaname().trim()+Long.toString(Calendar.getInstance().getTimeInMillis()).trim()+".jpg".trim());
							
							log.info("Captcha Found. This could mean A. Bad information entered before | B. Error in Setup | C. New Captcha Encountered");
							
							temp=m.group().trim();
							if(current.getCaptchaPrefix() != null)
							{
								temp=current.getCaptchaPrefix().trim()+temp.trim();
							}
	
							log.info("Captcha URL: "+temp);
							log.info("Captcha Upload URL: "+captchaurl);
							
							
							//set the proxy for getting the captcha image
							if(proxy)
							{
								setPullProxy();
							}
							
							//get the captcha image
							if(current.getBoolState(1))
							{
								//get the image with ssl
								if(current.getAuthority()!= null)
								{
									//get a secured image requiring an authority: ensure that remaining page grabs are set properly for next loop iteration
									if(get.get_authority()==null)
									{
										get.set_authority(current.getAuthority());
										get.set_host(current.getHost());
									}
									
									get.set_url(temp);
									get.get_secured();
									
									//convert image to bytes for nio upload
									imgbytes=get.get_html().getBytes();
								}
								else
								{
									
									if(captchaheaders != null)
									{
										//download ssl without need to change trust manager (have fun with my evolving code philosophy here, sorry)
										DownloadImage down=new DownloadImage();
										down.setCookies(get.cookiegrab());
										
										String[] headernames=new String[captchaheaders.size()];
										String[] headervals=new String[captchaheaders.size()];
										
										log.info("Adding Captcha Headers");
										
										
										int h=0;
										for(String header: captchaheaders.keySet())
										{
											headernames[h]=header;
											headervals[h]=captchaheaders.get(header);
											h++;
										}
										
										if(h != 0){
											down.set_header_names(headernames);
											down.set_values(headervals);
										}
										else{
											log.info("NO CAPTCHA HEADERS FOUND");
										}
										
										//convert image to bytes for nio upload
										log.info("Downloading");
										
										imgbytes=down.get_ssl_page(temp).getBytes();
									}else{
										//having some trouble with the bytes returned in ssl input
										try {
											log.info("Downloading");
											ByteArrayOutputStream baos=new ByteArrayOutputStream();
											ImageIO.write(ImageIO.read(new URL(temp)),"jpg",baos);
											imgbytes=baos.toByteArray();
										} catch (MalformedURLException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										} catch (IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
									
						
								}
							}
							else
							{
								//get the image with normal html
								DownloadImage down=new DownloadImage();
								down.setCookies(get.cookiegrab());
								down.setUrl(temp.trim());

								if(captchaheaders==null)
								{
									log.info("Retrieving Captcha at "+temp);
									down.download_as_html(get.getHeaders(), get.getValues());
								}
								else
								{
									log.info("Adding Captcha Headers");
									String[] hnames=new String[captchaheaders.size()];
									String[] vnames=new String[captchaheaders.size()];
									int h=0;
									for(String k: captchaheaders.keySet())
									{
										hnames[h]=k;
										vnames[h]=captchaheaders.get(k);
										h++;
									}
									
									log.info("Retrieving Captcha at "+temp);
									down.download_as_html(hnames,vnames);
								}
								
								//get byte string for nio upload
								imgbytes=down.get_ibytes();
								
							}

						if(temp != null)
						{
							if(recaptchaurlregex != null & recaptcharedirecturl ==null){
								Pattern u=Pattern.compile(recaptchaurlregex);
								Matcher um=u.matcher(temp);
								if(um.find()){
									log.info("Setting ReCaptcha Param "+um.group());
									recaptchaparam=um.group();
								}else{
									log.info("Could Not Find Recaptcha Param");
								}
							}
							
								
							//get the image bytes from the sftp server (a variety of methods are available
							SFTP sftp=new SFTP(sftpdir,sftpuser,sftppass,sftpurl);
							sftp.uploadBytes(imgbytes, captchaurl);
							sftp.disconnect();
							
							log.info("Querying Captcha DB @ "+Calendar.getInstance().getTime().toString());
							try {

										
									//log.info("Host "+InetAddress.getByName("localhost").getHostName()+": "+InetAddress.getByName("localhost").getHostAddress()+" Port: 1080");
									String id=genCaptchaIdSQL();
									String sql="INSERT INTO data.captchaurls(url,id,datestamp,lastupdate) Values('"+captchaurl+"','"+id+"','"+Long.toString(Calendar.getInstance().getTimeInMillis())+"','"+Long.toString(Calendar.getInstance().getTimeInMillis())+"')";
									log.info("Adding url to SQL");
									jdbcposter.execute(sql);
									log.info("Retrieved Id");
									int runs=0;
									
									log.info("Waiting for Response");
										
									while(cresponse==null)
									{
										if(runs==20)
										{
											//if the timeout runs for 5 minutes, throw a new error
											try{
													
												throw new UserInputNotObtainedException("Could not Get Captcha Response in 5 minutes! Get them Lazy Bums to Work :) \n");
													
											}catch(UserInputNotObtainedException e)
											{
												e.printStackTrace();
												System.exit(-1);
											}
										}
										
										sql="SELECT answer FROM data.captchaanswers WHERE id='"+id+"'";
											
										cresponse=this.jdbcposter.getColumn(sql,"answer");
											
										if(cresponse != null){
											
											//delete from captchaurls in case this fails from the server side
											sql="DELETE FROM data.captchaurls WHERE id='"+id+"'";	
											this.jdbcposter.execute(sql);
											
											cresponse+="~"+id;	
										}
										else
										{
											//sleep for 15 seconds for a total max timeout of 20 runs at 5 minutes
											Thread.sleep(15000);
										}
											
										runs++;
											
									}
										
									log.info("Response Loop Terminated");
										
									if(cresponse==null)
										log.info(" No Answer! Expect Restart");
									else
										log.info("Answer: "+cresponse);
									
								//log.info(cresponse);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
								
							log.info("Response from Captcha Server: "+cresponse+" Received @"+Calendar.getInstance().getTime().toString());
							
							//use the contents of the next node
							if(cresponse != null)
							{
								String[] resp=cresponse.split("~");
								current.addParameter(current.getCaptchaParam(), resp[0].trim());
								
								if(current.getCaptchahash()!= null)
								{
									p=Pattern.compile(current.getHash());
									m=p.matcher(htmlstr);
									
									if(m.find()){
										current.addParameter(current.getCaptchahash(), m.group());
									}
									else
									{
										try{
											throw new NullPointerException("Captcha Hash Not Found. Exiting!");
										}catch(NullPointerException e){
											
											System.exit(-1);
										}
									}
								}

							}
						}
					}
					else
					{
						try{
							throw new BadRegex("In Captcha Pull: \n");
						}catch(BadRegex e)
						{
							e.printStackTrace();
						}
					}
						
				}

		}
			
			averagesearchlength=0;
			pageforsearch=0;
		}
		
		//Post Stats if there was a searchterm
		if(totalpages>0){
			log.info("Posting Stats For a Searchterm.");
			String[] schema=table.split("\\.");
			String stats="{\"table\":\""+schema[0].trim()+".searchstats\""+",\"term\":\"TOTALS\",\"pages\":\""+totalpages+"\",\"avglength\":\""+Double.toString((totalsearchlength/totalpages))+"\",\"timestamp\":\""+Calendar.getInstance().getTime().toString()+"\"}";
			this.jdbcposter.postSingleJson(stats);
		}
		else{
			log.info("Posting Stats For a Searchterm. Expect No Results.");
			String[] schema=table.split("\\.");
			String stats="{\"table\":\""+schema[0].trim()+".searchstats\""+",\"term\":\"TOTALS\",\"pages\":\""+totalpages+"\",\"avglength\":\"0\",\"timestamp\":\""+Calendar.getInstance().getTime().toString()+"\"}";
			this.jdbcposter.postSingleJson(stats);
		}
		
		
		if(html.size() > 0)
		{
			log.info("Adding to Db @ "+Calendar.getInstance().getTime().toString());
			this.addtoDB(html);
			html.clear();
			log.info("Posted to Db @ "+Calendar.getInstance().getTime().toString());
		}
				
		log.info("Completed Pull @"+Calendar.getInstance().getTime().toString());
		log.info("RECORDS FOUND: "+Integer.toString(pnum));

	}
}
