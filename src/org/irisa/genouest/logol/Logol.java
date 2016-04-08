package org.irisa.genouest.logol;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerException;


import org.antlr.runtime.*;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.apache.xerces.impl.dv.util.Base64;
import org.apache.xpath.XPathAPI;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.SequenceTools;
import org.biojava.bio.seq.impl.RevCompSequence;
import org.biojava.bio.seq.impl.SimpleSequence;
import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojavax.SimpleNamespace;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;
import org.biojavax.bio.seq.RichSequence.IOTools;
import org.irisa.genouest.logol.Constants;
import org.irisa.genouest.logol.GrammarException;
import org.irisa.genouest.logol.LogolVariable;
import org.irisa.genouest.logol.Model;
import org.irisa.genouest.logol.Treatment;
import org.irisa.genouest.logol.VariableId;
import org.irisa.genouest.logol.dispatcher.Dispatch;
import org.irisa.genouest.logol.parser.logolLexer;
import org.irisa.genouest.logol.parser.logolParser;
import org.irisa.genouest.logol.types.ViewVariable;
import org.irisa.genouest.logol.utils.Converter;
import org.irisa.genouest.logol.utils.FastaConverter;
import org.irisa.genouest.logol.utils.GrammarTools;
import org.irisa.genouest.logol.utils.LogolUtils;
import org.irisa.genouest.logol.utils.MyRevCompSequence;
import org.irisa.genouest.logol.utils.MySequence;
import org.irisa.genouest.logol.utils.model.ModelConverter;
import org.irisa.genouest.logol.utils.model.ModelDefinitionException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import java.lang.OutOfMemoryError;


class StreamGobbler implements Runnable {
	String name;
	InputStream is;
	Thread thread;

	private static final Logger logger = Logger.getLogger(StreamGobbler.class);
	
	public StreamGobbler (String name, InputStream is) {
	this.name = name;
	this.is = is;
	}

	public void start () {
	thread = new Thread (this);
	thread.start ();
	}

	public void run () {
	try {
	InputStreamReader isr = new InputStreamReader (is);
	BufferedReader br = new BufferedReader (isr);

	while (true) {
	String s = br.readLine ();
	if (s == null) { break; }
	logger.debug ("[" + name + "] " + s);
	}

	is.close ();

	} catch (IOException ex) {
	logger.error ("Problem reading stream " + name + "... :" + ex);
	ex.printStackTrace ();
	}
	}
	}








/**
 * Main class to analyse a logol grammar and map it in prolog. It then executes the program with the input sequence.
 * @author osallou
 * History: 26/03/09 Fix 1271 Add error email
 * 			22/04/09 Fix 1327 Check directories existence 
 * 			27/04/03 Fix 1330 Wrong position on reverse
 * 			04/05/09 Fix 1333 Wrong position when using local split
 * 			20/05/09 Fix 1365 Add filtering to keep singletons only
 * 			12/06/09 Fix 1337 support relative path
 *			14/04/10 Fix 1602 Add additional filtering options
 *			21/07/10 Fix 1660 OutofMemory issue with Biojava
 *			22/08/10 Fix 1664 Unicode issues with special chars
 *          28/04/11 FIX 1794 sequencelength unknown
 *          08/06/12 Add check option
 *          12/06/12 FIX 2162 If maxlength=0, set it to sequence size
 *          04/10/13 FIX 2242 Error with overlap
 *          07/11/13 Add windows support
 */
public class Logol {

	
	
	
	private static final Logger logger = Logger.getLogger(org.irisa.genouest.logol.Logol.class);
	
	
	private static String OS = System.getProperty("os.name").toLowerCase();
	
	private static final String FILESEPARATORPROPERTY = "file.separator";
	
	
	private static boolean noClean=false;
	
	/**
	 * Location of the program
	 */
	private static String installPath=null;
	/**
	 * Location of result files
	 */
	private static String resultDir=null;
	
	/**
	 * Grammar file name with original path
	 */
	private static String grammarFile=null;
	
	/**
	 * Model file name with original path
	 */
	private static String modelFile=null;
	
	/**
	 * Sequence file name to analyse
	 */
	private static String sequenceFile=null;
	/**
	 * Minimum size a sequence file should have
	 */
	private static int minSplitSize=0;
	
	
	/**
	 * Execution file name
	 */
	private static String executable = null;
	
	
	/**
	 * Defines the maximum size of a spacer.<br/>. 
	 * Used as a maximum shift when a spacer is used.
	 */
	//private static int rLength = 10000;
	/**
	 * Defines the maximum size of a variable.<br/>
	 * Used if no length constraint is applied on variable or "_" in max of length constraint.
	 */
	//private static int maxLength = 1000;
	/**
	 * Defines the minimum size of a variable.<br/>
	 * Used if no length constraint is applied on variable.
	 */
	//private static int minLength = 10;

	/**
	 * Execution type:
	 * <li>0 : local</li>
	 * <li>0 : sge (drmaa)</li>
	 */

	/**
	 * Number of processors on computer when inn local mode
	 */
	private static int nbproc = 1;
	

	
	private static String maxSolutions="100";
	
	private static String outputSeqName=null;
	
	private static int direction=-1;
	
	private static int minTreeIndex=2;
	
	/**
	 * General offset to apply on positions, received as input arguments
	 */
	private static int offset=0;


	//TODO post-process: check that we have results in all "AND" sequences
	
	//TODO get all uses of variables (how many times are they called)
	// to preanalyse what we can do (search all duplicates (length constraint))...
	// ((fixed(2),((\+var(X11));(var(X11),X11='mod1,2')));(\+fixed(2),((\+var(X11),write(X11),write(' used in mod1,2'),nl;(var(X11))))))
	
	private static Configuration config =null;
	
	
	private static String outputResultFileName = null;
	
	
	private static String savfile=null;
	
	
	private static int startSequence=-1;
	private static int endSequence=-1;
	private static int sequenceID=1;
	
	private static int maxMatchSize=0;
	
	public static boolean singlesOnly = true;
	//Fix 1602
	private static FILTER filterType =FILTER.EXACT;
	public static enum FILTER {
		EXACT,
		LOCAL,
		LOCAL0,
		GLOBAL
	}
	
	public static void reset() {
		startSequence=-1;
		endSequence=-1;
		offset=0;
		sequenceID=1;
		grammarFile=null;
		modelFile=null;
	}
	
	/*
	 * Suffix tool to use:
	 *  0 = internal (default)
	 *  1 = vmatch
	 */
	private static int suffixTool = 0;
	
	public static int getSuffixTool() {
		return suffixTool;
	}



	public static void setSuffixTool(int suffixTool) {
		Logol.suffixTool = suffixTool;
	}
	
	private static String suffixPath = null;



	public static String getSuffixPath() {
		return suffixPath;
	}



	public static void setSuffixPath(String suffixPath) {
		Logol.suffixPath = suffixPath;
	}



	/**
	 * Initialize some data from properties file
	 * @param propFile property file path
	 * @throws ConfigurationException
	 */
	private static void init(String propFile) throws ConfigurationException {
		if(propFile==null) { 
			if(System.getProperty("logol.conf")!=null) {
				propFile=System.getProperty("logol.conf");
			}
			else {
			propFile="logol.properties";
			}
			}
		logger.info("Using configuration file: "+propFile);
		//Fix 1337 support relative path
		 File  lv_file = new File(propFile);			 	
		 config = new PropertiesConfiguration(lv_file.getAbsolutePath());
		 
		 	//installPath=config.getString("installPath");
			String path = System.getProperty("logol.install");
			if(path!=null)  { 
				installPath = path;
			}
			else {
				installPath=".";
			}
			
			if(config.containsKey("suffix.tool")) {
				suffixTool = config.getInt("suffix.tool");
			}
			
			if(config.containsKey("suffix.path") && !config.getString("suffix.path").equals("")) {
				suffixPath = config.getString("suffix.path");
			}
			
			// Manage Windows path
		 	installPath = installPath.replaceAll("\\\\", "/");
		 	Treatment.installPath = installPath;
		 	
		 	switch(suffixTool) {
		 	case 0:
		 		Treatment.suffixSearchPath=installPath+"/tools/logolSearch.rb";
		 		break;
		 	case 1:
		 		Treatment.suffixSearchPath=installPath+"/tools/suffixSearch.rb";
		 		break;
		 	case 2:
		 		Treatment.suffixSearchPath=installPath+"/tools/cassiopeeSearch.rb";
		 		break;
		 	default:
		 		Treatment.suffixSearchPath=installPath+"/tools/cassiopeeSearch.rb";
		 		break;
		 	}
		 	
		 	savfile = installPath+System.getProperty(FILESEPARATORPROPERTY)+"prolog"+System.getProperty(FILESEPARATORPROPERTY)+"logol.sav";
			//To use global exe, set exe as logol.exe
			executable = installPath+System.getProperty(FILESEPARATORPROPERTY)+"prolog"+System.getProperty(FILESEPARATORPROPERTY)+"logol.exe";
		 	
		 	minSplitSize =config.getInt("minSplitSize");	
		 
		 	Treatment.parentStrategy=config.getInt("parentStrategy");
		 
			// Maximum size to write the content on results, else write "..."
			Treatment.maxResultSize=config.getLong("maxResultSize");
			
			
			
			Treatment.workingDir=config.getString("workingDir");
			
			/*
			 * If dir.result not defined, keep working dir as destination
			 */
			if(config.getString("dir.result")!=null) {
				resultDir = config.getString("dir.result");
			}
			else {
				resultDir = Treatment.workingDir;
			}
						
				
			
			/**
			 * Maximum size of a spacer.
			 */
			Treatment.maxSpacerLength = config.getInt("maxSpacerLength");
			/*
			 * Maximum size of a variable
			 */
			Treatment.maxLength = config.getInt("maxLength");
			Treatment.minLength = config.getInt("minLength");		 
		 
			if(config.containsKey("nbProcessor")) {
			nbproc = config.getInt("nbProcessor");
			}
			
			
			maxSolutions = config.getString("maxSolutions");
			
			minTreeIndex = config.getInt("minTreeIndex");
			
			maxMatchSize = config.getInt("maxMatchSize");
			
		 
	}
	
	
	
