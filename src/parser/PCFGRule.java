package parser;

/**
 * Une instance de PCFGRule représente une règle d'une grammaire CNF<br>
 * à chacune des règles est assigné un poids, en vu d'un traitement probabiliste de cette grammaire 
 * @author antoine,misun,xin
 */
public class PCFGRule implements Comparable<PCFGRule> {

	public String non_terminal, rhr1; // non-terminal, élément de gauche de la règle binaire
	public String rhr2 = null; // élément de droite de la règle binaire (null si on a une règle lexicale)

	public double poids = 1.; // le poids de la règle

	/**
	 * Constructeur d'une règle si on veut une règle terminale
	 * @param non_terminal
	 * @param terminal
	 */
	public PCFGRule(String non_terminal, String terminal){
		this.non_terminal = non_terminal;
		this.rhr1 = terminal;
	}

	/**
	 * Constructeur d'une règle si on veut une règle avec deux non-terminaux
	 * @param non_terminal
	 * @param left la partie gauche de la partie droite de règle
	 * @param right la partie droite de la partie droite de règle
	 */
	public PCFGRule(String non_terminal,String left,String right){
		this(non_terminal, left);
		this.rhr2 = right;
	}

	public boolean isTerm(){
		return this.rhr2 == null;
	}

	/**
	 * Renvoie la partie droite de la règle courante
	 * @return la partie droite de la règle
	 */
	public String getLHR(){
		return this.non_terminal;
	}

	public double getPoids(){
		return this.poids;
	}

	public String toString(){
		if (this.isTerm())
			return this.non_terminal+" --> "+this.rhr1;
		return this.non_terminal+" --> "+this.rhr1+" "+this.rhr2;
	}

	public String toExport(){
		if (this.isTerm())
			return this.non_terminal+"\t"+this.rhr1+"\t"+Math.exp(this.poids);
		return this.non_terminal+"\t"+this.rhr1+"\t"+this.rhr2+"\t"+Math.exp(this.poids);
	}
	
	/**
	 * Indique si une règle est égale à une autre
	 * @param rule
	 * @return true si oui, non sinon
	 */
	public boolean equals(Object rule){
		if (rule instanceof PCFGRule){
		if (this.isTerm() && ((PCFGRule)rule).isTerm())
			return (this.non_terminal.equals(((PCFGRule)rule).non_terminal)) && this.rhr1.equals(((PCFGRule)rule).rhr1);
		else if (!this.isTerm() && !((PCFGRule)rule).isTerm())
			return (this.non_terminal.equals(((PCFGRule)rule).non_terminal)) && this.rhr1.equals(((PCFGRule)rule).rhr1) && this.rhr2.equals((((PCFGRule)rule).rhr2));
		}
		return false;
	}
	
	public int hashCode(){
		return this.toExport().hashCode();
	}
	
	public boolean hasRHR(String r1){
		if (!this.isTerm()) return false;
		return this.rhr1.equals(r1);
	}
	
	public int compareTo(PCFGRule rule) {
		return this.toString().compareTo(rule.toString());
	}
}