package com.mldm.core.patterns;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class TreeNode<T extends Comparable<T>> implements Iterable<TreeNode<T>>, Comparable<T> {
	 T data;
	 TreeNode<T> parent;
	 List<TreeNode<T>> children;
	 
	 public TreeNode(T data) {
        this.data = data;
        this.children = new LinkedList<TreeNode<T>>();
    }

    public TreeNode<T> addChild(T child) {
        TreeNode<T> childNode = new TreeNode<T>(child);
        childNode.parent = this;
        this.children.add(childNode);
        return childNode;
    }
	 
	@Override
	public Iterator<TreeNode<T>> iterator() {
		return this.children.iterator();
	}

	public T getData() {
		return data;
	}
	public TreeNode<T> getParent() {
		return parent;
	}
	public List<TreeNode<T>> getChildren() {
		return children;
	}

	@Override
	public int compareTo(T o) {
		System.out.println("***************** \n ***************** \n");
		return this.getData().compareTo(o);
	}

	@Override
	public String toString() {
		return (String) this.data;
	}
	
}
