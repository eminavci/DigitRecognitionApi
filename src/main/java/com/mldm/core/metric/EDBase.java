package com.mldm.core.metric;

public class EDBase implements Distance{

	public static int replaceCost = 1;
	public static int insertCost = 1;
	public static int deleteCost = 1;
	
	
	
	public long distance(String testStr, String trainStr) {
	
		return 0;
	}
	
	/** dir1 == 8, means deleted,<br>
	 *  dir2 == 8, means substitude
	 * @param dir1
	 * @param dir2
	 * @return
	 */
	public int directionalCost(int dir1, int dir2){
		if(dir1 == 8 || dir2 == 8) // insertion and deletion against empty
			return 1;
	
		return Math.min(Math.abs(dir1-dir2), 8 - Math.abs(dir1-dir2));
	}

}
