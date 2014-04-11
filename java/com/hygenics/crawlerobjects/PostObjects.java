package com.hygenics.crawlerobjects;

public class PostObjects {

	private String id;
	private String datestamp;
	private String html;
	private String link;
	private String root;
	private String additionalhtml;
	
	public PostObjects()
	{
		
	}
	
	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}


	public String getAdditionalhtml() {
		return additionalhtml;
	}






	public void setAdditionalhtml(String additionalhtml) {
		this.additionalhtml = additionalhtml;
	}



	public String getDatestamp() {
		return datestamp;
	}

	public void setDatestamp(String datestamp) {
		this.datestamp = datestamp;
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

	public String getRoot() {
		return root;
	}

	public void setRoot(String root) {
		this.root = root;
	}
	
	
}
