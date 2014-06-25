package parserCKY.tree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

import parserCKY.Utils;
import parserCKY.treebank.TokenDependancy;

/**
 * Un Tree est une repr�sentation de la structure de donn�e correspondant aux
 * arbres.<br>
 * Un Tree est un noeud ayant un nom (label), potentiellement un p�re, et un
 * nombre potentiellement infini<br>
 * d'enfants qui sont aussi des Tree
 * 
 * @author Antoine Misun Xin
 */
public class Tree {

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((this.children == null) ? 0 : this.children.hashCode());
		result = prime * result
				+ ((this.label == null) ? 0 : this.label.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Tree))
			return false;
		Tree other = (Tree) obj;
		if (this.children == null) {
			if (other.children != null)
				return false;
		} else if (!this.children.equals(other.children))
			return false;
		if (this.label == null) {
			if (other.label != null)
				return false;
		} else if (!this.label.equals(other.label))
			return false;
		return true;
	}

	private String label; // le nom de la racine de l'arbre

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public List<Tree> getChildren() {
		return children;
	}

	public void setChildren(List<Tree> children) {
		this.children = children;
	}

	private List<Tree> children = new ArrayList<Tree>(); // la liste des enfants
															// qui partent de la
															// racine de notre
															// arbre

	/**
	 * Constructeur d'un Tree, construit un arbre sans fils (une feuille)
	 * 
	 * @param label
	 */
	public Tree(String label) {
		this.label = label;
	}

	/**
	 * Constructeur d'un Tree, construit un arbre en lui ajoutant des fils
	 * 
	 * @param label
	 * @param children
	 */
	public Tree(String label, List<Tree> children) {
		this(label); // ici on applique le constructeur d'au dessus
		this.children = children; // puis on remplit la liste des fils
	}

	public Tree(String label, Tree uniqueChild) {
		this(label);
		children.add(uniqueChild);
	}

	/**
	 * Méthode permettant de savoir si l'arbre courant est une feuille ou non
	 * 
	 * @return true si le Tree courant est une feuille, false sinon
	 */
	public boolean is_leaf() {
		return children.isEmpty();
	}

	public String toString() {
		if (is_leaf()) {
			return "(" + label + ")";
		}
		return "("
				+ label
				+ " "
				+ children.stream().map(child -> child.toString())
						.collect(Collectors.joining(" ")) + ")";
	}

	public String toSentence() {
		if (is_leaf()) {
			return label.split(" ")[1];
		}
		return children.stream().map(child -> child.toSentence())
				.collect(Collectors.joining(" "));
	}

	/**
	 * Méthode permettant d'ajouter un enfant à l'arbre courant
	 * 
	 * @param child
	 */
	public void addChild(Tree child) {
		children.add(child);
	}

	/**
	 * Méthode d'instance renvoyant l'arbre courant sous forme d'arbre binarisé
	 * 
	 * @param i
	 *            le nombre d'éléments à conserver quand on crée un nouveau
	 *            noeud dans l'arbre pour la markovisation
	 */
	public void binarise(int i) {
		int size = children.size();
		if (label.equals("")) {
			// treebank, qui est vide
			children.get(0).binarise(i);
		}
		if (label.equals("") && size == 1) { // gestion des règles unaires
			label += "*" + children.get(0).label;
			children = children.get(0).children;
			binarise(i);
		}
		if (size == 2) {
			for (Tree fils : children) {
				fils.binarise(i);
			}
		}
		if (size >= 3) { // quand il y a 3 enfants ou plus
			markovisation(i, size);
		}
	}

	private void markovisation(int degreMarkovisation, int size) {
		List<Tree> newFils = new ArrayList<Tree>();
		// on crée une liste des futurs nouveaux enfants (fils de gauche et faux
		// fils)
		newFils.add(children.get(0)); 
		// on ajoute le premier fils à cette liste
		List<Tree> filsDuFauxFils = new ArrayList<Tree>(); 
		// on crée une liste pour les fils du Faux Fils
		for (Tree t : (children.subList(1, size))) { 
			// pour tous les autres fils (donc pas le premier)
			filsDuFauxFils.add(t); 
			// on les ajoute aux fils du faux fils
		}
		String newNodeLabel = "";
		if (degreMarkovisation >= 1)
			newNodeLabel += Utils.getFirstPart(children.get(1).label) + "$"; 
		// on crée ici le nom du Faux fils en récupérant que les non-terminaux
		if (degreMarkovisation >= 2) {
			newNodeLabel += Utils.getFirstPart(children.get(2).label) + "$";
		}
		newFils.add(new Tree(newNodeLabel, filsDuFauxFils));
		children = newFils;
		for (Tree fils : children) {
			fils.binarise(degreMarkovisation);
		}
	}

	/**
	 * fonction de débinarisation de l'arbre courant nécessite que l'arbre
	 * courant soit binarisé via la méthode "binarise" de cette classe
	 */
	public void unBinarise() {
		LinkedList<Tree> newFils = new LinkedList<Tree>();
		boolean childContainsDollar = false;
		if (!is_leaf() && label.contains("*")) { 
			// cas d'une production unaire
			int starIndex = label.indexOf("*");
			String newLabel = label.substring(0, starIndex); 
			// le nouveau label est l'ensemble des caractères jusqu'à la première étoile exclue
			Tree nouvelArbre = new Tree(label.substring(starIndex + 1));
			label = newLabel;
			nouvelArbre.children = children;
			nouvelArbre.unBinarise();
			newFils.add(nouvelArbre);
			this.children = newFils; // on met à jour la liste des fils de
										// l'arbre courant
		} else if (!label.equals("") && !is_leaf()
				&& !label.contains("$")) { // si l'arbre courant n'est pas
												// une feuille
			for (Tree child : children) { // on vérifie chacun de ses fils
				if (child.label.indexOf("$") != -1 && !child.is_leaf()) { 
					// si le fils contient une $
					childContainsDollar = true; 
					// on indique que l'arbre courant a au moins un fils avec une $
					for (Tree son : child.children){
						newFils.add(son); 
					// on ajoute chacun des fils de ce fils aux futurs fils de l'arbre courant
					}
				}
				else {
					newFils.add(child); // sinon on conserve ce fils
				}
			}
			children = newFils; // on met à jour la liste des fils de l'arbre courant
		}
		if (is_leaf() && label.contains("*")) {
			// et on remet les productions unaires des feuilles si il y en avait
			String newLabel = label.substring(0, label.indexOf("*")); 
			// le nouveau label est l'ensemble des caractères jusqu'à la première *  exclue
			Tree nouvelArbre = new Tree(label.substring(label
					.indexOf("*") + 1));
			label = newLabel;
			nouvelArbre.unBinarise();
			addChild(nouvelArbre);
		}// si on est une feuille contenant une * : on crée un nouveau niveau en
			// dessous qu'on débinarise
		if (childContainsDollar)
			unBinarise(); // si on a fait au moins une modification, on
								// recommence au même niveau
		else {
			for (Tree child : children) { 
				// sinon on débinarise tous les fils de l'arbre courant
				child.unBinarise();
			}
		}
	}

	// Fonctions statiques /////////////////////////////////////

	/**
	 * Méthode de classe. Prend en argument une chaîne de caractères
	 * représentant un arbre<br>
	 * d'un treebank et renvoi l'arbre correspondant à cette chaîne
	 * 
	 * @param treebankFormatTree
	 *            (X (YYYY y) (ZZZ z))
	 * @return un arbre
	 */
	public static Tree stringToTree(String treebankFormatTree) {
		char[] treebankSplit = treebankFormatTree.toCharArray();
		Stack<Tree> pileDArbres = new Stack<Tree>();
		String label = "";
		for (int i = 0; i < treebankSplit.length; i++) {
			if (treebankSplit[i] == '(') // début d'un niveau dans l'arbre
				while (true) {
					if (treebankSplit[i + 1] == '(') {
						pileDArbres.push(new Tree(label));
						label = "";
						break; // on sort du while
					} else if (treebankSplit[i + 1] == ')') {
						label += treebankSplit[i];
						pileDArbres.push(new Tree(label));
						label = "";
						break; // on sort du while
					} else {
						if (treebankSplit[i] != '(' && treebankSplit[i] != ')') {
							// ajout du caractère au label
							label += treebankSplit[i];
						} 
						i++; // passer au caractère suivant
					}
				} // sortie du while : on a ajout à un noeud à la pile
			if (treebankSplit[i] == ')') { // on est à la fin d'un niveau
				// enlever l'élément du sommet de la pile
				Tree monarbre = pileDArbres.pop(); 
				if (pileDArbres.size() > 0) {
					pileDArbres.peek().addChild(monarbre);
				} else {
					return monarbre;
				}
			}
		}
		return null; // en cas d'échec (mauvais fichier)
	}

	public static List<TokenDependancy> list2tokenDep(List<String> list) {
		List<TokenDependancy> tableau = new ArrayList<TokenDependancy>();
		for (String lst : list) {
			tableau.add(new TokenDependancy(lst));
		}
		return tableau;
	}

	public static Tree tokenList2Tree(List<String> mesLignes) {
		List<TokenDependancy> mesTokenDep = list2tokenDep(mesLignes);
		Tree arbre = new Tree("");
		TokenDependancy racine = racine(mesTokenDep);
		arbre.addChild(new Tree(racine.getCategory() + " " + racine.getToken()));
		arbre.children.get(0).buildTree(racine.getIndex(), mesTokenDep);
		return arbre;
	}

	private void buildTree(int indice, List<TokenDependancy> list) {
		boolean on_a_ajoute = false;
		List<TokenDependancy> children = getChildren(indice, list);
		for (Iterator<TokenDependancy> lines = children.iterator(); lines
				.hasNext();) {
			TokenDependancy line = lines.next();
			if (line.getIndex() >= indice && on_a_ajoute == false) { 
				// si l'indice qu'on veut trouver est superieur a celui du pere, on ajoute avant cette indice.
				on_a_ajoute = true;
				headed();
			}
			Tree fils = new Tree(line.getCategory() + " " + line.getToken());
			fils.buildTree(line.getIndex(), list);
			this.addChild(fils);
			if (!lines.hasNext() && on_a_ajoute == false) {
				on_a_ajoute = true;
				headed();
			}
		}
	}

	private void headed() {
		String newlabel = label; // on cree un nouveau label pour le pere
		label = Utils.getFirstPartBefore(label, ' '); // on recupere le CAT du pere
		label = label + "%"; // puis ajouter un %
		addChild(new Tree(newlabel)); // on ajoute V est aux fils.
	}

	private static TokenDependancy racine(List<TokenDependancy> list) {
		for (TokenDependancy line : list) {
			if (line.getFatherIndex() == 0) {
				return line;
			}
		}
		return null;
	}

	private static List<TokenDependancy> getChildren(int indice,
			List<TokenDependancy> list) {
		List<TokenDependancy> children = new ArrayList<TokenDependancy>();
		for (TokenDependancy line : list) {
			if (line.getFatherIndex() == indice) {
				children.add(line);
			}
		}
		return children;
	}

	public static void main(String[] args) {
		Tree t = stringToTree(
				"( (SENT (ADV Ensuite) (PONCT ,) (VN (V fut) (VPP installée)) (NP-SUJ (DET une) (ADJ autre) (NC forge)) (PP-MOD (P à) (NP (DET la) (NPP Vacquerie))) (PONCT ,) (PP-MOD (P à) (NP (DET l') (NC emplacement) (ADV aujourd'_hui) (PP (P de) (NP (NPP Cora))))) (PONCT .)))");
		System.out.println(t);
		System.out.println(t.toSentence());
	}

}