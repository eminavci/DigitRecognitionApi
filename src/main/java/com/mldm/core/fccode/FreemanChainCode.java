package com.mldm.core.fccode;

import java.awt.Color;
import java.awt.image.BufferedImage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mldm.core.utl.Consts;

/**
 * @author emin
 *
 */
public class FreemanChainCode {

	private static final Logger logger = LoggerFactory.getLogger(FreemanChainCode.class);
	
	private StringBuilder freemanCode;
	private BufferedImage origImg;
	private BufferedImage chainImg;
	private int[][] binaryImgMatrix;

	private Coord startingPixel;
	
	/**
	 * USe other constructor
	 * @param bImg
	 */
	@Deprecated
	public FreemanChainCode(BufferedImage bImg) {
		this.origImg = bImg;
		initBinaryImageMatrix();
		this.startingPixel = initStartingPixel(FROMDIR.TOP);
		this.freemanCode = new StringBuilder("");
	}
	
	public FreemanChainCode(int[][] origBinaryImgMatrix){
		this.origImg = new BufferedImage(origBinaryImgMatrix.length, origBinaryImgMatrix[0].length, BufferedImage.TYPE_INT_RGB);
		for (int y = 0; y < origBinaryImgMatrix.length; y++) {
	        for (int x = 0; x < origBinaryImgMatrix[0].length; x++) {
	            int pixel=origBinaryImgMatrix[y][x];
	            if(pixel == 0 )
	            	pixel = 255;
	            else
	            	pixel = 0;
	            this.origImg.setRGB(x, y, new Color(pixel, pixel, pixel).getRGB());
	        }
	    }

		this.binaryImgMatrix = origBinaryImgMatrix;
		
		this.startingPixel = initStartingPixel(FROMDIR.TOP);
		this.freemanCode = new StringBuilder("");
	}
	
	private void initBinaryImageMatrix(){
		int w = this.origImg.getWidth();
		int h = this.origImg.getHeight();
		binaryImgMatrix = new int[w][h];
		
		for( int y = 0; y < w; y++ ){
		    for( int x = 0; x < h; x++ ){
		    	binaryImgMatrix[y][x] = this.origImg.getRGB( x, y ) == Consts.PX_WHITE ? Consts.NO_PIXEL : Consts.PIXEL;        
		    }
		}
	}
	
	public StringBuilder computeFCC(){
		int dir = 0;
		PxTile nextPixel = new PxTile(dir, this.startingPixel);
		do {
			dir = (5 + nextPixel.getfCode()) % 8;
			nextPixel = new PxTile(dir, nextPixel.getCoord()).getNextBoundPixel(dir, this.binaryImgMatrix);
			this.freemanCode.append(nextPixel.getfCode());
		} while(nextPixel.getCoord().getX() != this.startingPixel.getX() || nextPixel.getCoord().getY() != this.startingPixel.getY());
		return this.freemanCode;
	}

	
	/** if isThick == true, will add extra object pixel to border, to make bound ticker <br>
	 *  for outside dir + 6 (mod 8) <br>
	 *  for inside  dir + 2 (mod 8) <br>
	 *  isCentralizeChar = true put drawn character in the center of matrix
	 * @param isThick
	 * @param isCentralizeChar
	 * @return
	 * @throws Exception
	 */
	public int[][] getBoundedImgMatrix(boolean isCentralizeChar, boolean isThick) throws Exception{
		if(this.freemanCode.length() < 2)
			throw new Exception("Freeman code should be calculated first!");
		
		int[][] ttt = new int[this.binaryImgMatrix.length][this.binaryImgMatrix[this.binaryImgMatrix.length -1].length];
		Coord coord;
		if(isCentralizeChar)
			coord = getCentralizedStartingPixel();
		else
			coord = this.startingPixel;
		
		int iindx = 0;
		do {
			ttt[coord.getY()][coord.getX()] = 1;
			int dirr = Character.getNumericValue(this.freemanCode.charAt(iindx));
			coord = nextCoord(dirr, coord);
			
			if(isThick){
				Coord thickCoord = null;
				try {
					thickCoord = nextCoord(dirr%2 == 0 ? (dirr + 6)%8 : (dirr + 7)%8, coord);
					ttt[thickCoord.getY()][thickCoord.getX()] = 1;
				} catch (Exception e) {
					try {
						thickCoord = nextCoord(dirr%2 == 0 ? (dirr + 2)%8 : (dirr + 1)%8, coord);
						ttt[thickCoord.getY()][thickCoord.getX()] = 1;
					} catch (Exception e1) {}
				}
			}
			
			iindx++;
		} while (this.freemanCode.length() > iindx);
		
		return ttt;
	}
	
