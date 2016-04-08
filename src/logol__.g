lexer grammar logol;
options {
  language=Java;

}
@header {
package org.irisa.genouest.logol.parser;
 }

T15 : '(' ;
T16 : ',' ;
T17 : ')' ;
T18 : '==>' ;
T19 : ';' ;
T20 : '|' ;
T21 : '.' ;
T22 : '==*>' ;
T23 : 'def:' ;
T24 : '{' ;
T25 : '}' ;
T26 : 'matrix' ;
T27 : 'morphism' ;
T28 : '[' ;
T29 : ']' ;
T30 : ':' ;
T31 : '!' ;
T32 : '!external' ;
T33 : '$' ;
T34 : '_' ;
T35 : '?' ;
T36 : '@' ;
T37 : '/@' ;
T38 : '#' ;
T39 : 'p$' ;
T40 : '/$' ;
T41 : 'p/$' ;
T42 : '.*' ;
T43 : 'repeat(' ;
T44 : ')+' ;
T45 : '_,' ;
T46 : '_;' ;
T47 : '+' ;
T48 : '-' ;
T49 : '"' ;
T50 : '.nboccur' ;
T51 : '.minDistance' ;
T52 : '.maxDistance' ;

// $ANTLR src "/Users/osallou/Development/eclipse/eclipse3_workspace/workspace/LogolMatch/src/logol.g" 79
ModelName returns [ String name]  
	:	'mod' INT ('%'('a'..'z'|'A'..'Z'|'0'..'9')+'%')? { $name="mod"+$INT.text; };

// $ANTLR src "/Users/osallou/Development/eclipse/eclipse3_workspace/workspace/LogolMatch/src/logol.g" 222
Sequence:	 'SEQ' INT ;
//sequence:	SequenceName '=' SequenceFileName;
// $ANTLR src "/Users/osallou/Development/eclipse/eclipse3_workspace/workspace/LogolMatch/src/logol.g" 224
SequenceDef 
	:	'SEQ' INT ',' ANY '.fsa'  ;

// $ANTLR src "/Users/osallou/Development/eclipse/eclipse3_workspace/workspace/LogolMatch/src/logol.g" 410
VARIABLE: ('A'..'Z')+('0'..'9')+ ('%'('a'..'z'|'A'..'Z'|'0'..'9')+'%')?;

// $ANTLR src "/Users/osallou/Development/eclipse/eclipse3_workspace/workspace/LogolMatch/src/logol.g" 414
VARID	:	('A'..'Z')+;
// $ANTLR src "/Users/osallou/Development/eclipse/eclipse3_workspace/workspace/LogolMatch/src/logol.g" 415
LOWID	: ('a'..'z'|'\-')+;
// $ANTLR src "/Users/osallou/Development/eclipse/eclipse3_workspace/workspace/LogolMatch/src/logol.g" 416
ID  :   ('a'..'z'|'A'..'Z')+ ;
// $ANTLR src "/Users/osallou/Development/eclipse/eclipse3_workspace/workspace/LogolMatch/src/logol.g" 417
INT :   ('0'..'9')+ ;
// $ANTLR src "/Users/osallou/Development/eclipse/eclipse3_workspace/workspace/LogolMatch/src/logol.g" 418
ANY	: ('a'..'z'|'A'..'Z'|'0'..'9')+ ;	
// $ANTLR src "/Users/osallou/Development/eclipse/eclipse3_workspace/workspace/LogolMatch/src/logol.g" 419
NEWLINE: '\r'? '\n' ;
// $ANTLR src "/Users/osallou/Development/eclipse/eclipse3_workspace/workspace/LogolMatch/src/logol.g" 420
WHITESPACE : ( '\t' | ' ' | '\r' | '\n'| '\u000C' )+ { $channel = HIDDEN; };
	


