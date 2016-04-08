package org.irisa.genouest.logol.utils.model;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Logger;

public class MatchToModel {

	private static final Logger logger = Logger.getLogger(org.irisa.genouest.logol.utils.model.MatchToModel.class);

	
	/**
	 * @param args: Input file, match Id, Output file
	 */
	public static void main(String[] args) throws Exception {
		
		Options options = new Options();

		 // add command line options
		 options.addOption("h",false,"get usage");
		
		 options.addOption("output", true, "output file name");
		 options.addOption("input", true, "Input file containing matches");
		 options.addOption("id", true, "Id of the match");
		
		CommandLineParser cparser = new PosixParser();
		CommandLine cmd = cparser.parse( options, args);
		
		if(cmd.hasOption("h")) {
			Collection optionList = options.getOptions();
			Iterator it = optionList.iterator();
			System.out.println("Usage:");
			while(it.hasNext()) {
				Option opt = (Option) it.next();
				System.out.println(" - "+opt.getOpt()+" : "+opt.getDescription());
			}
			System.exit(0);
		 }
		
		String output = null;
		if(cmd.hasOption("output")) {
			output = cmd.getOptionValue("output");
		}

		String input = null;
		if(cmd.hasOption("input")) {
			input = cmd.getOptionValue("input");
		}
		
		String match = null;
		if(cmd.hasOption("id")) {
			match = cmd.getOptionValue("id");
		}
		
		if(match==null || input==null || output==null) {
			System.err.println("A parameter is missing!");
			System.exit(1);
		}
		File infile = new File(input);
		if (!infile.exists()) {
			System.err.println("Input file does not exist!");
			System.exit(1);			
		}
		
		ModelConverter lgConv = new ModelConverter();
		String msg = lgConv.mapSequenceMatch(input,match,output);
		System.out.println(msg);

	}
	
	
}
