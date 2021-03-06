package com.hygenics.crawler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.annotation.*;

import net.java.frej.fuzzy.*;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

import com.hygenics.distance.Levenshtein;
import com.hygenics.exceptions.MismatchException;
import com.hygenics.exceptions.SQLMalformedException;
import com.hygenics.sort.Quicksort;

/**
 * Takes in a Map<table,list of columns> and adds columns that don't exist. Will
 * attempt to correct column names if Lev distances are short and stated to do so. 
 * 80% of errors are one deletion, insertion, or replacement away. (Lev. is better
 * where a ton of abbr. and _ exist)
 * 
 * This will add missing columns to conform with a certain schema. Provide a select,
 * then provide a list of columns. It will output the overall table to a new table of
 * your choice. The dirty way to do this is to create a full outer join on a schema 
 * table with a null row using obviously not matching columns but the id columns will 
 * be included without tedious spec.
 *
 * @author asevans
 *
 */
public class ForceConformity {

	private boolean fuzzy=false;
	private Logger log=Logger.getLogger(MainApp.class);
	private Map<String,List<String>> schema_columns;
	
	private List<String> exclude;
	private boolean correct=false;
	private int levcutoff=1;
	
	private getDAOTemplate template;
	
	public ForceConformity(){
		
	}
	
	
	
	public boolean isFuzzy() {
		return fuzzy;
	}



	public void setFuzzy(boolean fuzzy) {
		this.fuzzy = fuzzy;
	}



	public boolean isCorrect() {
		return correct;
	}



	public void setCorrect(boolean correct) {
		this.correct = correct;
	}



	public int getLevcutoff() {
		return levcutoff;
	}



	public void setLevcutoff(int levcutoff) {
		this.levcutoff = levcutoff;
	}



	public List<String> getExclude() {
		return exclude;
	}



	public void setExclude(List<String> exclude) {
		this.exclude = exclude;
	}



	public Map<String, List<String>> getSchema_columns() {
		return schema_columns;
	}


	@Required
	public void setSchema_columns(Map<String, List<String>> schema_columns) {
		this.schema_columns = schema_columns;
	}



	public getDAOTemplate getTemplate() {
		return template;
	}


	@Autowired
	@Required
	@Resource(name="getDAOTemplate")
	public void setTemplate(getDAOTemplate template) {
		this.template = template;
	}



	private void iter(){
		log.info("Starting Enforcement @ "+Calendar.getInstance().getTime().toString()+" | UTC "+Calendar.getInstance().getTimeInMillis());
		
		String sql;
		boolean add=true;
		ArrayList<String> columns=null;
		ArrayList<String> tablecolumns=null;
		Quicksort<String> qs=new Quicksort<String>();
		Levenshtein lev=new Levenshtein();
		
		//iterate through each table
		for(String table: schema_columns.keySet()){

			log.info("Checing for Table Existance "+table);
			String schema=null;
			
			try{
				schema=table.split("\\.")[0];
			}catch(IndexOutOfBoundsException e){
				try{
					throw new SQLMalformedException("FATAL ERROR: Table name "+table+" malformed");
				}
				catch(SQLMalformedException e2){
					e2.printStackTrace();
					System.exit(-1);
				}
			}
			
			log.info("Checking  table: "+table+ "&& schema: "+schema);
			
			
			if(template.checkTable(table, schema)){
			
			log.info("Conforming: "+table);
			//get the arraylist
			tablecolumns=template.getColumns(table);
			
			
			
			columns=(ArrayList<String>) schema_columns.get(table);
			
			if(exclude != null){
				//remove unmatchable columns
				if(exclude.size()>0){
					for(String col: exclude){
						tablecolumns.remove(col);
						columns.remove(col);
					}
				}
			}
			
			
			//sort each arraylist
			qs.setTosortlist(columns);
			columns=qs.sort();
			
			
			qs.setTosortlist(tablecolumns);
			tablecolumns=qs.sort();
			
			//check each remaining column
			for(int i=0;i<columns.size();i++){
				
					
				if(tablecolumns.contains(columns.get(i).trim())== false)
				{
					
					if(correct){
						lev.setStringa(tablecolumns.get(i));
						lev.setStringb(columns.get(i));
						
						log.info("Checking "+lev.getStringa()+" AGAINST "+lev.getStringb());
						int dist=lev.run();
						add=false;
						log.info("Distance was "+dist+" Cutoff is "+levcutoff);
						
						//check lev. distances if requested and ensure some match with fuzzy (use at your own risk)
						if(levcutoff>-1 & dist<=levcutoff){
							
							
							if((fuzzy==true &Fuzzy.equals(columns.get(i), tablecolumns.get(i)))|fuzzy==false){
								log.info("Approximate Match: Correcting Column");
								
								//if distance is within 1 or specified distances, correct it
								sql="ALTER TABLE "+table+" RENAME COLUMN "+tablecolumns.get(i)+" TO "+columns.get(i);
								template.execute(sql);
							
								//get the table columns
								tablecolumns=template.getColumns(table);
								qs.setTosortlist(tablecolumns);
								tablecolumns=qs.sort();
						
							}
						}
						else if(((fuzzy==true &Fuzzy.equals(columns.get(i), tablecolumns.get(i)))|fuzzy==false)&levcutoff==-1){
							//if distance is within 1 or specified distances, correct it
							sql="ALTER TABLE "+table+" RENAME COLUMN "+tablecolumns.get(i)+" TO "+columns.get(i);
							template.execute(sql);
						
							//get the table columns
							tablecolumns=template.getColumns(table);
							qs.setTosortlist(tablecolumns);
							tablecolumns=qs.sort();
					
						} 
						else{
							add=true;
						}
					}
					
					if(add){
						//add column if necessary
						sql="Alter Table "+table+" ADD COLUMN "+columns.get(i)+" text";
						template.execute(sql);
						
						//get the table columns again and resort them
						tablecolumns=template.getColumns(table);
						
						if(exclude != null){
							if(exclude.size()>0)
							{
								for(String ex: exclude){
									tablecolumns.remove(ex);
									columns.remove(ex);
								}
							}
						}
						
						qs.setTosortlist(tablecolumns);
						tablecolumns=qs.sort();
					}
					//iterate backward so that the integrety is assured
					i--;
				}
			}
		}
		else{
			try{
				throw new SQLMalformedException("WARNING: Table "+table+" is missing");
			}catch(SQLMalformedException e){
				e.printStackTrace();
			}
		}
			
		}
		log.info("Finishing Enforcement @ "+Calendar.getInstance().getTime().toString()+" | UTC "+Calendar.getInstance().getTimeInMillis());

	}
	
	public void run(){
		iter();
	}
	
}
