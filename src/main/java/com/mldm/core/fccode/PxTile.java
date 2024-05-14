package com.mldm.core.fccode;

import com.mldm.core.utl.Consts;

/**
 * @author emin
 *
 */
public class PxTile {

	private int fCode;
	private Coord coord;
	public PxTile( int fCode, Coord coord) {
		super();
		this.fCode = fCode;
		this.coord = coord;
	}

	public PxTile getNextBoundPixel(int dir, int[][] pixelMatrix) {
		
		int x_n = 0, y_n = 0;
		int exIndex = 0;
		do {
			
			x_n = this.coord.getX() + Consts.dirCol[dir];
			y_n = this.coord.getY() + Consts.dirRow[dir];
			
			try {
				if(pixelMatrix[y_n][x_n] == Consts.PIXEL){
					return new PxTile(dir, new Coord(x_n, y_n));
				} else {
					throw new Exception("Look other neighbor");
				}
			} catch (Exception e) {
				if(exIndex == 8)
					break;
				exIndex++;
				if(dir == 7)
					dir = 0;
				else
					dir++;
				continue;
			}
		} while (true);
		return null;
	}


	public int getfCode() {
		return fCode;
	}
	public void setfCode(int fCode) {
		this.fCode = fCode;
	}
	public Coord getCoord() {
		return coord;
	}
	public void setCoord(Coord coord) {
		this.coord = coord;
	}

}
