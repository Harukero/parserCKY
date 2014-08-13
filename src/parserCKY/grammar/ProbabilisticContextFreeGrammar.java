package parserCKY.grammar;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import parserCKY.Utils;
import parserCKY.paires.NonTerminalElementToProbability;
import parserCKY.tree.Tree;
import parserCKY.treebank.Treebank;

/**
 * Une instance de PCFG est une représentation d'une grammaire sous forme CNF<br>
 * Elle dispose d'un collection de règles sous forme quadratique auquelles on
 * assigne un poids<br>
 * selon le nombre de fois où chacune d'entre elles est observée au moment de
 * l'importation de la grammaire
 * 
 * @author antoine,misun,xin
 *
 */
public class ProbabilisticContextFreeGrammar {
	// liste de toutes les règles non lexicales de la grammaire
	private List<ProbabilisticContextFreeGrammarRule> binaryRules = new ArrayList<ProbabilisticContextFreeGrammarRule>();
	// liste de toutes les règles lexicales de la grammaire
	private List<ProbabilisticContextFreeGrammarRule> lexicalRules = new ArrayList<ProbabilisticContextFreeGrammarRule>();
	// une HashMap qui à chaque partie gauche lui associe le nombre de fois où elle a été vue
	// { NON-TERMINAL : nb de fois vu }
	private Map<String, Double> countsNonTerminalElements = new HashMap<String, Double>();
	// l'axiome de la grammaire
	private String axiome = null;
	// permet d'encoder les non-terminaux sur des nombres
	private List<String> nonTermnalElementPosition = new ArrayList<String>();
	// une matrice à deux dimensions contenant des tableaux de PaireLPoids
	// pour deux nt encodés par des nombres, on a l'ensemble des NT les
	// produisants, avec la proba de cet évènement
	private NonTerminalElementToProbability[][][] lookUpMatrice;
	private Map<String, List<NonTerminalElementToProbability>> lexicalLookUpMatrix = new HashMap<String, List<NonTerminalElementToProbability>>();
	private Map<String, Set<NonTerminalElementToProbability>> lexicalSuffixesLookUpMatrix = new HashMap<String, Set<NonTerminalElementToProbability>>();

	private double lexicalTokens = 0;
	private double vocabularySize = 0;
	private int nonTermnalsNumber = 0;
	
	public String getAxiome() {
		return axiome;
	}

	public void setAxiome(String axiome) {
		this.axiome = axiome;
	}

	
	public List<String> getNtPos() {
		return nonTermnalElementPosition;
	}

	public void setNtPos(List<String> ntPos) {
		this.nonTermnalElementPosition = ntPos;
	}

	/**
	 * Constructeur d'une CNFGrammWithProbs. Si le paramètre n'est pas binarisé,
	 * on le binarise de force
	 * 
	 * @param trebank
	 *            une instance de TreeBank
	 */
	public ProbabilisticContextFreeGrammar(Treebank trebank) {
		trebank.binariseTreeBank();
		for (Tree tree : trebank) {
			fillGrammar(tree);
		}
		System.out.println("Grammaire importée.");
		nonTermnalsNumber = 0;
		for (String nonTerminal : countsNonTerminalElements.keySet()) {
			nonTermnalElementPosition.add(nonTerminal);
			nonTermnalsNumber++;
		}
		Collections.sort(binaryRules); // tri des règles binaires NP -> Det N
		Collections.sort(lexicalRules); // tri des règles pour le lexique Det -> le
		filterAndCounts();
		System.out.println("Grammaire terminée.\nParsing désormais possible.");
	}

	/**
	 * Méthode privée qui permet de ne conserver chaque règles qu'une seule fois
	 * tout en mettant à jour les comptes. Cette méthode doit être appliquée
	 * aprés que gramm et lexicalRules aient été triés.
	 */
	private void filterAndCounts() {
		filterBinRules();
		System.out.println(nonTermnalsNumber
				+ " éléments non-terminaux dans la grammaire.");
		buildMatrice();
		System.out.println("Table de correspondance créée.");
		filterLexRules();
		System.out.println(vocabularySize
				+ " éléments terminaux dans la grammaire.");
		System.out.println(lexicalTokens
				+ " occurences dans votre lexique.");
		smooth(1E-05);
		System.out.println("Lissage effectué.");
		// this.exportGramm("binrules", "lexrules");
	}

