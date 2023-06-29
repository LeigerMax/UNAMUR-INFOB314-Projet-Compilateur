package be.unamur.info.b314.compiler;

import be.unamur.info.b314.compiler.exception.ArraysException;
import com.google.common.base.Preconditions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * La classe NBCPrinter permet d'écrire du code NBC dans un fichier ou dans une sortie.
 *
 * La classe a une énumération NBCCodeTypes qui définit les types de données NBC correspondant
 * aux types de données du langage de programmation.
 * Les constructeurs prennent en entrée un File, un nom de fichier ou une OutputStream.
 * La classe a également une méthode print qui prend en entrée un String et l'écrit dans le fichier de sortie,
 * ainsi que des méthodes pour l'impression de code NBC spécifiques.
 *
 * Les méthodes spécifiques permettent d'afficher des instructions pour déclarer et initialiser des variables,
 * définir des constantes et modifier leurs valeurs, etc.
 */
public class NBCPrinter {


    public enum NBCCodeTypes {
        Int("sdword"), Char("sbyte"), Bool("byte");

        private final String representation;

        private NBCCodeTypes(String r) {
            representation = r;
        }

    }

    private final PrintWriter writer;

    public NBCPrinter(File outFile) throws FileNotFoundException {
        writer = new PrintWriter(outFile);
    }

    public NBCPrinter(String fileName) throws FileNotFoundException {
        writer = new PrintWriter(fileName);
    }

    public NBCPrinter(OutputStream out) {
        writer = new PrintWriter(out);
    }

    /****************************
     *                          *
     *           MAP            *
     *                          *
     ****************************/

    /**
     * Affiche le include dans le fichier
     */
    public void printInclude(){
        writer.printf("#include \"NXTDefs.h\"\n").println();
    }

    /****************************
     *                          *
     *          VARIABLE        *
     *                          *
     ****************************/


    /**
     * Affiche les mots-clé "dseg segment" quand on init des variables
     */
    public void printEnterInitVariable() {
        writer.printf("dseg segment").println();
    }

    /**
     * Affiche les mots-clé "dseg ends" quand on a fini de init des variables
     */
    public void printEndInitVariable() {
        writer.printf("dseg ends").println();
    }

    /**
     * Affiche les informations lorsqu'on init une variable avec valeur.
     *
     * @param type type de la variable
     * @param name nom de la variable
     * @param value valeur de la variable
     */
    public void printInitVariableValue(NBCCodeTypes type, String name, String value) {
        writer.printf("%s %s %s", name, type.representation,value).println();
    }

    /**
     * Affiche les informations lorsqu'on init une variable sans valeur.
     *
     * @param type type de la variable
     * @param name nom de la variable
     */
    public void printInitVariable(NBCCodeTypes type, String name) {
        writer.printf("%s %s", name, type.representation).println();
    }

    /**
     * Affiche le mot-clé "define" lorsqu'on souhaite définir une constante
     *
     * @param name nom de la constante (MAJ)
     * @param value valeur de la constante
     */
    public void printConstVarialbe(String name, String value) {
        writer.printf("#define %s %s ", name, value).println();
    }


    /**
     * Affiche le mot-clé "set" pour signifier que l'on modifie la valeur d'une variable
     * La différence avec mov c'est qu'ici, le second argument devra être une constante
     *
     * @param name nom de la variable
     * @param value sa nouvelle valeur
     */
    public void printVariableSet(String name,String value) {
        writer.printf("set %s, %s", name, value).println();
    }

    /**
     * Affiche le mot-clé "mov" pour signifier que l'on modifie la valeur d'une variable
     *
     * @param name nom de la variable
     * @param value sa nouvelle valeur
     */
    public void printVariableMov(String name,String value) {
        writer.printf("mov %s, %s", name, value).println();
    }

    /****************************
     *                          *
     *          ARRAY           *
     *                          *
     ****************************/

