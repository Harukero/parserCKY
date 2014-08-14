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

public class StartClass {

	private static final String HELP = "--help";
	private static final String PARSE = "--parse";
	private static final String PARSE_DOC = "--parseDoc";

	public static void main(String[] args) throws IOException {
		int nb_args = args.length;
		if (args.length == 0) {
			usage();
			return;
		}
		switch (args[0]) {
			case HELP:
				usage();
				break;
			case PARSE:
				parse(IConstants.SEQUOIA_CORPUS_PATH);
				break;
			case PARSE_DOC:
				if (nb_args == 3) {
					parseDocument(IConstants.SEQUOIA_CORPUS_PATH, args[2], args[3]);
				} else {
					usage();
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
						+ "\t--parse :\n "
						+ "\t\tparse une à une des phrases à écrire soit même une à une à partir d'un treebank à donner en argument.\n "
						+ "\t--parseDoc file2parse outputfile : \n"
						+ "\t\tParse ligne par ligne un document complet à partir d'un treebank, considéré en constituant par défaut.\n");
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
	public static void parseDocument(String treebank, String inFilename, String outFilename) throws IOException {
		Treebank tb = new Treebank(treebank);
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
	public static void parse(String treebank) {

		Treebank tb = new Treebank(treebank);
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