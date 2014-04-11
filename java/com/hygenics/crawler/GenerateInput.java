package com.hygenics.crawler;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.slf4j.*;

import javax.sql.DataSource;

import com.hygenics.exceptions.*;

/**
 * This class generates input in for loops from specified parameters
 * @author aevans
 *
 */
public class GenerateInput implements Runnable{

	private final static Logger log=LoggerFactory.getLogger(MainApp.class);
	
	private boolean isNum=false;
	private int commit_size=100;
	private String table;
	private int increment=5;
	private boolean overwrite=false;
	private String savepath;
	private String name;
	private String fpath;
	private int length;
	private int loops;
	private int start=65;
	private int end=90;
	private boolean isFile=false;
	private String append;
	private String valname;
	private ArrayList<String> results;
	private getDAOTemplate template;
	
	public GenerateInput()
	{
		
	}
	
	
	
	public boolean isNum() {
		return isNum;
	}



	public void setIsNum(boolean isNum) {
		this.isNum = isNum;
	}



	public int getIncrement() {
		return increment;
	}



	public void setIncrement(int increment) {
		this.increment = increment;
	}



	public int getCommit_size() {
		return commit_size;
	}



	public void setCommit_size(int commit_size) {
		this.commit_size = commit_size;
	}



	public String getTable() {
		return table;
	}



	public void setTable(String table) {
		this.table = table;
	}



	public String getValname() {
		return valname;
	}



	public void setValname(String valname) {
		this.valname = valname;
	}



	public void setTemplate(getDAOTemplate template)
	{
		this.template=template;
	}
	

	public String getName() {
		return name;
	}




	public void setName(String name) {
		this.name = name;
	}




	public String getSavepath() {
		return savepath;
	}



	public void setSavepath(String savepath) {
		this.savepath = savepath;
	}



	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getLoops() {
		return loops;
	}

	public void setLoops(int loops) {
		this.loops = loops;
	}

	public ArrayList<String> getResults() {
		return results;
	}

	public void setResults(ArrayList<String> results) {
		this.results = results;
	}
	
	
	
	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}


	public String getAppend() {
		return append;
	}

	public void setAppend(String append) {
		this.append = append;
	}

	public String getFpath() {
		return fpath;
	}

	public void setFpath(String fpath) {
		this.fpath = fpath;
	}

	public boolean isFile() {
		return isFile;
	}

	public void setIsFile(boolean isFile) {
		this.isFile = isFile;
	}
	
	

	public boolean isOverwrite() {
		return overwrite;
	}



	public void setOverwrite(boolean overwrite) {
		this.overwrite = overwrite;
	}

	public void sendToDb(ArrayList<String> strs, Boolean check)
	{
		if(valname != null & strs != null & table != null)
		{
			//check for table
			if(check == true)
			{
				
				if(template.columnExists(table, valname)==false)
				{
					template.execute("ALTER TABLE "+table+" ADD COLUMN "+valname+" text");
				}
			}
			
			//check that strings is not "null" e.g. size is 0
			if(strs.size()>0)
			{
				log.info("INSERTING "+strs.size()+" ROWS");
				
				template.batchUpdateSingle(table, strs, valname, commit_size);
			}
			else
			{
				try{
					throw new NullPointerException("No Values in ArrayList");
				}catch(NullPointerException e)
				{
					e.printStackTrace();
				}
			}
		}
	}


	private void loopGen()
	{
		log.info("Generating Loop");
		ExecutorService services=Executors.newCachedThreadPool();
		
		Collection<Callable<ArrayList<String>>> set=new HashSet<Callable<ArrayList<String>>>();
		results=new ArrayList<String>();
		
		
		//create the initial loop
		for(int i=start;i<=end;i++)
		{
			if(isNum==false)
			{
				results.add(String.valueOf((char)i));
			}
			else
			{
				results.add(Integer.toString(i));
			}
		}
		
		int j=start;
		
		
		ArrayList<String> newarr=new ArrayList<String>();
		//creates the extra loops
		for(int k=1;k<loops;k++)
		{
			newarr.clear();
			try {
				for(String base:results)
				{

					while(j<end)
					{
						set.add(new Generate(j,(j+increment),loops,base,isNum));
						j+=increment;
					}
					
					
					j=start;
				}
			
			
				List<Future<ArrayList<String>>>list =services.invokeAll(set);
				services.awaitTermination(2000, TimeUnit.MILLISECONDS);
				services.shutdown();
				for(Future<ArrayList<String>> f: list)
				{
					for(String s: f.get())
					{
						newarr.add(s);
					}
				}
				
				if((k+1) != loops)
				{
					services=Executors.newCachedThreadPool();
				}
		
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			j=start;
			results=(ArrayList<String>)newarr.clone();
		}
		
		services.shutdownNow();
		
		
		if(append != null)
		{
			for(int i=0;i<results.size();i++)
			{
				results.add(i, results.get(i)+append);
			}
		}
		
		if(table != null)
		{
			log.info("Sending to DB @"+Calendar.getInstance().getTime().toString());
			sendToDb(results,true);
			log.info("Posted to DB @"+Calendar.getInstance().getTime().toString());
		}
		log.info("Completed Input Generation @"+Calendar.getInstance().getTime().toString());
	}
	
	/**
	 * File Generator
	 */
	private void fileGen()
	{
		results=new ArrayList<String>();
		if(fpath != null)
		{
			File f=new File(fpath);
			
			
			if(f.exists())
			{
				try {
					//well demarcated lines+less code--> reader
					FileReader fr=new FileReader(f);
					BufferedReader br=new BufferedReader(fr);
					
					String line;
					
					while((line=br.readLine()) != null)
					{
						if(append != null)
						{
							results.add(line.trim()+append.trim());
						}
						else 
						{
							results.add(line.trim());
						}
					}
					
					
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Saves the input to a Specified Path for the Future
	 * Will overwrite an existing file if told to.
	 */
	public void save()
	{
		if(savepath != null & results != null)
		{
			if(results.size()>0)
			{
				File f=new File(savepath);
				
				
				
				try {
					if(f.exists()==false)
					{
						f.delete();
					}
					
					f.createNewFile();
		
					PrintWriter pw=new PrintWriter(f);
				
					for(String r: results)
					{
						pw.println(r);
					}
					
					pw.flush();
					pw.close();
					
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		else
		{
			try{
				throw new NoFilePath("No save Path");
			}catch(NoFilePath e)
			{
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Generator Control
	 */
	public void generate()
	{
		log.info("Generating Input @"+Calendar.getInstance().getTime().toString());
		if(isFile==false)
		{
			loopGen();
		}
		else if(isFile==true)
		{
			fileGen();
		}
	}



	public void run() {
		// TODO Auto-generated method stub
		generate();
	}
	
	
}
