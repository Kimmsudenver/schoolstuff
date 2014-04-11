package com.hygenics.exceptions;

public class CrawlInterruptedException extends Exception{
	
	public CrawlInterruptedException()
	{
		super("Crawl Interrupted Fatally.");
	}
	
	public CrawlInterruptedException(String e)
	{
		super("Crawl Fatally Interrupted."+e);
	}

}
