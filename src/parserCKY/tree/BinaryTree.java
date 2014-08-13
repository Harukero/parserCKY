package parserCKY.tree;

public class BinaryTree<T> {

	private T label;

	private BinaryTree<T> father;
	private BinaryTree<T> left;
	private BinaryTree<T> right;

	public BinaryTree(T currentLabel) {
		this(currentLabel, null);
	}

	public BinaryTree(T currentLabel, BinaryTree<T> currentFather) {
		this(currentLabel, currentFather, null);
	}

	public BinaryTree(T currentLabel, BinaryTree<T> currentFather, BinaryTree<T> leftChild) {
		this(currentLabel, currentFather, leftChild, null);
	}

	public BinaryTree(T currentLabel, BinaryTree<T> currentFather, BinaryTree<T> leftChild, BinaryTree<T> rightChild) {
		label = currentLabel;
		father = currentFather;
		left = leftChild;
		right = rightChild;
	}

	T getLabel() {
		return label;
	}

	public BinaryTree<T> getFather() {
		return father;
	}

	public void setFather(BinaryTree<T> father) {
		this.father = father;
	}

	public BinaryTree<T> getLeft() {
		return left;
	}

	public void setLeft(BinaryTree<T> left) {
		this.left = left;
	}

	public BinaryTree<T> getRight() {
		return right;
	}

	public void setRight(BinaryTree<T> right) {
		this.right = right;
	}

	public void setLabel(T label) {
		this.label = label;
	}

	public boolean isRoot() {
		return father == null;
	}

	public boolean isLeaf() {
		return left == null && right == null;
	}

	public BinaryTree<T> cutLeft() {
		BinaryTree<T> elementCut = left;
		left = null;
		return elementCut;
	}

	public BinaryTree<T> cutRight() {
		BinaryTree<T> elementCut = right;
		right = null;
		return elementCut;
	}

	public String toRepresentation() {
		if (isLeaf()) {
			return "(" + label.toString() + ")";
		}
		return "(" + label.toString() + " " + left.toRepresentation() + " " + right.toRepresentation() + ")";
	}

}