	/**
	 * Entry point of the program
	 * @param args path to working directory + unique ID
	 * @throws IOException 
	 * @throws RecognitionException 
	 * @throws InterruptedException 
	 * @throws ConfigurationException 
	 * @throws ParseException 
	 * @throws GrammarException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException, ConfigurationException, ParseException, GrammarException {
 
	// create Options object
		manageCommandLine(args);
		 
			
		outputSeqName=Treatment.workingDir+System.getProperty(FILESEPARATORPROPERTY)+Treatment.filename+"."+Treatment.uID;

		// Fix 1271
		// set error file for this job, managed by GrammarException
		if(outputResultFileName==null) { logger.debug("No ouput file name specified, do not use error logging with GrammarException"); }
		else {
			String errFile=null;
			File tmpf = new File(outputResultFileName);
			if(tmpf.getParent()==null) {
			// If no output directory is specified, use default directory
				errFile = resultDir+'/'+outputResultFileName+".err";
			}
			else {
				// If a directory is specified , use it.
				errFile=outputResultFileName+".err";
			}
			GrammarException.setErrFile(errFile);
		}
		
		 // Check shared and local directory existence
		 File wDir = new File(Treatment.workingDir);
		 if(!wDir.exists()) {
			 throw new GrammarException("Error, directory "+Treatment.workingDir+" does not exist");
		 }
		 wDir = new File(resultDir);
		 if(!wDir.exists()) {
			 throw new GrammarException("Error, directory "+resultDir+" does not exist");
		 }
		
		
		if(grammarFile==null && modelFile!=null) {
			grammarFile  = outputSeqName+".logol";
			ModelConverter conv = new ModelConverter();
			try {
				conv.decode(modelFile,grammarFile);
			} catch (ParserConfigurationException e) {
				throw new GrammarException("Error during model decoding:\n "+e.getMessage());
			} catch (SAXException e) {
				throw new GrammarException("Error during model decoding:\n "+e.getMessage());
			} catch (TransformerException e) {
				throw new GrammarException("Error during model decoding:\n "+e.getMessage());
			} catch (ModelDefinitionException e) {
				throw new GrammarException("Error during model decoding:\n "+e.getMessage());
			}
		}
	
		// Store result in a different file.
		logger.debug("Split files if required, create suffix array data and call program");
		execute();
		 
	
	}

	/**
	 * Parse command line parameter to setup the program
	 * @param args command line arguments
	 * @throws GrammarException
	 * @throws ParseException
	 * @throws ConfigurationException
	 * @throws IOException 
	 */
	private static void manageCommandLine(String[] args) throws GrammarException, ParseException, ConfigurationException, IOException {
		 
		//TODO move options management in a new class, common to Dispatch and Logol.
		
		Options options = new Options();

		 // add command line options
		
		 options.addOption("v",false,"get version");
		 options.addOption("h",false,"get usage");
		
		 options.addOption("output", true, "output file name, msut be unique");
		 
		 options.addOption("conf", true, "specify configuration file");
		 options.addOption("uid", true, "unique identifier for the query");
		 options.addOption("g", true, "grammar file to analyse");
		 options.addOption("m", true, "model file to analyse");
		 options.addOption("s", true, "sequence file to analyse.");
		 options.addOption("max", true, "maximum returned solutions");

		 options.addOption("maxres", true, "maximum result size of a match");
		 options.addOption("maxmatchsize", true, "maximum size of a match");
		 options.addOption("maxspacer", true, "maximum size of a spacer");
		 options.addOption("lmax", true, "maximum length of a variable");
		 options.addOption("lmin", true, "minimum length of a variable");
		 
		 options.addOption("dna", false, "analyse dna, is default");
		 options.addOption("rna", false, "analyse rna");
		 options.addOption("protein", false, "analyse protein");
		 
		 options.addOption("noclean", false, "Do not delete created files after treatment");
		 
		 options.addOption("all",false,"analyse both directions of the sequence");
		 
		 options.addOption("offset", true, "Offset value to add to positions");
		 
		 
		 options.addOption("start", true, "Start value on sequence");
		 options.addOption("end", true, "End value on sequence");
		 options.addOption("sequenceID", true, "position of sequence in bank");
		 //Fix 1365
		 options.addOption("enabledups", false, "enable duplicate result matches. By default only keep singletons");
		 
		 options.addOption("filter",true,"Type of filter when enabledups is not enabled. Allowed types are exact(default),local,local0,global.");
		 
		 options.addOption("check",false,"Check logol grammar file, only require -g option");
		 
		 // To support dispatcher specific options, those are ignored
		 options.addOption("db", true, "Multi only: sequence database");
		 options.addOption("sge", false, "Multi only: use SGE");
		 options.addOption("local", false, "Multi only: use local system");
		 options.addOption("format", true, "Multi only: format of database");
		 options.addOption("contig", false, "Multi only: set the bank as contig sequences");
		 options.addOption("guid", true, "unique identifier for the query");
		 options.addOption("email", true, "Email address to send result availability info");
		 options.addOption("out", true, "Zip output file name");
		 options.addOption("forcesplit", false, "Force the sequence splitting according to parameters and whatever is the number of model used in grammar");
		 options.addOption("fasta",false,"Add fasta conversion to result archive");
		 options.addOption("gff",false,"Add gff conversion to result archive");
		 // *****************************
		 		
		
		 
		 CommandLineParser cparser = new PosixParser();
		 CommandLine cmd = cparser.parse( options, args);
		 
		 System.out.println("For help, use option -h");
		 
		 if(cmd.hasOption("v")) {
	        	Class<Logol> clazz = Logol.class; 
	        	String classContainer = clazz.getProtectionDomain().getCodeSource().getLocation().toString();
	        	URL manifestUrl = new URL("jar:" + classContainer + "!/META-INF/MANIFEST.MF");
	        	Manifest mf = new Manifest(manifestUrl.openStream());
	        	Attributes atts = mf.getMainAttributes();
	        	
	        	System.out.println("Logol version: "+atts.getValue("Implementation-Version")+"-"+atts.getValue("Implementation-Build"));        		
			 System.exit(0);
			 System.exit(0);
		 }
		 if(cmd.hasOption("h")) {
				Options usageOptions = new Options();
				usageOptions.addOption("v",false,"get version");
				usageOptions.addOption("output", true, "output file name, msut be unique");
				usageOptions.addOption("h",false,"get usage");				
				usageOptions.addOption("conf", true, "specify configuration file");
				usageOptions.addOption("uid", true, "unique identifier for the query");
				usageOptions.addOption("g", true, "grammar file to analyse");
				usageOptions.addOption("m", true, "model file to analyse");
				usageOptions.addOption("s", true, "sequence file to analyse.");
				usageOptions.addOption("max", true, "maximum returned solutions");				 
				usageOptions.addOption("maxres", true, "maximum result size of a match");
				usageOptions.addOption("maxspacer", true, "maximum size of a spacer");
				usageOptions.addOption("lmax", true, "maximum length of a variable");
				usageOptions.addOption("lmin", true, "minimum length of a variable");				 
				usageOptions.addOption("dna", false, "analyse dna, is default");
				usageOptions.addOption("rna", false, "analyse rna");
				usageOptions.addOption("protein", false, "analyse protein");				 
				usageOptions.addOption("noclean", false, "Do not delete created files after treatment");				 
				usageOptions.addOption("all",false,"analyse both directions of the sequence");				 
				usageOptions.addOption("offset", true, "Offset value to add to positions");
				usageOptions.addOption("start", true, "Start value on sequence");
				usageOptions.addOption("end", true, "End value on sequence");
				usageOptions.addOption("sequenceID", true, "position of sequence in bank");
				usageOptions.addOption("c", true, "maximum size of a match");
				usageOptions.addOption("enabledups", false, "enable duplicate result matches. By default only keep singletons");
				usageOptions.addOption("filter",true,"Type of filter when enabledups is not enabled. Allowed types are exact(default),local,local0,global.");
				usageOptions.addOption("check",false,"Check logol grammar file, only require -g option");
				LogolUtils.showUsage(usageOptions);
				 System.exit(0);
		 }			 
		 
		 
		 if( cmd.hasOption( "conf" ) ) {
			    // initialise the member variable
			 logger.info("option conf called with "+cmd.getOptionValue( "conf" ));
			    init(cmd.getOptionValue( "conf" ));
			}
		 else {
			 init(null);
		 }
		 
		 if( cmd.hasOption( "uid" ) ) {
			    // initialise the member variable
			 logger.info("option uid called with "+cmd.getOptionValue( "uid" ));
			 Treatment.uID=cmd.getOptionValue( "uid" );
			}
		 else {
			 Treatment.uID=UUID.randomUUID().toString();
		 }		 
		 if( cmd.hasOption( "g" ) ) {
			    // initialise the member variable
			//Fix 1337 support relative path
			 logger.info("option g called with "+cmd.getOptionValue( "g" ));
			 File  lv_file = new File(cmd.getOptionValue( "g" ));			 
			 grammarFile=lv_file.getAbsolutePath();
			 Treatment.filename = lv_file.getName();
			}
		 else {
			 if(cmd.hasOption("m")) {
				//Fix 1337 support relative path
				 logger.info("option m called with "+cmd.getOptionValue( "m" ));
				 File  lv_file = new File(cmd.getOptionValue( "m" ));
				 modelFile=lv_file.getAbsolutePath();
				 Treatment.filename = lv_file.getName();
			 }
			 else {
			 throw new GrammarException("Missing grammar or model file in command-line");
			 }
		 }
		 
		 
		 if (cmd.hasOption("check")) {
			 try {
			  analyse();
			  System.out.println("Grammar analysed with success");
			  System.exit(0);
			 }
			 catch(GrammarException e) {
				 System.err.println("Grammar analyse failed");
				 System.exit(1);
			 }
		 }
		 
		 if( cmd.hasOption( "s" ) ) {
			//Fix 1337 support relative path
			    // initialise the member variable
			 logger.info("option s called with "+cmd.getOptionValue( "s" ));
			 File  lv_file = new File(cmd.getOptionValue( "s" ));
			 sequenceFile=lv_file.getAbsolutePath();
			}
		 else {
			 throw new GrammarException("Missing sequence file name in command-line");
		 }			 

		 if( cmd.hasOption( "max" ) ) {
			    // initialise the member variable
			 logger.info("option max called with "+cmd.getOptionValue( "max" ));
			 maxSolutions=cmd.getOptionValue( "max" ).trim();
			}
		 else {
			logger.info("No maximum solutions defined, using defaults");
		 }	
		 
		 if( cmd.hasOption( "maxres" ) ) {
			    // initialise the member variable
			 logger.info("option maxres called with "+cmd.getOptionValue( "maxres" ));
			 Treatment.maxResultSize=Long.valueOf(cmd.getOptionValue( "maxres" ));
			}
		 if( cmd.hasOption( "maxmatchsize" ) ) {
			    // initialise the member variable
			 logger.info("option max match size called with "+cmd.getOptionValue( "maxmatchsize" ));
			 maxMatchSize=Integer.valueOf(cmd.getOptionValue( "maxmatchsize"));
			}		 
		 
		 if( cmd.hasOption( "maxspacer" ) ) {
			    // initialise the member variable
			 logger.info("option maxspacer called with "+cmd.getOptionValue( "maxspacer" ));
			 Treatment.maxSpacerLength=Integer.valueOf(cmd.getOptionValue( "maxspacer" ));
			}
		 if( cmd.hasOption( "lmax" ) ) {
			    // initialise the member variable
			 logger.info("option lmax called with "+cmd.getOptionValue( "lmax" ));
			 Treatment.maxLength=Integer.valueOf(cmd.getOptionValue( "lmax" ));
			}
		 if( cmd.hasOption( "lmin" ) ) {
			    // initialise the member variable
			 logger.info("option lmin called with "+cmd.getOptionValue( "lmin" ));
			 Treatment.minLength=Integer.valueOf(cmd.getOptionValue( "lmin" ));
			}		 

		
		if( cmd.hasOption( "all" ) ) {
			    // initialise the member variable
			 logger.info("option all called ");
			 direction=Constants.DIR_ALL;
			}	
		 else {
			 direction=Constants.DIR_PLUS;
		 }
		
		 if( cmd.hasOption( "noclean" ) ) {
			    // initialise the member variable
			 logger.info("option noclean called");
			  noClean=true;
			}
		 
		 if( cmd.hasOption( "dna" ) ) {
			    // initialise the member variable
			 logger.info("option dna called");
			 Treatment.dataType=Constants.DNA;
			}
		 else if( cmd.hasOption( "rna" ) ) {
				    // initialise the member variable
				 logger.info("option rna called");
				 Treatment.dataType=Constants.RNA;
				}
			 else if( cmd.hasOption( "protein" ) ) {
					    // initialise the member variable
					 logger.info("option protein called");
					 Treatment.dataType=Constants.PROTEIN;
					}

		 if( cmd.hasOption( "offset" ) ) {
			    // initialise the member variable
			 logger.info("option offset called with "+cmd.getOptionValue( "offset" ));
			  offset+=Integer.valueOf(cmd.getOptionValue( "offset" ));
			} 
		 
		 if( cmd.hasOption( "output" ) ) {
			    // initialise the member variable
			 logger.info("option output called with "+cmd.getOptionValue( "output" ));
			  outputResultFileName=cmd.getOptionValue( "output" );
			} 		 
		 
		 if( cmd.hasOption( "start" ) ) {
			    // initialise the member variable
			 logger.info("option start called with "+cmd.getOptionValue( "start" ));
			  startSequence=Integer.valueOf(cmd.getOptionValue( "start" ));
			  // Take into account the start in offset as sequence will be cut.
			  offset+=startSequence-1;
			} 
		 if( cmd.hasOption( "end" ) ) {
			    // initialise the member variable
			 logger.info("option end called with "+cmd.getOptionValue( "end" ));
			  endSequence=Integer.valueOf(cmd.getOptionValue( "end" ));
			} 
		 if( cmd.hasOption( "sequenceID" ) ) {
			    // initialise the member variable
			 logger.info("option sequenceID called with "+cmd.getOptionValue( "sequenceID" ));
			  sequenceID=Integer.valueOf(cmd.getOptionValue( "sequenceID" ));
			} 	
		 //Fix 1365
		 if( cmd.hasOption( "enabledups" ) ) {
			    // initialise the member variable
			 logger.info("option enabledups called");
			 singlesOnly = false;
			}
		 //Fix 1602
		 if( cmd.hasOption( "filter" ) ) {
			    // initialise the member variable
			 logger.info("option filter called with "+cmd.getOptionValue( "filter" ));
			 String filter = cmd.getOptionValue( "filter" );
			 if(filter.equalsIgnoreCase("exact")) {
				 filterType = FILTER.EXACT;
			 }
			 else if(filter.equalsIgnoreCase("local")) {
				 filterType = FILTER.LOCAL;				 
			 } else if(filter.equalsIgnoreCase("global")) {
				 filterType = FILTER.GLOBAL;
			 }
		}
		 
		 
	}


