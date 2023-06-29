package be.unamur.info.b314.compiler;

import be.unamur.info.b314.compiler.exception.FonctionException;
import be.unamur.info.b314.compiler.exception.VariableNameException;
import be.unamur.info.b314.compiler.symbolesTables.FunctionSymbol;
import be.unamur.info.b314.compiler.symbolesTables.StructSymbol;
import be.unamur.info.b314.compiler.symbolesTables.Symbol;
import be.unamur.info.b314.compiler.symbolesTables.VariableSymbol;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import static java.lang.Integer.parseInt;

/**
 * Print PCode for a given tree using provided symbol table and printer. This
 * class uses ANTLR visitor mechanism
 *
 * @author James Ortiz - james.ortizvega@unamur.be
 * @author Maxime Allemeersch - maxime.allemeersch@student.unamur.be
 * @author Simon Dourte - simon.dourte@student.unamur.be
 * @author Benjamin Pans - benjamin.pans@student.unamur.be
 */
public class NBCVisitor extends PlayPlusBaseVisitor<Object> {

    private final Map<Integer, SymTable> symTable;

    private final NBCPrinter printer;

    private final File inputFile;
    private String currentDir;

    private int currentPorte; // Portée actuel
    private int maxPorte;
    private String returnValueFunction;

    private ArrayList<String> variableSuppList = new ArrayList<>(); //Array qio contient les variables créer temporairement via la méthode createVarSupp()
    // private ArrayList<String> argumentListGlobal = new ArrayList<>(); //Array qui contient les paramètres de fonctions call
    // private ArrayList<String> fonctionAvecParam = new ArrayList<>();  //Aray qui contient le nom des fonctions qui doivent recevoir des arguments
    private Map<String, String> functionAndArgument = new HashMap<>();
    private ArrayList<PlayPlusParser.FctDeclContext> fonctionContextList = new ArrayList<PlayPlusParser.FctDeclContext>();
    private int indexName = 0;
    private int indexVarName = 0; //Permet de définir des variables "temporaire "

    /**
     * Constructeur de la classe NBCVisitor.
     * Initialise la symTable, le printer et l'inputFile.
     * Définis la portée courante à 0.
     * Définis la portée maximale courant à 0.
     * On récupère le chemin d'accès du répertoire courant où se trouve le fichier d'entrée inputFile utilisé dans l'objet NBCVisitor.
     */
    public NBCVisitor(Map<Integer, SymTable> symTable, NBCPrinter printer, File inputFile) {
        this.symTable = symTable;
        this.printer = printer;
        this.inputFile = inputFile;
        currentPorte = 0;
        maxPorte = 0;
        currentDir = inputFile.getParent();
    }

    /****************************
     *                          *
     *           MAP            *
     *                          *
     ****************************/


    /**
     * Méthode appelée lors de la visite du contexte "map" du parseur.
     * Récupère les informations de la carte et va les stocker dans la table des symboles ainsi que les imprimer.
     *
     * @param ctx le contexte "map" du parseur contenant les informations de la carte à stocker.
     * @return null
     */
    @Override
    public Object visitMap(PlayPlusParser.MapContext ctx) {
        List<ParseTree> listComplet = ctx.children;

        printer.printComments(ctx.children.toString());

        int ligne = Integer.parseInt(listComplet.get(2).getText());
        int colone = Integer.parseInt(listComplet.get(3).getText());

        // Récupère la liste des éléments de la carte
        ArrayList<Character> listElement = new ArrayList<>();
        for (int i = 4; i < listComplet.size(); i++) {
            listElement.add(listComplet.get(i).getText().charAt(0));
        }

        MapCheckFile mapCheck = new MapCheckFile(colone, ligne, listElement);
        symTable.get(0).setMapCheckFile(mapCheck);
        printer.printEnterInitVariable();
        return null;
    }

    /**
     * Méthode appelée lors de la visite d'un noeud "FileDecl".
     * Cette méthode récupère le nom du fichier à inclure et en crée un objet "File" correspondant.
     * Elle crée également un nouvel arbre de syntaxe à partir du fichier et visite cet arbre.
     *
     * @param ctx le contexte "FileDecl" du parseur contenant le nom du fichier à inclure.
     * @return null
     */
    @Override
    public Object visitFileDecl(PlayPlusParser.FileDeclContext ctx) {
        String filename = ctx.fileName().getText();
        String path = currentDir + "/" + filename.replace("\"", "");

        try {
            File inputFile = new File(path);
            System.out.println("input :" + inputFile);

            ANTLRInputStream input = new ANTLRInputStream(new FileInputStream(inputFile));
            CommonTokenStream tokens = new CommonTokenStream(new PlayPlusLexer(input));

            PlayPlusParser parser = new PlayPlusParser(tokens);
            PlayPlusParser.RootContext tree = parser.root();

            NBCVisitor visitor = new NBCVisitor(symTable, printer, inputFile);
            visitor.visit(tree);

        } catch (IOException e) {
            // throw new MapException("visitFileDecl() : Map pas trouvé"); // Map créer des erreurs sur Jenkies
        }

        return null;
    }


    /****************************
     *                          *
     *          Programme       *
     *                          *
     ****************************/


    /**
     * Visite le contexte "programme" et génère l'instruction #include.
     * Appelle ensuite la méthode visitChildren pour visiter tous les contextes enfants.
     *
     * @param ctx le contexte "programme" à visiter
     * @return null
     */
    @Override
    public Object visitProgramme(PlayPlusParser.ProgrammeContext ctx) {
        printer.printInclude();
        visitChildren(ctx);

        return null;
    }


    /****************************
     *                          *
     *          VARIABLE        *
     *                          *
     ****************************/

    //TODO, voir si nous avons d'autres instances à filtrer

    /**
     * Cette méthode permet de gérer la déclaration de variables dans le langage PlayPlus
     * en générant du code NBC correspondant à la déclaration de variable.
     * <p>
     * Récupère le nœud courant ctx de type PlayPlusParser.VarDeclContext
     * Parcourt l'arbre syntaxique à partir du nœud courant pour déterminer
     * si la variable est déclarée dans le bloc main
     * Si la variable est déclarée dans le bloc main, génère du code NBC pour initialiser la variable
     * Si la variable est un tableau, génère du code NBC pour initialiser le tableau avec les valeurs spécifiées
     * Si la variable n'est pas un tableau, génère du code NBC pour initialiser la variable avec ou sans valeur.
     *
     * @param ctx le contexte "déclaration de variable" à visiter
     * @return null
     */
    @Override
    public Void visitVarDecl(PlayPlusParser.VarDeclContext ctx) {

        ParseTree currentNode = ctx;
        ; // Le nœud à partir duquel vous souhaitez remonter dans l'arbre
        boolean foundMain = false;

        while (currentNode != null) {
            if (currentNode instanceof PlayPlusParser.MainContext) {
                foundMain = true;
                break;
            }
            if (currentNode instanceof PlayPlusParser.ProgrammeContext) {
                break; // Arrêter la recherche lorsque nous atteignons l'instance programme
            }
            currentNode = currentNode.getParent();
        }

        // Le parent est une instance de main, pour que @BenjaminPans soit content
        if (foundMain) {
            printer.printEnterInitVariable();
            /*
            String varId = ctx.ID(0).toString();
            String varType = ctx.type().scalar().getText();

            Symbol test = symTable.get(0).getSymbols(varId);
            if((test != null) && (varType != null)){
                if(test.getType() != null) {
                    throw new VariableNameException("visitVarDecl() : Variable déjà init dans global");
                }
            }*/

        }


        String varValue = "";
        String varId = ctx.ID(0).toString();
        String varType = ctx.type().scalar().getText();

        // Check si Array sans valeur
        boolean isArray = false;
        for (ParseTree child : ctx.children) {
            if (child instanceof PlayPlusParser.ArraysContext) {
                isArray = true;
                break;
            }
        }

        if (isArray) {
            List<ParseTree> children = ctx.children;
            String type = null;
            System.out.println(ctx.getChild(3).getText());
            switch (varType) {
                case "int":
                    type = "Int";
                    break;
                case "char":
                    type = "Char";
                    break;
                case "bool":
                    type = "Bool";
                    break;
                default:
                    break;
            }

            if (ctx.getChild(3).getText().contains("=")) {
                String variableSansBrackets = ctx.getChild(4).getText();
                String sansBrackets = variableSansBrackets.replaceAll("[{}]", "");
                String replacedString = sansBrackets.replaceAll("false", "0").replaceAll("true", "1");
                if (type.equals("Char")) {
                    StringBuilder charSeparatedString = new StringBuilder();
                    for (char c : replacedString.toCharArray()) {
                        if (Character.isLetter(c)) {
                            charSeparatedString.append("'").append(c).append("', ");
                        }
                    }
                    replacedString = charSeparatedString.toString().trim();
                    replacedString = replacedString.substring(0, replacedString.length() - 1);  // Remove the trailing comma
                }
                System.out.println("Variables : " + replacedString);
                printer.printArrayValue(type, varId, replacedString);
            } else {
                printer.printArray(type, varId);
            }


        }
        //Si pas Array
        else {
            NBCPrinter.NBCCodeTypes typeNotArray = null;

            switch (ctx.type().scalar().getText()) {
                case "int":
                    typeNotArray = NBCPrinter.NBCCodeTypes.Int;
                    break;
                case "char":
                    typeNotArray = NBCPrinter.NBCCodeTypes.Char;
                    break;
                case "bool":
                    typeNotArray = NBCPrinter.NBCCodeTypes.Bool;
                    break;
                default:
                    break;
            }

            if (ctx.initVariable().size() == 0) {
                printer.printInitVariable(typeNotArray, varId);
            } else {
                for (PlayPlusParser.InitVariableContext variable : ctx.initVariable()) {
                    printer.printInitVariableValue(typeNotArray, varId, variable.getText());
                }
            }

        }

        currentNode = ctx;
        ; // Le nœud à partir duquel vous souhaitez remonter dans l'arbre
        foundMain = false;

        while (currentNode != null) {
            if (currentNode instanceof PlayPlusParser.MainContext) {
                foundMain = true;
                break;
            }
            if (currentNode instanceof PlayPlusParser.ProgrammeContext) {
                break; // Arrêter la recherche lorsque nous atteignons l'instance programme
            }
            currentNode = currentNode.getParent();
        }

        // Le parent est une instance de main, pour que @BenjaminPans soit content
        if (foundMain) {
            printer.printEndInitVariable();
        }

        return null;
    }

    //TODO, il se peut que les constantes prennent un type, il faut peut-être également les inits comme une variable.
    //Check symTable si nécessaire, récupérer les valeurs et le type dedans.

    /**
     * Méthode appelée lors de la visite du contexte "constDecl" du parseur.
     * Stocke le nom et la valeur de la constante dans le fichier de sortie "printer".
     *
     * @param ctx le contexte "constDecl" du parseur contenant les informations de la constante à stocker.
     * @return null.
     */
    @Override
    public Void visitConstDecl(PlayPlusParser.ConstDeclContext ctx) {
        String name = ctx.ID(0).getText();
        String value = "";

        //Si const init sans valeur
        if (ctx.initVariable().size() == 0) {
            printer.printConstVarialbe(name, value);
        }
        //Si const init avec valeur
        else {
            for (PlayPlusParser.InitVariableContext variable : ctx.initVariable()) {
                printer.printConstVarialbe(name, variable.getText());
            }
        }

        return null;
    }




    /****************************
     *                          *
     *     TRAITEMENT VAR       *
     *                          *
     ****************************/

    //PEER

