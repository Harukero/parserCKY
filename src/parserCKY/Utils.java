package parserCKY;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Utils {
	public static String getFirstPartBefore(String label, char separator) {
		int separatorPos = label.indexOf(separator);
		if (separatorPos == -1) {
			return label;
		}
		if (separatorPos == 0) {
			return "";
		}
		return label.substring(0, separatorPos);
	}

	public static String getFirstPart(String label) {
		return getFirstPartBefore(label, ' ');
	}

	public static <T, R> void consumeCollectionToFile(String filename, Collection<T> liste,
			Function<? super T, String> mapper) {
		try {
			OutputStream os = Files.newOutputStream(Paths.get(filename), StandardOpenOption.CREATE,
					StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
			os.write(liste.stream().map(mapper).collect(Collectors.joining()).getBytes());
			System.out.println("fichier créé");
		} catch (IOException ioe) {
			System.out.print("Erreur : ");
			ioe.printStackTrace();
		}
	}

	public static void forEachLineInFile(String filename, Consumer<? super String> action) {
		try {
			Files.lines(Paths.get(filename)).forEach(action);
		} catch (Exception e) {
			System.out.print("Erreur : ");
			e.printStackTrace();
		}
	}

}
