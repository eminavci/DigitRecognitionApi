package com.mldm.core.patterns;

public class NodeData implements Comparable<NodeData>{

	public char digit;
	public long count;
	public int depth;
	public NodeData(char digit, long count, int depth) {
		super();
		this.digit = digit;
		this.count = count;
		this.depth = depth;
	}
	@Override
	public int compareTo(NodeData o) {
		System.out.println("############ \n ##################### \n");
		return new Character(this.digit).compareTo(o.digit);
	}
	@Override
	public String toString() {
		return "[digit=" + digit + ", count=" + count + "]";
	}
	
}
