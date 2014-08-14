package parserCKY.treebank;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import parserCKY.IConstants;
import parserCKY.Utils;
import parserCKY.tree.Tree;

/**
 * La classe de TreeBank représente un treebank, dans le sens le plus classique
 * du terme, soit une collection d'arbres syntaxiques déjà annotés.
 * 
 * @author antoine,misun,xin
 *
 */
public class Treebank implements Iterable<Tree> {

	private List<Tree> treebank = new ArrayList<Tree>();
	private final int markovDegree;

	public Treebank() {
		this(IConstants.SEQUOIA_CORPUS_PATH);
	}

	/**
	 * Constructeur d'un TreeBank. Prend en argument un nom de fichier dont
	 * chacune<br>
	 * des lignes est une chaîne de caractères représentant un arbre<br>
	 * d'un treebank et construit la collection des arbres binaires
	 * correspondants à ces chaînes
	 * 
	 * @param filename
	 */
	public Treebank(String filename) {
		this(filename, IConstants.MARKOVISATION_DEGREE);
	}

	/**
	 * Permet de contruire une instance de TreeBank en spécifiant le degré de
	 * markovisation
	 * 
	 * @param filename fichier contenant un treebank
	 * @param degre degré de markovisation
	 */
	public Treebank(String filename, int degre) {
		markovDegree = degre;
		Utils.forEachLineInFile(filename, line -> treebank.add(Tree.stringToTree(line)));
	}

	public void addTree(Tree tree) {
		treebank.add(tree);
	}

	/**
	 * Cette méthode permet de binariser le treebank entier, selon un certain
	 * degré de markovisation.
	 */
	public void binariseTreeBank() {
		treebank.forEach(tree -> tree.binarise(markovDegree));
	}

	/**
	 * Cette méthode permet de débinariser le treebank entier.
	 */
	public void unBinariseTreeBank() {
		treebank.forEach(Tree::unBinarise);
	}

	public String toString() {
		return treebank.stream().map(Tree::toString).collect(Collectors.joining("\n"));
	}

	/**
	 * Méthode d'instance. Prend un nom de fichier et <br>
	 * écrit dans ce fichier la représentation textuelle des arbres contenus
	 * dans le treebank
	 * 
	 * @param filename
	 */
	public void exportTreeBank(String filename) {
		Utils.consumeCollectionToFile(filename, treebank, tree -> tree.toString() + "\n");
	}

	public Iterator<Tree> iterator() {
		return treebank.iterator();
	}

	public void exportSent(String filename) {
		Utils.consumeCollectionToFile(filename, treebank, tree -> tree.toSentence() + "\n");
	}

}