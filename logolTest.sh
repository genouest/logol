#!/bin/bash


# Test script, execute all internal tests to validate installation
# Call with option --baseDir installationpath

export LOGOL_HOME="$( cd -P "$( dirname "$0" )" && pwd )"
export PATH=$PATH:$LOGOL_HOME/prolog

export SUFFIX_HOME=/local/vmatch/vmatch.distribution

echo "calling logol with parameters "$*

java -Xms512m -Xmx1024m -Dlogol.install=$LOGOL_HOME -Dlog4j.configuration=file://$LOGOL_HOME/log4j.properties -classpath   $LOGOL_HOME/lib/biojava.jar:$LOGOL_HOME/lib/bytecode:$LOGOL_HOME/lib/mail.jar:$LOGOL_HOME/lib/activation.jar:$LOGOL_HOME/lib/junit.jar:$LOGOL_HOME/lib/serializer.jar:$LOGOL_HOME/lib/xalan.jar:$LOGOL_HOME/lib/xercesImpl.jar:$LOGOL_HOME/lib/xml-apis.jar:$LOGOL_HOME/lib/commons-configuration-1.5.jar:$LOGOL_HOME/lib/LogolExec.jar:$LOGOL_HOME/lib/commons-cli-1.1.jar:$LOGOL_HOME/lib/commons-collections-3.2.1.jar:$LOGOL_HOME/lib/commons-lang-2.4.jar:$LOGOL_HOME/lib/commons-logging-1.1.1.jar:$LOGOL_HOME/lib/log4j-1.2.15.jar:$LOGOL_HOME/lib/antlrworks-1.4.2.jar  org.irisa.genouest.logol.test.TestSuite  $*


