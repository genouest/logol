#!/bin/bash

cp ../prolog/logol.pl logoldoc.pl
swipl -f swidoc.pl -- logoldoc.pl
pdflatex logol.tex

rm logoldoc.pl
rm logol.tex

mv logol.pdf ../Doc/logolParser.pdf

