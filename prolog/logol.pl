:- module(logol,[writeFinalMatches/0,isMin/5,replaceMatch/4,appendMatch/3,getCounter/1,config/1,optimalmatch/5,matchstore/2,isMax/3,parseResults/2,checkAlphabetPercentage/3,mapListExact/2,mapListGlobal/2,mapListLocal/2,mapListLocal0/2,matchExist/2,mapModels/3,externalinterfacewithspacer/2,externalinterface/2,notpred_pos/6,between/3,notexact_pos/6,notexact/3,matchExist/1,mapModels/2,mapList/2,mydb/1,matchlist/2,isexactwithgaponly/5,isexactwithgaponly_pos/5,setParent/2,getParent/2,logData/2,logolMain/5,cut_sequence/3,isequal/2,isexact_pos/4,isexactwithgapanderror_pos/5,isexactwitherroronly_pos/5,spacer_withresult_pos/5,wordSize_pos/4,getPosition_pos/3,wordContent_pos/4,checkPosition_pos/4,anySpacer_pos/7,repeatPredicate_pos/14,testOverlap_pos/5,getsuffixmatch_pos/12,getWordContent/4,openSequenceStream/2,closeSequenceStream/0,getCharsFromPosition/3,getNChar/3,resetParamList/3,getKnownVariables/2,getPosition/3,myCost/4,getDistance/3,initResultFile/1,closeResultFile/1,getParentVariable/2,is4me/5,outputfile/1,writeallresult/2,maxsize/1,getresult/2,offset/1,setSequence/2,computeCost/2,getsuffixmatch/12,writeSequence/2,sequenceData/3,suffixmatch/9,repeatPredicate/14,anySpacer/5,anySpacer/7,any/2,isoverlap/5,morphism/3,spacer_withresult/5,isexact/3,isexact/4,isexact/5,isexactwithgapanderror/4,isexactwithgapanderror/5,isexactwitherroronly/4,isexactwitherroronly/5,matrix/3, iscompequal/2,getcomplement/2,getreversecomplement/2,applymorphism/4, checkCost/4,cost/4,parentalCost/4,varDefinition/5,saveVariable/8,getVariable/9,removeVariable/1,wordSize/3,wordSize/4,percent/3,percent2int/3,checkCost/4,checkCost/5,checkPosition/3,checkPosition/5,checkRelativePosition/4,getContent/3,getContent/4,pushResult/2,countAlphabet/4,isexactwithdistinctgapanderror_pos/7,logolShell/1,cassiopee_pos/14,cassiopee/11],[]).
%:- module(logol,[writeFinalMatches/0,isMin/5,replaceMatch/4,appendMatch/3,getCounter/1,config/1,optimalmatch/5,matchstore/2,isMax/3,parseResults/2,checkAlphabetPercentage/3,mapListExact/2,mapListGlobal/2,mapListLocal/2,mapListLocal0/2,matchExist/2,mapModels/3,externalinterfacewithspacer/2,externalinterface/2,notpred_pos/6,between/3,notexact_pos/6,notexact/3,matchExist/1,mapModels/2,mapList/2,mydb/1,matchlist/2,isexactwithgaponly/5,isexactwithgaponly_pos/5,setParent/2,getParent/2,logData/2,logolMain/5,cut_sequence/3,isequal/2,isexact_pos/4,isexactwithgapanderror_pos/5,isexactwitherroronly_pos/5,spacer_withresult_pos/5,wordSize_pos/4,getPosition_pos/3,wordContent_pos/4,checkPosition_pos/4,anySpacer_pos/7,repeatPredicate_pos/14,testOverlap_pos/5,getsuffixmatch_pos/12,getWordContent/4,openSequenceStream/2,closeSequenceStream/0,getCharsFromPosition/3,getNChar/3,resetParamList/3,getKnownVariables/2,getPosition/3,myCost/4,getDistance/3,initResultFile/1,closeResultFile/1,getParentVariable/2,is4me/5,outputfile/1,writeallresult/2,maxsize/1,getresult/2,offset/1,setSequence/2,computeCost/2,getsuffixmatch/12,writeSequence/2,sequenceData/3,suffixmatch/9,repeatPredicate/14,anySpacer/5,anySpacer/7,any/2,isoverlap/5,morphism/3,spacer_withresult/5,isexact/3,isexact/4,isexact/5,isexactwithgapanderror/4,isexactwithgapanderror/5,isexactwitherroronly/4,isexactwitherroronly/5,matrix/3, iscompequal/2,getcomplement/2,getreversecomplement/2,applymorphism/4, checkCost/4,cost/4,parentalCost/4,varDefinition/5,saveVariable/8,getVariable/9,removeVariable/1,wordSize/3,wordSize/4,percent/3,percent2int/3,checkCost/4,checkCost/5,checkPosition/3,checkPosition/5,checkRelativePosition/4,getContent/3,getContent/4,pushResult/2,countAlphabet/4,isexactwithdistinctgapanderror_pos/7,logolShell/1],[assertions,basicmodes,functions,regtypes,hiord]).


/** <module> Logol Prolog API

This library gives all functions required to submit a Logol request

---++ History

* 2008/10/01 Creation
* 2010/03/01 Define externalinterface.
* 2010/04/15 Added additional filter predicated on mapList predicate family , #1602
* 2010/10/12 Added protein to dna morphism, #1682
* 2011/04/15 Percent calculation not floored, #1792
* 2011/05/02 Morphism output does not accept LOWID, #1795
* 2011/05/02 wrong results when match overlaps with end of sequence, #1799
* 2011/05/27 optimal issue when multiple match at same pos, #1802
* 2011/06/14 optimal issue subvars not found, #1805
* 2011/08/05 Add wooble morphism
* 2013/04/16 testOverlap_pos: check that output position is after start position, #2244
* 2013/04/19 testOverlap_pos: allow intermediate spacers with overlap, #2235
* 2013/08/08 fix exported predicate declarations,  #16249

---++ Information

 Author Olivier Sallou

 License CeCILL

 Version 1.2.0 Set doc for swi-prolog



---++ Predicates

*/



% Swi-prolog
file_search_path(foreign, ArchLib) :-
        current_prolog_flag(arch, Arch),
        (atom_concat('lib/', Arch, ArchLib);atom_concat('/usr/lib/swi-prolog/lib/', Arch, ArchLib)).

:- use_module(library(lists)).
:- use_module(library(process)).
%:- use_module(library(dynamic)).
%:- use_module(library(system)).
:- use_module(library(random)).

%% config(+List:list)
%
% Config data
%
% @param list Ordered array of config elements [ DEBUG , USEOPTIMAL]
%
:-dynamic config/1.

:-dynamic morphism/3.
:-dynamic matrix/3.

:-dynamic maxsize/1.
:-dynamic ident/2.
:-dynamic varDefinition/5.
:-dynamic varDefinition/8.
:-dynamic sequenceData/3.
:-dynamic suffixmatch/3.
:-dynamic suffixmatch/4.
:-dynamic outputfile/1.
:-dynamic fixed/1.
:-dynamic offset/1.

:-dynamic isequal/2.

:-dynamic mydb/1.
%  is var1 followed by rest of list
:-dynamic matchlist/2.
% Store all matches to write them to file later on
% Format (MATCHID,MATCH)
:-dynamic matchstore/2.

% [VARNAME, POSITION, SIZE, MATCHID, OPTIMALVARIABLE]
:-dynamic optimalmatch/5.

% program executor to execute external programs (suffixmatch): bash, ruby, ruby.exe, ...)
:-dynamic logolShell/1.


%% matchlist(+Mod:list,+OtherMethods:list)
%
% The first model will be the key to find an existing match. All matchlist elements will be unique
%
% @param Mod contains the list of variables defining the match for the first model of the grammar rule.
% @param OtherMethods list containing the remaining models if any.
%

%% matchstore(+MatchId:int,+List:OtherMethods:list)
%
% List of matches once filtered. Matches are recorded here to be queried if required
%
% @param MatchId contains the list of variables defining the match for the first model of the grammar rule.
% @param OtherMethods list containing the remaining models if any.
%

%% offset(+Value:int)
%
% Defined starting offset of the sequence.
% It is used in case of sequence splitting to update the start and end position when saving and getting variable via saveVariable and getVariable.
% @param Value offset to start reading the sequence

%% outputfile(+Outpath:char-list)
%
% Result file containing matches
%
% @param Outpath Defines the output file name

%% logolMain(+LogolFile:char-list,+InFile:char-list,+OutFile:char-list,+Offset:int,-Res)
%
% General entry point to load a logol file and call its query4match entry point
%
% @param LogolFile logol file to load
% @param InFile input sequence to  analyse
% @param OutFile output file name for results
% @param Offset offset to add on positions
% @param Res match found
%
logolMain(LogolFile,InFile,OutFile,Offset,Res):-
load_files(LogolFile,[]),
query4match(InFile,OutFile,Offset,Res).


%********************************************************

%% isexact_pos(+InputPos:int,+Pattern:list,-Errors:int,-OutPos:int)
%
% Test if a Pattern match on a position.
%
% Predicate using position on sequence. Read MaxChar character and apply treatment on sequence. Return new position on sequence.
%
% Fix1803 Specific case looking at empty string
%
% @param InputPos position on sequence
% @param Pattern string to match
% @param Errors number of errors found (always 0 as match is equal). For compatibility on predicates structure.
% @param OutPos position of the last character matching the pattern
%
isexact_pos(InputPos,[],Errors,OutPos):-OutPos=InputPos,Errors=0.
isexact_pos( InputPos, [E1 | E2] , Errors, OutPos) :- length([E1|E2],MaxChar),getCharsFromPosition(InputPos,MaxChar,InputList),!,isexact( InputList, [E1 | E2] , Errors, Z),length(Z,ZLength),OutPos is (InputPos + MaxChar - ZLength).

%% isexactwithgapanderror_pos(+InputPos:int,+Pattern:list,+Max:int,-Errors:int,-OutPos:int)
%
% Match if 2 sequences are equal with or without substitutions and gaps.
%
% Returns the number of errors and output position
%
% Fix1803 Specific case looking at empty string
%
% @param InputPos position on sequence
% @param Pattern string to match
% @param Max Max number of errors allowed
% @param Errors number of errors found (always 0 as match is equal). For compatibility on predicates structure.
% @param OutPos position of the last character matching the pattern
%
isexactwithgapanderror_pos(InputPos,[], Max, Errors, OutPos):- (OutPos=InputPos,Errors=0);(Max>0,NewMax is Max - 1,spacer_withresult_pos(InputPos,1, NewMax, _, OutPos)).
isexactwithgapanderror_pos(InputPos,[E1 | E2], Max, Errors, OutPos):- length([E1|E2],TmpMaxChar),MaxChar is TmpMaxChar + Max,getCharsFromPosition(InputPos,MaxChar,InputList),!,isexactwithgapanderror(InputList,[E1 | E2], Max, Errors, Z), length(Z,ZLength),length(InputList,InputListSize),OutPos is (InputPos + InputListSize - ZLength).

%% isexactwithgaponly_pos(+InputPos:int,+Pattern:list,+Max:int,-Errors:int,-OutPos:int)
%
% Match if 2 sequences are equal with or without gaps.
%
% Returns the number of errors and output position
%
% Fix1803 Specific case looking at empty string
%
% @param InputPos position on sequence
% @param Pattern string to match
% @param Max Max number of errors allowed
% @param Errors number of errors found (always 0 as match is equal). For compatibility on predicates structure.
% @param OutPos position of the last character matching the pattern
%
isexactwithgaponly_pos(InputPos,[], Max, Errors, OutPos):- (OutPos=InputPos,Errors=0);(Max>0,NewMax is Max - 1,spacer_withresult_pos(InputPos,1, NewMax, _, OutPos)).
isexactwithgaponly_pos(InputPos,[E1 | E2], Max, Errors, OutPos):- length([E1|E2],TmpMaxChar),MaxChar is TmpMaxChar + Max,getCharsFromPosition(InputPos,MaxChar,InputList),!,isexactwithgaponly(InputList,[E1 | E2], Max, Errors, Z), length(Z,ZLength),length(InputList,InputListSize),OutPos is (InputPos + InputListSize - ZLength).


%% isexactwitherroronly_pos(+InputPos:int,+Pattern:list,+Max:int,-Errors:int,-OutPos:int)
%
% Match if 2 sequences are equal with substitutions (gaps not allowed).
%
% Returns the number of errors and output position
%
% Fix1803 Specific case looking at empty string
%
% @param InputPos position on sequence
% @param Pattern string to match
% @param Max Max number of errors allowed
% @param Errors number of errors found (always 0 as match is equal). For compatibility on predicates structure.
% @param OutPos position of the last character matching the pattern
%
isexactwitherroronly_pos( InputPos,[], Max, Errors,OutPos):-OutPos=InputPos,Errors=0.
isexactwitherroronly_pos( InputPos,[E1 | E2], Max, Errors, OutPos):- length([E1|E2],MaxChar),getCharsFromPosition(InputPos,MaxChar,InputList),!,isexactwitherroronly(InputList,[E1 | E2], Max, Errors, Z), length(Z,ZLength),length(InputList,InputListSize),OutPos is (InputPos + InputListSize - ZLength).



%% spacer_withresult_pos(+InputPos:int,+Min:int,+Max:int,+Word:char-list,-OutPos:int)
%
% Spacer, match any alphabet within a range, return the result of the match
%
% @param InputPos position on sequence
% @param Min Min number of spacer allowed
% @param Max Max number of spacer allowed
% @param Word String to search.
% @param OutPos position of the last character matching the pattern
%
spacer_withresult_pos(InputPos,Min, Max, Word, OutPos):- MaxChar is Max+1,getCharsFromPosition(InputPos,MaxChar,InputList),!,spacer_withresult(InputList,Min, Max, Word, Z), length(Word,WLength),OutPos is (InputPos + WLength).

%% wordSize_pos(+InputPos:int,+OutPos:int,+Spacer:int,-Size:int)
%
% Gets the size of a match according to position and spacer
%
% @param InputPos position on sequence
% @param OutPos position after the word
% @param Spacer number of spacer in front of the match
% @param Size Legnth of the word
%
wordSize_pos(InputPos,OutPos,Spacer,Size):-Size is (OutPos - InputPos - Spacer).
wordSize_pos(InputPos,OutPos,Size):-Size is OutPos - InputPos.

%% getPosition_pos(+LocalPosition:int,-GlobalPosition:int)
%
% Get the real position in the sequence, offset is managed
%
% @param LocalPosition position on sequence
% @param GlobalPosition position on sequence, with offset
%
getPosition_pos(LocalPosition,GlobalPosition) :- offset(Offset),GlobalPosition is LocalPosition + Offset.


%% wordContent_pos(+InputPos:int,+Size:int,+Spacer:int,-Word:char-list)
%
% Gets the size of a match according to position and spacer
%
% @param InputPos position on sequence
% @param Size Legnth of the word
% @param Spacer number of spacer in front of the match
% @param Word match string content
%
wordContent_pos(InputPos,Size,Spacer,Word):- WordPos is InputPos+Spacer,getCharsFromPosition(WordPos,Size,Word).

%% checkPosition_pos(+Position:int,+Mincondition:int,+MaxCondition:int,+Spacer:int)
%
% Checks that position is between min and max. It adds offset from *offset* predicate to current position to compare with real positions on sequence.
%
% @param Position position on sequence
% @param MinCondition minimum position
% @param MaxCondition maximum position
% @param Spacer number of spacer in front of the match
%
checkPosition_pos(Position,MinCondition,MaxCondition,Spacer):- NewPos is Position+Spacer,checkPosition_pos(NewPos,MinCondition,MaxCondition).
checkPosition_pos(Position,MinCondition,MaxCondition):-offset(Offset),PositionWithOffset is Position + Offset,checkPosition(PositionWithOffset,MinCondition,MaxCondition).

%% anySpacer_pos(+InputPos:int,-OutPos:int,+Pred:goal,+Spacer:char-list,+Min:int,+Max:int,-NumberSpacer:int)
%
% Executes a predicate but accepts any spacers in front of match with min and max spacers.
%
% @param InputPos position on sequence
% @param OutPos position after match
% @param Pred Predicate to check the match
% @param Min Min number of spacer allowed
% @param Max Max number of spacer allowed
% @param Spacer number of spacer in front of the match
%
anySpacer_pos(InputPos,OutPos,Pred,Spacer, Min, Max, NumberSpacer):- NewPos is InputPos + Min,anySpacer_pos(NewPos,OutPos,Pred,Spacer,Min,Max,Min,NumberSpacer).

anySpacer_pos(InputPos,OutPos,Pred,Spacer, Min, Max,CountSpacer, NumberSpacer):- CountSpacer=<Max,(Pred=..VarDef,nth0(0,VarDef,Head),sublist(VarDef,Params,2,_,0),append([Head],[InputPos],NewPred0),append(NewPred0,Params,NewPred),FinalPred=..NewPred),((Spacer=0,FinalPred,NumberSpacer=0);(Spacer=1,((FinalPred,NumberSpacer=CountSpacer);(NewCountSpacer is CountSpacer+1,NewPos is InputPos+1,!,anySpacer_pos(NewPos,OutPos,FinalPred,Spacer,Min,Max,NewCountSpacer,NumberSpacer))))).

