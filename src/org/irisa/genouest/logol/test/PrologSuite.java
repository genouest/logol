package org.irisa.genouest.logol.test;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

public class PrologSuite {

	public static void main(String[] args) throws ParseException 
	{
		Options options = new Options();

		 // add command line options
		 options.addOption("baseDir", true, "specify configuration file");
		 
		 CommandLineParser cparser = new PosixParser();
		 CommandLine cmd = cparser.parse( options, args);
		 	
		 
		 if( cmd.hasOption( "baseDir" ) ) {
			    // initialise the member variable
			 System.out.println("option baseDir called with "+cmd.getOptionValue( "baseDir" ));
			    GrammarTest.installDir=cmd.getOptionValue( "baseDir" );
			}
		 else {
			 GrammarTest.installDir=System.getProperty("logol.install");
		 }
				
		junit.textui.TestRunner.run(PrologTest.class);
	}
	
}
