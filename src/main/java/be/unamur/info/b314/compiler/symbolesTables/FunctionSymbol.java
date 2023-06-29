package be.unamur.info.b314.compiler.symbolesTables;

import java.util.ArrayList;

/**
 * Classe représentant une fonction dans la table des symboles.
 * Hérite de la classe Symbol.
 *
 * @author Maxime Allemeersch - maxime.allemeersch@student.unamur.be
 * @author Simon Dourte - simon.dourte@student.unamur.be
 * @author Benjamin Pans - benjamin.pans@student.unamur.be
 */
public class FunctionSymbol extends Symbol {

    private be.unamur.info.b314.compiler.PlayPlusParser.FctDeclContext context;
    private ArrayList<String> argumentsList;
    private ArrayList<String> argumentsTypeList;
    private ArrayList<String> argumentsNameList;
    private int porte;

    /**
     * Constructeur d'un symbole de type fonction.
     *
     * @param name     le nom de la fonction.
     * @param type     le type de retour de la fonction.
     * @param porte    la portée de la fonction.
     * @param value    la valeur de la fonction.
     * @param constant indique si la fonction est une constante.
     * @param context  le contexte de la déclaration de la fonction.
     */
    public FunctionSymbol(String name,String type, int porte, Object value, Boolean constant, be.unamur.info.b314.compiler.PlayPlusParser.FctDeclContext context ) {
        super(name,type,value,constant);
        this.context = context;
        this.argumentsList = new ArrayList<>();
        this.porte = porte;
    }


    /**
     * @return la portée de la fonction.
     */
    public int getPorte(){
        return porte;
    }

    /**
     * @return la liste des arguments de la fonction.
     */
    public ArrayList<String> getArguments() {
        return argumentsList;
    }

    /**
     * @return la liste des types des arguments de la fonction.
     */
    public ArrayList<String> getArgumentsType() {
        return argumentsTypeList;
    }

    /**
     * @return la liste des noms des arguments de la fonction.
     */
    public ArrayList<String> getArgumentsName() {
        return argumentsNameList;
    }


    /**
     * Setter pour la liste des arguments de la fonction.
     *
     * @param argumentsList la liste des arguments de la fonction.
     */
    public void setargumentsList(ArrayList<String> argumentsList){
        this.argumentsList = argumentsList;
    }

    /**
     * Setter pour la liste des types des arguments de la fonction.
     *
     * @param argumentsTypeList la liste des types des arguments de la fonction.
     */
    public void setargumentsListType(ArrayList<String>  argumentsTypeList) {
        this.argumentsTypeList = argumentsTypeList;
    }

    /**
     * Setter pour la liste des noms des arguments de la fonction.
     *
     * @param argumentsNameList la liste des noms des arguments de la fonction.
     */
    public void setargumentsListName(ArrayList<String>  argumentsNameList) {
        this.argumentsNameList = argumentsNameList;
    }

    /**
     * Affiche les informations de la fonction.
     *
     * @modifies System.Out
     * @effect affiche dans le terminal les informations de l'objet fonction
     */
    public void showFonction(){
        System.out.println(" name : " + this.getName() + " | Type : " +this.getType()+ "| Constante :" +this.isConstant()+ "| Value : " +this.getValue()+ "| Porté :"+this.getPorte() );
    }
}
