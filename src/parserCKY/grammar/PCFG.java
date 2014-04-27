package parserCKY.grammar;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import parserCKY.paires.TupleNTProb;
import parserCKY.tree.Tree;
import parserCKY.treebank.TreeBank;

/**
 * Une instance de PCFG est une représentation d'une grammaire sous forme CNF<br>
 * Elle dispose d'un collection de règles sous forme quadratique auquelles on assigne un poids<br>
 * selon le nombre de fois où chacune d'entre elles est observée au moment de l'importation de la grammaire 
 * @author antoine,misun,xin
 *
 */
public class PCFG{
	// liste de toutes les règles non lexicales de la grammaire
	private ArrayList<PCFGRule> binRules = new ArrayList<PCFGRule>();
	//liste de toutes les règles lexicales de la grammaire
	private ArrayList <PCFGRule> lexRules = new ArrayList<PCFGRule>();
	// une HashMap qui à chaque partie gauche lui associe le nombre de fois où elle a été vue
	// { NON-TERMINAL : nb de fois vu }
	private HashMap <String,Double> countsNT = new HashMap<String,Double>();
	//l'axiome de la grammaire
	private String axiome = null;

	public String getAxiome() {
		return axiome;
	}

	public void setAxiome(String axiome) {
		this.axiome = axiome;
	}

	// permet d'encoder les non-terminaux sur des nombres
	private ArrayList<String> ntPos = new ArrayList<String>();

	public ArrayList<String> getNtPos() {
		return ntPos;
	}

	public void setNtPos(ArrayList<String> ntPos) {
		this.ntPos = ntPos;
	}

	// une matrice à deux dimensions contenant des tableaux de PaireLPoids
	// pour deux  nt encodés par des nombres, on a l'ensemble des NT les produisants, avec la proba de cet évènement 
	private TupleNTProb [][][] lookUpMatrice; 
	private HashMap<String,ArrayList<TupleNTProb>> lookUpMatriceLex = new HashMap<String,ArrayList<TupleNTProb>> ();
	private HashMap<String,HashSet<TupleNTProb>> lookUpMatriceSuffixLex = new HashMap<String,HashSet<TupleNTProb>> ();

	private double occurences_lexique = 0;
	private double taille_vocabulaire=0;
	private int nbNonTerminaux;

	/**
	 * Constructeur d'une CNFGrammWithProbs.
	 * Si le paramètre n'est pas binarisé, on le binarise de force
	 * @param tb une instance de TreeBank
	 */
	public PCFG(TreeBank tb){
		tb.binariseTreeBank();
		for (Tree t : tb) {
			this.fillGrammar(t);
		}
		System.out.println("Grammaire importée.");
		this.nbNonTerminaux = 0;
		for (String nt : this.countsNT.keySet()){
			this.ntPos.add(nt);
			this.nbNonTerminaux++;
		}
		Collections.sort(this.binRules); // tri des règles binaires NP -> Det N
		Collections.sort(this.lexRules); // tri des règles pour le lexique Det -> le
		this.filterAndCounts();
		System.out.println("Grammaire terminée.\nParsing désormais possible.");
	}

	public static void exportGramm(String fichier){

	}


	/**
	 * Méthode privée qui permet de ne conserver chaque règles qu'une seule fois tout en mettant à jour les comptes.
	 * Cette méthode doit être appliquée aprés que gramm et lexicalRules aient été triés.
	 */
	private void filterAndCounts(){
		this.filterBinRules();
		System.out.println(this.nbNonTerminaux+" éléments non-terminaux dans la grammaire.");
		this.buildMatrice();
		System.out.println("Table de correspondance créée.");
		this.filterLexRules();
		System.out.println(this.taille_vocabulaire+" éléments terminaux dans la grammaire.");
		System.out.println(this.occurences_lexique+" occurences dans votre lexique.");
		this.smooth(1E-05);
		System.out.println("Lissage effectué.");
		//		this.exportGramm("binrules", "lexrules");
	}