    /**
     * Affiche le mot-clé "arrbuild" lorsque l'on souhaite init une array avec une valeur en fonction du type
     *
     * @param type type de la variable
     * @param name nom de la variable
     * @param value valeur de la variable
     */
    public void printArrayValue(String type, String name,String value) {
        if(type.equals("Bool")) {
            writer.printf("arrbuild %s,%s", name,value).println();
        }
        else if (type.equals("Int")) {
            writer.printf("arrbuild  %s,%s", name,value).println();
        }
        else if(type.equals("Char")) {
            writer.printf("arrbuild %s,%s", name,value).println();
        }
        else {
            throw new ArraysException("Type inconnu");
        }
    }

    /**
     * Affiche le mot-clé "arrbuild" lorsque l'on souhaite init une array en fonction du type mais sans valeur
     *
     * @param type type de la variable
     * @param name nom de la variable
     */
    public void printArray(String type, String name) {
        if(type.equals("Bool")) {
            writer.printf("%s byte[] ", name).println();
        }
        else if (type.equals("Int")) {
            writer.printf("%s sdword[] ", name).println();
        }
        else if(type.equals("Char")) {
            writer.printf("%s sbyte[] ", name).println();
        }
        else {
            throw new ArraysException("Type inconnu");
        }
    }

    /**
     * Utilisation du mot-clé "index" pour récupèrer la valeur d'un élement d'une table avec l'index
     *
     * @param destination nom variable
     * @param table table que nous allons travailler
     * @param index index qu'on souhaite récupérer
     */
    public void printIndex(String destination, String table, String index) {
        writer.printf("index %s, %s, %s", destination, table, index).println();
    }

    /**
     * Ajout des différentes données dans un array avec le mot-clé "arrbuild"
     *
     * @param name du tableau
     * @param values valeurs a rentrer
     */
    public void printArrayBuild(String name, ArrayList<String> values) {
        writer.printf("arrbuild %s", name);
        for(String value : values){
            writer.printf(", %s", value);
        }
        writer.println();
    }

    /****************************
     *                          *
     *          STRUCT          *
     *                          *
     ****************************/

    /**
     * Affichage du nom du struct et utilisation du mot-clé "struct" lorsque l'on on rentre dans un struct()
     *
     * @param name nom du struct
     */
    public void printEnterStruct(String name) {
        writer.printf("%s struct", name).println();
    }

    /**
     * Affichage du nom du struct et utilisation du mot-clé "struct" lorsque l'on on sort d'un struct()
     *
     * @param name nom du struct
     */
    public void printEndStruct(String name) {
        writer.printf("%s ends", name).println();
    }

    /****************************
     *                          *
     *     TRAITEMENT VAR       *
     *                          *
     ****************************/


    /**
     * Affichage d'une instruction d'addition et utilisation du mot-clé "add" pour additionner les variables.
     *
     * @param destination nom variable
     * @param expr1 expression
     * @param expr2 expression
     */
    public void printPlusExprEnt(String destination, String expr1, String expr2) {
        writer.printf("add %s, %s ,%s", destination, expr1, expr2).println();
    }

    /**
     * Affichage d'une instruction de soustraction et utilisation du mot-clé "sub" pour soustraire les variables.
     *
     * @param destination nom variable
     * @param expr1 expression
     * @param expr2 expression
     */
    public void printMinusExprEnt(String destination, String expr1, String expr2) {
        writer.printf("sub %s, %s ,%s", destination, expr1, expr2).println();
    }

    /**
     * Affichage d'une instruction de multiplication et utilisation du mot-clé "mul" pour multiplier les variables.
     *
     * @param destination nom variable
     * @param expr1 expression
     * @param expr2 expression
     */
    public void printStarExprEnt(String destination, String expr1, String expr2) {
        writer.printf("mul %s, %s ,%s", destination, expr1, expr2).println();
    }

    /**
     * Affichage d'une instruction de division et utilisation du mot-clé "div" pour diviser les variables.
     *
     * @param destination nom variable
     * @param expr1 expression
     * @param expr2 expression
     */
    public void printSlashExprEnt(String destination, String expr1, String expr2) {
        writer.printf("div %s, %s ,%s", destination, expr1, expr2).println();
    }