	/**
	 * Executes the program. Will split the sequence file if required according to size thresholds and will parallelize the programs.
	 * @throws IOException
	 * @throws GrammarException
	 * @throws IllegalAlphabetException 
	 */
	private static void execute() throws IOException, GrammarException {
		
		String prologfile = Treatment.workingDir+System.getProperty(FILESEPARATORPROPERTY)+Treatment.filename+"."+Treatment.uID+".pro";
		
		Vector<SequenceAnalyser> threads  = new Vector<SequenceAnalyser>();
		
		Vector<String> resultFiles = new Vector<String>();
		

		String header = null; 
 
	    String resultFile = null;
	   
	     // Load sequence file
	      BufferedReader br = new BufferedReader(new FileReader(sequenceFile));
	      RichSequenceIterator iter =  null;
	      /*
	      switch(Treatment.dataType) {
	      case Constants.DNA: {
	    	  iter= (RichSequenceIterator)IOTools.readFastaDNA(br,null);
	    	  break;
	      }
	      case Constants.RNA: {
	    	  iter= (RichSequenceIterator)IOTools.readFastaRNA(br,null);
	    	  break;
	      }
	      case Constants.PROTEIN: {
	    	  iter= (RichSequenceIterator)IOTools.readFastaProtein(br,null);
	    	  break;
	      }	      
	      }
	      */
	      //Fix1660
	      iter = (RichSequenceIterator) org.irisa.genouest.logol.utils.MySequenceIterator.readFasta(br);
	      
	      // Sequence to analyse in the bank
	      Sequence seq = null;
	      
	      // Reverse complement sequence if required
	      //RevCompSequence rev = null;
	      //Fix1660
	      MyRevCompSequence rev = null;
	      
	      int position=0;
	      
	      try {
	    	  while(iter.hasNext() && position != sequenceID) {
	    		  // Search for sequence id, take sub sequence if a start or end is defined
	    		  position++;
	    		  seq =  iter.nextSequence();
	    		  if(position==sequenceID) {
	    		  if(startSequence!=-1 || endSequence!=-1) {
	    			  if(startSequence==-1) {
	    				  logger.debug("split sequence 1,"+endSequence);
	    				  //seq = SequenceTools.subSequence(seq,1,endSequence);
	    				  //Fix1660
	    				  seq = new MySequence(seq.getName(),seq.subStr(1, endSequence));
	    			  }
	    			  else if(endSequence==-1) {
	    				  logger.debug("split sequence "+startSequence+","+seq.length());
	    				  //seq = SequenceTools.subSequence(seq,startSequence,seq.length());
	    				  //Fix1660
	    				  seq = new MySequence(seq.getName(),seq.subStr(startSequence,seq.length()));
	    			  }
	    			  else {
	    				  logger.debug("split sequence "+startSequence+","+endSequence);
	    				  //seq = SequenceTools.subSequence(seq,startSequence,endSequence);
	    				  //Fix1660
	    				  seq = new MySequence(seq.getName(),seq.subStr(startSequence,endSequence));
	    			  }
	    		  }
	    		  break;
	    		  }
	    		  //position++;
	    	  }
		} catch (NoSuchElementException e1) {
			throw new GrammarException("Error on sequence: "+e1.getMessage());
		} catch (BioException e1) {
			throw new GrammarException("Error on sequence: "+e1.getMessage());
		} 
		
		
		int start=0;
		int end = seq.length();
		
		if(startSequence==-1) {
			start=0;
		}
		else {
			start=startSequence;
		}
		if(endSequence==-1) {
			end=seq.length();
		}
		else {
			end=endSequence;
		}
		
		//start+=offset;
		//end+=offset;
		
		
		
		Integer[][] sequences = LogolUtils.splitSequence(seq.length(), nbproc, minSplitSize, maxMatchSize, LogolVariable.mainModels.size());
		
		// If maxSpacerLength=0, set it to sequence size
		if(Treatment.maxSpacerLength==0)  {Treatment.maxSpacerLength=sequences[0][1]-sequences[0][0];}
		if(Treatment.maxResultSize==0) {Treatment.maxResultSize=sequences[0][1]-sequences[0][0];}
		// fix 2162
		if(Treatment.maxLength==0)  {Treatment.maxLength=sequences[0][1]-sequences[0][0];}
		
		// Fix 1794 Set max sequence size
		Treatment.sequenceLength = sequences[0][1]-sequences[0][0];
		
		// Now analyse and generate prolog
		logger.info("Start analyse to create grammar analyser");
		analyse();
		
		// For all sub sequences
		for(int s=0;s<sequences.length;s++) {
		
		Sequence subseq = null;
		// Take subsequence. If size==1, then sequence cannot be splitted, use seq.
		if(sequences.length>1) {
			//subseq=SequenceTools.subSequence(seq,sequences[s][0],sequences[s][1]);
			//Fix1660
			subseq = new MySequence(seq.getName(),seq.subStr(sequences[s][0], sequences[s][1]));
		}
		else {
			subseq=seq;
		}
		
		
   	 	//resultFile = outputSeqName+"."+(start+sequences[s][0])+"-"+(start+sequences[s][1]);
		// Starts at initial offset + input start offset + optional additional cut-off
		//FIX 1333
		resultFile = outputSeqName+"."+(offset+sequences[s][0])+"-"+(offset+sequences[s][1]);
   	 	resultFiles.add(resultFile);
	 
   	 	
   	 	// Start a new thread to execute analyser on subsequence
   	 	SequenceAnalyser analyseThread = new SequenceAnalyser(executable,prologfile,savfile,subseq,0,subseq.length(),resultFile,maxSolutions,offset+(sequences[s][0]).intValue()-1);
   	 	analyseThread.minTreeIndex=minTreeIndex;
   	 	analyseThread.start();
   	 	threads.add(analyseThread);
		   	 if(direction==Constants.DIR_ALL && Treatment.dataType!=Constants.PROTEIN) {
				 //Do same for reverse complement
		   		 
		   		 //try {
		   			 //rev = new RevCompSequence(subseq);
		   			rev = new MyRevCompSequence(subseq);
		   			 //FIX 1333
					 //resultFile = outputSeqName+".reverse."+(start+sequences[s][0])+"-"+(start+sequences[s][1]);
		   			resultFile = outputSeqName+".reverse."+(offset+sequences[s][0])+"-"+(offset+sequences[s][1]);
			    	 resultFiles.add(resultFile);
					 //FIX Do not set offset here due to reverse, will be taken into account at xml transformation time
			    	 SequenceAnalyser analyseThreadReverse = new SequenceAnalyser(executable,prologfile,savfile,rev,rev.length(),0,resultFile,maxSolutions,0);
			    	 analyseThreadReverse.minTreeIndex=minTreeIndex;
			    	 analyseThreadReverse.start();
			    	 threads.add(analyseThreadReverse);	
		   		 //} catch (IllegalAlphabetException e) {
				 //	throw new GrammarException("Error to reverse complement the sequence: "+e.getMessage());
				 //}
    		 
			 }		
		
		 
		}
		  
		logger.info("Analyse in progress..");		  
		// reset sequences, not needed anymore
		//seq=null;
		//rev=null;
	     
	     // Now wait for the end of all threads
	     logger.debug("Wait for the end of the treads");
	      for (int i=0; i <threads.size(); ++i) {
	    	  try {
	    		  	threads.get(i).join();
	    	  }
	    	   catch (InterruptedException e) {
	    	       logger.error("Threads join interrupted\n");
	    	   }
	      }
	      logger.debug("Threads are over, check status");
	      // All threads are over, job is finished, check status
	      for (int i=0; i <threads.size(); ++i) {
	    	  		header = threads.get(i).headerFile;
	    		  	if(threads.get(i).status!=0) {
	    		  		throw new GrammarException("ERROR: "+threads.get(i).msg);
	    		  	}	    	 
	      }
	      
	      
	      
	      logger.debug("All sequence analyser threads are over now.");
	                

	      /**
	       * Apply stylesheet to reorder variables in ascending position, calculate total number of error etc...
	       */
	      String inFile=null;
	      //String outFile=null;
	      String xsltFile=installPath+System.getProperty(FILESEPARATORPROPERTY)+"prolog"+System.getProperty(FILESEPARATORPROPERTY)+"logol.order.xsl";
	      String mergeFileName  = outputSeqName+"."+(start)+"-"+(end)+"-all.xml";
	      File mergeFile = new File(mergeFileName);
	      
	      PrintWriter mergeOutputStream = new PrintWriter(mergeFile);	  
	
	      int countMergedResults = 0;
	      
	      
	      mergeOutputStream.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
	      mergeOutputStream.println("<sequences>");
	      // Add header to keep sequence information in result file
	      mergeOutputStream.println("<fastaHeader>"+header.substring(1)+"</fastaHeader>");
	      mergeOutputStream.println("<sequenceBegin>"+start+"</sequenceBegin>");
	      mergeOutputStream.println("<sequenceEnd>"+end+"</sequenceEnd>");

	      // Include original grammar or model in result
	      FileReader mFile=null;
	      String mHeader = null;
	      if(modelFile!=null) {
	    	  mFile = new FileReader(modelFile);
	    	  mHeader = "model";	      
	      }
	      else {
	    	  mFile = new FileReader(grammarFile);
		      mHeader = "grammar";	    	  
	      }
	      BufferedReader bufRead = new BufferedReader(mFile);
	      String line = "";
	      String mContent="";
	      while((line = bufRead.readLine()) !=null) {
	    	  mContent = mContent + line+"\n";
	      }
	      mFile.close();
	      
	      mergeOutputStream.println("<"+mHeader+">"+Base64.encode(mContent.getBytes())+"</"+mHeader+">");
	      
	      for(int i=0;i<resultFiles.size();i++) {
	    	  logger.debug("transform xml result file with stylesheet");
	    	  inFile = resultFiles.get(i)+".xml";
	    	  logger.debug("in = "+inFile);
	    	  logger.debug("out = "+mergeFileName);

	    	
	    	  try {
	    		  if(countMergedResults<Integer.parseInt(Logol.maxSolutions)) {
	    			  int nbmatch = transformXSLT(inFile,xsltFile,mergeOutputStream,String.valueOf(i));
	    			  logger.info(inFile+": "+nbmatch);
	    			  countMergedResults += nbmatch;
	    		  }
	    		  else {
	    			  logger.debug("Max solutions reached, stop treating other files");
	    			  break;
	    		  }
			} catch (TransformerException e) {
				logger.error("Error while applying stylesheet in postprocess: "+e.getMessage());
			} catch (ParserConfigurationException e) {
				logger.error("Error while applying stylesheet in postprocess: "+e.getMessage());				
			} catch (SAXException e) {
				logger.error("Error while applying stylesheet in postprocess: "+e.getMessage());
			} catch (OutOfMemoryError e) {
                logger.error("Out of memory error, you should increase -Xmx value Java option in LogolExec.sh file");
            }
           

	      }
	      logger.info("Number of output match: "+Math.min(countMergedResults, Integer.parseInt(Logol.maxSolutions)));
	      if(countMergedResults==Integer.parseInt(Logol.maxSolutions)) {
	    	  logger.warn("Max number of match reached, other matches may be avaible.");
	      }
	      mergeOutputStream.println("</sequences>");
	      
	        mergeOutputStream.close();
	        logger.debug("file transformed and renamed");
	        
			String outputResultFile;
			if(outputResultFileName==null) { outputResultFile = resultDir+'/'+mergeFile.getName(); }
			else {
				File tmpf = new File(outputResultFileName);
				if(tmpf.getParent()==null) {
				// If no output directory is specified, use default directory
				outputResultFile = resultDir+'/'+outputResultFileName;
				}
				else {
					// If a directory is specified , use it.
					outputResultFile=outputResultFileName;
				}
			}
			logger.debug("Output file to: "+outputResultFile);
			//File out = new File(outputResultFile);			
						
			//boolean res = mergeFile.renameTo(out);
			boolean res = LogolUtils.moveFile(mergeFileName, outputResultFile);
			logger.debug("Rename file result: "+res);
			
			setOutputSeqName(outputResultFile);
	      

	      // If noClean option is not set, delete temporary files
	      if(!noClean) {
	      logger.debug("Erase temporary files now");
	      clean(resultFiles);
	      }
	      
	      logger.info("JOB OVER !!!");
	      logger.info("RESULT FILES ARE AVAILABLE: ");
	      for(int i=0;i<resultFiles.size();i++) {
	    	  logger.debug("- intermediate results : "+resultFiles.get(i)+".xml");
	      }
	      logger.info("- "+outputResultFile);
	      
	      
	 
	}


