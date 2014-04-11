package com.hygenics.crawler;


import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The callable class that implements the regex.
 * @author aevans
 *
 */

public class ParseLinkReg extends RecursiveTask<String[]>{

	private Pattern p;
	private String html;
	private String pullid;
	
	public ParseLinkReg(Pattern p, String html,String pullid)
	{
		super();
		this.pullid=pullid;
		this.p=p;
		this.html=html;
	}

	@Override
	public String[] compute() {
		// TODO Auto-generated method stub
		String[] links=new String[2];
		String l=null;
		
		if(p != null & html != null)
		{
			Matcher m=p.matcher(html);
		
			while(m.find())
			{
				l=(l==null)?m.group():l+"|"+m.group().replaceAll("\\|", ":*:");
			}
			links[1]=l;
		}
		
		if(links[1] != null)
		{
			links[0]=pullid;
		}
		
		return links;
	}
	
	
	
	
}
