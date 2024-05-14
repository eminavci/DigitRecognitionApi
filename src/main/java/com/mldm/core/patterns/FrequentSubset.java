package com.mldm.core.patterns;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mldm.core.utl.Consts;
import com.mldm.core.utl.FccData;
import com.mldm.core.utl.KEY;
import com.mldm.core.utl.Util;

/**
 * MAİN RULE; if a set is frequent all of its subsets are also frequent.
 * 
 * @author avci
 *
 */
public class FrequentSubset {

	private static Logger logger = LoggerFactory.getLogger(FrequentSubset.class);
	private List<String> dataSet;
	private TreeNode<NodeData> root;
	private final long minSupport;
	private final int freqItemMinLength; // just for boosting the tree search
	private final int lblClass;
	private List<FccData> frequentList; 
	private List<Character> items = new ArrayList<>(Arrays.asList('0', '1', '2', '3', '4', '5', '6', '7'));
	
	
	
	public FrequentSubset(List<String> dataSet, long minSupport, int freqItemMinLength, int lblClass) {
		super();
		this.dataSet = dataSet;
		this.minSupport = minSupport-1;
		this.freqItemMinLength = freqItemMinLength;
		this.lblClass = lblClass;
		this.root = new TreeNode<NodeData>(null); // Root should be null
		int indx = 0;
		for (Character ch : items ) {
			root.addChild(new NodeData(ch, 10000, 1));
			candidateGen(root.getChildren().get(indx));
			indx++;
		}
		this.frequentList = new ArrayList<FccData>();
	}
	
	private void candidateGen(TreeNode<NodeData> node) {
		
		for (Character ch : items ) {
			//System.out.println(Integer.valueOf(ch+"") + "   " + Integer.valueOf(ch) + " AAA : " + ch + " " + node.getData().digit);
			if(Math.abs(Character.getNumericValue(ch) - Character.getNumericValue(node.getData().digit))%4 != 0 ||  node.getData().digit == ch){
				String candidateStr = genSubStringUntilRoot(node, "") + ch;
				long supCount = computeSupportCount(candidateStr);
				if(supCount > this.minSupport){
					TreeNode<NodeData> child = node.addChild(new NodeData(ch, supCount, node.getData().depth+1));
					candidateGen(child);
					//System.out.println(child.getData().depth + " ~ " + supCount + " ## " + candidateStr);
				}
			}
		}
	}
	
	private String genSubStringUntilRoot(TreeNode<NodeData> node, String s){
		//System.out.println("aFFF : " + s);
		if(node.getParent() != null){
			return genSubStringUntilRoot(node.getParent(), node.getData().digit + s);
		} else
			return s;
		
	}
	
	private long computeSupportCount(String canditate){
		long supCount = 0l;
		for (String str : dataSet) {
			int lastIndex = 0;
		    int count = 0;
		    while ((lastIndex = str.indexOf(canditate, lastIndex)) != -1) {
		        count++;
		        lastIndex += canditate.length() - 1;
		    }
		    supCount = supCount + count;
		}
		return supCount;
	}

	/** No need to check support here sinc the tree built by considering only frequent subsets
	 * @param node
	 * @return
	 */
	public TreeNode<NodeData> computeFrequentSubsequents(TreeNode<NodeData> node){
		if(node.getChildren().size()>0){
			
			for (TreeNode<NodeData> treeNode : node.getChildren()) {
				TreeNode<NodeData> dn = computeFrequentSubsequents(treeNode);
				
				if(dn.getData().depth > this.freqItemMinLength){
					String s = "";
					s = genSubStringUntilRoot(dn,s);
					FccData frqData = new FccData();
					frqData.put(KEY.lblClass.name(), this.lblClass);
					frqData.put(KEY.support.name(), dn.getData().count);
					frqData.put(KEY.fcc, s);
					frqData.put(KEY.length, dn.getData().depth);
					this.frequentList.add(frqData);
				}
			}
		}
		return node;
	}
	
	public void optimizeFreqSubsRes(){
		List<FccData> optFreqItems = new ArrayList<>();
		for (FccData fqi : frequentList) {
			boolean found = false;
			for (FccData freqItem : frequentList) {
				if(fqi != freqItem && freqItem.getString(KEY.fcc.name()).indexOf(fqi.getString(KEY.fcc.name())) != -1){
					found = true;
					break;
				}
			}
			if(!found)
				optFreqItems.add(fqi);
		}
		this.frequentList = optFreqItems;
	}
	
	public void calculateImg(){
		
		Iterator<FccData> fccsIter = this.frequentList.iterator();
		while(fccsIter.hasNext()){
			FccData fcc = fccsIter.next();

			int xSize = 0;
			int ySize = 0;
			for(char c : fcc.getString(KEY.fcc.name()).toCharArray()){
				int cxc = Character.getNumericValue(c);
				xSize += Consts.dirCol[cxc];  
				ySize += Consts.dirRow[cxc]; 
			}	
	
			int xStart = (50 - (xSize/2));
			int yStart = (50 - (ySize/2));
			int imgMatrix[][] = new int[100][100];
			for(char c : fcc.getString(KEY.fcc.name()).toCharArray()){
				int cxc = Character.getNumericValue(c);
				imgMatrix[yStart][xStart] = 1;
				xStart = xStart + Consts.dirCol[cxc];
				yStart = yStart + Consts.dirRow[cxc];
			}
			BufferedImage bufImg = new BufferedImage(imgMatrix.length, imgMatrix[0].length, BufferedImage.TYPE_INT_RGB);
			for (int j = 0; j < imgMatrix.length; j++) {
				for (int j2 = 0; j2 < imgMatrix[j].length; j2++) {
					 int pixel=imgMatrix[j][j2];
			            if(pixel == 0 )
			            	bufImg.setRGB(j2, j, new Color(255, 255, 255).getRGB());
			            else
			            	bufImg.setRGB(j2, j, new Color(0, 0, 0).getRGB());
			            
				}
			}
			String imgStr = "";
			try {
				imgStr = Util.encode(Util.bufferedImageToByteArray(bufImg));
			} catch (IOException e) {
				logger.error("Frequent Subsets image error : " + e);
				fccsIter.remove();
				continue;
			}
			//ImageIO.write(bufImg, "png", new File("/home/avci/Desktop/imgsim/hellow_"+i+".png"));
			
			fcc.put(KEY.counteredBase64ImgStr, imgStr);
		}
	}



	public TreeNode<NodeData> getRoot() {
		return root;
	}

	public List<FccData> getFrequentList() {
		return frequentList;
	}
}
