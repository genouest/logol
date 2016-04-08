package org.irisa.genouest.logol.dispatcher;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.MessagingException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.apache.xerces.impl.dv.util.Base64;
import org.apache.xpath.XPathAPI;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.SequenceIterator;
import org.biojava.bio.seq.io.SeqIOTools;
import org.biojavax.SimpleNamespace;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;
import org.biojavax.bio.seq.RichSequence.IOTools;
import org.irisa.genouest.logol.Constants;
import org.irisa.genouest.logol.GrammarException;
import org.irisa.genouest.logol.Logol;
import org.irisa.genouest.logol.Treatment;
import org.irisa.genouest.logol.utils.LogolUtils;
import org.irisa.genouest.logol.utils.converter.FastaConverter;
import org.irisa.genouest.logol.utils.converter.GFFConverter;
import org.irisa.genouest.logol.utils.model.ModelConverter;
import org.irisa.genouest.logol.utils.model.ModelDefinitionException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;






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
	if (s.startsWith("ERROR")) {
		logger.error(s);
	}
	else {
		logger.debug(s);
	}
	}

	is.close ();

	} catch (IOException ex) {
	logger.error("Problem reading stream " + name + "... :" + ex);
	ex.printStackTrace ();
	}
	}
	}


class SubSequence {

	public String name=null;
	public int wholeLength=0;
	public int offset=0;
	public int sequenceID=1;
	public int start=0;
	public int length=0;
	public int maxMatchStart=0;

	public SubSequence() {

	}

}


/**
 * This class takes a bank as input, generate 1 file per sequence in bank then execute main Logol program for each sequence.
 * Job is submitted to a JobManager.
 * @author osallou
 *
 * History: 26/03/09 Fix 1271 Add error email
 * 			20/04/09 Fix 1320 Max solution not correct if -max not used
 * 			22/04/08 Fix 1327 Check directories existence and add -out option
 * 			20/05/09 Fix 1365 Add filtering to keep singletons only
 * 			12/06/09 Fix 1337 support relative path
 *          18/02/10 Fix 1576 Change email subject
 *			21/07/10 Fix 1660 OutofMemory issue with Biojava
 *			31/07/12 Fix If out option is used, used outfilename in mail
 *
 * Known issues in DMR mode: configuration file and model do not support relative path, only sequence path does support it.
 * This issue is due to the fact that when job is transfered on a node, the path is not the same and is system dependent. The software does not change this directory (TODO)
 *
 */
public class Dispatch {

	private static final Logger logger = Logger.getLogger(org.irisa.genouest.logol.dispatcher.Dispatch.class);

	private static final String FILESEPARATORPROPERTY = "file.separator";

	private static final int LOCAL=0;
	private static final int SGE=1;

	private static int type = LOCAL;

	private static String bank="";

	private static String configFile="";

	private static int format=1; // FASTADNA, 2 = RNA , 3 = PROTEIN

	private static String workDir=".";

	private static JobManager jobmngr = (JobManager) new LocalJobManager();;

	private static String jobArgs="";

	private static boolean isContig=false;

	private static String installDir = null;

	private static String guid=null;

	private static boolean noclean=false;

	private static String email=null;

	private static PropertiesConfiguration config=null;

	private static final String mailTemplatePath = "prolog/mail.tpl";

	/**
	 * Number of processors on computer when inn local mode
	 */
	private static int nbproc = 1;

	/**
	 * Max number of jobs to run in parallel for clusters. 0 is no limit.
	 */
	private static int nbjobs=0;

	private static String grammar=null;
	private static String model=null;

	private static int maxMatchSize=0;
	private static int minSplitSize=0;

	private static int maxSolutions=0;

	private static boolean noClean=false;

	private static String fromMail="do.not.reply";

	private static String zipFileName=null;

	private static boolean forceSplittingUsage = false;

	private static boolean convert2fasta=false;

	private static boolean convert2gff=false;