    /**
     * Méthode appelée lors de la visite du contexte "equalInstr" du parseur.
     * Effectue l'affectation d'une valeur à une variable, et gère également les appels de fonction
     * et la récupération des arguments associés.
     *
     * @param ctx le contexte "equalInstr" du parseur contenant les informations nécessaires à l'affectation.
     * @return null
     */
    @Override
    public Void visitEqualInstr(PlayPlusParser.EqualInstrContext ctx) {

        String varId = ctx.exprG().getText();
        int value = 0;

        ParseTree child = ctx.exprD().getChild(0);

        String return2 = test(child, varId, null);

        // Si fonction avec arguments
        if (child instanceof PlayPlusParser.IdParExprGContext) {
            String functionId = child.getChild(0).getText();
            // Récupère les arguments et les stocks dans argumentListGlobal
            for (ParseTree childOfChild : ((PlayPlusParser.IdParExprGContext) child).children) {
                if (childOfChild instanceof PlayPlusParser.ExprDContext) {
                    if (childOfChild instanceof PlayPlusParser.ExprBoolExprDContext) {
                        String result = String.valueOf(calculateExprBool(childOfChild));
                        functionAndArgument.put(functionId, result);
                        //this.fonctionAvecParam.add(functionId);
                        //this.argumentListGlobal.add(result);
                    } else {
                        functionAndArgument.put(functionId, childOfChild.getText());
                        // this.fonctionAvecParam.add(functionId);
                        // this.argumentListGlobal.add(childOfChild.getText());

                    }
                }
            }
        }

        return null;
    }


    //PEER

    /**
     * Méthode appelée lors de la visite du contexte "idParExprG" du parseur.
     * Récupère l'identifiant de la fonction ainsi que ses arguments et les stocke dans la table de symboles.
     *
     * @param ctx le contexte "idParExprG" du parseur contenant l'identifiant de la fonction et ses arguments.
     * @return null
     */
    @Override
    public Void visitIdParExprG(PlayPlusParser.IdParExprGContext ctx) {
        String varId = ctx.ID().getText();
        // Récupère les arguments et les stocks dans argumentListGlobal
        for (ParseTree child : ctx.children) {

            if (child instanceof PlayPlusParser.ExprDContext) {
                String data = test(child.getChild(0), varId, null);
                functionAndArgument.put(varId, data);
                // this.argumentListGlobal.add(data);
                // this.fonctionAvecParam.add(varId);
            }
        }
        return null;
    }


    // Le truc qui fait D2.X = value;

    /**
     * Méthode appelée lors de la visite d'un noeud de l'AST correspondant
     * à une affectation à une variable d'une valeur issue d'un champ d'une structure.
     *
     * @param ctx le contexte de la règle "structEqualInstr" dans l'AST.
     * @return null
     */
    @Override
    public Void visitStructEqualInstr(PlayPlusParser.StructEqualInstrContext ctx) {
        String varId = ctx.getChild(0).getText();
        String varIdInStruct = ctx.getChild(2).getChild(0).getText();

        ParseTree child = ctx.getChild(4).getChild(0);
        String return2 = test(child, varId, varIdInStruct);

        return null;
    }

    //PEER

    /**
     * Méthode appelée lors de la visite d'un noeud de l'AST correspondant à la déclaration d'un type enum.
     *
     * @param ctx le contexte de la règle "enumDecl" dans l'AST.
     * @return null
     */
    @Override
    public Void visitEnumDecl(PlayPlusParser.EnumDeclContext ctx) {

        String varId = null;
        String value = "";

        List<ParseTree> children = ctx.children;
        ArrayList<String> enumList = new ArrayList<>();

        // On check si le deuxième élément de children est un "{"
        // Si oui, cela veut dire que enum n'a pas d'ID
        // Sinon, il possède un ID
        if (children.get(1).getText().equals("{")) {
            for (int i = 0; i < ctx.ID().size(); i++) {
                if (i == ctx.ID().size() - 1) {
                    value = value + "'" + ctx.ID().get(i).getText() + "'" + "";
                } else {
                    value = value + "'" + ctx.ID().get(i).getText() + "'" + ",";
                }
            }
            varId = UUID.randomUUID().toString();
        } else {
            for (int i = 1; i < ctx.ID().size(); i++) {
                if (i == ctx.ID().size() - 1) {
                    value = value + "'" + ctx.ID().get(i).getText() + "'" + "";
                } else {
                    value = value + "'" + ctx.ID().get(i).getText() + "'" + ",";
                }
            }
            varId = ctx.getChild(0).toString();
        }


        printer.printElem(varId, value);

        return null;
    }


    /****************************
     *                          *
     *          STRUCT          *
     *                          *
     ****************************/

    //PEER
    // Inint struct

    /**
     * Méthode appelée lors de la visite d'un noeud de l'AST correspondant à la déclaration d'une structure.
     * Elle parcourt les noeuds enfants pour récupérer les informations sur les variables déclarées dans la structure.
     * Si une structure est déclarée dans une structure, elle appelle cette méthode récursivement pour traiter la structure interne.
     *
     * @param ctx le contexte de la règle "structDecl" dans l'AST.
     * @return null
     */
    @Override
    public Void visitStructDecl(PlayPlusParser.StructDeclContext ctx) {
        String varId = ctx.getChild(0).getChild(1).getText();
        StructSymbol structSymbol = (StructSymbol) symTable.get(0).getSymbols(varId);
        if (structSymbol == null) {
            structSymbol = (StructSymbol) symTable.get(currentPorte).getSymbols(varId);
        }

        int lastCurrentPorte = currentPorte;
        ParseTree contestStructInStruct = null;
        Boolean structInStruct = false;

        currentPorte = structSymbol.getPorte();
        //printer.printEnterInitVariable();
        printer.printEnterStruct(structSymbol.getName());


        PlayPlusParser.StructDeclContext context = structSymbol.getContext();

        //Prend le noeud structures
        ParseTree child0 = context.getChild(0);

        for (int i = 0; i < child0.getChildCount(); i++) {
            ParseTree grandchild = child0.getChild(i);


            // Si variable init
            if (grandchild instanceof PlayPlusParser.ListVarNameContext) {
                String varName = null;
                String varType = null;
                String varArray = null;

                for (ParseTree child : ((PlayPlusParser.ListVarNameContext) grandchild).children) {

                    if (child instanceof PlayPlusParser.TypeContext) {
                        varType = child.getText();
                    } else if (child instanceof PlayPlusParser.ArraysContext) {
                        varArray = child.getText();
                    } else if (child instanceof TerminalNodeImpl && ((TerminalNodeImpl) child).getSymbol().getType() == PlayPlusParser.ID && child.getText().matches("[a-zA-Z][a-zA-Z0-9_]*")) {
                        varName = child.getText();
                    } else if (child.getText().equals(";")) {
                        initVariableStructAndFunction(varArray, varName, varType, null);
                        varName = null;
                        varType = null;
                        varArray = null;
                    }
                }

            }

            // Si structure dans structure, on va attendre que le strucutre actuel est fini et faire une nouvelle strucutre à la fin de celle-ci
            if (grandchild.getText().contains("struct") && grandchild.getText().contains("{") && grandchild.getText().contains("}")) {
                structInStruct = true;
                contestStructInStruct = grandchild;
            }

        }

        printer.printEndStruct(varId);
        //printer.printEndInitVariable();
        currentPorte = lastCurrentPorte;

        // Si struct dans struct
        if (structInStruct) {
            visitStructDecl((PlayPlusParser.StructDeclContext) contestStructInStruct);
            structInStruct = false;
        }


        return null;
    }


    /****************************
     *                          *
     *    IF/ELSE &  BOUCLE     *
     *                          *
     ****************************/

    //PEER

    /**
     * Cette méthode visite un nœud de l'AST correspondant à une instruction "if" et effectue les actions suivantes:
     * <p>
     * Évalue l'expression booléenne associée à l'instruction "if" en appelant la méthode calculateExprBool().
     * Génère un nom unique pour l'instruction "if" et l'instruction "ifend" correspondante.
     * Émet une instruction de branchement conditionnel "brcmp" avec une condition "EQ"
     * pour vérifier si l'expression booléenne est "FALSE".
     * Émet une instruction de boucle "loop" avec le nom de l'instruction "if".
     * Visite les nœuds enfants de l'instruction "if" en appelant visitChildren().
     * Émet une instruction de boucle "loop" avec le nom de l'instruction "ifend".
     *
     * @param ctx Le contexte de l'instruction "if" dans l'AST.
     * @return null.
     */
    @Override
    public Void visitIfInstr(PlayPlusParser.IfInstrContext ctx) {
        String exprD = String.valueOf(calculateExprBool(ctx.exprBool())).toUpperCase();

        String ifName = "IF_" + this.indexName;
        String ifEndName = "IFEND_" + this.indexName;
        this.indexName++;

        printer.printBrcmp("EQ", ifEndName, exprD, "FALSE");

        printer.printLoop(ifName);
        visitChildren(ctx);
        printer.printLoop(ifEndName);


        return null;
    }


    //PEER

    /**
     * Cette méthode visite un nœud de l'AST correspondant à une instruction "if" et effectue les actions suivantes:
     * <p>
     * Évalue l'expression booléenne associée à l'instruction "if" en appelant la méthode calculateExprBool().
     * Génère un nom unique pour l'instruction "if" et l'instruction "ifend" correspondante.
     * Émet une instruction de branchement conditionnel "brcmp" avec une condition "EQ"
     * pour vérifier si l'expression booléenne est "FALSE".
     * Émet une instruction de boucle "loop" avec le nom de l'instruction "if".
     * Visite les nœuds enfants de l'instruction "if" en appelant visitChildren().
     * Émet une instruction de boucle "loop" avec le nom de l'instruction "ifend".
     *
     * @param ctx Le contexte de l'instruction "if" dans l'AST.
     * @return null.
     */
    @Override
    public Void visitIfElseInstr(PlayPlusParser.IfElseInstrContext ctx) {
        String exprD = String.valueOf(calculateExprBool(ctx.exprBool())).toUpperCase();

        String ifName = "IF_" + this.indexName;
        String ElseName = "ELSE_" + this.indexName;
        String ifEndName = "IFEND_" + this.indexName;
        this.indexName++;

        printer.printBrcmp("EQ", ElseName, exprD, "FALSE");


        printer.printLoop(ifName);
        visitChildren(ctx.instruction().get(0));
        printer.printJump(ifEndName);

        printer.printLoop(ElseName);
        visitChildren(ctx.instruction().get(1));
        printer.printJump(ifEndName);

        printer.printLoop(ifEndName);


        return null;
    }


    //TODO

    /**
     * Cette méthode visite un nœud de l'AST correspondant à une instruction "while" et effectue les actions suivantes:
     * <p>
     * Évalue l'expression boolean associée à l'instruction "while" en appelant la méthode calculateExprBool().
     * Génère un nom unique pour l'instruction de boucle "LOOP_WHILE_" et l'instruction de sortie "LOOP_WHILE_END_" correspondante.
     * Émet une instruction de boucle "loop" avec le nom de l'instruction "looprepeat".
     * Visite les nœuds enfants de l'instruction "while" autant de fois que la valeur de l'expression entière évaluée.
     * Émet une instruction de saut "jump" avec le nom de l'instruction de sortie "LoopWhileEndName".
     * Émet une instruction de boucle "loop" avec le nom de l'instruction de sortie "LoopWhileEndName".
     *
     * @param ctx Le contexte de l'instruction "while" dans l'AST.
     * @return null.
     */
    @Override
    public Void visitWhileInstr(PlayPlusParser.WhileInstrContext ctx) {

        String exprD = String.valueOf(calculateExprBool(ctx.exprBool())).toUpperCase();

        String LoopWhileName = "LOOP_WHILE_" + this.indexName;
        String LoopWhileEndName = "LOOP_WHILE_END_" + this.indexName;
        this.indexName++;

        printer.printBrcmp("EQ", LoopWhileEndName, exprD, "FALSE");
        printer.printJump(LoopWhileName);

        printer.printLoop(LoopWhileName);

        visitChildren(ctx);
        String loopingCond = ctx.exprBool().getText();

        printer.printBrcmp("EQ", LoopWhileName, loopingCond, "TRUE");
        printer.printJump(LoopWhileEndName);

        printer.printLoop(LoopWhileEndName);

        return null;
    }


