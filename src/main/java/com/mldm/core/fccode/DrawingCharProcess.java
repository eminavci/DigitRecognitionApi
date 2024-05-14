package com.mldm.core.fccode;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mldm.core.exception.CoreException;
import com.mldm.core.utl.Consts;
import com.mldm.core.utl.Util;
import com.mldm.rest.common.ApiException.MTYPE;

import net.coobird.thumbnailator.Thumbnailator;

/**
 * @author emin
 *
 */
public class DrawingCharProcess{

	private static final Logger logger = LoggerFactory.getLogger(DrawingCharProcess.class);

	private BufferedImage drawedImg;
	private int[][] drawedImgMatrix;
	private List<FreemanChainCode> detectedComponenets;
	
	
	public DrawingCharProcess(byte[] imageInByte) throws IOException {
		this.drawedImg = Util.byteArrayToBufferedImage(imageInByte);
//		File outputfile = new File("/home/avci/Desktop/dddimage.png");
//		ImageIO.write(drawedImg, "png", outputfile);
		initBinaryImageMatrix();
		this.detectedComponenets = new ArrayList<FreemanChainCode>();
	}

	private void initBinaryImageMatrix(){
		int w = this.drawedImg.getWidth();
		int h = this.drawedImg.getHeight();
		this.drawedImgMatrix = new int[w][h];
		
		for( int y = 0; y < w; y++ ){
		    for( int x = 0; x < h; x++ ){
		    	this.drawedImgMatrix[y][x] = this.drawedImg.getRGB( x, y ) == Consts.PX_WHITE ? Consts.NO_PIXEL : Consts.PIXEL;
		    }
		}
	}
	
	public void compute() throws Exception{
		if (this.drawedImgMatrix == null || this.drawedImgMatrix.length == 0)
			throw new Exception("Img Matrix is null! Check it again.");
		
		int count = 0;
		for (int i=0; i<this.drawedImgMatrix.length; i++) {
			for (int j=0; j<this.drawedImgMatrix[i].length; j++) {
				if (this.drawedImgMatrix[i][j] == 1) {
					int[][] detected1sGroup = new int[this.drawedImgMatrix.length][this.drawedImgMatrix[i].length];
					
					doFill(this.drawedImgMatrix, i, j, detected1sGroup);
					FreemanChainCode fcc = new FreemanChainCode(detected1sGroup);
					fcc.computeFCC();
					fcc.getBoundedBufferedImg(true, false);
					detectedComponenets.add(fcc);
					count++;
					if(count > 2)
						throw new CoreException(MTYPE.ERROR, "Maximum 2 separate segment can be drawed");
				}
			}
		}
	}

	/**
	 * @return
	 * @throws Exception
	 */
	private int[][] getBoundedImgMatrix() throws Exception{
		
		int[][] boundedImgMatrix = new int[this.drawedImgMatrix.length][this.drawedImgMatrix[0].length];
		
		for (int i = 0; i < this.detectedComponenets.size(); i++) {
			int[][] compBoundedImgMatrix = this.detectedComponenets.get(i).getBoundedImgMatrix((this.detectedComponenets.size() == 1), false);
			
			if(this.detectedComponenets.size() > 1)
				for (int y = 0; y < compBoundedImgMatrix.length; y++) {
					for (int x = 0; x < compBoundedImgMatrix[y].length; x++) {
						if(boundedImgMatrix[y][x] == Consts.NO_PIXEL) // if the pixel on the merged matrix is background (NO_PIXEL == 0, then update it)
							boundedImgMatrix[y][x] = compBoundedImgMatrix[y][x];
					}
				}
			else
				boundedImgMatrix = compBoundedImgMatrix;
		}		
		return boundedImgMatrix;
	}
	
	/**
	 * @return
	 * @throws Exception
	 */
	public String getCounteredBase64ImgStr() throws Exception{
		
		int[][] bMatrix = getBoundedImgMatrix();
		
		BufferedImage chainImg = new BufferedImage(bMatrix.length, bMatrix[0].length, BufferedImage.TYPE_INT_RGB);
	    for (int y = 0; y < bMatrix.length; y++) {
	        for (int x = 0; x < bMatrix[0].length; x++) {
	            int pixel=bMatrix[y][x];
	            if(pixel == 0 )
	            	pixel = 255;
	            else
	            	pixel = 0;
	            chainImg.setRGB(x, y, new Color(255, pixel, pixel).getRGB());
	        }
	    }
	    return Util.encode(Util.bufferedImageToByteArray(chainImg));
	}
	
	public static void doFill(int[][] matrix, int row, int col, int[][] gelen) {
		if (row < 0 || col < 0 || row >= matrix.length || col >=matrix[0].length || matrix[row][col] == 0) {
			return;
		}
		
		gelen[row][col] = 1;
		matrix[row][col] = 0;
		
		for (int i=0; i<Consts.dirRow.length; i++) {
			doFill(matrix, row + Consts.dirRow[i], col + Consts.dirCol[i], gelen);
		}
	}

	
	public String getFreemanCode() throws CoreException{
		if(detectedComponenets.size()>1)
			throw new CoreException(MTYPE.ERROR, "Only one component draw can be inserted to Database for now");
		
		return this.detectedComponenets.get(0).getFreemanCode().toString();
	}
	
	public BufferedImage getReduceDrawedImgSize(){
		return Thumbnailator.createThumbnail(this.getDrawedImg(), 100, 100);
	}

	public BufferedImage getDrawedImg() {
		return this.drawedImg;
	}

	public int[][] getDrawedImgMatrix() {
		return this.drawedImgMatrix;
	}
	public List<FreemanChainCode> getDetectedComponenets() {
		return this.detectedComponenets;
	}

}
