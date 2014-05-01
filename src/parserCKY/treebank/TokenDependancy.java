package parserCKY.treebank;

public class TokenDependancy {

	private int index, fatherIndex ;
	private String token, category ;

	public TokenDependancy (int indice, String token, String cat, int indicePere) {
		this.index = indice;
		this.token = token;
		this.category = cat;
		this.fatherIndex = indicePere;
	}

	public TokenDependancy (String ligne) {
		String[] tokenDep = ligne.split("\\t");
		this.token = tokenDep[1];
		this.category = tokenDep[4];
		this.index = Integer.valueOf(tokenDep[0]);
		this.fatherIndex = Integer.valueOf(tokenDep[6]);					
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
		this.fatherIndex = indicePere;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String cat) {
		this.category = cat;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int indice) {
		this.index = indice;
	}





}
