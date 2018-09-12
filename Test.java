import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.*;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Stack;

import javax.swing.tree.TreeNode;

public class Test {

	int val;
	Test left, right;

	static Test newNode(int num) {

		Test temp = new Test();
		temp.val = num;
		temp.left = null;
		temp.right = null;
		return temp;

	}


	static void inOrder(Test root) {

		if (root == null)
			return;

		inOrder(root.left);
		System.out.println(root.val);

		inOrder(root.right);

	}
	public Test trimBST(Test root, int L, int R) {
        if (root == null) return null;
        
        if (root.val < L) return trimBST(root.right, L, R);
        if (root.val > R) return trimBST(root.left, L, R);
        
        root.left = trimBST(root.left, L, R);
        root.right = trimBST(root.right, L, R);
        
        return root;
    }
	
	
	
	public static void main(String[] args) {
		
		Test root1 = newNode(3);	
		root1.left = newNode(1);
		root1.left.left = newNode(0);
		root1.left.right = newNode(2);
		root1.right = newNode(5);
		Test obj = new Test();
		Test rootFinal = obj.trimBST(root1, 2, 5);
		inOrder(rootFinal);
		
	}
}
