package parserCKY.treebank;

public class TokenDependancy {

	private int index, fatherIndex;
	private String token, category;

	public TokenDependancy(int index, String token, String category, int fatherIndex) {
		this.index = index;
		this.token = token;
		this.category = category;
		this.fatherIndex = fatherIndex;
	}

	public TokenDependancy(String line) {
		String[] tokenDep = line.split("\\t");
		token = tokenDep[1];
		category = tokenDep[4];
		index = Integer.valueOf(tokenDep[0]);
		fatherIndex = Integer.valueOf(tokenDep[6]);
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public int getFatherIndex() {
		return fatherIndex;
	}

	public void setFatherIndex(int indicePere) {
		fatherIndex = indicePere;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String cat) {
		category = cat;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int indice) {
		index = indice;
	}

}
