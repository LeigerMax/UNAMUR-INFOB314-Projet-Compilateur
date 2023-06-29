package be.unamur.info.b314.compiler;

import be.unamur.info.b314.compiler.exception.*;
import be.unamur.info.b314.compiler.symbolesTables.*;
import com.google.common.collect.Maps;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.*;


/**
 * Remplit une table de symboles à l'aide de l'écouteur ANTLR.
 *
 * @author James Ortiz - james.ortizvega@unamur.be
 * @author Maxime Allemeersch - maxime.allemeersch@student.unamur.be
 * @author Simon Dourte - simon.dourte@student.unamur.be
 * @author Benjamin Pans - benjamin.pans@student.unamur.be
 */
public class SymTableFiller extends PlayPlusBaseListener {


    private final Map<Integer, SymTable> symTable;

    private int currentPorte; // Portée actuel
    private int maxPorte; // Portée max crée
    private int structPorte = 0; // Porté actuel du struct
    private int maxStructPorte = 0; // Porté max du struct
    private FunctionSymbol currentFunction;
    private Symbol ExprGId;

    private ArrayList<String> argumentsList;
    private ArrayList<String> argumentsListType;
    private Boolean VarIdok = false; //Check si la variable à bien été init
    private Boolean fctOk = false;

    private int expr1;

    private int expr2;




    /**
     * Constructeur de la classe SymTableFiller.
     * Initialise la symTable avec une nouvelle HashMap.
     * Définis la portée courante à 0.
     * Définis la portée maximale courant à 0.
     * Ajoute une nouvelle SymTable à la porté courante.
     */
    public SymTableFiller() {
        this.symTable = Maps.newHashMap();
        currentPorte = 0;
        maxPorte = 0;
        symTable.put(currentPorte, new SymTable());
    }


    // =============
    //  NO OVERRIDE
    // =============

    /**
     * Ajoute une variable à la table des symboles pour la portée actuel.
     * Si le nom de la variable est "import" ou "main", une exception est levée.
     * Si un symbole existe déjà avec le même nom pour la portée actuelle, une exception est levée.
     * Si le symbole est de type EnumSymbol, sa valeur est initialisée avec la liste.
     *
     * @param name     le nom de la variable à ajouter.
     * @param symbol   le symbole à ajouter à la table des symboles.
     * @throws VariableNameException   si le nom de la variable est "import" ou "main", ou si le nom de la variable existe déjà pour la portée actuelle ou globale.
     */
    private void addVariable(String name, Symbol symbol) {
        if(currentFunction != null) {
            // Check si name est import ou main
            if(name.equals("import") || name.equals("main")) {
                throw new VariableNameException("SymTableFiller.addVariable() : Le nom ne peut pas être import ou main :" + name);
            }

            // Check si variable présent dans la fonction local (currentPorte)
            else if(symTable.get(currentPorte).getSymbols().containsKey(name)) {
                throw new VariableNameException("SymTableFiller.addVariable() : Nom de variable déjà existant dans la table de symbole global  :" + name);
            }

            // Check si variable présent dans la table de symbole globale
            //   else if(symTable.get(0).getSymbols().containsKey(name)) {
            //       throw new VariableNameException("Nom de variable déjà existant dans la table de symbole global :" +name);
            //   }


            // Check si nom différent du nom de la fonction où il se trouve
            else if(symTable.get(0).getFonctions().containsKey(name)) {
                if(symTable.get(0).getFonctions().get(name).getPorte() == currentPorte) {
                    throw new VariableNameException("SymTableFiller.addVariable() :  Nom de variable déjà existant, utilisé par la fonction où se trouve la variable :" + name);
                }
            }

            // Check si type null ou non init (EXPRESSION ???)
            else if(symbol.getType() == null) {
                throw new VariableNameException("SymTableFiller.addVariable() :  Type de variable pas init :" + name);
            }

            // Si tout est OK
            else {
                symTable.get(currentPorte).addSymbol(name, symbol);
            }
        }
        // Check si nom déja utilisé par une fonction (nom fonction)
        else if(currentPorte == 0 && (symTable.get(0).getFonctions().containsKey(name) || symTable.get(0).getSymbols().containsKey(name))) {
            throw new VariableNameException("SymTableFiller.addVariable() : Nom de variable déjà existant, utilisé par une fonction :" + name);
        }
        else {
            symTable.get(currentPorte).addSymbol(name, symbol);
        }

    }


    /**
     * Ajoute une fonction avec un nom et un symbole à la table des symboles.
     *
     * @param name               Le nom de la fonction.
     * @param functionSymbol     Le symbole représentant la fonction.
     * @throws FonctionException si le nom est déjà utilisé par une fonction ou une variable.
     */
    private void addFunction(String name, FunctionSymbol functionSymbol) {

        // Check si nom déja utilisé par une fonction
        if(symTable.get(0).getFonctions().containsKey(name)) {
            throw new FonctionException("SymTableFiller.addFunction : Nom déjà utilisé par une fonction" + name);
        }

        // Check si nom déja utilisé par une variable
        else if(symTable.get(0).getSymbols().containsKey(name)) {
            throw new FonctionException("SymTableFiller.addFunction : Nom déjà utilisé par une variable" + name);
        }

        // Si tout est OK
        else {
            symTable.get(currentPorte).addFonction(name, functionSymbol);
        }
    }


    // =============
    //  OVERRIDE
    // =============


    /**
     * Méthode appelée lors de l'entrée dans le contexte "rootMap" du parseur.
     * Récupère les informations de la carte et va les tester dans le fichier MapCheckFile
     *
     * @param ctx le contexte "rootMap" du parseur contenant les informations de la carte à stocker.
     */
    @Override
    public void enterRootMap(PlayPlusParser.RootMapContext ctx) {
        List<ParseTree> listComplet = ctx.map().children;

        int ligne = Integer.parseInt(listComplet.get(2).getText());
        int colone = Integer.parseInt(listComplet.get(3).getText());

        // Récupère la liste des éléments de la carte
        ArrayList<Character> listElement = new ArrayList<>();
        for(int i = 4; i < listComplet.size(); i++) {
            listElement.add(listComplet.get(i).getText().charAt(0));
        }

        MapCheckFile mapCheck = new MapCheckFile(colone, ligne, listElement);
        symTable.get(currentPorte).setMapCheckFile(mapCheck);
    }

    /**
     * Méthode appelée lorsqu'on entre dans le contexte d'une déclaration de fichier.
     * Récupère le nom du fichier et le stocke dans la table des symboles.
     *
     * @param ctx le contexte de la déclaration de fichier
     */
    @Override
    public void enterFileDecl(PlayPlusParser.FileDeclContext ctx) {
        symTable.get(currentPorte).setMap(ctx.getText());
        super.enterFileDecl(ctx);
    }

    /**
     * Méthode appelée lors de l'entrée dans le contexte "main" du parseur.
     * Crée un symbole de fonction pour la fonction principale et l'ajoute à la table des symboles
     * Met à jour la portée courante et la portée max en les incrémentants
     *
     * @param ctx le contexte "main" du parseur
     */
    @Override
    public void enterMain(PlayPlusParser.MainContext ctx) {
        maxPorte++;
        FunctionSymbol functionSymbol = new FunctionSymbol(ctx.MAIN().getText(), "void", maxPorte, null, false, null);
        addFunction(ctx.MAIN().getText(), functionSymbol);
        symTable.put(maxPorte, new SymTable());
        currentPorte = maxPorte;
    }

    /**
     * Méthode appelée lors de la sortie du contexte "main" du parseur.
     * Met à jour la portée courante en la décrémentant.
     *
     * @param ctx le contexte "main" du parseur
     */
    @Override
    public void exitMain(PlayPlusParser.MainContext ctx) {
        currentPorte--;
    }

