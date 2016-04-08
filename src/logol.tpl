:- ensure_loaded('stringAnalysis.pro').

% comment

%**********************************
% Execution test goals
% and definitions
%
%***********************************

% Template: define constants (morphism...)

% Template: define query

%%
% Executable creation
% Windows, with Visual Studio (need cl.exe)
% sictus --goal "compile(myLogolFile),save_program('myLogolFile.sav'),halt."
% spld --main=restore -o myLogolProgram.exe --resources=myLogolFile.sav=path_to_file/myLogolFile.sav
%


runtime_entry(start):- 
        seeing(OldInput),
        see('#PATH2FILE'),                 % Open file F
        repeat,
          read(T),              % Read a term
          ((T == end_of_file);     % Loop back if not at end of file
		  (bagof(Z,query(T,Z),All_Z),remove_dups(All_Z, All_ZPruned),write(All_ZPruned),nl)),
        !,
        seen,see(OldInput).