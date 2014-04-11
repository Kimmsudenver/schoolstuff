package com.hygenics.crawler;

import com.jolbox.bonecp.BoneCPDataSource;

import mjson.Json;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.hygenics.crawlerobjects.IndividualPage;
import com.hygenics.crawlerobjects.PostObjects;

/**
* The Data Source for the Page Grab
* 
* 
* @author aevans
*
*/
public class getDAOTemplate{
	
	private static final Logger log=LoggerFactory.getLogger(MainApp.class);
	private BoneCPDataSource datasource=null;
	private JdbcTemplate jdbcTemplateObject;
	private int uniqueid=0;
	
	public getDAOTemplate()
	{
		
	}
	


	/**
	 * Set DataSource
	 * 
	 */
	public void setDataSource(BoneCPDataSource datasource) {
		// TODO Auto-generated method stub
		this.datasource=datasource;
		this.jdbcTemplateObject=new JdbcTemplate(this.datasource);
	}
	
	/**
	 * Checks for a table schema
	 * 
	 * @param schema
	 * @return
	 */
	public boolean checkSchema(String schema)
	{
		String sql="SELECT count(schema_name) FROM information_schema.schemata WHERE schema_name='"+schema+"'";
		
		SqlRowSet rs= this.jdbcTemplateObject.queryForRowSet(sql);
		
		if(rs.next())
		{
			if(rs.getInt(1) >0)
			{
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Check Table
	 */
	public boolean checkTable(String table,String schema)
	{
		String[] table_split=table.split("\\.");
	
		if(table_split.length>0)
		{
			
			String sql="SELECT count(table_name) FROM information_schema.tables WHERE table_name='"+table_split[1]+"' AND table_schema='"+schema+"'";
			SqlRowSet rs=this.jdbcTemplateObject.queryForRowSet(sql);
		
			if(rs.next())
			{
				if(rs.getInt(1)>0)
				{
					return true;
				}
			}
			
		}
		return false;
	}
	
	/**
	 *Create a Schema 
	 */
	public void createSchema(String schema)
	{
		String sql="CREATE SCHEMA "+schema;
		this.jdbcTemplateObject.execute(sql);
	}
	
	
	/**
	 * Create a Table with a map
	 */
	public void createTablebyMap(String table, String schema, Map<String,String> columns,String key,String foreignkey,String fkeyref)
	{
		String fkey=foreignkey;
		String pkey=key;
		String sql;
		Set<String> keys=columns.keySet();
		if(checkTable(table,schema)==false & columns.size()>0)
		{
			if(key != null){
				
				if(fkey==null){
					fkey="";
				}
				
				sql="CREATE TABLE "+table+" (id SERIAL UNIQUE NOT NULL";
			}
			else{
				pkey="";
				fkey="";
				sql="CREATE TABLE "+table+" (id SERIAL PRIMARY KEY NOT NULL";
			}
			
			for(String c: keys)
			{
				if(c.compareTo("table") != 0)
				{
					if(c.compareTo(pkey) != 0){
						sql+=","+columns.get(c)+" text";
					}
					else if(c.compareTo(fkey)==0){
						sql+=","+columns.get(c)+" text FOREIGN KEY REFERENCES "+fkeyref;
					}
					else{
						sql+=","+columns.get(c)+" text PRIMARY KEY";
					}
				}
			}
		
			sql+=",date text, offenderhash text)";
			this.jdbcTemplateObject.execute(sql);
		}
	}
	
	/**
	 * Create a Table with a set
	 */
	public void createTable(String table, String schema, Set<String> columns,String inpkey, String infkey, String fkeyref)
	{
		String fkey=infkey;
		String pkey=inpkey;
		String sql=null;;
		
		if(checkTable(table,schema)==false & columns.size()>0)
		{
			if(pkey != null)
			{
				sql="CREATE TABLE "+table+ "(id SERIAL UNIQUE NOT NULL";
			}
			else{
				pkey="";
				fkey="";
				sql="CREATE TABLE "+table+ "(id SERIAL PRIMARY KEY NOT NULL";
			}
			
			for(String c: columns)
			{
				if(c.compareTo("table") != 0)
				{
					if(c.compareTo(fkey)==0){
						sql+=","+c+" text FOREIGN KEY REFERENCES "+fkeyref;
					}
					else if(c.compareTo(pkey)==0){
						sql+=","+c+" text PRIMARY KEY";
					}
					else{
						sql+=","+c+" text";
					}
				}
			}
		
			if(pkey.compareTo("offenderhash")==0){
				sql+=",date text, offenderhash text PRIMARY KEY)";
			}
			else if(fkey.compareTo("offenderhash")==0){
				sql+=",date text, offenderhash text FOREIGN KEY REFERENCES "+fkeyref+")";
			}
			else{
				sql+=",date text, offenderhash text)";
			}
			
			this.jdbcTemplateObject.execute(sql);
		}
	}
	
	/**
	 * Create a Column
	 * 
	 */
	public void createColumn(String table, String column)
	{
		String sql="ALTER TABLE "+table+" ADD COLUMN "+column+" text";
		this.jdbcTemplateObject.execute(sql);
	}

	
	

	/**
	 * check for column
	 */
	public boolean columnExists(String table, String column)
	{
		String[] data=table.split("\\.");
		if(data.length>1)
		{
			String query="SELECT count(column_name) FROM information_schema.columns WHERE table_name='".trim()+data[1].trim()+"' AND table_schema='"+data[0]+"' AND column_name='"+column.trim()+"'";
			SqlRowSet rs=this.jdbcTemplateObject.queryForRowSet(query);
			
			if(rs.next())
			{
				if(rs.getInt(1)!=0){
					return true;
				}
			}
			
		}
		
		return false;
	}
	
	/**
	 * Execute an non-returning query
	 * @param query
	 */
	public void execute(String query)
	{
		this.jdbcTemplateObject.execute(query);
	}
	
	/**
	 * Batch Update Script Using a Single Variable
	 * 
	 * @param table
	 * @param values
	 * @param column
	 * @param commit_size
	 */
	public void batchUpdateSingle(String table, ArrayList<String> values, String column, int commit_size)
	{
		String query="INSERT INTO "+table+"("+column+") VALUES(?)";
		this.jdbcTemplateObject.batchUpdate(query, getBatchSingle(values,commit_size));
	}
	
	/**
	 * Get Single Batch
	 * @param values
	 * @param commit_size
	 * @return
	 */
	private BatchPreparedStatementSetter getBatchSingle(final ArrayList<String> values,final int commit_size)
	{
		return (new BatchPreparedStatementSetter(){

			public int getBatchSize() {
				// TODO Auto-generated method stub
				return values.size();
			}

			public void setValues(PreparedStatement ps, int i)throws SQLException {
				// TODO Auto-generated method stub
				
				ps.setString(1, values.get(i));
				
			}
			
			
			
		});
		
	}
	
	/**
	 * Non-parametized batch update to avoid erros in batch setter
	 * @param table
	 * @param columns
	 * @param values
	 */
	public void BatchUpdateIndi(String table,String[] columns,ArrayList<IndividualPage> values,int indipageid,boolean genhash)
	{
		
		
		String query="INSERT INTO "+table+"(";
		String vals=" VALUES(";
		
		for(int i=0;i<columns.length;i++)
		{
			query=(i==0)?query+columns[i]:query+","+columns[i];
			vals=(i==0)?vals+"?":vals+",?";
		}
		query+=")"+vals+");";

		this.jdbcTemplateObject.batchUpdate(query, getPostIndiBatchSetter(values,indipageid,genhash));
	}
	
	/**
	 * Get Individual Batch Setter
	 * @param vals
	 * @param indipageid
	 * @return
	 */
	public BatchPreparedStatementSetter getPostIndiBatchSetter(final ArrayList<IndividualPage> vals,final int indipageid,final boolean genhash)
	{
		return(new BatchPreparedStatementSetter(){

			public int getBatchSize() {
				// TODO Auto-generated method stub
				return vals.size();
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
				return Long.toString(h);
			}

			public void setValues(PreparedStatement ps, int i)throws SQLException {
				// TODO Auto-generated method stub
				IndividualPage current=vals.get(i);
				ps.setString(1, current.getHtml());
				ps.setString(2, current.getLink());
				ps.setString(3, Integer.toString(indipageid+i));
				ps.setString(4, current.getDatestamp());
				
				if(genhash){	
					ps.setString(5,genHash(Integer.toString(indipageid+i)));
				}
				else{
					ps.setString(5, current.getId());
				}
			}
			
		});
	}
	
	/**
	 * Non-parametized batch update for PostObjects to avoid errors in batch setter
	 * @param table
	 * @param columns
	 * @param values
	 */
	public void BatchUpdatePostObjects(String table,String[] columns,ArrayList<PostObjects> values)
	{
		String query="INSERT INTO "+table+"(";
		String vals=" VALUES(";
		
		for(int i=0;i<columns.length;i++)
		{
			query=(i==0)?query+columns[i]:query+","+columns[i];
			vals=(i==0)?vals+"?":vals+",?";
		}
		query+=")"+vals+");";
	
		this.jdbcTemplateObject.batchUpdate(query, getPostObjectsBatchSetter(values,columns.length));
	}
	
	private BatchPreparedStatementSetter getPostObjectsBatchSetter(final ArrayList<PostObjects> values, final int columnlength)
	{
		return (new BatchPreparedStatementSetter(){

	
			public int getBatchSize() {
				// TODO Auto-generated method stub
				return values.size();
			}


			public void setValues(PreparedStatement ps, int i)throws SQLException {
				// TODO Auto-generated method stub
				PostObjects current=values.get(i);
				
				if(current != null){
				
				if(current.getHtml() != null){
					ps.setString(1, current.getHtml().replaceAll("\'","").trim());
				}
				else{
					ps.setString(1, "");
				}
				
				if(current.getLink()!= null)
				{
					ps.setString(2, current.getLink().replaceAll("\'","").trim());
				}
				else{
					ps.setString(2, "");
				}
				
				if(current.getRoot()!= null){
					ps.setString(3, current.getRoot().replaceAll("\'","").trim());
				}
				else{
					ps.setString(3, "");
				}
				
				if(current.getDatestamp() != null){
					ps.setString(4, current.getDatestamp().replaceAll("\'","").trim());
				}
				else{
					ps.setString(4, "");
				}
				
				if(current.getId() != null){
					ps.setString(5, current.getId().replaceAll("\'","").trim());
				}
				else{
					ps.setString(5, "");
				}
				
				if(current.getAdditionalhtml() != null)
				{
					ps.setString(6, current.getAdditionalhtml().replaceAll("\'",""));
				}
				else if(columnlength==6)
				{
					ps.setString(6, "");
				}
				
				}
			}
			
		});
	}
	
	
	/**
	 * Return the column names 
	 * @return
	 */
	public ArrayList<String> getColumns(String table){
		String[] split=table.split("\\.");
		String sql=null;
		
		if(split.length==2){
			 sql="SELECT column_name FROM information_schema.columns WHERE table_name='"+split[1].trim()+"' AND table_schema='"+split[0].trim()+"'";
		}
		else{
			//get rid of obscure sql error
			try{
				throw new NullPointerException("Table must be provided in schema.table format!");
			}catch(NullPointerException e){
				e.printStackTrace();
			}
		}
		
		return this.jdbcTemplateObject.query(sql, getArrayResultSetExtractor("column_name"));
	}
	
	/**
	 * Gets Data in Map<String,String> format based on a table and two columns
	 * 
	 * @param select
	 * @param column
	 * @param idcolumn
	 * @return
	 */
	public Map<String,String> getData(String select, String column, String idcolumn,boolean unique)
	{
		return this.jdbcTemplateObject.query(select, getExtractor(column, idcolumn,unique));
	}
	
	public ResultSetExtractor<Map<String,String>> getExtractor(final String column, final String idcolumn,final boolean unique)
	{
		return (new ResultSetExtractor<Map<String,String>>(){

			
			public Map<String,String> extractData(ResultSet rs) throws SQLException,DataAccessException {
				// TODO Auto-generated method stub
				Map<String,String> results=new HashMap<String,String>();
				
				while(rs.next())
				{
					if(unique==false)
					{
						results.put(rs.getString(column), rs.getString(idcolumn));
					}
					else{
						String id=rs.getString(column);
						id+="UQUQ"+uniqueid+"UQUQ";
						results.put(id,rs.getString(idcolumn));
						uniqueid++;
					}
				}
				rs.close();
				return results;
			}
			
		});
	}
	
	/**
	 * Gets an arraylist of strings corresponding to the column passe in
	 * @param sql
	 * @param column
	 * @return
	 */
	public ArrayList<String> getArrayList(String sql, String column)
	{
		return this.jdbcTemplateObject.query(sql, getArrayResultSetExtractor(column));
	}
	
	/**
	 * Extractor for ArrayList
	 * @param column
	 * @return
	 */
	private ResultSetExtractor<ArrayList<String>> getArrayResultSetExtractor(final String column)
	{
		return (new ResultSetExtractor<ArrayList<String>>(){

		
			public ArrayList<String> extractData(ResultSet rs)throws SQLException, DataAccessException {
				// TODO Auto-generated method stub
				ArrayList<String> results=new ArrayList<String>();
				
				while(rs.next())
				{
					results.add(rs.getString(column));
				}
				
				rs.close();
				return results;
			}
			
			
		});
	}
	
	
	/**
	 * Returns a string in JSon notation of the data.
	 * This saves from needing a map of maps.
	 * @return
	 */
	public ArrayList<String> getJsonData(String query)
	{
		return this.jdbcTemplateObject.query(query, getJsonExtractor());
	}
	
	/**
	 * Returns a Json Based Extractor
	 * @return
	 */
	public ResultSetExtractor<ArrayList<String>> getJsonExtractor()
	{
		return (new ResultSetExtractor<ArrayList<String>>(){

	
			public ArrayList<String> extractData(ResultSet rs)throws SQLException, DataAccessException {
				// TODO Auto-generated method stub
				int j=0;
				ArrayList<String> results=new ArrayList<String>();
				String obj=null;
				int columns=0;
				
				while(rs.next())
				{
					if(columns==0)
					{
						columns=rs.getMetaData().getColumnCount();
						if(columns ==0)
						{
							return results;
						}
					}
					
					obj=null;
					
					for(int i=1;i<=columns;i++)
					{
						obj=(obj==null)?"{\""+rs.getMetaData().getColumnName(i)+"\":\""+rs.getString(i).replaceAll("\t|\r|\n|\r\n|\"", "")+"\"":obj+",\""+rs.getMetaData().getColumnName(i)+"\":\""+rs.getString(i)+"\"";
						
					}
					
					if(obj != null)
					{
					 obj+="}";	
					}
					results.add(obj);
					obj=null;
					j++;
				}
				
				if(rs.getFetchSize()<=j)
				{
					rs.close();
				}
				
				return results;
			}
			
		});
	}
	
	
	/**
	 * Controls the posting of variable minimal json data
	 */
	public void postJsonData(String sql,ArrayList<String> jsondata)
	{
		//get the keyset order of the first posted string
		if(jsondata.size()>0)
		{
			ArrayList<String> keys=new ArrayList<String>();
			Map<String,Json> jmap=Json.read(jsondata.get(0)).asJsonMap();
		
			for(String k: jmap.keySet())
			{
				if(k.toLowerCase().compareTo("table") != 0)
				{
					keys.add(k);
				}
			}
		
			//post the keys to the database
			this.jdbcTemplateObject.batchUpdate(sql, jsonBatchSetter(jsondata,keys));
			keys=null;
			jmap=null;
		}
	}

	
	private BatchPreparedStatementSetter jsonBatchSetter(final ArrayList<String> jsondata, final ArrayList<String> keys)
	{
		return (new BatchPreparedStatementSetter(){

			
			public int getBatchSize() {
				// TODO Auto-generated method stub
				return jsondata.size();
			}

		
			public void setValues(PreparedStatement ps, int i)throws SQLException {
				// TODO Auto-generated method stub
				Map<String, Json> jmap=Json.read(jsondata.get(i)).asJsonMap();
				int j=0;
				
				for(String k:keys)
				{
					if(k.compareTo("table")!=0){
						j++;
						ps.setString(j, jmap.get(k).asString());
					}
				}
			}
			
		});
		
	}
	
	/**
	 * posts a single json row
	 * @param json
	 */
	public void postSingleJson(String json)
	{
		
		Map<String,Json> jmap=Json.read(json).asJsonMap();
		
		String sql="INSERT INTO "+jmap.get("table").asString()+"(";
		String val=" VALUES(";
		int i=0;
		
		for(String k:jmap.keySet())
		{
			if(k.trim().compareTo("table")!= 0)
			{
				sql=(i==0)?sql+k:sql+","+k;
				val=(i==0)?val+"'"+jmap.get(k).asString().trim().replaceAll("'", "")+"'":val+",'"+jmap.get(k).asString().trim().replaceAll("'", "")+"'";
				i++;
			}
		}
		
		sql+=") "+val+")";
		
		this.jdbcTemplateObject.execute(sql);
	}
	
	/**
	 *Posts Json Data with table in the Json String.
	 */
	public void postJsonDatawithTable(ArrayList<String> json)
	{
		ArrayList<String> sublist=new ArrayList<String>();
		ArrayList<String> keylist=new ArrayList<String>();
		String table="placeholder";
		String sql=null;
		Set<String> keys;
		int numkeys=0;
		int j=0;
		Map<String,Json> jmap=null;
		
		for(int i=0;i<json.size();i++)
		{
			String js=json.get(i).replaceAll("\"\"", "\"");
			jmap=Json.read(js).asJsonMap();
			
			
			if(jmap.containsKey("table"))
			{
				
				if(jmap.get("table").asString().compareTo(table)==0 & jmap.keySet().size()==numkeys & sublist.size()<=100)
				{
					sublist.add(js);
				}else
				{
					if(sql != null)
					{
						this.jdbcTemplateObject.batchUpdate(sql,getJsonwithTableSetter(sublist,keylist));
						sublist=new ArrayList<String>();
					}
					
					keys=jmap.keySet();
					sql="INSERT INTO "+jmap.get("table").asString()+"(";
					
					j=0;
					numkeys=keys.size();
					keylist=new ArrayList<String>();
					
					for(String k:keys)
					{
						
						if(k.compareTo("table")==0)
						{
							table=jmap.get(k).asString().trim();
						}
						else
						{
							keylist.add(k.trim());
							sql=(j==0)?sql+k.trim():sql+","+k.trim();		
						}
						j++;
					}
					
					sql+=") VALUES(";
					j=0;
					
					for(String k: keys)
					{
						if(k.compareTo("table") != 0)
						{
							sql=(j==0)?sql+"?":sql+",?";
							j++;
						}
					}
					sql+=");";
					sublist.add(js);
				}
			
			}
		}
		
		if(sublist.size()>0){
			this.jdbcTemplateObject.batchUpdate(sql,getJsonwithTableSetter(sublist,keylist));
		}
	
	}
	
	
	
	private BatchPreparedStatementSetter getJsonwithTableSetter(final ArrayList<String> json,final ArrayList<String> keys)
	{
		return (new BatchPreparedStatementSetter(){

			
			public int getBatchSize() {
				// TODO Auto-generated method stub
				return json.size();
			}

		
			public void setValues(PreparedStatement ps, int i)throws SQLException {
				// TODO Auto-generated method stub
			
				
					Map<String, Json> jmap=Json.read(json.get(i)).asJsonMap();
					int k=1;
				
					for(String key: keys)
					{		
						if(key.compareTo("table") != 0)
						{
							ps.setString(k, jmap.get(key).asString().trim());
							k++;
						}
					}
				
				
			}
			
		});
	}
	
	/**
	 * Returns a column
	 * @param data -column information
	 */
	public String getColumn(String sql,String column)
	{
		return this.jdbcTemplateObject.query(sql, getColumnExtractor(column));
	}
	
	
	/**
	 * Column Extractor
	 * @return
	 */
	public ResultSetExtractor<String> getColumnExtractor(final String column)
	{
		return (new ResultSetExtractor<String>(){

		
			public String extractData(ResultSet rs) throws SQLException,DataAccessException {
				
				// TODO Auto-generated method stub
				String result=null;
				
				if(rs.next())
				{
					result=rs.getString((column.trim()));
				}
				rs.close();
				
				return result;
			}			
		});
	}
	
	/**
	 * Get a count from a table
	 * @param table
	 * @return
	 */
	public int getCount(String table)
	{
		String sql="Select count(*) FROM "+table;
		return this.jdbcTemplateObject.queryForObject(sql,Integer.class);
	}
	
}