	/**
	* Search for fixed string with [] , i.e. "complex string" to transform them in views
	*
	*/
	private static String searchForComplexStrings(String grammar) {

        	logger.debug("Search for complex string");
        	Pattern pcomplex =  Pattern.compile("(([\\+\\-]\".*?\"\\s+)?\".*?\"(:\\{.*?\\})*)");
        	Matcher mcomplex = pcomplex.matcher(grammar);
		String modifier = "";
      		while(mcomplex.find()) {
        		logger.debug("found = "+mcomplex.groupCount()+", "+ mcomplex.group(1));
			String complexstring = mcomplex.group(1);
			String fixed2view = Logol.mapComplexStringToView(complexstring);
			grammar = grammar.replace(complexstring,fixed2view);
        	}
		return grammar;
	}

	/**
	* Transform a complex string in a view
	*/
	private static String mapComplexStringToView(String complexstring) {

        List<String[]> fixedElts = new ArrayList<String[]>();
		String[] complexstringElts = complexstring.split(":");
		String fixedValue = complexstringElts[0];
		// Modifier ?
		String modifier = "";
		String[] modifier_fixed = fixedValue.split("\\s+");
		if(modifier_fixed.length>1) {
			modifier = modifier_fixed[0]+" ";
			fixedValue = modifier_fixed[1];
		}
		if(! fixedValue.contains("[")) { return complexstring; }

                fixedValue = fixedValue.replace("\"","");
                String[] elts = fixedValue.split("\\[");
                for(int i=0;i<elts.length;i++) {
                    if(elts[i].equals("")) { continue; }
                    if(elts[i].contains("]")) {
                        String closeelts[] = elts[i].split("\\]");
                        if(closeelts.length>1) {
                        if(closeelts[0].contains("|")) {
                            fixedElts.add(closeelts[0].split("\\|"));
                        }
                        else {
                            fixedElts.add(new String[] {closeelts[0]} );
                            //fixedElts.add(new String[] {closeelts[1]}); 
                        }
                        fixedElts.add(new String[] {closeelts[1]});
                        }
                        else {
                        if(closeelts[0].contains("|")) {
                            fixedElts.add(closeelts[0].split("\\|"));
                        }
                        else {
                             fixedElts.add(new String[] {closeelts[0]}); }
                        }
                    }
                    else {
                        fixedElts.add(new String[] {elts[i]});
                    }
                }

		String fixed2view = "(";
                boolean first = true;


		boolean hasConstraints = false;
		String beginconstr = null;
		String endconstr = null;
		String structconstr = null;
		if(complexstringElts.length>1) {
			hasConstraints = true;
			for(int i=1;i<complexstringElts.length;i++) {
        			Pattern pconstr =  Pattern.compile("[{,](.*?{1,2}\\[.*?\\])");
        			Matcher mconstr = pconstr.matcher(complexstringElts[i]);
      				while(mconstr.find()) {
					String complexstringEltsConstr = mconstr.group(1);
					if(complexstringEltsConstr.startsWith("@[")) {
						// Begin constraint
						beginconstr = complexstringEltsConstr;
					}
					else if(complexstringEltsConstr.startsWith("@@[")) {
						// End constraint
						endconstr = complexstringEltsConstr;
					}
					else if(complexstringEltsConstr.startsWith("$[") || complexstringEltsConstr.startsWith("$$[")) {
						// Struct constraint
						structconstr = complexstringElts[i];
					}
				}
			}

		}

		logger.debug("modifier "+modifier);
		// Reverse, so need to reverse the list order
		if(modifier.startsWith("-")) {
			Collections.reverse(fixedElts);
		}
                for(int i=0;i<fixedElts.size();i++) {
                    if(! first) { fixed2view += ","; }

                    if(fixedElts.get(i).length==1) { 
                        fixed2view += modifier+"\""+fixedElts.get(i)[0]+"\"";
                        if(first && beginconstr!=null) { fixed2view += ":{"+beginconstr+"}"; }
			if(i==fixedElts.size()-1 && endconstr!=null) { fixed2view += ":{"+endconstr+"}"; }
			if (structconstr!=null) { fixed2view += ":"+structconstr; }
                    }
                    else {
                        boolean firstor = true;
                        fixed2view += "(";
                        for(int j=0;j<fixedElts.get(i).length;j++) {
                        if(! firstor) { fixed2view += "|"; }
                        fixed2view += modifier+"\""+fixedElts.get(i)[j]+"\"";
                        if(first && beginconstr!=null) { fixed2view += ":{"+beginconstr+"}"; }
			if(i==fixedElts.size()-1 && endconstr!=null) { fixed2view += ":{"+endconstr+"}"; }
			if (structconstr!=null) { fixed2view += ":"+structconstr; }
                        firstor = false;
                        }
                        fixed2view += ")";
                    } 
                    first = false;
                }
                fixed2view += ")";

		if(complexstringElts.length>1) {
			for(int i=1;i<complexstringElts.length;i++) {
				fixed2view += ":"+complexstringElts[i];
			}
		}

		return fixed2view;

	}

