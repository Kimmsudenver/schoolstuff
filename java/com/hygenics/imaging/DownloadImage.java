package com.hygenics.imaging;

//swing stuff for test
//for printing the image during testing

import com.hygenics.exceptions.*;

import javax.net.ssl.HttpsURLConnection;
import javax.swing.GrayFilter;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.color.ColorSpace;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


//streams
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Authenticator;
import java.net.CookieManager;
import java.net.CookieHandler;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URLEncoder;
//http connection
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.security.Security;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;










//meta data manipulation tool
import com.drew.imaging.ImageProcessingException;
import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.metadata.*;

//commons imaging for writing exif and metadata
import org.apache.commons.imaging.*;
import org.apache.commons.imaging.common.IImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;








//for getting the image
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;








//for reading out xml of metadata
import org.apache.commons.io.IOUtils;


/** 
 * Gets an Image using a single or multiple threads and adding metadata to the image.
 * Meta data is added to the user comment field under the Exif section.
 * 
 * The data can be SFTP/FTP using the SFTP or FTP classes.
 * 
 * aevans 8/01/2013
 */
public class DownloadImage {
	
	//method
	private String method;
	
	//url parameters
	private String url_params;
	
	//a header string[]
	private String[] headers;
	
	//header values
	private String[] values;
	
	//any cookies
	private String cookies;
	
	//boolean for image reading
	private boolean pass;
	
	//the bytes
	private byte[] ibytes;
	
	//a cookie manager
	private CookieManager mgr;
	
	//the string
	private String url;
	
	//the image type
	private String imgType;
	
	//the file path or ftp path to save to
	private String fpath;
	
	//the timeout for the connections (auto is 10 seconds)
	private int timeout=10000;

	private boolean proxy;
	
	public DownloadImage(){
		//TODO empty constructor
	}
	
	

	/**
	 * Return whether to use a proxy
	 * @return
	 */
	public boolean isProxy() {
		return proxy;
	}



	/**
	 * Set Proxy
	 * @param proxy
	 */
	public void setProxy(boolean proxy) {
		this.proxy = proxy;
	}



/**
 *Get Image bytes in byte[] 
 * @return
 */
	public byte[] get_ibytes()
	{
		//TODO get ibytes
		return ibytes;
	}
	
	/**
	 * Constructor with image bytes
	 * 
	 * @param inibytes 
	 */
	public void set_ibytes(byte[] inibytes)
	{
		//TODO set ibytes
		ibytes=inibytes;
	}
	
	
	/**
	 * Constructor with the url of an image
	 * @param inurl
	 */
	public DownloadImage(String inurl)
	{
		//TODO constructor that sets the download url
		url=inurl;
	}
	
	
	/**
	 * Constructor with with image url and file path 
	 * 
	 * @param inurl
	 * @param infpath
	 */
	public DownloadImage(String inurl, String infpath)
	{
		//TODO constructor with information for saving image
		url=inurl;
		fpath=infpath;
	}
	
	/**
	 * Set a proxy using a host, port, boolean for SSL, user, and password
	 * SSL is a boolean all else are strings. No System properties are used.
	 * @param inhost
	 * @param inport
	 * @param https
	 * @param user
	 * @param pass
	 */
	public void setProxy(String inhost,String inport, boolean https,String user, String pass)
	{
		proxy(inhost,inport,https,user,pass);
		proxy = true;
	}
	
	/**
	 * Uses system properties to set a proxy.
	 * https is the boolean specifying that SSL is used
	 * secured is the boolean specifying that a password and user auth. 
	 * are used
	 * @param https
	 * @param secured
	 */
	public void setProxybySystem(boolean https, boolean secured)
	{
		systemproxy(https,secured);
		proxy=true;
	}
	