%% repeatPredicate_pos(+InputPos:int,+Pred:goal,+N:int,+AllowStartSpacer:int,+MinStartSpacer:int,+MaxStartSpacer:int,+AllowIntermediateSpacer:int,+MinSpacer:int,+MaxSpacer:int,+AllowOverlap:int,+PreviousMatch:list,-NumberSpacer:int,-Errors:int,-Info:list,-OutPos:int)
%
% Get repetition of a predicate N times, spacers allowed in front of first match.
%
% @param InputPos position on sequence
% @param Pred Predicate to check the match
% @param N number of allowed repetitions (max)
% @param AllowStartSpacer spacer are allowed in front of match if equal to 1
% @param MinStartSpacer If *AllowStartSpacer*, minimum number of expected spacers
% @param MaxStartSpacer If *AllowStartSpacer*, minimum number of expected spacers
% @param AllowIntermediateSpacer spacer are allowed between repetitions
% @param MinSpacer If *AllowIntermediateSpacer*, minimum number of expected spacers
% @param MaxSpacer If *AllowIntermediateSpacer*, minimum number of expected spacers
% @param AllowOverlap overlap between matches is allowed
% @param NumberSpacer number of spacer in front of the match
% @param Errors Number of errors found according to *Pred*
% @param Info Submatches of each repeat in a list
% @param OutPos position after match
%
repeatPredicate_pos(InputPos, Pred, N, AllowStartSpacer, MinStartSpacer, MaxStartSpacer, AllowIntermediateSpacer, MinSpacer, MaxSpacer, AllowOverlap, NumberSpacer, Errors, Info, Z) :- Pred=..VarDef,sublist(VarDef,Params,3,_,4),getKnownVariables(Params,InitParams),repeatPredicate_pos(InputPos, Pred, InitParams, 0, N, AllowStartSpacer, MinStartSpacer, MaxStartSpacer, AllowIntermediateSpacer, MinSpacer, MaxSpacer, AllowOverlap, [], NumberSpacer, [0,0], Errors, [], Info, Z).
% sub predicate, do not call directly
repeatPredicate_pos(InputPos, Pred, InitParams, Count, N, AllowStartSpacer, MinStartSpacer, MaxStartSpacer,AllowIntermediateSpacer, MinSpacer, MaxSpacer, AllowOverlap, PreviousMatch, NumberSpacer, ErrorCount, Errors, InfoList, Info, Z2) :-
   (N=0,Count>1,Z2=InputPos,Errors=ErrorCount,Info=InfoList,NumberSpacer=0);
   % check number of repeat
   (N>0,Count>0,Count=<N,Z2=InputPos,Errors=ErrorCount,Info=InfoList,NumberSpacer=0);
   (((N>0,Count>=0,Count=<N);(N=0)),
   (
   % Execute Pred, replace X and Y as first arg of pred
   % If AllowSpacer, use with spacer check else call directly
   % Put output list (last param must be output = Z)
   (Pred=..VarDef,nth0(0,VarDef,Head),sublist(VarDef,Params,3,_,4),nth0(2,VarDef,Postponed),append([Head],[InputPos,Postponed],NewPred0),resetParamList(Params,InitParams,NewParams),append(NewPred0,NewParams,NewPred1),length(VarDef,VarDefLength),ParentPos is VarDefLength - 4,nth0(ParentPos,VarDef,ParentVal),append(NewPred1,[ParentVal,Tmp1,Tmp2,Z],NewPred),FinalPred=..NewPred),
   % No overlap
   % Fix #2235
   (((
    % spacers not allowed at beginning not intermediate
    (( AllowStartSpacer=0,NumberSpacer=0,((AllowIntermediateSpacer=0;Count=0),FinalPred,extractRepeatMatch(InputPos,FinalPred,NewMatch)));
    % spacers allowed only between recursive matches
    (Count>0,
     % test intermediate spacers
     (AllowIntermediateSpacer=1,anySpacer_pos(InputPos,Z,FinalPred,1,MinSpacer,MaxSpacer,IntermediateSpacer),NewInputPos is InputPos + IntermediateSpacer,extractRepeatMatch(NewInputPos,FinalPred,NewMatch))
    ));
    % spacers are allowed before matching
    (AllowStartSpacer=1,anySpacer_pos(InputPos,TmpOutput,FinalPred,1, MinStartSpacer,MaxStartSpacer,NumberSpacer),NewInputPos is InputPos + NumberSpacer,extractRepeatMatch(NewInputPos,FinalPred,NewMatch))
   ));
   % Overlap allowed
   ((AllowOverlap=1,Count>0),NumberSpacer=0,
    testOverlap_pos(InputPos,FinalPred,PreviousMatch,NewMatch,Z)
   ))
   ),
   FinalPred=..Result,length(Result,ResultLength), InfoCount is ResultLength-2, TmpErrorCount is ResultLength-3,nth0(InfoCount,Result,InfoData),nth0(TmpErrorCount,Result,ErrorData),TmpOut is ResultLength-1,nth0(TmpOut,Result,Output),
   ErrorData  = [ NewSub, NewIndel], ErrorCount = [ OldSub, OldIndel], TmpSub is OldSub + NewSub, TmpIndel is OldIndel + NewIndel,
   %NewErrorCount is ErrorCount + ErrorData,
   NewErrorCount = [ TmpSub,TmpIndel],
   append(InfoList,InfoData,NewInfoList),
   % got a match
   CountInc is Count+1,
   % Stop backward match search if predicate is exact match because only 1 result is possible
   % TODO: check why duplicate results appear in other cases
   ((Head='isexact',!);(Continue=1)),
   %!,
   % call function again (recursive call), start spacer set to zero because not applicable anymore
   (repeatPredicate_pos(Output, FinalPred, InitParams,CountInc, N, 0, 0, 0,AllowIntermediateSpacer, MinSpacer, MaxSpacer, AllowOverlap, NewMatch, 0, NewErrorCount, Errors, NewInfoList, Info, Z2))
   ).

% Extract match predicate used by repeatPredicate_pos
extractRepeatMatch(Input,Pred,Match):- Pred=..Result,length(Result,ResultLength), TmpOut is ResultLength-1,nth0(TmpOut,Result,Output),Len is Output - Input,getCharsFromPosition(Input,Len,Match).


%% testOverlap_pos(+InputPos:int,+Pred:goal,+PreviousMatch:list,-Match:list,-Outpos:int)
%
% Test a predicate, overlap with previous match is allowed.
%
% @param InputPos position on sequence
% @param Pred Predicate to check the match
% @param PreviousMatch match against which to test the overlap
% @param Match Detail of the match
% @param OutPos position after match
%
testOverlap_pos(InputPos,Pred,PreviousMatch,Match,Z):- length(PreviousMatch,PrevMatchLength),
            ((PrevMatchLength=0,NewInputPos=InputPos);(PrevMatchLength>0,NewInputPos is (InputPos - PrevMatchLength + 1))),
            doOverlap_pos(NewInputPos,Pred,Z, PrevMatchLength,0, OverlapCount),OutCount is NewInputPos + OverlapCount,Size is Z - OutCount, OutCount+Size > InputPos,getCharsFromPosition(OutCount,Size,Match).


doOverlap_pos(InputPos,Pred,Z, PrevMatchLength, Count, OutCount) :- (Count=<PrevMatchLength),(Pred=..VarDef,nth0(0,VarDef,Head),sublist(VarDef,Params,2,_,1),append([Head],[InputPos],NewPred0),append(NewPred0,Params,NewPred1),append(NewPred1,[Z],NewPred),FinalPred=..NewPred),
       ((FinalPred,FinalPred=..VarDef2,length(VarDef2,VarDef2Length),nth1(VarDef2Length,VarDef2,Output),Z=Output, OutCount=Count);
       (NewCount is Count+1,FinalPred=..VarDef2,length(VarDef2,VarDef2Length),nth1(VarDef2Length,VarDef2,Output),Z=Output,
       NewPos is InputPos + 1,
       doOverlap_pos(NewPos,Pred,Z, PrevMatchLength, NewCount, OutCount))).


%% cassiopee_pos(+ProgramName:char-list,+MotifFileName:char-list,+Motif:char-list,+Cost:int,+Distance:int,+SequenceLength:int,+InputPos:int,+MuteOnly:int,-Errors:int,-Outputpos:int,+MinSpacer:int,+MaxSpacer:int,-SpacerSize:int,+Type:int)
%
% Find matches using an external seach tool starting after current position, errors are allowed ( hamming or edit)
% Write the pattern in a temporary file
%
% @param ProgramName program to call
% @param MotifFileName path to the file where *pattern* will be written
% @param Motif String to search
% @param Cost Max number of indel allowed
% @param Distance Max number of indel allowed
% @param SequenceLength size of the input sequence
% @param InputPos position on sequence
% @param MuteOnly Allow only substitutions if equal to 1, else put 0 to allow insert/deletion/substitution
% @param Errors Number fo errors found
% @param OutputPos position after match
% @param MinSpacer minimum of spacer from position to match
% @param MaxSpacer maximum of spacer from position to match
% @param SpacerSize number fo spacers found
% @param Type DNA(0)/RNA(1) or Protein(2)
%
cassiopee_pos(ProgramName,MotifFilename,Motif,Substitution,Distance,SequenceLength,InputPos,MuteOnly,Errors,OutputPos,MinSpacer,MaxSpacer,SpacerSize, Type):-
sequenceData(SeqFile,_,_),atom_chars(SeqFile,SeqFileAtom),atom_chars(MotifFilename,MotifFileAtom),append(SeqFileAtom,MotifFileAtom,TmpFilename),atom_chars(Filename,TmpFilename),
writeSequence(Filename,Motif),CurPos = InputPos , MinPos is CurPos + MinSpacer, ( (MaxSpacer=0,MaxPos=0);(MaxSpacer>0, MaxPos is CurPos + MaxSpacer)) ,cassiopee(ProgramName,Filename,Substitution,Distance,MinPos,MaxPos,Pos,Len,MuteOnly,Errors, Type), SpacerSize is Pos-CurPos,OutputPos is Pos+Len.

%cassiopee(ProgramName,MotifFileName,NbSub,NbError,MinStart,MaxStart,Pos,Len,MuteOnly, Errors, Type):-sequenceData(Sequence,_,_),number_codes(NbError,TmpErr),atom_codes(Err,TmpErr),number_codes(NbSub,TmpSub),atom_codes(Sub,TmpSub),number_codes(MinStart,TmpMin),atom_codes(Min,TmpMin),number_codes(MaxStart,TmpMax),atom_codes(Max,TmpMax),number_codes(MuteOnly,TmpMute),atom_codes(Mute,TmpMute),random(Unique),number_codes(Type,TmpType),atom_codes(TypeSeq,TmpType),logolShell(Executor),!,process_create(path(Executor),[ProgramName, Sub,Err,Min,Sequence,MotifFileName,Mute,Max,TypeSeq],[stdin(null),stdout(pipe(Out)),stderr(null),process(Proc)]),
%                 repeat,
%                 read_line(Out,T), ((T == end_of_file);(number_codes(X,T),read_line(Out,TLen),number_codes(XLen,TLen),read_line(Out,TErr),number_codes(XErr,TErr),assert(suffixmatch(Unique,X,XLen,XErr)) )), % Read a term
%                 (T == end_of_file),     % Loop back if not at end of file
%                  !,
%                  close(Out),process_wait(Proc,Exit),suffixmatch(Unique,Pos,Len,Errors),retractall(suffixmatch(Unique,_,_,_)).

% Extract results from a file
getCassiopeeResult(Out,Pos,Len,Errors):-(read_line(Out,T),((T==end_of_file,!,close(Out),fail);(\+ T==end_of_file,number_codes(Pos,T),read_line(Out,TLen),number_codes(Len,TLen),read_line(Out,TErr),number_codes(Errors,TErr)));getCassiopeeResult(Out,Pos,Len,Errors)).

cassiopee(ProgramName,MotifFileName,NbSub,NbError,MinStart,MaxStart,Pos,Len,MuteOnly, Errors, Type):-sequenceData(Sequence,_,_),number_codes(NbError,TmpErr),atom_codes(Err,TmpErr),number_codes(NbSub,TmpSub),atom_codes(Sub,TmpSub),number_codes(MinStart,TmpMin),atom_codes(Min,TmpMin),number_codes(MaxStart,TmpMax),atom_codes(Max,TmpMax),number_codes(MuteOnly,TmpMute),atom_codes(Mute,TmpMute),random(Unique),number_codes(Type,TmpType),atom_codes(TypeSeq,TmpType),logolShell(Executor),!,process_create(path(Executor),[ProgramName, Sub,Err,Min,Sequence,MotifFileName,Mute,Max,TypeSeq],[stdin(null),stdout(pipe(Out)),stderr(null),process(Proc)]),process_wait(Proc,Exit),!,atom_concat(MotifFileName,'.cass.out', MotifResultFile),open(MotifResultFile,read,MotifOut),!,getCassiopeeResult(MotifOut,Pos,Len,Errors).

%
%

% end of cassiopee


%% getsuffixmatch_pos(+ProgramName:char-list,+MotifFileName:char-list,+Motif:char-list,+Distance:int,+SequenceLength:int,+InputPos:int,+MuteOnly:int,-Errors:int,-Outputpos:int,+MinSpacer:int,+MaxSpacer:int,-SpacerSize:int)
%
% Find matches using an external seach tool starting after current position, errors are allowed ( hamming or edit)
% Write the pattern in a temporary file
%
% @param ProgramName program to call
% @param MotifFileName path to the file where *pattern* will be written
% @param Motif String to search
% @param Distance Max number of errors allowed
% @param SequenceLength size of the input sequence
% @param InputPos position on sequence
% @param MuteOnly Allow only substitutions if equal to 1, else put 0 to allow insert/deletion/substitution
% @param Errors Number fo errors found
% @param OutputPos position after match
% @param MinSpacer minimum of spacer from position to match
% @param MaxSpacer maximum of spacer from position to match
% @param SpacerSize number fo spacers found
%
getsuffixmatch_pos(ProgramName,MotifFilename,Motif,Distance,SequenceLength,InputPos,MuteOnly,Errors,OutputPos,MinSpacer,MaxSpacer,SpacerSize):-
sequenceData(SeqFile,_,_),atom_chars(SeqFile,SeqFileAtom),atom_chars(MotifFilename,MotifFileAtom),append(SeqFileAtom,MotifFileAtom,TmpFilename),atom_chars(Filename,TmpFilename),
writeSequence(Filename,Motif),CurPos = InputPos , MinPos is CurPos + MinSpacer, ( (MaxSpacer=0,MaxPos=0);(MaxSpacer>0, MaxPos is CurPos + MaxSpacer)) ,suffixmatch(ProgramName,Filename,Distance,MinPos,MaxPos,Pos,Len,MuteOnly,Errors),
SpacerSize is Pos-CurPos,OutputPos is Pos+Len.



%*********************************************************



%% openSequenceStream(+File:char-list,+HeaderSize:int)
%
% Defines a stream on sequence file (open a stream, and store stream reference).
%
% @param File Path to the file
% @param HeaderSize size of the header (to skip it)
%
openSequenceStream(File,HeaderSize):-open(File, read, Stream,[reposition(true),type(binary)]),assert(sequenceData(File,HeaderSize,Stream)).

%% closeSequenceStream
%
% Close the sequence stream.
%
closeSequenceStream:-sequenceData(_,_,Stream),close(Stream),retractall(sequenceData(_,_,Stream)).



%% getCharsFromPosition(+Position:int,+N:int,-Word:list)
%
% Read N characters from a UTF-8 text file, from position *Position* and return result as a list..
% Fix 1326
%
% @param Position position in sequence
% @param N number fo chars to read
% @param Word result
%

getCharsFromPosition(Position,N,Z):-Position>=0,N>0->(sequenceData(_,HeaderSize,Stream),GotoPosition is (Position + HeaderSize + 1),seek(Stream,GotoPosition,bof,NewPos),\+ at_end_of_stream(Stream),getNChar(Stream,N,Z));Z=[].

%% getNChar(+Stream:stream,+N:int,-Word:list)
%
% Read N characters from a UTF-8 text file, from current stream.
%
% @param Stream stream to read
% @param N number fo chars to read
% @param Word result
%
% 09/09/16 O. Sallou - Change way to extract word from file for better performance

getNChar(Stream,N,Z):-getNChar(Stream,[],0,N,Z1),reverse(Z1,Z).
%getNChar(Stream,TmpList,Count,N,Z):-(Count=N,Z=TmpList);(Count<N,get_byte(Stream,B),(((B=(-1);B=10),Z=TmpList);(\+ (B=(-1);B=10),atom_codes(C,[B]),((at_end_of_stream(Stream),append(TmpList,[C],NewTmpList),Z=NewTmpList);(\+ at_end_of_stream(Stream),((C='\n',getNChar(Stream,TmpList,Count,N,Z));(\+C='\n',NewCount is Count + 1,((TmpList=[],getNChar(Stream,[C],NewCount,N,Z));(\+TmpList=[],append(TmpList,[C],NewTmpList),getNChar(Stream,NewTmpList,NewCount,N,Z)))))))))).
getNChar(Stream,TmpList,Count,N,Z):-(Count=N,Z=TmpList);(Count<N,get_byte(Stream,B),(((B=(-1);B=10),Z=TmpList);(\+ (B=(-1);B=10),atom_codes(C,[B]),((at_end_of_stream(Stream),Z=[C|TmpList]);(\+ at_end_of_stream(Stream),((C='\n',getNChar(Stream,TmpList,Count,N,Z));(\+C='\n',NewCount is Count + 1,((TmpList=[],getNChar(Stream,[C],NewCount,N,Z));(\+TmpList=[],getNChar(Stream,[C|TmpList],NewCount,N,Z)))))))))).



%% getWordContent(+BeforeList:list,+AfterList:list,+Spacer:int,-WordContent:list)
%
% Get the content of a word from before and after lists
%
% @param BeforeList List from which to extract the word
% @param AfterList Remaining list after word extract
% @param Spacer number of spacer before expected word
% @param WordContent result (diff between beforelist and afterlist, minus spacers)
%
getWordContent(BeforeList,AfterList,Spacer,WordContent) :- length(AfterList,AfterLength),sublist(BeforeList,WordContent,Spacer,_,AfterLength).
% End of get content



%% is4me(+VAR:list,+VARID:int,+TYPE:int,-REF:ref,-REFOTHER:ref)
%
% Check that current variable treatment is for current variable.
%
% @param VAR is a list with id of variable where treatment is postponed,type of constraint and variable reference
% @param VARID id of the current variable
% @param TYPE type of the constraint
% @param REF reference to the variable
% @param REFOTHER reference required for variable
%
is4me([],VARID,TYPE,REF,REFOTHER):- fail.
is4me([X|Y],VARID,TYPE,REF,REFOTHER):- (checkVar(X,VARID,TYPE,REF,REFOTHER));is4me(Y,VARID,TYPE,REF,REFOTHER).
% sub predicate, do not call directly.
checkVar([],VARID,TYPE,REF,REFOTHER):- fail.
checkVar([X,Y,Z,Z1],VARID,TYPE,REF,REFOTHER):- (\+ is_list(X),X=VARID,Y=TYPE,REF=Z,REFOTHER=Z1).
checkVar([X|Y],VARID,TYPE,REF,REFOTHER):- is_list(X),is4me([X|Y],VARID,TYPE,REF,REFOTHER).

%% setSequence(+Key:charlist,+Data:list)
%
% Check that current variable treatment is for current variable.
%
% @param Key Database reference
% @param Data Data to add
%
setSequence(Key,Y):-bb_get(Key, ResultDef),append(ResultDef,Y,NewResult),bb_put(Key,NewResult).


%:-comment(sequenceData/3,"Store the path of the sequence and its header length. @p Description: @bf{sequenceData(File,HeaderLength,Stream) , char-list * int * Stream => char-list * int * Stream}. @p Input @begin{itemize} @item @var{File} is the path of the sequence @item @var{Length} is the size of the sequence @item @var{Stream} is the stream on the sequence @end{itemize} @p Output: @p None ").
%% sequenceData(+File:char-list,+HeaderLength:int,+Stream:stream)
%
% Store the path of the sequence and its header length.
%
% @param File Path to file
% @param HeaderLength siz eof the file header
% @param Stream stream relative to the file
%

% Executes a predicate but accepts any spacers in front of match
%% anySpacer(+Input:list,-Out:int,+Pred:goal,+Spacer:char-list,+Min:int,+Max:int,-NumberSpacer:int)
%
% Executes a predicate but accepts any spacers in front of match with min and max spacers.
%
% @param Input List to search in
% @param Out Remaining list after match
% @param Pred Predicate to check the match
% @param Min Min number of spacer allowed
% @param Max Max number of spacer allowed
% @param Spacer number of spacer in front of the match
%
anySpacer([X|Y],Z,Pred,Spacer, Min, Max, NumberSpacer):- cut_sequence([X|Y],Min,NewList),anySpacer(NewList,Z,Pred,Spacer,Min,Max,Min,NumberSpacer).

% sub predicates, do not call directly
anySpacer([],Z,Pred,Spacer,Min, Max, CountSpacer, NumberSpacer):- fail.
anySpacer([X|Y],Z,Pred,Spacer, Min, Max,CountSpacer, NumberSpacer):- ((Max=0);(Max>0,CountSpacer=<Max)),(Pred=..VarDef,nth0(0,VarDef,Head),sublist(VarDef,Params,2,_,0),append([Head],[[X|Y]],NewPred0),append(NewPred0,Params,NewPred),FinalPred=..NewPred),((Spacer=0,FinalPred,NumberSpacer=0);(Spacer=1,((FinalPred,NumberSpacer=CountSpacer);(NewCountSpacer is CountSpacer+1,anySpacer(Y,Z,FinalPred,Spacer,Min,Max,NewCountSpacer,NumberSpacer))))).



% Executes a predicate but accepts any spacers in front of match

%% anySpacer(+Input:list,-Out:int,+Pred:goal,+Spacer:char-list,-NumberSpacer:int)
%
% Executes a predicate but accepts any spacers in front of match.
%
% @param Input List to search in
% @param Out Remaining list after match
% @param Pred Predicate to check the match
% @param Spacer number of spacer in front of the match
%
anySpacer([X|Y],Z,Pred,Spacer, NumberSpacer):- anySpacer([X|Y],Z,Pred,Spacer,0, NumberSpacer).