	/**
	 * Entry point
	 * @param args Arguments with bank, config file...
	 * @throws IOException
	 * @throws Exception
	 */
	public static void main(String[] args) throws IOException {

		Options options = new Options();

		 // add command line options
		 options.addOption("v",false,"get version");
		 options.addOption("h",false,"get usage");

		 options.addOption("s", true, "sequence database");
		 options.addOption("sge", false, "use SGE");
		 options.addOption("local", false, "use local system");
		 options.addOption("dna", false, "analyse dna, is default");
		 options.addOption("rna", false, "analyse rna");
		 options.addOption("protein", false, "analyse protein");
		 options.addOption("conf", true, "configuration file");
		 options.addOption("contig", false, "set the bank as contig sequences");
		 options.addOption("guid", true, "unique identifier for the query");
		 options.addOption("noclean", false, "Do not delete created files after treatment");
		 options.addOption("email", true, "Email address to send result availability info");
		 options.addOption("g", true, "grammar file to analyse");
		 options.addOption("max", true, "maximum returned solutions");
		 options.addOption("maxres", true, "maximum result size of a match");
		 options.addOption("maxmatchsize", true, "maximum size of a match");
		 options.addOption("m", true, "model file to analyse");
		 options.addOption("all",false,"analyse both directions of the sequence");
		 options.addOption("maxspacer", true, "maximum size of a spacer");
		 options.addOption("lmax", true, "maximum length of a variable");
		 options.addOption("lmin", true, "minimum length of a variable");
		 options.addOption("out", true, "Zip output file name");
		 //Fix 1365
		 options.addOption("enabledups", false, "enable duplicate result matches. By default only keep singletons");

		 // To support logol options
		 options.addOption("output", true, "output file name, must be unique");
		 options.addOption("forcesplit", false, "Force the sequence splitting according to parameters and whatever is the number of model used in grammar");
		 options.addOption("fasta",false,"Add fasta conversion to result archive");
		 options.addOption("gff",false,"Add gff conversion to result archive");
		 options.addOption("filter",true,"Type of filter when enabledups is not enabled. Allowed types are exact,local,local0,global.");


		 CommandLineParser cparser = new PosixParser();
		 CommandLine cmd=null;
		try {
			cmd = cparser.parse( options, args);
		} catch (ParseException e) {
			displayError(e.getMessage());
			System.exit(1);
		}

		System.out.println("For help, use option -h");

		 if(cmd.hasOption("v")) {
	        	Class<Dispatch> clazz = Dispatch.class;
	        	String classContainer = clazz.getProtectionDomain().getCodeSource().getLocation().toString();
	        	URL manifestUrl = new URL("jar:" + classContainer + "!/META-INF/MANIFEST.MF");
	        	Manifest mf = new Manifest(manifestUrl.openStream());
	        	Attributes atts = mf.getMainAttributes();

	        	System.out.println("Logol version: "+atts.getValue("Implementation-Version")+"-"+atts.getValue("Implementation-Build"));
			 System.exit(0);
		 }
		 if(cmd.hasOption("h")) {
				Options usageOptions = new Options();
				usageOptions.addOption("v",false,"get version");
				usageOptions.addOption("h",false,"get usage");
				usageOptions.addOption("guid", true, "unique identifier for the query");
				usageOptions.addOption("s", true, "sequence database");
				usageOptions.addOption("g", true, "grammar file to analyse");
				usageOptions.addOption("m", true, "model file to analyse");
				usageOptions.addOption("sge", false, "use SGE");
				usageOptions.addOption("local", false, "use local system");
				usageOptions.addOption("dna", false, "analyse dna, is default");
				usageOptions.addOption("rna", false, "analyse rna");
				usageOptions.addOption("protein", false, "analyse protein");
				usageOptions.addOption("conf", true, "configuration file");
				usageOptions.addOption("contig", false, "set the bank as contig sequences");
				usageOptions.addOption("noclean", false, "Do not delete created files after treatment");
				usageOptions.addOption("email", true, "Email address to send result availability info");
				usageOptions.addOption("max", true, "maximum returned solutions");
				usageOptions.addOption("maxres", true, "maximum result size of a match");
				usageOptions.addOption("maxmatchsize", true, "maximum size of a match");
				usageOptions.addOption("maxspacer", true, "maximum size of a spacer");
				usageOptions.addOption("lmax", true, "maximum length of a variable");
				usageOptions.addOption("lmin", true, "minimum length of a variable");
				usageOptions.addOption("all",false,"analyse both directions of the sequence");
				usageOptions.addOption("out", true, "Zip output file name");
				usageOptions.addOption("enabledups", false, "enable duplicate result matches. By default only keep singletons");
				usageOptions.addOption("forcesplit", false, "Force the sequence splitting according to parameters and whatever is the number of model used in grammar");
				usageOptions.addOption("fasta",false,"Add fasta conversion to result archive");
				usageOptions.addOption("gff",false,"Add gff conversion to result archive");
				usageOptions.addOption("filter",true,"Type of filter when enabledups is not enabled. Allowed types are exact(default),local,local0,global.");
				LogolUtils.showUsage(usageOptions);
				 System.exit(0);
		 }

		 if( cmd.hasOption( "guid" ) ) {
			    // initialise the member variable
			 logger.info("option uid called with "+cmd.getOptionValue( "guid" ));
			 guid=cmd.getOptionValue( "guid" );
			}
		 else {
			 guid=UUID.randomUUID().toString();
		 }


		 if( cmd.hasOption( "s" ) ) {
			 //Fix 1337 support relative path
			 File  lv_file = new File(cmd.getOptionValue( "s" ));
			    bank=lv_file.getAbsolutePath();
		 }
		 else {
			 displayError("Option s is missing");
			 System.exit(1);
		 }
		 if( cmd.hasOption( "g" ) ) {
			//Fix 1337 support relative path
			 File  lv_file = new File(cmd.getOptionValue( "g" ));
			    grammar=lv_file.getAbsolutePath();
		 }
		 else if( cmd.hasOption( "m" ) ) {
			//Fix 1337 support relative path
			 File  lv_file = new File(cmd.getOptionValue( "m" ));
			    model=lv_file.getAbsolutePath();
		 }
		 else {
			 displayError("Option m is missing");
			 System.exit(1);
		 }

		 if( cmd.hasOption( "local" ) ) {
			 logger.info("Using local system");
			   type=LOCAL;
			   jobmngr = (JobManager) new LocalJobManager();
		 }
		 if( cmd.hasOption( "sge" ) ) {
			 logger.info("Using sge system");
			    type=SGE;
			    jobmngr = (JobManager) new SGEJobManager();
		 }
		 if( cmd.hasOption( "dna" ) ) {
			    format=1;
		 }
		 if( cmd.hasOption( "rna" ) ) {
			    format=2;
		 }
		 if( cmd.hasOption( "protein" ) ) {
			    format=3;
		 }

		 if( cmd.hasOption( "conf" ) ) {
			//Fix 1337 support relative path
			 File  lv_file = new File(cmd.getOptionValue( "conf" ));
			 configFile=lv_file.getAbsolutePath();
		 }
		 else {
				if(System.getProperty("logol.conf")!=null) {
					configFile=System.getProperty("logol.conf");
					jobArgs+=" -conf "+configFile;
				}
				else {
					displayError("conf option is missing");
					System.exit(1);
				}

		 }

		 if( cmd.hasOption( "contig" ) ) {
			    isContig=true;;
		 }

		 if( cmd.hasOption( "noclean" ) ) {
			    noclean=true;;
		 }
		 if( cmd.hasOption( "email" ) ) {
			    email=cmd.getOptionValue( "email" );
		 }

		 if( cmd.hasOption( "max" ) ) {
			 maxSolutions=Integer.valueOf(cmd.getOptionValue( "max" ));
		 }


		 if( cmd.hasOption( "maxmatchsize" ) ) {
			    // initialise the member variable
			 logger.info("option max match size called with "+cmd.getOptionValue( "maxmatchsize" ));
			 maxMatchSize=Integer.valueOf(cmd.getOptionValue( "maxmatchsize"));
		}

		 if( cmd.hasOption( "noclean" ) ) {
			    // initialise the member variable
			 logger.info("option noclean called");
			  noClean=true;
			}

		 if( cmd.hasOption( "out" ) ) {
			    // initialise the member variable
			 logger.info("option out called with "+cmd.getOptionValue("out"));
			 zipFileName = cmd.getOptionValue("out");
			}

		 if(cmd.hasOption("forcesplit")) {
			 forceSplittingUsage = true;
		 }

		 if( cmd.hasOption( "fasta" ) ) {
			    convert2fasta=true;;
		 }

		 if( cmd.hasOption( "gff" ) ) {
			    convert2gff=true;;
		 }

		 try {
			config = new PropertiesConfiguration(configFile);
		} catch (ConfigurationException e) {
			displayError(e.getMessage());
			System.exit(1);
		}


		 workDir = config.getString("workingDir");
		 /*
		  * If working on a cluster, a shared directory with nodes is required to share the sequence.
		  * This directory must be defined with result.dir
		  */
		 if(config.getString("dir.result")!=null)  { workDir = config.getString("dir.result"); }

		 //installDir = config.getString("installPath");
			String path = System.getProperty("logol.install");
			if(path!=null)  {
				installDir = path;
			}
			else {
				installDir=".";
			}


		 if(config.containsKey("nbProcessor")) {
			nbproc = config.getInt("nbProcessor");
			}

		if(config.containsKey("nbJobs")) {
			nbjobs = config.getInt("nbJobs");
			}

		if(config.containsKey("mail.from")) {
			fromMail = config.getString("mail.from");
			}

		minSplitSize =config.getInt("minSplitSize");

		// if not in command line use default
		if(!cmd.hasOption( "maxmatchsize" )) {
		maxMatchSize = config.getInt("maxMatchSize");
		}

		// If maxSolutions not set in command-line
		// FIX 1320
		if(maxSolutions==0 && config.containsKey("maxSolutions")) {
			maxSolutions = config.getInt("maxSolutions");
		}


		 for(int j=0;j<args.length;j++) {
			 jobArgs+=" "+args[j];
		 }

		 jobmngr.setJobArgs(jobArgs);
		 jobmngr.setInstallPath(installDir);

		 // Check shared directory existence
		 File wDir = new File(workDir);
		 if(!wDir.exists()) {
			 displayError("Error, directory "+workDir+" does not exist");
		 }

		 try {
			int lv_nbmodel = 1;
			if (!forceSplittingUsage) {
				lv_nbmodel = LogolUtils.countGrammarModels(grammar,model);
			}
			splitBankandExecute(lv_nbmodel);
		} catch (BioException e) {
			displayError(e.getMessage());
			System.exit(1);
		} catch (IOException e) {
			displayError(e.getMessage());
			System.exit(1);
		} catch (GrammarException e) {
			displayError(e.getMessage());
			System.exit(1);
		}

	}

