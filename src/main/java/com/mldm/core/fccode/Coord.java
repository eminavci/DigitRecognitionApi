package com.mldm.core.fccode;

/**
 * @author emin
 *
 */
public class Coord {
	int x;
	int y;
	public Coord(int x, int y) {
		this.x = x;
		this.y = y;
	}
	public int getX() {
		return x;
	}
	public int getY() {
		return y;
	}
	@Override
	public String toString() {
		return "[" + x + "," + y + "]";
	}
	
	public boolean isOntheLeft(Coord c){
		
		return false;
	}
	
	public boolean isOntheRight(Coord c){
		
		return false;
	}
	
}
