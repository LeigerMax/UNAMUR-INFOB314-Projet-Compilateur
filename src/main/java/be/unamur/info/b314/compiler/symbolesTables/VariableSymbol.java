package be.unamur.info.b314.compiler.symbolesTables;

/**
 * Classe représentant une variable dans la table des symboles.
 * Hérite de la classe Symbol.
 *
 * @author Maxime Allemeersch - maxime.allemeersch@student.unamur.be
 * @author Simon Dourte - simon.dourte@student.unamur.be
 * @author Benjamin Pans - benjamin.pans@student.unamur.be
 */
public class VariableSymbol extends Symbol {

    /**
     * Constructeur d'un symbole de type variable.
     *
     * @param name     le nom de la variable.
     * @param type     le type de la variable.
     * @param value    la valeur de la variable.
     * @param constant indique si la variable est une constante.
     */
    public VariableSymbol(String name,String type, Object value, Boolean constant) {
        super(name,type,value,constant);
    }


}
