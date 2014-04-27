package parserCKY.paires;

public class NonTerminalElementToProbability  implements Paire<Integer, Double>, Comparable<NonTerminalElementToProbability>{

	private Integer nonTerminalElementId;
	private Double probability;

	public NonTerminalElementToProbability(Integer nt,Double poids){
		this.nonTerminalElementId=nt;
		this.probability=poids;
	}

	public String toString(){
		return "("+this.nonTerminalElementId+";"+this.probability+")";
	}


	public Integer getL() {
		return this.nonTerminalElementId;
	}

	public Double getR() {
		return this.probability;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((this.nonTerminalElementId == null) ? 0 : this.nonTerminalElementId.hashCode());
		result = prime * result
				+ ((this.probability == null) ? 0 : this.probability.hashCode());
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
		if (!(obj instanceof NonTerminalElementToProbability))
			return false;
		NonTerminalElementToProbability other = (NonTerminalElementToProbability) obj;
		if (this.nonTerminalElementId == null) {
			if (other.nonTerminalElementId != null)
				return false;
		} else if (!this.nonTerminalElementId.equals(other.nonTerminalElementId))
			return false;
		if (this.probability == null) {
			if (other.probability != null)
				return false;
		} else if (!this.probability.equals(other.probability))
			return false;
		return true;
	}

	public int compareTo(NonTerminalElementToProbability arg0) {
		return (this.toString()).compareTo(arg0.toString());
	}

}
