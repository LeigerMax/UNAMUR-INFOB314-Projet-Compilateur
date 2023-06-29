lexer grammar PlayPlusWords;

// Words
AFFECT: 'affect';
IMPORT: 'import';
MAP: 'map';
MAIN: 'main';
VOID: 'void';
RETURN: 'return';
BOOLEAN: 'bool';
INT: 'int';
CHAR: 'char';
STRUCT: 'struct';
CONST: 'const';
ENUM: 'enum';
TYPEDEF: 'typedef';
TRUE: 'true';
FALSE: 'false';
IF: 'if';
ELSE: 'else';
WHILE: 'while';
REPEAT: 'repeat';
FOR: 'for';

// Action
LEFT: 'left';
RIGHT: 'right';
UP: 'up';
DOWN: 'down';
JUMP: 'jump';
FIGHT: 'fight';
DIG: 'dig';

// Symbols
COMMA: ',';
POINT: '.';
PLUS: '+';
MINUS: '-';
STAR: '*';
EQUAL: '=';
COLON: ':';
SEMICOLON: ';';
QUOTE: '\'';
DQUOTE: '"';
IMPORTQUOTE: '^';
EXCL: '!';
SMALL: '<';
BIG: '>';
AND: '&';
PRCT: '%';
SLASH: '/';
BSLASH: '\\';
HASH: '#';
PIPE: '|';

// Parenthesis
LPAR: '(';
RPAR: ')';
LBRACKET: '{';
RBRACKET: '}';
LSQRTPAR: '[';
RSQRTPAR: ']';


// Carte
ROBOT: '@';
TRESOR: 'X';
PELOUSE: 'G';
PALMIERS:'P';
PONS: 'A';
BUISSON: 'B';
TONNEAU: 'T';
PUITS: 'S';
VIDE: '_';
SQUELETTE: 'Q';


// Lexical rules
//STRING : '"' (~["])+ '"';
CHARACTER: '\''  (DIGIT | LETTER | COLON | POINT | AND | SLASH  | BSLASH | SEMICOLON)+ '\'' ;
ENTIER: (DIGIT)+;

// Identifiers

ID: LETTER (LETTER | DIGIT)* ;

NUMBER: (DIGIT)+;

fragment LETTER: 'A'..'Z' | 'a'..'z' ;
fragment DIGIT: '0'..'9' ;

// Comments -> ignored

COMMENT: (('/*' (.*?) '*/') | ('//' .*? ('\r' | '\n' | EOF))) -> skip;


// Whitespaces -> ignored

NEWLINE: '\r'? '\n'  -> skip ;
WS: [ \t]+ -> skip ;


