import java.lang.*;
class Node {
	
	int data;
	Node left;
	Node right;
	
	public Node(int data, Node left, Node right){
		
		this.data = data;
		this.left = left;
		this.right = right;
		
	}
	
	static Node newNode(int data){
		
		return new Node(data, null, null);
	}
	
	static void preorder(Node node){
		if(node == null)
			return;
				
		preorder(node.left);
		
		System.out.println(node.data);
		
		preorder(node.right);		
	}

	static Node MergeTrees(Node t1, Node t2){
		
		if(t1==null)
			return t2;
		if(t2==null)
			return t1;
		t1.data += t2.data;
		t1.left = MergeTrees(t1.left, t2.left);
		t1.right = MergeTrees(t1.right, t2.right);
		return t1;
		
	}
	
	
	public static void main(String[] args){
		
		Node root1 = newNode(1);
		root1.left = newNode(2);
		root1.right = newNode(3);
		root1.left.left = newNode(4);
		root1.left.right = newNode(5);
		root1.right.left = newNode(6);
		
		Node root2 = newNode(2);
		root2.left = newNode(3);
		root2.right = newNode(5);
		root2.left.left = newNode(4);
		root2.left.right = newNode(8);
		root2.right.right = newNode(7);
		
		Node root3 = MergeTrees(root1, root2);
		System.out.println("Below is the tree: ");
		preorder(root3);
		
	}
	
}
