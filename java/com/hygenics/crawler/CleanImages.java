package com.hygenics.crawler;

/**
 * The purpose of this class is to clean images from a Mapping of files. It uses my freeware, a result of learning
 * high quality Matrix math and having fun with Wikepedia which sometimes (2 classes) uses other work I cite below.
 * Using other's work may be a time sink so almost all was written from scratch.
 * 
 * Contained in this class is every ability in the free evans.imagetools package
 * on my github. Nothing mor narc. than using your own name :)
 * 
 * 		Saves to same file:
 * 
 *  	*Blur-Gauss, box
 *  	*Average- remove red and other noise
 *  	*Greyscale-convert to GreyScale
 *  	*Sharpen-sharpen
 *  	*DeNoise- reduce noise
 *  	*Rotate- use a rotational matrix to rotate an image
 *  	*MetaData-use Drew Noakes imaging tools because I'm lazy to append meta data to a 
 *  	jpeg byte string (for those decoding Base 64, see my html_grab class, this comes after
 *  	JFIF(normally), you can do your own with the appropriate style and some easy io)	
 *  	
 *  
 *  	Saves to new folder with same file name:
 *  	*Edge Detection-Canny, specify a separate file in your string (sloppy/greedy behavior, non-greedy both fairly quick)
 *  	*Gabor Filter- for testing if you want, applies a specified gabor filter from the ImageJ sets (need to learn mesh grids so my class is 3/4 complete)
 *  	
 *  	Saves to a Database:
 *  	*Spline Map-a really awesome array :( stored in a database created from my spline
 *  	stuff, OpenCV feature rec. using HAAR cascades, and specified offsets
 * 
 * This class is ripe for being individualized and accessed via a server in the final 
 * distribution process.
 * 
 * @author asevans
 *
 */
public class CleanImages {
	
	
	
	public CleanImages(){
		
	}
	
	

}
