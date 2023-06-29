package be.unamur.info.b314.compiler.symbolesTables;

import be.unamur.info.b314.compiler.PlayPlusParser;

import java.util.ArrayList;

/**
 * Classe représentant un struct dans la table des symboles.
 * Hérite de la classe Symbol.
 *
 * @author Maxime Allemeersch - maxime.allemeersch@student.unamur.be
 * @author Simon Dourte - simon.dourte@student.unamur.be
 * @author Benjamin Pans - benjamin.pans@student.unamur.be
 */
public class StructSymbol extends Symbol {

    private int porte; //on peut avoir un struct dans un struct
    private ArrayList<String> listeName;

    private PlayPlusParser.StructDeclContext context;

    /**
     * Constructeur d'un symbole de type struct.
     *
     * @param name       le nom du struct.
     * @param type       le type du struct.
     * @param value      la valeur du struct.
     * @param constant   un booléen indiquant si le struct est constant.
     * @param porte      la portée du struct.
     * @param listeName  la liste des noms des champs du struct.
     */
    public StructSymbol(String name, String type, Object value, Boolean constant, int porte, ArrayList<String> listeName, PlayPlusParser.StructDeclContext context) {
        super(name, type, value, constant);
        this.porte = porte;
        this.listeName = listeName;
        this.context = context;
    }

    /**
     * @return la portée du struct
     */
    public int getPorte() {
        return porte;
    }

    /**
     * Setter pour la portée du struct
     *
     * @param porte la nouvelle portée du struct
     */
    public void setPorte(int porte) {
        this.porte = porte;
    }

    /**
     * @return la liste des noms des champs du struct.
     */
    public ArrayList<String> getListeName() {
        return listeName;
    }

    /**
     * @return le contexte.
     */
    public PlayPlusParser.StructDeclContext getContext() {
        return context;
    }


}