    /**
     * Cette méthode visite un nœud de l'AST correspondant à une instruction "repeat" et effectue les actions suivantes:
     * <p>
     * Évalue l'expression entière associée à l'instruction "repeat" en appelant la méthode calculateExprEnt().
     * Génère un nom unique pour l'instruction de boucle "loop" et l'instruction de sortie "loopend" correspondante.
     * Émet une instruction de boucle "loop" avec le nom de l'instruction "looprepeat".
     * Visite les nœuds enfants de l'instruction "repeat" autant de fois que la valeur de l'expression entière évaluée.
     * Émet une instruction de saut "jump" avec le nom de l'instruction de sortie "looprepeatend".
     * Émet une instruction de boucle "loop" avec le nom de l'instruction de sortie "looprepeatend".
     *
     * @param ctx Le contexte de l'instruction "repeat" dans l'AST.
     * @return null.
     */
    @Override
    public Void visitRepeatInstr(PlayPlusParser.RepeatInstrContext ctx) {
        int numberOfLoops = calculateExprEnt(ctx.exprEnt());

        String LoopRepeatName = "LOOP_REPEAT_" + this.indexName;
        String LoopRepeatEndName = "LOOP_REPEAT_END_" + this.indexName;
        this.indexName++;

        printer.printLoop(LoopRepeatName);

        while (numberOfLoops > 0) {
            visitChildren(ctx);
            numberOfLoops--;
        }

        // printer.printBrcmp("EQ", LoopRepeatName, "0", "0"); // Boucle infinie, à moins d'une instruction de sortie
        printer.printJump(LoopRepeatEndName);

        printer.printLoop(LoopRepeatEndName);
        return null;
    }


    /****************************
     *                          *
     *          METHODE         *
     *                          *
     ****************************/

    //OK

    /**
     * Cette méthode visite le nœud principal de l'AST et effectue les actions suivantes :
     * <p>
     * Initialise les variables et entre dans le thread principal.
     * Visite les nœuds enfants du nœud principal en appelant visitChildren().
     * Attend 200 millisecondes et appelle la méthode robotDig().
     * Quitte le programme et termine le thread principal.
     * Appelle la fonction fonctionInit() pour chaque déclaration de fonction.
     *
     * @param ctx Le contexte du nœud principal de l'AST.
     * @return null.
     */
    @Override
    public Void visitMain(PlayPlusParser.MainContext ctx) {
        printer.printEndInitVariable();
        printer.printEnterThreadMain();
        maxPorte++;
        currentPorte = 0;

        visitChildren(ctx);

        printer.printWait(200);
        printer.printRobotDig(); //Appel le dernier dig()

        printer.printLine("exit");
        printer.printEndThread();
        currentPorte = 0;

        if (this.fonctionContextList != null) {

            for (PlayPlusParser.FctDeclContext fonctionContext : this.fonctionContextList) {
                fonctionInit(fonctionContext);

            }
        }

        return null;
    }


    //PEER

    /**
     * Cette méthode visite le nœud de déclaration de fonction de l'AST et effectue les actions suivantes :
     * <p>
     * Ajoute le contexte de la déclaration de fonction à une liste.
     * Construit le nom unique de la fonction.
     * Incrémente le nombre maximal de portes et la porte courante.
     * Parcourt les nœuds enfants du bloc d'instructions de la déclaration de fonction.
     * Pour chaque nœud enfant, parcourt les nœuds enfants et les petits-enfants pour trouver les expressions gauche-droite (ExprGExprDContext) et appelle visitChildren() pour les visiter.
     * Décrémente la porte courante.
     *
     * @param ctx Le contexte de la déclaration de fonction de l'AST.
     * @return null.
     */
    @Override
    public Void visitFctDecl(PlayPlusParser.FctDeclContext ctx) {
        this.fonctionContextList.add(ctx);
        //fonctionInit(ctx);

        String functionName = ctx.ID().getText();
        String functionUniqueName = "function_temp_" + functionName;


        //Instructions
        // Dans cette instruction, nous allons juste chercher les calls fonctions afin d'ajouter les données en paramètres dans une globle variable
        this.maxPorte++;
        this.currentPorte++;
        for (int j = 0; j < ctx.instBlock().size(); j++) {
            ParseTree instBlockChild = ctx.instBlock().get(j);
            for (int k = 0; k < instBlockChild.getChildCount(); k++) {
                ParseTree child = instBlockChild.getChild(k);

                for (int i = 0; i < child.getChildCount(); i++) {
                    ParseTree child2 = child.getChild(i);

                    if (child2 instanceof PlayPlusParser.ExprGExprDContext) {
                        visitChildren((RuleNode) child2);
                    }

                    for (int a = 0; a < child2.getChildCount(); a++) {
                        ParseTree child3 = child2.getChild(a);

                        //System.out.println("Instance "+child3.getClass().getSimpleName()); // Permet de voir l'instance

                        if (child3 instanceof PlayPlusParser.ExprGExprDContext) {
                            visitChildren((RuleNode) child3);
                        }

                    }

                }

            }
        }
        this.currentPorte--;

        return null;
    }

    //PEER

    /**
     * Initialise une fonction à partir d'un contexte de déclaration de fonction.
     * <p>
     * Elle commence par extraire le nom et les arguments de la fonction depuis
     * le contexte d'analyse syntaxique fourni en entrée.
     * Ensuite, elle initialise une série de variables et parcourt chaque argument
     * en les ajoutant à une liste de variables.
     * <p>
     * Ensuite, elle initialise la portée de la fonction et exécute les instructions
     * contenues dans le corps de la fonction.
     * Finalement, elle termine l'exécution de la fonction en retournant les valeurs
     * appropriées en fonction de la déclaration de type.
     *
     * @param ctx le contexte de déclaration de fonction
     */
    private void fonctionInit(PlayPlusParser.FctDeclContext ctx) {
        String functionName = ctx.ID().getText();
        String functionUniqueName = "function_" + functionName;


        printer.printEnterSubRoutine(functionUniqueName);

        //Arguments
        String varName = null;
        String varType = null;
        String varArray = null;
        ArrayList<String> varNameList = new ArrayList<>();

        // Check si il y a des arguments
        if (ctx.argList() != null && ctx.argList().children != null) {
            // On va récupérer les arguments et init des variables
            for (ParseTree child : ctx.argList().children) {

                if (child instanceof PlayPlusParser.TypeContext) {
                    varType = child.getText();
                } else if (child instanceof PlayPlusParser.ArraysContext) {
                    varArray = child.getText();
                } else if (child instanceof TerminalNodeImpl && ((TerminalNodeImpl) child).getSymbol().getType() == PlayPlusParser.ID && child.getText().matches("[a-zA-Z][a-zA-Z0-9_]*")) {
                    varName = child.getText();
                    varNameList.add(varName);
                } else if (child.getText().equals(",")) {
                    initVariableStructAndFunction(varArray, varName, varType, functionName);
                    varName = null;
                    varType = null;
                    varArray = null;

                }
            }
            initVariableStructAndFunction(varArray, varName, varType, functionName);


            // Avec la variable functionAndArgument, on ajoute les valeurs de l'appel de la fonction dans les variables créer précédement
            int count = 0;
            Iterator<Map.Entry<String, String>> iterator = functionAndArgument.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                String key = entry.getKey();
                String value = entry.getValue();

                if (key.equals(functionName.toString())) {
                    String argValue = value;
                    String varNameGet = varNameList.get(count);
                    printer.printVariableSet(varNameGet + "_" + functionName, argValue);

                    Symbol symbol = symTable.get(currentPorte).getSymbols(varNameGet + "_" + functionName);
                    if (symbol == null) {
                        symbol = symTable.get(0).getSymbols(varNameGet + "_" + functionName);
                        if (symbol != null) {
                            symbol.setValue(argValue);
                        }
                    } else {
                        symbol.setValue(argValue);
                    }

                    count++;
                    iterator.remove(); // Supprime l'élément utilisé de la HashMap
                }
            }

             /*
            for (int check = 0; check < functionAndArgument.size(); check++) {
                if (functionAndArgument.get(check).toString().equals(functionName.toString())) {



                    for (int i = 0; i < varNameList.size(); i++) {
                        String argValue = this.functionAndArgument.get(i);
                        String varNameGet = varNameList.get(i);
                        printer.printVariableSet(varNameGet+"_"+functionName, argValue);

                        Symbol symbol = symTable.get(currentPorte).getSymbols(varNameGet+"_"+functionName);
                        if (symbol == null) {
                            symbol = symTable.get(0).getSymbols(varNameGet+"_"+functionName);
                            if (symbol != null) {
                                symbol.setValue(argValue);
                            }
                        }
                        else {
                            symbol.setValue(argValue);
                        }
                    }

                    // Remove les éléments utilisés.
                    for (int i = varNameList.size() - 1; i >= 0; i--) {
                       //this.argumentListGlobal.remove(i);
                       //fonctionAvecParam.remove(i);
                        functionAndArgument.remove(i);
                    }

                }

              */

        }


        //Instructions
        this.maxPorte++;
        this.currentPorte++;
        for (int j = 0; j < ctx.instBlock().size(); j++) {
            visitInstBlock(ctx.instBlock().get(j));
        }

        this.currentPorte--;

        //Return
        if (! (ctx.getChild(0).getText().equals("void"))) {
            /*
            String returnVar = ctx.exprD().getText()+ "_" + functionName;
            System.out.println("RETURN "+returnVar.toString());
            returnValueFunction = getValueID(returnVar).toString();
            System.out.println("RETURN "+returnValueFunction.toString());


            NBCPrinter.NBCCodeTypes type = null;
            switch (ctx.type().getText()) {
                case "int":
                    type = NBCPrinter.NBCCodeTypes.Int;
                    break;
                case "char":
                    type = NBCPrinter.NBCCodeTypes.Char;
                    break;
                case "bool":
                    type = NBCPrinter.NBCCodeTypes.Bool;
                    break;
                default:
                    break;
            }

            //printer.printInitVariable(type, returnVar);
            
            System.out.println("Return "+returnVar);
            */

            //printer.printEndInitVariable();
        }