	/**
	* Parse the grammar file and performs its analysis to generate prolog
	*
	*/
	public static void analyse() throws IOException, GrammarException {

	String grammarText = null;
   	BufferedReader br = new BufferedReader(new FileReader(grammarFile));
    	try {
        StringBuilder sb = new StringBuilder();
        String line = br.readLine();

        while (line != null) {
            sb.append(line);
            sb.append('\n');
            line = br.readLine();
        }
        grammarText = sb.toString();
    	} finally {
        	br.close();
    	}
	grammarText = searchForComplexStrings(grammarText);
	logger.debug("new grammar "+grammarText);


	//File fg = new File(grammarFile);
    	//InputStream fileGrammar = new BufferedInputStream(new FileInputStream(fg));
	InputStream fileGrammar = new ByteArrayInputStream(grammarText.getBytes());
	ANTLRInputStream input = new ANTLRInputStream(fileGrammar);
	logolLexer lexer = new logolLexer(input);
	CommonTokenStream tokens = new CommonTokenStream(lexer);
	logolParser parser = new logolParser(tokens);
	
	
	logger.debug("Start variable analysis step");
	// 1 : read variable, see where they are defined
	Treatment.setParseStep(Constants.VAR_ANALYSIS_STEP);
	try {
		parser.rule();
	} catch (RecognitionException e) {
		logger.error("Error in parsing: "+e.getMessage());
		return;
	} catch (Exception e) {
		logger.error("Error in parsing: "+e.getMessage());
		throw new GrammarException("Parsing error: "+e.getMessage());
	}


	logger.debug("Start conditions analysis step");
	// 2 : try a model to analyse variable definitions
	Treatment.setParseStep(Constants.POSTCONDITION_ANALYSIS_STEP);
	parser.reset();
	try {
		parser.rule();
	} catch (RecognitionException e) {
		logger.error("Error in parsing: "+e.getMessage());
		return;
	}
	catch (Exception e) {
	logger.error("Error in parsing: "+e.getMessage());
	throw new GrammarException("Parsing error: "+e.getMessage());
 	}	
	treatModelParameters();
	generatePreAnalysis();
	
	
	logger.debug("Create grammar content");
	// 3 : create content
	Treatment.setParseStep(Constants.EXECUTE_STEP);
	parser.reset();
	try {
		parser.rule();
	} catch (RecognitionException e) {
		logger.error("Error in parsing: "+e.getMessage());
		return;
	}
	catch (Exception e) {
	logger.error("Error in parsing: "+e.getMessage());
	throw new GrammarException("Parsing error: "+e.getMessage());
	}
	
	
	// Generate Logol file
	logger.debug("Generate prolog file");
	generateFile();

	// compile, CFile, makeExe not needed anymore since use of logol.exe and preanalyse.exe.
	// prolog file are loaded dynamically.
	
	// Compile and save state
	//logger.debug("Compile and save state of prolog file");
	//compile();
	
	// Prepare C main file
	//logger.debug("Generate C file that will call prolog");
	//createCFile();		
	
	// create executable
	//logger.debug("Create executable");
	//makeExe();
		
	
		
	}

/**
 * Clean intermediate generated files once treatment is over.
 * @param jobFiles List of files generated when split or reversed
 */
private static void clean(Vector<String> jobFiles) {
	String savFile = Treatment.workingDir+System.getProperty(FILESEPARATORPROPERTY)+Treatment.filename+"."+Treatment.uID+".sav";
	String proFile = Treatment.workingDir+System.getProperty(FILESEPARATORPROPERTY)+Treatment.filename+"."+Treatment.uID+".pro";
	String cFile = Treatment.workingDir+System.getProperty(FILESEPARATORPROPERTY)+Treatment.filename+"."+Treatment.uID+".c";
	String exeFile = Treatment.workingDir+System.getProperty(FILESEPARATORPROPERTY)+Treatment.filename+"."+Treatment.uID+".exe";	
	
	File tmpFile = new File(savFile);
	tmpFile.delete();
	tmpFile = new File(proFile);
	tmpFile.delete();
	tmpFile = new File(cFile);
	tmpFile.delete();
	tmpFile = new File(exeFile);
	tmpFile.delete();
	
	
	for(int i=0;i<jobFiles.size();i++) {
		tmpFile = new File(jobFiles.get(i)+".xml");
		tmpFile.delete();
		tmpFile = new File(jobFiles.get(i)+".fasta");
		tmpFile.delete();
		if(Treatment.dataType==Constants.DNA || Treatment.dataType==Constants.RNA) {
			tmpFile = new File(jobFiles.get(i)+".fasta.index.dna");
			tmpFile.delete();
		}
        // Delete pattern related file, we have not their id yet, so loop for
        // the moment
        for(int j=0;j<1000;j++) {
          tmpFile = new File(jobFiles.get(i)+".fasta.tmp_"+j+".fsa");
          if(tmpFile.exists()) {
              tmpFile.delete();
              tmpFile = new File(jobFiles.get(i)+".fasta.tmp_"+j+".fsa.cass.out");
              tmpFile.delete();
          }
        }
		switch(Logol.suffixTool) {
		case 0:
			tmpFile = new File(jobFiles.get(i)+".fasta.index.sfx");
			tmpFile.delete();		
			tmpFile = new File(jobFiles.get(i)+".fasta.index.sfc");
			if(tmpFile.exists()) {
			tmpFile.delete();	
			}
			break;
		case 1:
			tmpFile = new File(jobFiles.get(i)+".fasta.al1");
			tmpFile.delete();
			tmpFile = new File(jobFiles.get(i)+".fasta.bck");
			tmpFile.delete();
			tmpFile = new File(jobFiles.get(i)+".fasta.bwt");
			tmpFile.delete();
			tmpFile = new File(jobFiles.get(i)+".fasta.des");
			tmpFile.delete();
			tmpFile = new File(jobFiles.get(i)+".fasta.lcp");
			tmpFile.delete();
			tmpFile = new File(jobFiles.get(i)+".fasta.llv");
			tmpFile.delete();
			tmpFile = new File(jobFiles.get(i)+".fasta.ois");
			tmpFile.delete();
			tmpFile = new File(jobFiles.get(i)+".fasta.prj");
			tmpFile.delete();
			tmpFile = new File(jobFiles.get(i)+".fasta.sds");
			tmpFile.delete();
			tmpFile = new File(jobFiles.get(i)+".fasta.skp");
			tmpFile.delete();
			tmpFile = new File(jobFiles.get(i)+".fasta.sti1");
			tmpFile.delete();
			tmpFile = new File(jobFiles.get(i)+".fasta.suf");
			tmpFile.delete();
			tmpFile = new File(jobFiles.get(i)+".fasta.tis");
			tmpFile.delete();
			break;
		case 2:
		default:
			tmpFile = new File(jobFiles.get(i)+".fasta.cass");
			tmpFile.delete();
			tmpFile = new File(jobFiles.get(i)+".fasta.cass.cass.idx");
			tmpFile.delete();
            tmpFile = new File(jobFiles.get(i)+".fasta.cass.cass.meta");
            tmpFile.delete();
			break;
		}
		if(Logol.suffixTool==0) {
			tmpFile = new File(jobFiles.get(i)+".fasta.index.sfx");
			tmpFile.delete();		
			tmpFile = new File(jobFiles.get(i)+".fasta.index.sfc");
			if(tmpFile.exists()) {
			tmpFile.delete();	
			}
		}
		else {
		tmpFile = new File(jobFiles.get(i)+".fasta.al1");
		tmpFile.delete();
		tmpFile = new File(jobFiles.get(i)+".fasta.bck");
		tmpFile.delete();
		tmpFile = new File(jobFiles.get(i)+".fasta.bwt");
		tmpFile.delete();
		tmpFile = new File(jobFiles.get(i)+".fasta.des");
		tmpFile.delete();
		tmpFile = new File(jobFiles.get(i)+".fasta.lcp");
		tmpFile.delete();
		tmpFile = new File(jobFiles.get(i)+".fasta.llv");
		tmpFile.delete();
		tmpFile = new File(jobFiles.get(i)+".fasta.ois");
		tmpFile.delete();
		tmpFile = new File(jobFiles.get(i)+".fasta.prj");
		tmpFile.delete();
		tmpFile = new File(jobFiles.get(i)+".fasta.sds");
		tmpFile.delete();
		tmpFile = new File(jobFiles.get(i)+".fasta.skp");
		tmpFile.delete();
		tmpFile = new File(jobFiles.get(i)+".fasta.sti1");
		tmpFile.delete();
		tmpFile = new File(jobFiles.get(i)+".fasta.suf");
		tmpFile.delete();
		tmpFile = new File(jobFiles.get(i)+".fasta.tis");
		tmpFile.delete();
		}
	}
	
	}