% sub predicates, do not call directly
anySpacer([],Z,Pred,Spacer,CountSpacer, NumberSpacer):-fail.
anySpacer([X|Y],Z,Pred,Spacer,CountSpacer, NumberSpacer):- (Pred=..VarDef,nth0(0,VarDef,Head),sublist(VarDef,Params,2,_,0),append([Head],[[X|Y]],NewPred0),append(NewPred0,Params,NewPred),FinalPred=..NewPred),((Spacer=0,FinalPred,NumberSpacer=0);(Spacer=1,((FinalPred,NumberSpacer=CountSpacer);(NewCountSpacer is CountSpacer+1,anySpacer(Y,Z,FinalPred,Spacer,NewCountSpacer,NumberSpacer))))).


% Main predicate
% DEPRECATED
%% repeatPredicate(+Input:list,+Pred:goal,+N:int,+AllowStartSpacer:int,+MinStartSpacer:int,+MaxStartSpacer:int,+AllowIntermediateSpacer:int,+MinSpacer:int,+MaxSpacer:int,+AllowOverlap:int,+PreviousMatch:list,-NumberSpacer:int,-Errors:int,-Info:list,-Out:list)
%
% Get repetition of a predicate N times, spacers allowed in front of first match.
% @deprecated
%
repeatPredicate([ X | Y], Pred, N, AllowStartSpacer, MinStartSpacer, MaxStartSpacer, AllowIntermediateSpacer, MinSpacer, MaxSpacer, AllowOverlap, NumberSpacer, Errors, Info, Z) :- Pred=..VarDef,sublist(VarDef,Params,3,_,4),getKnownVariables(Params,InitParams),repeatPredicate([ X | Y], Pred, InitParams, 0, N, AllowStartSpacer, MinStartSpacer, MaxStartSpacer, AllowIntermediateSpacer, MinSpacer, MaxSpacer, AllowOverlap, [], NumberSpacer, 0, Errors, [], Info, Z).

% sub predicate, do not call directly

repeatPredicate([ X | Y], Pred, InitParams, Count, N, AllowStartSpacer, MinStartSpacer, MaxStartSpacer,AllowIntermediateSpacer, MinSpacer, MaxSpacer, AllowOverlap, PreviousMatch, NumberSpacer, ErrorCount, Errors, InfoList, Info, Z2) :-
   (N=0,Count>1,Z2=[X|Y],Errors=ErrorCount,Info=InfoList);
   % check number of repeat
   (N>0,Count=N,Z2=[X|Y],Errors=ErrorCount,Info=InfoList);
   (((N>0,Count>=0,Count=<N);(N=0)),
   (
   % Execute Pred, replace X and Y as first arg of pred
   % If AllowSpacer, use with spacer check else call directly
   % Put output list (last param must be output = Z)
   (Pred=..VarDef,nth0(0,VarDef,Head),sublist(VarDef,Params,3,_,4),nth0(2,VarDef,Postponed),append([Head],[[X|Y],Postponed],NewPred0),resetParamList(Params,InitParams,NewParams),append(NewPred0,NewParams,NewPred1),length(VarDef,VarDefLength),ParentPos is VarDefLength - 4,nth0(ParentPos,VarDef,ParentVal),append(NewPred1,[ParentVal,Tmp1,Tmp2,Z],NewPred),FinalPred=..NewPred),
   % No overlap
   (((AllowOverlap=0;(AllowOverlap=1,Count=0)),(
    % spacers not allowed at beginning not intermediate
    (( AllowStartSpacer=0,NumberSpacer=0,((AllowIntermediateSpacer=0;Count=0),FinalPred));
    % spacers allowed only between recursive matches
    (Count>0,
     % test intermediate spacers
     (AllowIntermediateSpacer=1,anySpacer([X|Y],Z,FinalPred,1,MinSpacer,MaxSpacer,IntermediateSpacer))
    ));
    % spacers are allowed before matching
    (AllowStartSpacer=1,anySpacer([X|Y],TmpOutput,FinalPred,1, MinStartSpacer,MaxStartSpacer,NumberSpacer))
   ));
   % Overlap allowed
   ((AllowOverlap=1,Count>0),NumberSpacer=0,
    testOverlap([X|Y],FinalPred,PreviousMatch,NewMatch,Z)
   ))
   ),
   FinalPred=..Result,length(Result,ResultLength), InfoCount is ResultLength-2, TmpErrorCount is ResultLength-3,nth0(InfoCount,Result,InfoData),nth0(TmpErrorCount,Result,ErrorData),TmpOut is ResultLength-1,nth0(TmpOut,Result,Output),
   NewErrorCount is ErrorCount + ErrorData,
   append(InfoList,InfoData,NewInfoList),
   % got a match
   CountInc is Count+1,
   % Stop backward match search if predicate is exact match because only 1 result is possible
   % TODO: check why duplicate results appear in other cases
   ((Head='isexact',!);(Continue=1)),
   %!,
   % call function again (recursive call), start spacer set to zero because not applicable anymore
   (repeatPredicate(Output, FinalPred, InitParams,CountInc, N, 0, 0, 0,AllowIntermediateSpacer, MinSpacer, MaxSpacer, AllowOverlap, NewMatch, 0, NewErrorCount, Errors, NewInfoList, Info, Z2))
   ).


% Create a list based on input parameter list and sets 0 if it is a variable, 1 else.

%% getKnownVariables(+Input:list,-Output:list)
%
% Create a list based on input parameter list and sets 0 if it is a variable, 1 else.
%
% @param Input input list
% @param Output  output list after analyse.
%
getKnownVariables([X|Y],Z):-getKnownVariables([X|Y],[],Z).
% sub predicates
getKnownVariables([X],List,Z):-((var(X),append(List,[0],NewList));(\+var(X),append(List,[1],NewList))),Z=NewList.
getKnownVariables([X|Y],List,Z):-((var(X),append(List,[0],NewList));(\+var(X),append(List,[1],NewList))),getKnownVariables(Y,NewList,Z).

% Analyse list of parameters and a previous list of set/unset parameters. Reset init variables and keep other ones.

%% resetParamList(+Input:list,+InitList:list,-Output:list)
%
% Analyse list of parameters and a previous list of set/unset parameters. Reset init variables and keep other ones.
%
% This is required for repeats where output parameters must be reset while input ones must be kept.
%
% @param Input input list
% @param InitList list based on init analyse, @see getKnownVariables/2
% @param Output  output list after analyse.
%
resetParamList([X|Y],InitList,Z):-resetParamList([X|Y],InitList,[],Z).
resetParamList([X],[A],List,Z):- ((A=0,append(List,[_],NewList));(A=1,append(List,[X],NewList))),Z=NewList.
resetParamList([X|Y],[A|B],List,Z):- ((A=0,append(List,[_],NewList));(A=1,append(List,[X],NewList))),resetParamList(Y,B,NewList,Z).



% Main predicate, used by repeatPredicate, do not call directly.
% test if a predicate matches with the current list, concatenated with the previous match to see if there is an overlap
% the first element of the previous match is deleted to prevent finding the word again.
% Example:
%  list= a,c,g,t,c,c,c,....
%  previous word is a,c,g,t
%  predicate looks for exact match a,c,g,t
%  List to be tested must be c,g,t + a,c,g,t,c,c... and NOT a,c,g,t + a,c,g,t,c,c... else first match will be found and will loop again and again
%:-comment(testOverlap/5,"Test a predicate, overlap with previous match is allowed. @p Description: @bf{testOverlap(L,Predicate,PreviousMatch,Match,Z) , list * atom * list * var * var => list * atom * list * list * list}. @p Predicate parameters MUST begin with input list and MUST end with output list. @p Input @begin{itemize} @item @var{L} is input sequence @item @var{Predicate} is the predicate to execute for match. Predicate must be like: @bf{mypredicate([],...,Z)}. First argument will be replaced by @var{L}, output id unified by Z. @item @var{PreviousMatch} is a list representing the previous characters. First element of list is removed to exclude matching with previous element itself. @end{itemize} @p Output: @begin{itemize} @item @var{Z} is output list after match. @item @var{Match} is the word matching the predicate, possibly overalpped with previous sequence input. @end{itemize} ").

testOverlap([X|Y],Pred,PreviousMatch,Match,Z):- length(PreviousMatch,PLength),
            ((PLength>0,(nth0(0, PreviousMatch, FirstElemPreviousMatch), delete(PreviousMatch,FirstElemPreviousMatch,1,NewPreviousMatch)),
            append(NewPreviousMatch, [X|Y],NewList),length(NewPreviousMatch,PrevMatchLength));(PLength=0,PrevMatchLength=0, NewList=[X|Y])),

            doOverlap(NewList,Pred,Z, PrevMatchLength,0, OutCount),length(NewList,InputLength),length(Z,OutputLength),sublist(NewList,Match,OutCount,_,OutputLength).
