package parserCKY.grammar;

/**
 * Une instance de PCFGRule représente une règle d'une grammaire CNF<br>
 * à chacune des règles est assigné un poids, en vu d'un traitement probabiliste
 * de cette grammaire
 * 
 * @author antoine,misun,xin
 */
public class ProbabilisticContextFreeGrammarRule implements
Comparable<ProbabilisticContextFreeGrammarRule> {

	public String nonTerminal, rhr1; // non-terminal, élément de gauche de la
	// règle binaire
	public String rhr2 = null; // élément de droite de la règle binaire (null si
	// on a une règle lexicale)

	public double poids = 1.; // le poids de la règle

	/**
	 * Constructeur d'une règle si on veut une règle terminale
	 * 
	 * @param non_terminal
	 * @param terminal
	 */
	public ProbabilisticContextFreeGrammarRule(String non_terminal,
			String terminal) {
		nonTerminal = non_terminal;
		rhr1 = terminal;
	}

	/**
	 * Constructeur d'une règle si on veut une règle avec deux non-terminaux
	 * 
	 * @param non_terminal
	 * @param left
	 *            la partie gauche de la partie droite de règle
	 * @param right
	 *            la partie droite de la partie droite de règle
	 */
	public ProbabilisticContextFreeGrammarRule(String non_terminal,
			String left, String right) {
		this(non_terminal, left);
		rhr2 = right;
	}

	public boolean isTerm() {
		return rhr2 == null;
	}

	/**
	 * Renvoie la partie droite de la règle courante
	 * 
	 * @return la partie droite de la règle
	 */
	public String getLHR() {
		return nonTerminal;
	}

	public double getPoids() {
		return poids;
	}

	public String toString() {
		if (isTerm()) {
			return nonTerminal + " --> " + rhr1;
		}
		return nonTerminal + " --> " + rhr1 + " " + rhr2;
	}

	public String toExport() {
		if (isTerm()) {
			return nonTerminal + "\t" + rhr1 + "\t"
					+ Math.exp(poids);
		}
		return nonTerminal + "\t" + rhr1 + "\t" + rhr2 + "\t"
		+ Math.exp(poids);
	}

	/**
	 * Indique si une règle est égale à une autre
	 * 
	 * @param rule
	 * @return true si oui, non sinon
	 */
	public boolean equals(Object rule) {
		if (rule instanceof ProbabilisticContextFreeGrammarRule) {
			if (isTerm()
					&& ((ProbabilisticContextFreeGrammarRule) rule).isTerm()) {
				return (nonTerminal
						.equals(((ProbabilisticContextFreeGrammarRule) rule).nonTerminal))
						&& rhr1
						.equals(((ProbabilisticContextFreeGrammarRule) rule).rhr1);
			} else if (!isTerm()
					&& !((ProbabilisticContextFreeGrammarRule) rule).isTerm()) {
				return (nonTerminal
						.equals(((ProbabilisticContextFreeGrammarRule) rule).nonTerminal))
						&& rhr1
						.equals(((ProbabilisticContextFreeGrammarRule) rule).rhr1)
						&& rhr2
						.equals((((ProbabilisticContextFreeGrammarRule) rule).rhr2));
			}
		}
		return false;
	}

	public int hashCode() {
		return toExport().hashCode();
	}

	public boolean hasRHR(String r1) {
		if (!isTerm()) {
			return false;
		}
		return rhr1.equals(r1);
	}

	public int compareTo(ProbabilisticContextFreeGrammarRule rule) {
		return toString().compareTo(rule.toString());
	}
}