	/**
	 * Transform generated xml file in a more appropriate format for analyse (reorder variables according to position...)
	 * @param inFile generated xml file
	 * @param xsltFile stylesheet to use
	 * @param mergeOutputStream new xml file
	 * @throws TransformerException
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 */
	//private static void transformXSLT(String inFile, String xsltFile, String outFile,String id) throws FileNotFoundException, TransformerException {
		
	private static int transformXSLT(String inFile, String xsltFile,
				PrintWriter mergeOutputStream, String id) throws TransformerException, ParserConfigurationException, SAXException, IOException, OutOfMemoryError {

		logger.debug("Transform xml with stylesheet");

		// 1. Instantiate a TransformerFactory.
		javax.xml.transform.TransformerFactory tFactory = javax.xml.transform.TransformerFactory.newInstance();

		// 2. Use the TransformerFactory to process the stylesheet Source and generate a Transformer.
		javax.xml.transform.Transformer transformer = tFactory.newTransformer
		                (new javax.xml.transform.stream.StreamSource(xsltFile));

		File lv_file = new File(inFile);
		String lv_filename = lv_file.getName();

		// if it is a reverse sequence operation, need to recalculate begin/end according to left2right reading.
		Pattern p = Pattern.compile("\\.reverse\\.(\\d+)\\-(\\d+)\\.xml");
		Matcher m = p.matcher(lv_filename);
		long lv_offset=0;
		if(m.find()) {				
				long start = Long.valueOf(m.group(1));
				long end = Long.valueOf(m.group(2));
				// Fix 1330 offset
				//lv_offset =end + offset - start;
				lv_offset = end;
			
		}
		
		
		// Set offset parameter for reverse case (reset begin/pos positions)
		transformer.setParameter("offset", lv_offset);
		transformer.setParameter("fileid", id);
		// 3. Use the Transformer to transform an XML Source and send the output to a Result object.	
		transformer.transform
	    (new javax.xml.transform.stream.StreamSource(inFile), 
	     new javax.xml.transform.stream.StreamResult( mergeOutputStream));		
		
	    mergeOutputStream.flush();
		
	    /*
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document document = builder.parse(new File(inFile));
		NodeList matchNodeList =  XPathAPI.selectNodeList(document,"/sequences/match");
		if(matchNodeList==null) {
			return 0;
		}		
		return matchNodeList.getLength();
		*/
	    //Fix1768
	    
	    /*
	    String jaxpPropertyName = "javax.xml.parsers.SAXParserFactory";
	      // Pass the parser factory in on the command line with
	      // -D to override the use of the Apache parser.
	      if (System.getProperty(jaxpPropertyName) == null) {
	        String apacheXercesPropertyValue =  "org.apache.xerces.jaxp.SAXParserFactoryImpl";
	        System.setProperty(jaxpPropertyName, apacheXercesPropertyValue);
	      }
	      XmlCountHandler handler = new XmlCountHandler("match");
	      SAXParserFactory factory = SAXParserFactory.newInstance();
	      try {
	        SAXParser parser = factory.newSAXParser();
	        parser.parse(inFile, (DefaultHandler)handler);
	      } catch(Exception e) {
	    	  logger.error(e.getMessage());
	      }
	      logger.info("Found "+handler.getTotalCount()+" elements in xml file "+inFile);
	      return handler.getTotalCount();
	      */
		return LogolUtils.countMatch(inFile);
		
	}
	
	
	
	/**
	 * Create an executable from C file and prolog sav file
	 * @throws IOException
	 */
	@Deprecated
	private static void makeExe() throws IOException {
		// Generate executable: spld --main=user logol.c -o MyGeneratedGrammarFile.exe
		String cFile = Treatment.workingDir+System.getProperty(FILESEPARATORPROPERTY)+Treatment.filename+"."+Treatment.uID+".c";
		String exeFile = Treatment.workingDir+System.getProperty(FILESEPARATORPROPERTY)+Treatment.filename+"."+Treatment.uID+".exe";
		
		executable = exeFile;
		
		Runtime runtime = Runtime.getRuntime();        
        Process prolog = runtime.exec(new String ("spld --main=user "+cFile+" -o "+exeFile) );
        try {
        	PrintStream os = new PrintStream(prolog.getOutputStream());
        	os.println("");
        	os.flush();
        	        	
        	StreamGobbler s1 = new StreamGobbler ("stdin", prolog.getInputStream ());
        	StreamGobbler s2 = new StreamGobbler ("stderr", prolog.getErrorStream ());
        	s1.start ();
        	s2.start ();
        	logger.debug("Executable ready");
			prolog.waitFor();
			
			os.close();
	
		} catch (InterruptedException e) {
			logger.error("could not create executable "+ e.getMessage());
		}
	
		
	}