% sub predicates, do nto call directly
doOverlap([],Pred,Z, PrevMatchLength, Count, OutCount) :- fail.
%Recursivly test if Predicate is true, at each test remove one element from input list up to the number of previous word match
doOverlap([X|Y],Pred,Z, PrevMatchLength, Count, OutCount) :- (Count=<PrevMatchLength),(Pred=..VarDef,nth0(0,VarDef,Head),sublist(VarDef,Params,2,_,1),append([Head],[[X|Y]],NewPred0),append(NewPred0,Params,NewPred1),append(NewPred1,[Z],NewPred),FinalPred=..NewPred),
       %((FinalPred,FinalPred=..VarDef2,length(VarDef2,VarDef2Length),nth1(VarDef2Length,VarDef2,Output),Z=Output,!);
       ((FinalPred,FinalPred=..VarDef2,length(VarDef2,VarDef2Length),nth1(VarDef2Length,VarDef2,Output),Z=Output, OutCount=Count);
       (NewCount is Count+1,FinalPred=..VarDef2,length(VarDef2,VarDef2Length),nth1(VarDef2Length,VarDef2,Output),Z=Output,
       doOverlap(Y,Pred,Z, PrevMatchLength, NewCount, OutCount))).



% Check if a list of chars are part of alphabet, return remaining list

%% any(+Input:list,-Output:list)
%
% Check if a list of chars are part of alphabet, return remaining list
%
% Used to match any character, usefull to match any begining or end.
%
% @param Input input list
% @param Output  output list after analyse.
%
any([],Z) :- Z = [].
any([X|Y],Z) :- (isalphabet(X), any(Y,Z));  Z = Y.
% End of any match

% (4) match list with motif , allowing overlop over previous match
% Main predicate

%% isoverlap(+Input:list,+Pattern,+PreviousMatch,+Max:int,-Output:list)
%
% Looks for a match, with errors allowed and overlap between matches.
%
% Used to match any character, usefull to match any begining or end.
%
% @param Input input list
% @param Pattern string to search
% @param PreviousMatch match to try the overlap against
% @param Max max number of errors allowed ( @see isexactwithgapanderror/5 ).
% @param Output  output list after analyse.
%
isoverlap([X|Y],[E1 | E2], PreviousMatch, Max, Z) :-
 (isexactwithgapanderror([X|Y],[E1 | E2], 0, Max, ZRes),Z=ZRes);
 (reverse(PreviousMatch , ReversePreviousMatch),
 isoverlap([X|Y],[E1 | E2], ReversePreviousMatch, 0, Max, ZRes),Z=ZRes).
% sub predicate, do not call directly
isoverlap([X|Y],[E1 | E2], [], N, Max, Z) :- !,fail.
% sub predicate, do not call directly
isoverlap([X|Y],[E1 | E2], ReversePreviousMatch, N, Max, Z) :-
 nth0(0, ReversePreviousMatch, FirstElemPreviousMatch),
 select(FirstElemPreviousMatch,ReversePreviousMatch, RestOfReversePreviousMatch),
 append([FirstElemPreviousMatch],[X|Y], OverlapList),
 (isexactwithgapanderror(OverlapList,[E1 | E2], N, Max, ZRes) -> Z=ZRes;(isoverlap(OverlapList,[E1 | E2], RestOfReversePreviousMatch, N, Max, ZRes),Z=ZRes)).



% Check morphism,complement of nucleotide

%% morphism(+Name:charlist,?Left:char,?Right:char) is multi
%
% Defines some morphism on sequence.
%
% 4 default morphism are available: *wcdna* and *wcrna* for respectively dna and rna complements, *wooble* for rna, while *p2d* maps a protein string definition to its dna translations.
%
% @param Name name fo the morphism
% @param Left left side of the test
% @param Right right side of the test
%

morphism('wcdna','t','a').
morphism('wcdna','a','t').
morphism('wcdna','c','g').
morphism('wcdna','g','c').

morphism('wcrna','u','a').
morphism('wcrna','a','u').
morphism('wcrna','c','g').
morphism('wcrna','g','c').

morphism('wooble','u','a').
morphism('wooble','u','g').
morphism('wooble','a','u').
morphism('wooble','c','g').
morphism('wooble','g','c').
morphism('wooble','g','u').

morphism('wcdna','k','m').
morphism('wcdna','m','k').
morphism('wcdna','r','y').
morphism('wcdna','r','y').
morphism('wcdna','w','w').
morphism('wcdna','s','s').
morphism('wcdna','b','v').
morphism('wcdna','v','b').
morphism('wcdna','h','d').
morphism('wcdna','d','h').
morphism('wcdna','n','n').
morphism('wcdna','x','n').


morphism('wcrna','k','m').
morphism('wcrna','m','k').
morphism('wcrna','r','y').
morphism('wcrna','r','y').
morphism('wcrna','w','w').
morphism('wcrna','s','s').
morphism('wcrna','b','v').
morphism('wcrna','v','b').
morphism('wcrna','h','d').
morphism('wcrna','d','h').
morphism('wcrna','n','n').
morphism('wcrna','x','n').

morphism('wooble','k','m').
morphism('wooble','m','k').
morphism('wooble','r','y').
morphism('wooble','r','y').
morphism('wooble','w','w').
morphism('wooble','s','s').
morphism('wooble','b','v').
morphism('wooble','v','b').
morphism('wooble','h','d').
morphism('wooble','d','h').
morphism('wooble','n','n').
morphism('wooble','x','n').

% Protein to dna translation
%Fix 1682 add support for protein to dna mapping

morphism('p2d','f',[t,t,t]).
morphism('p2d','f',[t,t,c]).
morphism('p2d','l',[t,t,a]).
morphism('p2d','l',[t,t,g]).
morphism('p2d','l',[c,t,t]).
morphism('p2d','l',[c,t,c]).
morphism('p2d','l',[c,t,a]).
morphism('p2d','l',[c,t,g]).
morphism('p2d','i',[a,t,t]).
morphism('p2d','i',[a,t,c]).
morphism('p2d','i',[a,t,a]).
morphism('p2d','m',[a,t,g]).
morphism('p2d','v',[g,t,t]).
morphism('p2d','v',[g,t,c]).
morphism('p2d','v',[g,t,a]).
morphism('p2d','v',[g,t,g]).
morphism('p2d','s',[t,c,t]).
morphism('p2d','s',[t,c,c]).
morphism('p2d','s',[t,c,a]).
morphism('p2d','s',[t,c,g]).
morphism('p2d','p',[c,c,t]).
morphism('p2d','p',[c,c,c]).
morphism('p2d','p',[c,c,a]).
morphism('p2d','p',[c,c,g]).
morphism('p2d','t',[a,c,t]).
morphism('p2d','t',[a,c,c]).
morphism('p2d','t',[a,c,a]).
morphism('p2d','t',[a,c,g]).
morphism('p2d','a',[g,c,t]).
morphism('p2d','a',[g,c,c]).
morphism('p2d','a',[g,c,a]).
morphism('p2d','a',[g,c,g]).
morphism('p2d','y',[t,a,t]).
morphism('p2d','y',[t,a,c]).
morphism('p2d','*',[t,a,a]).
morphism('p2d','*',[t,a,g]).
morphism('p2d','h',[c,a,t]).
morphism('p2d','h',[c,a,c]).
morphism('p2d','q',[c,a,a]).
morphism('p2d','q',[c,a,g]).
morphism('p2d','n',[a,a,t]).
morphism('p2d','n',[a,a,c]).
morphism('p2d','k',[a,a,a]).
morphism('p2d','k',[a,a,g]).
morphism('p2d','d',[g,a,t]).
morphism('p2d','d',[g,a,c]).
morphism('p2d','e',[g,a,a]).
morphism('p2d','e',[g,a,g]).
morphism('p2d','c',[t,g,t]).
morphism('p2d','c',[t,g,c]).
morphism('p2d','*',[t,g,a]).
morphism('p2d','r',[c,g,t]).
morphism('p2d','r',[c,g,c]).
morphism('p2d','r',[c,g,a]).
morphism('p2d','r',[c,g,g]).
morphism('p2d','s',[a,g,t]).
morphism('p2d','d',[a,g,c]).
morphism('p2d','r',[a,g,a]).
morphism('p2d','r',[a,g,g]).
morphism('p2d','g',[g,g,t]).
morphism('p2d','g',[g,g,c]).
morphism('p2d','g',[g,g,a]).
morphism('p2d','g',[g,g,g]).

morphism('p2d','F',[t,t,t]).
morphism('p2d','F',[t,t,c]).
morphism('p2d','L',[t,t,a]).
morphism('p2d','L',[t,t,g]).
morphism('p2d','L',[c,t,t]).
morphism('p2d','L',[c,t,c]).
morphism('p2d','L',[c,t,a]).
morphism('p2d','L',[c,t,g]).
morphism('p2d','I',[a,t,t]).
morphism('p2d','I',[a,t,c]).
morphism('p2d','I',[a,t,a]).
morphism('p2d','M',[a,t,g]).
morphism('p2d','V',[g,t,t]).
morphism('p2d','V',[g,t,c]).
morphism('p2d','V',[g,t,a]).
morphism('p2d','V',[g,t,g]).
morphism('p2d','S',[t,c,t]).
morphism('p2d','S',[t,c,c]).
morphism('p2d','S',[t,c,a]).
morphism('p2d','S',[t,c,g]).
morphism('p2d','P',[c,c,t]).
morphism('p2d','P',[c,c,c]).
morphism('p2d','P',[c,c,a]).
morphism('p2d','P',[c,c,g]).
morphism('p2d','T',[a,c,t]).
morphism('p2d','T',[a,c,c]).
morphism('p2d','T',[a,c,a]).
morphism('p2d','T',[a,c,g]).
morphism('p2d','A',[g,c,t]).
morphism('p2d','A',[g,c,c]).
morphism('p2d','A',[g,c,a]).
morphism('p2d','A',[g,c,g]).
morphism('p2d','Y',[t,a,t]).
morphism('p2d','Y',[t,a,c]).
morphism('p2d','H',[c,a,t]).
morphism('p2d','H',[c,a,c]).
morphism('p2d','Q',[c,a,a]).
morphism('p2d','Q',[c,a,g]).
morphism('p2d','N',[a,a,t]).
morphism('p2d','N',[a,a,c]).
morphism('p2d','K',[a,a,a]).
morphism('p2d','K',[a,a,g]).
morphism('p2d','D',[g,a,t]).
morphism('p2d','D',[g,a,c]).
morphism('p2d','E',[g,a,a]).
morphism('p2d','E',[g,a,g]).
morphism('p2d','C',[t,g,t]).
morphism('p2d','C',[t,g,c]).
morphism('p2d','R',[c,g,t]).
morphism('p2d','R',[c,g,c]).
morphism('p2d','R',[c,g,a]).
morphism('p2d','R',[c,g,g]).
morphism('p2d','S',[a,g,t]).
morphism('p2d','D',[a,g,c]).
morphism('p2d','R',[a,g,a]).
morphism('p2d','R',[a,g,g]).
morphism('p2d','G',[g,g,t]).
morphism('p2d','G',[g,g,c]).
morphism('p2d','G',[g,g,a]).
morphism('p2d','G',[g,g,g]).


morphism('foo','t','c').
morphism('foo','ta','a').
morphism('foo','at','aa').

% End of morphism definition

% End of check complement



% suffixmatch

%% suffixmatch(+ProgramName:char-list,+MotifFileName:char-list,+Distance:int,+MinStart:int,+MaxStart:int,-OutputPos:int,-Length:int,+MuteOnly:int,-Errors:int)
%
% Find matches using an external seach tool starting after current position, errors are allowed ( hamming or edit)
%
% Fix 1410 zombie processes
%
% @param ProgramName program to call
% @param MotifFileName path to the file where *pattern* will be written
% @param Distance Max number of errors allowed
% @param MinStart minimum position to search
% @param MaxStart maximum position to search
% @param OutputPos match position in sequence
% @param Length size of the match
% @param MuteOnly Allow only substitutions if equal to 1, else put 0 to allow insert/deletion/substitution
% @param Errors Number fo errors found
%

%suffixmatch(MotifFileName,NbError,MinStart,MaxStart,Pos,Len,MuteOnly, Errors):-sequenceData(Sequence,_),number_codes(NbError,TmpErr),atom_codes(Err,TmpErr),number_codes(MinStart,TmpMin),atom_codes(Min,TmpMin),number_codes(MaxStart,TmpMax),atom_codes(Max,TmpMax),number_codes(MuteOnly,TmpMute),atom_codes(Mute,TmpMute),process_create(path(bash),['suffixSearch.sh', Err,Min,Sequence,MotifFileName,Mute,Max],[stdin(null),stdout(pipe(Out)),stderr(null),process(Proc)]),
%Fix 1410 zombie processes
suffixmatch(ProgramName,MotifFileName,NbError,MinStart,MaxStart,Pos,Len,MuteOnly, Errors):-sequenceData(Sequence,_,_),number_codes(NbError,TmpErr),atom_codes(Err,TmpErr),number_codes(MinStart,TmpMin),atom_codes(Min,TmpMin),number_codes(MaxStart,TmpMax),atom_codes(Max,TmpMax),number_codes(MuteOnly,TmpMute),atom_codes(Mute,TmpMute),random(Unique),logolShell(Executor),!,process_create(path(Executor),[ProgramName, Err,Min,Sequence,MotifFileName,Mute,Max],[stdin(null),stdout(pipe(Out)),stderr(null),process(Proc)]),
                 repeat,
                 read_line(Out,T), ((T == end_of_file);(number_codes(X,T),read_line(Out,TLen),number_codes(XLen,TLen),read_line(Out,TErr),number_codes(XErr,TErr),assert(suffixmatch(Unique,X,XLen,XErr)) )), % Read a term
                 (T == end_of_file),     % Loop back if not at end of file
                  !,
                  close(Out),process_wait(Proc,Exit),suffixmatch(Unique,Pos,Len,Errors),retractall(suffixmatch(Unique,_,_,_)).

% end of suffixmatch

% savemotif in filename, evaluate current pos, exec suffixmatch, get new remaining list.

%% suffixmatch(+ProgramName:char-list,+MotifFileName:char-list,+Pattern:list,+Distance:int,+SequenceLength:int,+InputSeq:int,+MuteOnly:int,-Errors:int,-OutputPos:int,+MinSpacer:int,+MaxSpacer:int,-SpacerSize:int)
%
% Find matches using an external seach tool starting after current position, errors are allowed ( hamming or edit).
% Method: save motif in filename, evaluate current pos, exec suffixmatch, get new remaining list.
%
% @param ProgramName program to call
% @param MotifFileName path to the file where *pattern* will be written
% @param Pattern pattern to search
% @param Distance Max number of errors allowed
% @param SequenceLength size of the sequence
% @param InputSeq current position on sequence
% @param MuteOnly hamming only, set to 1, else 0
% @param Errors Number fo errors found
% @param OutputPos match position in sequence
% @param MinSpacer minimum spacers before match
% @param MaxSpacer maximum spacers before match
% @param SpacerSize number of spacers before match
%
getsuffixmatch(ProgramName,MotifFilename,Motif,Distance,SequenceLength,InputSeq,MuteOnly,Errors,OutputSeq,MinSpacer,MaxSpacer,SpacerSize):-
sequenceData(SeqFile,_,_),atom_chars(SeqFile,SeqFileAtom),atom_chars(MotifFilename,MotifFileAtom),append(SeqFileAtom,MotifFileAtom,TmpFilename),atom_chars(Filename,TmpFilename),
writeSequence(Filename,Motif),length(InputSeq,InputLength), CurPos is SequenceLength - InputLength, MinPos is CurPos + MinSpacer, ( (MaxSpacer=0,MaxPos=0);(MaxSpacer>0, MaxPos is CurPos + MaxSpacer)) ,suffixmatch(ProgramName,Filename,Distance,MinPos,MaxPos,Pos,Len,MuteOnly,Errors),
           SpacerSize is Pos-CurPos,Size is SpacerSize+Len,spacer_withresult(InputSeq,Size,Size,Word,OutputSeq).


% spacer_noresult: any alphabet character with Min =< size =< Max , checks only, no result


% (6) Spacer: any alphabet character with Min =< size =< Max, return remaining list
% Spacer might be empty
% Main predicate

%% spacer_withresult(+Input:list,+Min:int,+Max:int,+Word:list,-Output:list)
%
% Spacer, match any alphabet within a range, return the result of the match.
%
% @param Input input list to analyse
% @param Min  min number of spacer
% @param Max  max number of spacer
% @param Word string to search
% @param Output remaining list after match
%
spacer_withresult([X|Y],Min, Max, Word, Z) :- (((Min=0,NewX=[X|Y]);(Min>0,cut_sequence([X|Y],Min,NewX))),sublist([X|Y],SP,0,Min,_),NewMax is Max - Min, spacer_withresult(NewX,0,0,NewMax,Z,SP, Word)).
%spacer_withresult([X|Y],Min, Max, Word, Z) :- (Min=0,cut_sequence([X|Y],Max,Z),sublist([X|Y],Word,0,Max,_),!)
%           ;(cut_sequence([X|Y],Min,NewX),sublist([X|Y],SP,0,Min,_),NewMax is Max - Min, spacer_withresult(NewX,0,0,NewMax,Z,SP, Word)).

%spacer_withresult([X|Y],Min, Max, Z, Word) :- N is 0,((Min=0,Z=[X | Y]);(spacer_withresult([X|Y],N,Min,Max,Z,[], Word))).

% sub predicate, do not call directly
%spacer_withresult([], N, Min, Max, Z, Spacer, Word) :- fail.
spacer_withresult([], N, Min, Max, Z, Spacer, Word) :- Word=Spacer.
% sub predicate, do not call directly
%spacer_withresult([X|Y],N, Min, Max, Z, Spacer, Word) :- Ninc is N+1,append(Spacer, [X], IncWord),((Ninc<Max,isalphabet(X), spacer_withresult(Y,Ninc,Min,Max,Z,IncWord, Word));(Ninc>=Min,Z = Y, Word=IncWord)).
spacer_withresult([X|Y],N, Min, Max, Z, Spacer, Word) :-  (Z = [X |Y], Word=Spacer); (Ninc is N+1,append(Spacer, [X], IncWord),Ninc=<Max, spacer_withresult(Y,Ninc,Min,Max,Z,IncWord, Word)).
% End of spacer


% (1) Checks 2 words match
% sub predicates, do not call directly
isexact([]).
isexact( X , [] , Z) :- Z=X.
%isexact( [ X | Y], [E1 | E2] , Z) :- X=E1 , isexact(Y, E2, Z).
% Main predicate

%% isexact(+Input:list,+Word:list,-Output:list)
%
% Match if 2 sequences are equal.
%
% @param Input input list to analyse
% @param Word string to search
% @param Output remaining list after match
%
isexact( [ X | Y], [E1 | E2] , Z) :- isequal(X,E1) , isexact(Y, E2, Z).


%% isexact(+Input:list,+Word:list,-Errors:int,-Output:list)
%
% Match if 2 sequences are equal.
%
% @see isexact/4
% @param Input input list to analyse
% @param Word string to search
% @param Errors Always 0 (for compatibility)
% @param Output remaining list after match
%

isexact( [], [], Errors, Z):- Z=[],Errors=0.
isexact( [ X | Y], [E1 | E2] , Errors, Z) :- isequal(X,E1) , isexact(Y, E2, Z), Errors=0.





% sub predicate for isexact/5
isexact([],E,Z,Spacer,CountSpacer, NumberSpacer):-fail.
isexact([X|Y],E,Z,Spacer,CountSpacer, NumberSpacer):- (Spacer=0,isexact([X|Y],E,Z),NumberSpacer=0);(Spacer=1,((isexact([X|Y],E,Z),NumberSpacer=CountSpacer);(NewCountSpacer is CountSpacer+1,isexact(Y,E,Z,Spacer,NewCountSpacer,NumberSpacer)))).

% Main predicate

%% isexact(+Input:list,+Word:list,-Output:list,+Spacer:int,-NumberSpacer:int)
%
% Match if 2 sequences are equal, spacer allowed before match.
%
% @param Input input list to analyse
% @param Word string to search
% @param Output remaining list after match
% @param Spacer Max spacers allowed
% @param NumberSpacer Number of spacers foudn before match
%

isexact([X|Y],E,Z,Spacer, NumberSpacer):- isexact([X|Y],E,Z,Spacer, 0, NumberSpacer).



% Main predicate

%% isexactwithgaponly(+Input:list,+Word:list,+Max:int,-Errors:int,-Output:list)
%
% Match if 2 sequences are equal with gaps only (no substitution). Returns the number of errors.
%
% @param Input input list to analyse
% @param Word string to search
% @param Max Max errors allowed
% @param Errors Number of errors foudn before match
% @param Output remaining list after match
%

isexactwithgaponly([X|Y],[E1 | E2], Max, Errors, Z) :- isexactwithgaponly([X|Y],[E1 | E2], 0, Max, Errors, Z).


isexactwithgaponly([]).
% Fix 1336
isexactwithgaponly( [] , [] , N, Max, Errors, Z) :- N=<Max,Z=[], Errors=N.
isexactwithgaponly( [X|Y] , [] , N, Max, Errors, Z) :- N=<Max,((Z=[X|Y], Errors=N);(Ninc is N + 1,isexactwithgaponly(Y,[], Ninc, Max ,Errors,Z))).
% End fix 1336
isexactwithgaponly([X|Y],[E1 | E2], N, Max, Errors, Z) :-  N=<Max,((length([E1|E2],ModelSize),PossibleErr is N + ModelSize, PossibleErr =<Max ,Z=[X|Y],Errors=PossibleErr);(isequal(X,E1), isexactwithgaponly(Y,E2,N, Max,Errors,Z));(\+isequal(X,E1),Ninc is N+1,isexactwithgaponly(Y,[E1 | E2],Ninc, Max,Errors,Z ));(\+isequal(X,E1),Ninc is N+1,isexactwithgaponly( [X | Y], E2,Ninc, Max,Errors,Z ))).




% (9) Check if list XY is equal to constant E1E2, support insertions and substitutions until Max errors
% sub predicates, do not call directly
isexactwithgapanderror([]).

%Fix 1336
% change from:
% isexactwithgapanderror( X , [] , N, Max, Errors, Z) :- N=<Max,Z=X, Errors=N.
% to:
isexactwithgapanderror( [] , [] , N, Max, Errors, Z) :- N=<Max,Z=[], Errors=N.
isexactwithgapanderror( [X|Y] , [] , N, Max, Errors, Z) :- N=<Max,((Z=[X|Y], Errors=N);(Ninc is N + 1,isexactwithgapanderror(Y,[], Ninc, Max ,Errors,Z))).
%End fix 1336


% FIX: remove stop backward operator
%isexactwithgapanderror([X|Y],[E1 | E2], N, Max, Errors, Z) :-  N=<Max,((isequal(X,E1),!, isexactwithgapanderror(Y,E2,N, Max,Errors,Z));(\+isequal(X,E1),Ninc is N+1,!,isexactwithgapanderror(Y,E2,Ninc, Max,Errors,Z ));(\+isequal(X,E1),Ninc is N+1,!,isexactwithgapanderror(Y,[E1 | E2],Ninc, Max,Errors,Z ));(\+isequal(X,E1),Ninc is N+1,!,isexactwithgapanderror( [X | Y], E2,Ninc, Max,Errors,Z ))).
isexactwithgapanderror([X|Y],[E1 | E2], N, Max, Errors, Z) :-  N=<Max,((length([E1|E2],ModelSize),PossibleErr is N + ModelSize, PossibleErr =<Max ,Z=[X|Y],Errors=PossibleErr);(isequal(X,E1), isexactwithgapanderror(Y,E2,N, Max,Errors,Z));(\+isequal(X,E1),Ninc is N+1,isexactwithgapanderror(Y,E2,Ninc, Max,Errors,Z ));(\+isequal(X,E1),Ninc is N+1,isexactwithgapanderror(Y,[E1 | E2],Ninc, Max,Errors,Z ));(\+isequal(X,E1),Ninc is N+1,isexactwithgapanderror( [X | Y], E2,Ninc, Max,Errors,Z ))).


% Main predicate

%% isexactwithgapanderror(+Input:list,+Word:list,+Max:int,-Errors:int,-Output:list)
%
% Match if 2 sequences are equal with or without substitutions and gaps. Returns the number of errors.
%
% @param Input input list to analyse
% @param Word string to search
% @param Max Max errors allowed
% @param Errors Number of errors foudn before match
% @param Output remaining list after match
%

isexactwithgapanderror([X|Y],[E1 | E2], Max, Errors, Z) :- isexactwithgapanderror([X|Y],[E1 | E2], 0, Max, Errors, Z).



% Main predicate

%% isexactwithgapanderror(+Input:list,+Word:list,+Max:int,-Output:list)
%
% Match if 2 sequences are equal with or without substitutions and gaps. Returns the number of errors.
%
% @see isexactwithgapanderror/5 (errors are not returned here).
% @param Input input list to analyse
% @param Word string to search
% @param Max Max errors allowed
% @param Output remaining list after match
%

isexactwithgapanderror([X|Y],[E1 | E2], Max, Z) :- isexactwithgapanderror([X|Y],[E1 | E2], 0, Max, Errors, Z).

% End of check error and gaps

% (9) Check if list XY is equal to constant E1E2, support errors only until Max errors
% sub predicates, do not call directly
isexactwitherroronly([]).
isexactwitherroronly( X , [] , N, Max, Errors, Z) :- N=<Max,Z=X, Errors=N.
isexactwitherroronly([X|Y],[E1 | E2], N, Max, Errors, Z) :- N=<Max,((isequal(X,E1), isexactwitherroronly(Y,E2,N, Max, Errors,Z));(\+isequal(X,E1),Ninc is N+1,isexactwitherroronly(Y,E2,Ninc, Max, Errors, Z ))).
% End of check errors only


% Main predicate

%% isexactwitherroronly(+Input:list,+Word:list,+Max:int,-Output:list)
%
% Match if 2 sequences are equal with substitutions (gaps not allowed), returns the number of errors.
%
% @param Input input list to analyse
% @param Word string to search
% @param Max Max errors allowed
% @param Output remaining list after match
%

isexactwitherroronly([X|Y],[E1 | E2], Max, Errors, Z) :- isexactwitherroronly([X|Y],[E1 | E2], 0, Max, Errors, Z).



% Main predicate

%% isexactwitherroronly(+Input:list,+Word:list,+Max:int,-Output:list)
%
% Match if 2 sequences are equal with or without substitutions and gaps. Returns the number of errors.
%
% @see isexactwitherroronly/5 (errors are not returned here).
% @param Input input list to analyse
% @param Word string to search
% @param Max Max errors allowed
% @param Output remaining list after match
%

isexactwitherroronly([X|Y],[E1 | E2], Max, Z) :- isexactwitherroronly([X|Y],[E1 | E2], 0, Max, _, Z).

% Check if characters are equal
%isequal(X,Y):- X=Y.

% (8) Compare complement of X with Y
%:-comment(iscompequal/2,"Match if dna input sequence match the complement of motif.@p Description: @begin{itemize} @item @bf{iscompequal(L,Motif) , list * list  => list * list }. @end{itemize} @p Input: @begin{itemize} @item @var{L} is input sequence @item @var{Motif} is motif to compare to @end{itemize} @p Output: None ").

%% iscompequal(+Input:list,+Word:list)
%
% Match if dna input sequence match the complement of *Word*.
%
% @param Input input list to analyse
% @param Word string to compare
%

iscompequal(X,Y):- getcomplement(X,Z), isequal(Z,Y).
% end of complement equal

% (3) Get the complement of a list
getcomplement([],Tmplist,Z):- Z=Tmplist.
getcomplement([X|Y],Tmplist,Z):- morphism('wcdna',X,CX),append(Tmplist,[CX],Tmplist2),getcomplement(Y,Tmplist2,Z).
%Main predicate

%% getcomplement(+Input:list,-Output:list)
%
% Get the complement of a dna sequence.
%
% @param Input input list to analyse
% @param Output complement of input list
%
getcomplement([X|Y],Z):- morphism('wcdna',X,CX),append([],[CX],Tmplist),getcomplement(Y,Tmplist,Z).


%% getreversecomplement(+Input:list,-Output:list)
%
% Get the reverse complement of a dna sequence.
%
% @see morphism/3
% @see getcomplement/2
% @param Input input list to analyse
% @param Output reverse complement of input list
%

getreversecomplement([X|Y],Z):- morphism('wcdna',X,CX),append([],[CX],Tmplist),getcomplement(Y,Tmplist,Z2),reverse(Z2,Z).

%End of get complement

% (3) Get morphism of a word (works for complement too
% Main predicate


%% applymorphism(+Input:list,+Morph:char-list,Reverse:int,-Output:list)
%
% Apply morphism *Morph* on input data.
% Fix 1682
%
% @see morphism/3
% @param Input input list to analyse
% @param Morph morphism name
% @param Reverse apply reverse operation on string before morphism is equal to 1, else keep to 0.
% @param Output output list
%
applymorphism([],Morph,Reverse,Z):- Z=[].
%Fix 1682 add support for protein to dna mapping
applymorphism([X|Y],Morph,Reverse,Z):- ((Reverse=1,reverse([X|Y],List));(Reverse=0,List=[X|Y]))   ,matchmorphism(Morph,List,CX, CZ),(is_list(CX)->(append([],CX,Tmplist));(atom_chars(CX,CXLIST),append([],CXLIST,Tmplist))),getmorphism(CZ,Morph,Tmplist,Z).
%Sub predicate, do not call directly
getmorphism([],Morph,Tmplist,Z):- Z=Tmplist.
%Sub predicate, do not call directly
getmorphism([X|Y],Morph,Tmplist,Z):- matchmorphism(Morph,[X|Y],CX,CZ),(is_list(CX)->(append(Tmplist,CX,Tmplist2));(atom_chars(CX,CXLIST),append(Tmplist,CXLIST,Tmplist2))),getmorphism(CZ,Morph,Tmplist2,Z).

% Main predicate, used by getmorphism
% Morph: name of morph array
% X|Y: list to analyse
% CX: returns matched character of morphism
% CZ: returns remaining list
matchmorphism(Morph,[X|Y],CX,CZ):- matchmorphism(Morph,[X|Y],[],CX,CZ).
%Sub predicate, do not call directly
matchmorphism(Morph, [], Prev, CX, CZ):- !,fail.
%Sub predicate, do not call directly
matchmorphism(Morph, [X|Y], Prev, CX, CZ) :- append(Prev,[X],NewPrev),atom_chars(Morphsearch,NewPrev),((morphism(Morph, Morphsearch, CX), CZ=Y);matchmorphism(Morph, Y, NewPrev, CX, CZ)).

% End of morphism



% Check if character is part of known alphabet
isalphabet(X) :- member(X,[a,c,g,t]).
isDna(X) :- member(X,[a,c,g,t]).
isProtein(X) :- member(X,[a,c,d,e,f,g,h,i,k,l,m,n,p,q,r,s,t,v,w,y]).
isRna(X) :- member(X,[g,a,u,c]).
% end of alphabet check


% Check cost for the string
% Matrix is name of matrix element to use, Min and Max are cost values validating the query
% Length of matched word must be lower than matrix length
% Main predicate


%% checkCost(+Input:list,+Matrix:char-list,+Min:int,+Max:int)
%
% Match for a sequence whose cost is lower than *Max* according to *Matrix*.
%
% To match a sequence with a cost, a spacer  must be searched, then predicate should be applied on results.
%
% @param Input input list to analyse
% @param Matrix name of the matrix to use to calculate the cost.
% @param Min minimal cost value.
% @param Max maximal cost value
%

checkCost([X|Y],Matrix,Min,Max):- checkCost([X|Y],0,Matrix,0,Min,Max).
% sub predicate, do not call directly
checkCost([],CurPos,Matrix,Cost,Min,Max):- Cost>=Min,Cost=<Max, length(MatrixAtom,MatrixLength), CurPos<MatrixLength.
% sub predicate, do not call directly
checkCost([X|Y],CurPos,Matrix,Cost,Min,Max):- matrix(Matrix,X,MatrixAtom), length(MatrixAtom,MatrixLength), CurPos<MatrixLength,
            nth0(CurPos,MatrixAtom,AtomCost),
            NewCost is Cost+AtomCost, NewPos is CurPos+1,
            checkCost(Y,NewPos,Matrix,NewCost,Min,Max).

% Main predicate

%% checkCost(+Input:list,+Matrix:char-list,+Min:int,+Max:int,-Output:list)
%
% Match for a sequence whose cost is lower than *Max* according to *Matrix*.
%
% To match a sequence with a cost, a spacer  must be searched, then predicate should be applied on results.
%
% @param Input input list to analyse
% @param Matrix name of the matrix to use to calculate the cost.
% @param Min minimal cost value.
% @param Max maximal cost value
% @param Output output list after match
%

checkCost([X|Y],Matrix,Min,Max,Z):- checkCost([X|Y],0,Matrix,0,Min,Max,Z).
% sub predicate, do not call directly
checkCost([],CurPos,Matrix,Cost,Min,Max,Z):- Cost>=Min,Cost=<Max, length(MatrixAtom,MatrixLength), CurPos<MatrixLength, Z=Cost.
% sub predicate, do not call directly
checkCost([X|Y],CurPos,Matrix,Cost,Min,Max,Z):- (matrix(Matrix,X,MatrixAtom), length(MatrixAtom,MatrixLength), CurPos<MatrixLength,
            nth0(CurPos,MatrixAtom,AtomCost),
            NewCost is Cost+AtomCost, ((CurPos is MatrixLength-1,NewPos=0);(\+ CurPos is MatrixLength-1,NewPos is CurPos+1)),
            checkCost(Y,NewPos,Matrix,NewCost,Min,Max,Z)).

% End of check cost

% General purpose functions
wordEnd([X,Y|_], EndPosition) :- length(X,Size), EndPosition is Y+Size.
% X and Y are List before word and after word finding, Y < X.


% Cut a sequence with Length characters, return remaining list

%% cut_sequence(+Input:list,+Length:int,-Output:list)
%
% cut an input sequence with a specified size.
%
% @param Input input list to analyse
% @param Length Size to cut
% @param Output output list
%

cut_sequence(X,Length,Z):- sublist(X,Z,Length,_,0).


%% wordSize(+Before:list,+After:list,-Size:int)
%
% Get the size of a matched word.
%
% @param Before List before match
% @param After List after match
% @param Size size of the word
%

wordSize(X,Y,WS) :- length(X,SizeX),length(Y,SizeY),WS is SizeX-SizeY.


%% wordSize(+Before:list,+After:list,+Spacer:int,-Size:int)
%
% Get the size of a matched word removing spacer if any.
%
% @param Before List before match
% @param After List after match
% @param Spacer Numerb fo spacer in front of match
% @param Size size of the word
%

wordSize(X,Y,Spacer,WS) :- length(X,SizeX),length(Y,SizeY),WS is (SizeX-SizeY-Spacer).


% X is a List, N is number of errors. Returns percentage of errors relative to list size

%% percent(+Word:list,+Size:int,-Percent:int)
%
% Get a percentage according to word size.
%
% @param Word word to analyse
% @param Size number of chars in word to get percentage
% @param Percent percent (Size*100/WordSize)
%

percent(X,N,Percentage):- length(X,SizeX), Percentage is (N*100/SizeX).



%% percent2int(+Word:list,-Size:int,+Percentage:int)
%
% Get a number of char according to word size and allowed percentage.
%
% @param Word word to analyse
% @param Size number of chars corresponding to percentage
% @param Percentage Allowed percentage
%

percent2int(X,N,Percentage):- length(X,SizeX), N is floor(Percentage*SizeX/100).



% Checks that current position is between min and max.

%% checkPosition(+Cur:int,+Min:int,+Max:int)
%
% checks that position is between min and max.
%
% @param Cur current position in sequence
% @param Min minimum position
% @param Max maximum position
%

checkPosition(Cur,Min,Max):- Cur>=Min,Cur=<Max.
% Checks that relative position is between min and max.

%% checkPosition(+Cur:int,+Prev:int,+Min:int,+Max:int)
%
% checks that the relative position of the word is between min and max.
%
% Looks at the distance between 2 words.
%
% @param Cur current position in sequence
% @param Cur previous position in sequence
% @param Min minimum position
% @param Max maximum position
%

checkRelativePosition(Cur,Prev,Min,Max):- Size is (Cur-Prev),Size>=Min,Size=<Max.
% (11) Checks position regarding the Lists
% WholeListSize is the size of the complete List
% ListWordMatch is the remaining List, before word match if testing start condition or after word match if testing end position.


%% checkPosition(+WholeListSize:list,+ListWordMatch:list,+Min:int,+Max:int,+Spacer:int)
%
% Checks that the position of the word is between min and max.
% The check is made based on remaining list to calculate the current position.
%
% Requires offset to be set.
%
% If position is inferior to offset value, then offset is added to get real position, else it means that position is at its real value
%
% Looks at the distance between 2 words.
%
% @param WholeListSize size of the whole sequence
% @param ListWordMatch remaining after last word match
% @param Min minimum position  minimum expected position
% @param Max maximum position maximum expected position
% @param Spacer number of spacer between start of list and word match
%

checkPosition(WholeListSize,ListWordMatch, MinCondition,MaxCondition, Spacer) :- length(ListWordMatch,RemainingSize),TmpPosition is WholeListSize-RemainingSize, Position is TmpPosition + Spacer, offset(Offset),((Position<Offset,PositionWithOffset is Position + Offset);(Position>=Offset,PositionWithOffset=Position)),checkPosition(PositionWithOffset,MinCondition,MaxCondition).
% End of position check

% Get content of matched word
% X: list of characters before word match
% XLength: length of word



%% getPosition(+WholeListLength:int,+List:list,-Position:int)
%
% Get the current position in the sequence, offset is managed.
%
% @param WholeListLength size of the sequence
% @param List remaining list after position
% @param Position Current position with offset
%

getPosition(WholeListLength,List,Position) :- offset(Offset),length(List,ListLength),Position is (WholeListLength-ListLength+Offset).



%% getContent(+List:list,+Length:int,-Word:list)
%
% Get the content of a word
%
% @param List input list
% @param Length length of the word
% @param Word result word
%

getContent(X,XLength,Z) :- sublist(X,Z,0,XLength,_).
% End of get content

%% getContent(+List:list,+Length:int,+Spacer:int,-Word:list)
%
% Get the content of a word, removing spacers in front of match
%
% @param List input list
% @param Length length of the word
% @param Spacer number fo spacer in front of the word
% @param Word result word
%

getContent(X,XLength,Spacer,Z) :- sublist(X,Z,Spacer,XLength,_).
% End of get content



% Save variable
% Creates a list as a varDefinition
% X: name of variable
% YLength: length of while string
% YPre: list before word match
% YPost: list after word match
% returns R: reference in database

%% saveVariable(+Name:char-list,+PosBefore:int,+Spacer:int,+PosAfter:int,+Info:list,+Level:int,+Errors:list,+Config:list,-Variable:list)
%
% Save a variable content as a list. Positions are recalculated according to offset.
%
% Variable = [X,XContent,BeginPos,EndPos,XContentLength, Info, Level, Errors,_,Config], Errors=[Cost,Distance].
%
% Requires sequence info to be set with sequence predicate.
%
% @param Name name of the variable
% @param PosBefore position on sequence before match
% @param Spacer number of spacers in front of the match
% @param PosAfter position on sequence after match
% @param Info sub variables as sub-matches in a list
% @param Level not used
% @param Config configuration data for the variable
% @param Variable result data as a list
%

%[X,XContent,BeginPos,EndPos,XContentLength, Info, Level, Errors,_,Config]
saveVariable(X,Xposbefore,XSpacer,Xposafter,Info,Level,Errors,Config,R) :- offset(Offset),XContentLength is (Xposafter - Xposbefore - XSpacer),wordContent_pos(Xposbefore,XContentLength,XSpacer,XContent),BeginPos is (Offset + Xposbefore + XSpacer),EndPos is BeginPos + XContentLength,R=[X,XContent,BeginPos,EndPos,XContentLength, Info, Level, Errors,_,Config].




%%:-comment(saveVariableByRef/8," Save a variable as a clause in database with additional details@p Description: @p If Reference already exist, replace the content. Requires sequence info to be set with sequence predicate@begin{itemize} @item @bf{saveVariableByRef(VarName, ListBeforeVar, SpacerSizeBeforeVar, ListAfterVar, Info, Level, Errors, Reference) , char-list * list * int * list * char-list * int * int * var => char-list * list * int * list * char-list * int * int * ref}. @end{itemize} @p Input: @begin{itemize} @item @var{VarName} is the name of the variable to save in @pred{varDefinition}@item @var{ListBeforeVar} is the input sequence before word match@item @var{SpacerSizeBeforeVar} is the number of spacers found before word match (0 if not allowed)@item @var{ListAfterVar} is the input sequence after word match@item @var{Info} is info details on var( sub data of a repeat for example @item @var{Level} is the level of the variable within the levels of the query@item @var{Errors} is the number of errors, cost or distance, found to match@end{itemize} @p Output: @var{Reference} database reference, used to get variable (see @pred{getVariable}) ").
%saveVariableByRef(X,YPre0,YPreSpacer ,YPost,Info, Level, Errors, R) :- sequenceData(_,_,YLength),(var(R);(\+ var(R),erase(R))),cut_sequence(YPre0,YPreSpacer,YPre),length(YPre, YPreLength),length(YPost,YPostLength),YPosBegin is YLength-YPreLength,YPosEnd is YLength-YPostLength, YSize is YPosEnd-YPosBegin,getContent(YPre,YSize,YContent),offset(Offset),YPosBeginWithOffset is YPosBegin + Offset,YPosEndWithOffset is YPosEnd + Offset,assert(varDefinition(X,YContent,YPosBeginWithOffset,YPosEndWithOffset,YSize, Info, Level, Errors),R).


%% saveVariable(+Name:char-list,+PosBefore:int,+Size:int,+Info:list,+Level:int,+Errors:list,+Config:list,-Variable:list)
%
% Save a variable content as a list. Positions are recalculated according to offset.
%
% Variable = [X,XContent,BeginPos,EndPos,XContentLength, Info, Level, Errors,_,Config]
%
% Requires sequence info to be set with sequence predicate.
%
% @param Name name of the variable
% @param PosBefore position on sequence before match
% @param Size size of the match
% @param Info sub variables as sub-matches in a list
% @param Level not used
% @param Config configuration data for the variable
% @param Variable result data as a list
%

saveVariable(X,YPos,YSize,Info, Level, Errors, Config,R) :- YPosBegin = YPos,YPosEnd is YPosBegin+YSize,maxsize(Maxsize),((YSize=<Maxsize,getCharsFromPosition(YPos,YSize,YContent));(YSize>Maxsize,YContent=['.','.','.'])),offset(Offset),YPosBeginWithOffset is YPosBegin + Offset,YPosEndWithOffset is YPosEnd + Offset,R=[X,YContent,YPosBeginWithOffset,YPosEndWithOffset,YSize, Info, Level, Errors,_,Config].



%%:-comment(saveVariableByRef/7," Save a variable as a clause in database with additional details@p Description: @p If Reference already exist, replace the content@p Content of variable is saved ONLY if size < maxsize, else save value '...' @p Mainly used for spacers recording where content is not used elsewhere.@begin{itemize} @item @bf{saveVariableByRef(VarName, ListBeforeVar, Size, Info, Level, Errors, Reference) , char-list * list * int *  char-list * int * int * var => char-list * list * int *  char-list * int * int  * ref}. @end{itemize} @p Input: @begin{itemize} @item @var{VarName} is the name of the variable to save in @pred{varDefinition}@item @var{ListBeforeVar} is the input sequence before word match@item @var{Size} is the size of the word@item @var{Info} is info details on var( sub data of a repeat for example @item @var{Level} is the level of the variable within the levels of the query@item @var{Errors} is the number of errors, cost or distance, found to match@end{itemize} @p Output: @var{Reference} database reference, used to get variable (see @pred{getVariable}) ").
%saveVariableByRef(X,YPre,YSize,Info, Level, Errors, R) :- sequenceData(Y,_,YLength),(var(R);(\+ var(R),erase(R))),length(YPre, YPreLength),YPosBegin is YLength-YPreLength,YPosEnd is YPosBegin+YSize,maxsize(Maxsize),((YSize=<Maxsize,getContent(YPre,YSize,YContent));(YSize>Maxsize,YContent=['.','.','.'])),offset(Offset),YPosBeginWithOffset is YPosBegin + Offset,YPosEndWithOffset is YPosEnd + Offset,assert(varDefinition(X,YContent,YPosBeginWithOffset,YPosEndWithOffset,YSize, Info, Level, Errors),R).

%% getVariable(+Variable:list,-Content:list,-PosBefore:int,-PosAfter:int,-Size:int,-Info:list,-Level:int,-Errors:list)
%
% Save a variable content as a list. Positions are recalculated according to offset.
%
% Variable = [X,XContent,BeginPos,EndPos,XContentLength, Info, Level, Errors,_,Config], Errors=[Cost,Distance].
%
% Get a variable from a list
%
% @param Variable variable data as a list
% @param Content match as a string from sequence
% @param PosBefore position on sequence before match
% @param PosAfter position on sequence after match
% @param Size size of the match
% @param Info sub variables as sub-matches in a list
% @param Level not used
% @param Errors errors found for variable.
% @param Name name of the variable
%

getVariable(R,Content,Begin,End,Size,Info,Level,Errors,Name) :-  R=[Name,Content,Begin,End,Size,Info,Level,Errors,_,_].
getVariable(R,Content,Begin,End,Size,Info,Level,Errors,Name,Config) :-  R=[Name,Content,Begin,End,Size,Info,Level,Errors,_,Config].


%:-comment(getParentVariable/2," Get content of parent variable@p Description: @begin{itemize} @item @bf{getParentVariable(Ref, Content) , ref * var => char-list * list}  @end{itemize} @p Input: @begin{itemize} @item @var{Ref} is the reference in database of the saved variable OR a list@end{itemize} @p Output: @begin{itemize} @item @var{Content} is the content of variable@end{itemize} ").

getParentVariable(Ref,Content):-(is_list(Ref),Ref=[_,Content,_,_,_,_,_,_,_]);(\+is_list(Ref),varDefinition(Ref,Content,_,_,_, _, _,_,_)).

%%:-comment(getVariableByRef/9," Get a variable from a clause in database@p Description: @begin{itemize} @item @bf{getVariableByRef(Reference, Content, BeginPosition, EndPosition, Size, Info, Parent, Errors, Name) , ref * var * var * var * var * var *var * var => char-list * list * int * int * int * list * int * int * char-list}. @end{itemize} @p Input: @begin{itemize} @item @var{Reference} is the reference in database of the saved variable@end{itemize} @p Output: @begin{itemize} @item @var{Content} is the content of variable@item @var{BeginPosition} is the start position variable@item @var{EndPosition} is the end position variable@item @var{Size} is the size of variable@item @var{Info} contains information on variable@item @var{Parent} is the nameof the parent of variable@item @var{Errors} is the size of variable@item @var{Name} is the name of variable@end{itemize} ").
%getVariableByRef(R,Content,Begin,End,Size,Info,Level,Errors,Name) :- clause(H,B,R),H=..VarDef,nth0(1,VarDef,VarName),nth0(2,VarDef,VarContent),nth0(3,VarDef,Begin),nth0(4,VarDef,End),nth0(5,VarDef,VarSize),nth0(6,VarDef,VarInfo),nth0(7,VarDef,VarLevel),nth0(8,VarDef,VarErrors),Content=VarContent,Size=VarSize,Info=VarInfo,Level=VarLevel,Errors=VarErrors,Name=VarName.




% Remove variable from assertions

%:-comment(removeVariable/1," Remove a variable from clauses in database@p Description: @begin{itemize} @item @bf{removeVariable(Reference) , char-list => char-list }. @end{itemize} @p Input: @begin{itemize} @item @var{Reference} is the reference in database of the saved variable@end{itemize} @p Output: @p None ").

removeVariable(R) :- erase(R).



% Remove variable from assertions
%:-comment(removeVariableByRef/1," Remove a variable from clauses in database@p Description: @begin{itemize} @item @bf{removeVariableByRef(Reference) , char-list => char-list }. @end{itemize} @p Input: @begin{itemize} @item @var{Reference} is the reference in database of the saved variable@end{itemize}@p Output: None ").

removeVariableByRef(R) :- erase(R).





% Push a variable in a result variable defined by Key.
%:-comment(pushResult/2," Push a variable (see @pred{varDefinition}) in a result variable.@p @bf{CAUTION}: result variable @bf{must} have been created previsouly using predicate @pred{bb_put} like @em{bb_put('myresult',[])}. @p Description: @begin{itemize} @item @bf{pushResult(Key,Variable) , char-list * list  => char-list  * list }. @end{itemize} @p Input: @begin{itemize} @item @var{Key} is the key used at variable creation to store result@item @var{Variable} is variable content@end{itemize} @p Output: @p None").

pushResult(Key,X) :- bb_get(Key, ResultDef),append(ResultDef,[X],NewResult),bb_put(Key,NewResult).


%% writeSequence(+Filename:char-list,+Seq:list)
%
% Writes a sequence in FASTA format in a file.
%
% @param Filename name of the file
% @param Seq string to write
%

writeSequence(Filename,Seq) :- atom_codes(Filename,Name),open(Filename, write, StreamOut),
          atom_chars(X,Seq),format(StreamOut,'>SequenceMatch\n~w',[X]),
          close(StreamOut).


% ###### DNA / RNA comparison clauses

%% isequalnuc(+Left:atom,+Right:atom)
%
% Defines if 2 chars are equal regarding dna/rna definitions (including dna ambiguity).
%
% @param Left char to compare
% @param Right char to compare
%


% Base nucleotides
isequalnuc('a','a').
isequalnuc('c','c').
isequalnuc('g','g').
isequalnuc('t','t').
isequalnuc('u','u').

% Keto
isequalnuc('k','g').
isequalnuc('k','t').
isequalnuc('k','u').
isequalnuc('g','k').
isequalnuc('t','k').
isequalnuc('u','k').

% Amino
isequalnuc('m','a').
isequalnuc('m','c').
isequalnuc('a','m').
isequalnuc('c','m').

% Purine
isequalnuc('r','a').
isequalnuc('r','g').
isequalnuc('a','r').
isequalnuc('g','r').

% Pyrimidine
isequalnuc('y','c').
isequalnuc('y','t').
isequalnuc('y','u').
isequalnuc('c','y').
isequalnuc('t','y').
isequalnuc('u','y').

% Strong
isequalnuc('s','c').
isequalnuc('s','g').
isequalnuc('c','s').
isequalnuc('g','s').

% Weak
isequalnuc('w','a').
isequalnuc('w','t').
isequalnuc('w','u').
isequalnuc('a','w').
isequalnuc('t','w').
isequalnuc('u','w').

% not A
isequalnuc('b','c').
isequalnuc('b','g').
isequalnuc('b','t').
isequalnuc('b','u').
isequalnuc('c','b').
isequalnuc('g','b').
isequalnuc('t','b').
isequalnuc('u','b').

% not T/U
isequalnuc('v','c').
isequalnuc('v','g').
isequalnuc('v','a').
isequalnuc('c','v').
isequalnuc('g','v').
isequalnuc('a','v').

% not G
isequalnuc('h','c').
isequalnuc('h','a').
isequalnuc('h','t').
isequalnuc('h','u').
isequalnuc('c','h').
isequalnuc('a','h').
isequalnuc('t','h').
isequalnuc('u','h').

% not C
isequalnuc('d','a').
isequalnuc('d','g').
isequalnuc('d','t').
isequalnuc('d','u').
isequalnuc('a','d').
isequalnuc('g','d').
isequalnuc('t','d').
isequalnuc('u','d').

% any
isequalnuc('x','a').
isequalnuc('x','g').
isequalnuc('x','t').
isequalnuc('x','u').
isequalnuc('x','c').
isequalnuc('n','a').
isequalnuc('n','g').
isequalnuc('n','t').
isequalnuc('n','u').
isequalnuc('n','c').

isequalnuc('a','x').
isequalnuc('g','x').
isequalnuc('t','x').
isequalnuc('u','x').
isequalnuc('c','x').
isequalnuc('a','n').
isequalnuc('g','n').
isequalnuc('t','n').
isequalnuc('u','n').
isequalnuc('c','n').

% ###### protein comparison clauses

%% isequalprot(+Left:atom,+Right:atom)
%
% Defines if 2 chars are equal regarding protein definitions.
%
% @param Left char to compare
% @param Right char to compare
%

isequalprot(A,B):- A=B;isequalgeneric(A,B).

% ###### Generic character for alignments

%% isequalgeneric(+Left:atom,+Right:atom)
%
% Defines if 2 chars are equal for special chars:  '_' (any).
%
% @param Left char to compare
% @param Right char to compare
%

isequalgeneric('-','-').
% Define a ANY charac that always match
isequalgeneric('_',_).
isequalgeneric(_,'_').



% alphabet definition and matching (lower/uppercase)

%% isequal(+Left:atom,+Right:atom)
%
% Defines if two char are equals. If one char is *'_'* , then comparison is always true.
%
% @param Left char to compare
% @param Right char to compare
%
% @see isequalnuc
% @see isequalgeneric
%

isequal(A,B):-isequalnuc(A,B);isequalgeneric(A,B).
% For protein analysis, previous predicate should be abolished, and replaced by the following:
% isequal(A,B):-A=B;isequalgeneric(A,B).

%isequal('a','a').
%isequal('b','b').
%isequal('c','c').
%isequal('d','d').
%isequal('e','e').
%isequal('f','f').
%isequal('g','g').
%isequal('h','h').
%isequal('i','i').
%isequal('j','j').
%isequal('k','k').
%isequal('l','l').
%isequal('m','m').
%isequal('n','n').
%isequal('o','o').
%isequal('p','p').
%isequal('q','q').
%isequal('r','r').
%isequal('s','s').
%isequal('t','t').
%isequal('u','u').
%isequal('v','v').
%isequal('w','w').
%isequal('x','x').
%isequal('y','y').
%isequal('z','z').
%isequal('A','A').
%isequal('B','B').
%isequal('C','C').
%isequal('D','D').
%isequal('E','E').
%isequal('F','F').
%isequal('G','G').
%isequal('H','H').
%isequal('I','I').
%isequal('J','J').
%isequal('K','K').
%isequal('L','L').
%isequal('M','M').
%isequal('N','N').
%isequal('O','O').
%isequal('P','P').
%isequal('Q','Q').
%isequal('R','R').
%isequal('S','S').
%isequal('T','T').
%isequal('U','U').
%isequal('V','V').
%isequal('W','W').
%isequal('X','X').
%isequal('Y','Y').
%isequal('Z','Z').

% Generic character for alignments
%isequal('-','-').

% Define a ANY charac that always match
%isequal('_',_).
%isequal(_,'_').


% End of general purpose


% Matrix definitions
%:-comment(matrix/3," Define a matrix@p Description: @begin{itemize} @item @bf{matrix(Name,Element,Matrix) , char-list * char-list * list => char-list * char-list * list }. @item @bf{matrix(Name,Element,Matrix) , char-list * char-list * var => char-list * char-list * list }. @end{itemize} @p Input: @begin{itemize} @item @var{Name} is the name of the matrix@item @var{Element} is the element alphabet of the matrix@item @var{Matrix} is the matrix content@end{itemize} @p Output: @var{Matrix}  the matrix content ").

%% matrix(+Name:char-list,+Char:atom,?Matrix:list)
%
% Define a matrix
%
% @param Name name of the matrix
% @param Char char mapping to a cost list
% @param Matrix cost matrix
%

matrix('test','a',[1,0,1,0,1]).
matrix('test','c',[0,1,0,1,0]).
matrix('test','g',[0,1,0,1,0]).
matrix('test','t',[1,1,1,1,1]).


% End of matrix



% substitution cost matrix

%% cost(+Name:char-list,+LeftChar:atom,+RightChar:atom,?Cost:int)
%
% Define a matrix
%
% @param Name name of the substitution cost
% @param LeftChar char to compare
% @param RightChar char to compare
% @param Cost cost of the substitution between *LeftChar* and *RightCar*
%

cost('substitution','a','a',0).
cost('substitution','_','a',1).
cost('substitution','b','b',0).
cost('substitution','_','b',1).
cost('substitution','c','c',0).
cost('substitution','_','c',1).
cost('substitution','d','d',0).
cost('substitution','_','d',1).
cost('substitution','e','e',0).
cost('substitution','_','e',1).
cost('substitution','f','f',0).
cost('substitution','_','f',1).
cost('substitution','g','g',0).
cost('substitution','_','g',1).
cost('substitution','h','h',0).
cost('substitution','_','h',1).
cost('substitution','i','i',0).
cost('substitution','_','i',1).
cost('substitution','j','j',0).
cost('substitution','_','j',1).
cost('substitution','k','k',0).
cost('substitution','_','k',1).
cost('substitution','l','l',0).
cost('substitution','_','l',1).
cost('substitution','m','m',0).
cost('substitution','_','m',1).
cost('substitution','n','n',0).
cost('substitution','_','n',1).
cost('substitution','o','o',0).
cost('substitution','_','o',1).
cost('substitution','p','p',0).
cost('substitution','_','p',1).
cost('substitution','q','q',0).
cost('substitution','_','q',1).
cost('substitution','r','r',0).
cost('substitution','_','r',1).
cost('substitution','s','s',0).
cost('substitution','_','s',1).
cost('substitution','t','t',0).
cost('substitution','_','t',1).
cost('substitution','u','u',0).
cost('substitution','_','u',1).
cost('substitution','v','v',0).
cost('substitution','_','v',1).
cost('substitution','w','w',0).
cost('substitution','_','w',1).
cost('substitution','x','x',0).
cost('substitution','_','x',1).
cost('substitution','y','y',0).
cost('substitution','_','y',1).
cost('substitution','z','z',0).
cost('substitution','_','z',1).
cost('substitution','A','A',0).
cost('substitution','_','A',1).
cost('substitution','B','B',0).
cost('substitution','_','B',1).
cost('substitution','C','C',0).
cost('substitution','_','C',1).
cost('substitution','D','D',0).
cost('substitution','_','D',1).
cost('substitution','E','E',0).
cost('substitution','_','E',1).
cost('substitution','F','F',0).
cost('substitution','_','F',1).
cost('substitution','G','G',0).
cost('substitution','_','G',1).
cost('substitution','H','H',0).
cost('substitution','_','H',1).
cost('substitution','I','I',0).
cost('substitution','_','I',1).
cost('substitution','J','J',0).
cost('substitution','_','J',1).
cost('substitution','K','K',0).
cost('substitution','_','K',1).
cost('substitution','L','L',0).
cost('substitution','_','L',1).
cost('substitution','M','M',0).
cost('substitution','_','M',1).
cost('substitution','N','N',0).
cost('substitution','_','N',1).
cost('substitution','O','O',0).
cost('substitution','_','O',1).
cost('substitution','P','P',0).
cost('substitution','_','P',1).
cost('substitution','Q','Q',0).
cost('substitution','_','Q',1).
cost('substitution','R','R',0).
cost('substitution','_','R',1).
cost('substitution','S','S',0).
cost('substitution','_','S',1).
cost('substitution','T','T',0).
cost('substitution','_','T',1).
cost('substitution','U','U',0).
cost('substitution','_','U',1).
cost('substitution','V','V',0).
cost('substitution','_','V',1).
cost('substitution','W','W',0).
cost('substitution','_','W',1).
cost('substitution','X','X',0).
cost('substitution','_','X',1).
cost('substitution','Y','Y',0).
cost('substitution','_','Y',1).
cost('substitution','Z','Z',0).
cost('substitution','_','Z',1).

% generic char for substitution
cost('substitution','-','-',0).
cost('substitution','_','-',1).

% End of cost matrix

% Return possible parents based on a cost matrix  and a maximum cost, only substitution are tested
% X | Y, child string
% Z parent string to find
% Maximum cost of substitution


%:-comment(parentalCost/4," Get all parents matching a maximum substition cost.@p To compare 2 sequences based on parents, @pred{parentalCost} should be called independently upon 2 words and search for parental equality such as parentalCost(M1,X1,Z1,Max1),parentCost(M1,X2,Z2,Max1),Z1==Z2.@p Description: @begin{itemize} @item @bf{parentalCost(Matrix,List,Z,Max) , char-list * list * var * int => char-list * list * list * int}. @end{itemize} @p Input: @begin{itemize} @item @var{Matrix} is the name of the cost matrix to use (see @pred{cost})@item @var{List} is the word sequence to analyse to get possible parents@item @var{Max} is the maximum cost of evolution@end{itemize} @p Output: @begin{itemize} @item @var{Z} are the parents matching the substitution cost@end{itemize} ").

parentalCost(Matrix, [X | Y],Z, Max) :- parentalCost(Matrix, [X | Y],[], Z,0, Max).
% sub predicates, do not call directly
parentalCost(Matrix, [], Parent, Z , Count, Max) :- Count=<Max, Z=Parent.
parentalCost(Matrix, [X | Y],Parent, Z,Count, Max) :- cost(Matrix, X1, X, CostValue),NewCount is Count+CostValue,append(Parent,[X1],NewParent),parentalCost(Matrix, Y ,NewParent, Z,NewCount, Max) .


% Variables
% Definition varDefinition('name',[content], startPosition, endPosition, length).
% Example varDefinition('X',[a,c,g,t],10,15,5).
% Set dynamically variable with assert
%:-comment(varDefinition/5," Define a variable.@p Description: @begin{itemize} @item @bf{varDefinition(VarName,VarContent,VarBegin,VarEnd,VarSize) , char-list * list * int * int * int => char-list * list * int * int * int }. @end{itemize} @p Input: @begin{itemize} @item @var{VarName} is the name of the variable@item @var{VarContent} is the content of the variable@item @var{VarBegin} is the start position of the variable@item @var{VarEnd} is the end position of the variable@item @var{VarSize} is the size of the variable@end{itemize} @p Output: @begin{itemize} @item @var{VarContent} is the content of the variable@item @var{VarBegin} is the start position of the variable@item @var{VarEnd} is the end position of the variable@item @var{VarSize} is the size of the variable@end{itemize} ").

%End of variables


%% computeCost(+Variables:list,-Cost:int)
%
% Get the whole cost of all input variables.
%
% @param Variables list of Variables cost
% @param Cost computed cost of all variables
%

computeCost([X|Y], Cost) :- computeCost([X|Y],0, Cost).

computeCost([],CurCost, FinalCost):- FinalCost=CurCost.
computeCost([X|Y],CurCost, FinalCost):- ((\+ var(X),Errors=X, TmpCost is CurCost + Errors);(var(X),TmpCost=CurCost)),computeCost(Y,TmpCost, FinalCost).


%TODO define getDistanceByRef

%% getDistance(+Variables:list,-Cost:int,+Type:int)
%
% Gets the minimum or maximum number of errors from a list of variables
%
% @param Variables list of Variables cost
% @param Cost computed cost of all variables
% @param Type minimum (0) or maximum(1) search
%

% main predicate
getDistance([X,Y],Distance,Type):- getDistance([X,Y],Distance,Type,-1).
% sub predicates
getDistance([],Distance,Type,Count) :- Distance=Count.

getDistance([X],Distance,Type,Count) :- getVariable(X,_,_,_,_,_,_,[Substitution,Indel],_),Errors is Substitution + Indel,write('Errors= ')
     ,(
      % search for minimum
      (Type=0, ((Count>(-1),Errors < Count, NewCount = Errors);( Count>(-1),Errors>=Count,NewCount=Count);(Count=(-1),NewCount=Errors))  )
      ;
      % search for maximum
      (Type=1, ((Errors >= Count, NewCount = Errors);(Errors<Count,NewCount=Count))  )
     )
     ,Distance=NewCount.

getDistance([X,Y],Distance,Type,Count) :- getVariable(X,_,_,_,_,_,_,[Substitution,Indel],_),Errors is Substitution + Indel,write('Errors= ')
     ,(
      % search for minimum
      (Type=0, ((Count>(-1),Errors < Count, NewCount = Errors);(Count>(-1),Errors>=Count,NewCount=Count);(Count=(-1),NewCount=Errors))  )
      ;
      % search for maximum
      (Type=1, ((Errors >= Count, NewCount = Errors);(Errors<Count,NewCount=Count))  )
     )
     ,getDistance([Y],Distance,Type,NewCount).

% Define max size to get content of a variable
%:-comment(maxsize/1," Define the maximum size for which content result will be written, else write content=... @p Description: @begin{itemize} @item @bf{maxsize(Size) , int  =>  int}. @end{itemize} @p Input: @begin{itemize} @item @var{Size} is the maximum size@end{itemize} @p Output: @begin{itemize} @p None@end{itemize} ").

%maxsize(X).

% Write result data based on a list of var reference
% used by getresult
getcontent(X,Data,NewData):- maxsize(Maxsize),getVariable(X,_,Begin,End,Size,Info,Level,Errors,Name),((Size>Maxsize,Content='...');(Size=<Maxsize,getVariable(X,TmpContent,_,_,_,_,_,_,_),atom_chars(Content,TmpContent))),append(Data,[[Name,Begin,End,Size,Info,Level,Errors,Content]],NewData).

%sub predicates, do not call directly
getresult([X],Data,Z):-getcontent(X,Data,NewData),Z=NewData.
getresult([X|Y],Data,Z):-getcontent(X,Data,NewData),getresult(Y,NewData,Z).


%% getresult(+VariableRefs:list,-Variables:list)
%
% Gets the result of the query based on a list a variables
%
% @param VariableRefs list of Variable references
% @param Variables list with all variables details
%

getresult([X|Y],Z):-getresult([X|Y],[],Z).


%% writeallresult(+File:char-list,+Match:list)
%
% Write the result of the query (match) based on a list a variables to a file in XMl format.
%
% Format of input list is [ [Ref1,Ref2],[Ref3,Ref4]], [Ref1,Ref2] will be put in a 'model' and each [Ref1,Ref2],[Ref3,Ref4] in a 'match'.
%
% @param File file name
% @param Match result match to write
%

%sub predicates, do not call
writecontent(X,OutStream):- maxsize(Maxsize),getVariable(X,_,Begin,End,Size,Info,Level,[Errors,Indel],Name),((Size>Maxsize,Content='-');(Size=0,Content='NULL');(Size=<Maxsize,Size>0,getVariable(X,TmpContent,_,_,_,_,_,_,_),atom_chars(Content,TmpContent)))
,format(OutStream,'<variable name="~w">~N~10|<data><begin>~w</begin>~N~10|<end>~w</end>~N~10|<size>~w</size>~N~10|<errors>~w</errors>~N~10|<distance>~w</distance>~N~10|<content>~w</content>~N~10|<text>~w</text>~N~10|</data>~N',[Name,Begin,End,Size,Errors,Indel,Content,Level]),(Info=[];(\+ Info=[],format(OutStream,'<subdata>~N',[]),writeresult(Info,OutStream),format(OutStream,'</subdata>~N',[]))),format(OutStream,'</variable>~N',[]).
writeresult([],OutStream):- format(OutStream,'',[]).
writeresult([X],OutStream):- writecontent(X,OutStream).
writeresult([X|Y],OutStream):- writecontent(X,OutStream),writeresult(Y,OutStream).
%main predicate
writeresult(Id,[X|Y],OutStream):- format(OutStream,'<model id="~w">~N',[Id]),writeresult([X|Y],OutStream),format(OutStream,'</model>~N',[]).

writeallresult(Id,[X],OutStream):- writeresult(Id,X,OutStream).
writeallresult(Id,[X|Y],OutStream):- writeresult(Id,X,OutStream),NewId is Id+1,writeallresult(NewId,Y,OutStream).
writeallresult(File,[X|Y]):- open(File,append,OutStream),bb_get('matchCounter',Count),format(OutStream,'<match id="~w">~N',[Count]),NewCount is Count + 1,bb_put('matchCounter',NewCount),writeallresult(1,[X|Y],OutStream),format(OutStream,'</match>~N',[]),close(OutStream),!.

%% initResultFile(+File:char-list)
%
% Write header for xml output file
%
% @param File file name
%

initResultFile(File):-open(File,write,OutStream),format(OutStream,'<?xml version="1.0" encoding="UTF-8"?><sequences>~N',[]),close(OutStream).


%% closeResultFile(+File:char-list)
%
% Write footer for xml output file
%
% @param File file name
%

closeResultFile(File):-open(File,append,OutStream),format(OutStream,'</sequences>~N',[]),close(OutStream).


%% myCost(+Program:char-list,+Word:list,+Max:int,-Errors:int)
%
% Execute an external shell to analyse a sequence and get a global cost and compare it to a maximum value
%
% Fix 1410
%
% @param Program external program name
% @param Word word to analyse
% @param Max max number of "errors" allowed
% @param Errors number of "errors" found by the program
%

%Fix 1410 zombie processes
myCost(Program,Word,Max,Errors):-atom_chars(WordTmp,Word),process_create(path(bash),[Program, WordTmp],[stdin(null),stdout(pipe(Out)),stderr(null),process(Proc)]),
%                 repeat,
%                 read_line(Out,T), ((T == end_of_file);(number_codes(Errors,T) )), % Read a term
%                 (T == end_of_file),     % Loop back if not at end of file
%                  !,
                  read_line(Out,T),process_wait(Proc,Exit),number_codes(Errors,T),close(Out),Errors=<Max.




% Add distinct cost and distance search

%% isexactwithdistinctgapanderror_pos(+InputPos:int,+Word:list,+MaxError:int,MaxDistance:int,-Errors:int,-DistanceErrors:int,-OutPos:int)
%
% Match if 2 sequences are equal with or without substitutions and gaps. Returns the number of substitutions and errors
%
% Fix 1336
%
% @param InputPos position in sequence
% @param Word word to match
% @param MaxError max number of substitutions allowed
% @param MaxDistance max number of edit errors allowed
% @param Errors number of substitutions found by the program
% @param DistanceErrors number of edit errors found by the program
% @param OutPos position after match
%

isexactwithdistinctgapanderror_pos(InputPos,[E1 | E2], MaxError, MaxDistance, Errors, DistanceErrors, OutPos):- length([E1|E2],TmpMaxChar),MaxChar is TmpMaxChar + MaxDistance,getCharsFromPosition(InputPos,MaxChar,InputList),!,isexactwithdistinctgapanderror(InputList,[E1 | E2], MaxError, MaxDistance, Errors, DistanceErrors, Z), length(Z,ZLength),OutPos is (InputPos + MaxChar - ZLength).


isexactwithdistinctgapanderror([]).
% Fix 1336: accept indel at the end of match
isexactwithdistinctgapanderror( [] , [] , N, NDist, MaxError, MaxDistance, Errors, DistanceErrors, Z) :- NDist=<MaxDistance,N=<MaxError, (Z=[],Errors=N, DistanceErrors=NDist).
isexactwithdistinctgapanderror( [X |Y ] , [] , N, NDist, MaxError, MaxDistance, Errors, DistanceErrors, Z) :- NDist=<MaxDistance,N=<MaxError, ((Z=[X|Y],Errors=N, DistanceErrors=NDist);(Ninc is NDist + 1,isexactwithdistinctgapanderror(Y,[],N, Ninc, MaxError, MaxDistance,Errors,DistanceErrors,Z))).
%isexactwithdistinctgapanderror([X|Y],[E1 | E2], N, NDist, MaxError, MaxDistance, Errors, DistanceErrors, Z) :-  N=<MaxError,NDist=<MaxDistance,((isequal(X,E1),!, isexactwithdistinctgapanderror(Y,E2,N, NDist, MaxError, MaxDistance,Errors,DistanceErrors,Z));(\+isequal(X,E1),Ninc is N+1,!,isexactwithdistinctgapanderror(Y,E2,Ninc, NDist,MaxError,MaxDistance,Errors,DistanceErrors,Z ));(\+isequal(X,E1),Ninc is NDist+1,!,isexactwithdistinctgapanderror(Y,[E1 | E2],N,Ninc, MaxError,MaxDistance,Errors,DistanceErrors,Z ));(\+isequal(X,E1),Ninc is NDist+1,!,isexactwithdistinctgapanderror( [X | Y], E2,N,Ninc, MaxError,MaxDistance,Errors,DistanceErrors,Z ))).
% Fix1336 change  from: isexactwithdistinctgapanderror([X|Y],[E1 | E2], N, NDist, MaxError, MaxDistance, Errors, DistanceErrors, Z) :-  N=<MaxError,NDist=<MaxDistance,((isequal(X,E1), isexactwithdistinctgapanderror(Y,E2,N, NDist, MaxError, MaxDistance,Errors,DistanceErrors,Z));(\+isequal(X,E1),Ninc is N+1,isexactwithdistinctgapanderror(Y,E2,Ninc, NDist,MaxError,MaxDistance,Errors,DistanceErrors,Z ));(\+isequal(X,E1),Ninc is NDist+1,isexactwithdistinctgapanderror(Y,[E1 | E2],N,Ninc, MaxError,MaxDistance,Errors,DistanceErrors,Z ));(\+isequal(X,E1),Ninc is NDist+1,isexactwithdistinctgapanderror( [X | Y], E2,N,Ninc, MaxError,MaxDistance,Errors,DistanceErrors,Z ))).
% to:
isexactwithdistinctgapanderror([X|Y],[E1 | E2], N, NDist, MaxError, MaxDistance, Errors, DistanceErrors, Z) :-  N=<MaxError,NDist=<MaxDistance,((length([E1|E2],ModelSize),PossibleErr is NDist + ModelSize, PossibleErr =<MaxDistance ,Z=[X|Y],Errors=N,DistanceErrors is PossibleErr);(isequal(X,E1), isexactwithdistinctgapanderror(Y,E2,N, NDist, MaxError, MaxDistance,Errors,DistanceErrors,Z));(\+isequal(X,E1),Ninc is N+1,isexactwithdistinctgapanderror(Y,E2,Ninc, NDist,MaxError,MaxDistance,Errors,DistanceErrors,Z ));(\+isequal(X,E1),Ninc is NDist+1,isexactwithdistinctgapanderror(Y,[E1 | E2],N,Ninc, MaxError,MaxDistance,Errors,DistanceErrors,Z ));(\+isequal(X,E1),Ninc is NDist+1,isexactwithdistinctgapanderror( [X | Y], E2,N,Ninc, MaxError,MaxDistance,Errors,DistanceErrors,Z ))).


% Main predicate
%:-comment(isexactwithdistinctgapanderror/7,"Match if 2 sequences are equal with or without substitutions and gaps. Returns the number of substitutions and distance errors@p Description: @begin{itemize} @item @bf{isexactwithdistinctgapanderror(L,Motif, MaxError, MaxDistance, Errors, DistanceErrors, Z) , list * list  * int * int * var * var * var => list * list  * int * int * int * int * list}. @end{itemize} @p Input: @begin{itemize} @item @var{L} is input sequence @item @var{Motif} is motif to compare to @item @var{MaxError} maximum number of errors for substitutions @item @var{MaxDistance} maximum number of errors for gaps @end{itemize} @p Output: @item @var{Errors}  number of substitutions errors found @item @var{DistanceErrors}  number of distance errors found @var{Z} rest of the list after the match ").

%% isexactwithdistinctgapanderror(+InputList:list,+Word:list,+MaxError:int,MaxDistance:int,-Errors:int,-DistanceErrors:int,-OutList:list)
%
% Match if 2 sequences are equal with or without substitutions and gaps. Returns the number of substitutions and errors
%
% Fix 1336
%
% @param InputList input list
% @param Word word to match
% @param MaxError max number of substitutions allowed
% @param MaxDistance max number of edit errors allowed
% @param Errors number of substitutions found by the program
% @param DistanceErrors number of edit errors found by the program
% @param OutList list after match
%

isexactwithdistinctgapanderror([X|Y],[E1 | E2], MaxError, MaxDistance, Errors, DistanceErrors, Z) :- isexactwithdistinctgapanderror([X|Y],[E1 | E2], 0, 0, MaxError, MaxDistance, Errors, DistanceErrors, Z).


%% logData(+Filename:char-list,+Message:char-list)
%
% Log a message to a file
%
% @param Filename log file name
% @param Message message to log
%

logData(File,Message):-open(File,append,OutStream),format(OutStream,'~w~N',Message),close(OutStream).


%:-comment(setParent/2," Set parent value in variable structure@p Description: @p @bf{DEPRECATED}@p Set the parent value in structure @begin{itemize} @item @bf{setParent(Var, Parent) , list * char-list => list * char-list}. @end{itemize} @p Input: @begin{itemize} @item @var{Var} is variable structure saved in @pred{varDefinition} @item @var{Parent} is the parent string@end{itemize} @p Output: ").

setParent(Var, Parent) :- Var=[_,_,_,_,_,_,_,_,Parent,_].

%:-comment(getParent/2," Get parent value from variable structure@p Description: @p Get the parent value in structure@begin{itemize} @item @bf{getParent(Var, Parent) , list * var => list * char-list}. @end{itemize} @p Input: @begin{itemize} @item @var{Var} is variable structure saved in @pred{varDefinition} @end{itemize} @p Output: @item @var{Parent} is the parent string").

getParent(Var, Parent) :- Var=[_,_,_,_,_,_,_,_,Parent,_].


%:-comment(mapList/2," Map a variable, as list, to a shorter list containing only a set of sub-data@p Description: @p Take a variable as input and transform it in a new list containing begin,end,cost,distance information while keeping same structure@begin{itemize} @item @bf{mapList(Var, ConvertedVar) , list * var => list * list}. @end{itemize} @p Input: @begin{itemize} @item @var{Var} is variable structure saved in @pred{varDefinition} @end{itemize} @p Output: @item @var{ConvertedVar} is the new list").

%% mapList(+In:list,+Out:list)
%
% Map a variable, as list, to a shorter list containing only a set of sub-data.
% Take a variable as input and transform it in a new list containing begin,end,cost,distance information while keeping same structure
%
% @param In input variable
% @param Out output variable
%
% @see mapListExact/2
%

% Map from a match to keep only db key data e.g. begin, end, Errors and Info (itself converted)
%mapList([],Conv):-Conv=[].
%mapList([X|Y],[XConv|YConv]):-X=[Name,Content,Begin,End,Size,Info,Level,Errors,_],mapList(Info,InfoConv),XConv=[Begin,End,Errors,InfoConv],mapList(Y,YConv).
mapList(In,Out):-mapListExact('mapListExact',In,Out).

%:-comment(mapModels/2," Convert a list of models using @pred{mapList} for each model@p Description: @p Convert a list of models using @pred{mapList} for each model @begin{itemize} @item @bf{mapModels(Models, ConvertedModels) , list * var => list * list}. @end{itemize} @p Input: @begin{itemize} @item @var{Models} is list of models according to the grammar rule@end{itemize} @p Output: @item @var{ConvertedModels} is the new list of models").

%% mapModels(+In:list,+Out:list)
%
% Convert a list of models using mapList/2 for each model
%
% @param In input models
% @param Out output models
%

% Map a listof models
mapModels([],Conv):-Conv=[].
mapModels([X|Y],[XConv|YConv]):- mapList(X,XConv),mapModels(Y,YConv).


%% matchExist(+Match:list)
%
% Check if a match is already found, e.g. a list of model matches is present with matchlist/2
%
% @param Match match to check
%

matchExist([X|Y]):- mapModels([X|Y],[XConv|YConv]),\+matchlist(XConv,YConv),assert(matchlist(XConv,YConv)).



% Get last element of a list
lastList([Elem], Elem).
lastList([_|Tail], Elem) :- lastList(Tail, Elem).


% Fix 1602
% Map a listof models

%% matchExist(+FilterPred:goal,+Match:list)
%
% Check if a match is already found using a dedicated filter.
%
% Fix 1602
%
% @param FilterPred Name of the filter to use, must be a mapListXXX filter.
% @param Match list of model matches
%

matchExist(FilterPredicate,[X|Y]):- mapModels(FilterPredicate,[X|Y],[XConv|YConv]),\+matchlist(XConv,YConv),assert(matchlist(XConv,YConv)).

%% mapModels(+FilterPred:goal,+Match:list,-FilteredMatch:list)
%
% Convert a list of models using a mapList/2 filter predicate for each model.
%
% @param FilterPred Name of the filter to use, must be a mapListXXX filter.
% @param Match list of model matches
% @param FilteredMatch match filtered
%


mapModels(FilterPredicate,[],Conv):-Conv=[].
mapModels(FilterPredicate,[X|Y],[XConv|YConv]):-  append([FilterPredicate],[X,XConv],PredicateSkeleton),PredicateToCall=..PredicateSkeleton,PredicateToCall,mapModels(FilterPredicate,Y,YConv).
% Map from a match to keep only db key data e.g. begin, end, Errors and Info (itself converted)

%% mapListExact(+Match:list,-FilteredMatch:list)
%
% Map a variable, as list, to a shorter list containing only a set of sub-data@p Description:
%
% Take a variable as input and transform it in a new list containing begin,end,cost,distance information while keeping same structure.
%
% @param Match list of model matches
% @param FilteredMatch match filtered
%

mapListExact([],Conv):-Conv=[].
mapListExact([X|Y],[XConv|YConv]):-X=[Name,Content,Begin,End,Size,Info,Level,Errors,_,Config],mapListExact(Info,InfoConv),XConv=[Begin,End,Errors,InfoConv],mapListExact(Y,YConv).
%Check global start and end only

%% mapListGlobal(+Match:list,-FilteredMatch:list)
%
% Map a variable, as list, to a shorter list containing only a set of sub-data@p Description:
%
% Take a variable as input and transform it in a new list containing begin,end of global match, it does not check whole structure.
%
% @param Match list of model matches
% @param FilteredMatch match filtered
%

mapListGlobal([],Conv):-Conv=[].
mapListGlobal([X],[Begin|End]):- X=[_,_,Begin,End|_].
mapListGlobal([X|Y],[XConv|YConv]):- X=[_,_,XConv|_],lastList(Y,TmpOut),TmpOut=[_,_,_,YConv|_].

%Convert to keep position and structure

%% mapListLocal(+Match:list,-FilteredMatch:list)
%
% Map a variable, as list, to a shorter list containing only a set of sub-data@p Description:
%
% Take a variable as input and transform it in a new list containing begin,end while keeping same structure.
%
% @param Match list of model matches
% @param FilteredMatch match filtered
%

mapListLocal([],Conv):-Conv=[].
mapListLocal([X|Y],[XConv|YConv]):-X=[Name,Content,Begin,End,Size,Info,Level,Errors,_,Config],mapListLocal(Info,InfoConv),XConv=[Begin,End,InfoConv],mapListLocal(Y,YConv).
%Convert to keep position on first level of structure, not in subdata
%:-comment(mapListLocal0/2," Map a variable, as list, to a shorter list containing only a set of sub-data@p Description: @p Take a variable as input and transform it in a new list containing begin,end,cost,distance information but only on first level, not whole structure@begin{itemize} @item @bf{mapList(Var, ConvertedVar) , list * var => list * list}. @end{itemize} @p Input: @begin{itemize} @item @var{Var} is variable structure saved in @pred{varDefinition} @end{itemize} @p Output: @item @var{ConvertedVar} is the new list").

%% mapListLocal0(+Match:list,-FilteredMatch:list)
%
% Map a variable, as list, to a shorter list containing only a set of sub-data@p Description:
%
% Take a variable as input and transform it in a new list containing begin,end,cost,distance information but only on first level, not whole structure.
%
% @param Match list of model matches
% @param FilteredMatch match filtered
%

mapListLocal0([],Conv):-Conv=[].
mapListLocal0([X|Y],[XConv|YConv]):-X=[Name,Content,Begin,End,Size,Info,Level,Errors,_,Config],XConv=[Begin,End],mapListLocal0(Y,YConv).

% ** NEGATIVE CONSTRAINTS *******************************
% FIX 1394

% (1) Checks 2 words are different
% sub predicates, do not call directly
notexact([]).
%notexact( [] , [] , Z) :- fail.
notexact( X , [] , Z) :- \+X=[],Z=X.
notexact( [] , E , Z) :- \+E=[],Z=[].
% Main predicate
%:-comment(notexact/3," Match if 2 sequences are different.@p Description: @begin{itemize} @item @bf{notexact(L,Motif,Z) , list * list * var > list * list * list}. @end{itemize} @p Input: @begin{itemize} @item @var{L} is input sequence @item @var{Motif} sequence to match @end{itemize} @p Output: @var{Z} rest of the list after the match ").

%% notexact(+Input:list,+Motif:list,-Output:list)
%
% Match if 2 sequences are different
%
% @param Input input sequence
% @param Motif to check against
% @param Output ouput list atfer the "match"
%

notexact( [ X | Y], [E1 | E2] , Z) :- \+X=[],\+E1=[],(\+isequal(X,E1),Z=[X|Y]) ; (isequal(X,E1),notexact(Y, E2, Z)).

%% notexact_pos(+InputPos:int,+Motif:list,+MinSize:int,+MaxSize:int,-Errors:int,-OutputPos:list)
%
% Find different matches within size range. If Max is equal to zero, take assumption to use *Motif* size
%
% Fix 1397
%
% @param InputPos input sequence position
% @param Motif to check against
% @param MinSize min size of the match
% @param MaxSize max size of the match
% @param Errors always 0, for compatibility with other preds.
% @param OutputPos position after match
%


notexact_pos( InputPos, Motif , MinSize, MaxSize, Errors, OutPos) :- MinSize=<MaxSize,((MaxSize=0,length(Motif,Max));(MaxSize>0,Max=MaxSize)),((MinSize=0,length(Motif,Min));(MinSize>0,Min=MinSize)),getCharsFromPosition(InputPos,Min,InputList),!,((notexact( InputList, Motif, Z),Errors=0,OutPos is (InputPos + Min));(NewMinSize is Min + 1,notexact_pos( InputPos, Motif , NewMinSize, MaxSize, Errors, OutPos) )).


%% between(+Min:int,+Max:int,-Value:int)
%
% Retuns a range of values. Gets possible values between *Min* and *Max*.
%
% @param Min min value
% @param Max max value
% @param Value values between min and max (including)
%

between(Min,Max,Value):-between(Min,Min,Max,Value).
between(Min,Cur,Max,Value):-Cur<Max,(Value=Cur;Next is Cur + 1,between(Min,Next,Max,Value)).


%% notpred_pos(+Id:int,+InputPos:int,+Pred:goal,+MinSize:int,+MaxSize:int,-OutputPos:list)
%
% Returns all positions where predicate does not match. Warning, predicate must return matches above *Pos*
%
% @param Id id for the search, unique
% @param InputPos input sequence position
% @param Pred predicate to use
% @param MinSize min size of the match
% @param MaxSize max size of the match
% @param OutputPos position after match
%


:-dynamic hasmatch/2.

%notpred_pos(VarId,StartPos,Pred,Min,Max,Value):-random(Id),retractall(hasmatch(Id,_)),Pred=..VarDef,length(VarDef,Predlength),OutPos is Predlength - 1,PosMin is StartPos + Min, PosMax is StartPos + Max,!,between(StartPos,PosMax,Value),(Pred -> (nth0(OutPos,VarDef,Pos),(\+hasmatch(Id,Pos) -> (write('register'),write(Pos),nl,assert(hasmatch(Id,Pos)));1=1),\+hasmatch(Id,Value),assert(hasmatch(Id,Value)));1=1),write(Pred2),nl,Value>=PosMin.
% Fix 1406
% Call predicate. If match, return last argument e.g. position, else return 0.
callpred(Pred,OutPos,Pos):- (Pred ,(Pred=..VarDef,nth0(OutPos,VarDef,Pos)));(Pos=0).
notpred_pos(VarId,StartPos,Pred,Min,Max,Value):-random(Id),retractall(hasmatch(Id,_)),Pred=..VarDef,length(VarDef,Predlength),OutPos is Predlength - 1,PosMin is StartPos + Min, PosMax is StartPos + Max,!,between(StartPos,PosMax,Value),(callpred(Pred,OutPos,Pos) , ((\+hasmatch(Id,Pos) -> (assert(hasmatch(Id,Pos)));1=1),\+hasmatch(Id,Value),assert(hasmatch(Id,Value)))),Value>=PosMin.


% Test predicate to use with predicate requiring a position relative predicate as parameter.
testPos(X,Y,Z):-X=1,Y=1,(Z=5;Z=10;Z=20).

% Test predicate for external calls
% It represents the interface to implement for such predicates
% predname( [start position => int , input parameters] => list , [output parameters, Spacer output => int, Errors output => int, Substitutions output => out, Out position => int] => list)

%% externalinterface(+Inputs:list,-Outputs:list)
%
% Interface to implement when using external call in grammar.
% It represents the interface to implement for such predicates
% predname( [start position => int , input parameters] => list , [output parameters, Spacer output => int, Errors output => int, Substitutions output => out, Out position => int] => list)
%
% Example:
%
% ==
%  test([StartPos,X,Y],[Z,Spacer,Errors,Indel,EndPos]):- EndPos is StartPos + 2,Spacer=0,Errors=0,Indel=0.
% ==
%
% Fix 1578
%
% @param Inputs list of inputs. First element must be start position, given automatically by the program, next elements can be variable names or integers.
% @param Outputs variables required to set variable data in following order Spacer,Errors,Indel,EndPos
%

test([StartPos,X,Y],[Z,Spacer,Errors,Indel,EndPos]):- EndPos is StartPos + 2,Spacer=0,Errors=0,Indel=0.

%% externalinterfacewithspacer(+Inputs:list,-Outputs:list)
%
% Interface to implement when using external call in grammar and in front of a spacer element.
%
% Example:
%
% ==
%  test_withspacer([StartPos,MinSpacer,MaxSpacer,X,Y],[Z,Spacer,Errors,Indel,EndPos]):- EndPos is StartPos + 5,Spacer=2,Errors=0,Indel=0.
% ==
%
% Fix 1578
%
% @param Inputs list of inputs. First element must be start position, then minimum spacer and maximum spacer values, given automatically by the program, next elements can be variable names or integers.
% @param Outputs variables required to set variable data in following order Spacer,Errors,Indel,EndPos
%

test_withspacer([StartPos,MinSpacer,MaxSpacer,X,Y],[Z,Spacer,Errors,Indel,EndPos]):- EndPos is StartPos + 5,Spacer=2,Errors=0,Indel=0.

%Fix 1683 Add a percentage check on Alphabet
% checkAlphabetPercentage("+Constants.LOGOLVARREF+varID+","+constr.alphabetConstraint+","+constr.min+")"

%% checkAlphabetPercentage(+Variable:char-list,+Alphabet,-Percent:int)
%
% Interface to check percentage of a defined alphabet.
%
% Fix 1683
%
% @param Variable Input variable reference used to retreive content
% @param Alphabet list of chars defining the alphabet
% @param Percent Minimal percentage found
%

checkAlphabetPercentage(VAR,ALPHABET,PERCENT):- getVariable(VAR,CONTENT,_,_,SIZE,_,_,_,_),countAlphabet(CONTENT,ALPHABET,COUNT,0), ALPHABETPERCENT  is (COUNT/SIZE*100),ALPHABETPERCENT>=PERCENT.
%sub-predicates
countAlphabet([],ALPHABET,COUNT,TMPCOUNT):-COUNT=TMPCOUNT.
countAlphabet([C1|C2],ALPHABET,COUNT,TMPCOUNT):-member(C1,ALPHABET)->(NEWCOUNT is TMPCOUNT + 1,countAlphabet(C2,ALPHABET,COUNT,NEWCOUNT));(countAlphabet(C2,ALPHABET,COUNT,TMPCOUNT)).

% ************ OPTIMAL FUNCTIONS ********************
% Prints matches to file according to matchstore
%:-comment(writeFinalMatches/0,"If @bf{config} has OPTIMAL set, parse stored matches in @bf{matchstore} and write results to result file.").

%% writeFinalMatches
%
% If config/1 has OPTIMAL set, parse stored matches in matchstore/2 and write results to result file.
%

writeFinalMatches:- (config(Config),nth0(1,Config,USEOPTIMAL),(USEOPTIMAL=1->(outputfile(ResFile),resetCounter,matchstore(_,Z),writeallresult(ResFile,Z));(1=1))).


% Should build [X|Y] from matches, each is [varname,pos,len]

%****** TO UPDATE *********
%isMax(MATCH,[X|Y],[A|B],NEWID):- isVarMax(X,[A|B],NEWID,STATUS),( STATUS=1-> (\+matchstore(NEWID,_)->assert(matchstore(NEWID,MATCH));1=1);(1=1)), isMax(MATCH,Y,[A|B],NEWID).
%isVarMax(X,[Y|Z],NEWID,STATUS):-X=[VAR,POS,LEN],\+optimalmatch(_,_,_,NEWID,VAR),optimalmatch(VAR,POS,CURLEN,ID,VAR)->(
%(\+optimalmatch(_,_,_,NEWID,VAR),LEN>CURLEN,X=[OPTVAR,_,_],replaceMatch(OPTVAR,[Y|Z],ID,NEWID),STATUS=1)
%;
%(\+optimalmatch(_,_,_,NEWID,VAR),LEN=CURLEN,X=[OPTVAR,_,_],(isOptimalVarEqual(OPTVAR,[Y|Z],ID)->(appendMatch(OPTVAR,[Y|Z],NEWID));isMin(OPTVAR,[Y|Z],[Y|Z],ID,NEWID)->(1=1);(1=1)),STATUS=1)
%);(
%X=[OPTVAR,_,_],\+optimalmatch(_,_,_,NEWID,OPTVAR),appendMatch(OPTVAR,[Y|Z],NEWID),STATUS=1
%);STATUS=0.



% ***********************

%% isMax(+Match:list,+OptVars:list,-NewId:int)
%
% Check if an optimal variable is maximal.
%
% @param Variable Input variable reference used to retreive content
% @param OptVars  Optimal variable list to check
% @param NewId  Id of the new optimal match
%

isMax(MATCH,[X|Y],NEWID):-isMax(MATCH,[X|Y],[X|Y],NEWID).
%sub predicates
isMax(MATCH,[],[A|B],NEWID):-1=1.

isMax(MATCH,[X|Y],[A|B],NEWID):- isVarMax(X,[A|B],NEWID,STATUS),( STATUS=1-> (\+matchstore(NEWID,_)->assert(matchstore(NEWID,MATCH));1=1);(1=1)), isMax(MATCH,Y,[A|B],NEWID).

%replaceMatchForce : if superior, replace for all variables
%replaceMatch: replace only for current variable
% Test: 1,1  ; 1,8 ; 2,1  ; 2,8 should keep 2,1 (max v1) and 2,8 (max v2)
isVarMax(X,[Y|Z],NEWID,STATUS):-X=[VAR,POS,LEN],\+optimalmatch(_,_,_,NEWID,VAR),optimalmatch(VAR,POS,CURLEN,ID,VAR)->(
	(\+optimalmatch(_,_,_,NEWID,VAR),LEN>CURLEN,X=[OPTVAR,_,_],replaceMatch(OPTVAR,[Y|Z],ID,NEWID),STATUS=1)
	;
    (\+optimalmatch(_,_,_,NEWID,VAR),LEN=CURLEN,X=[OPTVAR,_,_],(isOptimalVarEqual(OPTVAR,[Y|Z],ID)->(appendMatch(OPTVAR,[Y|Z],NEWID),STATUS=1);isMin(OPTVAR,[Y|Z],[Y|Z],ID,NEWID)->(1=1,STATUS=1);(1=1,STATUS=0)))
	);(
	X=[OPTVAR,_,_],\+optimalmatch(_,_,_,NEWID,OPTVAR),appendMatch(OPTVAR,[Y|Z],NEWID),STATUS=1
	);STATUS=0.



%isMax(MATCH,[X|Y],[A|B],NEWID):- isVarMax(X,[A|B],NEWID)-> (\+matchstore(NEWID,_)->assert(matchstore(NEWID,MATCH));1=1) ; isMax(MATCH,Y,[A|B],NEWID).
%isVarMax(X,[Y|Z],NEWID):-X=[VAR,POS,LEN],\+optimalmatch(_,_,_,NEWID,VAR),optimalmatch(VAR,POS,CURLEN,ID,VAR)->(
%										(\+optimalmatch(_,_,_,NEWID,VAR),LEN>CURLEN,X=[OPTVAR,_,_],replaceMatch(OPTVAR,[Y|Z],ID,NEWID))
%										;
%										(\+optimalmatch(_,_,_,NEWID,VAR),LEN=CURLEN,X=[OPTVAR,_,_],(isOptimalVarEqual(OPTVAR,[Y|Z],ID)->appendMatch(OPTVAR,[Y|Z],NEWID);isMin(OPTVAR,[Y|Z],[Y|Z],ID,NEWID)))
%								        );(
%								        X=[OPTVAR,_,_],\+optimalmatch(_,_,_,NEWID,OPTVAR),appendMatch(OPTVAR,[Y|Z],NEWID)
%								        ).


% Look is all optimal variables are the same. If yes, it is a different solution for same optimal variables
isOptimalVarEqual(OPTVAR,[],ID):-1=1.
isOptimalVarEqual(OPTVAR,[X|Y],ID):-X=[VAR,POS,LEN],optimalmatch(VAR,POS,CURLEN,ID,OPTVAR),LEN=CURLEN,isOptimalVarEqual(OPTVAR,Y,ID).


appendMatch(OPTVAR,[],ID):-1=1.
%appendMatch(OPTVAR,X,ID):-X=[VAR,POS,LEN],appendMatch(OPTVAR,VAR,POS,LEN,ID).

%% appendMatch(+Variable:char-list,+OptVar:list,-NewId:int)
%
% Check if an optimal variable is maximal.
%
% @param Variable Optimal variable name
% @param OptVars   Optimal variable list to store
% @param NewId  Id of the new optimal match
%

appendMatch(OPTVAR,[Y|Z],ID):-Y=[VAR,POS,LEN],appendMatch(OPTVAR,VAR,POS,LEN,ID),appendMatch(OPTVAR,Z,ID).
%sub predicate
appendMatch(OPTVAR,VAR,POS,LEN,ID):- assert(optimalmatch(VAR,POS,LEN,ID,OPTVAR)).

replaceMatch(OPTVAR,[],ID,NEWID):-1=1.
%replaceMatch(OPTVAR,X,ID,NEWID):-X=[VAR,POS,LEN],replaceMatch(OPTVAR,VAR,POS,LEN,ID,NEWID).

%% replaceMatch(+Variable:char-list,+OptVar:list,Id:int,-NewId:int)
%
% Replace a list of variables to optimalmatch/2 for the maximal input variable. Same as appendMatch/3 but remove id it replaces first.
%
% Fix 1802
%
% @param Variable Optimal variable name
% @param OptVars   Optimal variable list to store
% @param Id Id of the old match
% @param NewId  Id of the new optimal match
%

replaceMatch(OPTVAR,[Y|Z],ID,NEWID):-Y=[VAR,POS,LEN],replaceMatch(OPTVAR,VAR,POS,LEN,ID,NEWID),replaceMatch(OPTVAR,Z,ID,NEWID).
%sub predicate
% Replace an optimal by an other. IF no more optimal remains for an id, remove id from matchstore.
%replaceMatch(OPTVAR,VAR,POS,LEN,ID,NEWID):-retractall(optimalmatch(_,POS,_,_,OPTVAR)),assert(optimalmatch(VAR,POS,LEN,NEWID,OPTVAR)),((\+optimalmatch(_,_,_,ID,_),matchstore(ID,_))->retract(matchstore(ID,_));1=1).
% Get ids of optimals matching position for optvar, retract previous optimals, append new one and remove from match store the old matches
replaceMatch(OPTVAR,VAR,POS,LEN,ID,NEWID):-bagof(OLDID,optimalmatch(_,POS,_,OLDID,OPTVAR),IDLIST),retractall(optimalmatch(_,POS,_,_,OPTVAR)),assert(optimalmatch(VAR,POS,LEN,NEWID,OPTVAR)),replaceMatchStore(IDLIST,OPTVAR).
% Fix 1802
% Remove a match from store, only if it is not anymore an optimal
replaceMatchStore([],OPTVAR):-1=1.
replaceMatchStore([X|Y],OPTVAR):-((\+optimalmatch(_,_,_,X,OPTVAR),matchstore(X,_))->(retract(matchstore(X,_)));1=1),replaceMatchStore(Y,OPTVAR).



% Same as replaceMatch but force the replacement whatever are remaining assertions
replaceMatchForce(OPTVAR,[],ID,NEWID):-1=1.
replaceMatchForce(OPTVAR,[Y|Z],ID,NEWID):-Y=[VAR,POS,LEN],replaceMatchForce(OPTVAR,VAR,POS,LEN,ID,NEWID),replaceMatchForce(OPTVAR,Z,ID,NEWID).
replaceMatchForce(OPTVAR,VAR,POS,LEN,ID,NEWID):-retractall(optimalmatch(_,_,_,ID,_)),assert(optimalmatch(VAR,POS,LEN,NEWID,OPTVAR)),((\+optimalmatch(_,_,_,ID,_),matchstore(ID,_))->(retract(matchstore(ID,_)),write('retract store '),write(ID),nl);1=1).



% Id ID already present, stop
% OPTVAR : current variable we try to optimize
% [A|B]: list of variables to minimize (including current one, which of course will fail)
% [Y|Z]: list of all variales
% ID: id of the match found
% NEWID: id of the new match

%% isMin(+Variable:char-list,+OptVar:list,+AllVars:list,Id:int,-NewId:int)
%
% Check if, for an optimal variable, if other variables are minimal
%
% @param Variable Optimal variable name
% @param OptVars Optimal variable list to check
% @param AllVars   Optimal variable list to store
% @param Id Id of the old match
% @param NewId  Id of the new optimal match
%

isMin(OPTVAR,[A|B],[Y|Z],ID,NEWID):- A=[VAR,POS,LEN],((optimalmatch(VAR,POS,CURLEN,ID,OPTVAR)->(
										\+optimalmatch(_,_,_,NEWID,OPTVAR),LEN<CURLEN,replaceMatch(OPTVAR,[Y|Z],ID,NEWID)
									);(
										\+optimalmatch(_,_,_,NEWID,OPTVAR),appendMatch(OPTVAR,[Y|Z],NEWID)
									))
									;
									(\+optimalmatch(_,_,_,NEWID,OPTVAR),isMin(OPTVAR,B,[Y|Z],ID,NEWID))
									).


% Parse result to get optimized variables
% Array of models
parseResults([X|Y],OPTDATA):-parseResults([X|Y],[],OPTDATA).

parseResults([],TMPDATA,OPTDATA):-reverse(TMPDATA,OPTDATA).
parseResults([X|Y],TMPDATA,OPTDATA):- parseResult(X,[],DATA),append(TMPDATA,DATA,NEWDATA), parseResults(Y,NEWDATA,OPTDATA).

%parse sub data too
parseResult([],TMPDATA,DATA):-DATA=TMPDATA.
parseResult([X|Y],TMPDATA,DATA):- (parseVariable(X,LOCALDATA) -> append(LOCALDATA,TMPDATA,NEWTMPDATA) ; NEWTMPDATA = TMPDATA ),parseResult(Y,NEWTMPDATA,DATA).

% Parse a variable and its subvariable to find optimized var in config
% X=[Name,Content,Begin,_,Size,Info,_,_,_,Config]
% Fix 1805
parseVariable([],DATA):-fail.
parseVariable(X,DATA):- getVariable(X,_,Begin,_,Size,Info,_,_,Name,Config),nth0(1,Config,IsOpt),((IsOpt=1->VARDATA=[Name,Begin,Size];VARDATA=[]),(parseResult(Info,[],TMPDATA),(VARDATA=[]->DATA=TMPDATA;DATA=[VARDATA|TMPDATA]))).



%% getCounter(-NewCount:int)
%
% Get a new count identifier (e.g. increment counter and return value)
%
% @param NewCount counter incremented by 1
%
% @see matchCounter/1
%


getCounter(NewCount):-bb_get('matchCounter',Count),NewCount is Count + 1,bb_put('matchCounter',NewCount).
resetCounter:-bb_put('matchCounter',1).


%% getControlVal(+Match:list,-Control:int,+Operator:int,+Options:list,+Vars:list)
%
% Get a new count identifier (e.g. increment counter and return value)
%
% @param Match input match
% @param Control value found according to operator on variable
% @param Operator control operator, begin=1,end=2,size=3,cost=4,distance=5,%cost=6,%distance=7,%alphabet=8.
% @param Options used for alphabet (alphabet list)
% @param Vars List of variables to use
%

getControlVal(MATCH,CONTROL,OPERATOR,OPTIONS,VARS):-
													%TODO manage percentages, get values then calculate percent

													% Begin constraint operator
													(OPERATOR=1,1=1);
													% End constraint operator
													(OPERATOR=2,1=1);
													% Size constraint operator
													(OPERATOR=3,getLengthVal(MATCH,CONTROL,VARS));
													% Cost constraint operator
													(OPERATOR=4,getCostVal(MATCH,CONTROL,VARS));
													% Distance constraint operator
													(OPERATOR=5,getDistVal(MATCH,CONTROL,VARS));
													% Percent Cost constraint operator
													(OPERATOR=6,getCostVal(MATCH,COUNT,VARS),getLengthVal(MATCH,LENGTH,VARS), CONTROL is (COUNT/LENGTH*100));
													% Percent Distance constraint operator
													(OPERATOR=7,getDistVal(MATCH,COUNT,VARS),getLengthVal(MATCH,LENGTH,VARS), CONTROL is (COUNT/LENGTH*100));
													% Alphabet percent constraint operator
													(OPERATOR=8,getAlphabetVal(MATCH,COUNT,OPTIONS,VARS),getLengthVal(MATCH,LENGTH,VARS), CONTROL is (COUNT/LENGTH*100))
													.
%getVariable(R,Content,Begin,End,Size,Info,Level,[Cost,Dist],Name,Config)
getLengthVal(MATCH,CONTROL,[]):-CONTROL=0.
getLengthVal(MATCH,CONTROL,[VAR|VARS]):- extractVariableFromMatch(MATCH,VAR,VARFOUND),getVariable(VARFOUND,_,_,_,Length,_,_,_,_,_),(getLengthVal(MATCH,OthersLength,VARS)->(CONTROL is Length + OthersLength);(CONTROL = Length)).

getCostVal(MATCH,CONTROL,[]):-CONTROL=0.
getCostVal(MATCH,CONTROL,[VAR|VARS]):- extractVariableFromMatch(MATCH,VAR,VARFOUND),getVariable(VARFOUND,_,_,_,_,_,_,[Cost,_],_,_),(getCostVal(MATCH,OthersCost,VARS)->(CONTROL is Cost + OthersCost);(CONTROL = Cost)).

getDistVal(MATCH,CONTROL,[]):-CONTROL=0.
getDistVal(MATCH,CONTROL,[VAR|VARS]):- extractVariableFromMatch(MATCH,VAR,VARFOUND),getVariable(VARFOUND,_,_,_,_,_,_,[_,Cost],_,_),(getDistVal(MATCH,OthersCost,VARS)->(CONTROL is Cost + OthersCost);(CONTROL = Cost)).

getAlphabetVal(MATCH,CONTROL,ALPHABET,[]):-CONTROL=0.
getAlphabetVal(MATCH,CONTROL,ALPHABET,[VAR|VARS]):-extractVariableFromMatch(MATCH,VAR,VARFOUND),getVariable(VARFOUND,CONTENT,_,_,_,_,_,_,_,_),countAlphabet(CONTENT,ALPHABET,Count,0),(getAlphabetVal(MATCH,OthersCount,ALPHABET,VARS)->(CONTROL is Count + OthersCount);(CONTROL = Count)).



extractVariableFromMatch([],VAR,VARFOUND):-fail.
extractVariableFromMatch([MODEL|MODELS],VAR,VARFOUND):- (extractVariableFromModel(MODEL,VAR,VARFOUND))->(1=1);extractVariableFromMatch(MODELS,VAR,VARFOUND).

extractVariableFromModel([],VAR,VARFOUND):-fail.
extractVariableFromModel([X|Y],VAR,VARFOUND):- getVariable(X,_,_,_,_,Info,_,_,Name,_), ((Name=VAR)->(VARFOUND=X);(extractVariableFromModel(Info,VAR,VARFOUND)->(1=1);(extractVariableFromModel(Y,VAR,VARFOUND)))).


% *******************************************************

% TODO implement when dbd can be linked statically with program
% openMyDb(DBPATH):- db_open(DBPATH,update,[matchlist(+,-)],DBREF),assert(mydb(DBREF)).
% closeMyDb():-mydb(DBREF),close(DBREF).
% matchExistDB([X|Y]):-mydb(DBREF),\+db_fetch(DBREF,matchlist(X,Y),_),db_store(DBREF,matchlist(X,Y),_).