	private void smooth(double lambda) {
		Set<NonTerminalElementToProbability> digits = new HashSet<NonTerminalElementToProbability>();
		Set<NonTerminalElementToProbability> rares = new HashSet<NonTerminalElementToProbability>();
		for (ProbabilisticContextFreeGrammarRule lexicalRule : lexicalRules) {
			double probLambda = Math
					.log(lambda
							/ (this.countsNonTerminalElements
									.get(lexicalRule.nonTerminal) + (this.vocabularySize * lambda)));
			if (lexicalRule.poids <= 3) {
				rares.add(new NonTerminalElementToProbability(
						nonTermnalElementPosition.indexOf(lexicalRule.nonTerminal),
						probLambda));
			}
			lexicalRule.poids = Math
					.log((lexicalRule.poids + lambda)
							/ (countsNonTerminalElements
									.get(lexicalRule.nonTerminal) + (vocabularySize * lambda)));
			if (!lexicalLookUpMatrix.containsKey(lexicalRule.rhr1)) {
				lexicalLookUpMatrix.put(lexicalRule.rhr1,
						new ArrayList<NonTerminalElementToProbability>());
			}
			lexicalLookUpMatrix.get(lexicalRule.rhr1).add(
					new NonTerminalElementToProbability(
							nonTermnalElementPosition
									.indexOf(lexicalRule.nonTerminal), lexicalRule.poids));
			int taille = lexicalRule.rhr1.length();
			if (taille >= 3) {
				String suff = lexicalRule.rhr1.substring(taille - 3, taille)
						.toLowerCase();
				if (!lexicalSuffixesLookUpMatrix.containsKey(suff)) {
					lexicalSuffixesLookUpMatrix.put(suff,
							new HashSet<NonTerminalElementToProbability>());
				}
				lexicalSuffixesLookUpMatrix.get(suff).add(
						new NonTerminalElementToProbability(
								nonTermnalElementPosition
										.indexOf(lexicalRule.nonTerminal), probLambda));
			}
			if (Character.isDigit(lexicalRule.rhr1.charAt(0))) {
				digits.add(new NonTerminalElementToProbability(
						nonTermnalElementPosition.indexOf(lexicalRule.nonTerminal),
						probLambda));
			}
		}
		lexicalSuffixesLookUpMatrix.put("digits", digits);
		lexicalSuffixesLookUpMatrix.put("dummies", rares);
	}

	private void filterLexRules() {
		// on va compter et filtrer ici les règles lexicales
		ArrayList<ProbabilisticContextFreeGrammarRule> filteredLexique = new ArrayList<ProbabilisticContextFreeGrammarRule>();
		for (int i = 0; i < lexicalTokens - 1; i++) {
			ProbabilisticContextFreeGrammarRule currentRule = lexicalRules.get(i);
			ProbabilisticContextFreeGrammarRule nextRule = lexicalRules
					.get(i + 1);
			if (currentRule.equals(nextRule))
				nextRule.poids = 1 + currentRule.getPoids();
			else {
				filteredLexique.add(currentRule);
				vocabularySize += 1;
			}
			if (Math.abs(i + 1 - lexicalTokens - 1) == 0) {
				filteredLexique.add(nextRule);
				vocabularySize += 1;
			}
		}
		// ici on fait le smoothing
		lexicalRules = filteredLexique;
	}

