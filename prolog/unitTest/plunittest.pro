%% plunittest Unit testing framwork
%
% Used for unit testing of prolog predicates and XML JUNIT like report genration, it generates an XML JUNIT like report for unit testing some predicates.
% This module provides functions validate or unvalidate some predicate results and to generate a report.
%
% @author Olivier Sallou
% @license CeCILL
% @version 1.0.0 Set doc for swi-prolog

:- module(plunittest,[assertEqual/2,begin_suite/1,end_suite/1,begin_test/1,end_test/0,assertTrue/1,assertFalse/1,assertTrue/0,assertFalse/0]).

:- use_module(library(dynamic)).
:- use_module(library(lists)).

:- meta_predicate(assertTrue(:)).
:- meta_predicate(assertFalse(:)).


:-dynamic testResult/2.
:-dynamic dumpFile/1.
:-dynamic currentTest/1.
:-dynamic testdata/1.

%% setDumpFile(+FilePath:char-list)
%
% Sets the file name for report
%  @param FilePath  Path to the output file
%

setDumpFile(FilePath):-assert(dumpFile(FilePath)).

%% begin_suite(+FilePath:char-list)
%
% Starts a new suite of tests e.g. a report.
%  @param FilePath  Path to the output file
%
begin_suite(FilePath):-retractall(currentTest(_)),retractall(testResult(_,_)),setDumpFile(FilePath),initTmpDumpFile.

%% end_suite(+Name:char-list)
%
% Ends a suite of tests e.g. a report.
%  @param Name  Name or description of the test suite
%
end_suite(Name):-initDumpFile(Name),closeDumpFile,halt.

%% begin_test(+Name:char-list)
%
% Starts a new test, can enclose multiple predicate calls but will be seen as a single test in report.
%  @param Name  Name or description of the test
%
begin_test(Testname) :- assert(currentTest(Testname)),statistics(walltime,[_,_]).

%% end_test
%
% Ends a test.
%
end_test :- dumpResults,retractall(currentTest(_)).

%WARNING: predicate must be able to run with current predicates, not with caller predicates.
:-comment(assertTrue/1,"Test a predicate. Results in success if predicate returns True, else generate error. @p Description: @begin{itemize} @item @bf{assertTrue(PredInfo) , char-list => char-list }. @end{itemize} @p Input: @begin{itemize} @item @var{PredInfo} Predicate to test. Must be able to execute with current runtime, cannot use other libraries. @end{itemize}").
%assertTrue(PredInfo) :- Pred=..PredInfo,(Pred -> success;(error,fail)).
assertTrue(Goal) :- (call(Goal) -> success;(error,fail)).
:-comment(assertFalse/1,"Test a predicate. Results in error if predicate returns True, else generate success. @p Description: @begin{itemize} @item @bf{assertFalse(PredInfo) , char-list => char-list }. @end{itemize} @p Input: @begin{itemize} @item @var{PredInfo} Predicate to test. Must be able to execute with current runtime, cannot use other libraries. @end{itemize}").
%assertFalse(PredInfo) :- Pred=..PredInfo,(Pred -> (error,fail);success).
assertFalse(Goal):-(call(Goal) -> (error,fail);success).
:-comment(assertTrue/1,"Sets success for the test. @p Description: @begin{itemize} @item @bf{assertTrue}. @end{itemize} ").
assertTrue :-  success.
:-comment(assertFalse/1,"Sets error for the test. @p Description: @begin{itemize} @item @bf{assertFalse}. @end{itemize} ").
assertFalse :- error.
:-comment(assertEqual/1,"Test equality between two entities. @p Description: @begin{itemize} @item @bf{assertEqual(X,Y) , char-list => char-list }. @end{itemize} @p Input: @begin{itemize} @item @var{X} First value to compare. @item @var{Y} Second value to compare. @end{itemize}").
assertEqual(X,Y) :- ( X=Y -> success;(error,fail)).


success :- isTestPresent(Pred),assert(testResult(Pred,1)).
error :- isTestPresent(Pred),assert(testResult(Pred,0)).

isTestPresent(Pred):- currentTest(Pred),(testResult(Pred,Status) -> (Status=1 -> retract(testResult(Pred,Status));fail) ; 1=1).

%read tmpfile,store then write final with header
initDumpFile(Name):-total(OKLength,KOLength),
				 dumpFile(File),open(File,read,InStream),
                 repeat,
                 read_line(InStream,T), ((T == end_of_file);(assert(testdata(T)))), 
                 (T == end_of_file),     % Loop back if not at end of file       
                  !,
                close(InStream),!,             
				dumpFile(File),open(File,write,OutStream),
				format(OutStream,'<?xml version="1.0" encoding="UTF-8"?>~N<testsuite name="~p" tests="~p" errors="~p">~N',[Name,OKLength,KOLength]),
				findall(X, testdata(X), R),formatTests(R,OutStream),
				%testdata(T1)->(format(OutStream,'~p~N',[T1]));(close(OutStream)),1=1.
				close(OutStream).


formatTests([],_).
formatTests([X|Y],OutStream):- formatTest(X,OutStream),formatTests(Y,OutStream).

formatTest(X,OutStream):-atom_codes(Data,X),format(OutStream,'~p~N',[Data]).

initTmpDumpFile:-dumpFile(File),open(File,write,OutStream),format(OutStream,'~N',[]),close(OutStream).
addPropertiesDumpFile(1):-dumpFile(File),open(File,append,OutStream),format(OutStream,'<properties/>~N',[]),close(OutStream).
closeDumpFile:-dumpFile(File),open(File,append,OutStream),
			format(OutStream,'<system-out><![CDATA[]]></system-out><system-out><![CDATA[]]></system-out>~N',[]),
			format(OutStream,'</testsuite>~N',[]),close(OutStream).

addTestDumpFile(TestCase,TestResult,TestTime):-dumpFile(File),open(File,append,OutStream),
			format(OutStream,'<testcase classname="~p" name="~p"  time="~p">~N',[TestCase,TestCase,TestTime]),
			(TestResult=0 -> format(OutStream,'<error message="Error at predicate ~p" type="Assertion error"/>~N',[TestCase]);1=1),
			format(OutStream,'</testcase>~N',[]),
			close(OutStream).
			
dumpResults:- statistics(walltime,[_,Time]),currentTest(Pred),testResult(Pred,Status),addTestDumpFile(Pred,Status,Time).


total(OKLength,KOLength):-findall(X, testResult(X,_), OK), length(OK,OKLength),findall(X, testResult(X,0), KO), length(KO,KOLength).   

