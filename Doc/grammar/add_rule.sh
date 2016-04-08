#!/bin/bash

FILE=$(basename $1)

NAME=`echo "$FILE" | cut -d'.' -f1`

echo "\\section{$NAME}" >> logol-grammar.tex

echo "\\includegraphics{$1}" >> logol-grammar.tex