	/** if isThick == true, will add extra object pixel to border, to make bound ticker <br>
	 *  for outside dir + 6 (mod 8) <br>
	 *  for inside  dir + 2 (mod 8) <br>
	 *  centralizeChar = true put drawn character in the center of matrix
	 * @param isThick
	 * @return
	 * @throws Exception
	 */
	public BufferedImage getBoundedBufferedImg(boolean centralizeChar, boolean isThick) throws Exception{
		
		int[][] bMatrix = getBoundedImgMatrix(centralizeChar, isThick);
		
		this.chainImg = new BufferedImage(bMatrix.length, bMatrix[0].length, BufferedImage.TYPE_INT_RGB);
	    for (int y = 0; y < bMatrix.length; y++) {
	        for (int x = 0; x < bMatrix[0].length; x++) {
	            int pixel=bMatrix[y][x];
	            if(pixel == 0 )
	            	pixel = 255;
	            else
	            	pixel = 0;
	            this.chainImg.setRGB(x, y, new Color(pixel, pixel, pixel).getRGB());
	        }
	    }
	    return this.chainImg;
	}
	
	private Coord getCentralizedStartingPixel(){
		Coord l = initStartingPixel(FROMDIR.LEFT);
		Coord r = initStartingPixel(FROMDIR.RIGHT);
		Coord b = initStartingPixel(FROMDIR.BOTTOM);
		//logger.debug("MATRIX LENGHTS : [" + this.binaryImgMatrix.length + ", " + this.binaryImgMatrix[0].length + "]");
		//logger.debug("TOP : " + this.startingPixel + " LEFT : " + l + " RIGHT : " + r + " BOTTOM : " + b);
		
		int hor = Math.abs(r.getX() - l.getX());
		int ver = Math.abs(b.getY() - this.startingPixel.getY());	
		
		//logger.debug("HORIZONTAL : " + hor + " VERTICAL : " + ver);
		
		int temp_x = 0, temp_y = 0;
		if(ver < this.binaryImgMatrix.length)
			temp_y = (int) Math.floor((this.binaryImgMatrix.length - ver - 1) / 2.0); // because it starst from 0
		
		if(hor < this.binaryImgMatrix.length)
			temp_x = (int) Math.floor((this.binaryImgMatrix[0].length - hor - 1) / 2.0);
		
		//logger.debug("NEW POINT : [" + temp_x + ", " + temp_y + "]");
		
		if(temp_x < l.getX())
			temp_x  = this.startingPixel.getX() - Math.abs(temp_x - l.getX());
		else
			temp_x  = this.startingPixel.getX() + Math.abs(temp_x - l.getX());
		
		if(temp_y < this.startingPixel.getY())
			temp_y = this.startingPixel.getY() - Math.abs(temp_y - this.startingPixel.getY());
		else
			temp_y = this.startingPixel.getY() + Math.abs(temp_y - this.startingPixel.getY());
		
		return new Coord(temp_x, temp_y);
	}
	
	
	
	private Coord nextCoord(int dirr, Coord coord) throws Exception{
		return new Coord(coord.getX() + Consts.dirCol[dirr], coord.getY() + Consts.dirRow[dirr]);
	}
	