    /**
     * Methode qui est appelée lors de la déclaration d'une variable/array du parseur.
     * Elle permet de gérer les cas de déclaration de variable simple et d'array.
     *
     * @param ctx le contexte "var" du parseur
     * @throws ArraysException si type dans array incorrect ou si la taille de l'array n'est pas correcte
     */
    @Override
    public void enterVarDecl(PlayPlusParser.VarDeclContext ctx) {
        Boolean constant = false;
        Boolean arraySolo = false; //Si nous avons l'init de l'array, sans la liste

        // On ajoute le 0 pour éviter d'avoir les [] avec l'ID
        String varDeclId = ctx.ID(0).toString();
        String varDeclType = ctx.type().scalar().getText();
        String varValue = null;

        // On init une liste contenant tous les types possibles (pour check type)
        ArrayList<String> expectedTypes = new ArrayList<>(Arrays.asList("int", "char", "bool"));

        // Scotch qui split le ctx.getText() en deux sur base du char "=", on récupère seulement la deuxième partie (value)
        String value = ctx.getText();
        String[] parts = value.split("=");

        if(ctx.getText().contains("[")){
            arraySolo = true;
        }

        // On check que la taille de la liste soit bien au moins à 2, sinon value est vide
        if(parts.length >= 2) {
            // la valeur de la variable est la deuxième partie de la chaîne
            // la méthode trim() est utilisée pour supprimer les espaces en début et fin de la deuxième partie
            varValue = parts[1].trim();
            // On retire le ";"
            varValue = varValue.substring(0, varValue.length() - 1);

        }

        // On gère le cas où c'est une array que l'on veut crée
        // Init les variables pour ligne et colonne
        int nbLine = 0;
        int nbCol = 0;

        // arraySym contiendra le symbole array
        ArraySymbol arraySym = new ArraySymbol(varDeclId, varDeclType, varValue, constant,nbLine, nbCol);
        List<ParseTree> children = ctx.children;
        // on va parcourir tout ce qu'il y a dans var decl
        for(ParseTree child : children) {
            // On ne garde que les arrays
            if(child instanceof PlayPlusParser.ArraysContext) {
                if(parts.length > 1) { // ICI j'avais une erreur ArrayIndexOutOfBoundsException: Index 1 out of bounds for length 1, j'ai fais une condition, car parts[1] était le problème
                    // on doit redéfinir varValue pour ne pas interférer avec celui de var tout court
                    varValue = parts[1].trim();

                    // On retire le ";"
                    varValue = varValue.substring(0, varValue.length() - 1);
                    // On récupère l'array
                    String arrayTest = child.getText();
                    //System.out.println("Test Taille array : " + arrayTest);
                    // On enlève les crochets
                    String arrayContent = arrayTest.substring(1, arrayTest.length() - 1);
                    System.out.println("Contenu de l'array : " + arrayContent);

                    // On vérifie si on est dans le cas d'une array simple
                    if(arrayContent.length() == 1) {
                        nbLine = arrayContent.length();
                        nbCol = Integer.parseInt(arrayContent);
                        System.out.println("Array à " + nbLine + " ligne et à " + nbCol + " colonnes");
                        // On transforme le String en Array
                        // On enlève les {} et on split avec ","
                        String[] strArray = varValue.substring(1, varValue.length() - 1).split(",");

                        // On va devoir trouver le type d'array pour cela
                        // On doit faire en fonction de chaque type possible
                        if(expectedTypes.contains(varDeclType)) {
                            if(varDeclType.equals("int")) {
                                // Init array int et une liste contenant tous les
                                int[] typeArray = new int[strArray.length];
                                ArrayList<String> chiffres = new ArrayList<>(Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9"));
                                // On ajoute les éléments dans l'array
                                for(int i = 0; i < strArray.length; i++) {
                                    // On check si l'élément est bien un entier
                                    if(chiffres.contains(strArray[i])) {
                                        typeArray[i] = Integer.parseInt(strArray[i]);
                                    }
                                    else {
                                        throw new ArraysException("SymTableFiller.enterVarDecl() : L'array contient un élément non entier "+varDeclId);
                                    }
                                }
                                // Check si les tailles sont correctes
                                if(typeArray.length == nbCol) {
                                    arraySym.setCol(nbCol);
                                } else {
                                    throw new ArraysException("SymTableFiller.enterVarDecl() : La taille de l'array n'est pas correcte "+varDeclId);
                                }
                            } else if(varDeclType.equals("char")) {
                                // Init array char
                                String[] typeArray = new String[strArray.length];
                                // On ajoute les éléments dans l'array
                                for(int i = 0; i < strArray.length; i++) {
                                    // Pas besoin de check si l'élément est bien un String car tout peut être un String
                                    typeArray[i] = strArray[i];
                                }
                                if(typeArray.length == nbCol) {
                                    arraySym.setCol(nbCol);
                                }
                                else {
                                    throw new ArraysException("SymTableFiller.enterVarDecl() : La taille de l'array n'est pas correcte "+varDeclId);
                                }
                            } else if(varDeclType.equals("bool")) {
                                // Init array bool
                                boolean[] typeArray = new boolean[strArray.length];
                                // On ajoute les éléments dans l'array
                                for(int i = 0; i < strArray.length; i++) {
                                    System.out.println("Test array : " + strArray[i]);
                                    // Pour vérifier qu'on a bien un élément bool, on check si c'est true ou false
                                    if(strArray[i].equals("true") || strArray[i].equals("false")) {
                                        typeArray[i] = Boolean.parseBoolean(strArray[i]);
                                    }
                                    else {
                                        throw new ArraysException("SymTableFiller.enterVarDecl() : L'array contient un élément non booléen "+varDeclId);
                                    }
                                }
                                // Check si les tailles sont correctes
                                System.out.println("test size array : " + typeArray.length + nbCol);
                                if(typeArray.length == nbCol) {
                                    arraySym.setCol(nbCol);
                                }
                                else {
                                    throw new ArraysException("SymTableFiller.enterVarDecl() : La taille de l'array n'est pas correcte "+varDeclId);
                                }
                            }
                            else {
                                throw new ArraysException("SymTableFiller.enterVarDecl() : Type incorrect "+varDeclId);
                            }

                        }
                    }
                    // Ou dans le cas d'une array qui contient des arrays
                    // Faut encore gérer le fait d'avoir les bons types (comme en haut mais essayer de factoriser)
                    else {
                        // On transforme le String en Array
                        // On enlève les { et }
                        String[] arraysStr = varValue.substring(2, varValue.length() - 2).split("},\\s*\\{");

                        for(int i = 0; i < arraysStr.length; i++) {
                            // On enlève les { et } de chaque array
                            String[] strArray = arraysStr[i].split(",");
                            if(varDeclType.equals("int")) {
                                int[][] intArrays = new int[arraysStr.length][];
                                intArrays[i] = new int[strArray.length];
                                for(int j = 0; j < strArray.length; j++) {
                                    // On ajoute chaque élément booléen dans l'array de booleans
                                    intArrays[i][j] = Integer.parseInt(strArray[j]);
                                }
                            }
                            if(varDeclType.equals("char")) {
                                String[][] charArrays = new String[arraysStr.length][];
                                charArrays[i] = new String[strArray.length];
                                for(int j = 0; j < strArray.length; j++) {
                                    // On ajoute chaque élément booléen dans l'array de booleans
                                    charArrays[i][j] = strArray[j];
                                }
                            }
                            if(varDeclType.equals("bool")) {
                                // Init array pour les arrays de booleans
                                boolean[][] boolArrays = new boolean[arraysStr.length][];
                                boolArrays[i] = new boolean[strArray.length];
                                for(int j = 0; j < strArray.length; j++) {
                                    // On ajoute chaque élément booléen dans l'array de booleans
                                    boolArrays[i][j] = Boolean.parseBoolean(strArray[j]);
                                }
                            }

                        }
                        // Check si on a bien un array qui contient des arrays
                        //if (boolArrays.getClass().isArray() && boolArrays.getClass().getComponentType().isArray()) {
                        //    System.out.println("C'est un tableau multidimensionnel");
                        //} else {
                        //    System.out.println("Ce n'est pas un tableau multidimensionnel");
                        //}
                        // On split pour récupérer la taille des arrays
                        String[] arrayParts = arrayContent.split(",");
                        String first = arrayParts[0];
                        String last = arrayParts[1];
                        // Passage de str en int
                        nbLine = Integer.parseInt(first);
                        nbCol = Integer.parseInt(last);

                        System.out.println("Array à " + nbLine + " lignes et à " + nbCol + " colonnes");
                        /*
                        // Check si les tailles sont correctes
                        if (boolArrays.length == nbLine) {

                            // On init un compteur pour être sûr que chaque array a la bonne taille
                            int counter = 0;
                            // Si l'array a la bonne taille, on incrémente le compteur
                            for (int i = 0; i < boolArrays.length; i++){
                                if (boolArrays[i].length == nbCol){
                                    counter++;
                                }
                                // On ajoute une sécurité
                                // Exemple : le compteur est à 4 mais il y a 5 arrays dont 1 avec la mauvaise taille,
                                // donc on décrémente de 1 le compteur
                                else {
                                    counter--;
                                }
                            }
                            // Si le compteur est bien égal au nombre de colonne, c'est que le nombre de colonne est correct
                            if (counter == nbLine){
                                arraySym.setLine(nbLine);
                            }
                            else {
                                throw new ArraysException("Le nombre de colonnes de l'array n'est pas correcte");
                            }
                        }
                        else {
                            throw new ArraysException("Le nombre de lignes de l'array n'est pas correcte");
                        }*/
                    }
                }
            }
        }
        // Si array
        if((nbLine != 0 && nbCol != 0) || arraySolo) {
            VariableSymbol symbol = new VariableSymbol(varDeclId, varDeclType, arraySym, constant);
            symbol.setArray(true);
            addVariable(varDeclId, symbol);
        }
        // Si variable
        else {
            VariableSymbol symbol = new VariableSymbol(varDeclId, varDeclType, varValue, constant);
            addVariable(varDeclId, symbol);
        }
    }

    /**
     * Methode qui est appelée lors de la déclaration d'une const variable/array du parseur.
     * Elle permet de gérer les cas de déclaration de variable simple et d'array.
     *
     * @param ctx le contexte "var" du parseur
     * @throws ArraysException si type dans array incorrect ou si la taille de l'array n'est pas correcte
     */
    @Override
    public void enterConstDecl(PlayPlusParser.ConstDeclContext ctx) {
        Boolean constant = true;
        Boolean arraySolo = false; //Si nous avons l'init de l'array, sans la liste

        // On ajoute le 0 pour éviter d'avoir les [] avec l'ID
        String varDeclId = ctx.ID(0).toString();
        String varDeclType = ctx.type().scalar().getText();
        String varValue = null;

        // On init une liste contenant tous les types possibles (pour check type)
        ArrayList<String> expectedTypes = new ArrayList<>(Arrays.asList("int", "char", "bool"));

        // Scotch qui split le ctx.getText() en deux sur base du char "=", on récupère seulement la deuxième partie (value)
        String value = ctx.getText();
        String[] parts = value.split("=");

        if(ctx.getText().contains("[")){
            arraySolo = true;
        }

        // On check que la taille de la liste soit bien au moins à 2, sinon value est vide
        if(parts.length >= 2) {
            // la valeur de la variable est la deuxième partie de la chaîne
            // la méthode trim() est utilisée pour supprimer les espaces en début et fin de la deuxième partie
            varValue = parts[1].trim();
            // On retire le ";"
            varValue = varValue.substring(0, varValue.length() - 1);

        }

        // On gère le cas où c'est une array que l'on veut crée
        // Init les variables pour ligne et colonne
        int nbLine = 0;
        int nbCol = 0;

        // arraySym contiendra le symbole array
        ArraySymbol arraySym = new ArraySymbol(varDeclId, varDeclType, varValue, constant,nbLine, nbCol);
        List<ParseTree> children = ctx.children;
        // on va parcourir tout ce qu'il y a dans var decl
        for(ParseTree child : children) {
            // On ne garde que les arrays
            if(child instanceof PlayPlusParser.ArraysContext) {
                if(parts.length > 1) { // ICI j'avais une erreur ArrayIndexOutOfBoundsException: Index 1 out of bounds for length 1, j'ai fais une condition, car parts[1] était le problème
                    // on doit redéfinir varValue pour ne pas interférer avec celui de var tout court
                    varValue = parts[1].trim();

                    // On retire le ";"
                    varValue = varValue.substring(0, varValue.length() - 1);
                    // On récupère l'array
                    String arrayTest = child.getText();
                    //System.out.println("Test Taille array : " + arrayTest);
                    // On enlève les crochets
                    String arrayContent = arrayTest.substring(1, arrayTest.length() - 1);
                    System.out.println("Contenu de l'array : " + arrayContent);

                    // On vérifie si on est dans le cas d'une array simple
                    if(arrayContent.length() == 1) {
                        nbLine = arrayContent.length();
                        nbCol = Integer.parseInt(arrayContent);
                        System.out.println("Array à " + nbLine + " ligne et à " + nbCol + " colonnes");
                        // On transforme le String en Array
                        // On enlève les {} et on split avec ","
                        String[] strArray = varValue.substring(1, varValue.length() - 1).split(",");

                        // On va devoir trouver le type d'array pour cela
                        // On doit faire en fonction de chaque type possible
                        if(expectedTypes.contains(varDeclType)) {
                            if(varDeclType.equals("int")) {
                                // Init array int et une liste contenant tous les
                                int[] typeArray = new int[strArray.length];
                                ArrayList<String> chiffres = new ArrayList<>(Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9"));
                                // On ajoute les éléments dans l'array
                                for(int i = 0; i < strArray.length; i++) {
                                    // On check si l'élément est bien un entier
                                    if(chiffres.contains(strArray[i])) {
                                        typeArray[i] = Integer.parseInt(strArray[i]);
                                    }
                                    else {
                                        throw new ArraysException("SymTableFiller.enterConstDecl() : L'array contient un élément non entier "+varDeclId);
                                    }
                                }
                                // Check si les tailles sont correctes
                                if(typeArray.length == nbCol) {
                                    arraySym.setCol(nbCol);
                                } else {
                                    throw new ArraysException("SymTableFiller.enterConstDecl() : La taille de l'array n'est pas correcte "+varDeclId);
                                }
                            } else if(varDeclType.equals("char")) {
                                // Init array char
                                String[] typeArray = new String[strArray.length];
                                // On ajoute les éléments dans l'array
                                for(int i = 0; i < strArray.length; i++) {
                                    // Pas besoin de check si l'élément est bien un String car tout peut être un String
                                    typeArray[i] = strArray[i];
                                }
                                if(typeArray.length == nbCol) {
                                    arraySym.setCol(nbCol);
                                }
                                else {
                                    throw new ArraysException("SymTableFiller.enterConstDecl() : La taille de l'array n'est pas correcte "+varDeclId);
                                }
                            } else if(varDeclType.equals("bool")) {
                                // Init array bool
                                boolean[] typeArray = new boolean[strArray.length];
                                // On ajoute les éléments dans l'array
                                for(int i = 0; i < strArray.length; i++) {
                                    System.out.println("Test array : " + strArray[i]);
                                    // Pour vérifier qu'on a bien un élément bool, on check si c'est true ou false
                                    if(strArray[i].equals("true") || strArray[i].equals("false")) {
                                        typeArray[i] = Boolean.parseBoolean(strArray[i]);
                                    }
                                    else {
                                        throw new ArraysException("SymTableFiller.enterConstDecl() : L'array contient un élément non booléen "+varDeclId);
                                    }
                                }
                                // Check si les tailles sont correctes
                                System.out.println("test size array : " + typeArray.length + nbCol);
                                if(typeArray.length == nbCol) {
                                    arraySym.setCol(nbCol);
                                }
                                else {
                                    throw new ArraysException("SymTableFiller.enterConstDecl() : La taille de l'array n'est pas correcte "+varDeclId);
                                }
                            }
                            else {
                                throw new ArraysException("SymTableFiller.enterConstDecl() : Type incorrect "+varDeclId);
                            }

                        }
                    }
                    // Ou dans le cas d'une array qui contient des arrays
                    // Faut encore gérer le fait d'avoir les bons types (comme en haut mais essayer de factoriser)
                    else {
                        // On transforme le String en Array
                        // On enlève les { et }
                        String[] arraysStr = varValue.substring(2, varValue.length() - 2).split("},\\s*\\{");

                        for(int i = 0; i < arraysStr.length; i++) {
                            // On enlève les { et } de chaque array
                            String[] strArray = arraysStr[i].split(",");
                            if(varDeclType.equals("int")) {
                                int[][] intArrays = new int[arraysStr.length][];
                                intArrays[i] = new int[strArray.length];
                                for(int j = 0; j < strArray.length; j++) {
                                    // On ajoute chaque élément booléen dans l'array de booleans
                                    intArrays[i][j] = Integer.parseInt(strArray[j]);
                                }
                            }
                            if(varDeclType.equals("char")) {
                                String[][] charArrays = new String[arraysStr.length][];
                                charArrays[i] = new String[strArray.length];
                                for(int j = 0; j < strArray.length; j++) {
                                    // On ajoute chaque élément booléen dans l'array de booleans
                                    charArrays[i][j] = strArray[j];
                                }
                            }
                            if(varDeclType.equals("bool")) {
                                // Init array pour les arrays de booleans
                                boolean[][] boolArrays = new boolean[arraysStr.length][];
                                boolArrays[i] = new boolean[strArray.length];
                                for(int j = 0; j < strArray.length; j++) {
                                    // On ajoute chaque élément booléen dans l'array de booleans
                                    boolArrays[i][j] = Boolean.parseBoolean(strArray[j]);
                                }
                            }

                        }
                        // Check si on a bien un array qui contient des arrays
                        //if (boolArrays.getClass().isArray() && boolArrays.getClass().getComponentType().isArray()) {
                        //    System.out.println("C'est un tableau multidimensionnel");
                        //} else {
                        //    System.out.println("Ce n'est pas un tableau multidimensionnel");
                        //}
                        // On split pour récupérer la taille des arrays
                        String[] arrayParts = arrayContent.split(",");
                        String first = arrayParts[0];
                        String last = arrayParts[1];
                        // Passage de str en int
                        nbLine = Integer.parseInt(first);
                        nbCol = Integer.parseInt(last);

                        System.out.println("Array à " + nbLine + " lignes et à " + nbCol + " colonnes");
                        /*
                        // Check si les tailles sont correctes
                        if (boolArrays.length == nbLine) {

                            // On init un compteur pour être sûr que chaque array a la bonne taille
                            int counter = 0;
                            // Si l'array a la bonne taille, on incrémente le compteur
                            for (int i = 0; i < boolArrays.length; i++){
                                if (boolArrays[i].length == nbCol){
                                    counter++;
                                }
                                // On ajoute une sécurité
                                // Exemple : le compteur est à 4 mais il y a 5 arrays dont 1 avec la mauvaise taille,
                                // donc on décrémente de 1 le compteur
                                else {
                                    counter--;
                                }
                            }
                            // Si le compteur est bien égal au nombre de colonne, c'est que le nombre de colonne est correct
                            if (counter == nbLine){
                                arraySym.setLine(nbLine);
                            }
                            else {
                                throw new ArraysException("Le nombre de colonnes de l'array n'est pas correcte");
                            }
                        }
                        else {
                            throw new ArraysException("Le nombre de lignes de l'array n'est pas correcte");
                        }*/
                    }
                }
            }
        }
        // Si array
        if((nbLine != 0 && nbCol != 0) || arraySolo) {
            VariableSymbol symbol = new VariableSymbol(varDeclId, varDeclType, arraySym, constant);
            addVariable(varDeclId, symbol);
        }
        // Si variable
        else {
            VariableSymbol symbol = new VariableSymbol(varDeclId, varDeclType, varValue, constant);
            addVariable(varDeclId, symbol);
        }
    }


    /**
     * Méthode appelée lors de la déclaration d'une structure dans le contexte de l'arbre syntaxique généré par ANTLR.
     *
     * @param ctx le nœud de l'arbre syntaxique correspondant à la déclaration de structure
     * @throws StructException si une exception liée à la structure survient
     */
    @Override
    public void enterStructDecl(PlayPlusParser.StructDeclContext ctx) {
        String structName = ctx.structures().ID().getText();
        String structValue = ctx.structures().listVarName().getText();

        // On init une liste contenant tous les types possibles
        ArrayList<String> expectedTypes = new ArrayList<>(Arrays.asList("int", "char", "bool"));

        // Ici ce sont les arrays qui vont permettre de réassembler les instructions
        List<String> listType = new ArrayList<>();
        List<String> listID = new ArrayList<>();
        List<String> listSemicolon = new ArrayList<>();

        // Cette variable contient les instructions
        List<ParseTree> children = ctx.structures().listVarName().children;

        // On va parcourir la variable children qui contient toutes les instructions séparées
        for(ParseTree child : children) {
            // On récupère chaque élément de children
            String elem = child.getText();
            // On ajoute l'elem dans une array en fonction
            // Si c'est un ";"
            if(elem.equals(";")) {
                listSemicolon.add(elem);
            }
            // Si c'est un type reconnu
            if(expectedTypes.contains(elem)) {
                listType.add(elem);
            }
            // Sinon c'est un ID
            if((! elem.equals(";")) && (! expectedTypes.contains(elem))) {
                if(listID.contains(elem)) {
                    throw new StructException("SymTableFiller.enterStructDecl() : ID déjà existant dans le struct : " + elem);
                }
                listID.add(elem);
            }
        }
        // La boucle ici permet d'ajouter le [x] à une init d'array
        // Pour chaque élément dans la listID, si on croise un élément avec [ et ], on le remet avec l'ID juste avant dans la liste
        // Puis on supprime l'élément [x]
        // [a, b, [2]] => [a, b[2]]
        for(int i = 0; i < listID.size(); i++) {
            if(listID.get(i).contains("[") && listID.get(i).contains("]")) {
                listID.set(i - 1, listID.get(i - 1) + listID.get(i));
                listID.remove(i);
            }
        }

        // On init la liste qui sera le résultat avec toutes les instructions
        List<String> result = new ArrayList<>();
        ArrayList<String> listeName = new ArrayList<>();
        // Cette boucle permet de réassembler les instructions entre-elles sur base de leur place dans leurs arrays respectives
        for(int i = 0; i < listID.size(); i++) {
            String element = listType.get(i) + " " + listID.get(i) + " " + listSemicolon.get(i);
            result.add(element);
            listeName.add(listID.get(i));
        }

        // Check s'il manque un élément, mais pour le moment s'il manque un élément, ça ne marche tout simplement pas
        for(int i = 0; i < result.size(); i++) {
            String elem = result.get(i);
            if(elem.split(" ").length != 3) {
                throw new StructException("SymTableFiller.enterStructDecl() : Il manque un élément");
            }
        }


        structPorte++;
        maxStructPorte++;
        StructSymbol structSymbol = new StructSymbol(structName, "struct", result, null, structPorte, listeName, ctx);
        addVariable(structName, structSymbol);
    }

    /**
     * Méthode appelée lors de la sortie du contexte de déclaration de structure du parseur.
     * Met à jour la portée courante pour la structure en la décrémentant.
     *
     * @param ctx le contexte de déclaration de structure du parseur
     */
    @Override
    public void exitStructDecl(PlayPlusParser.StructDeclContext ctx) {
        structPorte--;
    }


    /**
     * Méthode appelée lorsqu'on entre dans le contexte de déclaration d'une fonction.
     * Elle récupère les informations de la fonction, ajoute la fonction à la table des symboles,
     * crée une nouvelle table des symboles pour la portée de la fonction, et vérifie si la variable de retour est correctement utilisée dans la fonction.
     *
     * @param ctx le contexte de déclaration de la fonction
     * @throws FonctionException si la variable de retour n'est pas du même type que la fonction ou si elle n'est pas inclus dans la fonction
     */
    @Override
    public void enterFctDecl(PlayPlusParser.FctDeclContext ctx) {
        String idFonction = ctx.ID().getText();
        String typeFonction = "void";

        // Récupère le type de la fonction
        if(ctx.type() != null) {
            if(ctx.type().scalar() != null) {
                typeFonction = ctx.type().scalar().getText();
            }
        }

        // Récupère l'id du return
        String idReturn = "";
        if(ctx.exprD() != null) {
            idReturn = ctx.exprD().getText();
        }

        maxPorte++;
        FunctionSymbol functionSymbol = new FunctionSymbol(idFonction, typeFonction, maxPorte, null, false, ctx);
        addFunction(idFonction, functionSymbol);


        symTable.put(maxPorte, new SymTable());
        currentPorte = maxPorte;
        currentFunction = functionSymbol;

        // Boucle les instructions, si on retrouve l'id du return c'est ok, sinon on lance une exception
        boolean idReturnPresent = false;
        for(PlayPlusParser.InstBlockContext inst : ctx.instBlock()) {
            for(int i = 0; i < inst.children.size(); i++) {

              if(inst.children.get(i).getChild(1).getText().equals(idReturn)) {
                  if(! (inst.children.get(i).getChild(0).getText().equals(typeFonction))) {
                      throw new FonctionException("SymTableFiller.enterFctDecl() : Variable de return pas du même type que la fonction : " + idReturn);
                  }
              }

            }

            if(inst.getText().contains(idReturn)) {
                idReturnPresent = true;
            }
        }
        if(! idReturnPresent) {
            throw new FonctionException("SymTableFiller.enterFctDecl() : Variable de return pas inclus dans la fonction : " + idReturn);
        }
    }

    /**
     * Méthode appelée lors de la sortie du contexte de déclaration de fonction du parseur.
     * Réinitialise la portée courante à 0 et la fonction courante à null.
     *
     * @param ctx le contexte de déclaration de fonction du parseur
     */
    @Override
    public void exitFctDecl(PlayPlusParser.FctDeclContext ctx) {
        currentPorte = 0;
        currentFunction = null;
    }

    /**
     * Méthode appelée lorsqu'on entre dans le contexte d'une liste d'arguments de fonction.
     * Récupère les noms et types des arguments et les stocke dans les listes d'arguments (total, type et nom) de la fonction en cours.
     *
     * @param ctx le contexte de la liste d'arguments de fonction
     */
    @Override
    public void enterArgList(PlayPlusParser.ArgListContext ctx) {
        ArrayList<String> argumentsListAll = new ArrayList<>();
        ArrayList<String> argumentsListType = new ArrayList<>();
        ArrayList<String> argumentsListName = new ArrayList<>();
        for(int i = 0; i < ctx.ID().size(); i++) {
            String paramName = ctx.ID(i).getText();
            String paramType = ctx.type(i).getText();
            argumentsListAll.add(paramType + " " + paramName);
            argumentsListType.add(paramType);
            argumentsListName.add(paramName);
            currentFunction.setargumentsList(argumentsListAll);
            currentFunction.setargumentsListType(argumentsListType);
            currentFunction.setargumentsListName(argumentsListName);
        }
    }

    @Override
    public void enterEqualInstr(PlayPlusParser.EqualInstrContext ctx) {
        //String varId = ctx.exprG().getText();
        String varId = ctx.exprG().getChild(0).getText();
        String varIdExprD = ctx.exprD().getText();
        String valueExprD = ctx.exprD().getChild(0).getText();


        // Check que exprG est init
        // Check si init dans currentPorte
        Symbol symbol = symTable.get(currentPorte).getSymbols(varId); //ctx.exprG().children.get(0).getText()
        if(symbol == null) {
            //Check si init dans global
            symbol = symTable.get(0).getSymbols(varId); //ctx.exprG().children.get(0).getText()
            if(symbol == null) {
                ArrayList<String> argumentsList = currentFunction.getArgumentsName();
                if(! argumentsList.isEmpty()) {
                    Boolean ok2 = false;
                    for(int i = 0; i < argumentsList.size(); i++) {
                        if(varId.equals(argumentsList.get(i))) {
                            ok2 = true;
                        }
                    }
                    if(! ok2) {
                        // Check si variable init dans la porté actuel
                        if(symbol == null) {
                            symbol = symTable.get(0).getSymbols(varId);
                            if(symbol == null) {
                                if(! (varId.matches(".*[\\[\\]].*"))) {
                                    throw new ExpressionException("Variable non initialisée : " + varId);
                                }
                            } else {
                                System.out.println("La variable '" + varId + "' est ok.");
                                VarIdok = true;
                            }
                        }
                    }
                }
                // throw new ExpressionException("Variable non init :" +varId);
            }
            else {
                //symTable.get(0).getSymbols(varId).setValue(valueExprD);
            }
        }
        else{
           // symTable.get(currentPorte).getSymbols(varId).setValue(valueExprD);
        }
        if(symbol != null) {
            Boolean ok = false;
            Symbol symbolExprD = symTable.get(currentPorte).getSymbols(varIdExprD);
            if(symbolExprD == null) {
                symbolExprD = symTable.get(0).getSymbols(varIdExprD);
                if(symbolExprD != null) {
                    ok = true;
                }
            }
            else {
                ok = true;
            }
            if(ok) {
                if(! (symbol.getType().equals(symbolExprD.getType()))) {
                    throw new ExpressionException("Type différent ExprG != ExprD :" + varId + " != " + varIdExprD);
                }
            }


        }

        ExprGId = symbol;

    }

    /**
     * Méthode appelée lorsqu'on sort du contexte d'une instruction d'égalité.
     * Elle réinitialise la variable ExprGId à null.
     *
     * @param ctx le contexte de l'instruction d'égalité
     */
    @Override
    public void exitEqualInstr(PlayPlusParser.EqualInstrContext ctx) {
        ExprGId = null;
    }

    /**
     * Méthode appelée lorsqu'on entre dans le contexte d'un id d'une expression de gauche.
     *
     *  @param ctx Le contexte de l'identifiant dans la grammaire PlayPlus.
     *  @throws ExpressionException si la variable n'est pas initialisée ou si son type ne correspond pas à celui de l'argument.
     */
    @Override
    public void enterIdExprG(PlayPlusParser.IdExprGContext ctx) {
        String varID = ctx.ID().getText();
        ParseTree parent = ctx.ID().getParent().getParent();
        Boolean ok = false;

        //Check si l'identifiant est utilisé comme fonction
        if(!VarIdok) {
            if(!(parent instanceof PlayPlusParser.FctInstrContext)) {
                Symbol symbol = symTable.get(currentPorte).getSymbols(varID);
                if(symbol == null) {
                    symbol = symTable.get(0).getSymbols(varID);
                    if(symbol == null) {
                        ArrayList<String> argumentsList = null;

                        if(currentFunction != null) {
                            // Récupération de la liste des noms des arguments de la fonction en cours
                            argumentsList = currentFunction.getArgumentsName();
                        }

                        if(argumentsList != null && ! argumentsList.isEmpty()) {
                            for(int i = 0; i < argumentsList.size(); i++) {
                                //Check si l'identifiant est un argument de la fonction
                                if(varID.equals(argumentsList.get(i))) {
                                    ok = true;
                                }
                            }
                            if(!ok) {
                                // Check si la variable est initialisée dans la portée actuelle
                                if(symbol == null) {
                                    symbol = symTable.get(0).getSymbols(varID);
                                    if(symbol == null) {
                                        // Check si l'identifiant est un tableau
                                        if(!(varID.matches(".*[\\[\\]].*"))) {
                                            throw new ExpressionException("SymTableFiller.enterIdExprG() : Variable non initialisée : " + varID);
                                        }
                                    }
                                    else {
                                        VarIdok = true;
                                    }
                                }
                            }
                        }
                    }
                }
                if(symbol != null && (parent.getParent() instanceof PlayPlusParser.IdParExprGContext)) {
                    Boolean find = false;
                    for(String argument : argumentsListType) {
                        // Check si le type de la variable correspond au type de l'argument
                        if(symbol.getType().equals(argument)) {
                            find = true;
                        }
                    }
                    if(!find) {
                        throw new ExpressionException("SymTableFiller.enterIdExprG() : Variable pas init " + varID);
                    }
                }
            }
        }
        VarIdok = false;
    }

    /**
     * Méthode appelée lorsqu'on entre dans le contexte d'une expression entière utilisée comme expression droite.
     * Vérifie si l'entier est utilisé comme un index d'un tableau et si les types sont valides.
     *
     * @param ctx le contexte de l'expression entière utilisée comme expression droite
     * @throws ExpressionException si les types de l'expression droite ne sont pas valides
     */
    @Override
    public void enterExprEntExprD(PlayPlusParser.ExprEntExprDContext ctx) {
        ParseTree parent = ctx.exprEnt().getParent().getParent();
        //Check si l'entier est un index d'un tableau
        if(! (parent instanceof PlayPlusParser.IdSqrtparExprGContext)) {
            if(ExprGId != null) {
                if(! ExprGId.getType().equals("int")) {
                    throw new ExpressionException("SymTableFiller.enterExprEntExprD() : Les types de l'expression droite de la variable " + ExprGId.getName() + " ne sont pas valides");
                }
            }
        }
    }


    /**
     * Méthode appelée lorsqu'on entre dans le contexte d'une expression de chaîne de caractères à droite d'une affectation.
     * Vérifie si le type de l'expression de gauche (si elle existe) est compatible avec une expression de chaîne de caractères.
     * Si ce n'est pas le cas, et si `fctOk` est `false`, une exception `ExpressionException` est levée avec un message d'erreur approprié.
     *
     * @param ctx le contexte de l'expression de chaîne de caractères à droite d'une affectation
     * @throws ExpressionException si le type de l'expression de gauche n'est pas compatible avec une expression de chaîne de caractères
     */
    @Override
    public void enterStringExprD(PlayPlusParser.StringExprDContext ctx) {
        if(ExprGId != null) {
            if(! ExprGId.getType().equals("string")) {
                if(! fctOk) {
                    throw new ExpressionException("SymTableFiller.enterStringExprD() : Les types de l'expression droite de la variable " + ExprGId.getName() + " ne sont pas valides");
                }
            }
        }
    }

    /**
     * Méthode appelée lorsqu'on entre dans le contexte d'une expression de caractère à droite d'une affectation.
     * Vérifie si le type de l'expression de gauche (si elle existe) est compatible avec une expression de caractère.
     * Si ce n'est pas le cas, et si `fctOk` est `false`, une exception `ExpressionException` est levée avec un message d'erreur approprié.
     *
     * @param ctx le contexte de l'expression de caractère à droite d'une affectation
     * @throws ExpressionException si le type de l'expression de gauche n'est pas compatible avec une expression de caractère
     */
    @Override
    public void enterExprCharExprD(PlayPlusParser.ExprCharExprDContext ctx) {
        if(ExprGId != null) {
            if(! ExprGId.getType().equals("char")) {
                if(! fctOk) {
                    throw new ExpressionException("SymTableFiller.enterExprCharExprD() : Les types de l'expression droite de la variable " + ExprGId.getName() + " ne sont pas valides");
                }
            }
        }
    }

    /**
     * Méthode appelée lorsqu'on entre dans le contexte d'une expression booléenne à droite d'une affectation.
     * Vérifie si le type de l'expression de gauche (si elle existe) est compatible avec une expression booléenne.
     * Si ce n'est pas le cas, et si `fctOk` est `false`, une exception `ExpressionException` est levée avec un message d'erreur approprié.
     *
     * @param ctx le contexte de l'expression booléenne à droite d'une affectation
     * @throws ExpressionException si le type de l'expression de gauche n'est pas compatible avec une expression booléenne
     */
    @Override
    public void enterExprBoolExprD(PlayPlusParser.ExprBoolExprDContext ctx) {
        if(ExprGId != null) {
            if(! ExprGId.getType().equals("bool")) {
                if(! fctOk) {
                    throw new ExpressionException("SymTableFiller.enterExprBoolExprD() : Les types de l'expression droite de la variable " + ExprGId.getName() + " ne sont pas valides");
                }
            }
        }
    }

    @Override
    public void enterExprGExprEnt(PlayPlusParser.ExprGExprEntContext ctx) {
        String varId = ctx.exprG().getText();
        Symbol symbol = symTable.get(currentPorte).getSymbols(varId);
        fctOk = false;


        if(currentFunction != null) {
            //Check si appel fonction
            if(! ctx.exprG().getChild(0).getText().equals(currentFunction.getName())) {
                // Check si variable importer via argument d'une fonction
                ArrayList<String> argumentsList = currentFunction.getArguments();
                if(! argumentsList.isEmpty()) {
                    String firstArgument = argumentsList.get(0);
                    String[] parts = firstArgument.split(" ");
                    String variableName = parts[1];
                    if(! (variableName.equals(varId))) {
                        // Check si variable init dans la porté actuel
                        if(symbol == null) {
                            symbol = symTable.get(0).getSymbols(varId);
                            if(symbol == null) {
                                if(! (varId.matches(".*[\\[\\]].*"))) {
                                    throw new ExpressionException("Variable non initialisée : " + varId);
                                }
                            }
                            else {
                                System.out.println("La variable '" + varId + "' est ok.");
                                VarIdok = true;
                            }
                        }
                    }
                    else {
                        System.out.println("La variable '" + varId + "' est ok.");
                        fctOk = true;
                        VarIdok = true;
                    }
                }
            }

        }


    }

    /**
     * Méthode appelée lorsqu'on sort du contexte d'une expression entière à gauche d'une affectation.
     * Elle met la valeur de `fctOk` à `false`.
     *
     * @param ctx le contexte de l'expression entière à gauche d'une affectation
     */
    @Override
    public void exitExprGExprEnt(PlayPlusParser.ExprGExprEntContext ctx) {
        fctOk = false;
    }

    @Override
    public void enterIdParExprG(PlayPlusParser.IdParExprGContext ctx) {
        String functionName = ctx.ID().getText();
        FunctionSymbol functionDefinition = symTable.get(currentPorte).getFonctions(functionName);
        // Check si c'est une fonction et défini un array contenant tout les arguments de la fonction
        if(functionDefinition == null) {
            functionDefinition = symTable.get(0).getFonctions(functionName);
            if(functionDefinition != null) {
                argumentsList = functionDefinition.getArguments();
                argumentsListType = functionDefinition.getArgumentsType();
                if(argumentsList.size() == ctx.exprD().size()) {
                    for(String argument : argumentsList) {
                        fctOk = true;

                    }
                } else {
                    throw new ExpressionException("Nombre d'argument de l'appel différent au nombre d'argument attendu");
                }

            }
        }

    }

    /**
     * Méthode appelée lorsqu'on sort du contexte d'un identifiant utilisé comme une expression à gauche d'une affectation.
     * Elle met la valeur de `fctOk` à `false`.
     *
     * @param ctx le contexte de l'identifiant utilisé comme une expression à gauche d'une affectation
     */
    @Override
    public void exitIdParExprG(PlayPlusParser.IdParExprGContext ctx) {
        fctOk = false;
    }

    /**
     * Méthode appelée lorsqu'on entre dans le contexte d'une instruction de boucle "repeat".
     * Elle vérifie si la variable d'itération de la boucle a le type approprié (int) dans la table des symboles.
     * Si ce n'est pas le cas, elle lance une exception.
     *
     * @param ctx le contexte de l'instruction de boucle "repeat"
     * @throws ExpressionException si le type de la variable d'itération n'est pas valide pour une boucle "repeat"
     */
    @Override
    public void enterRepeatInstr(PlayPlusParser.RepeatInstrContext ctx) {
        String varId = ctx.exprEnt().getText();
        Symbol symbol = symTable.get(currentPorte).getSymbols(varId);
        Boolean verifEquals = false;
        if(symbol == null) {
            symbol = symTable.get(0).getSymbols(varId);
            if(symbol != null) {
                verifEquals = true;
            }
        }
        else {
            verifEquals = true;
        }

        if(verifEquals) {
            if(! (symbol.getType().equals("int"))) {
                throw new ExpressionException("SymTableFiller.enterRepeatInstr() : Type incorrect, ne peut pas être utiliser dans une boucle : " + varId);
            }
        }
    }


    /**
     * Méthode appelée lorsqu'on entre dans le contexte d'un identifiant suivi de l'opérateur de racine carrée et d'une expression.
     * Elle vérifie si l'identifiant est défini comme une liste (tableau) et si son utilisation est compatible avec les opérations
     * qui l'utilisent dans son contexte parent.
     *
     * @param ctx le contexte de l'identifiant suivi de l'opérateur de racine carrée et d'une expression
     * @throws ExpressionException si la variable n'est pas initialisée, si elle n'est pas définie comme une liste (tableau), ou si un opérateur incompatible est utilisé avec un tableau de type CHAR/BOOL
     */
    @Override
    public void enterIdSqrtparExprG(PlayPlusParser.IdSqrtparExprGContext ctx) {
        String varId = ctx.ID().getText();
        Symbol symbol = symTable.get(currentPorte).getSymbols(varId);
        Boolean isStruct = false;


        // Quand nous avons un struct.id on passe la vérif, car il a déjà été vérif dans une autre méthode.
        String parentText = ctx.getParent().getText();
        if(parentText.contains(".")) {
            isStruct = true;
        }

        // Quand nous avons un struct.id on passe la vérif, car il a déjà été vérif dans une autre méthode.
        parentText = ctx.getParent().getParent().getText();
        if(parentText.contains(".")) {
            isStruct = true;
        }

        if(symbol == null) {
            symbol = symTable.get(0).getSymbols(varId);
            if(symbol == null) {
                if(!isStruct) {
                    throw new ExpressionException("SymTableFiller.enterIdSqrtparExprG() : Variable non init " + varId);
                }
            }
        }
        if(symbol != null) {
            if(!(symbol.isArray())) {
                throw new ExpressionException("SymTableFiller.enterIdSqrtparExprG() : N'est pas défini comme une liste : " + varId);
            }

            // Check si le parent contient un opérateur incompatible
            RuleContext parent = ctx.getParent();
            while (parent != null) {
                if (parent instanceof PlayPlusParser.ExprEntPlusExprEntContext || parent instanceof PlayPlusParser.ExprEntMinusExprEntContext || parent instanceof PlayPlusParser.ExprEntSlashExprEntContext || parent instanceof PlayPlusParser.ExprEntStarExprEntContext) {
                    String parentText2 = parent.getText();
                    if(!(symbol.getType().equals("int"))) {
                        throw new ExpressionException("SymTableFiller.enterIdSqrtparExprG() : Opérateur incompatible pour le type CHAR/BOOL : " + parentText2);
                    }
                }
                parent = parent.getParent();
            }
        }
    }


    /**
     * Méthode appelée lorsqu'on entre dans le contexte d'une déclaration d'énumération.
     * Elle récupère les valeurs d'énumération et les ajoute dans la table des symboles.
     *
     * @param ctx le contexte de la déclaration d'énumération
     */
    @Override
    public void enterEnumDecl(PlayPlusParser.EnumDeclContext ctx) {
        List<ParseTree> children = ctx.children;
        ArrayList<String> enumList = new ArrayList<>();

        // On check si le deuxième élément de children est un "{"
        // Si oui, cela veut dire que enum n'a pas d'ID
        // Sinon, il possède un ID
        if(children.get(1).getText().equals("{")) {
            for(int i = 0; i < ctx.ID().size(); i++) {
                String enumDeclId = ctx.ID(i).toString();
                enumList.add(enumDeclId);
            }
            String idRandom = UUID.randomUUID().toString();
            EnumSymbol enumSymbol = new EnumSymbol(idRandom, enumList);
            addVariable(idRandom, enumSymbol);
        }
        else {
            for(int i = 1; i < ctx.ID().size(); i++) {
                String enumDeclId = ctx.ID(i).toString();
                enumList.add(enumDeclId);
            }
            EnumSymbol enumSymbol = new EnumSymbol(children.get(1).getText(), enumList);
            addVariable(children.get(1).getText(), enumSymbol);
        }
    }

    /**
     * Méthode appelée lors de l'entrée dans le contexte de déclaration de typedef du parseur.
     * Récupère le nom et le type du typedef, crée un symbole de typedef correspondant,
     * et l'ajoute à la table des symboles comme une variable.
     *
     * @param ctx le contexte de déclaration de typedef du parseur
     */
    @Override
    public void enterTypedefDecl(PlayPlusParser.TypedefDeclContext ctx) {
        String idTypeDef = ctx.ID().getText();
        String typeTypeDef = ctx.type().getText();
        TypedefSymbol typedefSymbol = new TypedefSymbol(idTypeDef, typeTypeDef, null, null);
        addVariable(idTypeDef, typedefSymbol);
    }

    /**
     * Méthode qui renvoie la table des symboles.
     *
     * @return la table des symboles
     */
    public Map<Integer, SymTable> getSymTable() {
        return symTable;
    }

    /*
    @Override
    public void enterExprEntPlusExprEnt(PlayPlusParser.ExprEntPlusExprEntContext ctx) {
        int expr1;
        int expr2;
        String varId = ctx.getParent().getParent().getText();
        String[] parts = varId.split("=");
        varId = parts[0].trim();



        Symbol symbol = symTable.get(currentPorte).getSymbols(varId);
        if(symbol == null) {
            symbol = symTable.get(0).getSymbols(varId);
        }

        String expr1Data = ctx.getChild(0).getText();
        String expr2Data = ctx.getChild(2).getText();


        Object expr1ValueId = getValueID(expr1Data);
        Object expr2ValueId = getValueID(expr2Data);

        if(expr1ValueId != null) {
            expr1 = Integer.parseInt((String) expr1ValueId);
        }
        else {
            expr1 = Integer.parseInt( (String) ctx.getChild(0).getText());
        }
        if(expr2ValueId != null) {
            expr2 = Integer.parseInt((String) expr2ValueId);
        }
        else {
            expr2 = Integer.parseInt( (String) ctx.getChild(2).getText());
        }

        if(symbol != null) {
            int value = expr1 + expr2;
            symbol.setValue(String.valueOf(value));
        }
    }


    @Override
    public void enterExprEntMinusExprEnt(PlayPlusParser.ExprEntMinusExprEntContext ctx) {
        int expr1;
        int expr2;
        String varId = ctx.getParent().getParent().getText();
        String[] parts = varId.split("=");
        varId = parts[0].trim();



        Symbol symbol = symTable.get(currentPorte).getSymbols(varId);
        if(symbol == null) {
            symbol = symTable.get(0).getSymbols(varId);
        }

        System.out.println("TEst :"+ctx.getText());

        String expr1Data = ctx.getChild(0).getText();
        String expr2Data = ctx.getChild(2).getText();


        Object expr1ValueId = getValueID(expr1Data);
        Object expr2ValueId = getValueID(expr2Data);

        if(expr1ValueId != null) {
            expr1 = Integer.parseInt((String) expr1ValueId);
        }
        else {
            expr1 = Integer.parseInt( (String) ctx.getChild(0).getText());
        }
        if(expr2ValueId != null) {
            expr2 = Integer.parseInt((String) expr2ValueId);
        }
        else {
            expr2 = Integer.parseInt( (String) ctx.getChild(2).getText());
        }

        if(symbol != null) {
            int value = expr1 - expr2;
            symbol.setValue(String.valueOf(value));
        }
    }

    @Override
    public void enterExprEntStarExprEnt(PlayPlusParser.ExprEntStarExprEntContext ctx) {
        int expr1;
        int expr2;
        String varId = ctx.getParent().getParent().getText();
        String[] parts = varId.split("=");
        varId = parts[0].trim();


        Symbol symbol = symTable.get(currentPorte).getSymbols(varId);
        if(symbol == null) {
            symbol = symTable.get(0).getSymbols(varId);
        }

        String expr1Data = ctx.getChild(0).getText();
        String expr2Data = ctx.getChild(2).getText();


        Object expr1ValueId = getValueID(expr1Data);
        Object expr2ValueId = getValueID(expr2Data);

        if(expr1ValueId != null) {
            expr1 = Integer.parseInt((String) expr1ValueId);
        }
        else {
            expr1 = Integer.parseInt( (String) ctx.getChild(0).getText());
        }
        if(expr2ValueId != null) {
            expr2 = Integer.parseInt((String) expr2ValueId);
        }
        else {
            expr2 = Integer.parseInt( (String) ctx.getChild(2).getText());
        }


        if(symbol != null) {
            int value = expr1 * expr2;
            symbol.setValue(String.valueOf(value));
        }

    }


    @Override
    public void enterExprEntSlashExprEnt(PlayPlusParser.ExprEntSlashExprEntContext ctx) {
        int expr1;
        int expr2;
        String varId = ctx.getParent().getParent().getText();
        String[] parts = varId.split("=");
        varId = parts[0].trim();

        Symbol symbol = symTable.get(currentPorte).getSymbols(varId);
        if(symbol == null) {
            symbol = symTable.get(0).getSymbols(varId);
        }


        String expr1Data = ctx.getChild(0).getText();
        String expr2Data = ctx.getChild(2).getText();


        Object expr1ValueId = getValueID(expr1Data);
        Object expr2ValueId = getValueID(expr2Data);

        if(expr1ValueId != null) {
            expr1 = Integer.parseInt((String) expr1ValueId);
        }
        else {
            expr1 = Integer.parseInt( (String) ctx.getChild(0).getText());
        }
        if(expr2ValueId != null) {
            expr2 = Integer.parseInt((String) expr2ValueId);
        }
        else {
            expr2 = Integer.parseInt( (String) ctx.getChild(2).getText());
        }

        if(symbol != null) {
            int value = expr1 / expr2;
            symbol.setValue(String.valueOf(value));
        }

    }
    */

    /**
     * Return la valeur de l'id.
     * @param varId nom de l'id
     * @return valeur id
     */
    public Object getValueID(String varId) {
        Symbol symbol = symTable.get(currentPorte).getSymbols(varId);
        if(symbol == null) {
            symbol = symTable.get(0).getSymbols(varId);
            if(symbol == null) {
                return null;
            }
            else{
                return symTable.get(0).getSymbols(varId).getValue();
            }
        }
        else{
            return symTable.get(currentPorte).getSymbols(varId).getValue();
        }
    }



    public void checkSymbol(String varId) {
        Symbol symbol = symTable.get(currentPorte).getSymbols(varId);
        if(symbol == null) {
            symbol = symTable.get(0).getSymbols(varId);
            if(symbol == null) {
                throw new ExpressionException("Variable non init " + varId);
            }
        }
    }


    @Override
    public void enterFctInstrVoid(PlayPlusParser.FctInstrVoidContext ctx) {
        String varId = ctx.ID().getText();
        Boolean ok = false;
        FunctionSymbol functionSymbol = symTable.get(currentPorte).getFonctions(varId);
        if(functionSymbol == null) {
            functionSymbol = symTable.get(0).getFonctions(varId);
            if(functionSymbol == null) {
                ArrayList<String> argumentsList = null;

                if(currentFunction != null) {
                    // Récupération de la liste des noms des arguments de la fonction en cours
                    argumentsList = currentFunction.getArgumentsName();
                }

                if(argumentsList != null && !argumentsList.isEmpty()) {
                    for(int i = 0; i < argumentsList.size(); i++) {
                        //Check si l'identifiant est un argument de la fonction
                        if(varId.equals(argumentsList.get(i))) {
                            ok = true;
                        }
                    }
                    if(!ok) {
                        // Check si la variable est initialisée dans la portée actuelle
                            Symbol symbol = symTable.get(0).getSymbols(varId);
                            if(symbol == null) {
                                // Check si l'identifiant est un tableau
                                if(!(varId.matches(".*[\\[\\]].*"))) {
                                    throw new ExpressionException("SymTableFiller.enterFctInstrVoid() : Variable non initialisée : " + varId);
                                }
                            }
                            else {
                                VarIdok = true;
                            }
                    }
                }

            }
            else {
                ArrayList<String> arguments = functionSymbol.getArgumentsType();
                int argumentSize = functionSymbol.getArgumentsType().size();
                int elem = 0;
                for(int i = 0; i < ctx.getChildCount(); i++) {
                    ParseTree argumentCall = ctx.getChild(i);
                    if(argumentCall instanceof PlayPlusParser.ExprDContext) {
                        if(elem < argumentSize) {
                              //TODO faire les vérifications type
                                elem++;
                            }
                        }
                    }
                }

        }
        else {
            ArrayList<String> arguments = functionSymbol.getArgumentsType();
            int argumentSize = functionSymbol.getArgumentsType().size();
            int elem = 0;
            for(int i = 0; i < ctx.getChildCount(); i++) {
                ParseTree argumentCall = ctx.getChild(i);
                if(argumentCall instanceof PlayPlusParser.ExprDContext) {
                    if(elem < argumentSize) {
                        System.out.println("TEST 2 "+argumentCall.getText());
                        elem++;
                    }
                }
            }
        }


    }


}

