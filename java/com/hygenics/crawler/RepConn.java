package com.hygenics.crawler;

import java.util.List;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.List;

import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleMissingPluginsException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;


/**
 * Takes in a list of transformation files and jobs and edits the connection information using
 * a series of properties and a conname.
 * 
 * Required Properties
 * 
 * -jdbpass (password)
 * -jdbhost
 * -jdbuser
 * -jdbdatabase
 * -jdbname
 * 
 * @author asevans
 *
 */
public class RepConn {
	
	private Logger log=LoggerFactory.getLogger(MainApp.class);
	private List<String> transformations;
	private String connname;
	private String jdbhost;
	private String jdbuser;
	private String jdbpass;
	private String jdbdatabase;
	private String jdbport;
	private String jdbname;
	
	public RepConn(){
		
	}

	public List<String> getTransformations() {
		return transformations;
	}

	@Required
	@Autowired
	public void setTransformations(List<String> transformations) {
		this.transformations = transformations;
	}

	public String getConnname() {
		return connname;
	}

	/**
	 * Sets the database connection name: the top bar in Pentaho that asks you to name the connection
	 * @param connname
	 */
	@Required
	public void setConnname(String connname) {
		this.connname = connname;
	}

	public String getJdbhost() {
		return jdbhost;
	}

	@Required
	public void setJdbhost(String jdbhost) {
		this.jdbhost = jdbhost;
	}

	public String getJdbuser() {
		return jdbuser;
	}

	@Required
	public void setJdbuser(String jdbuser) {
		this.jdbuser = jdbuser;
	}

	public String getJdbpass() {
		return jdbpass;
	}

	@Required
	public void setJdbpass(String jdbpass) {
		this.jdbpass = jdbpass;
	}

	public String getJdbdatabase() {
		return jdbdatabase;
	}

	@Required
	public void setJdbdatabase(String jdbdatabase) {
		this.jdbdatabase = jdbdatabase;
	}

	public String getJdbport() {
		return jdbport;
	}

	@Required
	public void setJdbport(String jdbport) {
		this.jdbport = jdbport;
	}

	public String getJdbname() {
		return jdbname;
	}

	@Required
	public void setJdbname(String jdbname) {
		this.jdbname = jdbname;
	}
	
	/**
	 * Run the Pentaho Transformation Cleaner that replaces the connection information for the 
	 * specified transformations
	 * 
	 * @throws -NullPointerException when no transformation list given
	 */
	public void run(){
		//TODO manipulate job and/or transformation database information and save it
		
		log.info("Starting RepConn");
		if(transformations == null)
		{
			try{
				throw new NullPointerException("No transformation files specified!");
			}catch(NullPointerException e){
				e.printStackTrace();
			}
		}
		else{
			//vars
			TransMeta transMeta;
			DatabaseMeta dbm;
			JobMeta jMeta;
		
			/**
			 * Iterate through the provided list of transformations and perform the requested update
			 */
			for(String trfpath: transformations){
				log.info("Manipulating: "+trfpath+" @ "+Calendar.getInstance().getTime());
				transMeta=null;
				dbm=null;
				jMeta=null;
				
				File f=new File(trfpath);
				
				if(f.exists()){
				
					try {
						KettleEnvironment.init();
						transMeta=new TransMeta(trfpath);
					
						if(trfpath.toLowerCase().contains(".ktr")){
							//change transformation database information
							dbm=new DatabaseMeta();
							dbm.setDatabaseInterface(DatabaseMeta.getDatabaseInterface("POSTGRESQL"));
							dbm.setName(connname.trim());
							dbm.setHostname(jdbname.trim());
							dbm.setDBName(jdbdatabase.trim());
							dbm.setDBPort(jdbport.trim());
							dbm.setUsername(jdbuser.trim());
							dbm.setPassword(jdbpass.trim());
				
							//save transformation
							transMeta.addOrReplaceDatabase(dbm);
							transMeta.setChanged();
							
							String xml=transMeta.getXML();
							f.delete();
							
							DataOutputStream dos = new DataOutputStream(new FileOutputStream(new File(trfpath)));
							dos.write(xml.getBytes("UTF-8"));
							dos.close();
						}
						else if(trfpath.contains(".kjb")){
						
							//change job database information
							jMeta=new JobMeta(trfpath,null);
							dbm=new DatabaseMeta();
							dbm.setDatabaseInterface(DatabaseMeta.getDatabaseInterface("POSTGRESQL"));
							dbm.setName(connname.trim());
							dbm.setHostname(jdbhost.trim());
							dbm.setDBName(jdbdatabase.trim());
							dbm.setDBPort(jdbport.trim());
							dbm.setUsername(jdbuser.trim());
							dbm.setPassword(jdbpass.trim());
						
							//save job
							jMeta.addOrReplaceDatabase(dbm);
							jMeta.saveSharedObjects();
							
							String xml=transMeta.getXML();
							f.delete();
							
							DataOutputStream dos = new DataOutputStream(new FileOutputStream(new File(trfpath)));
							dos.write(xml.getBytes("UTF-8"));
							dos.close();
						}
						else{
							//this will hit if the file is not a .kjb or.ktr
							log.warn("SKIP FILE WARNING: "+trfpath+" Is Not a Proper or Recognized Pentaho File!\n The extensions .ktr and .kjb are accepted!\n");
						}
					
					} catch (KettleXMLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (KettleMissingPluginsException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (KettleDatabaseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (KettleException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}else{
					try{
						throw new FileNotFoundException("File Not Found");
					}catch(FileNotFoundException e){
						e.printStackTrace();
					}
				}

				log.info("Done!\nGetting next Fpath");
			}
			log.info("Ending Repconn");
		}
		
	}
}
