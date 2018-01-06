grammar Ator;
/*
 * Parser Rules
 */
program    : stmt+ EOF;
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
/*
вот обещанное задание:

Написать интерпретатор и интерактивный редактор для следующего языка:

Грамматика (псевдо-BNF):

expr ::= expr op expr | (expr) | identifier | { expr, expr } | number | map(expr, identifier -> expr) | reduce(expr, expr, identifier identifier -> expr)
op ::= + | - | * | / | ^
stmt ::= var identifier = expr | out expr | print "string"
program ::= stmt | program stmt

Пояснения:
number - произвольное целое или вещественное число
приоритеты операторов такие (в возрастающем порядке): + и -, * и /, ^
{expr1, expr2}, где expr1 и expr2 - выражения с целым результатом - последовательность чисел  { expr1, expr1 + 1, expr + 2 .. expr2 } включительно. Если результат вычисления expr1 или expr2 не целый или expr1 > expr2, результат не определен.
map - оператор над элементами последовательности, применяет отображение к элементам последовательности и получает другую последовательность. Последовательность может из целой стать вещественной. Лямбда у map имеет один параметр - элемент последовательности.
reduce - свертка последовательности. Первый аргумент - последовательность, второй - нейтральный элемент, третий - операция. Свертка применяет операцию (лямбду) ко всем элементам последовательности. Например, “reduce({5, 7}, 1, x y -> x * y)” должен вычислять 1 * 5 * 6 * 7. Можно полагаться на то, что операция в reduce будет ассоциативна.
области видимости переменных - от ее объявления (var) до конца файла. Переменные у лямбд в map / reduce - имеют областью видимости соответствующую лямбду. У лямбд отсутствует замыкание, к глобальным переменным обращаться нельзя
out, print - операторы вывода. "string" - произвольная строковая константа, не содержащая кавычек, без экранирования

Пример:
var n = 500
var sequence = map({0, n}, i -> (-1)^i / (2.0 * i + 1))
var pi = 4 * reduce(sequence, 0, x y -> x + y)
print "pi = "
out pi
*/