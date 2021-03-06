package parserCKY.treebank;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import parserCKY.tree.Tree;

public class TreebankDependancy extends Treebank implements Iterable<Tree> {

	private List<Tree> treebankDep = new ArrayList<Tree>();

	public TreebankDependancy(String filename) {
		try {
			FileReader fr = new FileReader(new File(filename));
			BufferedReader breader = new BufferedReader(fr);
			List<String> mesLignes = new ArrayList<String>();
			String line;
			while ((line = breader.readLine()) != null) {
				if (!line.equals("")) {
					mesLignes.add(line);
				} else {
					Tree toAdd = Tree.tokenList2Tree(mesLignes);
					this.treebankDep.add(toAdd);
					mesLignes.clear();
				}
			}
			breader.close();
		} catch (Exception e) {
			System.out.print("Erreur : ");
			e.printStackTrace();
		}
	}

	public TreebankDependancy() {
		super();
	}

	public void exportTreeBank(String filePath) {
		// Attention ! Cette méthode écrit à la suite du fichier mis en argument, pas par dessus !
		try {
			FileWriter fw = new FileWriter(filePath, true);
			BufferedWriter output = new BufferedWriter(fw);
			for (Tree t : this)
				output.write(t.toString() + "\n");
			output.flush();
			output.close();
			System.out.println("fichier créé");
		} catch (IOException ioe) {
			System.out.print("Erreur : ");
			ioe.printStackTrace();
		}
	}

	public Iterator<Tree> iterator() {
		return this.treebankDep.iterator();
	}

}
