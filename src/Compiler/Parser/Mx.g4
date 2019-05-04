grammar Mx;

//Compiler.Parser

program
    :   declaration* EOF
    ;

declaration
    :   variableDeclaration
    |   functionDeclaration
    |   classDeclaration
    ;

variableDeclaration
    :   type variableDeclarators ';'
    ;

variableDeclarators
    :   variableDeclarator (',' variableDeclarator)*
    ;

variableDeclarator
    :   IDENTIFIER ('=' expression)?
    ;

functionDeclaration
    :   returnType IDENTIFIER '(' parameterList? ')' functionBody
    ;

classDeclaration
    :   'class' IDENTIFIER '{' (variableDeclaration | functionDeclaration | constructorDeclaration)* '}';

parameterList
    :   type IDENTIFIER (',' type IDENTIFIER)*
    ;

type
    :   baseType
    |   arrayType
    ;

baseType
    :   token=BOOL
    |   token=INT
    |   token=IDENTIFIER
    |   token=STRING
    ;

arrayType
    :   baseType ('[' ']')+
    ;

returnType
    :   VOID
    |   type
    ;

constructorDeclaration
    :   IDENTIFIER '(' ')' functionBody
    ;

functionBody
    :   '{' statement* '}'
    ;

statement
    :   '{' statement* '}'                                                                          #blockStatement
    |   expression ';'                                                                              #expressionStatement
    |   IF '(' expression ')' statement (ELSE statement)?                                           #ifStatement
    |   FOR '(' init=expression? ';' cond=expression? ';' step=expression? ')' statement            #forStatement
    |   WHILE '(' expression ')' statement                                                          #whileStatement
    |   CONTINUE ';'                                                                                #continueStatement
    |   BREAK ';'                                                                                   #breakStatement
    |   RETURN expression? ';'                                                                      #returnStatement
    |   variableDeclaration                                                                         #variableDeclarationStatement
    |   ';'                                                                                         #emptyStatement
    ;

expression
    :   expression suffix=('++' | '--')                                         #unaryExpression
    |   functionCall                                                            #functionCallExpression
    |   expression '[' expression ']'                                           #arrayExpression
    |   '(' expression ')'                                                      #primaryExpression
    |   expression '.' (IDENTIFIER | functionCall)                              #memberExpression

    |   <assoc=right> prefix=('++' | '--') expression                           #unaryExpression
    |   <assoc=right> prefix=('+' | '-') expression                             #unaryExpression
    |   <assoc=right> prefix=('!' | '~' ) expression                            #unaryExpression
    |   <assoc=right> NEW creator                                               #newExpression

    |   expression operator=('*' | '/' | '%') expression                        #binaryExpression
    |   expression operator=('+' | '-') expression                              #binaryExpression
    |   expression operator=('<<' | '>>') expression                            #binaryExpression
    |   expression operator=('<' | '>' | '<=' | '>=') expression                #binaryExpression
    |   expression operator=('==' | '!=') expression                            #binaryExpression
    |   expression operator='&' expression                                      #binaryExpression
    |   expression operator='^' expression                                      #binaryExpression
    |   expression operator='|' expression                                      #binaryExpression
    |   expression operator='&&' expression                                     #binaryExpression
    |   expression operator='||' expression                                     #binaryExpression

    |   <assoc=right> expression '=' expression                                 #assignExpression

    |   token=IDENTIFIER                                                        #primaryExpression
    |   token=THIS                                                              #primaryExpression
    |   token=BOOL_LITERAL                                                      #primaryExpression
    |   token=NULL_LITERAL                                                      #primaryExpression
    |   token=INTEGER_LITERAL                                                   #primaryExpression
    |   token=STRING_LITERAL                                                    #primaryExpression
    ;

functionCall
    :    IDENTIFIER '(' (expression (',' expression)*)? ')'
    ;

//creator
//    :   baseType ('[' expression ']')* ('[' empty ']')*
//    |   baseType ('('')')?
//    ;

creator
    :   baseType ('[' expression ']')+ ('[' empty ']')+ ('[' expression ']')+   # creatorError
    |   baseType ('[' expression ']')+ ('[' empty ']')*                         # creatorArray
    |   baseType ('('')')?                                                      # creatorNonArray
    ;

empty
    :;

//reserved

BOOL_LITERAL    :   'true' | 'false';
NULL_LITERAL    :   'null';
STRING          :   'string';
INT             :   'int';
BOOL            :   'bool';
VOID            :   'void';
CLASS           :   'class';
IF              :   'if';
WHILE           :   'while';
FOR             :   'for';
BREAK           :   'break';
CONTINUE        :   'continue';
RETURN          :   'return';
THIS            :   'this';
NEW             :   'new';
ELSE            :   'else';

//Lexer

IDENTIFIER      :   [a-zA-Z][a-zA-Z0-9_]*;

INTEGER_LITERAL :   [0-9]+;

STRING_LITERAL  :   '"' (CHAR | .)*? '"';

fragment CHAR   :   '\\' [btnr"\\] ;

LINECOMMENT     :   '//' ~[\n\r]* -> skip;

BLOCKCOMMENT    :   '/*' .*? '*/' -> skip;

WHITESPACE      :   [ \t\r\n]+ -> skip;



