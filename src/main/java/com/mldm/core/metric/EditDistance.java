package com.mldm.core.metric;

public class EditDistance extends EDBase{

	
	@Override
	public long distance(String testStr, String trainStr) {
		long  distance[][] = null;
		int n=testStr.length()+1;
		int m=trainStr.length()+1;
		
		// initializing  matrix keeping distances
		if(distance == null){
			distance= new long[n][m];	
			for(int i=0;i<n;i++)
				distance[i][0]=i;
			
			for(int j=0;j<m;j++)
				distance[0][j]=j;
		}
		
		long delete =0;
		long insert =0;
		long subtitute = 0;
		char car1;
		char car2;
		long min=0;
		
		for(int i=1; i<n;i++){
			for(int j=1;j<m;j++){
				
				car1=testStr.charAt(i-1);
				car2=trainStr.charAt(j-1);
				
				int dirCost = directionalCost(Integer.valueOf(car1), Integer.valueOf(car2));
				
				delete = distance[i-1][j] + deleteCost * dirCost;
				insert = distance[i][j-1] + insertCost * dirCost;
				
				if(car1!=car2){
					subtitute = distance[i-1][j-1] + replaceCost * dirCost;
				}
				else{
					subtitute= distance[i-1][j-1];
				}
				
				min=0;
				if(delete<insert){
					min=delete;
				}
				else{
					min=insert;
				}
				if(subtitute<min){
					min=subtitute;
				}
				
				distance[i][j]=min;
			}
		}
//		for(int i=0; i<n;i++){
//			for(int j=0;j<m;j++){
//				System.out.print(distance[i][j] + " ");
//			}
//			System.out.println("");
//		}

		return distance[n-1][m-1];
	}
	
/*	public static void main(String[] args) {
		EditDistance ed = new EditDistance();
		System.out.println(ed.distance("001122", "115566"));
	}*/
	
}
