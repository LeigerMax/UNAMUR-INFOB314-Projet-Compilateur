grammar PlayPlus;

import PlayPlusWords;

root: programme  #rootProgramme
    | map        #rootMap
    ;

programme: impDecl
    ( varDecl+ | fctDecl+ | constDecl+ | structDecl+ | enumDecl+ | typedefDecl+ )*
    main;

// Import file
impDecl: HASH IMPORT DQUOTE fileDecl DQUOTE;
fileDecl: fileName POINT MAP;
fileName: ID;

// Déclaration main
main: VOID MAIN LPAR RPAR LBRACKET
         (DIG LPAR RPAR SEMICOLON | (instruction))+
         RETURN VOID SEMICOLON
         RBRACKET;

// Déclaration map
map: MAP COLON ENTIER ENTIER (ROBOT|TRESOR|PELOUSE|PALMIERS|PONS|BUISSON|TONNEAU|PUITS|VIDE|SQUELETTE)+;

// Déclaration d'un string
// On précise tous les caractères qu'on ne souhaite pas avoir dans un string
string: DQUOTE ~('[' | '\\' | ',' | '\r' | ']')* DQUOTE;

// Déclaration variable global
/// Type
type: scalar
    | structures
    ;

scalar: BOOLEAN | INT | CHAR ;
structures: STRUCT  (ID)? LBRACKET  (listVarName)? (structDecl)? RBRACKET SEMICOLON;
arrays: LSQRTPAR  ( (initVariable)+ (COMMA initVariable)* )? RSQRTPAR ;
structDecl: structures ;

listVarName: (type ID (arrays)? ((COMMA ID (arrays)?)? SEMICOLON))*;

/// Variables
varDecl: type ID (arrays)? (EQUAL initVariable)? (COMMA ID (arrays)? (EQUAL initVariable)?)* SEMICOLON;
initVariable: TRUE
            | FALSE
            | ENTIER
            | string
            | CHARACTER
            | exprEnt
            | exprG
            | exprBool
            | initArrays
            | initStruct
            | LPAR initVariable RPAR
            ;
initArrays: LBRACKET (initVariable (COMMA initVariable)*)? RBRACKET;
initStruct: LBRACKET (ID COLON initVariable (COMMA ID COLON initVariable)*)? RBRACKET;

/// Constants
constDecl: CONST type ID (arrays)? ((EQUAL initVariable))? (COMMA ID (arrays)? (EQUAL initVariable ))? SEMICOLON;

/// Enum
enumDecl: ENUM (ID)? LBRACKET ID (COMMA ID)*? RBRACKET SEMICOLON;

/// Typedef
typedefDecl: TYPEDEF type ID SEMICOLON;


// Pour enlever la récursivité mutuelle gauche, il a fallu réécrire les expressions exprD, exprEnt et exprBool
// Maintenant exprEnt et exprBool n'appellent plus exprD, donc la récursivité n'existe que dans exprD

// Expression droite
exprD: exprG                #exprGExprD
     | exprEnt              #exprEntExprD
     | string               #stringExprD
     | exprChar             #exprCharExprD
     | exprBool             #exprBoolExprD
     | LPAR exprD RPAR      #parExprD
     ;

// Expression character
exprChar: CHARACTER         #characterExprChar
        | exprG             #exprGExprChar
        ;

// Expression entière
exprEnt: ENTIER                     #entierExprEnt
       | exprG                      #exprGExprEnt
       | LPAR exprEnt RPAR          #parExprEntExprEnt
       | MINUS exprEnt              #minusExprEntExprEnt
       | exprEnt PLUS  exprEnt      #exprEntPlusExprEnt
       | exprEnt MINUS exprEnt      #exprEntMinusExprEnt
       | exprEnt STAR  exprEnt      #exprEntStarExprEnt
       | exprEnt SLASH exprEnt      #exprEntSlashExprEnt
       | exprEnt PRCT  exprEnt      #exprEntPrctExprEnt
       | exprEnt POINT exprEnt      #exprEntPointtExprEnt // J'ai du mettre du scotch pour si nous avons un struct dans les actions
       ;