	/**
	 * Compile prolog and create a saved-state file in .sav.
	 * @throws IOException
	 */
	@Deprecated
	private static void compile() throws IOException {
		//sicstus --goal "set_prolog_flag(syntax_errors,quiet),compile('MyGeneratedGrammarFile'),save_program('MyGeneratedGrammarFile.sav'), halt."
		String savFile = Treatment.workingDir+System.getProperty(FILESEPARATORPROPERTY)+Treatment.filename+"."+Treatment.uID+".sav";
		String proFile = Treatment.workingDir+System.getProperty(FILESEPARATORPROPERTY)+Treatment.filename+"."+Treatment.uID+".pro";
		
		proFile = proFile.replace('\\', '/');
		savFile = savFile.replace('\\', '/');
		
		Runtime runtime = Runtime.getRuntime();        
        Process prolog = runtime.exec(new String ("sicstus") );
        try {
        	PrintStream os = new PrintStream(prolog.getOutputStream());
        	os.println("set_prolog_flag(syntax_errors,quiet),compile('"+proFile+"'),save_program('"+savFile+"'), halt.");
        	os.flush();
        	        	
        	StreamGobbler s1 = new StreamGobbler ("stdin", prolog.getInputStream ());
        	StreamGobbler s2 = new StreamGobbler ("stderr", prolog.getErrorStream ());
        	s1.start ();
        	s2.start ();
        	logger.debug("Saving state");
			prolog.waitFor();
			
			os.close();
		
			

			
		} catch (InterruptedException e) {
			logger.error("could not save prolog state "+ e.getMessage());
		}   
		
		
	}


	/**
	 * Generate the C file corresponding to actual treatment
	 * @throws IOException
	 */
	@Deprecated
	private static void createCFile() throws IOException {
		String data="";
		String cFile = Treatment.workingDir+System.getProperty(FILESEPARATORPROPERTY)+Treatment.filename+"."+Treatment.uID+".c";
		String savFile = Treatment.workingDir+System.getProperty(FILESEPARATORPROPERTY)+Treatment.filename+"."+Treatment.uID+".sav";
		
		cFile = cFile.replace('\\', '/');
		savFile = savFile.replace('\\', '/');
		
        BufferedReader input =  new BufferedReader(new FileReader("prolog/logol.c"));
          String line = null; 
          while (( line = input.readLine()) != null){	
        	  data+=line+"\n";
          }
		
         input.close();
          
          data = data.replaceAll("SP_restore\\(\"logol.sav\"\\)", "SP_restore(\""+savFile+"\")");
          

		File fg = new File(cFile);
        PrintStream fileanalysis;
        fileanalysis = new PrintStream(new FileOutputStream(fg));

        fileanalysis.print(data);        
        fileanalysis.close();	
		
	}


	/**
	 * Manage model parameters. If a variable is used in model and is not defined in current predicate, then it is a parameter of the current predicate.
	 */
	private static void treatModelParameters() {
		for(int i=0;i<Treatment.models.size();i++) {
			Model mod = Treatment.models.get(i);
			for(int j=0;j<mod.vars.size();j++) {
				String varName = (String)mod.vars.get(j);				
				if(!LogolVariable.userVariables.containsKey(new VariableId(varName,mod.name))&&!LogolVariable.parentVariables.containsKey(new VariableId(varName,mod.name))) {
					// This is a model input, add it as a param
					int tmpcounter = (Integer) Treatment.counters.get(0);
					LogolVariable.paramVariables.put(new VariableId(varName,mod.name), Integer.toString(tmpcounter));
					LogolVariable.matchedVariables.add(varName);
					tmpcounter++;
					Treatment.counters.put(0,tmpcounter);
			
				}
			}					
		}
		
	}
	
	
	/**
	 * generate pre-analysis file to know which variable are known at which time. This is required to postpone some treatment at the time of variable analysis.
	 * @throws IOException
	 */
	private static void generatePreAnalysis() throws IOException {
		String data="";		
				
		data+="% ** Main query ************\n";
		//data+=":-dynamic fixed/1.\n";
		data+=":-dynamic currentstream/1.\n";
		data+="\n";
		
		data+="% Loop over variables\n";
		//To use global exe, add a dummy query4match that will call preanalyse
		data+="query4match(Dummy1,Dummy2,Dummy3,Res):-preanalyse(Res).\n";
		
		String preanalysisResultFile = Treatment.workingDir+System.getProperty("file.separator")+Treatment.filename+"."+Treatment.uID+".pre.res";
		preanalysisResultFile=preanalysisResultFile.replace("\\", "/");
		
		data+="preanalyse(Loop):- Loop=1,open('"+preanalysisResultFile+"',append,OutStream),assert(currentstream(OutStream))";
		
		Integer id;
		
		for(int i=0;i<LogolVariable.variables.size();i++) {
			id = (Integer)LogolVariable.variables.get(i);
			data+=",assert(fixed("+id+"))";
			data+=",query(_)";
			data+=",retract(fixed("+id+"))";
		}		
		data+=",close(OutStream),retractall(currentstream(_)).\n";
		
		data+=Treatment.query+"\n\n";

		data+="% **************************\n\n";

		data+="% ** Internal predicates ***\n";

		for(int i=0;i<ViewVariable.getPredicateArraySize();i++) {
			data+=ViewVariable.getPredicate(i)+"\n\n";			
		}
		
		data+="% **************************\n\n";
		
						
		data+="% ** MODELS DEFINITION *****\n";

		
		
		Model mod = null;
		for(int i=0;i<Treatment.models.size();i++) {
			mod = Treatment.models.get(i);
			data+="% "+mod.text+"\n";
			data+=mod.name+"(";
			
			
			if(mod.vars.size()==0) {
				data+="LOGOLVARDUMMY";
			}
			for(int j=0;j<mod.vars.size();j++) {
				if(j>0)  {data+=","; }				
				data+=mod.vars.get(j);
			}
			data+= ") :- currentstream(OutStream)," + mod.predicate + ".\n\n";
		}		
		
		data+="% **************************\n\n";
		
				
		String preanalysisFile = Treatment.workingDir+System.getProperty(FILESEPARATORPROPERTY)+Treatment.filename+"."+Treatment.uID+".pre";
		File fg = new File(preanalysisFile);
        PrintStream fileanalysis = new PrintStream(new FileOutputStream(fg));
        fileanalysis.print(data);        
        fileanalysis.close();
        
        logger.debug("Execute pre analyse");        

		// execute something like sicstus -l preanalysisResultFile --goal "preanalyse(L),halt."
		// read content of file and load variableOrganization according to results
        Runtime runtime = Runtime.getRuntime();

        Process prolog = runtime.exec(new String (installPath+System.getProperty(FILESEPARATORPROPERTY)+"prolog"+System.getProperty(FILESEPARATORPROPERTY)+"preanalyse.exe "+preanalysisFile+" "+savfile) );
        //Process prolog = runtime.exec(new String ("sicstus -l "+preanalysisFile+" ") );
        
        try {
        	PrintStream os = new PrintStream(prolog.getOutputStream());
        	//os.println("preanalyse(L),halt.");
        	os.println("");
        	os.flush();
        	        	
        	StreamGobbler s1 = new StreamGobbler ("stdin", prolog.getInputStream ());
        	StreamGobbler s2 = new StreamGobbler ("stderr", prolog.getErrorStream ());
        	s1.start ();
        	s2.start ();
        	System.out.println("Executing prolog for pre-analyse");
			prolog.waitFor();
			
			os.close();
		
			

			
		} catch (InterruptedException e) {
			logger.error("could not execute prolog analysis "+ e.getMessage());
		}      
		
		// Delete analysis file now
		logger.debug("Delete preanalyse file");
		if(!noClean) {
			fg.delete();
		}
		
        loadVariables2Postpone(preanalysisResultFile);

		
	}

	/**
	 * Read preanalysis result information to see which variable treatments need to be postponed
	 * @param preanalysisResultFile name of result file
	 */
	private static void loadVariables2Postpone(String preanalysisResultFile) {

		File pfile = new File(preanalysisResultFile);
  		Pattern p = Pattern.compile("(\\w+\\d+),(\\d+),(\\d+),(\\w+\\d+),(\\d+)");
  		Matcher m=null;
  		
	    try {
	        //use buffering, reading one line at a time
	        //FileReader always assumes default encoding is OK!
	        BufferedReader input =  new BufferedReader(new FileReader(pfile));
	        try {
	          String line = null; 
	          String inMod= null;
	          String inVar= null;
	          String inType= null;
	          String outMod= null;
	          String outVar= null;
	          while (( line = input.readLine()) != null){
	    		m = p.matcher(line);
	    		while(m.find()) {		    			
	    				 inMod=m.group(1);
	    				 inVar=m.group(2);
	    				 inType=m.group(3);
	    				 outMod=m.group(4);
	    				 outVar=m.group(5);

	    			// Add variable and constraint type to known treatments to postpone.
	    			// At variable analysis time, appropriate constraint will be postponed based on this.	    				 	    				 
	    				 LogolVariable.constrainedVariables.put(new VariableId(inMod,inVar,inType),new VariableId(outMod,outVar));
	    		}
	        	  
	          }
	        }
	        finally {
	          input.close();
	        }
	      }
	      catch (IOException ex){
	        ex.printStackTrace();
	      }
		
		logger.debug("postponed variables loaded, now delete file");
		if(!noClean) {
			pfile.delete();
		}
		
		
		
	}

