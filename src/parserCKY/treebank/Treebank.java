package parserCKY.treebank;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import parserCKY.tree.Tree;

/**
 * La classe de TreeBank représente un treebank, dans le sens le plus classique du terme, soit une collection d'arbres 
 * syntaxiques déjà annotés.
 * @author antoine,misun,xin
 *
 */
public class Treebank implements Iterable<Tree>{

	private Collection <Tree> treebank = new LinkedList<Tree>();
	private int markovDegree = 2; // par défaut markovisation de degré 2 en cas de binarisation


	public Treebank(){

	}

	/**
	 * Constructeur d'un TreeBank. Prend en argument un nom de fichier dont chacune<br>
	 * des lignes est une chaîne de caractères représentant un arbre<br>
	 * d'un treebank et construit la collection des arbres binaires correspondants à ces chaînes
	 * @param filename
	 */
	public Treebank(String filename){
		try {
			FileReader fr = new FileReader(new File(filename));
			BufferedReader breader=new BufferedReader(fr);
			String line;
			while ((line=breader.readLine())!=null){
				this.treebank.add(Tree.string2Tree(line));
			}
			breader.close();
		}
		catch (Exception e){
			System.out.print("Erreur : ");
			e.printStackTrace();
		}
	}

	/**
	 * Permet de contruire une instance de TreeBank en spécifiant le degré de markovisation
	 * @param filename fichier contenant un treebank
	 * @param degre degré de markovisation
	 */
	public Treebank (String filename, int degre){
		this(filename);
		this.markovDegree = degre;
	}


	public void addTree(Tree tree){
		this.treebank.add(tree);
	}

	/**
	 * Cette méthode permet de binariser le treebank entier, selon un certain degré de markovisation.
	 */
	public void binariseTreeBank(){
		for (Tree t : this){
			t.binarise(this.markovDegree);
		}
	}

	/**
	 * Cette méthode permet de débinariser le treebank entier.
	 */
	public void unBinariseTreeBank(){
		for (Tree t : this)
			t.unBinarise();
	}

	public String toString(){
		StringBuffer buffer = new StringBuffer();
		for (Tree t : this){
			buffer.append(t.toString()+"\n");
		}
		return buffer.toString();
	}

	/**
	 * Méthode d'instance. Prend un nom de fichier et <br>
	 * écrit dans ce fichier la représentation textuelle des arbres contenus dans le treebank 
	 * @param nomFic
	 */
	public void exportTreeBank (String nomFic){
		// Attention ! Cette méthode écrit à la suite du fichier mis en argument, pas par dessus !
		try{
			FileWriter fw = new FileWriter(nomFic, true);
			BufferedWriter output = new BufferedWriter(fw);
			for (Tree t : this)
				output.write(t.toString()+"\n");
			output.flush();
			output.close();
			System.out.println("fichier créé");
		}
		catch(IOException ioe){
			System.out.print("Erreur : ");
			ioe.printStackTrace();
		}
	}

	public Iterator<Tree> iterator() {
		return this.treebank.iterator();
	}

	public void exportSent(String out) {
		// Attention ! Cette méthode écrit à la suite du fichier mis en argument, pas par dessus !
		try{
			FileWriter fw = new FileWriter(out, true);
			BufferedWriter output = new BufferedWriter(fw);
			for (Tree t : this)
				output.write(t.toSentence()+"\n");
			output.flush();
			output.close();
			System.out.println("fichier créé");
		}
		catch(IOException ioe){
			System.out.print("Erreur : ");
			ioe.printStackTrace();
		}
	}

}