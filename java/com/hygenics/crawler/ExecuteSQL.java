package com.hygenics.crawler;

import java.util.List;

import javax.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;


public class ExecuteSQL {

	private Logger log=LoggerFactory.getLogger(MainApp.class);
	private List<String> sql;
	private getDAOTemplate template;
	
	public ExecuteSQL()
	{
		
	}

	

	public List<String> getSql() {
		return sql;
	}



	public void setSql(List<String> sql) {
		this.sql = sql;
	}



	public getDAOTemplate getTemplate() {
		return template;
	}

	@Required
	@Resource(name="getDAOTemplate")
	public void setTemplate(getDAOTemplate template) {
		this.template = template;
	}

	public void execute()
	{
		for(String cmd: sql)
		{
			log.info("Executing: "+cmd);
			this.template.execute(cmd);
		}
	}

}
