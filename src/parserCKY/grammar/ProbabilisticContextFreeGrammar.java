package parserCKY.grammar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import parserCKY.IConstants;
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
	private static final String DUMMIES = "dummies";
	private static final String DIGITS = "digits";
	// liste de toutes les règles non lexicales de la grammaire
	private Map<ProbabilisticContextFreeGrammarRule, Double> binaryRules = new HashMap<ProbabilisticContextFreeGrammarRule, Double>();
	// liste de toutes les règles lexicales de la grammaire
	private Map<ProbabilisticContextFreeGrammarRule, Double> lexicalRules = new HashMap<ProbabilisticContextFreeGrammarRule, Double>();
	// une HashMap qui à chaque partie gauche lui associe le nombre de fois où elle a été vue { NON-TERMINAL : nb de fois vu }
	private Map<String, Double> countsNonTerminalElements = new HashMap<String, Double>();
	// l'axiome de la grammaire
	private String axiome = null;
	// permet d'encoder les non-terminaux sur des nombres
	private List<String> nonTermnalElementPosition = new ArrayList<String>();
	// une matrice à deux dimensions contenant des tableaux de PaireLPoids
	// pour deux nt encodés par des nombres, on a l'ensemble des NT les
	// produisants, avec la proba de cet évènement
	private Map<Integer, Map<Integer, Set<NonTerminalElementToProbability>>> lookUpMatrice = new HashMap<Integer, Map<Integer, Set<NonTerminalElementToProbability>>>();
	//	private NonTerminalElementToProbability[][][] lookUpMatrice;
	private Map<String, List<NonTerminalElementToProbability>> lexicalLookUpMatrix = new HashMap<String, List<NonTerminalElementToProbability>>();
	private Map<String, Set<NonTerminalElementToProbability>> lexicalSuffixesLookUpMatrix = new HashMap<String, Set<NonTerminalElementToProbability>>();

	private double vocabularySize = 0;
	private int nonTermnalsNumber = 0;

	/**
	 * Constructeur d'une ProbabilisticContextFreeGramm²ar. Si le paramètre n'est pas binarisé, on le binarise de force
	 * 
	 * @param treebank une instance de TreeBank
	 */
	public ProbabilisticContextFreeGrammar(Treebank treebank) {
		treebank.binariseTreeBank();
		treebank.forEach(tree -> fillGrammar(tree));
		System.out.println("Grammaire importée.");
		nonTermnalsNumber = 0;
		countsNonTerminalElements.keySet().forEach(nonTerminal -> {
			nonTermnalElementPosition.add(nonTerminal);
			nonTermnalsNumber++;
		});
		filterAndCounts();
	}

	/*
	 * Méthode privée qui permet de ne conserver chaque règles qu'une seule fois
	 * tout en mettant à jour les comptes. Cette méthode doit être appliquée
	 * aprés que gramm et lexicalRules aient été triés.
	 */
	private void filterAndCounts() {
		filterBinRules();
		buildMatrice();
		filterLexRules();
		smooth(IConstants.SMOOTHING_LAMBDA);
	}

	private void smooth(double lambda) {
		Set<NonTerminalElementToProbability> digits = new HashSet<NonTerminalElementToProbability>();
		Set<NonTerminalElementToProbability> rares = new HashSet<NonTerminalElementToProbability>();
		for (ProbabilisticContextFreeGrammarRule lexicalRule : lexicalRules.keySet()) {
			double probLambda = Math.log(lambda
					/ (countsNonTerminalElements.get(lexicalRule.nonTerminal) + (vocabularySize * lambda)));
			if (lexicalRule.poids <= IConstants.CUT) {
				rares.add(new NonTerminalElementToProbability(nonTermnalElementPosition
						.indexOf(lexicalRule.nonTerminal), probLambda));
			}
			lexicalRule.poids = Math.log((lexicalRule.poids + lambda)
					/ (countsNonTerminalElements.get(lexicalRule.nonTerminal) + (vocabularySize * lambda)));
			if (!lexicalLookUpMatrix.containsKey(lexicalRule.rhr1)) {
				lexicalLookUpMatrix.put(lexicalRule.rhr1, new ArrayList<NonTerminalElementToProbability>());
			}
			lexicalLookUpMatrix.get(lexicalRule.rhr1).add(
					new NonTerminalElementToProbability(nonTermnalElementPosition.indexOf(lexicalRule.nonTerminal),
							lexicalRule.poids));
			int taille = lexicalRule.rhr1.length();
			if (taille >= 3) {
				takeSuffix(lexicalRule, probLambda, taille);
			}
			if (Character.isDigit(lexicalRule.rhr1.charAt(0))) {
				digits.add(new NonTerminalElementToProbability(nonTermnalElementPosition
						.indexOf(lexicalRule.nonTerminal), probLambda));
			}
		}
		lexicalSuffixesLookUpMatrix.put(DIGITS, digits);
		lexicalSuffixesLookUpMatrix.put(DUMMIES, rares);
	}

	private void takeSuffix(ProbabilisticContextFreeGrammarRule lexicalRule, double probLambda, int taille) {
		String suff = lexicalRule.rhr1.substring(taille - 3, taille).toLowerCase();
		if (!lexicalSuffixesLookUpMatrix.containsKey(suff)) {
			lexicalSuffixesLookUpMatrix.put(suff, new HashSet<NonTerminalElementToProbability>());
		}
		lexicalSuffixesLookUpMatrix.get(suff).add(
				new NonTerminalElementToProbability(nonTermnalElementPosition.indexOf(lexicalRule.nonTerminal),
						probLambda));
	}

	private void filterLexRules() {
		lexicalRules.forEach((key, value) -> key.poids = value);
		vocabularySize = lexicalRules.size();
	}

	// ici on filtre les règles binaires et on met en place les probabilités pour chacunes de ces règles
	private void filterBinRules() {
		binaryRules.forEach((key, value) -> key.poids = value / countsNonTerminalElements.get(key.nonTerminal));
	}

	// à partir d'ici on va créer notre lookup matrice où à chaque paire de non
	// terminaux correspond l'ensemble des
	// non-terminaux qui peuvent les produire ensemble
	private void buildMatrice() {
		binaryRules.keySet().forEach(rule -> fillMatrix(rule));
	}

	private void fillMatrix(ProbabilisticContextFreeGrammarRule rule) {
		int r1 = nonTermnalElementPosition.indexOf(rule.rhr1);
		int r2 = nonTermnalElementPosition.indexOf(rule.rhr2);
		NonTerminalElementToProbability premierePaire = new NonTerminalElementToProbability(
				nonTermnalElementPosition.indexOf(rule.nonTerminal), rule.poids);
		lookUpMatrice.putIfAbsent(r1, new HashMap<Integer, Set<NonTerminalElementToProbability>>());
		lookUpMatrice.get(r1).putIfAbsent(r2, new HashSet<NonTerminalElementToProbability>());
		lookUpMatrice.get(r1).get(r2).add(premierePaire);
	}

	/**
	 * Méthode privée de remplissage de notre grammaire à partir d'un arbre
	 * 
	 * @param tree l'arbre doit être binarisé
	 */
	private void fillGrammar(Tree tree) {
		if (tree.getLabel().equals("")) {
			tree.getChildren().forEach(child -> fillGrammar(child));
		} else {
			if (axiome == null) { // mise en place de l'axiome
				axiome = tree.getLabel().split("\\*")[0];
			}
			if (tree.isLeaf()) { // règle lexicale
				fillLexicalRule(tree);
			} else { // règle binaire
				fillBinaryRule(tree);
			}
		}
	}

	private void fillBinaryRule(Tree tree) {
		String elem1 = Utils.getFirstPart(tree.getLabelFromChildAt(0));
		String elem2 = Utils.getFirstPart(tree.getLabelFromChildAt(1));
		ProbabilisticContextFreeGrammarRule currentRule = new ProbabilisticContextFreeGrammarRule(tree.getLabel(),
				elem1, elem2);
		updateMap(binaryRules, currentRule);
		updateMap(countsNonTerminalElements, tree.getLabel());
		tree.getChildren().forEach(child -> fillGrammar(child));
	}

	private void fillLexicalRule(Tree tree) {
		String[] ntAndLex = tree.getLabel().split(" ");
		ProbabilisticContextFreeGrammarRule maregle = new ProbabilisticContextFreeGrammarRule(ntAndLex[0], ntAndLex[1]);
		updateMap(lexicalRules, maregle);
		updateMap(countsNonTerminalElements, ntAndLex[0]);
	}

	private <K> void updateMap(Map<K, Double> map, K key) {
		map.computeIfPresent(key, (k, v) -> v + 1.0);
		map.putIfAbsent(key, 1.0);
	}

	public String toString() {
		StringBuffer toReturn = new StringBuffer();
		toReturn.append(binaryRules.keySet().stream().map(rule -> rule.toString()).collect(Collectors.joining("\n")));
		toReturn.append(lexicalRules.keySet().stream().map(rule -> rule.toString()).collect(Collectors.joining("\n")));
		return toReturn.toString();
	}

	/**
	 * Méthode permettant d'exporter l'instance courante de notre grammaire dans un fichier
	 * 
	 * @param string
	 */
	public void exportGramm(String binaryRulesFilePath, String lexicalRulesFilePath) {
		Utils.consumeCollectionToFile(binaryRulesFilePath, binaryRules.keySet(), rule -> rule.toExport() + "\n");
		Utils.consumeCollectionToFile(lexicalRulesFilePath, lexicalRules.keySet(), rule -> rule.toExport() + "\n");
	}

	/**
	 * Pour tout couple r1 r2 renvoie la liste des éléments LHR tels que <br>
	 * LHR --> r1 r2 est une règle de la grammaire et la probabilité associée à cette règle sous forme de paires
	 * 
	 * @param r1
	 * @param r2
	 * @return le tableau des PairesLPoids
	 */
	public Set<NonTerminalElementToProbability> lookUp(int r1, int r2) {
		Map<Integer, Set<NonTerminalElementToProbability>> map = lookUpMatrice.get(r1);
		if (map == null) {
			return new HashSet<NonTerminalElementToProbability>();
		}
		Set<NonTerminalElementToProbability> toReturn = map.get(r2);
		return toReturn == null ? new HashSet<NonTerminalElementToProbability>() : toReturn;
	}

	public NonTerminalElementToProbability[] lookUp(String r1) {
		// lookup pour les règles lexicales
		List<NonTerminalElementToProbability> toReturn;
		if (lexicalLookUpMatrix.containsKey(r1)) {
			toReturn = lexicalLookUpMatrix.get(r1);
			return toReturn.toArray(new NonTerminalElementToProbability[0]);
		}
		Set<NonTerminalElementToProbability> toReturnSuff = new HashSet<NonTerminalElementToProbability>();
		int size = r1.length();
		if (size >= 3) {
			toReturnSuff = returnSuffixes(size, toReturnSuff, r1);
		}
		if (toReturnSuff.size() == 0) {
			toReturnSuff = lexicalSuffixesLookUpMatrix.get(DUMMIES);
		}
		return toReturnSuff.toArray(new NonTerminalElementToProbability[0]);
	}

	private Set<NonTerminalElementToProbability> returnSuffixes(int size,
			Set<NonTerminalElementToProbability> toReturnSuff, String r1) {
		String suffixe = r1.substring(size - 3).toLowerCase();
		if (toReturnSuff.size() == 0 && lexicalSuffixesLookUpMatrix.containsKey(suffixe)) {
			lexicalSuffixesLookUpMatrix.get(suffixe);
		}
		if (toReturnSuff.size() == 0 && Character.isDigit(r1.charAt(0))) {
			toReturnSuff = lexicalSuffixesLookUpMatrix.get(DIGITS);
		}
		return toReturnSuff;
	}

	public String getAxiome() {
		return axiome;
	}

	public String getPositionForNonTerminal(int nonTerminal) {
		return nonTermnalElementPosition.get(nonTerminal);
	}

}