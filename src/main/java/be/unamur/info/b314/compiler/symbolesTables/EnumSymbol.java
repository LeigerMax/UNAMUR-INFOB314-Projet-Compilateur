package be.unamur.info.b314.compiler.symbolesTables;

import java.util.ArrayList;

/**
 * Classe représentant une enumération dans la table des symboles.
 * Hérite de la classe Symbol.
 *
 * @author Maxime Allemeersch - maxime.allemeersch@student.unamur.be
 * @author Simon Dourte - simon.dourte@student.unamur.be
 * @author Benjamin Pans - benjamin.pans@student.unamur.be
 */
public class EnumSymbol extends Symbol {

    /**
     * Constructeur d'un symbole de type enumération.
     *
     * @param name     le nom de l'énumaration.
     * @param enumList la liste des valeurs de l'énumération.
     */
    public EnumSymbol(String name, ArrayList<String> enumList) {
        super(name);
        setValue(enumList);
        setType("enum");
        setConstant(true);
    }

}