	private void smooth(double lambda) {
		HashSet<TupleNTProb> digits = new HashSet<TupleNTProb>();
		HashSet<TupleNTProb> rares = new HashSet<TupleNTProb>();
		for (PCFGRule i : this.lexRules){
			double probLambda = Math.log(lambda/(this.countsNT.get(i.non_terminal)+(this.taille_vocabulaire*lambda)));
			if (i.poids<=3){
				rares.add(new TupleNTProb(this.ntPos.indexOf(i.non_terminal),probLambda));
			}
			i.poids = Math.log((i.poids+lambda)/(this.countsNT.get(i.non_terminal)+(this.taille_vocabulaire*lambda)));
			if (!this.lookUpMatriceLex.containsKey(i.rhr1))
				this.lookUpMatriceLex.put(i.rhr1, new ArrayList<TupleNTProb>());
			this.lookUpMatriceLex.get(i.rhr1).add(new TupleNTProb(this.ntPos.indexOf(i.non_terminal),i.poids));
			int taille = i.rhr1.length();
			if (taille >= 3){
				String suff = i.rhr1.substring(taille-3,taille).toLowerCase();
				if(!this.lookUpMatriceSuffixLex.containsKey(suff)){
					this.lookUpMatriceSuffixLex.put(suff, new HashSet<TupleNTProb>());
				}
				this.lookUpMatriceSuffixLex.get(suff).add(new TupleNTProb(this.ntPos.indexOf(i.non_terminal),probLambda));
			}
			if (Character.isDigit(i.rhr1.charAt(0))){
				digits.add(new TupleNTProb(this.ntPos.indexOf(i.non_terminal),probLambda));
			}
		}
		this.lookUpMatriceSuffixLex.put("digits", digits);
		this.lookUpMatriceSuffixLex.put("dummies", rares);
	}

	private void filterLexRules() {		
		// on va compter et filtrer ici les règles lexicales
		ArrayList<PCFGRule> filteredLexique = new ArrayList<PCFGRule>(); 
		for (int i=0 ; i<this.occurences_lexique-1;i++){
			PCFGRule ici = this.lexRules.get(i);
			PCFGRule plusUn = this.lexRules.get(i+1);
			if (ici.equals(plusUn))
				plusUn.poids = 1+ici.getPoids();
			else{
				filteredLexique.add(ici);
				this.taille_vocabulaire+=1;
			}
			if (Math.abs(i+1-this.occurences_lexique-1)==0){
				filteredLexique.add(plusUn);
				this.taille_vocabulaire+=1;
			}
		}
		// ici on fait le smoothing
		this.lexRules = filteredLexique;
	}

	// ici on filtre les règles binaires et on met en place les probabilités pour chacunes de ces règles
	private void filterBinRules() {
		ArrayList<PCFGRule> filteredGramm = new ArrayList<PCFGRule>(); 
		for (int i=0 ; i<this.binRules.size()-1;i++){
			PCFGRule ici = this.binRules.get(i); // la règle où on est
			PCFGRule plusUn = this.binRules.get(i+1); // la règle suivante
			if (ici.equals(plusUn)) // si elles sont égales
				plusUn.poids = 1.0+ici.poids; // on met à jour le poids (nb de fois vue) de la suivante
			else{
				ici.poids = Math.log(ici.poids/this.countsNT.get(ici.non_terminal)); // sinon, on calcule la probabilité de la règle
				filteredGramm.add(ici); // et on l'ajoute à notre grammaire filtrée
			}
			if (i+1==this.binRules.size()-1){
				plusUn.poids = Math.log(plusUn.poids/this.countsNT.get(plusUn.non_terminal)); // sinon, on calcule la probabilité de la règle
				filteredGramm.add(plusUn);
			}
		}
		this.binRules = filteredGramm; // notre set de règle devient cet ensemble filtré
	}

	// à partir d'ici on va créer notre lookup matrice où à chaque paire de non terminaux correspond l'ensemble des
	// non-terminaux qui peuvent les produire ensemble
	private void buildMatrice() {		
		int nt_size = this.nbNonTerminaux;
		this.lookUpMatrice = new TupleNTProb[nt_size][][];
		for (int i=0;i<nt_size;i++){
			this.lookUpMatrice[i] = new TupleNTProb[nt_size][];
			for (int j=0;j<nt_size;j++){
				this.lookUpMatrice[i][j] = new TupleNTProb[0];;
			}
		}
		for (PCFGRule rule : this.binRules){
			fillMatrix(rule);
		}
	}

	private void fillMatrix(PCFGRule rule) {
		int r1 = this.ntPos.indexOf(rule.rhr1);
		int r2 = this.ntPos.indexOf(rule.rhr2);
		TupleNTProb premierePaire = new TupleNTProb(this.ntPos.indexOf(rule.non_terminal),rule.poids);
		TupleNTProb[] toAdd = {premierePaire};
		if (this.lookUpMatrice.length==0){
			this.lookUpMatrice[r1][r2] = toAdd;
		}
		else this.lookUpMatrice[r1][r2] = this.concat(this.lookUpMatrice[r1][r2], toAdd);
	}

