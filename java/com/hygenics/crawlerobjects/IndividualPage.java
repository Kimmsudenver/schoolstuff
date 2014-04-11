package com.hygenics.crawlerobjects;

public class IndividualPage {
	
	private String id;
	private String html;
	private String link;
	private String datestamp;

	public IndividualPage()
	{
		
	}

	
	
	public String getDatestamp() {
		return datestamp;
	}



	public void setDatestamp(String datestamp) {
		this.datestamp = datestamp;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getHtml() {
		return html;
	}

	public void setHtml(String html) {
		this.html = html;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}
	
}