	private void systemproxy(boolean https,boolean secured)
	{
		if(https)
		{
			System.setProperty("https.proxyHost", System.getenv("httpsproxyhost"));
			System.setProperty("https.proxyPort", System.getenv("httpsproxyPort"));
			
			if(secured)
			{
				Authenticator authenticator = new Authenticator() {

			        public PasswordAuthentication getPasswordAuthentication() {
			            return (new PasswordAuthentication(System.getenv("httpsproxyuser"),
			            		System.getenv("httpsproxypass").toCharArray()));
			        }
			    };
			    Authenticator.setDefault(authenticator);
			}
		}
		else
		{
			System.setProperty("https.proxyHost", System.getenv("httpproxyhost"));
			System.setProperty("https.proxyPort", System.getenv("httpproxyPort"));
			
			if(secured)
			{
				Authenticator authenticator = new Authenticator() {

			        public PasswordAuthentication getPasswordAuthentication() {
			            return (new PasswordAuthentication(System.getenv("httpproxyuser"),
			            		System.getenv("httpproxypass").toCharArray()));
			        }
			    };
			    Authenticator.setDefault(authenticator);
			}
		}
	}
	
	private void proxy(String host, String port, boolean https,String user,String pass)
	{
		if(https)
		{
			System.setProperty("https.proxyHost", host);
			System.setProperty("https.proxyPort", port);
			
			if(user != null)
			{
				System.setProperty("https.proxyUser", user);
			}
			
			if(pass != null)
			{
				System.setProperty("https.proxyPassword", pass);
			}
		}
		else
		{
			System.setProperty("http.proxyHost", host);
			System.setProperty("http.proxyPort", port);
			
			if(user != null)
			{
				System.setProperty("http.proxyUser", user);
			}
			
			if(pass != null)
			{
				System.setProperty("http.proxyPassword", pass);
			}
		}
	}
	
