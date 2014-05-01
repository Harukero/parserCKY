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

import parserCKY.paires.NonTerminalElementToProbability;
import parserCKY.tree.Tree;
import parserCKY.treebank.Treebank;

/**
 * Une instance de PCFG est une représentation d'une grammaire sous forme CNF<br>
 * Elle dispose d'un collection de règles sous forme quadratique auquelles on assigne un poids<br>
 * selon le nombre de fois où chacune d'entre elles est observée au moment de l'importation de la grammaire 
 * @author antoine,misun,xin
 *
 */
public class ProbabilisticContextFreeGrammar{
	// liste de toutes les règles non lexicales de la grammaire
	private List<ProbabilisticContextFreeGrammarRule> binaryRules = new ArrayList<ProbabilisticContextFreeGrammarRule>();
	//liste de toutes les règles lexicales de la grammaire
	private List <ProbabilisticContextFreeGrammarRule> lexicalRules = new ArrayList<ProbabilisticContextFreeGrammarRule>();
	// une HashMap qui à chaque partie gauche lui associe le nombre de fois où elle a été vue
	// { NON-TERMINAL : nb de fois vu }
	private Map <String,Double> countsNonTerminalElements = new HashMap<String,Double>();
	//l'axiome de la grammaire
	private String axiome = null;

	public String getAxiome() {
		return axiome;
	}

	public void setAxiome(String axiome) {
		this.axiome = axiome;
	}

	// permet d'encoder les non-terminaux sur des nombres
	private List<String> nonTermnalElementPosition = new ArrayList<String>();

	public List<String> getNtPos() {
		return nonTermnalElementPosition;
	}

	public void setNtPos(List<String> ntPos) {
		this.nonTermnalElementPosition = ntPos;
	}

	// une matrice à deux dimensions contenant des tableaux de PaireLPoids
	// pour deux  nt encodés par des nombres, on a l'ensemble des NT les produisants, avec la proba de cet évènement 
	private NonTerminalElementToProbability [][][] lookUpMatrice; 
	private Map<String,List<NonTerminalElementToProbability>> lexicalLookUpMatrix = new HashMap<String,List<NonTerminalElementToProbability>> ();
	private Map<String,Set<NonTerminalElementToProbability>> lexicalSuffixesLookUpMatrix = new HashMap<String,Set<NonTerminalElementToProbability>> ();

	private double lexicalTokens = 0;
	private double vocabularySize=0;
	private int nonTermnalsNumber;

	/**
	 * Constructeur d'une CNFGrammWithProbs.
	 * Si le paramètre n'est pas binarisé, on le binarise de force
	 * @param trebank une instance de TreeBank
	 */
	public ProbabilisticContextFreeGrammar(Treebank trebank){
		trebank.binariseTreeBank();
		for (Tree tree : trebank) {
			this.fillGrammar(tree);
		}
		System.out.println("Grammaire importée.");
		this.nonTermnalsNumber = 0;
		for (String nonTerminal : this.countsNonTerminalElements.keySet()){
			this.nonTermnalElementPosition.add(nonTerminal);
			this.nonTermnalsNumber++;
		}
		Collections.sort(this.binaryRules); // tri des règles binaires NP -> Det N
		Collections.sort(this.lexicalRules); // tri des règles pour le lexique Det -> le
		this.filterAndCounts();
		System.out.println("Grammaire terminée.\nParsing désormais possible.");
	}

	/**
	 * Méthode privée qui permet de ne conserver chaque règles qu'une seule fois tout en mettant à jour les comptes.
	 * Cette méthode doit être appliquée aprés que gramm et lexicalRules aient été triés.
	 */
	private void filterAndCounts(){
		this.filterBinRules();
		System.out.println(this.nonTermnalsNumber+" éléments non-terminaux dans la grammaire.");
		this.buildMatrice();
		System.out.println("Table de correspondance créée.");
		this.filterLexRules();
		System.out.println(this.vocabularySize+" éléments terminaux dans la grammaire.");
		System.out.println(this.lexicalTokens+" occurences dans votre lexique.");
		this.smooth(1E-05);
		System.out.println("Lissage effectué.");
		//		this.exportGramm("binrules", "lexrules");
	}

