package org.irisa.genouest.logol;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.RandomAccessFile;

import org.apache.log4j.Logger;
import org.biojava.bio.seq.Sequence;
import org.biojavax.SimpleNamespace;
import org.irisa.genouest.logol.utils.FastaConverter;
import org.irisa.genouest.logol.utils.MySequence;

public class SequenceAnalyser extends Thread {

	private static final Logger logger = Logger.getLogger(org.irisa.genouest.logol.SequenceAnalyser.class);

	String executable=null;
	String prologFile=null;
	String savFile=null;
	String sequenceFileName=null;
	int sequenceOffset=0;
	String headerFile=null;
	String outputFile=null;
    long startOffset;
    long endOffset;
    long sequenceId;
    int status=0;
    String msg=null;

    String resultFileName=null;
    String maxSolutions=null;

    int minTreeIndex=2;

    Sequence sequence=null;


    SequenceAnalyser(String exec,String proFile,String savFilePath,Sequence seq,long start,long end,String output, String max,int offset) {
    	executable = exec;
    	prologFile=proFile;
    	savFile = savFilePath;
    	sequence = seq;
    	resultFileName=output+".xml";
    	startOffset=start;
    	endOffset=end;
    	sequenceId=0;
    	sequenceOffset=offset;

    	outputFile=output+".fasta";
    	logger.debug("output file = "+outputFile);

    	maxSolutions=max;



    }



    SequenceAnalyser(String exec,String proFile,String savFilePath,String sequence,String header,long start,long end,String output, String max,int offset) {
    	executable = exec;
    	prologFile=proFile;
    	savFile = savFilePath;
    	sequenceFileName = sequence;
    	resultFileName=output+".xml";
    	headerFile=header;
    	startOffset=start;
    	endOffset=end;
    	sequenceId=0;
    	sequenceOffset=offset;

    	outputFile=output+".fasta";
    	logger.debug("output file = "+outputFile);

    	maxSolutions=max;
    }

    public void run() {
    	logger.debug("Start thread for file sequence "+sequenceId);
    	// read and save partial file

    	long lv_startTime = System.currentTimeMillis();

    	try {
			//rewriteSequence(sequenceFileName,outputFile,startOffset,endOffset);
      	  	File outFile = new File(outputFile);
      	  	BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(outFile));
      	  	//FastaConverter.writeFasta(os, sequence, new SimpleNamespace("Logol search"));
    		//Fix1660
      	  	MySequence.writeFasta(os, sequence);
      	  	os.close();
    		BufferedReader br = new BufferedReader(new FileReader(outputFile));
    		headerFile = br.readLine();
    		br.close();

    		// Create a file if type is RNA or DNA so that external scripts know the type of sequence
    		if(Treatment.dataType==Constants.DNA || Treatment.dataType==Constants.RNA) {
    			FileWriter typeFile = new FileWriter(outputFile+".index.dna");
    			BufferedWriter dnaos = new BufferedWriter(typeFile);
    			dnaos.write("dna/rna");
    			dnaos.close();
    		}

		} catch (IOException e1) {
			logger.error("Error while rewriting sequence file "+e1.getMessage());
		}

    	//create tree
        try {
			createTree();
		} catch (IOException e) {
			status=-1;
			msg="Coudld not make tree for suffix array: "+e.getMessage();
			return;
		}

        try {
			callProgram();
		} catch (IOException e) {
			status=-1;
			msg="Coudld not execute program: "+e.getMessage();
			return;
		}

		long lv_endTime = System.currentTimeMillis();

