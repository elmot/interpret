grammar Ator;
/*
 * Parser Rules
 */
program    : stmt* EOF;
stmt       : print | out | var;
print      : 'print' TEXT ;
out        : 'out' expr ;
expr       : (MINUS expr) | (operand (op operand)*);
operand    : name | number | braces | seq | map | reduce;
seq        : '{' expr ',' expr '}' ;
braces     : '(' expr ')' ;
var        : 'var' NAME '=' expr ;
map        : 'map' '(' expr ',' NAME '->' expr ')';
reduce     : 'reduce' '(' expr ',' expr ',' NAME NAME '->' expr ')';
name       : NAME ;
number     : NUMBER ;
op         : OP;

/*
 * Lexer Rules
 */
TEXT        : '"'(~["])+ '"';
NAME        : [_a-zA-Z] [_a-zA-Z0-9]* ;
WS          : (' ' | '\t' | '\r' | '\n') -> skip;
NUMBER      : ([0-9]+)('.' [0-9]+)? ([eE] ('-' | '+')? [0-9]+)? ;
MINUS       : '-' ;
OP          : (MINUS | '+' | '*' | '/' | '^') ;