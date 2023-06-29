package be.unamur.info.b314.compiler;

import be.unamur.info.b314.compiler.symbolesTables.FunctionSymbol;
import be.unamur.info.b314.compiler.symbolesTables.Symbol;

import java.util.HashMap;
import java.util.Map;

/**
 * Classe représentant une table des symboles.
 * Elle contient les informations sur les symboles et fonctions déclarés dans le code source.
 * Elle permet d'ajouter des symboles ou des fonctions dans la table, et de les récupérer.
 *
 * @author Maxime Allemeersch - maxime.allemeersch@student.unamur.be
 * @author Simon Dourte - simon.dourte@student.unamur.be
 * @author Benjamin Pans - benjamin.pans@student.unamur.be
 */
public class SymTable {

    //private String name = "";
    private String mapName;
    private Map<String, Symbol> symbols;
    private Map<String, FunctionSymbol> fonctions;
    private MapCheckFile mapCheckFile;


    /**
     * Constructeur de la classe.
     * Init la table des symboles et fonctions.
     */

    public SymTable(){
        this.symbols  = new HashMap<>();
        this.fonctions = new HashMap<>();

        mapName = "";
    }

    // =============
    //  GETTER
    // =============

    /**
     * @return la table des symbols.
     */
    public Map<String, Symbol> getSymbols() {
        return symbols;
    }

    /**
     * Obtient le symbole associé à un nom de symbole donné.
     *
     * @param symbol le nom du symbole.
     * @return le symbole associé au nom de symbole donné, ou null si le symbole n'existe pas.
     */
    public Symbol getSymbols(String symbol) {
        for(String key : symbols.keySet()) {
            if(key.equals(symbol)) {
                return symbols.get(symbol);
            }
        }
        return null;
    }


    /**
     * Obtient la fonction associée à un nom de fonction donné.
     *
     * @param fonction le nom de la fonction.
     * @return la fonction associée au nom de fonction donné, ou null si la fonction n'existe pas.
     */
    public FunctionSymbol getFonctions(String fonction) {
        for(String key : fonctions.keySet()) {
            if(key.equals(fonction)) {
                return fonctions.get(fonction);
            }
        }
        return null;
    }

    /**
     * @return la table des fonctions.
     */
    public Map<String, FunctionSymbol> getFonctions() {
        return fonctions;
    }

    /**
     * @return le nom de la table des symboles.
     */
    public String getName(){
        return mapName;
    }

    // =============
    //  SETTER
    // =============


    /**
     * Setter le nom de la carte courante.
     *
     * @param mapName le nom de la carte.
     */
    public void setMap(String mapName){
        this.mapName = mapName;
    }

    /**
     * Setter le fichier de vérification de carte.
     *
     * @param mapCheckFile le fichier de vérification de carte.
     */
    public void setMapCheckFile(MapCheckFile mapCheckFile){
        this.mapCheckFile = mapCheckFile;
    }

    // =============
    //  ADD
    // =============

    /**
     * Ajoute un symbole dans la table des symboles.
     *
     * @param name le nom du symbole.
     * @param symbol le symbole à ajouter.
     */
    public void addSymbol(String name, Symbol symbol) {
        symbols.put(name, symbol);
        showTable();
    }

    /**
     * Ajoute une fonction dans la table des fonctions.
     *
     * @param name le nom de la fonction.
     * @param functionSymbol la fonction à ajouter.
     */
    public void addFonction(String name, FunctionSymbol functionSymbol) {
        fonctions.put(name, functionSymbol);
        showFonction();
        System.out.println("Nb tableau :"+fonctions.size());
    }


    // =============
    //  SHOW
    // =============

    /**
     * Affiche la table des symboles
     *
     * @modifies System.Out
     * @effect affiche dans le terminal la table des symboles
     */
    public void showTable() {
        System.out.println("Symbols Tableau :");
        for(Map.Entry<String, Symbol> entry : symbols.entrySet()) {
            entry.getValue().showSymbol();
        }
    }

    /**
     * Affiche la table des fonctions
     *
     * @modifies System.Out
     * @effect affiche dans le terminal la table des fonctions
     */
    public void showFonction() {
        System.out.println("Fonction Tableau :");
        for(Map.Entry<String, FunctionSymbol> entry : fonctions.entrySet()) {
            entry.getValue().showFonction();
        }
    }



}