	private static void displayError(String message) {
	      if(email!=null) {
	    	  //send an email with file result
	    	  // Use a email.tpl and replace a tag with file name (http:// or path)
	 	     //Set the host smtp address

	 	     Properties props = new Properties();
	 	     props.put("mail.smtp.host", config.getString("smtp.host"));
	 	     logger.debug("send mail to host "+config.getString("smtp.host"));
	 	     String user = config.getString("mail.user");
	 	     if(user!=null && !user.equals("")) {
	 	    	props.put("mail.smtp.user", config.getString("mail.user"));
	 	    	logger.debug("Mail: use user id "+config.getString("mail.user"));
	 	     }
	 	    try {
	 	     	logger.debug("Send message: "+message);
	 	     	String subject = "[LogolMatch] Job error: ";
	 	     	if(model==null) {
	 	     		File gfile = new File(grammar);
	 	     		if(gfile!=null)	subject += gfile.getName();
	 	     	}
	 	     	else {
	 	     		File mfile = new File(model);
	 	     		if(mfile!=null)	subject += mfile.getName();
	 	     	}
	 	     	File bfile =  new File(bank);
	 	     	if(bfile!=null) subject += " vs "+ bfile.getName();
	 	     	subject += " is over";
				LogolUtils.sendMail(props, new String[] {email}, subject, message, fromMail);
			} catch (MessagingException e) {
				logger.error("Result email could not be sent: "+e.getMessage());
			}
	      }
	      else {
	    	  System.err.println(message);
	      }

	}

