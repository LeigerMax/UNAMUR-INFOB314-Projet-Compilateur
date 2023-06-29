package be.unamur.info.b314.compiler.symbolesTables;

/**
 * Classe représentant un typeDef dans la table des symboles.
 * Hérite de la classe Symbol.
 *
 * @author Maxime Allemeersch - maxime.allemeersch@student.unamur.be
 * @author Simon Dourte - simon.dourte@student.unamur.be
 * @author Benjamin Pans - benjamin.pans@student.unamur.be
 */
public class TypedefSymbol extends Symbol{

    /**
     * Constructeur d'un symbole de type typeDef
     *
     * @param name      le nom de typeDef
     * @param type      le type de typeDef
     * @param value     la valeur de typeDef
     * @param constant  un booléen indiquant si le typeDef est constant.
     */
    public TypedefSymbol(String name, String type, Object value, Boolean constant) {
        super(name,type,value,constant);
    }
}