	// ici on filtre les règles binaires et on met en place les probabilités
	// pour chacunes de ces règles
	private void filterBinRules() {
		ArrayList<ProbabilisticContextFreeGrammarRule> filteredGramm = new ArrayList<ProbabilisticContextFreeGrammarRule>();
		for (int i = 0; i < binaryRules.size() - 1; i++) {
			ProbabilisticContextFreeGrammarRule currentRule = binaryRules.get(i); 
			ProbabilisticContextFreeGrammarRule nextRule = binaryRules
					.get(i + 1); // la règle suivante
			if (currentRule.equals(nextRule)) {
				nextRule.poids = 1.0 + currentRule.poids; // on met à jour le poids (nb de
			} else {
				currentRule.poids = Math.log(currentRule.poids
						/ countsNonTerminalElements.get(currentRule.nonTerminal)); 
				// sinon,  on calcule la probabilité de la règle
				filteredGramm.add(currentRule); // et on l'ajoute à notre grammaire
										// filtrée
			}
			if (i + 1 == binaryRules.size() - 1) {
				nextRule.poids = Math.log(nextRule.poids
						/ countsNonTerminalElements
								.get(nextRule.nonTerminal)); // sinon, on calcule
															// la probabilité de
															// la règle
				filteredGramm.add(nextRule);
			}
		}
		this.binaryRules = filteredGramm; // notre set de règle devient cet
											// ensemble filtré
	}

	// à partir d'ici on va créer notre lookup matrice où à chaque paire de non
	// terminaux correspond l'ensemble des
	// non-terminaux qui peuvent les produire ensemble
	private void buildMatrice() {
		int nt_size = nonTermnalsNumber;
		lookUpMatrice = new NonTerminalElementToProbability[nt_size][][];
		for (int i = 0; i < nt_size; i++) {
			lookUpMatrice[i] = new NonTerminalElementToProbability[nt_size][];
			for (int j = 0; j < nt_size; j++) {
				lookUpMatrice[i][j] = new NonTerminalElementToProbability[0];
			}
		}
		for (ProbabilisticContextFreeGrammarRule rule : binaryRules) {
			fillMatrix(rule);
		}
	}

	private void fillMatrix(ProbabilisticContextFreeGrammarRule rule) {
		int r1 = nonTermnalElementPosition.indexOf(rule.rhr1);
		int r2 = nonTermnalElementPosition.indexOf(rule.rhr2);
		NonTerminalElementToProbability premierePaire = new NonTerminalElementToProbability(
				nonTermnalElementPosition.indexOf(rule.nonTerminal),
				rule.poids);
		NonTerminalElementToProbability[] toAdd = { premierePaire };
		if (lookUpMatrice.length == 0) {
			lookUpMatrice[r1][r2] = toAdd;
		} else {
			lookUpMatrice[r1][r2] = concat(
					lookUpMatrice[r1][r2], toAdd);
		}
	}

	// Permet la concaténation de deux tableaux de PaireLPoids
	private NonTerminalElementToProbability[] concat(
			NonTerminalElementToProbability[] add1,
			NonTerminalElementToProbability[] add2) {
		NonTerminalElementToProbability[] toReturn = new NonTerminalElementToProbability[add1.length + add2.length];
		System.arraycopy(add1, 0, toReturn, 0, add1.length);
		System.arraycopy(add2, 0, toReturn, add1.length, add2.length);
		return toReturn;
	}

	/**
	 * Méthode privée de remplissage de notre grammaire à partir d'un arbre
	 * 
	 * @param tree
	 *            l'arbre doit être binarisé
	 */
	private void fillGrammar(Tree tree) {
		if (tree.getLabel().equals("")) {
			for (Tree child : tree.getChildren()) {
				fillGrammar(child);
			}
		} else {
			if (axiome == null) { // mise en place de l'axiome
				axiome = tree.getLabel();
			}
			if (tree.isLeaf()) { // règle lexicale !
				fillLexicalRule(tree);
			} else { // règle binaire
				fillBinaryRule(tree);
			}
		}
	}

	private void fillBinaryRule(Tree t) {
		String elem1 = Utils.getFirstPart(t.getLabelFromChildAt(0));
		String elem2 = Utils.getFirstPart(t.getLabelFromChildAt(1));
		binaryRules.add(new ProbabilisticContextFreeGrammarRule(t.getLabel(), elem1, elem2));
		updateCountsNonTerm(t.getLabel());
		t.getChildren().forEach(tree -> fillGrammar(tree));
	}