	/**
	 * Returns the image as a BufferedImage
	 * 
	 * @return img
	 */
	public BufferedImage getImage()
	{
		//TODO return the image
		InputStream is=new ByteArrayInputStream(ibytes);
		BufferedImage img=null;
		
		try {
			img = ImageIO.read(is);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return img;
	}
	
	/**
	 * Set the URL for the image
	 * @param url
	 */
	public void setUrl(String url)
	{
		this.url=url;
	}
	
	/**
	 * Set whether or not to ignore a null URL 
	 * 
	 * @param inpass
	 */
	public void setPass(Boolean inpass)
	{
		//TODO set pass for image reading
		pass=inpass;
	}
	
	/**
	 * Set the method to use in grabbing the image (GET or POST)
	 * @param inmethod
	 */
	public void setMethod(String inmethod)
	{
		method=inmethod;
	}
	
	/**
	 * Set a timeout in milliseconds for the pull
	 * Sets the @param timeout variable
	 * 
	 * @param millis
	 */
	public void setTimeout(int millis)
	{
		//TODO set the timeout in milli-seconds
		timeout=millis;
	}
	
	
	/**
	 * Get the timeout
	 * 
	 * @return timeout
	 */
	public int getTimeout()
	{
		//TODO return the number of milliseconds in the timeout
		return timeout;
	}
	
	/**
	 * @return url
	 */
	public String get_url()
	{
		//TODO return the url
		return url;
	}
	
	
	/**
	 * Set the url path
	 * @param inurl
	 */
	public void set_path(String inurl)
	{
		//TODO set the path
		url=inurl;
	}
	
	/**
	 * Kickstart the download when not using SSL
	 * Sets a variety of parameters including @param img
	 */
	public void download()
	{
		//TODO download via single threader
		download_single();
	}
	
	/**
	 * Set the fpath
	 * @param infpath
	 */
	public void set_fpath(String infpath)
	{
		//TODO set fpath
		fpath=infpath;
	}
	
	
	
	/**
	 * 
	 * @return fpath
	 */
	public String get_fpath()
	{
		//TODO return the file path
		return fpath;
	}
	
	/**
	 * Get the cookies: for compatibility with other code including html_grab/asp_grab
	 * @return
	 */
	public String getCookies()
	{
		return cookies;
	}
	
	/**
	 * Set the metadata of an image
	 * @param data
	 */
	public void setMetaData(String data)
	{
		//TODO set meta data using keys, values (an image must be set or an error is thrown relating to the type)
			
		add_meta(data);
	}
	
	public void download_html_post(String[] names, String[] vals, String[] parnames, String[] parvals,String inurl){
		
		try {
			HttpURLConnection conn=(HttpURLConnection) new URL(inurl).openConnection();
			
			for(int i=0;i<names.length;i++){
				conn.setRequestProperty(names[i], vals[i]);
			}
			
			encode_url_params(parnames, parvals);
			
			OutputStream os=conn.getOutputStream();
			os.write(url_params.getBytes());
			
			InputStream is=conn.getInputStream();
			ByteArrayOutputStream baos=new ByteArrayOutputStream();
			int b=0;
			
			while((b=is.read())!= -1){
				baos.write(b);
			}
			
			ibytes=baos.toByteArray();
			
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	public void download_ssl_post(String[] names, String[] vals, String[] parnames, String[] parvals,String inurl){
	
		try {
			HttpsURLConnection conn=(HttpsURLConnection) new URL(inurl).openConnection();
			
			for(int i=0;i<names.length;i++){
				conn.setRequestProperty(names[i], vals[i]);
			}
			
			encode_url_params(parnames, parvals);
			
			OutputStream os=conn.getOutputStream();
			os.write(url_params.getBytes());
			
			InputStream is=conn.getInputStream();
			ByteArrayOutputStream baos=new ByteArrayOutputStream();
			int b=0;
			
			
			while((b=is.read())!= -1){
				baos.write(b);
			}
			
			ibytes=baos.toByteArray();
			
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Download an image using headers and values using the IOutils string
	 * @param headers
	 * @param values
	 */
	public void download_as_html(String[] headers, String[] values)
	{
		//TODO download as a web page which can preserve exif and other meta data
		html_download(headers,values);
	}
	
	/**
	 * Set a cookie manager, this may also be done using a cookie string
	 * @param cmanager
	 */
	public void setCookies(CookieManager cmanager)
	{
		mgr=cmanager;
		mgr.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
		CookieHandler.setDefault(mgr);
	}
	
	/**
	 * Creates a Cookie Manager
	 */
	public void createCookieManager()
	{
		mgr=new CookieManager();
		mgr.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
		CookieHandler.setDefault(mgr);
	}
	
	/**
	 * set header names from primitive array
	 * @param innames
	 */
	public void set_header_names(String[] innames)
	{
		//TODO add new header names array
		headers=innames;
	}
	
	/**
	 * Set header values from primitive array
	 * @param invalues
	 */
	public void set_values(String[] invalues)
	{
		//TODO add new header values array
		values=invalues;
	}
	
	/**
	 * Get the html page (sorry for name its convention by now)
	 * @param inurl
	 * @return
	 */
	public String get_cookies(String inurl)
	{
		//TODO Method to call to get the page and cookies when a certificate is self-signed by the site
		return getAspsessions(inurl);
	}
	
	private String getAspsessions(String the_url)
	{
		//TODO return the cookie string
				
				String html=null;
				//try and catch block to get the pages
				try {
					
					/* get the starting page and its view state using a GET command*/
					//this can be re-used since base 64 decoding is used by ASP .Net
					
					//set up the initial GEt
					
					URL url = new URL (the_url);
					HttpURLConnection conn= (HttpURLConnection) url.openConnection();
					conn.setConnectTimeout(timeout);
					conn.setReadTimeout(timeout);
					
					//set up browser
					conn.setInstanceFollowRedirects(false);
					conn.setUseCaches(false);
					
					for(int i=0;i<values.length;i++)
					{
						if(values[i] != null)
							conn.setRequestProperty(headers[i], values[i]);
					}
					
					//set the method type
					if(method != null)
					{
						conn.setRequestMethod(method);
						
						if(method.compareTo("POST")==0)
						{
							conn.setDoOutput(true);
							conn.setDoInput(true);
							conn.setInstanceFollowRedirects(false);
						}
					
					}
					
					if(url_params != null)
					{
						DataOutputStream wr=new DataOutputStream(conn.getOutputStream());
						wr.writeBytes(url_params);
						wr.flush();
						wr.close();
					}
					
					BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));

					html="";
					int c=0;
					
					while((c=br.read())!=-1)
					{
						html+=(char) c;
					}
					
					conn.getContent();
					
					
					}catch(MalformedURLException e)
					{//TODO failed to form url
						e.printStackTrace();
					} catch (IOException e) {
						// TODO failed to read site
						e.printStackTrace();
					}
				
				//return the cookie string
				return format_cookies(mgr.getCookieStore().getCookies());
	}
	
	/**
	 * Encode the url parameters
	 * 
	 * @param innames
	 * @param invals
	 */
	public void encode_url_params(String[] innames, String[] invals)
	{
		//TODO encode the url parameters
		int length=innames.length;
		url_params="";
		for(int i=0;i<length;i++)
		{
				try{
					if(i==0)
						url_params=URLEncoder.encode(innames[i],"UTF-8")+"="+URLEncoder.encode(invals[i],"UTF-8");
					else
						url_params+="&"+URLEncoder.encode(innames[i],"UTF-8")+"="+URLEncoder.encode(invals[i],"UTF-8");
				}catch(UnsupportedEncodingException e)
				{
					e.printStackTrace();
				}
				
		}
	}
	
	/**
	 * Get the ssl page without changing the trust manager
	 * @param the_url
	 * @return
	 */
	public String get_ssl_page(String the_url)
	{
		//TODO manually get SSL pages, Preferred method over Trust Manager
		//for use when the certificate is signed by a trusted authority
		//uses a basic HttpsUrlConnection
		String html=null;
		try{
			
	        
			//url
	        URL url=new URL(the_url);
			
	        //connection 
			HttpsURLConnection conn= (HttpsURLConnection) url.openConnection();
			conn.setConnectTimeout(timeout);
			conn.setReadTimeout(timeout);
			//set the ssl provider to be sun
			Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
			
			//set the request headers
			for(int i=0;i<values.length;i++)
			{
				if(values[i] != null)
					conn.setRequestProperty(headers[i], values[i]);
			}
			
			//set up browser
			conn.setInstanceFollowRedirects(false);
			conn.setUseCaches(false);
			
			
			//set the method type
			if(method != null)
			{
				conn.setRequestMethod(method);
				
				if(method.compareTo("POST")==0)
				{
					conn.setDoOutput(true);
					conn.setDoInput(true);
					conn.setInstanceFollowRedirects(false);
					
					if(url_params != null)
					{
						DataOutputStream wr=new DataOutputStream(conn.getOutputStream());
						wr.writeBytes(url_params);
						wr.flush();
						wr.close();
					}
				}
			
			}
			
			
			
			BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
			html="";
			int c=0;
			
			while((c=br.read())!=-1)
			{
				html+=(char) c;
			}
			br.close();
			
			conn.getContent();
			if(mgr!=null){
				cookies=format_cookies(mgr.getCookieStore().getCookies());
			}
			
			conn.disconnect();
		}catch(IOException e)
		{
			e.printStackTrace();
		} 
		
		
		return html;
	}
	
	
	private String format_cookies(List<HttpCookie> incookies)
	{
		
		
		String cookie_string=null;
		HttpCookie c=null;
		Iterator<HttpCookie> it=incookies.iterator();
		
		while(it.hasNext())
		{
			c=it.next();
			try
			{
				//add the cookie to the list in the appropriate UTF-8 encoded format
				//separated by a semi-colon
				if(cookie_string == null)
				{
					cookie_string=URLEncoder.encode(c.getName(),"UTF-8");
				}
				else
				{
					cookie_string+=URLEncoder.encode(c.getName(),"UTF-8");
				}
				cookie_string+="="+URLEncoder.encode(c.getValue(),"UTF-8")+URLEncoder.encode(";", "UTF-8")+";";
			
				
			}catch(UnsupportedEncodingException e)
			{
				e.printStackTrace();
			}
		}
		
		return cookie_string;
	}
	
	
	
	/**
	 * Set a cookie string which will be checked later
	 * @param incookies
	 */
	public void setCookies(String incookies)
	{
		//TODO set any cookies
		cookies=incookies;
	}
	
	/**
	 * Download using an SSL cert: will check for cookies
	 * Sets the @param img variable
	 * 
	 * @param headers
	 * @param values
	 */
	public void download_ssl(String[] headers, String[] values, String inurl)
	{
		//TODO download an image from an SSL connection

		try {
			
			//url
	        URL urlset=new URL(inurl.replaceAll(" ", "%"));
			
	        //connection 
			HttpsURLConnection conn;
			conn = (HttpsURLConnection) urlset.openConnection();
		
			conn.setConnectTimeout(timeout);
			conn.setReadTimeout(timeout);
			
			//set the ssl provider to be sun
			Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
		
			//set up browser
			conn.setInstanceFollowRedirects(false);
			conn.setUseCaches(false);
			
			//set the headers and header values
			for(int i=0;i<values.length;i++)
			{
				if(values[i] != null)
					conn.setRequestProperty(headers[i], values[i]);
			}
			
			if(cookies != null)
			{
				conn.setRequestProperty("Cookie", cookies);
			}
			
			
			
			
			//the input stream from the net
			InputStream is=conn.getInputStream();
			
			
			//get byte array 
			byte[] bytes=IOUtils.toByteArray(is);
			
			
			
			//get image bytes
			ibytes=bytes;
			
			//set the image type
			set_image_type(inurl);
	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Download as if using a web page
	 * 
	 * @param headers
	 * @param values
	 */
	private void html_download(String[] headers,String[] values)
	{
		//TOOD perform the get request and put into an image
		/* get the starting page and its view state using a GET command*/
		//this can be re-used since base 64 decoding is used by ASP .Net
		
		String the_url=url;
		
		//Get the page
		URL url;
		try {
			url = new URL (the_url);
			HttpURLConnection conn= (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(timeout);
			conn.setReadTimeout(timeout);
			
			//set up browser
			conn.setInstanceFollowRedirects(false);
			conn.setUseCaches(false);
			
			for(int i=0;i<values.length;i++)
			{
				if(values[i] != null)
					conn.setRequestProperty(headers[i], values[i]);
			}
			
			if(cookies != null)
			{
				conn.setRequestProperty("Cookie",cookies);
			}
			
			
			//the input stream from the net
			InputStream is=conn.getInputStream();
			
			//get byte array 
			byte[] bytes=IOUtils.toByteArray(is);
			
			//get image bytes
			ibytes=bytes;
			
			//set the image type
			if(ibytes != null)
			{
				set_image_type(the_url);
			}
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Straight Download for Speed.
	 * Sets the @param img variable
	 */
	private void download_single()
	{
		//TODO download an image from an fpath on a single thread
		
		if(url != null)
		{
			try {
				//create the url
				URL connurl=new URL(url);
				
				//open the connection
				HttpURLConnection conn=(HttpURLConnection) connurl.openConnection();
				conn.setConnectTimeout(timeout);
				conn.setReadTimeout(timeout);
				
				//the input stream from the net
				InputStream is=conn.getInputStream();
				
				//get byte array 
				byte[] bytes=IOUtils.toByteArray(is);

				//set the image bytes to save the 
				ibytes=bytes;
				
				//set the downloaded iamge type
				set_image_type(url);

			}catch(IOException e)
			{
				//failure to get the image
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 
	 * @return imgType
	 */
	public String get_image_type()
	{
		//TODO return the image type
		return imgType;
	}
	
	/**
	 * Set the image type to jpg
	 */
	private void set_image_type(String inurl)
	{
		//TODO get and return the image type using a buffered image
		imgType=null;
		String urltst=inurl;
		//get the image type from the url
		
		if(url != null)
		{
			urltst=inurl.toLowerCase();
		}
		else if(pass=true)
		{
			urltst="jpg";
		}
		//get the image type from the url which should contain the MIME type
		
		if(!urltst.toLowerCase().contains("jpg") | !urltst.toLowerCase().contains("jpeg"))
		{
			ByteArrayInputStream bis=new ByteArrayInputStream(ibytes);
			
			if(bis != null)
			{
				ByteArrayOutputStream bos=new ByteArrayOutputStream();
				//convert to jpeg for compression
				try {
					//use apache to read to a buffered image with certain metadata and then convert to a jpg
					BufferedImage image=Imaging.getBufferedImage(bis);
					ImageIO.write(image, "jpg", bos);
					ibytes=bos.toByteArray();
				
				} catch (ImageReadException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		imgType="jpg";
		
	}
	
	/**
	 * Read Image Meta Data
	 * 
	 * @return metadata
	 */
	public String read_meta_data()
	{
		//TODO allows for checking meta data to get content names to pass for setting metadata
		//a byte array of the image
		
		//the meta_dataString
		String meta="";
		
				//the image input stream
				ImageInputStream iis=null;
				
				
				//the input stream for a byte array of the image
				InputStream is=null;
				
				
				//get a byte array representing the image
				ByteArrayOutputStream bos=new ByteArrayOutputStream();
				
				//a buffered input stream
				BufferedInputStream bis=null;
				
				try {
					
					is= new ByteArrayInputStream(ibytes);
					bis=new BufferedInputStream(new ByteArrayInputStream(ibytes));
					bos.close();
					
					//attempt to take the byte array and use streams to get a reader of metadata
					/*onlty the method read destroys meta data*/
					iis=ImageIO.createImageInputStream(is);

					//the metadata from apache
					
					Metadata metadata=null;
					
					metadata=JpegMetadataReader.readMetadata(bis);
					if(metadata != null)
					{
						
						Iterator<Directory> it=metadata.getDirectories().iterator();
						while(it.hasNext())
						{
							Directory d=it.next();
							Iterator<Tag> it2=d.getTags().iterator();
								while(it2.hasNext())
								{
									Tag tag=it2.next();
									meta+="[" + d.getName() + "] " + tag.getTagName() + " = " + tag.getDescription()+"\n";
								}
						}
					}
							
					iis.close();
					is.close();
					bis.close();
						
					} catch (ImageProcessingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				return meta;
	}

	
	/**
	 * Add a metadata string to a jpegs user comment field
	 * Only the comment may be passed in.
	 * @param data
	 */
	
	private void add_to_jpeg(String data)
	{
		//TODO add the data if dealing with a jpeg
		
		//the image data
			TiffOutputSet outset=null;
			BufferedImage bi;
			IImageMetadata metadata;
			ByteArrayOutputStream bos=null;
			try {
				
				//get the buffered image to write to
				bi=Imaging.getBufferedImage(ibytes);
				metadata = Imaging.getMetadata(ibytes);
				JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
				
				if(null != jpegMetadata)
				{
					//get the image exif data
					TiffImageMetadata exif=jpegMetadata.getExif();
					outset=exif.getOutputSet();
				}
				
				if(outset==null)
				{
					//get a new set (directory structured to write to)
					outset=new TiffOutputSet();
				}


				
				TiffOutputDirectory exdir=outset.getOrCreateExifDirectory();
				exdir.removeField(ExifTagConstants.EXIF_TAG_USER_COMMENT);
				
				exdir.add(ExifTagConstants.EXIF_TAG_USER_COMMENT,data.trim());

				bos=new ByteArrayOutputStream();
				ByteArrayInputStream bis=new ByteArrayInputStream(ibytes);
				
				ExifRewriter exrw=new ExifRewriter();
				
				//read to a byte stream
				exrw.updateExifMetadataLossy(bis, bos, outset);
			
				//read the input from the byte buffer
				ibytes=bos.toByteArray();
				bis.close();
				bos.close();
				
				
			} catch (ImageReadException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ImageWriteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				if(bos != null)
				{
					try{
						bos.flush();
					bos.close();
					}catch(IOException e)
					{
						e.printStackTrace();
					}
				}
			}
		
	}
	
	
	/**
	 * Call the meta data add
	 * @param data
	 */
	private void add_meta(String data)
	{
		//TODO write meta data to a jpeg, must specify type
		
		//attempt to take the byte array and use streams to get a reader of metadata
		
			if(imgType=="jpg")
			{
				//read to a jpeg
				add_to_jpeg(data);	
			}
	}
	
	/**
	 * Get the image bytes
	 * @return byte_string
	 */
	public String get_ibytes_string()
	{
		//TODO gets the image_bytes_as a string
		//jpeg file is iniitially in bytes representing characters as metadata and ids contain chars
		StringBuilder builder=new StringBuilder();
		
		ByteArrayInputStream bis=new ByteArrayInputStream(ibytes);
		int b;

		while((b=bis.read())!=-1)
		{
			builder.append((char)b);
		}
		
		return builder.toString();
	}
	
	/**
	 * Attempts to tone down accidental red coloring
	 */
	public void reaverage(double factor)
	{
		BufferedImage color=getImage();
		
		if(color != null)
		{
			BufferedImage averaged=new BufferedImage(color.getWidth(),color.getHeight(),BufferedImage.TYPE_INT_RGB);
		
			for(int i=0; i<color.getWidth();i++)
			{
				for(int j=0;j<color.getHeight();j++)
				{
					Color c=new Color(color.getRGB(i, j));
				
					averaged.setRGB(i, j, new Color((int) Math.round(c.getRed()/factor),c.getGreen(),c.getBlue()).getRGB());
				}
			}
		
			ByteArrayOutputStream bos=new ByteArrayOutputStream();
			//convert to jpeg for compression
			try {
				//use apache to read to a buffered image with certain metadata and then convert to a jpg
				ImageIO.write(averaged, "jpg", bos);
				ibytes=bos.toByteArray();
			
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	/**
	 * Attempt to Reset anything to Black and White
	 */
	public void setBlackandWhite()
	{
		ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);  
		ColorConvertOp op = new ColorConvertOp(cs, null);  
		BufferedImage image = op.filter(getImage(), null); 

		ByteArrayOutputStream bos=new ByteArrayOutputStream();
		//convert to jpeg for compression
		try {
			//use apache to read to a buffered image with certain metadata and then convert to a jpg
			ImageIO.write(image, "jpg", bos);
			ibytes=bos.toByteArray();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Save to a file path
	 * @param fpath
	 */
	public void save(String fpath)
	{
		//TODO Save an image to a Specified File Path on Disk
		
		if(ibytes != null){
			//get the directory from the file path
			String[] temp=fpath.split("\\\\");
			String dir=null;
			for(int s=0;s<temp.length-1;s++)
			{
				dir=(dir==null)?temp[s]:dir+"/"+temp[s];
			}
		
			//throw an exception if the directory is null
			if(dir==null | temp.length==1 | temp.length==0)
			{
				try{
					throw new BadDirectory();
				}catch(BadDirectory e)
				{
					e.printStackTrace();
					System.exit(-1);
				}
			}
		
			//create the directory if it does not exist
			File f=new File(dir);
		
			if(f.exists()==false)
			{
				//make the directory if it does not exist
				f.mkdir();
			}
		
		
		
			//create the file path
			f=new File(fpath);
		
		
			if(f.exists()==false)
			{
				//create the file if it does not exist
				try {
					f.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		
			
			//attempt to write the image to a file path
			//writes the byte stream to a file to preserve the metadata
			try {
				
				if(f.exists()){
					FileOutputStream fos=new FileOutputStream(f);
					BufferedOutputStream s=new BufferedOutputStream(fos);
					
					if(ibytes != null){
						s.write(ibytes);
					}
					
					fos.close();
					s.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	
	/**
	 * Save an image using bytes.
	 * Takes in a path as a string.
	 * 
	 * @param inpath
	 */
	public void save_bytes(String inpath)
	{
		//TODO write bytes to the path
		try {
			File f=new File(inpath);
			
			if(!f.exists())
			{
				f.createNewFile();
			}
			
			if(f.exists())
			{
				FileOutputStream fos=new FileOutputStream(new File(inpath));
				fos.write(ibytes);
				fos.flush();
				fos.close();
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
