package parserCKY;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

import parserCKY.grammar.ProbabilisticContextFreeGrammar;
import parserCKY.parser.ParserCKY;
import parserCKY.tree.Tree;
import parserCKY.treebank.Treebank;
import parserCKY.treebank.TreebankDependancy;

public class StartClass {

	private static final String HELP = "--help";
	private static final String PARSE = "--parse";
	private static final String DEP = "--dep";
	private static final String PARSE_DOC = "--parseDoc";

	public static void main(String[] args) throws IOException {
		int nb_args = args.length;
		if (args.length == 0) {
			usage();
			System.exit(0);
		}
		switch (args[0]) {
			case HELP:
				usage();
				break;
			case PARSE:
				if (nb_args == 2) {
					parse(args[1], false);
				} else if (nb_args == 3 && args[2].equals(DEP)) {
					parse(args[1], true);
				}
				break;
			case PARSE_DOC:
				if (nb_args == 4) {
					parseDocument(args[1], args[2], args[3], false);
				} else if (nb_args == 5 && args[4].equals(DEP)) {
					parseDocument(args[1], args[2], args[3], true);
				}
				break;
			default:
				usage();
				break;
		}
	}

	private static void usage() {
		System.out
				.println("Ce programme est un parser probabiliste parsant via l'algorithme CKY\n"
						+ "Options :\n"
						+ "\t--help : \taffiche cette aide\n"
						+ "\t--parse treebankfile [--dep]:\n "
						+ "\t\tparse une à une des phrases à écrire soit même une à une à partir d'un treebank à donner en argument.\n "
						+ "\t\tAjouter l'option --dep si votre treebank est en dépendance\n"
						+ "\t--parseDoc treebankfile file2parse outputfile [--dep] : \n"
						+ "\t\tParse ligne par ligne un document complet à partir d'un treebank, considéré en constituant par défaut.\n"
						+ "\t\tAjouter l'option --dep si votre treebank est en dépendance");
	}

	/**
	 * Cette méthode ouvre un fichier et vérifie pour chacune des phrases si
	 * elle est dans le langage<br>
	 * engendré par la grammaire extraite du treebank
	 * 
	 * @param treebank le nom d'un fichier contenant un treebank arboré
	 * @param inFilename un nom de fichier de test avec une phrase par ligne
	 * @param outFilename un nom de fichier où exporter le résultat
	 * @throws IOException
	 */
	public static void parseDocument(String treebank, String inFilename, String outFilename, boolean dep)
			throws IOException {
		Treebank tb;
		if (!dep)
			tb = new Treebank(treebank, 2);
		else
			tb = new TreebankDependancy(treebank);
		ProbabilisticContextFreeGrammar gramm = new ProbabilisticContextFreeGrammar(tb);
		FileReader fr = new FileReader(new File(inFilename));
		BufferedReader breader = new BufferedReader(fr);
		String line;
		Treebank toExport = new Treebank();
		try {
			while ((line = breader.readLine()) != null) {
				toExport.addTree(ParserCKY.parse(line, gramm));
			}
			breader.close();
			fr.close();
			toExport.exportTreeBank(outFilename);
		} catch (Exception e) {
			System.out.print("Erreur : ");
			e.printStackTrace();
			System.exit(0);
		} finally {
			breader.close();
			fr.close();
			toExport.exportTreeBank(outFilename);
		}
	}

	/**
	 * Cette méthode demande à l'utilisateur d'écrire des phrases et vérifie
	 * pour chacune si elle est dans le langage engendré par la grammaire
	 * extraite du treebank et affiche l'arbre syntaxique correspondant
	 * 
	 * @param treebank
	 *            un fichier contenant un treebank
	 */
	public static void parse(String treebank, boolean dep) {

		Treebank tb;
		if (!dep) {
			tb = new Treebank(treebank, 2);
		} else {
			tb = new TreebankDependancy(treebank);
		}
		ProbabilisticContextFreeGrammar gramm = new ProbabilisticContextFreeGrammar(tb);
		String sentence = " ";
		Scanner sc = new Scanner(System.in);
		System.out.println("prêt à tenter de parser votre phrase !");
		while (!(sentence = sc.nextLine()).equals("")) {
			Tree parsedTree = ParserCKY.parse(sentence, gramm);
			parsedTree.unBinarise();
		}
		sc.close();
	}

}