	private void smooth(double lambda) {
		Set<NonTerminalElementToProbability> digits = new HashSet<NonTerminalElementToProbability>();
		Set<NonTerminalElementToProbability> rares = new HashSet<NonTerminalElementToProbability>();
		for (ProbabilisticContextFreeGrammarRule i : this.lexicalRules){
			double probLambda = Math.log(lambda/(this.countsNonTerminalElements.get(i.non_terminal)+(this.vocabularySize*lambda)));
			if (i.poids<=3){
				rares.add(new NonTerminalElementToProbability(this.nonTermnalElementPosition.indexOf(i.non_terminal),probLambda));
			}
			i.poids = Math.log((i.poids+lambda)/(this.countsNonTerminalElements.get(i.non_terminal)+(this.vocabularySize*lambda)));
			if (!this.lexicalLookUpMatrix.containsKey(i.rhr1))
				this.lexicalLookUpMatrix.put(i.rhr1, new ArrayList<NonTerminalElementToProbability>());
			this.lexicalLookUpMatrix.get(i.rhr1).add(new NonTerminalElementToProbability(this.nonTermnalElementPosition.indexOf(i.non_terminal),i.poids));
			int taille = i.rhr1.length();
			if (taille >= 3){
				String suff = i.rhr1.substring(taille-3,taille).toLowerCase();
				if(!this.lexicalSuffixesLookUpMatrix.containsKey(suff)){
					this.lexicalSuffixesLookUpMatrix.put(suff, new HashSet<NonTerminalElementToProbability>());
				}
				this.lexicalSuffixesLookUpMatrix.get(suff).add(new NonTerminalElementToProbability(this.nonTermnalElementPosition.indexOf(i.non_terminal),probLambda));
			}
			if (Character.isDigit(i.rhr1.charAt(0))){
				digits.add(new NonTerminalElementToProbability(this.nonTermnalElementPosition.indexOf(i.non_terminal),probLambda));
			}
		}
		this.lexicalSuffixesLookUpMatrix.put("digits", digits);
		this.lexicalSuffixesLookUpMatrix.put("dummies", rares);
	}

	private void filterLexRules() {		
		// on va compter et filtrer ici les règles lexicales
		ArrayList<ProbabilisticContextFreeGrammarRule> filteredLexique = new ArrayList<ProbabilisticContextFreeGrammarRule>(); 
		for (int i=0 ; i<this.lexicalTokens-1;i++){
			ProbabilisticContextFreeGrammarRule ici = this.lexicalRules.get(i);
			ProbabilisticContextFreeGrammarRule plusUn = this.lexicalRules.get(i+1);
			if (ici.equals(plusUn))
				plusUn.poids = 1+ici.getPoids();
			else{
				filteredLexique.add(ici);
				this.vocabularySize+=1;
			}
			if (Math.abs(i+1-this.lexicalTokens-1)==0){
				filteredLexique.add(plusUn);
				this.vocabularySize+=1;
			}
		}
		// ici on fait le smoothing
		this.lexicalRules = filteredLexique;
	}

	// ici on filtre les règles binaires et on met en place les probabilités pour chacunes de ces règles
	private void filterBinRules() {
		ArrayList<ProbabilisticContextFreeGrammarRule> filteredGramm = new ArrayList<ProbabilisticContextFreeGrammarRule>(); 
		for (int i=0 ; i<this.binaryRules.size()-1;i++){
			ProbabilisticContextFreeGrammarRule ici = this.binaryRules.get(i); // la règle où on est
			ProbabilisticContextFreeGrammarRule plusUn = this.binaryRules.get(i+1); // la règle suivante
			if (ici.equals(plusUn)) // si elles sont égales
				plusUn.poids = 1.0+ici.poids; // on met à jour le poids (nb de fois vue) de la suivante
			else{
				ici.poids = Math.log(ici.poids/this.countsNonTerminalElements.get(ici.non_terminal)); // sinon, on calcule la probabilité de la règle
				filteredGramm.add(ici); // et on l'ajoute à notre grammaire filtrée
			}
			if (i+1==this.binaryRules.size()-1){
				plusUn.poids = Math.log(plusUn.poids/this.countsNonTerminalElements.get(plusUn.non_terminal)); // sinon, on calcule la probabilité de la règle
				filteredGramm.add(plusUn);
			}
		}
		this.binaryRules = filteredGramm; // notre set de règle devient cet ensemble filtré
	}

