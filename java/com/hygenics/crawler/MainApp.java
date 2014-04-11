package com.hygenics.crawler;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;



/**
 * Main Application for Crawler that handles processing. 
 * The activities are highly dependent even if the code is not so they follow one after another.
 * Calling the garbage collector performs wonders due to the memory intensive behavior of the program.
 * 
 * Future Goals: 
 * 
 * 1. Scalability: Several server types (http and parser) that can split up tasks and take on new ones in a generic fashion
 * 		*The parser can be used as the basis of a star cluster-like server (backlogs would be delegator node and SQL server where data is sent)
 * 		*need for overall SQL server to input data directly to database
 * 2. Automation: 
 * 		A.Take in a page and determine the best way to crawl.(a new step that attempts to perform a run of the site,creating the crawl queue as it goes)
 * 		B.Look at samples of pages for a way to split into keywords (a new step that creates the regex map)
 * 			*common keywords
 * 			*way to recognize where useful data ends
 * 
 * Everything in this program can be parceled out to different processes/servers for better performance with PostGresSQL dbs serving as middlemen. 
 * 
 * @author aevans
 *
 */
public class MainApp {
	
	/**
	 * The entire programs main method.
	 * @param args
	 */
	public static void main(String[] args)
	{
		
		final Logger log=LoggerFactory.getLogger(MainApp.class);


		ClassPathXmlApplicationContext context=new ClassPathXmlApplicationContext(("file:"+System.getProperty("beansfile").trim()));
		log.info("Found beans @ "+System.getProperty("beansfile").trim());
		log.info("Starting");
		ArrayList<String> results=null;
		
		String activity=null;
		log.info("Obtaining Activities");
		ActivitiesStack stack=(ActivitiesStack) context.getBean("ActivitiesStack");

		
		//integers keeping track of bean number to pull (e.g. DumpToText0 or DumpToText1)
		//in keeping with the spirit of dumping out data
		int bm=0;
		int dump=0;
		int kettle=0;
		int execute=0;
		int sqls=0;
		int job=0;
		int parsepages=0;
		int getpages=0;
		
		Pattern p=Pattern.compile("[A-Za-z]+[0-9]+");
		Matcher m;
		
		//start stack init
		log.info("Stack Initialized with Size of "+stack.getSize()+" @ "+Calendar.getInstance().getTime().toString());
		
		while(stack.getSize()>0)
		{
			//pop activity form stack
			activity=stack.Pop();
			log.info("Activities Remaining "+stack.getSize());
			m=p.matcher(activity);
			
			if(activity.toLowerCase().contains("droptables")){
				//TOOD drop tables
				log.info("Dropping Tables @ "+Calendar.getInstance().getTime().toString());
				
				DropTables droptables=(DropTables) context.getBean("DropTables");
				droptables.run();
				
				droptables=null;
				log.info("Done Dropping Tables @ "+Calendar.getInstance().getTime().toString());
			}
			else if(activity.toLowerCase().contains("createtable")){
				//TODO create tables
				log.info("Creating Tables @ "+Calendar.getInstance().getTime().toString());
				
				CreateTable create=(CreateTable) context.getBean("CreateTables");
				create.run();
				create=null;
				
				log.info("Done Creating Tables @ "+Calendar.getInstance().getTime().toString());
			}
			else if(activity.toLowerCase().contains("truncate"))
			{
				//TODO truncate table
				log.info("Truncating @ "+Calendar.getInstance().getTime().toString());
				Truncate truncate=(Truncate) context.getBean("Truncate");
				truncate.truncate();
				truncate=null;
				log.info("Truncated @ "+Calendar.getInstance().getTime().toString());
				Runtime.getRuntime().gc();
				log.info("Free Memory: "+Runtime.getRuntime().freeMemory());
			}
			else if(activity.toLowerCase().contains("enforce")){
				log.info("Enforcing Schema @"+Calendar.getInstance().getTime().toString());
				ForceConformity ef=(ForceConformity)context.getBean("EnforceStandards");
				ef.run();
				log.info("Done Enforcing Schema @"+Calendar.getInstance().getTime().toString());
				Runtime.getRuntime().gc();
				log.info("Free Memory: "+Runtime.getRuntime().freeMemory());
			}
			else if(activity.toLowerCase().contains("repconn")){
				log.info("Replacing Transformation Connection Information  @"+Calendar.getInstance().getTime().toString());
				
				RepConn rep=(RepConn) context.getBean("repconn");
				rep.run();
				
				log.info("Finished Replacing Connection Information  @"+Calendar.getInstance().getTime().toString());
			}
			else if(activity.toLowerCase().contains("job"))
			{
				//TODO run a Pentaho job as opposed to a Pentaho Transformation
				log.info("Run Job kjb file @"+Calendar.getInstance().getTime().toString());
				
				RunJob kjb=null;
				
				if(m.find())
				{
					kjb=(RunJob) context.getBean(activity);
				}
				else{
					kjb=(RunJob) context.getBean("Job"+job);
				}
				
				kjb.run();
				kjb=null;
				log.info("Pentaho Job Complete @"+Calendar.getInstance().getTime().toString());
				Runtime.getRuntime().gc();
				
			}
			else if(activity.toLowerCase().contains("generateinput"))
			{
				//TODO generate input 
				log.info("Generate Input @"+Calendar.getInstance().getTime().toString());
				GenerateInput gen=(GenerateInput) context.getBean("GenerateInput");
				gen.generate();
				gen=null;
				log.info("Input Obtained @"+Calendar.getInstance().getTime().toString());
				Runtime.getRuntime().gc();

			}
			else if(activity.toLowerCase().compareTo("execute")==0)
			{
				//TODO Execute a process
				log.info("Executing Process @"+Calendar.getInstance().getTime().toString());
				
				ExecuteProcess proc=(ExecuteProcess) context.getBean(("Execute"+execute));	
				proc.Execute();
				
				log.info("Pages Obtained @"+Calendar.getInstance().getTime().toString());
				Runtime.getRuntime().gc();
				log.info("Free Memory: "+Runtime.getRuntime().freeMemory());
			}
			else if(activity.toLowerCase().compareTo("getpages")==0)
			{
				//TODO pull pages
				log.info("Get Pages @"+Calendar.getInstance().getTime().toString());
				
				GetPages pages=(GetPages) context.getBean(("GetPages"+getpages).trim());	
				getpages++;
				
				if(results != null)
				{
					if(results.size()>0)
						pages.setTerms(results);
				}
				
				pages.pull();
				pages=null;
				log.info("Pages Obtained @"+Calendar.getInstance().getTime().toString());
				Runtime.getRuntime().gc();
				log.info("Free Memory: "+Runtime.getRuntime().freeMemory());
			}
			else if(activity.toLowerCase().contains("parselinks"))
			{
				//TODO get links
				log.info("Getting Links @"+Calendar.getInstance().getTime().toString());
				ParseLinks getLinks=(ParseLinks) context.getBean("ParseLinks");
				getLinks.parse();
				getLinks=null;
				log.info("Links Attained");
				Runtime.getRuntime().gc();
				log.info("Free Memory: "+Runtime.getRuntime().freeMemory());
			}
			else if(activity.toLowerCase().contains("getindipage"))
			{
				//TODO get individual pages
				log.info("Get Individual Pages @"+Calendar.getInstance().getTime().toString());
				GetIndividualPages gip=(GetIndividualPages)context.getBean("GetIndividualPages");
				gip.pull();
				gip=null;
				log.info("Finished Getting Individual Pages @"+Calendar.getInstance().getTime().toString());
				Runtime.getRuntime().gc();
				log.info("Free Memory: "+Runtime.getRuntime().freeMemory());
			}
			else if(activity.toLowerCase().contains("parsepages"))
			{
				//TODO parse pages
				log.info("Parsing Individual Pages  @"+Calendar.getInstance().getTime().toString());
				ParseDispatcher pd= (ParseDispatcher) context.getBean("ParsePages"+parsepages);
				pd.run();
				pd=null;
				parsepages++;
				log.info("Finished Parsing @"+Calendar.getInstance().getTime().toString());
				Runtime.getRuntime().gc();
				log.info("Free Memory: "+Runtime.getRuntime().freeMemory());
			}
			else if(activity.toLowerCase().contains("breakmultiple"))
			{
				//TODO break apart multi-part records
				log.info("Breaking apart Records (BreakMultiple) @"+Calendar.getInstance().getTime().toString());
				BreakMultiple br=(BreakMultiple) context.getBean(("BreakMultiple"+Integer.toString(bm)));
				br.run();
				bm++;
				br=null;
				log.info("Finished Breaking Apart Records @"+Calendar.getInstance().getTime().toString());
				log.info("Free Memory: "+Runtime.getRuntime().freeMemory());
			}
			else if(activity.toLowerCase().contains("getimages"))
			{
				//TODO Get Images in a Separate Step
				log.info("Beggining Image Pull @ "+Calendar.getInstance().getTime().toString());
				GetImages gi=(GetImages) context.getBean("getImages");
				gi.run();
				log.info("Image Pull Complete @ "+Calendar.getInstance().getTime().toString());
				gi=null;
				Runtime.getRuntime().gc();
				System.gc();
				log.info("Free Memory: "+Runtime.getRuntime().freeMemory());
				
			}
			else if(activity.toLowerCase().compareTo("sql")==0)
			{
				//TODO execute a sql command 
				log.info("Executing SQL Query @ "+Calendar.getInstance().getTime().toString());
				
				ExecuteSQL sql;
				
				if(m.find())
				{
					sql=(ExecuteSQL) context.getBean(activity);
				}else{
					sql=(ExecuteSQL) context.getBean("SQL"+sqls);
				}
					
				sql.execute();
				sqls++;
				sql=null;
				log.info("Finished SQL Query @ "+Calendar.getInstance().getTime().toString());
				Runtime.getRuntime().gc();
				System.gc();
				log.info("Free Memory: "+Runtime.getRuntime().freeMemory());
			}
			else if(activity.toLowerCase().compareTo("kettle")==0)
			{
				//TODO run one or more kettle transformation(s)
				log.info("Beginning Kettle Transformation @ "+Calendar.getInstance().getTime().toString());
				RunTransformation rt=null;
				
				if(m.find())
				{
					rt=(RunTransformation) context.getBean(activity);
				}else{
					rt=(RunTransformation) context.getBean(("kettle"+kettle));
				}
				
				rt.run();
				rt=null;
				log.info("Ending Kettle Transformation @ "+Calendar.getInstance().getTime().toString());
				Runtime.getRuntime().gc();
				System.gc();
				log.info("Free Memory: "+Runtime.getRuntime().freeMemory());
				kettle++;
			}
			else if(activity.toLowerCase().contains("dumptotext"))
			{
				//TODO dump to a text file via java
				log.info("Dumping to Text @ "+Calendar.getInstance().getTime().toString());
				
				DumptoText dtt=null;
				if(m.find())
				{
					dtt=(DumptoText) context.getBean(activity);
				}else{
					dtt=(DumptoText) context.getBean("DumpToText"+dump);
				}
				
				dtt.run();
				dump++;
				log.info("Completed Dump @ "+Calendar.getInstance().getTime().toString());
				dtt=null;
				Runtime.getRuntime().gc();
				System.gc();
				log.info("Free Memory: "+Runtime.getRuntime().freeMemory());
			}
			else if(activity.toLowerCase().compareTo("commanddump")==0)
			{
				//TODO dump to text using a client side sql COPY TO command
				log.info("Dumping via SQL @ "+Calendar.getInstance().getTime().toString());
				CommandDump d=(CommandDump) context.getBean("dump");
				d.run();
				d=null;
				log.info("Completed Dump @ "+Calendar.getInstance().getTime().toString());
				
				//most likely not needed by satisfies my paranoia
				Runtime.getRuntime().gc();
				System.gc();
			}
			else if(activity.toLowerCase().compareTo("specdump")==0){
				log.info("Dumping via Specified Tables, Files, and Attributes @ "+Calendar.getInstance().getTime().toString());
				SpecifiedDump sd=(SpecifiedDump) context.getBean("SpecDump");
				sd.run();
				sd=null;
				log.info("Completed Dumping via Specified Tables, Files, and Attributes @ "+Calendar.getInstance().getTime().toString());
			}
			else if(activity.toLowerCase().compareTo("email")==0)
			{
				//TODO email completion notice
				log.info("Sending Notice of Completion @ "+Calendar.getInstance().getTime().toString());
				Send s=(Send) context.getBean("Email");
				s.run();
				log.info("Completed Email @ "+Calendar.getInstance().getTime().toString());
				s=null;
				Runtime.getRuntime().gc();
				System.gc();
				log.info("Free Memory: "+Runtime.getRuntime().freeMemory());
			}
			
		}
		

		log.info("Complete");
		
		context.destroy();
		context.close();
		
		
	}
 
}