	private void fillLexicalRule(Tree t) {
		String[] ntAndLex = t.getLabel().split(" ");
		ProbabilisticContextFreeGrammarRule maregle = new ProbabilisticContextFreeGrammarRule(ntAndLex[0], ntAndLex[1]);
		lexicalRules.add(maregle); // ajout de la règle lexicale
		lexicalTokens++;
		updateCountsNonTerm(ntAndLex[0]); // mise à jour des comptes
	}

	// méthode privée qui met à jour les comptes des non-terminaux de la
	// grammaire
	private void updateCountsNonTerm(String nt) {
		if (countsNonTerminalElements.containsKey(nt)) {
			countsNonTerminalElements.put(nt, new Double(countsNonTerminalElements.get(nt) + 1.)); 
			// dico[nt] += 1
		} else {
			countsNonTerminalElements.put(nt, new Double(1.)); 
			// dico[nt] = 1
		}
	}

	public String toString() {
		StringBuffer toReturn = new StringBuffer();
		toReturn.append(binaryRules.stream().map(rule -> rule.toString()).collect(Collectors.joining("\n")));
		toReturn.append(lexicalRules.stream().map(rule -> rule.toString()).collect(Collectors.joining("\n")));
		return toReturn.toString();
	}

	/**
	 * Méthode permettant d'exporter l'instance courante de notre grammaire dans
	 * un fichier
	 * 
	 * @param string
	 */
	public void exportGramm(String binaryRulesFilePath,
			String lexicalRulesFilePath) {
		try {
			FileWriter fw = new FileWriter(binaryRulesFilePath, true);
			BufferedWriter output = new BufferedWriter(fw);
			output.write(axiome + "\n");
			for (ProbabilisticContextFreeGrammarRule t : binaryRules) {
				output.write(t.toExport() + "\n");
			}
			output.flush();
			output.close();
			output = new BufferedWriter(new FileWriter(lexicalRulesFilePath,
					true));
			for (ProbabilisticContextFreeGrammarRule rule : lexicalRules) {
				output.write(rule.toExport() + "\n");
			}
			output.flush();
			output.close();
			System.out.println("fichier créé");
		} catch (IOException ioe) {
			System.out.print("Erreur : ");
			ioe.printStackTrace();
		}
	}

	/**
	 * Pour tout couple r1 r2 renvoie la liste des éléments LHR tels que <br>
	 * LHR --> r1 r2 est une règle de la grammaire et la probabilité associée à
	 * cette règle <br>
	 * sous forme de paires
	 * 
	 * @param r1
	 * @param r2
	 * @return le tableau des PairesLPoids
	 */
	public NonTerminalElementToProbability[] lookUp(int r1, int r2) { 
		// lookup pour les règles binaires
		return this.lookUpMatrice[r1][r2];
	}

	public NonTerminalElementToProbability[] lookUp(String r1) { 
		// lookup pour les règles lexicales
		List<NonTerminalElementToProbability> toReturn;
		if (lexicalLookUpMatrix.containsKey(r1)) {
			toReturn = lexicalLookUpMatrix.get(r1);
			return toReturn
					.toArray(new NonTerminalElementToProbability[toReturn
							.size()]);
		}
		Set<NonTerminalElementToProbability> toReturnSuff = new HashSet<NonTerminalElementToProbability>();
		int size = r1.length();
		if (size >= 3) {
			toReturnSuff = returnSuffixes(size, toReturnSuff, r1);
		}
		if (toReturnSuff.size() == 0) {
			toReturnSuff = lexicalSuffixesLookUpMatrix.get("dummies");
		}
		return toReturnSuff
				.toArray(new NonTerminalElementToProbability[toReturnSuff
						.size()]);
	}

	private Set<NonTerminalElementToProbability> returnSuffixes(int size,
			Set<NonTerminalElementToProbability> toReturnSuff, String r1) {
		String suffixe = r1.substring(size - 3, size).toLowerCase();
		if (toReturnSuff.size() == 0
				&& lexicalSuffixesLookUpMatrix.containsKey(suffixe)) {
			lexicalSuffixesLookUpMatrix.get(suffixe);
		}
		if (toReturnSuff.size() == 0 && Character.isDigit(r1.charAt(size - 1))) {
			toReturnSuff = lexicalSuffixesLookUpMatrix.get("digits");
		}
		return toReturnSuff;
	}
}