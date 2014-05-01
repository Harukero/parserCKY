package parserCKY.paires;

import parserCKY.tree.Tree;

public class NonTerminalElementToTree  implements Paire<Integer, Tree>, Comparable<NonTerminalElementToTree>{

	private Integer nonTerminalElement;
	private Tree tree;
	
	public NonTerminalElementToTree(Integer nt,Tree tree){
		this.nonTerminalElement=nt;
		this.tree=tree;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((this.nonTerminalElement == null) ? 0 : this.nonTerminalElement.hashCode());
		result = prime * result
				+ ((this.tree == null) ? 0 : this.tree.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof NonTerminalElementToTree))
			return false;
		NonTerminalElementToTree other = (NonTerminalElementToTree) obj;
		if (this.nonTerminalElement == null) {
			if (other.nonTerminalElement != null)
				return false;
		} else if (!this.nonTerminalElement.equals(other.nonTerminalElement))
			return false;
		if (this.tree == null) {
			if (other.tree != null)
				return false;
		} else if (!this.tree.equals(other.tree))
			return false;
		return true;
	}

	public Integer getLeftElement() {
		return this.nonTerminalElement;
	}

	public Tree getRightElement() {
		return this.tree;
	}

	public String toString(){
		return "("+this.nonTerminalElement+";"+this.tree+")";
	}

	public int compareTo(NonTerminalElementToTree arg0) {
		return (this.toString()).compareTo(arg0.toString());
	}
	
}
