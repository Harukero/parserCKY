package parserCKY.treebank;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

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
	// par défaut markovisation de degré 2 en cas de binarisation
	private int markovDegree = 2;

	public Treebank() {

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
		try {
			Files.lines(Paths.get(filename)).forEach(line -> treebank.add(Tree.stringToTree(line)));
		} catch (Exception e) {
			System.out.print("Erreur : ");
			e.printStackTrace();
		}
	}

	/**
	 * Permet de contruire une instance de TreeBank en spécifiant le degré de
	 * markovisation
	 * 
	 * @param filename fichier contenant un treebank
	 * @param degre degré de markovisation
	 */
	public Treebank(String filename, int degre) {
		this(filename);
		markovDegree = degre;
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
		String joinedTrees = treebank.stream().map(Tree::toString).collect(Collectors.joining("\n"));
		return joinedTrees;
	}

	/**
	 * Méthode d'instance. Prend un nom de fichier et <br>
	 * écrit dans ce fichier la représentation textuelle des arbres contenus
	 * dans le treebank
	 * 
	 * @param filename
	 */
	public void exportTreeBank(String filename) {
		// Attention ! Cette méthode écrit à la suite du fichier mis en
		// argument, pas par dessus !
		try {
			Files.write(Paths.get(filename), treebank.stream().map(tree -> tree.toString()).collect(Collectors.toList()),
					StandardOpenOption.APPEND);
			System.out.println("fichier créé");
		} catch (IOException ioe) {
			System.out.print("Erreur : ");
			ioe.printStackTrace();
		}
	}

	public Iterator<Tree> iterator() {
		return treebank.iterator();
	}

	public void exportSent(String out) {
		// Attention ! Cette méthode écrit à la suite du fichier mis en argument, pas par dessus !
		try {
			Files.write(Paths.get(out), treebank.stream().map(tree -> tree.toSentence()).collect(Collectors.toList()),
					StandardOpenOption.APPEND);
			System.out.println("fichier créé");
		} catch (IOException ioe) {
			System.out.print("Erreur : ");
			ioe.printStackTrace();
		}
	}

}