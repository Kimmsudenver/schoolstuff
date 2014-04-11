package com.hygenics.distance;

/** 
 * The levenshtein algorithm. Can help enforce a schema.
 * @author asevans
 *
 */
public class Levenshtein {

	private String stringa;
	private String stringb;
	
	public Levenshtein(){
		
	}
	
	
	
	public String getStringa() {
		return stringa;
	}



	public void setStringa(String stringa) {
		this.stringa = stringa;
	}



	public String getStringb() {
		return stringb;
	}



	public void setStringb(String stringb) {
		this.stringb = stringb;
	}


	/**
	 * Perform the alg.
	 * Not the fastest (it looks O^n but actually iterates down the string a bunch of times)
	 * It works, I'm happy and its no worse than the original alg.
	 * 
	 * @return
	 */
	private int getDistance(){
		int dist=0;
		
		//check for any insertions
		if(stringa.length()!=stringb.length()){
			
			//check stringa for points where an insertion would bring the string closer to stringb
			//this is meant to get rid of mistakes where the strings contents have been merely left out
			int j=0;
			String tmp;
			String tmpb;
			
			while(j<stringa.length() & j<stringb.length()){
				
				if(stringa.charAt(j)!=stringb.charAt(j)){
					tmp=stringa.substring(0, j);
					tmpb=stringa.substring(j,stringa.length());
					tmp=tmp+stringb.charAt(j);
					
					if((j+1)<stringb.length()){
						
						if(tmp.compareTo(stringb.substring(0,(j+1)))==0){
							dist++;
							stringa=tmp+tmpb;
						}
						
					}
				}
				j++;
			}
			
		}
		
		//check for any updates
		for(int i=0;i<stringa.length();i++){
			
			if(i>stringb.length())
			{
				i=stringa.length();
			}
			else if(i>(stringa.length()-1)| i>(stringb.length()-1)){
				break;
			}
			else if(stringa.charAt(i)!=stringb.charAt(i)){
				dist++;
			}			
		}
		
		
		//check for remaining insertions and for deletions
		if(stringa.length() != stringb.length())
		{
			dist+=Math.abs(stringa.length()-stringb.length());
		}

		
		//insertion of any remaining characters
		return dist;
	}
	
	/**
	 * run the alg
	 * @return
	 */
	public int run(){
		return getDistance();
	}
	
	/**
	 * run the alg
	 * @param stringa
	 * @param stringb
	 * @return
	 */
	public int run(String stringa, String stringb){
		this.stringa=stringa;
		this.stringb=stringb;
		return run();
	}
	
	
}