// Expression boolean
exprBool: TRUE                              #trueExprBool
        | FALSE                             #falseExprBool
        | exprG                             #exprGExprBool
        | LPAR exprBool RPAR                #parExprBool
        | EXCL LPAR? exprBool RPAR?         #exclExprBool
        | exprBool AND AND exprBool         #andExprBool
        | exprBool PIPE PIPE exprBool       #orExprBool
        | exprBool EQUAL EQUAL exprBool     #equalExprBool
        | exprBool EXCL EQUAL exprBool      #exclEqualExprBool
        | exprEnt SMALL exprEnt             #exprEntSmallExprBool
        | exprEnt BIG exprEnt               #exprEntBigExprBool
        | exprEnt SMALL EQUAL exprEnt       #exprEntSmallEqualExprBool
        | exprEnt BIG EQUAL exprEnt         #exprEntBigEqualExprBool
        | exprEnt EQUAL EQUAL exprEnt       #exprEntEqualExprBool
        | exprEnt EXCL EQUAL exprEnt        #exprEntExclEqualExprBool
        | exprChar EQUAL EQUAL exprChar     #exprCharEqualExprBool
        | exprChar EXCL EQUAL exprChar      #exprCharExclExprBool
        ;

// Expression gauche
exprG: ID                                            #idExprG
     | ID LSQRTPAR exprD (COMMA exprD)? RSQRTPAR     #idSqrtparExprG
     | POINT ID exprG                                #pointIdExprG
     | ID LPAR (exprD (COMMA exprD)*)? RPAR          #idParExprG
     ;

// Instruction
instruction: AFFECT LPAR ID COMMA expression RPAR                                                                      #affectInstr
           | varDecl                                                                                                   #declVar
           | IF LPAR exprBool RPAR LBRACKET instruction+ RBRACKET                                                      #ifInstr
           | IF LPAR exprBool RPAR LBRACKET instruction+ RBRACKET ELSE LBRACKET instruction+ RBRACKET                  #ifElseInstr
           | WHILE LPAR exprBool RPAR LBRACKET instruction+ RBRACKET                                                   #whileInstr
           | REPEAT LPAR exprEnt RPAR LBRACKET instruction+ RBRACKET                                                   #repeatInstr // Une expression entière ici par bool
           | FOR LPAR ID COLON EQUAL exprBool SEMICOLON exprD SEMICOLON exprD RPAR LBRACKET instruction+ RBRACKET      #forInstr
           | exprG EQUAL exprD SEMICOLON                                                                               #equalInstr
           | exprG POINT exprG EQUAL exprD SEMICOLON                                                                   #structEqualInstr //Scotch
           | actionType SEMICOLON                                                                                      #actionInstr
           | exprG LPAR RPAR SEMICOLON                                                                                 #fctInstr
           | ID LPAR (exprD (COMMA exprD)*)? RPAR SEMICOLON                                                            #fctInstrVoid
           ;


// Action
// Les actions attendent un entier.
actionType: LEFT LPAR (exprEnt)? RPAR     #leftAction
          | RIGHT LPAR (exprEnt)? RPAR    #rightAction
          | UP LPAR (exprEnt)? RPAR       #upAction
          | DOWN LPAR (exprEnt)? RPAR     #downAction
          | JUMP LPAR (exprEnt)? RPAR     #jumpAction
          | FIGHT LPAR RPAR               #fightAction
          | DIG LPAR RPAR                 #digAction
          ;

// Expression
expression: NUMBER                                                                      #constantExpr
          | ID                                                                          #variableExpr
          | left=expression op=(PLUS|MINUS) right=expression                            #plusMinusExpr
          | (ROBOT|TRESOR|PELOUSE|PALMIERS|PONS|BUISSON|TONNEAU|PUITS|VIDE|SQUELETTE)   #carteExpr
          | arrays                                                                      #arrayExpr
          | ENTIER                                                                      #entierExpr
          ;

// Fonction / procédure
fctDecl: (type | VOID) ID LPAR (argList)?  RPAR  LBRACKET (instBlock)+ RETURN (exprD | VOID) SEMICOLON RBRACKET;  // (LPAR (argList)?  RPAR)*
argList: type ID (arrays)?  (COMMA type ID (arrays)?)*?;
instBlock: ((varDecl)* | (enumDecl)* | (typedefDecl)* | (constDecl)* | (structDecl)*)? (instruction)+ ;