        printer.printReturn();
        printer.printEndSubRoutine();

    }


    //PEER

    /**
     * Initialise une variable ou un tableau de variable avec le nom, le type, et l'array de la variable
     * (s'il y en a un) dans la portée courante, et l'ajoute à la table des symboles.
     * Si c'est un tableau, imprime le code de l'initialisation de l'array,
     * sinon imprime le code de l'initialisation de la variable.
     *
     * @param varArray     un String qui représente l'array de la variable à initialiser ou null s'il s'agit d'une variable simple.
     * @param varName      un String qui représente le nom de la variable à initialiser.
     * @param varType      un String qui représente le type de la variable à initialiser.
     * @param functionName un String qui représente le nom de la fonction courante à laquelle la variable appartient.
     */
    public void initVariableStructAndFunction(String varArray, String varName, String varType, String functionName) {
        printer.printEnterInitVariable();
        // INIT variables
        // Si array
        if (varArray != null && varName != null && varType != null) {
            String type = null;
            String value = "";

            switch (varType) {
                case "int":
                    type = "Int";
                    break;
                case "char":
                    type = "Char";
                    break;
                case "bool":
                    type = "Bool";
                    break;
                default:
                    break;
            }


            indexVarName++;
            varName = varName + "_" + functionName;


            VariableSymbol symbol = new VariableSymbol(varName, varType, null, false);
            symTable.get(currentPorte).addSymbol(varName, symbol);
            printer.printArray(type, varName);

        }
        // Variable qui n'est pas un array
        else if (varArray == null && varName != null && varType != null) {
            NBCPrinter.NBCCodeTypes type = null;
            String value = "";
            switch (varType) {
                case "int":
                    type = NBCPrinter.NBCCodeTypes.Int;
                    break;
                case "char":
                    type = NBCPrinter.NBCCodeTypes.Char;
                    break;
                case "bool":
                    type = NBCPrinter.NBCCodeTypes.Bool;
                    break;
                default:
                    break;
            }

            indexVarName++;
            varName = varName + "_" + functionName;


            VariableSymbol symbol = new VariableSymbol(varName, varType, null, false);
            symTable.get(currentPorte).addSymbol(varName, symbol);
            printer.printInitVariableValue(type, varName, value);
        }
        printer.printEndInitVariable();
    }


    //PEER call fonction sans arg et aucun return !

    /**
     * Visite une instruction de type "appel de fonction" dans l'AST généré par ANTLR4 et génère du code NBC correspondant.
     *
     * @param ctx Le contexte de l'instruction dans l'AST généré par ANTLR4.
     * @return null.
     */
    @Override
    public Void visitFctInstr(PlayPlusParser.FctInstrContext ctx) {
        String varId = ctx.exprG().getText();

        FunctionSymbol functionSymbol = symTable.get(0).getFonctions(varId);
        ArrayList<String> argumentList = functionSymbol.getArguments();

        String functionUniqueName = "function_" + varId;


        printer.printCall(functionUniqueName);

        return null;
    }

    //PEER call fonction sans return !

    /**
     * Visite l'arbre syntaxique correspondant à l'appel d'une fonction sans valeur de retour
     * et génère le code correspondant à cet appel dans le programme NBC.
     *
     * @param ctx le contexte de l'appel de fonction dans l'arbre syntaxique
     * @return null
     * @throws FonctionException si la fonction appelée n'existe pas
     */
    @Override
    public Void visitFctInstrVoid(PlayPlusParser.FctInstrVoidContext ctx) {
        String functionId = ctx.ID().getText();

        FunctionSymbol functionSymbol = symTable.get(0).getFonctions(functionId);
        if (functionSymbol == null) {
            functionSymbol = symTable.get(currentPorte).getFonctions(functionId);
            if (functionSymbol == null) {
                throw new FonctionException("Exception FctInstrVoidContext() : Fonction qui n'existe pas");
            }
        }
        ArrayList<String> argumentList = functionSymbol.getArguments();

        String functionUniqueName = "function_" + functionId;

        printer.printCall(functionUniqueName);


        ArrayList<String> arguments = functionSymbol.getArgumentsType();
        int argumentSize = functionSymbol.getArgumentsType().size();
        int elem = 0;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree argumentCall = ctx.getChild(i);
            if (argumentCall instanceof PlayPlusParser.ExprDContext) {
                if (elem < argumentSize) {
                    //this.fonctionAvecParam.add(functionId);
                    //this.argumentListGlobal.add(argumentCall.getText());
                    functionAndArgument.put(functionId, argumentCall.getText());
                    elem++;
                }
            }
        }

        return null;
    }


    /****************************
     *                          *
     *           ROBOT          *
     *                          *
     ****************************/

    //PEER

    /**
     * Visite le nœud correspondant à l'action "right" dans l'arbre de syntaxe abstraite généré par ANTLR4,
     * et effectue l'action correspondante sur le robot.
     * <p>
     * Si le nœud contient 4 enfants, la méthode interprète les valeurs entre parenthèses pour
     * déterminer le nombre de mouvements à effectuer.
     * Si le nœud ne contient pas d'enfant, la méthode effectue un seul mouvement "right".
     *
     * @param ctx Le contexte de l'action "right" dans l'arbre de syntaxe abstraite.
     * @return null
     */
    @Override
    public Void visitRightAction(PlayPlusParser.RightActionContext ctx) {
        List<ParseTree> children = ctx.children;

        //Si nous avons 4 éléments, c'est que nous avons des valeurs entre ().
        if (children.size() == 4) {
            ParseTree childVerif = ctx.children.get(2);

            Object isValueID = checkIsVarTemp(childVerif);

            int value = 0;

            if (isValueID != null) {
                value = Integer.parseInt((String) isValueID.toString());
            } else {
                int expr1;
                int expr2;
                for (ParseTree child : children) {

                    //System.out.println("TEST  " +child.getText());
                    //System.out.println("Instance "+child.getClass().getSimpleName());

                    // Si entier seul
                    if (child instanceof PlayPlusParser.EntierExprEntContext) {
                        value = Integer.parseInt((String) child.getChild(0).getText());
                    }
                    if (child instanceof PlayPlusParser.ExprGExprEntContext) {
                        ParseTree expr1Data = child.getChild(0);

                        Object expr1ValueId = checkIsVarTemp(expr1Data);

                    }
                    // Si opérateur
                    if (child.getChild(0) != null && child.getChild(2) != null) {
                        //String expr1Data = child.getChild(0).getText();
                        //String expr2Data = child.getChild(2).getText();

                        // Object expr1ValueId = getValueID(expr1Data);
                        // Object expr2ValueId = getValueID(expr2Data);

                        ParseTree expr1Data = child.getChild(0);
                        ParseTree expr2Data = child.getChild(2);

                        Object expr1ValueId = checkIsVarTemp(expr1Data);
                        Object expr2ValueId = checkIsVarTemp(expr2Data);


                        // Si expr1 est une expression avec opérateur
                        if (child.getChild(0) instanceof PlayPlusParser.ExprEntPlusExprEntContext
                                || child.getChild(0) instanceof PlayPlusParser.ExprEntMinusExprEntContext
                                || child.getChild(0) instanceof PlayPlusParser.ExprEntSlashExprEntContext
                                || child.getChild(0) instanceof PlayPlusParser.ExprEntStarExprEntContext) {
                            expr1 = calculateExprEnt(child.getChild(0));
                        } else if (expr1ValueId != null) {
                            expr1 = Integer.parseInt((String) expr1ValueId);
                        } else {
                            expr1 = Integer.parseInt((String) child.getChild(0).getText());
                        }

                        // Si expr2 est une expression avec opérateur
                        if (child.getChild(2) instanceof PlayPlusParser.ExprEntPlusExprEntContext
                                || child.getChild(2) instanceof PlayPlusParser.ExprEntMinusExprEntContext
                                || child.getChild(2) instanceof PlayPlusParser.ExprEntSlashExprEntContext
                                || child.getChild(2) instanceof PlayPlusParser.ExprEntStarExprEntContext) {
                            expr2 = calculateExprEnt(child.getChild(2));
                        } else if (expr2ValueId != null) {
                            expr2 = Integer.parseInt((String) expr2ValueId.toString());
                        } else {
                            expr2 = Integer.parseInt((String) child.getChild(2).getText());
                        }

                        // Calcul du résultat de l'expression avec opérateur
                        if (child instanceof PlayPlusParser.ExprEntPlusExprEntContext) {
                            value = expr1 + expr2;
                        } else if (child instanceof PlayPlusParser.ExprEntMinusExprEntContext) {
                            value = expr1 - expr2;
                        } else if (child instanceof PlayPlusParser.ExprEntSlashExprEntContext) {
                            value = expr1 / expr2;
                        } else if (child instanceof PlayPlusParser.ExprEntStarExprEntContext) {
                            value = expr1 * expr2;
                        }
                    }


                }
            }
            for (int i = 0; i < value; i++) {
                printer.printWait(200);
                printer.printOnForward("OUT_B", 45);
                printer.printWait(200);
                printer.printRobotRev("OUT_C", 45);
                printer.printWait(200);
                printer.printRobotRev("OUT_BC", 1);
            }
        }

        // Si pas d'élément, faire une fois right.
        else {
            printer.printWait(200);
            printer.printOnForward("OUT_B", 45);
            printer.printWait(200);
            printer.printRobotRev("OUT_C", 45);
            printer.printWait(200);
            printer.printRobotRev("OUT_BC", 1);
        }

        printer.printOff("OUT_BC");

        return null;
    }


    //PEER

    /**
     * Visite un nœud représentant une action de mouvement vers la gauche dans l'arbre syntaxique.
     * <p>
     * Si l'élément contient des valeurs entre parenthèses, elles sont utilisées pour déterminer
     * le nombre de mouvements à effectuer.
     * Sinon, un seul mouvement vers la gauche est effectué.
     * Cette méthode utilise la classe Printer pour envoyer les commandes de mouvement au robot.
     *
     * @param ctx le contexte de l'action de mouvement vers la gauche dans l'arbre syntaxique
     * @return null.
     */
    @Override
    public Void visitLeftAction(PlayPlusParser.LeftActionContext ctx) {
        List<ParseTree> children = ctx.children;

        //Si nous avons 4 éléments, c'est que nous avons des valeurs entre ().
        if (children.size() == 4) {
            ParseTree childVerif = ctx.children.get(2);

            Object isValueID = checkIsVarTemp(childVerif);

            int value = 0;

            if (isValueID != null) {
                value = Integer.parseInt((String) isValueID.toString());
            } else {
                int expr1;
                int expr2;
                for (ParseTree child : children) {

                    // Si entier seul
                    if (child instanceof PlayPlusParser.EntierExprEntContext) {
                        value = Integer.parseInt((String) child.getChild(0).getText());
                    }
                    // Si opérateur
                    if (child.getChild(0) != null && child.getChild(2) != null) {
                        //String expr1Data = child.getChild(0).getText();
                        //String expr2Data = child.getChild(2).getText();

                        //Object expr1ValueId = getValueID(expr1Data);
                        //Object expr2ValueId = getValueID(expr2Data);

                        ParseTree expr1Data = child.getChild(0);
                        ParseTree expr2Data = child.getChild(2);

                        Object expr1ValueId = checkIsVarTemp(expr1Data);
                        Object expr2ValueId = checkIsVarTemp(expr2Data);


                        // Si expr1 est une expression avec opérateur
                        if (child.getChild(0) instanceof PlayPlusParser.ExprEntPlusExprEntContext
                                || child.getChild(0) instanceof PlayPlusParser.ExprEntMinusExprEntContext
                                || child.getChild(0) instanceof PlayPlusParser.ExprEntSlashExprEntContext
                                || child.getChild(0) instanceof PlayPlusParser.ExprEntStarExprEntContext) {
                            expr1 = calculateExprEnt(child.getChild(0));
                        } else if (expr1ValueId != null) {
                            expr1 = Integer.parseInt((String) expr1ValueId.toString());
                        } else {
                            expr1 = Integer.parseInt((String) child.getChild(0).getText());
                        }

                        // Si expr2 est une expression avec opérateur
                        if (child.getChild(2) instanceof PlayPlusParser.ExprEntPlusExprEntContext
                                || child.getChild(2) instanceof PlayPlusParser.ExprEntMinusExprEntContext
                                || child.getChild(2) instanceof PlayPlusParser.ExprEntSlashExprEntContext
                                || child.getChild(2) instanceof PlayPlusParser.ExprEntStarExprEntContext) {
                            expr2 = calculateExprEnt(child.getChild(2));
                        } else if (expr2ValueId != null) {
                            expr2 = Integer.parseInt((String) expr2ValueId.toString());
                        } else {
                            expr2 = Integer.parseInt((String) child.getChild(2).getText());
                        }

                        // Calcul du résultat de l'expression avec opérateur
                        if (child instanceof PlayPlusParser.ExprEntPlusExprEntContext) {
                            value = expr1 + expr2;
                        } else if (child instanceof PlayPlusParser.ExprEntMinusExprEntContext) {
                            value = expr1 - expr2;
                        } else if (child instanceof PlayPlusParser.ExprEntSlashExprEntContext) {
                            value = expr1 / expr2;
                        } else if (child instanceof PlayPlusParser.ExprEntStarExprEntContext) {
                            value = expr1 * expr2;
                        }
                    }


                }
            }
            for (int i = 0; i < value; i++) {
                printer.printWait(200);
                printer.printOnForward("OUT_B", - 45);
                printer.printWait(200);
                printer.printRobotRev("OUT_C", - 45);
                printer.printWait(200);
                printer.printRobotRev("OUT_BC", 1);
            }
        }
        // Si pas d'élément, faire une fois jump.
        else {
            printer.printWait(200);
            printer.printOnForward("OUT_B", - 45);
            printer.printWait(200);
            printer.printRobotRev("OUT_C", - 45);
            printer.printWait(200);
            printer.printRobotRev("OUT_BC", 1);
        }

        printer.printOff("OUT_BC");

        return null;
    }


    //PEER

    /**
     * Méthode qui visite un noeud de l'arbre syntaxique représentant l'action "Up".
     * <p>
     * Elle calcule le nombre de déplacements vers le haut à effectuer selon les valeurs fournies
     * en paramètres.
     * Si aucune valeur n'est fournie, elle effectue un déplacement vers le haut.
     * Si une valeur est fournie, elle vérifie si cette valeur est une variable temporaire ou une
     * expression mathématique, puis elle calcule la valeur finale pour le nombre de déplacements à effectuer.
     * Elle utilise ensuite une imprimante pour envoyer les instructions nécessaires
     * à la réalisation des déplacements vers le haut.
     *
     * @param ctx le contexte de l'arbre syntaxique correspondant à l'action "Up"
     * @return null.
     */
    @Override
    public Void visitUpAction(PlayPlusParser.UpActionContext ctx) {
        List<ParseTree> children = ctx.children;

        //Si nous avons 4 éléments, c'est que nous avons des valeurs entre ().
        if (children.size() == 4) {
            ParseTree childVerif = ctx.children.get(2);

            Object isValueID = checkIsVarTemp(childVerif);

            int value = 0;

            if (isValueID != null) {
                value = Integer.parseInt((String) isValueID.toString());
            } else {
                int expr1;
                int expr2;
                for (ParseTree child : children) {

                    // Si entier seul
                    if (child instanceof PlayPlusParser.EntierExprEntContext) {
                        value = Integer.parseInt((String) child.getChild(0).getText());
                    }
                    // Si opérateur
                    if (child.getChild(0) != null && child.getChild(2) != null) {
                        //String expr1Data = child.getChild(0).getText();
                        //String expr2Data = child.getChild(2).getText();

                        //Object expr1ValueId = getValueID(expr1Data);
                        //Object expr2ValueId = getValueID(expr2Data);

                        ParseTree expr1Data = child.getChild(0);
                        ParseTree expr2Data = child.getChild(2);


                        Object expr1ValueId = checkIsVarTemp(expr1Data);
                        Object expr2ValueId = checkIsVarTemp(expr2Data);


                        // Si expr1 est une expression avec opérateur
                        if (child.getChild(0) instanceof PlayPlusParser.ExprEntPlusExprEntContext
                                || child.getChild(0) instanceof PlayPlusParser.ExprEntMinusExprEntContext
                                || child.getChild(0) instanceof PlayPlusParser.ExprEntSlashExprEntContext
                                || child.getChild(0) instanceof PlayPlusParser.ExprEntStarExprEntContext) {
                            expr1 = calculateExprEnt(child.getChild(0));
                        } else if (expr1ValueId != null) {
                            expr1 = Integer.parseInt((String) expr1ValueId);
                        } else {
                            expr1 = Integer.parseInt((String) child.getChild(0).getText());
                        }

                        // Si expr2 est une expression avec opérateur
                        if (child.getChild(2) instanceof PlayPlusParser.ExprEntPlusExprEntContext
                                || child.getChild(2) instanceof PlayPlusParser.ExprEntMinusExprEntContext
                                || child.getChild(2) instanceof PlayPlusParser.ExprEntSlashExprEntContext
                                || child.getChild(2) instanceof PlayPlusParser.ExprEntStarExprEntContext) {
                            expr2 = calculateExprEnt(child.getChild(2));
                        } else if (expr2ValueId != null) {
                            expr2 = Integer.parseInt((String) expr2ValueId.toString());
                        } else {
                            expr2 = Integer.parseInt((String) child.getChild(2).getText());
                        }

                        // Calcul du résultat de l'expression avec opérateur
                        if (child instanceof PlayPlusParser.ExprEntPlusExprEntContext) {
                            value = expr1 + expr2;
                        } else if (child instanceof PlayPlusParser.ExprEntMinusExprEntContext) {
                            value = expr1 - expr2;
                        } else if (child instanceof PlayPlusParser.ExprEntSlashExprEntContext) {
                            value = expr1 / expr2;
                        } else if (child instanceof PlayPlusParser.ExprEntStarExprEntContext) {
                            value = expr1 * expr2;
                        }
                    }


                }
            }

            for (int i = 0; i < value; i++) {
                printer.printWait(200);
                printer.printOnForward("OUT_BC", 100);
                printer.printWait(200);
            }
        }
        // Si pas d'élément, faire une fois jump.
        else {
            printer.printWait(200);
            printer.printOnForward("OUT_BC", 100);
            printer.printWait(200);
        }

        printer.printOff("OUT_BC");

        return null;
    }


    //PEER

    /**
     * Visite le nœud DownAction de l'arbre de syntaxe.
     * <p>
     * Si nous avons 4 éléments, c'est que nous avons des valeurs entre ().
     * Si un des éléments est une variable temporaire, récupérer sa valeur.
     * Sinon, récupérer la valeur numérique correspondante.
     * Si un des éléments est une expression avec opérateur, la calculer.
     * Répéter l'action de descente autant de fois que la valeur obtenue.
     * Si pas d'élément, faire une fois down.
     *
     * @param ctx le nœud à visiter
     * @return
     */
    @Override
    public Void visitDownAction(PlayPlusParser.DownActionContext ctx) {
        List<ParseTree> children = ctx.children;

        //Si nous avons 4 éléments, c'est que nous avons des valeurs entre ().
        if (children.size() == 4) {
            ParseTree childVerif = ctx.children.get(2);

            Object isValueID = checkIsVarTemp(childVerif);

            int value = 0;

            if (isValueID != null) {
                value = Integer.parseInt((String) isValueID.toString());
            } else {
                int expr1;
                int expr2;
                for (ParseTree child : children) {

                    // Si entier seul
                    if (child instanceof PlayPlusParser.EntierExprEntContext) {
                        value = Integer.parseInt((String) child.getChild(0).getText());
                    }
                    // Si opérateur
                    if (child.getChild(0) != null && child.getChild(2) != null) {
                        // String expr1Data = child.getChild(0).getText();
                        // String expr2Data = child.getChild(2).getText();

                        // Object expr1ValueId = getValueID(expr1Data);
                        // Object expr2ValueId = getValueID(expr2Data);

                        ParseTree expr1Data = child.getChild(0);
                        ParseTree expr2Data = child.getChild(2);

                        Object expr1ValueId = checkIsVarTemp(expr1Data);
                        Object expr2ValueId = checkIsVarTemp(expr2Data);


                        // Si expr1 est une expression avec opérateur
                        if (child.getChild(0) instanceof PlayPlusParser.ExprEntPlusExprEntContext
                                || child.getChild(0) instanceof PlayPlusParser.ExprEntMinusExprEntContext
                                || child.getChild(0) instanceof PlayPlusParser.ExprEntSlashExprEntContext
                                || child.getChild(0) instanceof PlayPlusParser.ExprEntStarExprEntContext) {
                            expr1 = calculateExprEnt(child.getChild(0));
                        } else if (expr1ValueId != null) {
                            expr1 = Integer.parseInt((String) expr1ValueId);
                        } else {
                            expr1 = Integer.parseInt((String) child.getChild(0).getText());
                        }

                        // Si expr2 est une expression avec opérateur
                        if (child.getChild(2) instanceof PlayPlusParser.ExprEntPlusExprEntContext
                                || child.getChild(2) instanceof PlayPlusParser.ExprEntMinusExprEntContext
                                || child.getChild(2) instanceof PlayPlusParser.ExprEntSlashExprEntContext
                                || child.getChild(2) instanceof PlayPlusParser.ExprEntStarExprEntContext) {
                            expr2 = calculateExprEnt(child.getChild(2));
                        } else if (expr2ValueId != null) {
                            expr2 = Integer.parseInt((String) expr2ValueId.toString());
                        } else {
                            expr2 = Integer.parseInt((String) child.getChild(2).getText());
                        }

                        // Calcul du résultat de l'expression avec opérateur
                        if (child instanceof PlayPlusParser.ExprEntPlusExprEntContext) {
                            value = expr1 + expr2;
                        } else if (child instanceof PlayPlusParser.ExprEntMinusExprEntContext) {
                            value = expr1 - expr2;
                        } else if (child instanceof PlayPlusParser.ExprEntSlashExprEntContext) {
                            value = expr1 / expr2;
                        } else if (child instanceof PlayPlusParser.ExprEntStarExprEntContext) {
                            value = expr1 * expr2;
                        }
                    }


                }
            }
            for (int i = 0; i < value; i++) {
                printer.printWait(200);
                printer.printOnForward("OUT_BC", - 100);
                printer.printWait(200);
            }
        }
        // Si pas d'élément, faire une fois jump.
        else {
            printer.printWait(200);
            printer.printOnForward("OUT_BC", - 100);
            printer.printWait(200);
        }

        printer.printOff("OUT_BC");

        return null;
    }

    //PEER

    /**
     * Visite le nœud JumpAction de l'arbre de syntaxe.
     * <p>
     * Si nous avons 4 éléments, c'est que nous avons des valeurs entre ().
     * Si un des éléments est une variable temporaire, récupérer sa valeur.
     * Sinon, récupérer la valeur numérique correspondante.
     * Si un des éléments est une expression avec opérateur, la calculer.
     * Répéter l'action de saut autant de fois que la valeur obtenue.
     * Si pas d'élément, faire une fois jump.
     *
     * @param ctx le nœud à visiter
     * @return null
     */
    @Override
    public Void visitJumpAction(PlayPlusParser.JumpActionContext ctx) {
        List<ParseTree> children = ctx.children;

        //Si nous avons 4 éléments, c'est que nous avons des valeurs entre ().
        if (children.size() == 4) {
            ParseTree childVerif = ctx.children.get(2);

            Object isValueID = checkIsVarTemp(childVerif);
            int value = 0;

            if (isValueID != null) {
                value = Integer.parseInt((String) isValueID.toString());
            } else {
                int expr1;
                int expr2;
                for (ParseTree child : children) {

                    // Si entier seul
                    if (child instanceof PlayPlusParser.EntierExprEntContext) {
                        value = Integer.parseInt((String) child.getChild(0).getText());
                    }
                    // Si opérateur
                    if (child.getChild(0) != null && child.getChild(2) != null) {
                        //String expr1Data = child.getChild(0).getText();
                        //String expr2Data = child.getChild(2).getText();

                        //Object expr1ValueId = getValueID(expr1Data);
                        //Object expr2ValueId = getValueID(expr2Data);

                        ParseTree expr1Data = child.getChild(0);
                        ParseTree expr2Data = child.getChild(2);

                        Object expr1ValueId = checkIsVarTemp(expr1Data);
                        Object expr2ValueId = checkIsVarTemp(expr2Data);


                        // Si expr1 est une expression avec opérateur
                        if (child.getChild(0) instanceof PlayPlusParser.ExprEntPlusExprEntContext
                                || child.getChild(0) instanceof PlayPlusParser.ExprEntMinusExprEntContext
                                || child.getChild(0) instanceof PlayPlusParser.ExprEntSlashExprEntContext
                                || child.getChild(0) instanceof PlayPlusParser.ExprEntStarExprEntContext) {
                            expr1 = calculateExprEnt(child.getChild(0));
                        } else if (expr1ValueId != null) {
                            expr1 = Integer.parseInt((String) expr1ValueId.toString());
                        } else {
                            expr1 = Integer.parseInt((String) child.getChild(0).getText());
                        }

                        // Si expr2 est une expression avec opérateur
                        if (child.getChild(2) instanceof PlayPlusParser.ExprEntPlusExprEntContext
                                || child.getChild(2) instanceof PlayPlusParser.ExprEntMinusExprEntContext
                                || child.getChild(2) instanceof PlayPlusParser.ExprEntSlashExprEntContext
                                || child.getChild(2) instanceof PlayPlusParser.ExprEntStarExprEntContext) {
                            expr2 = calculateExprEnt(child.getChild(2));
                        } else if (expr2ValueId != null) {
                            expr2 = Integer.parseInt((String) expr2ValueId);
                        } else {
                            expr2 = Integer.parseInt((String) child.getChild(2).getText());
                        }

                        // Calcul du résultat de l'expression avec opérateur
                        if (child instanceof PlayPlusParser.ExprEntPlusExprEntContext) {
                            value = expr1 + expr2;
                        } else if (child instanceof PlayPlusParser.ExprEntMinusExprEntContext) {
                            value = expr1 - expr2;
                        } else if (child instanceof PlayPlusParser.ExprEntSlashExprEntContext) {
                            value = expr1 / expr2;
                        } else if (child instanceof PlayPlusParser.ExprEntStarExprEntContext) {
                            value = expr1 * expr2;
                        }
                    }


                }
            }
            for (int i = 0; i < value; i++) {
                printer.printWait(200);
                printer.printRobotJump();
            }
        }
        // Si pas d'élément, faire une fois jump.
        else {
            printer.printWait(200);
            printer.printRobotJump();
        }

        return null;
    }

    //PEER

    /**
     * Visite l'action de combat d'un robot dans le contexte spécifié et exécute les actions correspondantes :
     * <p>
     * Attendre pendant 200 millisecondes avant d'exécuter l'action suivante
     * Imprimer la commande de combat de robot
     *
     * @param ctx le contexte de l'action de combat de robot à visiter
     * @return null car la méthode ne retourne rien (type Void)
     */
    @Override
    public Void visitFightAction(PlayPlusParser.FightActionContext ctx) {

        printer.printWait(200);
        printer.printRobotFight();

        return null;
    }

    //PEER

    /**
     * Visite l'action de creuser dans l'arbre d'analyse syntaxique PlayPlus.
     * <p>
     * Imprime le message de creuser avec le robot et attend 200 millisecondes.
     *
     * @param ctx Le contexte de l'action de creuser dans l'arbre d'analyse syntaxique PlayPlus.
     * @return null
     */
    @Override
    public Void visitDigAction(PlayPlusParser.DigActionContext ctx) {

        printer.printWait(200);
        printer.printRobotDig();

        return null;
    }


    /****************************
     *                          *
     *           AUTRES         *
     *                          *
     ****************************/

    /**
     * Cette méthode vérifie si un des parents de la variable est une instance de InstBlock,
     * afin de renommer la variable en ajoutant le nom de la fonction, sinon elle renvoie
     * simplement la valeur de l'ID.
     *
     * @param child le nœud de départ à partir duquel la recherche doit commencer
     * @return la valeur de l'ID.
     */
    private Object checkIsVarTemp(ParseTree child) {
        ParseTree currentNode = child;
        ; // Le nœud à partir duquel vous souhaitez remonter dans l'arbre
        boolean foundInstBlock = false;
        Object isValueID;
        String functionName = null;

        while (currentNode != null) {
            if (currentNode instanceof PlayPlusParser.InstBlockContext) {
                foundInstBlock = true;
                functionName = currentNode.getParent().getChild(1).getText();
                break;
            }
            if (currentNode instanceof PlayPlusParser.ProgrammeContext) {
                break; // Arrêter la recherche lorsque nous atteignons l'instance programme
            }
            currentNode = currentNode.getParent();
        }

        // Le parent est une instance de instBlock
        // Si il trouve une valeur, il faut vérifié que celui-ci n'est pas un ID
        if (foundInstBlock) {
            isValueID = getValueID(child.getText() + "_" + functionName);
            if (isValueID == null) {
                return isValueID;
            } else {
                Object isValueID2 = getValueID(isValueID.toString());
                return isValueID2;
            }

        }
        // Le parent n'est pas une instance de instBlock
        else {
            isValueID = getValueID(child.getText());

            return isValueID;
        }


    }


    /**
     * Retourne la valeur associée à l'identifiant spécifié dans la table des symboles. Si l'identifiant n'existe pas
     * dans la table des symboles, retourne null.
     *
     * @param varId le nom de l'identifiant dont on veut récupérer la valeur
     * @return la valeur associée à l'identifiant, ou null si l'identifiant n'existe pas dans la table des symboles
     */
    public Object getValueID(String varId) {
        Symbol symbol;


        for (int indexVarNameCount = indexVarName; indexVarNameCount != 0; indexVarNameCount--) {
            symbol = symTable.get(currentPorte).getSymbols(varId);
            if (symbol == null) {
                symbol = symTable.get(0).getSymbols(varId);
                if (symbol != null) {
                    return symTable.get(0).getSymbols(varId).getValue();
                }
            } else {
                return symTable.get(currentPorte).getSymbols(varId).getValue();
            }
        }


        symbol = symTable.get(0).getSymbols(varId);
        if (symbol == null) {
            symbol = symTable.get(currentPorte).getSymbols(varId);
            if (symbol == null) {
                return null;
            } else {
                return symTable.get(currentPorte).getSymbols(varId).getValue();
            }
        } else {
            return symTable.get(0).getSymbols(varId).getValue();
        }

    }

    /**
     * Renvoie le symbole associé à l'identifiant spécifié.
     * <p>
     * Cette méthode recherche d'abord le symbole associé à l'identifiant dans la table des symboles de la portée actuelle.
     * Si le symbole n'est pas trouvé dans cette table, la méthode recherche dans la table des symboles de la portée globale.
     * Si le symbole est trouvé dans une des tables, il est renvoyé. Sinon, la méthode renvoie null.
     *
     * @param varId le nom de l'identifiant recherché
     * @return le symbole correspondant à l'identifiant ou null s'il n'existe pas
     */
    public Symbol getSymbol(String varId) {
        Symbol symbol = symTable.get(currentPorte).getSymbols(varId);
        if (symbol == null) {
            symbol = symTable.get(0).getSymbols(varId);
            if (symbol == null) {
                return null;
            } else {
                return symbol;
            }
        } else {
            return symbol;
        }
    }

    //PEER
    //Calcul les expressions Entier

    /**
     * Calcule la valeur entière d'une expression arithmétique représentée sous forme d'arbre syntaxique.
     *
     * @param expr l'arbre syntaxique représentant l'expression arithmétique
     * @return la valeur entière de l'expression arithmétique
     */
    private int calculateExprEnt(ParseTree expr) {
        // Si l'expression est une valeur entière
        if (expr instanceof PlayPlusParser.EntierExprEntContext) {
            return Integer.parseInt(expr.getText());
        }
        // Si l'expression est une opération arithmétique
        else if (expr instanceof PlayPlusParser.ExprEntPlusExprEntContext
                || expr instanceof PlayPlusParser.ExprEntMinusExprEntContext
                || expr instanceof PlayPlusParser.ExprEntStarExprEntContext
                || expr instanceof PlayPlusParser.ExprEntSlashExprEntContext) {

            // Récupérer les sous-expressions gauche et droite
            //ParseTree leftExpr = expr.getChild(0);
            //ParseTree rightExpr = expr.getChild(2);

            //Object expr1ValueId = getValueID(leftExpr.getText());
            //Object expr2ValueId = getValueID(rightExpr.getText());


            ParseTree leftExpr = expr.getChild(0);
            ParseTree rightExpr = expr.getChild(2);

            Object expr1ValueId = checkIsVarTemp(leftExpr);
            Object expr2ValueId = checkIsVarTemp(rightExpr);


            // Calculer les valeurs des sous-expressions gauche et droite
            int leftValue = 0;
            int rightValue = 0;

            if (expr1ValueId == null) {
                leftValue = calculateExprEnt(leftExpr);
            } else {
                leftValue = Integer.parseInt((String) expr1ValueId.toString());
            }
            if (expr2ValueId == null) {
                rightValue = calculateExprEnt(rightExpr);
            } else {
                rightValue = Integer.parseInt((String) expr2ValueId.toString());
            }

            // Calculer la valeur de l'expression en fonction de l'opérateur
            if (expr instanceof PlayPlusParser.ExprEntPlusExprEntContext) {
                return leftValue + rightValue;
            } else if (expr instanceof PlayPlusParser.ExprEntMinusExprEntContext) {
                return leftValue - rightValue;
            } else if (expr instanceof PlayPlusParser.ExprEntStarExprEntContext) {
                return leftValue * rightValue;
            } else if (expr instanceof PlayPlusParser.ExprEntSlashExprEntContext) {
                return leftValue / rightValue;
            }
        }
        // Si l'expression est entre parenthèses
        else if (expr instanceof PlayPlusParser.ParExprEntExprEntContext) {
            // Récupérer l'expression entre parenthèses

            ParseTree exprInPar = expr.getChild(1);
            //Object exprValueId = getValueID(exprInPar.getText());
            Object exprValueId = checkIsVarTemp(exprInPar);

            int exprValue = 0;

            if (exprValueId == null) {
                exprValue = calculateExprEnt(exprInPar);
            } else {
                exprValue = Integer.parseInt((String) exprValueId.toString());
            }

            return exprValue;
        }

        return 0;
    }

    //PEER
    //Calcul les expressions Bool

    /**
     * Calcule la valeur booléenne d'une expression booléenne représentée sous forme d'arbre syntaxique.
     *
     * @param expr l'arbre syntaxique représentant l'expression booléenne
     * @return la valeur booléenne de l'expression booléenne
     */
    private boolean calculateExprBool(ParseTree expr) {
        // Si l'expression est un booléen vrai ou faux
        if (expr instanceof PlayPlusParser.ExprBoolExprDContext
                || expr instanceof PlayPlusParser.TrueExprBoolContext
                || expr instanceof PlayPlusParser.FalseExprBoolContext) {

            return Boolean.parseBoolean(expr.getText());
        }

        // Si l'expression est une comparaison avec 2 symboles
        else if (expr instanceof PlayPlusParser.EqualExprBoolContext
                || expr instanceof PlayPlusParser.ExprEntSmallEqualExprBoolContext
                || expr instanceof PlayPlusParser.ExprEntBigEqualExprBoolContext
                || expr instanceof PlayPlusParser.ExprEntEqualExprBoolContext
                || expr instanceof PlayPlusParser.ExprEntExclEqualExprBoolContext) {


            // Récupérer les sous-expressions gauche et droite.
            ParseTree leftExpr = expr.getChild(0);
            ParseTree rightExpr = expr.getChild(3);

            //Object expr1ValueId = getValueID(leftExpr.getText());
            //Object expr2ValueId = getValueID(rightExpr.getText());

            Object expr1ValueId = checkIsVarTemp(leftExpr);
            Object expr2ValueId = checkIsVarTemp(rightExpr);

            // Calculer les valeurs des sous-expressions gauche et droite
            int leftValue = 0;
            int rightValue = 0;

            if (expr1ValueId == null) {
                leftValue = calculateExprEnt(leftExpr);
            } else {
                leftValue = Integer.parseInt((String) expr1ValueId.toString());
            }
            if (expr2ValueId == null) {
                rightValue = calculateExprEnt(rightExpr);
            } else {
                rightValue = Integer.parseInt((String) expr2ValueId.toString());
            }


            // Calculer la valeur de l'expression en fonction de l'opérateur
            if (expr instanceof PlayPlusParser.EqualExprBoolContext) {
                return leftValue == rightValue;
            } else if (expr instanceof PlayPlusParser.ExprEntSmallEqualExprBoolContext) {
                return leftValue <= rightValue;
            } else if (expr instanceof PlayPlusParser.ExprEntBigEqualExprBoolContext) {
                return leftValue >= rightValue;
            } else if (expr instanceof PlayPlusParser.ExprEntEqualExprBoolContext) {
                return leftValue == rightValue;
            } else if (expr instanceof PlayPlusParser.ExprEntExclEqualExprBoolContext) {
                return leftValue != rightValue;
            }
        }

        //  Si l'expression est une comparaison avec 1 symbole
        else if (expr instanceof PlayPlusParser.ExprEntSmallExprBoolContext
                || expr instanceof PlayPlusParser.ExprEntBigExprBoolContext) {


            // Récupérer les sous-expressions gauche et droite
            ParseTree leftExpr = expr.getChild(0);
            ParseTree rightExpr = expr.getChild(2);

            // Object expr1ValueId = getValueID(leftExpr.getText());
            // Object expr2ValueId = getValueID(rightExpr.getText());


            Object expr1ValueId = checkIsVarTemp(leftExpr);
            Object expr2ValueId = checkIsVarTemp(rightExpr);


            // Calculer les valeurs des sous-expressions gauche et droite
            int leftValue = 0;
            int rightValue = 0;

            if (expr1ValueId == null) {
                leftValue = calculateExprEnt(leftExpr);
            } else {
                if (expr1ValueId.toString().contains("+") || expr1ValueId.toString().contains("-") || expr1ValueId.toString().contains("*") || expr1ValueId.toString().contains("/")) {
                    leftValue = calculateExprEnt(leftExpr);
                } else {
                    if (expr1ValueId.toString().equals("true")) {
                        leftValue = 1;
                    } else if (expr1ValueId.toString().equals("false")) {
                        leftValue = 0;
                    } else {
                        leftValue = Integer.parseInt((String) expr1ValueId.toString());
                    }

                }
            }

            if (expr2ValueId == null) {
                rightValue = calculateExprEnt(rightExpr);
            } else {
                if (expr2ValueId.toString().contains("+") || expr2ValueId.toString().contains("-") || expr2ValueId.toString().contains("*") || expr2ValueId.toString().contains("/")) {
                    rightValue = calculateExprEnt(leftExpr);
                } else {
                    rightValue = Integer.parseInt((String) expr2ValueId.toString());
                }
            }

            // Calculer la valeur de l'expression en fonction de l'opérateur
            if (expr instanceof PlayPlusParser.ExprEntSmallExprBoolContext) {
                return leftValue < rightValue;
            } else if (expr instanceof PlayPlusParser.ExprEntBigExprBoolContext) {
                return leftValue > rightValue;
            }
        }

        // Si l'expression est une négation
        else if (expr instanceof PlayPlusParser.ExclExprBoolContext) {
            ParseTree boolExpr = expr.getChild(1);

            // Récupérer la valeur booléenne de l'expression à nier
            boolean boolValue = calculateExprBool(boolExpr);

            // Nier la valeur booléenne
            return ! boolValue;
        }

        // Si l'expression est une conjonction ou une disjonction
        else if (expr instanceof PlayPlusParser.AndExprBoolContext
                || expr instanceof PlayPlusParser.OrExprBoolContext) {


            // Récupérer les sous-expressions gauche et droite
            ParseTree leftExpr = expr.getChild(0);
            ParseTree rightExpr = expr.getChild(3);


            // Calculer la valeur booléenne des sous-expressions gauche et droite
            boolean leftValue = calculateExprBool(leftExpr);
            boolean rightValue = calculateExprBool(rightExpr);


            // Calculer la valeur de l'expression en fonction de l'opérateur
            if (expr instanceof PlayPlusParser.AndExprBoolContext) {
                return leftValue && rightValue;
            } else if (expr instanceof PlayPlusParser.OrExprBoolContext) {
                return leftValue || rightValue;
            }
        }

        //Si l'expression est entre paranthèse
        else if (expr instanceof PlayPlusParser.ParExprBoolContext) {
            // Récupérer la sous-expression entre paranthèses
            ParseTree boolExpr = expr.getChild(1);
            // Calculer la valeur booléenne de la sous-expression entre paranthèses
            return calculateExprBool(boolExpr);
        }

        return false; // Si l'expression ne correspond à aucun des cas ci-dessus, retourner faux par défaut

    }


    //TODO

    /**
     * Teste les méthodes précédentes.
     *
     * @param child         ParseTree à tester.
     * @param varId         identifiant de la variable à modifier.
     * @param varIdInStruct identifiant de la variable dans la structure (null si la variable n'est pas dans une structure).
     * @return la valeur de l'expression.
     */
    public String test(ParseTree child, String varId, String varIdInStruct) {

        // Si Expression Entier/Bool/Char seul
        if (child instanceof PlayPlusParser.EntierExprEntContext
                || child instanceof PlayPlusParser.TrueExprBoolContext
                || child instanceof PlayPlusParser.FalseExprBoolContext
                || child instanceof PlayPlusParser.ExprCharExprDContext) {

            if (varIdInStruct == null) {
                Symbol symbol = symTable.get(0).getSymbols(varId);
                if (symbol == null) {
                    symbol = symTable.get(currentPorte).getSymbols(varId);
                    if (symbol != null) {
                        if (! symbol.isConstant()) {
                            symbol.setValue(child.getText());
                            if (child instanceof PlayPlusParser.TrueExprBoolContext) {
                                printer.printVariableSet(varId, "1");
                                return child.getText();
                            } else if (child instanceof PlayPlusParser.FalseExprBoolContext) {
                                printer.printVariableSet(varId, "0");
                                return child.getText();
                            } else {
                                printer.printVariableSet(varId, child.getText());
                                return child.getText();
                            }

                        }
                    } else {
                        return child.getText();
                    }
                } else {
                    if (! symbol.isConstant()) {
                        symbol.setValue(child.getText());
                        if (child instanceof PlayPlusParser.TrueExprBoolContext) {
                            printer.printVariableSet(varId, "1");
                            return child.getText();
                        } else if (child instanceof PlayPlusParser.FalseExprBoolContext) {
                            printer.printVariableSet(varId, "0");
                            return child.getText();
                        } else {
                            printer.printVariableSet(varId, child.getText());
                            return child.getText();
                        }
                    } else {
                        return child.getText();
                    }
                }
            } else if (varIdInStruct != null) {
                StructSymbol structSymbol = (StructSymbol) symTable.get(0).getSymbols(varId);
                if (structSymbol == null) {
                    structSymbol = (StructSymbol) symTable.get(currentPorte).getSymbols(varId);
                    if (structSymbol != null) {
                        //Check si la variable existe
                        for (ParseTree varName : structSymbol.getContext().children) {
                            for (int i = 0; i != varName.getChildCount(); i++) {
                                if (varName.getChild(i) instanceof PlayPlusParser.ListVarNameContext) {
                                    for (int j = 0; j != varName.getChild(i).getChildCount(); j++) {
                                        if (Objects.equals(varName.getChild(i).getChild(j).getText(), varIdInStruct)) {
                                            if (child.getText().equals("true")) {
                                                printer.printVariableSet(varName.getChild(i).getChild(j).getText(), child.getText());
                                            } else if (child.getText().equals("false")) {
                                                printer.printVariableSet(varName.getChild(i).getChild(j).getText(), child.getText());
                                            } else {
                                                printer.printVariableSet(varName.getChild(i).getChild(j).getText(), child.getText());
                                            }
                                        }
                                    }
                                }
                            }
                        }

                    }
                } else {
                    //Check si la variable existe
                    for (ParseTree varName : structSymbol.getContext().children) {
                        for (int i = 0; i != varName.getChildCount(); i++) {
                            if (varName.getChild(i) instanceof PlayPlusParser.ListVarNameContext) {
                                for (int j = 0; j != varName.getChild(i).getChildCount(); j++) {
                                    if (Objects.equals(varName.getChild(i).getChild(j).getText(), varIdInStruct)) {
                                        if (child.getText().equals("true")) {
                                            printer.printVariableSet(varName.getChild(i).getChild(j).getText(), child.getText());
                                        } else if (child.getText().equals("false")) {
                                            printer.printVariableSet(varName.getChild(i).getChild(j).getText(), child.getText());
                                        } else {
                                            printer.printVariableSet(varName.getChild(i).getChild(j).getText(), child.getText());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }

        // Si Expression Bool
        else if (child instanceof PlayPlusParser.AndExprBoolContext
                || child instanceof PlayPlusParser.OrExprBoolContext
                || child instanceof PlayPlusParser.ExclExprBoolContext
                || child instanceof PlayPlusParser.EqualExprBoolContext
                || child instanceof PlayPlusParser.ExprEntSmallExprBoolContext
                || child instanceof PlayPlusParser.ExprEntSmallEqualExprBoolContext
                || child instanceof PlayPlusParser.ExprEntBigExprBoolContext
                || child instanceof PlayPlusParser.ExprEntBigEqualExprBoolContext
                || child instanceof PlayPlusParser.ExprEntEqualExprBoolContext
                || child instanceof PlayPlusParser.ExprEntExclEqualExprBoolContext
                || child instanceof PlayPlusParser.ExprCharEqualExprBoolContext
                || child instanceof PlayPlusParser.ExprCharExclExprBoolContext
                || child instanceof PlayPlusParser.ParExprBoolContext) {
            //ctx.getChild(4) instanceof PlayPlusParser.ExprBoolExprDContext

            String expr2Data = null;
            Object expr1ValueId = null;
            Object expr2ValueId = null;
            Boolean expr1Value = null;
            Boolean expr2Value = null;

            // Si paranthèse, on rentre dans enfant 1
            if (varIdInStruct != null) {
                if (child instanceof PlayPlusParser.ParExprBoolContext) {
                    child = child.getChild(1).getChild(0);
                }
            } else if (varIdInStruct == null) {
                if (child instanceof PlayPlusParser.ParExprBoolContext) {
                    child = child.getChild(1);
                }
            }

            String expr1Data = child.getChild(0).getText();

            // Cherche la partie de droite, ne pas prendre en compte les symboles
            Boolean find = false;
            int numberChildLeft = 1;
            while (! find) {
                String check = child.getChild(numberChildLeft).getText();
                if (! (check.equals(">") || check.equals("<") || check.equals("=") || check.equals("!") || check.equals("&") || check.equals("|"))) {
                    expr2Data = child.getChild(numberChildLeft).getText();
                    find = true;
                } else {
                    numberChildLeft++;
                }
            }

            // Vérifier si l'expression gauche a besoin d'être évaluée.
            if ((expr1Data.contains("+") || expr1Data.contains("-") || expr1Data.contains("*") || expr1Data.contains("/")) && (expr1Data.contains("&&") || expr1Data.contains("||") || expr1Data.contains(">") || expr1Data.contains("<") || expr1Data.contains("="))) {
                expr1ValueId = calculateExprBool(child.getChild(0));
            } else if (expr1Data.contains("+") || expr1Data.contains("-") || expr1Data.contains("*") || expr1Data.contains("/")) {
                expr1ValueId = calculateExprEnt(child.getChild(0));
            } else if (expr1Data.contains("&&") || expr1Data.contains("||") || expr1Data.contains(">") || expr1Data.contains("<") || expr1Data.contains("=")) {
                expr1ValueId = calculateExprBool(child.getChild(0));
            }
            ///else if(expr1Data.contains("true") || expr1Data.contains("false")) {
            ///    expr1Value = Boolean.valueOf(expr1Data);
            ///}
            ///else if(expr1Data.matches(".*[a-zA-Z].*")) {
            ///    expr1ValueId = getValueID(expr1Data);
            ///}
            ///else {
            ///    expr1ValueId = expr1Data;
            ///}
            else {
                expr1Value = Boolean.valueOf(expr1Data);
            }


            // Vérifier si l'expression droite a besoin d'être évaluée
            if ((expr2Data.contains("+") || expr2Data.contains("-") || expr2Data.contains("*") || expr2Data.contains("/")) && (expr2Data.contains("&&") || expr2Data.contains("||") || expr2Data.contains(">") || expr2Data.contains("<") || expr2Data.contains("="))) {
                expr2ValueId = calculateExprBool(child.getChild(numberChildLeft));
            } else if (expr2Data.contains("+") || expr2Data.contains("-") || expr2Data.contains("*") || expr2Data.contains("/")) {
                expr2ValueId = calculateExprEnt(child.getChild(numberChildLeft));
            } else if (expr2Data.contains("&&") || expr2Data.contains("||") || expr2Data.contains(">") || expr2Data.contains("<") || expr2Data.contains("=")) {
                expr2ValueId = calculateExprBool(child.getChild(numberChildLeft));
            }
            ///  else if(expr2Data.contains("true") || expr2Data.contains("false")) {
            ///      expr2Value = Boolean.valueOf(expr2Data);
            ///  }
            ///  else if(expr2Data.matches(".*[a-zA-Z].*")) {
            ///      expr2ValueId = getValueID(expr2Data);
            ///  }
            ///  else {
            ///      expr2ValueId = expr2Data;
            ///  }
            else {
                expr2Value = Boolean.valueOf(expr2Data);
            }


            // Calculer les valeurs des sous-expressions gauche et droite si elles n'ont pas déjà été évaluées
            int leftValueInt = 0;
            int rightValueInt = 0;
            Boolean leftValueBool = null;
            Boolean rightValueBool = null;


            // Gauche
            if (expr1Data.contains("&&") || expr1Data.contains("||")) {
                leftValueBool = (Boolean) expr1ValueId;
            } else if (expr1Data.contains("+") || expr1Data.contains("-") || expr1Data.contains("*") || expr1Data.contains("/") || expr1Data.contains(">") || expr1Data.contains("<") || expr1Data.contains("=")) {
                if (expr1ValueId.toString().equals("true") || expr1ValueId.toString().equals("false")) {
                    leftValueBool = Boolean.parseBoolean(expr1ValueId.toString());
                } else {
                    leftValueInt = Integer.parseInt((String) expr1ValueId.toString());
                }
            } else if (expr1Value != null) {
                leftValueBool = expr1Value;
            } else if (expr1ValueId == null) {
                leftValueInt = calculateExprEnt(child.getChild(0));
            }
            ///  else if(expr1Data.contains("true") || expr1Data.contains("false")) {
            ///      leftValueBool =  Boolean.parseBoolean(expr1ValueId.toString());
            ///  }
            else {
                leftValueInt = Integer.parseInt(expr1ValueId.toString());
            }


            // Droite
            if (expr2Data.contains("&&") || expr2Data.contains("||")) {
                rightValueBool = (Boolean) expr2ValueId;
            } else if (expr2Data.contains("+") || expr2Data.contains("-") || expr2Data.contains("*") || expr2Data.contains("/") || expr2Data.contains(">") || expr2Data.contains("<") || expr2Data.contains("=")) {
                if (expr2ValueId.toString().equals("true") || expr2ValueId.toString().equals("false")) {
                    rightValueBool = Boolean.parseBoolean(expr2ValueId.toString());
                } else {
                    rightValueInt = Integer.parseInt((String) expr2ValueId.toString());
                }
            } else if (expr2Value != null) {
                rightValueBool = expr2Value;
            } else if (expr2ValueId == null) {
                rightValueInt = calculateExprEnt(child.getChild(numberChildLeft));
            }
            ///   else if(expr2Data.contains("true") || expr2Data.contains("false")) {
            ///       rightValueBool =  Boolean.parseBoolean(expr2ValueId.toString());
            ///   }
            else {
                rightValueInt = Integer.parseInt(expr2ValueId.toString());
            }


            // Calculer la valeur de l'expression en fonction de l'opérateur
            boolean resultBool = false;


            if (child.getChild(1).getText().equals("=")) {
                resultBool = leftValueInt == rightValueInt;
            } else if (child.getChild(1).getText().equals("<")) {
                resultBool = leftValueInt < rightValueInt;
            } else if (child.getChild(1).getText().equals(">")) {
                resultBool = leftValueInt > rightValueInt;
            } else if (child.getChild(1).getText().equals("<") && child.getChild(2).getText().equals("=")) {
                resultBool = leftValueInt <= rightValueInt;
            } else if (child.getChild(1).getText().equals(">") && child.getChild(2).getText().equals("<=")) {
                resultBool = leftValueInt >= rightValueInt;
            } else if (child.getChild(1).getText().equals("<") && child.getChild(2).getText().equals(">")) {
                resultBool = leftValueInt != rightValueInt;
            } else if (child.getChild(1).getText().equals("&") && child.getChild(2).getText().equals("&")) {
                resultBool = leftValueBool && rightValueBool;
            } else if (child.getChild(1).getText().equals("|") && child.getChild(2).getText().equals("|")) {
                resultBool = leftValueBool || rightValueBool;

            }


            // Sauvegarder le résultat final
            if (varIdInStruct != null) {
                StructSymbol structSymbol = (StructSymbol) symTable.get(0).getSymbols(varId);
                if (structSymbol == null) {
                    structSymbol = (StructSymbol) symTable.get(currentPorte).getSymbols(varId);
                    if (structSymbol != null) {
                        //Check si la variable existe
                        for (ParseTree varName : structSymbol.getContext().children) {
                            for (int i = 0; i != varName.getChildCount(); i++) {
                                if (varName.getChild(i) instanceof PlayPlusParser.ListVarNameContext) {
                                    for (int j = 0; j != varName.getChild(i).getChildCount(); j++) {
                                        if (Objects.equals(varName.getChild(i).getChild(j).getText(), varIdInStruct)) {
                                            if (String.valueOf(resultBool).equals("true")) {
                                                printer.printVariableSet(varIdInStruct, "1");
                                            } else if (String.valueOf(resultBool).equals("false")) {
                                                printer.printVariableSet(varIdInStruct, "0");
                                            }
                                        }
                                    }
                                }
                            }
                        }

                    }
                } else {
                    //Check si la variable existe
                    for (ParseTree varName : structSymbol.getContext().children) {
                        for (int i = 0; i != varName.getChildCount(); i++) {
                            if (varName.getChild(i) instanceof PlayPlusParser.ListVarNameContext) {
                                for (int j = 0; j != varName.getChild(i).getChildCount(); j++) {
                                    if (Objects.equals(varName.getChild(i).getChild(j).getText(), varIdInStruct)) {
                                        if (String.valueOf(resultBool).equals("true")) {
                                            printer.printVariableSet(varIdInStruct, "1");
                                        } else if (String.valueOf(resultBool).equals("false")) {
                                            printer.printVariableSet(varIdInStruct, "0");
                                        }
                                    }
                                }
                            }

                        }
                    }
                }
            } else if (varIdInStruct == null) {
                if (String.valueOf(resultBool).equals("true")) {
                    printer.printVariableSet(varId, "1");
                } else if (String.valueOf(resultBool).equals("false")) {
                    printer.printVariableSet(varId, "0");
                }
                return String.valueOf(resultBool);
            }

        }


        // Si Expression Entier
        else if (child instanceof PlayPlusParser.ExprEntPlusExprEntContext
                || child instanceof PlayPlusParser.ExprEntMinusExprEntContext
                || child instanceof PlayPlusParser.ExprEntSlashExprEntContext
                || child instanceof PlayPlusParser.ExprEntStarExprEntContext
                || child instanceof PlayPlusParser.ParExprEntExprEntContext) {

            //String expr1Data;
            //String expr2Data;
            ParseTree expr1Data;
            ParseTree expr2Data;
            String expr1 = null;
            String expr2 = null;
            int expr1Int = 0;
            int expr2Int = 0;
            int resultInt = 0;

            // Si paranthèse
            if (child instanceof PlayPlusParser.ParExprEntExprEntContext) {
                child = child.getChild(1);
            }

            // expr1Data = child.getChild(0).getText();
            // expr2Data = child.getChild(2).getText();

            // Object expr1ValueId = getValueID(expr1Data);
            // Object expr2ValueId = getValueID(expr2Data);

            expr1Data = child.getChild(0);
            expr2Data = child.getChild(2);

            Object expr1ValueId = checkIsVarTemp(expr1Data);
            Object expr2ValueId = checkIsVarTemp(expr2Data);

            // Si expr1 est une expression avec opérateur
            if (child.getChild(0) instanceof PlayPlusParser.ExprEntPlusExprEntContext
                    || child.getChild(0) instanceof PlayPlusParser.ExprEntMinusExprEntContext
                    || child.getChild(0) instanceof PlayPlusParser.ExprEntSlashExprEntContext
                    || child.getChild(0) instanceof PlayPlusParser.ExprEntStarExprEntContext
                    || child.getChild(0) instanceof PlayPlusParser.ParExprEntExprEntContext) {
                expr1 = String.valueOf(calculateExprEnt(child.getChild(0)));
                expr1Int = calculateExprEnt(child.getChild(0));

            } else if (expr1ValueId != null) {
                expr1 = expr1Data.getText();
                if (expr1ValueId.equals("true")) {
                    expr1Int = 1;
                } else if (expr1ValueId.equals("false")) {
                    expr1Int = 0;
                } else {
                    expr1Int = Integer.parseInt((String) expr1ValueId);
                }

            } else {
                if (expr1Data.getText().matches(".*[a-zA-Z].*")) {
                    expr1 = expr1Data.getText();
                } else {
                    expr1 = expr1Data.getText();
                    expr1Int = Integer.parseInt(expr1Data.getText());
                }
            }


            // Si expr2 est une expression avec opérateur
            if (child.getChild(2) instanceof PlayPlusParser.ExprEntPlusExprEntContext
                    || child.getChild(2) instanceof PlayPlusParser.ExprEntMinusExprEntContext
                    || child.getChild(2) instanceof PlayPlusParser.ExprEntSlashExprEntContext
                    || child.getChild(2) instanceof PlayPlusParser.ExprEntStarExprEntContext
                    || child.getChild(2) instanceof PlayPlusParser.ParExprEntExprEntContext) {
                expr2 = String.valueOf(calculateExprEnt(child.getChild(2)));
                expr2Int = calculateExprEnt(child.getChild(2));
            } else if (expr2ValueId != null) {
                expr2 = expr2Data.getText();
                expr2Int = Integer.parseInt((String) expr2ValueId);
            } else {
                if (expr2Data.getText().matches(".*[a-zA-Z].*")) {
                    expr2 = expr2Data.getText();
                } else {
                    expr2 = expr2Data.getText();
                    expr2Int = Integer.parseInt(expr2Data.getText());

                }

            }


            // Calcul du résultat de l'expression avec opérateur
            if (child instanceof PlayPlusParser.ExprEntPlusExprEntContext) {
                resultInt = expr1Int + expr2Int;
                printer.printPlusExprEnt(varId, expr1, expr2);
            } else if (child instanceof PlayPlusParser.ExprEntMinusExprEntContext) {
                resultInt = expr1Int - expr2Int;
                printer.printSlashExprEnt(varId, expr1, expr2);
            } else if (child instanceof PlayPlusParser.ExprEntSlashExprEntContext) {
                resultInt = expr1Int / expr2Int;
                printer.printMinusExprEnt(varId, expr1, expr2);
            } else if (child instanceof PlayPlusParser.ExprEntStarExprEntContext) {
                resultInt = expr1Int * expr2Int;
                printer.printStarExprEnt(varId, expr1, expr2);
            }

        }

        return null;
    }

    //PEER
    //Créer une variable supplémentaire

    /**
     * Cette méthode permet de créer une nouvelle variable supplémentaire à partir
     * d'un préfixe et d'un type de variable donnés.
     *
     * @param prefix  le préfixe de la variable à créer
     * @param typeVar le type de variable à créer (int, char ou bool)
     * @return le nom de la nouvelle variable créée
     */
    private String createVarSupp(String prefix, String typeVar) {
        int num = 0;
        String varName;
        do {
            varName = prefix + num;
            num++;
        }
        while (symTable.get(0).getSymbols(varName).getName() != null && symTable.get(currentPorte).getSymbols(varName).getName() != null);

        NBCPrinter.NBCCodeTypes type = null;
        switch (typeVar) {
            case "int":
                type = NBCPrinter.NBCCodeTypes.Int;
                break;
            case "char":
                type = NBCPrinter.NBCCodeTypes.Char;
                break;
            case "bool":
                type = NBCPrinter.NBCCodeTypes.Bool;
                break;
            default:
                break;
        }


        //printer.printEnterInitVariable();
        printer.printInitVariable(type, varName);
        //printer.printEndInitVariable();

        return varName;
    }


}