		logger.debug("Thread for "+outputFile+" terminated in "+(lv_endTime-lv_startTime));

    }


    @Deprecated
    private void rewriteSequence(String sequenceFileName,String outputFile,long startOffset, long endOffset) throws IOException {


        FileWriter out = new FileWriter(outputFile);
        BufferedWriter bw = new BufferedWriter(out);
        int record;

        File wholeSequence = new File(sequenceFileName);
        RandomAccessFile input = new RandomAccessFile(wholeSequence,"r");



		long size=0;

		bw.write(headerFile+"\n");

		if(endOffset>startOffset) {
		// reading in left to right
		input.seek(startOffset);

		while((size<(endOffset-startOffset)) && (record = input.read())!=-1) {
			size++;
	     	if(Character.isLetter(record)) {

	         	   bw.write(Character.toLowerCase(record));
	         	  }
		}

		}
		else {
			input.seek(startOffset-1);
			long pos=startOffset-1;
			while((size<(startOffset-endOffset)) && (record = input.read())!=-1) {
				size++;
				pos--;
		     	if(Character.isLetter(record)) {
		         	   bw.write(Character.toLowerCase(record));
		         	  }
		     	input.seek(pos);
			}


		}

        bw.close();
        input.close();
    }


	private void callProgram() throws IOException {
		Runtime runtime = Runtime.getRuntime();
		long fileOffset= startOffset;
		if(endOffset<startOffset) {
			fileOffset = endOffset;
		}
		//long offset = sequenceOffset + fileOffset - headerFile.length();
		//TODO CHECK offset is ok
		long offset = sequenceOffset + fileOffset;

		String parameters = " "+outputFile+" "+resultFileName+" "+maxSolutions+" "+offset+" "+headerFile.getBytes().length;
		//FIXME to use global exe, add logol generated file as parameter and call logol.exe in prolog dir
		parameters+=" "+prologFile+" "+savFile;

		logger.debug("Call program with params: "+parameters);

		Process logol = runtime.exec(executable+parameters);
        try {
        	PrintStream os = new PrintStream(logol.getOutputStream());
        	os.println("");
        	os.flush();

        	StreamGobbler s1 = new StreamGobbler ("stdin", logol.getInputStream ());
        	StreamGobbler s2 = new StreamGobbler ("stderr", logol.getErrorStream ());
        	s1.start ();
        	s2.start ();
        	logger.debug("start program execution ");
        	logol.waitFor();
			int status_code = logol.exitValue();
			if(status_code != 0){
				logger.error("Program exited with wrong status code: " + status_code);
				throw new IOException("Program exited with wrong status code");
			}

			logger.debug("program is over, results are available in file "+resultFileName);
			os.close();

		} catch (InterruptedException e) {
			logger.error("Error during execution "+ e.getMessage());
		}

	}

	private void createTree() throws IOException {
		String lv_type="dna";
		switch(Treatment.dataType) {
		case 0: { lv_type="dna"; break; }
		case 1: { lv_type="dna"; break; }
		case 2: { lv_type="protein"; break; }
		}
	 	logger.debug("Create tree for "+outputFile);

	 	String suffixPath="";
	 	if(Logol.getSuffixPath()!=null) {
	 		suffixPath=Logol.getSuffixPath()+System.getProperty(Constants.FILESEPARATORPROPERTY);
	 		logger.debug("Create tree with path: "+suffixPath);
	 	}
	 	else {
	 		logger.warn("Path to suffix search tool is not set in system environment. Will try to execute directly but may fail if not in PATH of current user");
	 	}

	 	Runtime runtime = Runtime.getRuntime();
	 	String command = null;
	 	switch(Logol.getSuffixTool()) {
	 	case 0:
	 		command = "ruby "+Treatment.installPath+System.getProperty(Constants.FILESEPARATORPROPERTY)+"tools"+System.getProperty(Constants.FILESEPARATORPROPERTY)+"logolIndex.rb --index "+outputFile+" --out "+outputFile+".index";
	 		break;
	 	case 1:
		 	command = suffixPath+"mkvtree -db "+outputFile+" -pl "+minTreeIndex+" -allout -v -indexname "+outputFile;
		 	if(lv_type.equals("dna")) {
		 		// Use specific map file in index creation
		 		command+=" -smap "+Treatment.installPath+System.getProperty(Constants.FILESEPARATORPROPERTY)+"prolog"+System.getProperty(Constants.FILESEPARATORPROPERTY)+"mapfile.dna";
		 	}
		 	else {
		 		command+=" -"+lv_type;
		 	}
		 	break;
	 	case 2:
	 		logger.debug("no index creation required");
	 		command = null;
	 		break;
	 	default:
	 		command = null;
	 		break;
	 	}

	 	if(command!=null) {
	 		logger.debug("Tree creation command: "+command);
	 		Process mkvtree = runtime.exec(command);
	 		try {
	 			PrintStream os = new PrintStream(mkvtree.getOutputStream());
	 			os.println("");
	 			os.flush();

	 			StreamGobbler s1 = new StreamGobbler ("stdin", mkvtree.getInputStream ());
	 			StreamGobbler s2 = new StreamGobbler ("stderr", mkvtree.getErrorStream ());
	 			s1.start ();
	 			s2.start ();
	 			int status = mkvtree.waitFor();
	 			if (status>0) {
	 				logger.error("An error occured while creating index tree for command: "+command);
	 			}
	 			else {
	 				logger.debug("Tree created for "+outputFile);
	 			}
	 			os.close();

	 		} catch (InterruptedException e) {
	 			logger.error("could not create Tree "+ e.getMessage());
	 		}
	 	}

	}

}
