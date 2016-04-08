:- module(test,[test1/0,test2/0,test3/0,test4/0,hello/1]).
%execute with sicstus -l myfile -a path_to_output_xml path_to_punitfile path_to_logol.pro  path_to_fastafile
:- prolog_flag(argv, [_,_,_,Lib,_,_]),compile(Lib).
:- prolog_flag(argv, [_,_,_,_,Logol,_]),compile(Logol).
% compatibility for swi-prolog
:- prolog_flag(argv, [_,_,_,_,_,_,_,Lib,_,_]),compile(Lib).
:- prolog_flag(argv, [_,_,_,_,_,_,_,_,Logol,_]),compile(Logol).

% samples
test1:- assertTrue(1=1).
test2:- assertTrue(1=2).
test3:- assertEqual('1','1').
test4:- assertTrue(hello(1)).
hello(X):-X=1.
% end of samples

initFile:-(prolog_flag(argv, [_,_,Out,Lib,Logol,Fasta]);prolog_flag(argv, [_,_,_,_,_,_,Out,Lib,Logol,Fasta])),openSequenceStream(Fasta,74),bb_put('matchCounter',1),assert(offset(0)),assert(maxsize("100")),assert(outputfile('out.res')).
closeFile:-closeSequenceStream,retractall(offset(_)),retractall(maxsize(_)),retractall(outputfile(_)).

testWordContent:-getWordContent([a,a,c,c,c],[c,c,c],0,WordContent),assertTrue(WordContent=[a,a]).
testReverseMorphism:-applymorphism(['t','a','c'],'wcdna',1,Z),assertTrue(Z=['g','t','a']).
testMorphism:-applymorphism(['t','a','c'],'wcdna',0,Z),assertTrue(Z=['a','t','g']).
testGetChars:-getCharsFromPosition(0,3,Z),assertTrue(Z=['c','c','c']).
testIsExact:-isexact_pos( 0, [c,c,c] , Errors, OutPos),assertTrue(Errors=0).
testIsExactGapError:-isexactwithgapanderror_pos( 0, [c,g,c] , 1, Errors, OutPos),assertTrue(Errors=1).
testIsExactGapError2:-isexactwithgapanderror_pos( 0, [c,g,c,c] , 1, Errors, OutPos),assertTrue(Errors=1).
testIsExactErrorOnly:-isexactwitherroronly_pos( 0, [c,g,c] , 1, Errors, OutPos),assertTrue(Errors=1).
testIsExactDistinctGapError:-isexactwithdistinctgapanderror_pos( 0, [a,c,c,c,g,a,a] , 1, 1, Errors, DistanceErrors, OutPos),assertTrue(Errors=1),assertTrue(DistanceErrors=1).
testIsExactGapOnly:-isexactwithgaponly( [a,c,c,c,g,t], [a,c,g,t] , 2,  Errors, OutPos),assertTrue(Errors=2).
testNotExact:- \+notexact( [a,c,g,t], [a,c,g,t] , Out) -> assertTrue ; assertFalse.
testNotExact2:-notexact( [a,c,c,g,t], [a,c,g,t] , Out),assertTrue(Out=[c,g,t]).
testNotExactPos:- \+notexact_pos( 0, [c,c,c] , 0, 0, Errors, OutPos)-> assertTrue ; assertFalse.
testNotExactPos2:-notexact_pos( 0, [c,c,c] , 3, 5, Errors, OutPos),assertTrue(Out=4).
testNotExactPos3:-notexact_pos( 0, [c,c,c] , 3, 5, Errors, OutPos),assertTrue(Out=5).
testP2DMorphism:-applymorphism(['f'],'p2d',1,Z),assertTrue(Z=['t','t','t']).
testListMorphism:-applymorphism(['a','t','t'],'foo',0,Z),assertTrue(Z=['a','a','c']).
testCountAlphabet:-countAlphabet([a,c,g,t,a,c,g,t,a],[a,c],COUNT,0),assertTrue(COUNT=5).
testOptimalFunction:-isMax(['sample1'],[ ['VAR1' , 100 , 10 ] , ['VAR2' , 200 , 100 ] , ['VAR3' , 300 , 2 ] ],10),optimalmatch('VAR1',POS,LEN,ID,'VAR1'),!,assertTrue(POS=100).
testOptimalFunction2:-isMax(['sample2'],[ ['VAR1' , 100 , 100 ] , ['VAR2' , 200 , 100 ] , ['VAR3' , 300 , 2 ] ],11),optimalmatch('VAR1',POS,LEN,ID,'VAR1'),!,assertTrue(ID=11).
testEmptyString:-getCharsFromPosition(10,0,Z),assertTrue(Z=[]).
testEmptyMatch:-(isexact_pos( 0, [c,c,c] , Errors, OutPos),isexact_pos( OutPos, [] , Errors2, OutPos2),isexact_pos( OutPos2, [c,a,a] , Errors3, OutPos3))-> assertTrue;assertFalse.

:-prolog_flag(argv, [_,_,Out,_,_,_]),begin_suite(Out).
:-prolog_flag(argv, [_,_,_,_,_,_,Out,_,_,_]),begin_suite(Out).
:-begin_test('testIsExactDistinctGapError/0').
	:-initFile.
	:-testIsExactDistinctGapError.
	:-closeFile.
:-end_test.	
:-begin_test('testIsExactErrorOnly/0').
	:-initFile.
	:-testIsExactErrorOnly.
	:-closeFile.
