package be.unamur.info.b314.compiler;

import be.unamur.info.b314.compiler.exception.MapException;


import java.util.ArrayList;

/**
 * Classe qui vérifie si le fichier .map est bien construite.
 *
 * @author Maxime Allemeersch - maxime.allemeersch@student.unamur.be
 * @author Simon Dourte - simon.dourte@student.unamur.be
 * @author Benjamin Pans - benjamin.pans@student.unamur.be
 */
public class MapCheckFile {
    private int colone;
    private int ligne;
    private char[][] elements;
    private ArrayList<Character> charactersAccepted = new ArrayList<>();


    /**
     * Constructeur de la classe MapCheckFile.
     *
     * @param colone   Le nombre de colonnes du plateau.
     * @param ligne    Le nombre de lignes du plateau.
     * @param elements La liste des caractères du plateau.
     * @throws MapException Si la taille du plateau est incorrecte, si un caractère non accepté est présent, si un joueur ou un trésor est en double, ou si aucun joueur ou trésor n'est présent.
     */
    public MapCheckFile(int colone, int ligne, ArrayList<Character> elements) {
        this.colone = colone;
        this.ligne = ligne;
        this.elements = new char[ligne][colone];

        // Ajout d'éléments acceptés dans le tableau
        charactersAccepted.add('@'); //ROBOT: '@';
        charactersAccepted.add('X'); //TRESOR: 'X';
        charactersAccepted.add('G'); //PELOUSE: 'G';
        charactersAccepted.add('P'); //PALMIERS:'P';
        charactersAccepted.add('A'); //PONS: 'A';
        charactersAccepted.add('B'); //BUISSON: 'B';
        charactersAccepted.add('T'); //TONNEAU: 'T';
        charactersAccepted.add('S'); //PUITS: 'S';
        charactersAccepted.add('_'); //VIDE: '_';
        charactersAccepted.add('Q'); //SQUELETTE: 'Q';


        // Init les compteurs pour vérifier leur présence unique sur le plateau
        int compteurJoueur = 0;
        int compteurTresor = 0;
        int compteurSquelette = 0;

        // Check la taille du plateau
        if (elements.size() == colone * ligne) {
            // Parcourir la liste (le plateau)
            for (int i = 0; i < elements.size(); i++) {
                // On check si l'élément de la liste est bien contenu dans la liste des éléments acceptés
                // S'il n'est pas accepté, on affiche une erreur
                // Sinon on continue
                if (! charactersAccepted.contains(elements.get(i))) {
                    throw new MapException("MapCheckFile.MapCheckFile() : Caractère non accepté");
                }
                else {
                    // Si l'élément est un joueur
                    // On check que le compteur soit bien à 0
                    // Sinon on a un joueur en trop
                    if (elements.get(i) == '@') {
                        if (compteurJoueur == 0) {
                            compteurJoueur++;
                        }
                        else {
                            throw new MapException("MapCheckFile.MapCheckFile() : Trop de robots");
                        }
                    }
                    // Si c'est un trésor
                    // On check qu'il soit bien seul sur le plateau
                    // Sinon on a un trésor en trop
                    if (elements.get(i) == 'X') {
                        if (compteurTresor == 0) {
                            compteurTresor++;
                        }
                        else {
                            throw new MapException("MapCheckFile.MapCheckFile() : Trop de trésor");
                        }
                    }
                    // Si l'élément est un squelette
                    // On rajoute +1 au compteur
                    if (elements.get(i) == 'Q') {
                            compteurSquelette++;
                    }
                }
            }
            // Après la boucle, on check si on a bien au moins un robot et un trésor
            if (compteurJoueur == 0) {
                throw new MapException("MapCheckFile.MapCheckFile() : Aucun robot");
            }
            if (compteurTresor == 0) {
                throw new MapException("MapCheckFile.MapCheckFile() : Aucun tresor");
            }
            if (compteurSquelette == 0) {
                throw new MapException("MapCheckFile.MapCheckFile() : Aucun squelette");
            }
        }
        // Taille de la map n'est pas correcte
        else {
            throw new MapException("MapCheckFile.MapCheckFile() : Mauvaise taille de plateau");
        }
    }
}
