package com.hygenics.crawler;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.hygenics.crawler.ProxyImage;

/**
 * Given a silhuoette image, detect anyhting that is so-close as to be considered the silhouette. This can be 
 * done quickly with point to point comparison or, for more difficult images, using splines and covar
 * 
 * @author asevans
 */
public class DetectSilhuoette {
	
	private Logger log=LoggerFactory.getLogger(MainApp.class);
	private long timeout=5000L;
	private int maxthreads=100;
	private String silpath;
	private String dir;
	private double allowablevar=0;
	private boolean deepcompare=false;
	
	public DetectSilhuoette(){
		
	}

	
	
	public long getTimeout() {
		return timeout;
	}



	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}



	public String getSilpath() {
		return silpath;
	}

	@Required
	public void setSilpath(String silpath) {
		this.silpath = silpath;
	}

	

	public int getMaxthreads() {
		return maxthreads;
	}

	public void setMaxthreads(int maxthreads) {
		this.maxthreads = maxthreads;
	}

	@Required
	public String getDir() {
		return dir;
	}

	public void setDir(String dir) {
		this.dir = dir;
	}

	public boolean isDeepcompare() {
		return deepcompare;
	}

	public void setDeepcompare(boolean deepcompare) {
		this.deepcompare = deepcompare;
	}
	

	public double getAllowablevar() {
		return allowablevar;
	}

	public void setAllowablevar(double allowablevar) {
		this.allowablevar = allowablevar;
	}

	protected class Hash implements Callable<String>{
		
		private String fpath;
		private BufferedImage bi;
		
		/**
		 * Constructor for Callable
		 * @param bi
		 * @param fpath
		 */
		public Hash(BufferedImage bi,String fpath){
			this.bi=bi;
			this.fpath=fpath;
		}
		
		
		public String call(){
			//TODO create a working color hash
			String hash=null;
			if(bi != null){
				for(int i=0;i<bi.getWidth();i++){
					for(int j=0;j<bi.getHeight();j++){
						hash=(hash==null)?Integer.toString(bi.getRGB(i, j)):hash+Integer.toString(bi.getRGB(i, j));
					}
				}
			}
			else{
				hash=Long.toString(0L);
			}
			
			return hash;
			
		}
	}
	
	
	/**
	 * Compare the pixels of the image together to see if they are an exact match.
	 * @return
	 */
	public void shallowCompare(){
		log.info("Performing Shallow Compare in the specified directory");
		//map of hash and file
		Map<String,String> hashes=new HashMap<String,String>();
		
		//get proxy image
		ProxyImage silhouette=ProxyImage.getProxy();
		
		if(silpath != null){
			//set proxy if it is not correct-the proxy is the silhouette path
			//the simple compare is fast enough, ImageIO is synchronized and I've ensured it
			if(silhouette.getFpath()==null){
				silhouette.setBi(silpath);
			}else if(silhouette.getFpath().compareTo(silpath) != 0){
				silhouette.setBi(silpath);
			}
		}
		
		if(silhouette.getBI()==null){
			try{
				throw new NullPointerException("A Valid Silhouette Path Must be Provided");
			}catch(NullPointerException e){
				e.printStackTrace();
			}
		}
		
		//get the silhouette hash
		String silhash=null;
		for(int i=0;i<silhouette.getBI().getHeight();i++){
			for(int j=0;j<silhouette.getBI().getWidth();j++){
				silhash=(silhash==null)?Integer.toString(silhouette.getRgb(i, j)):silhash+Integer.toString(silhouette.getRgb(i, j));
			}
		}
		
		if(silhash==null){
			try{
				throw new NullPointerException("Silhouette is not valid");
			}catch(NullPointerException e){
				e.printStackTrace();
				System.exit(-1);
			}
		}
		
		int[] colors;
		
		//get the file
		String[] split;
		ExecutorService service=Executors.newCachedThreadPool();
		ArrayList<Future<String>> futures=new ArrayList<Future<String>>();
		int i=0;
		for(String ifpath: new File(dir).list()){
			try {
				
				futures.add(service.submit(new Hash(ImageIO.read(new File(ifpath)),ifpath)));
				
				
				if((i% maxthreads)==0){
					//iterate and create the color hash for each
					service.awaitTermination(timeout, TimeUnit.MILLISECONDS);
					service.shutdown();
					
					
					for(Future<String> f:futures){
						String hash=f.get();
						split=hash.split("~");
						hashes.put(split[0].trim(),split[1].trim());
					}
					
					
				}
				i++;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	
		
		
		//perform the color compare from the byte array
		for(String k: hashes.keySet()){
			if(hashes.get(k).compareTo(silhash)==0){
				//remove the image
				log.info("Deleting Image: "+k);
				File f=new File(k);
				f.delete();
			}
		}
		
	}
	
	/**
	 * Use the spline map to perform a more mathematical comparison on covariance. This will be threaded.
	 * Its takes some memory but is probably about as intensive as the string classes.
	 * @return
	 */
	public void deepCompare(){
		//get the features
		
		
		//get the spline points
		
		//compare to the stored points
		
		//check cutoff value
	
	}
	
}
