#!/usr/bin/ruby

LOGOL_HOME=File.expand_path(File.dirname(__FILE__))

args = ""

ARGV.each do |a|
    args += " "+a.to_s
end

#puts "calling logol with parameters "+args

cmd="java -Xms512m -Xmx1024m  -Dlogol.install=$LOGOL_HOME -Dlogol.conf=$LOGOL_HOME/prolog/logol.properties -Dlog4j.configuration=file://$LOGOL_HOME/log4j.properties -classpath  $LOGOL_HOME/lib/biojava.jar:$LOGOL_HOME/lib/bytecode:$LOGOL_HOME/lib/mail.jar:$LOGOL_HOME/lib/activation.jar:$LOGOL_HOME/lib/xalan.jar:$LOGOL_HOME/lib/xercesImpl.jar:$LOGOL_HOME/lib/xml-apis.jar:$LOGOL_HOME/lib/serializer.jar:$LOGOL_HOME/lib/commons-configuration-1.5.jar:$LOGOL_HOME/lib/LogolExec.jar:$LOGOL_HOME/lib/commons-cli-1.1.jar:$LOGOL_HOME/lib/commons-collections-3.2.1.jar:$LOGOL_HOME/lib/commons-lang-2.4.jar:$LOGOL_HOME/lib/commons-logging-1.1.1.jar:$LOGOL_HOME/lib/log4j-1.2.15.jar:$LOGOL_HOME/lib/antlrworks-1.4.3.jar  org.irisa.genouest.logol.Logol "+args.to_s
cmd=cmd.gsub(":",";")
cmd=cmd.gsub("file;","file:")
cmd=cmd.gsub("$LOGOL_HOME",LOGOL_HOME)
cmd=cmd.gsub("/","\\")
cmd=cmd.gsub("\\\\","/")
puts "Execute "+cmd
exec(cmd)