	// Permet la concaténation de deux tableaux de PaireLPoids
	private TupleNTProb[] concat(TupleNTProb[] add1, TupleNTProb[] add2) {
		TupleNTProb[] toReturn = new TupleNTProb[add1.length+add2.length];
		System.arraycopy(add1, 0, toReturn, 0, add1.length);
		System.arraycopy(add2, 0, toReturn, add1.length, add2.length);
		return toReturn;
	}

	/**
	 * Méthode privée de remplissage de notre grammaire à partir d'un arbre
	 * @param t l'arbre doit être binarisé
	 */
	private void fillGrammar(Tree t){
		if (t.getLabel().equals("")){
			for (Tree child : t.getChildren() ){
				this.fillGrammar(child);
			}
		}
		else{
			if (this.axiome==null){ // mise en place de l'axiome

				this.axiome = t.getLabel();
			}
			if (t.is_leaf()){ // règle lexicale !
				fillLexicalRule(t);
			}
			else{ // règle binaire
				fillBinRule(t);
			}
		}
	}

	private void fillBinRule(Tree t) {
		String elem1 = t.getChildren().get(0).getLabel().split(" ")[0];
		String elem2 = t.getChildren().get(1).getLabel().split(" ")[0];
		this.binRules.add(new PCFGRule(t.getLabel(), elem1, elem2));
		this.updateCountsNonTerm(t.getLabel());
		for (Tree tree : t.getChildren()){
			this.fillGrammar(tree);
		}
	}

	private void fillLexicalRule(Tree t) {
		String[]ntAndLex = t.getLabel().split(" ");
		PCFGRule maregle = new PCFGRule(ntAndLex[0],ntAndLex[1]);
		this.lexRules.add(maregle); //ajout de la règle lexicale
		this.occurences_lexique++;
		this.updateCountsNonTerm(ntAndLex[0]); // mise à jour des comptes		
	}

	// méthode privée qui met à jour les comptes des non-terminaux de la grammaire
	private void updateCountsNonTerm(String nt){
		if (this.countsNT.containsKey(nt)){
			this.countsNT.put(nt,new Double(this.countsNT.get(nt)+1.)) ; //dico[nt] += 1
		}
		else{
			this.countsNT.put(nt,new Double(1.)) ; // dico[nt] = 1
		}
	}

	public String toString(){
		StringBuffer toReturn = new StringBuffer();
		for (PCFGRule rule : this.binRules){
			toReturn.append(rule.toString()+"\n");
		}
		for (PCFGRule rule : this.lexRules){
			toReturn.append(rule.toString()+"\n");
		}
		return toReturn.toString();
	}

	/**
	 * Méthode permettant d'exporter l'instance courante de notre grammaire dans un fichier
	 * @param string
	 */
	public void exportGramm(String file_binrules,String file_lexrules) {
		try{
			FileWriter fw = new FileWriter(file_binrules, true);
			BufferedWriter output = new BufferedWriter(fw);
			output.write(this.axiome+"\n");
			for (PCFGRule t : this.binRules){
				output.write(t.toExport()+"\n");
			}
			output.flush();
			output.close();
			output = new BufferedWriter(new FileWriter(file_lexrules,true));
			for (PCFGRule r : this.lexRules){
				output.write(r.toExport()+"\n");
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
	public TupleNTProb[] lookUp(int r1,int r2){ //lookup pour les règles binaires
		return this.lookUpMatrice[r1][r2];		
	}

	public TupleNTProb[] lookUp(String r1){ //lookup pour les règles lexicales
		ArrayList<TupleNTProb> toReturn;
		if (this.lookUpMatriceLex.containsKey(r1)){
			toReturn = this.lookUpMatriceLex.get(r1);
			return toReturn.toArray(new TupleNTProb[toReturn.size()]);
		}
		HashSet<TupleNTProb>toReturnSuff = new HashSet<TupleNTProb>();
		int size = r1.length();
		if (size>=3){
			toReturnSuff = returnSuff(size,toReturnSuff,r1);
		}
		if (toReturnSuff.size()==0){
			toReturnSuff = this.lookUpMatriceSuffixLex.get("dummies");
		}
		return toReturnSuff.toArray(new TupleNTProb[toReturnSuff.size()]);
	}

	private HashSet<TupleNTProb> returnSuff(int size, HashSet<TupleNTProb> toReturnSuff,String r1) {
		String suffixe = r1.substring(size-3, size).toLowerCase();
		if (toReturnSuff.size()==0 && this.lookUpMatriceSuffixLex.containsKey(suffixe)){
			this.lookUpMatriceSuffixLex.get(suffixe);
		}
		if (toReturnSuff.size()==0 && Character.isDigit(r1.charAt(size-1))){
			toReturnSuff = this.lookUpMatriceSuffixLex.get("digits");
		}
		return toReturnSuff;
	}
}