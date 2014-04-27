package parser;

public class TupleNTProb  implements Paire<Integer, Double>, Comparable<TupleNTProb>{

	private Integer fstElt;
	private Double sndElt;

	public TupleNTProb(Integer nt,Double poids){
		this.fstElt=nt;
		this.sndElt=poids;
	}

	public String toString(){
		return "("+this.fstElt+";"+this.sndElt+")";
	}


	public Integer getL() {
		return this.fstElt;
	}

	public Double getR() {
		return this.sndElt;
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
		if (!(obj instanceof TupleNTProb))
			return false;
		TupleNTProb other = (TupleNTProb) obj;
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

	public int compareTo(TupleNTProb arg0) {
		return (this.toString()).compareTo(arg0.toString());
	}

}
