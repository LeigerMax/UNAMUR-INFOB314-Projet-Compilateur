package be.unamur.info.b314.compiler.symbolesTables;

/**
 *  Classe représentant un symbole dans la table des symboles.
 *
 *  @author Maxime Allemeersch - maxime.allemeersch@student.unamur.be
 *  @author Simon Dourte - simon.dourte@student.unamur.be
 *  @author Benjamin Pans - benjamin.pans@student.unamur.be
 */

public class Symbol {

    private String name;
    private String type;
    private Boolean constant = false;
    private Object value;
    private Boolean arrayBool = false;


    /**
     * Constructeur d'un symbole.
     *
     * @param name     Le nom du symbole.
     * @param type     Le type du symbole.
     * @param value    La valeur du symbole.
     * @param constant Indique si le symbole est une constante.
     */
    public Symbol(String name, String type, Object value, Boolean constant) {
        this.name = name;
        this.type = type;
        this.value = value;
        this.constant = constant;
    }


    /**
     * Constructeur d'un symbole sans valeur ni constante.
     *
     * @param name Le nom du symbole.
     */
    public Symbol(String name) {
        this.name = name;
    }

    /**
     * @return Le nom du symbole.
     */
    public String getName(){
        return name;
    }

    /**
     * @return Le type du symbole.
     */
    public String getType(){
        return type;
    }

    /**
     * @return La valeur du symbole.
     */
    public Object getValue(){
        return value;
    }

    /**
     * @return Vrai si le symbole est un tableau, faux sinon.
     */
    public Boolean isArray(){
        return arrayBool;
    }

    /**
     * @return Vrai si le symbole est une constante, faux sinon.
     */
    public Boolean isConstant(){
        return constant;
    }

    /**
     * Setter pour la valeur du symbole.
     *
     * @param value La nouvelle valeur du symbole.
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * Setter pour le type du symbole.
     *
     * @param type Le nouveau type du symbole.
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Setter pour indiquer si le symbole est un tableau.
     *
     * @param arrayBool Vrai si le symbole est un tableau, faux sinon.
     */
    public void setArray(Boolean arrayBool){
        this.arrayBool = arrayBool;
    }

    /**
     * Setter pour indiquer si le symbole est une constante.
     *
     * @param constant Vrai si le symbole est une constante, faux sinon.
     */
    public void setConstant(Boolean constant) {
        this.constant = constant;
    }

    /**
     * Affiche le symbole
     *
     * @modifies System.Out
     * @effect affiche dans le terminal les informations de l'objet Symbol
     */
    public void showSymbol(){
        System.out.println(" name : " +this.name+ " | Type : " +this.type+ "| Constante :" +this.constant+ "| Value : " +this.value+ "| arrayBool : " +this.arrayBool);
    }
}