    /**
     * Affichage et utilisation du mot-clé "not" pour inverser la valeur d'une variable
     *
     * @param destination nom variable
     * @param expr expression
     */
    public void printNotExprEnt(String destination, String expr) {
        writer.printf("not %s, %s", destination, expr).println();
    }

    /**
     * Utiliastion du mot-clé "and" pour effectuer une vérification "et" entre deux valeurs booléennes
     *
     * @param destination nom variable
     * @param expr1 expression
     * @param expr2 expression
     */
    public void printAndExprBool(String destination, String expr1, String expr2) {
        writer.printf("and %s, %s ,%s", destination, expr1, expr2).println();
    }

    /**
     * Utiliastion du mot-clé "or" pour effectuer une vérification "ou" entre deux valeurs booléennes
     *
     * @param destination nom variable
     * @param expr1 expression
     * @param expr2 expression
     */
    public void printOrExprBool(String destination, String expr1, String expr2) {
        writer.printf("or %s, %s ,%s", destination, expr1, expr2).println();
    }

    /**
     * Affichage et utilisation du mot-clé "cmp" pour comparer les valeurs de deux variables
     * On compare ces deux expressions sur base de plusieurs types de comparaison :
     *  LT : plus petit que, GT : plus grand que, LTEQ : plus petit ou égal à, GTEQ : plus grand ou égal à,
     *  EQ : égal à, NEQ : pas égal à.
     *
     * @param operator type de comparaison (LT,GT,LTEQ,GTEQ,EQ,NEQ)
     * @param destination nom variable
     * @param expr1 expression
     * @param expr2 expression
     */
    public void printComparExpr(String operator, String destination, String expr1, String expr2) {
        writer.printf("cmp %s, %s, %s, %s", operator, destination, expr1, expr2).println();
    }

    /**
     * Affichage et utilisation du mot-clé "sbyte[]" pour l'énumération
     *
     * @param name nom de enum
     * @param value valeur de enum
     */
    public void printElem(String name, String value){
        writer.printf("%s sbyte[] %s", name, value).println();
    }


    /****************************
     *                          *
     *     IF/ESLE BOUCLE       *
     *                          *
     ****************************/


    /**
     * Affichage et utilisation du mot-clé brcmp afin d'effectuer une comparaison (// à "cmp")
     * Cependant, si la comparaison est valide, on est redirigé vers l'instruction spécifiée (nomLabel).
     * Les opérateurs sont les mêmes que pour "cmp".
     *
     * @param operateur type de comparaison (LT,GT,LTEQ,GTEQ,EQ,NEQ)
     * @param nomLabel nom de l'instruction à laquelle on saute si condition = TRUE
     * @param expr1 première expression
     * @param expr2 seconde expression
     */
     public void printBrcmp(String operateur, String nomLabel, String expr1, String expr2) {
         writer.printf("brcmp %s, %s, %s, %s", operateur, nomLabel, expr1, expr2).println();
     }


    /**
     * Affichage et utilisation des deux points ":" pour créer une boucle
     * Remarque : Une boucle est formée si on appelle la boucle à l'intérieur d'elle-même (récursion)
     *
     * @param name nom de la loop
     */
    public void printLoop(String name) {
        writer.printf("%s:", name).println(); // on a pas mis loop pour des raisons (maxime, pas moi, benjamin)
    }

    /**
     * Affichage et utilisation du mot-clé "jmp" afin de sauter à une instruction spécifique
     *
     * @param name nom de l'instruction où il faut aller
     */
    public void printJump(String name){
        writer.printf("jmp %s", name).println();
    }

    /****************************
     *                          *
     *          METHODE         *
     *                          *
     ****************************/

    /**
     * Affichage et utilisation des mots-clé "thread main" quand on rentre dans l'instruction principale main()
     */
    public void printEnterThreadMain() {
        writer.printf("thread main").println();
    }

    /**
     * Affichage et utilisation du mot-clé "thread" quand on rentre dans une fonction
     *
     * @param name nom de la fonction
     */
    public void printEnterThread(String name) {
        writer.printf("thread %s", name).println();
    }

