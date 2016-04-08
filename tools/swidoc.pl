:- use_module(library(doc_latex)).
:- open('logol.tex',write,OS),latex_for_file('logoldoc.pl',OS,[]),close(OS).

:-halt.
