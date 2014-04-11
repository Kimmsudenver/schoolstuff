package com.hygenics.crawler;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;

import com.hygenics.crawlerobjects.CrawlObject;
import com.hygenics.crawlerobjects.PostObjects;

/**
 * This is Where a Traditional Crawler Goes. It may be possible to automated the db pull by writing an AI program that can script xml by looking at a page.
 * 
 * The crawler takes in a map of commands in the format Map<crawllink,Map<commandname,commands>> all in strings. One of the commands
 * must be the output table for a database. There is no particular order required for the map but the following are required:
 * 
 *     a. "table" - your table name
 *     b. "headers" -in json format --> host and referer are derived from the url and/or previous url
 *     c. (for posts) "parameters" -in json format [includes viewstates, evals, and javax server faces]
 *     d. (for posts) "regexparameters" -in json format [includes viewstates,evals, and javax server faces]
 * 	
 * The following are optional:
 * 	   b. "urlmanips" -url manipulations
 * 	   c. "cannotcontain" -string a page cannot contain
 * 	   d. "mustcontain" -string a page must contain
 * 	   e. "timeout" - automatic is 2000 milliseconds (0 is not friendly but won't kill you)
 * 	   f. "captcharegex" -for a captcha send through the server and in the future hopefully an OCR 
 * 	   g. "captchaprefix" - the captcha prefix
 * 	   h. "captchaparamname" - the captcha paramater --> the page is found to post to when this is hit
 * 
 * ***timeouts are also a property and that is where the automatic property is set
 * ***hitting a captcha multiple times will kill the process
 * ***This will not handle scraping/crawling a database that is for the GetPages class to handle
 * 
 * A map of headers is also required. Post commands are accepted
 * 
 * The traditional Crawler will Create a page. Add it to the Page Node and send to the database.
 * I purposefully Limited the GetPages crawler becuase of its massive size so that you are 
 * using only the most proper number of resources and not needing to code extra xml.
 * 
 * Supply multiple urls with a map of Links to Follow and this thing will get them all.
 * It is impossible to supply the same starting page more than once and please limit the map
 * to one site per entry for friendliness. There is a blacklist.
 * 
 * The database connection is threaded so higher commit sizes (over 10 but size dependent) are recommended.
 * 
 * Additional url manipuations are supported.
 * 
 * @author aevans
 *
 */
public class TraditionalCrawler {
	
	Map<String, Map<String,String>> urls;
	
	private int sqlnum=10;
	private int numprocs=2;
	private int commit_size=100;
	
	private int timeout=2000;
	
	
	private getDAOTemplate template;
	
	public TraditionalCrawler()
	{
		
	}
	
	/**
	 * The actual crawler, uses Recursive action due to intensity
	 * @author aevans
	 *
	 */
	private class SplitPost extends RecursiveTask<ArrayList<PostObjects>>{

		private final String url;
		private final Map<String,String> links;
		private ArrayList<CrawlObject> nodes=new ArrayList<CrawlObject>();
		private int timeout;
		
		public SplitPost(final String url, final Map<String,String> links, final int timeout)
		{
			this.timeout=timeout;
			this.url=url;
			this.links=links;
		}
		
		@Override
		protected ArrayList<PostObjects> compute() {
			// TODO crawl a page generating page objects along the way
			ArrayList<PostObjects> po=new ArrayList<PostObjects>();
			
			
			return po;
		}
		
	}
	
	
	
	public int getTimeout() {
		return timeout;
	}


	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}


	public Map<String, Map<String, String>> getUrls() {
		return urls;
	}


	public void setUrls(Map<String, Map<String, String>> urls) {
		this.urls = urls;
	}


	public int getSqlnum() {
		return sqlnum;
	}


	public void setSqlnum(int sqlnum) {
		this.sqlnum = sqlnum;
	}


	public int getNumprocs() {
		return numprocs;
	}


	public void setNumprocs(int numprocs) {
		this.numprocs = numprocs;
	}


	public int getCommit_size() {
		return commit_size;
	}


	public void setCommit_size(int commit_size) {
		this.commit_size = commit_size;
	}


	public getDAOTemplate getTemplate() {
		return template;
	}


	public void setTemplate(getDAOTemplate template) {
		this.template = template;
	}


	private void crawl()
	{
		ForkJoinPool fjp=new ForkJoinPool(numprocs);
		
	}
	
	
	public void run()
	{
		crawl();
	}

}