	private Coord initStartingPixel(FROMDIR fromD){
		if(fromD == FROMDIR.TOP)
			for (int y = 0; y < this.binaryImgMatrix.length; y++) {
				for (int x = 0; x < this.binaryImgMatrix[y].length; x++) {
					if(this.binaryImgMatrix[y][x] == Consts.PIXEL){
						return new Coord(x, y);
					}
				}
			}
		else if(fromD == FROMDIR.RIGHT)
			for (int x = this.binaryImgMatrix[0].length - 1; x >= 0 ; x--) {
				for (int y = 0; y < this.binaryImgMatrix.length ; y++) {
					if(this.binaryImgMatrix[y][x] == Consts.PIXEL){
						return new Coord(x, y);
					}
				}
			}
		else if(fromD == FROMDIR.LEFT)
			for (int x = 0; x < this.binaryImgMatrix[0].length; x++) {
				for (int y = 0; y < this.binaryImgMatrix.length; y++) {
					if(this.binaryImgMatrix[y][x] == Consts.PIXEL){
						return new Coord(x, y);
					}
				}
			}
		else if(fromD == FROMDIR.BOTTOM)
			for (int y = this.binaryImgMatrix.length -1 ; y >= 0 ; y--) {
				for (int x = 0 ; x < this.binaryImgMatrix[y].length; x++) {
					if(this.binaryImgMatrix[y][x] == Consts.PIXEL){
						return new Coord(x, y);
					}
				}
			}
		return null;
	}
	
	
/*
 * ################################################################################################
 * ################################  *** PRINT STUFFS #############################################
 * ################################################################################################
 * */
	public int[][] getOriginalImageMatrix(boolean isPrint){
		int w = this.origImg.getWidth();
		int h = this.origImg.getHeight();
		int[][] pixels = new int[w][h];
		
		for( int y = 0; y < w; y++ ){
		    for( int x = 0; x < h; x++ ){
		        pixels[y][x] = this.origImg.getRGB( x, y );
		        if(isPrint)
		        	System.out.print(pixels[y][x] + " ");
		    }
		    if(isPrint)
		    	System.out.println("");
		}
		return pixels;
	}
	
	/**
	 *  Prints the original binary matrix of buffered Image
	 */
	public void printCounterImgMatrix(){
		for (int y = 0; y < this.binaryImgMatrix.length; y++) {
			for (int x = 0; x < this.binaryImgMatrix[y].length; x++) {
				System.out.print(this.binaryImgMatrix[y][x] == 1 ? this.binaryImgMatrix[y][x] + " " : " " + " ");
			}
			System.out.println("");
		}
	}
	
	/**
	 * Prints binary matrix of bounder found buffered image <br>
	 * withBackground = true, prints 0 for background, else, its empty chatacter
	 * @param withBackground
	 * @throws Exception
	 */
	public void printBoundedImgMatrix(boolean withBackground) throws Exception{
		
		if(this.freemanCode.length() < 2)
			throw new Exception("Freemon code should be calculated first!");
		
		int[][] ttt = getBoundedImgMatrix(false, false);
		
		for (int y = 0; y < ttt.length; y++) {
			for (int x = 0; x < ttt[y].length; x++) {
				int cc = ttt[y][x];
				if(!withBackground){
					if(cc == Consts.NO_PIXEL){
						System.out.print("  ");
					} else
						System.out.print(cc + " ");
				} else
					System.out.print(cc + " ");
				
			}
			System.out.println("");
		}
		
	}
	
	public StringBuilder getFreemanCode() {
		return freemanCode;
	}

	public void setFreemanCode(StringBuilder freemanCode) {
		this.freemanCode = freemanCode;
	}

	public BufferedImage getOrigImg() {
		return origImg;
	}

	public void setOrigImg(BufferedImage origImg) {
		this.origImg = origImg;
	}

	public BufferedImage getChainImg() {
		return chainImg;
	}

	public void setChainImg(BufferedImage chainImg) {
		this.chainImg = chainImg;
	}

	public int[][] getBinaryImgMatrix() {
		return binaryImgMatrix;
	}

	public void setBinaryImgMatrix(int[][] binaryImgMatrix) {
		this.binaryImgMatrix = binaryImgMatrix;
	}

	public Coord getStartingPixel() {
		return startingPixel;
	}

	public void setStartingPixel(Coord startingPixel) {
		this.startingPixel = startingPixel;
	}
	
	public enum FROMDIR{
		LEFT,
		RIGHT,
		TOP,
		BOTTOM;
	}
}
