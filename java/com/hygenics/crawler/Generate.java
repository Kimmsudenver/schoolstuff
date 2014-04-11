package com.hygenics.crawler;

import java.util.ArrayList;
import java.util.concurrent.Callable;

public class Generate implements Callable<ArrayList<String>>{

	private int start;
	private int end;
	private int loops;
	private String base;
	private boolean isNum;
	
	public Generate(int start, int end, int loops, String base,boolean isNum)
	{
		this.start=start;
		this.end=end;
		this.loops=loops;
		this.base=base;
		this.isNum=isNum;
	}


	public ArrayList<String> call() throws Exception {
		// TODO Auto-generated method stub

		ArrayList<String> newlist=new ArrayList<String>();
		
		
		for(int i=this.start;i<=this.end;i++)
		{
			if(isNum==false)
			{
				newlist.add((base+String.valueOf((char)i)));
			}
			else
			{
				newlist.add(base+Integer.toString(i));
			}
		}
		
		return newlist;
	}
}
