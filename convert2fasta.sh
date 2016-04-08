#!/bin/bash

# ARGS:
# Mandatory		 input file name
# Mandatory		 output file name
#
#

# Installation directory
export REALDIR="$(readlink -f $0)"
export LOGOL_HOME="$(dirname "$REALDIR" )"


echo "calling convert2fasta with parameters "$*

java -Dlog4j.configuration=file://$LOGOL_HOME/log4j.properties -classpath  $LOGOL_HOME/lib/xalan.jar:$LOGOL_HOME/lib/xercesImpl.jar:$LOGOL_HOME/lib/xml-apis.jar:$LOGOL_HOME/lib/mail.jar:$LOGOL_HOME/lib/serializer.jar:$LOGOL_HOME/lib/commons-configuration-1.5.jar:$LOGOL_HOME/lib/LogolExec.jar:$LOGOL_HOME/lib/commons-cli-1.1.jar:$LOGOL_HOME/lib/commons-collections-3.2.1.jar:$LOGOL_HOME/lib/commons-lang-2.4.jar:$LOGOL_HOME/lib/commons-logging-1.1.1.jar:$LOGOL_HOME/lib/log4j-1.2.15.jar:$LOGOL_HOME/lib/antlrworks-1.4.2.jar  org.irisa.genouest.logol.utils.converter.FastaConverter  $*

