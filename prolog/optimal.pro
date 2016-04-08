:- use_module(library(lists)).
:- use_module(library(process)).
:- use_module(library(dynamic)).
:- use_module(library(system)).
:- use_module(library(random)).
:- use_module(logol).

%:-dynamic matchstore/2.

%:-dynamic optimalmatch/5.

% For testing purpose only

%optimalmatch('VAR1',10,5,1,'VAR1').
%optimalmatch('VAR2',20,5,1,'VAR1').
%optimalmatch('VAR3',30,5,1,'VAR1').
%optimalmatch('VAR1',100,5,2,'VAR1').
%optimalmatch('VAR2',200,5,2,'VAR1').
%optimalmatch('VAR3',300,5,2,'VAR1').
%optimalmatch('VAR1',1000,5,3,'VAR1').
%optimalmatch('VAR2',2000,5,3,'VAR1').
%optimalmatch('VAR3',3000,5,3,'VAR1').

matchstore(1,['object1'],[]).
matchstore(2,['object2'],[]).
matchstore(3,['object3'],[]).

testout(OUT):-parseResults([[ ['Var1','acgt',100,110,10,[],0,0,0,[1,0] ],['Var2','acgt',200,210,10,[],0,0,0,[0,0] ], ['Var3','acgt',300,310,10,[],0,0,0,[1,0] ]]],[],OUT).
go:-test,test2.										
test:-isMax(['sample1'], [ ['VAR1' , 100 , 10 ] , ['VAR2' , 200 , 100 ] , ['VAR3' , 300 , 2 ] ],10).
test2:-isMax(['sample2'],[ ['VAR1' , 100 , 9 ] , ['VAR2' , 200 , 100 ] , ['VAR3' , 300 , 2 ] ],11).