	/**
	 * Split the bank using BioJava and execute a logol match for each one.
	 * @param nbModels of models required to match the sequence
	 * @throws BioException
	 * @throws IOException
	 */
	private static void splitBankandExecute(int nbModels) throws BioException, IOException {

		BufferedReader br = new BufferedReader(new FileReader(bank));

	      /*
	       * get a Sequence Iterator over all the sequences in the file.
	       * SeqIOTools.fileToBiojava() returns an Object. If the file read
	       * is an alignment format like MSF and Alignment object is returned
	       * otherwise a SequenceIterator is returned.
	       */
	      RichSequenceIterator iter =  null;
	      /*
	      switch (format) {
	      case 1: {
	    	  iter= (RichSequenceIterator)IOTools.readFastaDNA(br,null);
	    	  break;
	      }
	      case 2: {
	    	  iter= (RichSequenceIterator)IOTools.readFastaRNA(br,null);
	    	  break;
	      }
	      case 3: {
	    	  iter= (RichSequenceIterator)IOTools.readFastaProtein(br,null);
	    	  break;
	      }
	      }
	      */
	      //Fix1660
	      iter = (RichSequenceIterator) org.irisa.genouest.logol.utils.MySequenceIterator.readFasta(br);


	      File bFile = new File(bank);
	      String bankName = bFile.getName();

	      String uid = null;

	      int seqID=1;

	      Vector<String> outFiles = new Vector<String>();
	      Vector<String> xmlFiles = new Vector<String>();

	      HashMap<String,Vector<SubSequence>> sequenceFiles = new HashMap<String,Vector<SubSequence>>();
	      Vector<SubSequence> subSequences = null;

	      int offset=0;

	      while(iter.hasNext()){
	    	  subSequences = new Vector<SubSequence>();
	    	  uid = UUID.randomUUID().toString();
	    	  String outFileName = workDir+System.getProperty(FILESEPARATORPROPERTY)+bankName+"."+uid;

	    	  Sequence seq = iter.nextSequence();
	    	  //Check if can be splitted, if yes add all sub seq to subSequences
	    	  Integer[][] subData = LogolUtils.splitSequence(seq.length(), nbjobs, minSplitSize, maxMatchSize, nbModels);
	    	  logger.info("Sequence "+seqID+" splitted in "+subData.length+" parts");
	    	  for(int sub=0;sub<subData.length;sub++) {
	    	  SubSequence subSeq = new SubSequence();
	    	  subSeq.name=outFileName;

	    	  subSeq.sequenceID=seqID;
	    	  subSeq.wholeLength=seq.length();
	    	  subSeq.offset=offset;
	    	  subSeq.start=subData[sub][0];
	    	  subSeq.length=subData[sub][1]-subData[sub][0];
	    	  logger.debug("Add subsequence to job list: length "+subSeq.wholeLength+", start: "+subSeq.start+", length: "+subSeq.length);
	    	  subSequences.add(subSeq);
	    	  }


	    	  logger.info("Add sequence for treatment: "+outFileName);
	    	  logger.debug("Add subsequences, size: "+subSequences.size());
	    	  sequenceFiles.put(outFileName,subSequences);

	    	  if(isContig) {
	    		  offset+=seq.length();
	    	  }

	    	  seqID++;
	      }


	      String seqArgs = "";

	      Set<String> files = sequenceFiles.keySet();
	      Iterator<String> itFiles = files.iterator();

	      while(itFiles.hasNext()){
	    	  String seqName = (String) itFiles.next();
	    	  logger.debug("seqName: "+seqName);
	    	  Vector<SubSequence> seqs = (Vector<SubSequence>) sequenceFiles.get(seqName);
	    	  logger.debug("subsequences size : "+seqs.size());
	    	  // Get global info (same for all sub sequences
	    	  SubSequence seq = seqs.get(0);
	    	  String outFileName = seq.name;
	    	  String xmlFileName = outFileName+"."+seq.sequenceID+".xml";
	    	  int length = seq.wholeLength;

	    	  logger.debug("write file "+outFileName);
	    	  seqArgs="";

	    	  // 1 name per sequence
	    	  outFiles.add(outFileName);
	    	  xmlFiles.add(xmlFileName);
	    	  //now add job info

	    	  if(isContig) {
	    		  logger.info("Add offset to sequence : "+seq.offset);
	    		  seqArgs = "-offset "+seq.offset;
	    	  }
	    	  seqArgs += " -sequenceID "+seq.sequenceID;

	    	  logger.info("Args: "+seqArgs);

	    	  if(seqs.size()==1) {
	    		  jobmngr.addJob(outFileName,xmlFileName,seqArgs);
	    	  }else {
	    		  String subArgs="";
		    	  for(int job=0;job<seqs.size();job++) {
		    			  SubSequence sub = seqs.get(job);
		    			  subArgs=" -start "+sub.start;
		    			  subArgs+=" -end "+(sub.start+sub.length);
		    			  logger.info("Subsequence Args: "+subArgs);
		    			  logger.debug("Temp xml file generated: "+xmlFileName+"."+job);
		    			  jobmngr.addJob(outFileName,xmlFileName+"."+job,seqArgs+subArgs);
		    	  }
	    	  }


	      }

	      String nativeSpecifications=null;
	      if(config.getString("drm.queue")!=null && !config.getString("drm.queue").equals("")) {
	    	  nativeSpecifications=config.getString("drm.queue");
	      }

	      //TODO modify to do run only no wait. jobmngr should manage max parallel jobs for sge case.
	      jobmngr.runJobs(nativeSpecifications);


	      jobmngr.waitForJobsOver();




	      //Merge files requiring it ( subSequences.length>1 )
	      files = sequenceFiles.keySet();
	      itFiles = files.iterator();
	      while(itFiles.hasNext()){
	    	  String seqName = (String) itFiles.next();
	    	  Vector<SubSequence> seqs = (Vector<SubSequence>) sequenceFiles.get(seqName);
	    	  // If size > 1, then needs to be merged
	    	  if(seqs.size()>1) {
	    		  mergeSubSequenceResults(seqs);
	    	  }
	      }

	      // Fix 1271
	      // Parse error files for all sequences
	      itFiles = files.iterator();
	      String errMsg="";

	      while(itFiles.hasNext()){
	    	  String seqName = (String) itFiles.next();
	    	  Vector<SubSequence> seqs = (Vector<SubSequence>) sequenceFiles.get(seqName);
	    	  if(seqs.size()==1) {
	    		  SubSequence sub = seqs.get(0);
	    		  String err = readErrorFile(sub.name+"."+sub.sequenceID+".xml");
	    		  if(err.length()>0) { errMsg += sub.name+"."+sub.sequenceID+".xml"+":\n"+err+"\n"; }
	    	  }
	    	  else {
	    		  for(int j=0;j<seqs.size();j++) {
	    	    	  SubSequence sub = seqs.get(j);
	    	    	  String err= readErrorFile(sub.name+"."+sub.sequenceID+".xml."+j);
	    	    	  if (err.length()>0) { errMsg += sub.name+"."+sub.sequenceID+".xml."+j+":\n"+err+"\n";}
	    		  }
	    	  }
	      }

              if(errMsg.length()>0) {
                 logger.error("Sub processes errors: \n"+errMsg);
              }


	      String zipFile = null;

	      if(zipFileName!=null) {
	    	  // If output path specified
	    	  zipFile = zipFileName;
	    	  }
	      else {
	    	  // Else get unique file
	    	  zipFile = workDir+System.getProperty(FILESEPARATORPROPERTY)+bankName+"."+guid+".zip";
	    	  }


	      if(convert2fasta || convert2gff) {
	    	 String fastafile="";
	    	 String gfffile="";
	    	 String file2convert="";
	    	 FastaConverter fc  = null;
	    	 GFFConverter gc  = null;
	    	 Vector files2convert = (Vector) xmlFiles.clone();
		        for (int i=0; i<files2convert.size(); i++) {
		        	file2convert = (String) files2convert.get(i);
		        	if(convert2fasta) {
			        	fastafile = file2convert+".fasta";
			        	fc = new FastaConverter();
			        	fc.convert2Fasta(file2convert, fastafile);
			        	xmlFiles.add(fastafile);
		        	}
		        	if(convert2gff) {
			        	gfffile = file2convert+".gff";
			        	gc = new GFFConverter();
			        	gc.convert2GFF(file2convert, gfffile);
			        	xmlFiles.add(gfffile);
			        }
		        }

	      }

	      LogolUtils.countMatches(xmlFiles);

	      LogolUtils.zipFiles(xmlFiles, zipFile);
	      logger.info("Result file is available at: "+zipFile);

	      if(email!=null) {
	    	  //send an email with file result
	    	  // Use a email.tpl and replace a tag with file name (http:// or path)
	 	     //Set the host smtp address

	 	     Properties props = new Properties();
	 	     props.put("mail.smtp.host", config.getString("smtp.host"));
	 	     logger.debug("send mail to host "+config.getString("smtp.host"));
	 	     String user = config.getString("mail.user");
	 	     if(user!=null && !user.equals("")) {
	 	    	props.put("mail.smtp.user", config.getString("mail.user"));
	 	    	logger.debug("Mail: use user id "+config.getString("mail.user"));
	 	     }
	 	    try {
	 	     String messageTemplate=getEmailTemplate();
	 	     String message=messageTemplate.replaceAll("__FILE__",bankName+"."+guid+".zip");
		      if(zipFileName!=null) {
		    	  File tmpfile = new File(zipFileName);
		    	  message=messageTemplate.replaceAll("__FILE__",tmpfile.getName());
		     }
	 	     if(errMsg.length()>0) {
	 	    	 message += "ERRORS: \n"+errMsg;
	 	     }

	 	     	logger.debug("Send message: "+message);
	 	     	String subject = "[LogolMatch] Job request ";
	 	     	if(model==null) {
	 	     		subject += new File(grammar).getName();
	 	     	}
	 	     	else {
	 	     		subject += new File(model).getName();
	 	     	}
	 	     	subject += " vs "+ new File(bank).getName();
	 	     	subject += " is over";
				LogolUtils.sendMail(props, new String[] {email}, subject, message, fromMail);
			} catch (MessagingException e) {
				logger.error("Result email could not be sent: "+e.getMessage());
			} catch (IOException e) {
				logger.error("Result email could not be sent: "+e.getMessage());
			}
	      }

	      // Now clean intermediate files
	      for(int i=0;i<outFiles.size();i++) {
	    	  File tmpFile = new File((String)outFiles.get(i));
	    	  tmpFile.delete();
	      }
	      if(!noclean) {
		      for(int i=0;i<xmlFiles.size();i++) {
		    	  File tmpFile = new File((String)xmlFiles.get(i));
		    	  tmpFile.delete();
				  File f = new File((String)xmlFiles.get(i)+".err");
				  if(f.exists()) { f.delete(); }
				  f = new File((String)xmlFiles.get(i)+".out");
				  if(f.exists()) { f.delete(); }
			  }
	      }

	}