	/**
	 * Generate prolog file for the current grammar
	 * @throws FileNotFoundException
	 * @throws GrammarException 
	 */
	private static void generateFile() throws FileNotFoundException, GrammarException {
		String data="";
		String init="";
		
		//data+=":- ensure_loaded('"+installPath+System.getProperty(FILESEPARATORPROPERTY)+"prolog"+System.getProperty(FILESEPARATORPROPERTY)+"logol.pro').\n";
		//data+=":- use_module(library(process)).\n\n";
		
		data+="% ** USAGE *******\n";
		data+="% Call predicate query4match(File,OutFile,Offset,Res)\n";
		data+="% File is input sequence\n";
		data+="% OutFile is the xml result file\n";
		data+="% Offset is the offset when main file is splitted\n";
		data+="% Never call query4match several times within a single context, some clauses are not deleted and would cause failure of next queries\n";
		data+="% **************************\n\n";
		
		data+="% ** INIT OF DATA *******\n";

		for(int i=0;i<Treatment.definitions.size();i++) {
			data+="% "+Treatment.definitions.get(i)+"\n\n";	
			init+="\tassert("+Treatment.definitions.get(i)+"),\n";
		}
		// Set config
		String optimal="0";
		if(Treatment.isOptimalConstrainted()) {
			optimal="1";
		}
		init+="\tassert(config([0,"+optimal+"])),\n";
		data+="% config([0,"+optimal+"])\n";
		
		data+="% **************************\n\n";		
		
		
		data+="% ** INFO ***************\n";
		data+="% Maximum result size of a record = "+Treatment.maxResultSize+"\n";
		data+="% Identifier = "+Treatment.uID+"\n";
		data+="% Work directory = "+Treatment.workingDir+"\n";		
		data+="% Maximum length of an instance for a match = "+Treatment.maxLength+"\n";
		data+="% **************************\n\n";
		
		
		Set<Integer> keys = Treatment.varInfo.keySet();
		
		
		Iterator<Integer> it = keys.iterator();
		
		TreeSet<Integer> orderedKeys = new TreeSet<Integer>();
		while(it.hasNext()) {
			orderedKeys.add((Integer)it.next());
		}		
		it = orderedKeys.iterator();
		
		data+="% ** VAR DEFINITIONS *******\n";
		while(it.hasNext()) {
			Integer key = (Integer)it.next();
			LogolVariable lvar = (LogolVariable) Treatment.varInfo.get(key);
			//LogolVariable lvar = (LogolVariable) LogolVariable.varData.get(key);
			data+="% Model "+lvar.model+" : "+Constants.LOGOLVAR+key+" : "+lvar.text+", parent = "+Constants.LOGOLVAR+lvar.parentId+"\n";
		}
		data+="% **************************\n\n";
		
		
		data+="% ** META CONTROLS *********\n";
		data+=Treatment.getMetaControls();	
		data+="% **************************\n\n";
		
		String ruby_path = "";
		/*
		try {
			ruby_path = System.getProperty("ruby_path");
			if(ruby_path==null) {
				ruby_path = "";
			}
		}
		catch(Exception e) {
			// That's fine
		}
		*/
		
		if(OS.startsWith("win")) {
			data+=":-assert(logolShell('"+ruby_path+"ruby.exe')).\n";
		}
		else {
			data+=":-assert(logolShell('"+ruby_path+"ruby')).\n";
		}
		
		data+="% ** Main query ************\n";
		data+=Treatment.query+"\n\n";

		data+="% **************************\n\n";
		
		data+="% ** INTERMEDIATE PREDICATES DEFINITION *******\n";
		
		for(int i=0;i<ViewVariable.predicates.size();i++) {
			data+=ViewVariable.predicates.get(i)+"\n\n";			
		}
		data+="% **************************\n\n";

						
		data+="% ** MODELS DEFINITION *****\n";
	
		// Get in each model a list of all references, add it as output param (Res1,..). 
		
		Model mod = null;
		for(int i=0;i<Treatment.models.size();i++) {
			mod = Treatment.models.get(i);
			data+="% "+mod.text+"\n";
			data+=mod.name+"(Input";
			
			data+=",PostponedVariables";
			data+=","+mod.name.toUpperCase()+"_"+Constants.POSTPONED;		
						
			
			for(int j=0;j<mod.vars.size();j++) {
				data+=",";
				if(LogolVariable.userVariables.containsKey(new VariableId((String)mod.vars.get(j),mod.name)))
					{ data+=Constants.LOGOLVARREF+LogolVariable.userVariables.get(new VariableId((String)mod.vars.get(j),mod.name)); }
				if(LogolVariable.paramVariables.containsKey(new VariableId((String)mod.vars.get(j),mod.name)))
					{ data+=Constants.LOGOLVARREF+LogolVariable.paramVariables.get(new VariableId((String)mod.vars.get(j),mod.name));	}		
			}
			
			data+=",Parent";
			data+=","+mod.name.toUpperCase();

			String output = Constants.LOGOLVARAFTER+LogolUtils.getPredicateOutput(mod.predicate);
			data+= ",[Errors,Indel],Output) :- " + mod.predicate + ", Output="+ output;

			data+= ","+mod.name.toUpperCase()+"="+LogolUtils.getAllSavedVariables(mod.predicate,-1,mod.name);
			
			data+= ","+mod.name.toUpperCase()+"_"+Constants.POSTPONED+"="+LogolVariable.getPostponedVariableList(mod.name);
			
			// ** Compute cost for model
			HashSet<String> vars = LogolUtils.getErrorVariables(mod.predicate);
			
			Iterator<String> itVars = vars.iterator();
			String elt = itVars.next();
			String list = "[" + elt;
			String listindel = "[" + elt.replaceAll(Constants.LOGOLVARERRORS, Constants.LOGOLVARINDEL);
			while(itVars.hasNext()) {
				elt = itVars.next();
				list+=","+elt;
				listindel+=","+elt.replaceAll(Constants.LOGOLVARERRORS, Constants.LOGOLVARINDEL);
			}
			list+="]";
			listindel+="]";
			
			data += ",computeCost("+list+",Errors)"+",computeCost("+listindel+",Indel)";
			//  ***************
			
			
			data+=".\n\n";
		}		
		
		data+="% **************************\n\n";
		
		data+="% ** MAIN ******************\n";
		
		data+="query4match(File,OutFile,Offset,Res):-\n";
		data+=init+"\n";
		data+="\tbb_put('matchCounter',1),\n";
		data+="\tassert(offset(Offset)),\n";
		data+="\tassert(maxsize("+ Treatment.maxResultSize +")),\n";
		data+="\tassert(outputfile(OutFile)),\n";
				 
		data+="\t!,\n";
		// Call the models and get the variable references as result. Each match will be written to file OutFile		
		//data+="\tcatch(query(Res),Err,format('ERROR: An error occured during execution\n',[]))\n";						
		data+="\tquery(Res)\n";
		data+=".\n";		
		data+="runtime_entry(Res):-\n";	
		data+="\tprolog_flag('argv',Args),nth0(0,Args,File),\n";
		data+="\tprolog_flag('argv',Args),nth0(1,Args,OutFile),\n";
		data+="\tprolog_flag('argv',Args),nth0(2,Args,Offset),\n";
		data+="\tquery4match(File,OutFile,Offset,Res)\n";
		data+=".\n";
		
		if(Treatment.dataType==Constants.PROTEIN) {
			// redefine is equal if protein
			data+=":-abolish(isequal/2).\n";
			data+="isequal(A,B):-A=B;isequalgeneric(A,B).\n";
		}
		/*
		else {
			// not needed, set by default
			data+="isequal(A,B):-isequalnuc(A,B);isequalgeneric(A,B).\n";
		}
		*/
		
		
		data+="% **************************\n\n";
		
		String prologResultFile = Treatment.workingDir+System.getProperty(FILESEPARATORPROPERTY)+Treatment.filename+"."+Treatment.uID+".pro";
		File fg = new File(prologResultFile);
        PrintStream fileanalysis = new PrintStream(new FileOutputStream(fg));
        fileanalysis.print(data);        
        fileanalysis.close();		
		
        logger.debug("Wrote file "+prologResultFile);
        
	}


	/**
	 * Return output sequenmax
	 * ce name
	 * @return output sequence name
	 */
	public static String getOutputSeqName() {
		return outputSeqName;
	}


	/**
	 * Sets output sequence name
	 * @param outputSeqName the output sequence name
	 */
	public static void setOutputSeqName(String outputSeqName) {
		Logol.outputSeqName = outputSeqName;
	}

	public static String getGrammarFile() {
		return grammarFile;
	}

	public static void setGrammarFile(String grammarFile) {
		Logol.grammarFile = grammarFile;
	}

	public static String getFilterType() {

		if(filterType == FILTER.EXACT) {
			return "mapListExact";
		}
		if(filterType == FILTER.GLOBAL) {
			return "mapListGlobal";
		}
		if(filterType == FILTER.LOCAL) {
			return "mapListLocal";
		}
		if(filterType == FILTER.LOCAL0) {
			return "mapListLocal0";
		}
		
		return "mapListExact";
	}

}