	// à partir d'ici on va créer notre lookup matrice où à chaque paire de non terminaux correspond l'ensemble des
	// non-terminaux qui peuvent les produire ensemble
	private void buildMatrice() {		
		int nt_size = this.nonTermnalsNumber;
		this.lookUpMatrice = new NonTerminalElementToProbability[nt_size][][];
		for (int i=0;i<nt_size;i++){
			this.lookUpMatrice[i] = new NonTerminalElementToProbability[nt_size][];
			for (int j=0;j<nt_size;j++){
				this.lookUpMatrice[i][j] = new NonTerminalElementToProbability[0];
			}
		}
		for (ProbabilisticContextFreeGrammarRule rule : this.binaryRules){
			fillMatrix(rule);
		}
	}

	private void fillMatrix(ProbabilisticContextFreeGrammarRule rule) {
		int r1 = this.nonTermnalElementPosition.indexOf(rule.rhr1);
		int r2 = this.nonTermnalElementPosition.indexOf(rule.rhr2);
		NonTerminalElementToProbability premierePaire = new NonTerminalElementToProbability(this.nonTermnalElementPosition.indexOf(rule.non_terminal),rule.poids);
		NonTerminalElementToProbability[] toAdd = {premierePaire};
		if (this.lookUpMatrice.length==0){
			this.lookUpMatrice[r1][r2] = toAdd;
		}
		else this.lookUpMatrice[r1][r2] = this.concat(this.lookUpMatrice[r1][r2], toAdd);
	}

	// Permet la concaténation de deux tableaux de PaireLPoids
	private NonTerminalElementToProbability[] concat(NonTerminalElementToProbability[] add1, NonTerminalElementToProbability[] add2) {
		NonTerminalElementToProbability[] toReturn = new NonTerminalElementToProbability[add1.length+add2.length];
		System.arraycopy(add1, 0, toReturn, 0, add1.length);
		System.arraycopy(add2, 0, toReturn, add1.length, add2.length);
		return toReturn;
	}

	/**
	 * Méthode privée de remplissage de notre grammaire à partir d'un arbre
	 * @param tree l'arbre doit être binarisé
	 */
	private void fillGrammar(Tree tree){
		if (tree.getLabel().equals("")){
			for (Tree child : tree.getChildren() ){
				this.fillGrammar(child);
			}
		}
		else{
			if (this.axiome==null){ // mise en place de l'axiome

				this.axiome = tree.getLabel();
			}
			if (tree.is_leaf()){ // règle lexicale !
				fillLexicalRule(tree);
			}
			else{ // règle binaire
				fillBinRule(tree);
			}
		}
	}

	private void fillBinRule(Tree t) {
		String elem1 = t.getChildren().get(0).getLabel().split(" ")[0];
		String elem2 = t.getChildren().get(1).getLabel().split(" ")[0];
		this.binaryRules.add(new ProbabilisticContextFreeGrammarRule(t.getLabel(), elem1, elem2));
		this.updateCountsNonTerm(t.getLabel());
		for (Tree tree : t.getChildren()){
			this.fillGrammar(tree);
		}
	}

	private void fillLexicalRule(Tree t) {
		String[]ntAndLex = t.getLabel().split(" ");
		ProbabilisticContextFreeGrammarRule maregle = new ProbabilisticContextFreeGrammarRule(ntAndLex[0],ntAndLex[1]);
		this.lexicalRules.add(maregle); //ajout de la règle lexicale
		this.lexicalTokens++;
		this.updateCountsNonTerm(ntAndLex[0]); // mise à jour des comptes		
	}

