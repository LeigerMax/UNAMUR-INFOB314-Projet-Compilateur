package be.unamur.info.b314.compiler.symbolesTables;

/**
 * Classe représentant un array dans la table des symboles.
 * Hérite de la classe Symbol.
 *
 * @author Maxime Allemeersch - maxime.allemeersch@student.unamur.be
 * @author Simon Dourte - simon.dourte@student.unamur.be
 * @author Benjamin Pans - benjamin.pans@student.unamur.be
 */
public class ArraySymbol extends Symbol {

    private int line;
    private int col;
    private Symbol[][] val = new Symbol[line][col];


    /**
     * Constructeur d'un symbole de type array.
     *
     * @param name     le nom du array.
     * @param type     le type du array.
     * @param value    la valeur du array.
     * @param constant un booléen indiquant si le array est constant.
     * @param line     le nombre de lignes du array.
     * @param col      le nombre de colonnes du array.
     */
    public ArraySymbol(String name, String type, Object value, Boolean constant, int line, int col) {
        super(name, type, value, constant);
        this.line = line;
        this.col = col;
        setArray(true);
    }

    /**
     * @return le nombre de lignes du tableau.
     */
    public int getLines() {
        return line;
    }

    /**
     * @return le nombre de colonnes du tableau.
     */
    public int getCol() {
        return col;
    }

    /**
     * Setter pour le nombre de lignes du tableau.
     *
     * @param lines le nouveau nombre de lignes.
     */
    public void setLine(int lines) {
        this.line = lines;
    }

    /**
     * Setter pour le nombre de colonnes du tableau.
     *
     * @param col le nouveau nombre de colonnes.
     */
    public void setCol(int col) {
        this.col = col;
    }

    /**
     * Affiche les informations de l'objet ArraySymbol
     *
     * @modifies System.Out
     * @effect affiche dans le terminal les informations de l'objet ArraySymbol
     */
    public void showArray() {
        System.out.println(" name : " + this.getName() + " | Type : " + this.getType() + "| Constante :" + this.isConstant() + "| Value : " + this.getValue() + "| Ligne :" + this.line + "| Colonne : " + this.col);
    }


}
