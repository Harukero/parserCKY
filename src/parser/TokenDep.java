package parser;

public class TokenDep {

	private int indice, indicePere ;
	private String token, cat ;

	public TokenDep (int indice, String token, String cat, int indicePere) {
		this.indice = indice;
		this.token = token;
		this.cat = cat;
		this.indicePere = indicePere;
	}

	public TokenDep (String ligne) {
		String[] tokenDep = ligne.split("\\t");
		this.token = tokenDep[1];
		this.cat = tokenDep[4];
		this.indice = Integer.valueOf(tokenDep[0]);
		this.indicePere = Integer.valueOf(tokenDep[6]);					
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public int getIndicePere() {
		return indicePere;
	}

	public void setIndicePere(int indicePere) {
		this.indicePere = indicePere;
	}

	public String getCat() {
		return cat;
	}

	public void setCat(String cat) {
		this.cat = cat;
	}

	public int getIndice() {
		return indice;
	}

	public void setIndice(int indice) {
		this.indice = indice;
	}





}
