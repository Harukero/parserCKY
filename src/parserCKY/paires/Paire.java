package parserCKY.paires;

/**
 * Une classe de type Paire est une classe qui à un élément donné associe un autre élément
 * @author antoine,misun,xin
 *
 * @param <L>
 * @param <R>
 */
public interface Paire <L,R>{
	

	public String toString();
	
	/**
	 * Permet de récupérer le premier élément de la Paire
	 * @return l'élément de gauche
	 */
	public L getL();

	/**
	 * Permet de récupérer le second élément de la Paire
	 * @return l'élément de droite
	 */
	public R getR();
}
