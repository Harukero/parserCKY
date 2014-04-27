package parserCKY.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import parserCKY.grammar.PCFG;
import parserCKY.paires.TupleNTProb;
import parserCKY.paires.TupleNTTree;
import parserCKY.tree.Tree;

public class ParserCKY {

	private static TupleNTProb [][][] chart;
	private static TupleNTTree[][][] treeChart;
	private static int span=2,begin=0,end,split;
	private static HashMap<Integer,Double> aAjouter;
	private static HashMap<Integer,Tree> mesArbres;
	
	private ParserCKY(){
		
	}
	
	/**
	 * Cette méthode prend une phrase en imput et vérifie si cette phrase peut être<br>
	 * engendrée par la grammaire du parser, si c'est le cas, elle renvera le meilleur arbre d'analyse possible
	 * Sinon elle renvera un arbre dont la racine est CAN'T_BE_PARSED
	 * @param sent une phrase quelqu'on dont on veut avoir l'arbre syntaxique
	 * @return le meilleur arbre possible (le plus probable)
	 */
	public static Tree parse(String sent,PCFG gramm){
		String[] words = sent.split(" ");
		int nbMots = words.length;
		buildCharts(nbMots,words,gramm);
		for (span=2;span <= nbMots;span++){ // nombre de mots que gère la case courante
			for (begin=0 ;begin <= nbMots-span;begin++){
				end = begin+span;
				aAjouter = new HashMap<Integer,Double>();
				mesArbres = new HashMap<Integer,Tree>();
				for (split=begin+1;split<end;split++){
					for (int i=0;i<chart[begin][split].length;i++){ // pour tout B possible
						for (int j = 0 ; j<chart[split][end].length;j++){ // pour tout C possible
							getBestA(gramm,i,j);
						}
					}
					chart[begin][end] = toPaireArray(aAjouter);
					treeChart[begin][end] = toPaireLTArray(mesArbres);
				}
			}
		}
		return getBestTree(chart[0][nbMots],treeChart[0][nbMots],gramm);
	}

	private static void getBestA(PCFG gramm,int i,int j) {
		TupleNTProb b = chart[begin][split][i]; // récupération de B et sa probabilité
		TupleNTProb c = chart[split][end][j]; // récupération de C et sa probabilité
		TupleNTProb[] toAdd = gramm.lookUp(b.getL(), c.getL());
		// pour chaque A tel que A --> B C est dans la grammaire
		for (int k =0; k<toAdd.length;k++){ 
			TupleNTProb a = toAdd[k];
			Double prob;
			Tree monArbre = new Tree(a.getL().toString()); // arbre de racine A
			monArbre.addChild(treeChart[begin][split][i].getR()); // ajout du fils gauche
			monArbre.addChild(treeChart[split][end][j].getR()); // ajout du fils droit
			prob = a.getR()+b.getR()+c.getR();
			//assert !prob.isNaN();
			if (aAjouter.containsKey(a.getL())){ 
				if (aAjouter.get(a.getL()).compareTo(prob)<=0){
					aAjouter.put(a.getL() , prob);
					mesArbres.put(a.getL(), monArbre);
				}
			}
			else{
				aAjouter.put(a.getL(), prob);
				mesArbres.put(a.getL(), monArbre); 
			}
		}		
	}

	private static void buildCharts(int nbMots,String[]words,PCFG gramm) {
		chart = new TupleNTProb [nbMots][][];
		treeChart = new TupleNTTree[nbMots][][];
		for (int i=0;i<nbMots;i++){ // création des matrices de paires et d'arbres
			chart[i] = new TupleNTProb[nbMots+1][];
			treeChart[i] = new TupleNTTree[nbMots+1][];
			for (int j = 0;j<chart[i].length;j++){
				chart[i][j] = new TupleNTProb[0];
				treeChart[i][j] = new TupleNTTree[0];
			}
		}
		for (int i=0;i<nbMots;i++){ // remplissage des cases de lexique
			chart[i][i+1] = gramm.lookUp(words[i]);
			ArrayList<TupleNTTree> arbresL = new ArrayList<TupleNTTree>();
			for (TupleNTProb paire : chart[i][i+1])
				// remplissage des feuilles de l'arbre de parsing
				arbresL.add(new TupleNTTree(paire.getL(),new Tree(paire.getL().toString()+" "+words[i])));
			treeChart[i][i+1] = arbresL.toArray(new TupleNTTree[arbresL.size()]);
		}
	}

	private static Tree getBestTree(TupleNTProb[] probPossibles, TupleNTTree[] arbresPossibles,PCFG gramm) {
		Double proba = null;
		Tree toReturn = new Tree(" ");
		boolean found = false;
		String axiome = gramm.getAxiome();
		for (int i=0 ; i<probPossibles.length;i++){ // ici on vérifie que la phrase d'input est dans le langage 
			TupleNTProb maPaire = probPossibles[i];
			String categoryPaire = gramm.getNtPos().get(maPaire.getL()).split("\\*")[0];
			if (categoryPaire.equals(axiome)){ // si c'est le cas, on renvoie l'arbre de parsing
				found = true;
				if (proba == null || maPaire.getR()>proba){
					toReturn = new Tree("",arbresPossibles[i].getR());
					proba = maPaire.getR();
				}
			}
		}
		if (found){
			buildTree(toReturn,gramm);
			toReturn.unBinarise();
			}
		System.out.println(toReturn);
		return toReturn;
	}

	private static void buildTree(Tree tree,PCFG gramm) {
		if (tree.is_leaf()){
			int spacePos = tree.getLabel().indexOf(' ');
			Integer key = Integer.valueOf(tree.getLabel().substring(0, spacePos));
			String category = gramm.getNtPos().get(key);
			tree.setLabel(category +" "+tree.getLabel().substring(spacePos+1));
		}
		else {
			if (!tree.getLabel().equals("")){
				Integer category = Integer.valueOf(tree.getLabel());
				tree.setLabel(gramm.getNtPos().get(category));
			}
			for (Tree t : tree.getChildren()){
				buildTree(t,gramm);
			}
		}
	}

	// prend une table de hashage et renvoie le tableau des PaireLPoids correspondantes aux Entry
	private static TupleNTProb[] toPaireArray(HashMap<Integer, Double> aAjouter) {
		Set<Entry<Integer,Double>> mesEntries = aAjouter.entrySet();
		TupleNTProb [] toReturn = new TupleNTProb[mesEntries.size()];
		int i = 0;
		for (Entry<Integer,Double> paire : mesEntries ){
			toReturn[i++] = new TupleNTProb(paire.getKey(), paire.getValue());
		}
		return toReturn;
	}

	// prend une table de hashage et renvoie le tableau des PaireLPoids correspondantes aux Entry
	private static TupleNTTree[] toPaireLTArray(HashMap<Integer, Tree> aAjouter) {
		Set<Entry<Integer,Tree>> mesEntries = aAjouter.entrySet();
		TupleNTTree [] toReturn = new TupleNTTree[mesEntries.size()];
		int i = 0;
		for (Entry<Integer,Tree> paire : mesEntries ){
			toReturn[i++] = new TupleNTTree(paire.getKey(), paire.getValue());
		}
		return toReturn;
	}

}