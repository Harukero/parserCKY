package parserCKY;

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
}