	// méthode privée qui met à jour les comptes des non-terminaux de la grammaire
	private void updateCountsNonTerm(String nt){
		if (this.countsNonTerminalElements.containsKey(nt)){
			this.countsNonTerminalElements.put(nt,new Double(this.countsNonTerminalElements.get(nt)+1.)) ; //dico[nt] += 1
		}
		else{
			this.countsNonTerminalElements.put(nt,new Double(1.)) ; // dico[nt] = 1
		}
	}

	public String toString(){
		StringBuffer toReturn = new StringBuffer();
		for (ProbabilisticContextFreeGrammarRule rule : this.binaryRules){
			toReturn.append(rule.toString()+"\n");
		}
		for (ProbabilisticContextFreeGrammarRule rule : this.lexicalRules){
			toReturn.append(rule.toString()+"\n");
		}
		return toReturn.toString();
	}

	/**
	 * Méthode permettant d'exporter l'instance courante de notre grammaire dans un fichier
	 * @param string
	 */
	public void exportGramm(String binaryRulesFilePath,String lexicalRulesFilePath) {
		try{
			FileWriter fw = new FileWriter(binaryRulesFilePath, true);
			BufferedWriter output = new BufferedWriter(fw);
			output.write(this.axiome+"\n");
			for (ProbabilisticContextFreeGrammarRule t : this.binaryRules){
				output.write(t.toExport()+"\n");
			}
			output.flush();
			output.close();
			output = new BufferedWriter(new FileWriter(lexicalRulesFilePath,true));
			for (ProbabilisticContextFreeGrammarRule rule : this.lexicalRules){
				output.write(rule.toExport()+"\n");
			}
			output.flush();
			output.close();
			System.out.println("fichier créé");
		}
		catch(IOException ioe){
			System.out.print("Erreur : ");
			ioe.printStackTrace();
		}
	}

	/**
	 * Pour tout couple r1 r2 renvoie la liste des éléments LHR tels que <br>
	 * LHR --> r1 r2 est une règle de la grammaire et la probabilité associée à cette règle <br>
	 * sous forme de paires
	 * @param r1 
	 * @param r2
	 * @return le tableau des PairesLPoids
	 */
	public NonTerminalElementToProbability[] lookUp(int r1,int r2){ //lookup pour les règles binaires
		return this.lookUpMatrice[r1][r2];		
	}

	public NonTerminalElementToProbability[] lookUp(String r1){ //lookup pour les règles lexicales
		List<NonTerminalElementToProbability> toReturn;
		if (this.lexicalLookUpMatrix.containsKey(r1)){
			toReturn = this.lexicalLookUpMatrix.get(r1);
			return toReturn.toArray(new NonTerminalElementToProbability[toReturn.size()]);
		}
		Set<NonTerminalElementToProbability>toReturnSuff = new HashSet<NonTerminalElementToProbability>();
		int size = r1.length();
		if (size>=3){
			toReturnSuff = returnSuffixes(size,toReturnSuff,r1);
		}
		if (toReturnSuff.size()==0){
			toReturnSuff = this.lexicalSuffixesLookUpMatrix.get("dummies");
		}
		return toReturnSuff.toArray(new NonTerminalElementToProbability[toReturnSuff.size()]);
	}

	private Set<NonTerminalElementToProbability> returnSuffixes(int size, Set<NonTerminalElementToProbability> toReturnSuff,String r1) {
		String suffixe = r1.substring(size-3, size).toLowerCase();
		if (toReturnSuff.size()==0 && this.lexicalSuffixesLookUpMatrix.containsKey(suffixe)){
			this.lexicalSuffixesLookUpMatrix.get(suffixe);
		}
		if (toReturnSuff.size()==0 && Character.isDigit(r1.charAt(size-1))){
			toReturnSuff = this.lexicalSuffixesLookUpMatrix.get("digits");
		}
		return toReturnSuff;
	}
}