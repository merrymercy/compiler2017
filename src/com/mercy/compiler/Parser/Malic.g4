grammar Malic;

compilationUnit
    : (variableDefinition | functionDefinition | classDefinition)* EOF
    ;

classDefinition
    : 'class' name=Identifier '{' (functionDefinition | variableDefinition)* '}'
    ;

functionDefinition
    : ret=typeType name=Identifier '(' (parameter (',' parameter)*)? ')'
               block
    ;

variableDefinition
    : typeType Identifier ('=' expression)? ';'
    ;

parameter
    : typeType Identifier
    ;

primitiveType
    : type = ('bool' | 'int' | 'string' | 'void')
    ;

typeType
    : (Identifier | primitiveType) ('[' ']')*
    ;

block
    : '{' statement* '}'
    ;

statement
    : block                                                              # blockStmt
    | variableDefinition                                                 # varDefStmt
    | 'if' '(' expression ')' statement ('else' statement)?              # ifStmt
    | 'for' '(' init=expression? ';' cond=expression? ';'
                                     incr=expression? ')' statement      # forStmt
    | 'while' '(' expression ')' statement                               # whileStmt
    | 'return' expression? ';'                                           # returnStmt
    | 'break' ';'                                                        # breakStmt
    | 'continue' ';'                                                     # continueStmt
    | expression ';'                                                     # exprStmt
    | ';'                                                                # blankStmt
    ;

expressionList
    : expression (',' expression)*
    ;

expression
    : primary                                            # primaryExpr
    | expression '.' Identifier                          # memberExpr
    | expression '[' expression ']'                      # arefExpr
    | expression '(' expressionList? ')'                 # funcallExpr
    | 'new' creator                                      # newExpr
    | expression op=('++' | '--')                        # suffixExpr
    | op=('+' | '-' | '++' | '--') expression            # prefixExpr
    | op=('~' | '!' ) expression                         # prefixExpr
    | expression op=('*' | '/' | '%') expression         # binaryExpr
    | expression op=('+' | '-') expression               # binaryExpr
    | expression op=('<<' | '>>') expression             # binaryExpr
    | expression op=('<' | '>' | '>=' | '<=') expression # binaryExpr
    | expression op=('==' | '!=' ) expression            # binaryExpr
    | expression op='&' expression                       # binaryExpr
    | expression op='^' expression                       # binaryExpr
    | expression op='|' expression                       # binaryExpr
    | expression '&&' expression                         # logicalAndExpr
    | expression '||' expression                         # logicalOrExpr
    | <assoc=right> expression '=' expression            # assignExpr
    ;

primary
    : '(' expression ')'   # subExpr
    | 'this'               # thisExpr
    | Identifier           # variableExpr
    | literal              # literalExpr
    ;

literal
    : DecimalInteger          # DecIntegerConst
    | StringLiteral           # StringConst
    | value=('true'|'false')  # boolConst
    | 'null'                  # nullConst
    ;

creator
    : (Identifier | primitiveType) ('[' expression ']')+ ('[' ']')* # arrayCreator
    | Identifier                                                    # nonarrayCreator
    ;

StringLiteral
    : '"' StringCharacter* '"'
    ;

fragment
StringCharacter
    : ~["\\\n\r]
    | '\\' ["n\\]
    ;

Identifier
    : [a-zA-Z_] [a-zA-Z_0-9]*
    ;

DecimalInteger
    : [1-9] [0-9]*
    | '0'
    ;

WS
    : [ \t\r\n]+ -> skip
    ;

BLOCK_COMMENT
    : '/*' .*? '*/' -> skip
    ;

LINE_COMMENT
    : '//' ~[\r\n]* -> skip
    ;