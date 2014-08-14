package parserCKY.tree;

import java.util.ArrayList;
import java.util.Iterator;
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
		result = prime * result + ((this.children == null) ? 0 : this.children.hashCode());
		result = prime * result + ((this.label == null) ? 0 : this.label.hashCode());
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

	// la liste des enfants qui partent de la racine de notre arbre
	private List<Tree> children;

	public Tree(String label) {
		this(label, new ArrayList<Tree>());
	}

	public Tree(String label, List<Tree> children) {
		this.label = label;
		this.children = children; // puis on remplit la liste des fils
	}

	public Tree(String label, Tree uniqueChild) {
		this(label);
		children.add(uniqueChild);
	}

	public boolean isLeaf() {
		return children.isEmpty();
	}

	public String toString() {
		if (isLeaf()) {
			return "(" + label + ")";
		}
		return "(" + label + " " + children.stream().map(child -> child.toString()).collect(Collectors.joining(" "))
				+ ")";
	}

	public String toSentence() {
		if (isLeaf()) {
			int spacePos = label.indexOf(' ');
			return label.substring(spacePos + 1);
		}
		return children.stream().map(child -> child.toSentence()).collect(Collectors.joining(" "));
	}

	public void addChild(Tree child) {
		children.add(child);
	}

	public void binarise(int i) {
		int size = children.size();
		if (label.equals("")) {
			// treebank, qui est vide
			children.get(0).binarise(i);
		}
		switch (size) {
			case 0:
				break;
			case 1:
				if (!label.equals("")) {
					label += "*" + children.get(0).label;
					children = children.get(0).children;
					binarise(i);
				}
				break;
			case 2:
				children.forEach(tree -> tree.binarise(i));
				break;
			default:
				markovisation(i, size);
				break;
		}
	}

	private void markovisation(int degreMarkovisation, int size) {
		List<Tree> newFils = new ArrayList<Tree>();
		newFils.add(children.get(0));
		List<Tree> filsDuFauxFils = new ArrayList<Tree>();
		for (Tree t : (children.subList(1, size))) {
			filsDuFauxFils.add(t);
		}
		String newNodeLabel = "";
		for (int i = 1; i <= degreMarkovisation && i < size; i++) {
			newNodeLabel += Utils.getFirstPart(children.get(i).label) + "$";
		}
		newFils.add(new Tree(newNodeLabel, filsDuFauxFils));
		children = newFils;
		children.forEach(fils -> fils.binarise(degreMarkovisation));
	}

	/**
	 * fonction de débinarisation de l'arbre courant nécessite que l'arbre
	 * courant soit binarisé via la méthode "binarise" de cette classe
	 */
	public void unBinarise() {
		List<Tree> newFils = new ArrayList<Tree>();
		boolean childContainsDollar = false;
		if (!isLeaf() && label.contains("*")) {
			// cas d'une production unaire
			int starIndex = label.indexOf("*");
			String newLabel = label.substring(0, starIndex);
			// le nouveau label est l'ensemble des caractères jusqu'à la
			// première étoile exclue
			Tree nouvelArbre = new Tree(label.substring(starIndex + 1));
			label = newLabel;
			nouvelArbre.children = children;
			nouvelArbre.unBinarise();
			newFils.add(nouvelArbre);
			this.children = newFils; // on met à jour la liste des fils de l'arbre courant
		} else if (!label.equals("") && !isLeaf() && !label.contains("$")) { // si l'arbre  courant n'est pas une feuille
			for (Tree child : children) { // on vérifie chacun de ses fils
				if (child.label.indexOf("$") != -1 && !child.isLeaf()) {
					// si le fils contient une $
					childContainsDollar = true;
					// on indique que l'arbre courant a au moins un fils avec une $
					for (Tree son : child.children) {
						newFils.add(son);
						// on ajoute chacun des fils de ce fils aux futurs fils de l'arbre courant
					}
				} else {
					newFils.add(child); // sinon on conserve ce fils
				}
			}
			children = newFils; // on met à jour la liste des fils de l'arbre courant
		}
		if (isLeaf() && label.contains("*")) {
			// et on remet les productions unaires des feuilles si il y en avait
			String newLabel = label.substring(0, label.indexOf("*"));
			// le nouveau label est l'ensemble des caractères jusqu'à la première * exclue
			Tree nouvelArbre = new Tree(label.substring(label.indexOf("*") + 1));
			label = newLabel;
			nouvelArbre.unBinarise();
			addChild(nouvelArbre);
		}// si on est une feuille contenant une * : on crée un nouveau niveau en dessous qu'on débinarise
		if (childContainsDollar)
			unBinarise(); // si on a fait au moins une modification, on recommence au même niveau
		else {
			for (Tree child : children) {
				// sinon on débinarise tous les fils de l'arbre courant
				child.unBinarise();
			}
		}
	}

	public Tree getChildAt(int position) {
		if (position < 0 || children.size() == 0 || children.size() < position) {
			return null;
		}
		return children.get(position);
	}

	public String getLabelFromChildAt(int position) {
		return getChildAt(position) == null ? null : getChildAt(position).label;
	}

	// Fonctions statiques /////////////////////////////////////

	/**
	 * Méthode de classe. Prend en argument une chaîne de caractères
	 * représentant un arbre<br> d'un treebank et renvoi l'arbre correspondant à cette chaîne
	 * 
	 * @param treebankFormatTree (X (YYYY y) (ZZZ z))
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
			if (treebankSplit[i] == ')') { // on est à la fin d'un niveau enlever l'élément du sommet de la pile
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
		for (Iterator<TokenDependancy> lines = children.iterator(); lines.hasNext();) {
			TokenDependancy line = lines.next();
			if (line.getIndex() >= indice && on_a_ajoute == false) {
				// si l'indice qu'on veut trouver est superieur a celui du pere,
				// on ajoute avant cette indice.
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

	private static List<TokenDependancy> getChildren(int indice, List<TokenDependancy> list) {
		List<TokenDependancy> children = new ArrayList<TokenDependancy>();
		for (TokenDependancy line : list) {
			if (line.getFatherIndex() == indice) {
				children.add(line);
			}
		}
		return children;
	}

	public static void main(String[] args) {
		Tree t = stringToTree("( (SENT (NP (NC M.) (NPP Teulade)) (VN (V peut)) (PONCT ,) (ADV à_juste_titre) (PONCT ,) (VPinf (VN (VINF considérer)) (Ssub (CS que) (PONCT \") (NP (DET la) (NC crédibilité) (PP (P+D du) (NP (NC système) (AP (ADJ conventionnel))))) (VN (V est)) (ADV en_jeu) (PONCT \"))) (PONCT .)))");
		System.out.println(t);
		System.out.println(t.toSentence());
		t.binarise(1);
		System.out.println(t);
	}

}