	private static String readErrorFile(String seqName) {
		String err = "";
		logger.debug("Analysing error file "+seqName+".err");
		File input = new File(seqName+".err");
		if(input.exists()) {
			try {
				String line=null;
				 BufferedReader bufRead = new BufferedReader(new FileReader(input));
				while((line=bufRead.readLine())!=null) {
					err += line+"\n";
				}
				bufRead.close();
			} catch (FileNotFoundException e) {
				logger.error("Could not read error file "+seqName+": "+e.getMessage());
			} catch (IOException e) {
				logger.error("Error reading file "+seqName+": "+e.getMessage());
			}
		}
		return err;
	}

	private static void mergeSubSequenceResults(Vector<SubSequence> seqs) {
	      /**
	       * Apply stylesheet to merge files and update id to get a unique among all sub results
	       */

	      String inFile=null;
	      //String outFile=null;
	      String xsltFile=installDir+System.getProperty(FILESEPARATORPROPERTY)+"prolog"+System.getProperty(FILESEPARATORPROPERTY)+"logol.merge.xsl";
	      SubSequence seq = seqs.get(0);
	      String mergeFileName  = seq.name+"."+seq.sequenceID+".xml";
	      logger.info("Merge sub sequences to file "+mergeFileName);
	      File mergeFile = new File(mergeFileName);

	      PrintWriter mergeOutputStream=null;
		try {
			mergeOutputStream = new PrintWriter(mergeFile);
		      mergeOutputStream.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		      mergeOutputStream.println("<sequences>");
		      // Add header to keep sequence information in result file
		      //mergeOutputStream.println("<fastaHeader>"+seq.header+"</fastaHeader>");
		      mergeOutputStream.println("<sequenceBegin>"+seq.offset+"</sequenceBegin>");
		      mergeOutputStream.println("<sequenceEnd>"+(seq.offset+seq.wholeLength)+"</sequenceEnd>");

		      // Include original grammar or model in result
		      FileReader mFile=null;
		      String mHeader = null;
		      if(model!=null) {
		    	  mFile = new FileReader(model);
		    	  mHeader = "model";
		      }
		      else {
		    	  mFile = new FileReader(grammar);
			      mHeader = "grammar";
		      }

		      BufferedReader bufRead = new BufferedReader(mFile);
		      String line = "";
		      String mContent="";
		      while((line = bufRead.readLine()) !=null) {
		    	  mContent+=line+"\n";
		      }
		      mFile.close();


		      mergeOutputStream.println("<"+mHeader+">"+Base64.encode(mContent.getBytes())+"</"+mHeader+">");



		} catch (FileNotFoundException e1) {
			logger.error("Error while applying stylesheet in postprocess: "+e1.getMessage());
		} catch (IOException e) {
			logger.error("Error while applying stylesheet in postprocess: "+e.getMessage());
		}

	      int countMergedResults = 0;


	      for(int i=0;i<seqs.size();i++) {
	    	  logger.debug("transform xml result file with stylesheet");
	    	  SubSequence sub = seqs.get(i);
	    	  inFile = sub.name+"."+sub.sequenceID+".xml."+i;
	    	  logger.debug("merging "+inFile);

	    	  try {
	    		  if(countMergedResults<maxSolutions) {
	    			  countMergedResults += transformXSLT(inFile,xsltFile,mergeOutputStream,String.valueOf(i));
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
			} catch (IOException e) {
				logger.error("Error while applying stylesheet in postprocess: "+e.getMessage());
			}

	      }
	      mergeOutputStream.println("</sequences>");

	      // Now remove intermediate xml result files
	      for(int i=0;i<seqs.size();i++) {
	    	  logger.debug("remove temp result files");
	    	  SubSequence sub = seqs.get(i);
	    	  inFile = sub.name+"."+sub.sequenceID+".xml."+i;
	    	  File in = new File(inFile);
	    	  if(!noClean) {
	    		  in.delete();
	    	  }
	      }


	      mergeOutputStream.close();
	        logger.debug("file transformed and renamed");

	}

	/**
	 * Loads email template.
	 * @return Template content
	 * @throws IOException
	 */
	private static String getEmailTemplate() throws IOException {
		File tpl = new File(installDir+System.getProperty(FILESEPARATORPROPERTY)+mailTemplatePath);
		FileReader fr = new FileReader(tpl);
		BufferedReader in  = new BufferedReader(fr);
		String line=null;
		String res="";
		while((line = in.readLine())!=null) {
			res+=line+"\n";
		}
		in.close();
		return res;
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
				PrintWriter mergeOutputStream, String id) throws TransformerException, ParserConfigurationException, SAXException, IOException {

		logger.debug("Transform xml with stylesheet");

		// 1. Instantiate a TransformerFactory.
		javax.xml.transform.TransformerFactory tFactory = javax.xml.transform.TransformerFactory.newInstance();

		// 2. Use the TransformerFactory to process the stylesheet Source and generate a Transformer.
		javax.xml.transform.Transformer transformer = tFactory.newTransformer
		                (new javax.xml.transform.stream.StreamSource(xsltFile));

		File lv_file = new File(inFile);
		String lv_filename = lv_file.getName();

		transformer.setParameter("fileid", id);
		// 3. Use the Transformer to transform an XML Source and send the output to a Result object.
		transformer.transform
	    (new javax.xml.transform.stream.StreamSource(inFile),
	     new javax.xml.transform.stream.StreamResult( mergeOutputStream));

	    mergeOutputStream.flush();

		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document document = builder.parse(new File(inFile));
		NodeList matchNodeList =  XPathAPI.selectNodeList(document,"/sequences/match");
		if(matchNodeList==null) {
			return 0;
		}
		return matchNodeList.getLength();


	}


}