:-end_test.	
:-begin_test('testIsExactGapError/0').
	:-initFile.
	:-testIsExactGapError.
	:-testIsExactGapError2.
	:-closeFile.
:-end_test.		
:-begin_test('testIsExact/0').
	:-initFile.
	:-testIsExact.
	:-closeFile.
:-end_test.		
:-begin_test('testGetChars/0').
	:-initFile.
	:-testGetChars.
	:-closeFile.
:-end_test.		
:-begin_test('testWordContent/0').
	:-testWordContent.
:-end_test.	
:-begin_test('reversemorphism/0').
	:-testReverseMorphism.
:-end_test.	
:-begin_test('testIsExactGapOnly/0').
	:-testIsExactGapOnly.
:-end_test.	
:-begin_test('testMorphism/0').
	:-testMorphism.
:-end_test.	
:-begin_test('testNotExact/0').
	:-testNotExact.
	:-testNotExact2.
	:-initFile.
	:-testNotExactPos.
	:-testNotExactPos2.
	:-testNotExactPos3.
	:-closeFile.
:-end_test.	
:-begin_test('testFilterExact/0').
	:-retractall(matchlist(_,_)).
	:-matchExist('mapListExact',[[['sample',[A,C,G,T],10,19,10,[],0,0,_,[0,0]]]])-> assertTrue;assertFalse.
	:-matchExist('mapListExact',[[['sample',[A,C,G,T],10,19,10,[],0,1,_,[0,0]]]])-> assertTrue;assertFalse.
	:- \+matchExist('mapListExact',[[['sample',[A,C,G,T],10,19,10,[],0,0,_,[0,0]]]])-> assertTrue;assertFalse.
:-end_test.
:-begin_test('testFilterGlobal/0').
	:-retractall(matchlist(_,_)).
	:-matchExist('mapListGlobal',[[['sample',[A,C,G,T],10,19,10,[],0,0,_,[0,0]]]])-> assertTrue;assertFalse.
	:-matchExist('mapListGlobal',[[['sample',[A,C,G,T],10,20,10,[],0,1,_,[0,0]]]])-> assertTrue;assertFalse.
	:- \+matchExist('mapListGlobal',[[['sample',[A,C,G,T],10,19,10,[],0,3,_,[0,0]]]])-> assertTrue;assertFalse.
:-end_test.	
:-begin_test('testFilterLocal/0').
	:-retractall(matchlist(_,_)).
	:-matchExist('mapListLocal',[[['sample',[A,C,G,T],10,19,10,[['sample',[A,C,G,T],10,13,10,[],0,1,_,[0,0]]],0,0,_,[0,0]]]])-> assertTrue;assertFalse.
	:-matchExist('mapListLocal',[[['sample',[A,C,G,T],10,19,10,[['sample',[A,C,G,T],10,14,10,[],0,1,_,[0,0]]],0,1,_,[0,0]]]])-> assertTrue;assertFalse.
	:- \+matchExist('mapListLocal',[[['sample',[A,C,G,T],10,19,10,[['sample',[A,C,G,T],10,13,10,[],0,1,_,[0,0]]],0,1,_,[0,0]]]])-> assertTrue;assertFalse.
:-end_test.
:-begin_test('testFilterLocal0/0').
	:-retractall(matchlist(_,_)).
	:-matchExist('mapListLocal0',[[['sample',[A,C,G,T],10,19,10,[['sample',[A,C,G,T],10,13,10,[],0,1,_,[0,0]]],0,0,_,[0,0]]]])-> assertTrue;assertFalse.
	:-matchExist('mapListLocal0',[[['sample',[A,C,G,T],10,20,10,[['sample',[A,C,G,T],10,13,10,[],0,1,_,[0,0]]],0,1,_,[0,0]]]])-> assertTrue;assertFalse.
	:- \+matchExist('mapListLocal0',[[['sample',[A,C,G,T],10,19,10,[['sample',[A,C,G,T],10,12,10,[],0,1,_,[0,0]]],0,1,_,[0,0]]]])-> assertTrue;assertFalse.
:-end_test.
:-begin_test('testP2DMorphism/0').
	:-testP2DMorphism.
:-end_test.	
:-begin_test('testListMorphism/0').
	:-testListMorphism.
:-end_test.	
:-begin_test('testCountAlphabet/0').
	:-testCountAlphabet.
:-end_test.	
:-begin_test('testOptimalFunction/0').
	:-testOptimalFunction.
	:-testOptimalFunction2.
:-end_test.
:-begin_test('testEmptyString/0').
    :-testEmptyString.
:-end_test.
:-begin_test('testEmptyMatch_1803/0').
	:-initFile.
    :-testEmptyMatch.
    :-closeFile.
:-end_test.
:-begin_test('testAmbiguityMatch/0').
	:-initFile.
	:-isexact_pos( 0, [c,c,c] , Errors, OutPos),assertTrue(Errors=0).
    :-isexact_pos( 0, [m,c,m] , Errors, OutPos),assertTrue(Errors=0).   
    :-closeFile.
:-end_test.
:-begin_test('testAmbiguityMatch2/0').
	:-initFile.
	:-isexact_pos( 0, [c,c,c] , Errors, OutPos),assertTrue(Errors=0).
    :- \+isexact_pos( 0, [d,c,d] , Errors, OutPos).  
    :-closeFile.
:-end_test. 

:-end_suite('prolog unit testing').

