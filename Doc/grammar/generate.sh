#!/bin/bash

export REALDIR="$(readlink -f $0)"
export CURDIR="$(dirname "$REALDIR" )"

cd $CURDIR

sed 's/backtrack=true;//' $CURDIR/../../src/logol.g > $CURDIR/logol.tmp.g

rm -rf $CURDIR/output
mkdir $CURDIR/output
java -cp $CURDIR/../../lib/antlrworks-1.4.2.jar org.antlr.works.Console -f $CURDIR/logol.tmp.g -o $CURDIR/output/ -sd eps -verbose

rm $CURDIR/logol.tmp.g

cp $CURDIR/grammar.tex $CURDIR/logol-grammar.tex

find $CURDIR/output -name *.eps -type f -exec $CURDIR/add_rule.sh {} \;

echo "\\end{document}" >> $CURDIR/logol-grammar.tex

htlatex $CURDIR/logol-grammar.tex

rm -rf $CURDIR/output