    /**
     * Affichage et utilisation du mot-clé "endt" quand on sort de main()
     */
    public void printEndThread() {
        writer.printf("endt").println();
    }

    /**
     * Affichage et utilisation du mot-clé "subroutine" quand on rentre dans un sous programme
     * C'est une sorte de fonction que l'on peut définir lorsque l'on a plusieurs fois le même morceau de code à exécuter
     * Remarque : ceci est définit en dehors du main()
     *
     * @param name nom de la routine
     */
    public void printEnterSubRoutine(String name) {
        writer.printf("subroutine %s", name).println();
    }

    /**
     * Affichage et utilisation du mot-clé "ends" quand on sort d'un sous programme
     */
    public void printEndSubRoutine() {
        writer.printf("ends").println();
    }


    /**
     * Affichage et utilisation du mot-clé "return" pour spécifier une renvoie de variable
     */
    public void printReturn() {
        writer.println("return");
    }

    /**
     * Affichage et utilisation du mot-clé "call" pour effectuer un appel de méthode
     *
     * @param name nom de la méthode à appeler
     */
    public void printCall(String name){
        writer.printf("call %s", name).println();
    }



    /****************************
     *                          *
     *           ROBOT          *
     *                          *
     ****************************/

    /**
     * Affichage et utilisation du mot-clé "OnFwd" afin de faire avancer le robot tout droit selon une certaine puissance
     *
     * @param port moteur que l'on souhaite utiliser
     * @param power puissance (correspond à sa vitesse de déplacement)
     */
    public void printOnForward(String port, int power) {
        writer.printf("OnFwd(%s, %s)", port, power).println();
    }

    /**
     * Affichage et utilisation du mot-clé "OnRev" afin de faire avancer le robot dans le sens opposé
     * selon une certaine puissance
     *
     * @param port moteur que l'on souhaite utiliser
     * @param power puissance (correspond à sa vitesse de déplacement)
     */
    public void printRobotRev(String port, int power){
        writer.printf("OnRev(%s, %s)", port, power).println();
    }


    /**
     * Affichage et utilisation du mot-clé "Off" lorsque l'on souhaite éteindre le moteur
     *
     * @param port moteur à fermer
     */
    public void printOff(String port) {
        writer.printf("Off(%s)", port).println();
    }


    /**
     * Affichage et utilisation du mot-clé "wait" afin de suspendre un thread selon une certaine durée
     *
     * @param time durée de l'attente
     */
    public void printWait(int time){
        writer.printf("wait %s", time).println();
    }


    /**
     * Affichage et utilisation du mot-clé "PlayTone" afin de faire jouer un son au robot en réaction à un saut (jump())
     *
     * fréquence de TONE_B5 (son spécifique)
     * durée de 200 millisecondes (durée du son)
     */
    public void printRobotJump() {
        writer.printf("PlayTone(TONE_B5, 200)").println();
    }

    /**
     * Affichage et utilisation du mot-clé "PlayTone" afin de faire jouer un son au robot en réaction à un combat (fight())
     *
     * fréquence de TONE_C5 (son spécifique)
     * durée de 200 millisecondes (durée du son)
     */
    public void printRobotFight() {
        writer.printf("PlayTone(TONE_C5, 200)").println();
    }

    /**
     * Affichage et utilisation du mot-clé "PlayTone" afin de faire jouer un son au robot qui creuse (dig())
     *
     * fréquence de TONE_E5 (son spécifique)
     * durée de 200 millisecondes (durée du son)
     */
    public void printRobotDig() {
        writer.printf("PlayTone(TONE_E5, 200)").println();
    }



    /****************************
     *                          *
     *           AUTRES         *
     *                          *
     ****************************/

    /**
     * Ecrit un texte
     *
     * @param text
     */
    public void printLine(String text) {
        writer.printf(text).println();
    }

    /**
     * Utilisation de ";" afin d'écire un commentaire
     *
     * @param texte commentaire à écrire
     */
    public void printComments(String texte) {
        writer.printf("; %s", texte).println();
    }




    /*****************/


    public void flush() {
        writer.flush();
    }

    public void close() {
        writer.flush();
        writer.close();
    }

}
