package parserCKY.paires;

import parserCKY.tree.Tree;

public class TupleNTTree  implements Paire<Integer, Tree>, Comparable<TupleNTTree>{

	private Integer fstElt;
	private Tree sndElt;
	
	public TupleNTTree(Integer nt,Tree tree){
		this.fstElt=nt;
		this.sndElt=tree;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((this.fstElt == null) ? 0 : this.fstElt.hashCode());
		result = prime * result
				+ ((this.sndElt == null) ? 0 : this.sndElt.hashCode());
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
		if (!(obj instanceof TupleNTTree))
			return false;
		TupleNTTree other = (TupleNTTree) obj;
		if (this.fstElt == null) {
			if (other.fstElt != null)
				return false;
		} else if (!this.fstElt.equals(other.fstElt))
			return false;
		if (this.sndElt == null) {
			if (other.sndElt != null)
				return false;
		} else if (!this.sndElt.equals(other.sndElt))
			return false;
		return true;
	}

	public Integer getL() {
		return this.fstElt;
	}

	public Tree getR() {
		return this.sndElt;
	}

	public String toString(){
		return "("+this.fstElt+";"+this.sndElt+")";
	}

	public int compareTo(TupleNTTree arg0) {
		return (this.toString()).compareTo(arg0.toString());
	}
	
}
