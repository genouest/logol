#!/bin/bash

# ARGS:
# Optional		 options.addOption("conf", true, "specify configuration file")
# Optional		 options.addOption("uid", true, "unique identifier for the query")
# Mandatory		 options.addOption("g", true, "grammar file to analyse")
# Mandatory		 options.addOption("s", true, "sequence file to analyse")
# Optional		 options.addOption("max", true, "maximum returned solutions")
#

# Installation directory

export REALDIR="$(readlink -f $0)"
export LOGOL_HOME="$(dirname "$REALDIR" )"

export PATH=$PATH:$LOGOL_HOME/prolog

echo "calling logol with parameters "$*

if [ -z "$LOGOL_LOG4J" ]; then
  LOGOL_LOG4J=$LOGOL_HOME/log4j.properties
fi

if [ -z "$JAVA_OPTS" ]; then
  JAVA_OPTS=" -Xms512m -Xmx4096m"
fi

java $JAVA_OPTS  -Dlogol.install=$LOGOL_HOME -Dlogol.conf=$LOGOL_HOME/prolog/logol.properties -Dlog4j.configuration=file://$LOGOL_LOG4J -classpath  $LOGOL_HOME/lib/biojava.jar:$LOGOL_HOME/lib/bytecode:$LOGOL_HOME/lib/mail.jar:$LOGOL_HOME/lib/activation.jar:$LOGOL_HOME/lib/xalan.jar:$LOGOL_HOME/lib/xercesImpl.jar:$LOGOL_HOME/lib/xml-apis.jar:$LOGOL_HOME/lib/serializer.jar:$LOGOL_HOME/lib/commons-configuration-1.5.jar:$LOGOL_HOME/lib/LogolExec.jar:$LOGOL_HOME/lib/commons-cli-1.1.jar:$LOGOL_HOME/lib/commons-collections-3.2.1.jar:$LOGOL_HOME/lib/commons-lang-2.4.jar:$LOGOL_HOME/lib/commons-logging-1.1.1.jar:$LOGOL_HOME/lib/log4j-1.2.15.jar:$LOGOL_HOME/lib/antlrworks-1.4.3.jar  org.irisa.genouest.logol.Logol  $*

