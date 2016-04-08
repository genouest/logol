package org.irisa.genouest.logol.dispatcher;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.irisa.genouest.logol.Constants;


/**
 * Manage execution of jobs on local computer. No parallelization is done.
 * 
 * @author osallou
 *
 */
public class LocalJobManager implements JobManager {

	private static final Logger logger = Logger.getLogger(org.irisa.genouest.logol.dispatcher.LocalJobManager.class);
	
	private static String OS = System.getProperty("os.name").toLowerCase();

	
	Vector<String> jobs = new Vector<String>();
	Vector<String> jobsArgs = new Vector<String>();
	Vector<String> jobsOutput = new Vector<String>();
	String arguments = "";

	
	String installPath=null;
	
	public LocalJobManager() {
	}

	/**
	 * Adds sequence as a job to execute
	 */
	public int addJob(String sequence,String output,String jobArgs) {
		logger.debug("Add new job for sequence "+sequence+" with args "+jobArgs+" to output "+output);
		jobsOutput.add(output);
		jobs.add(sequence);
		jobsArgs.add(jobArgs);
		return 0;
	}

	/**
	 * Executes the job as sequential treatment.
	 */
	public int runJobs(String nativeSpecifications) {
		logger.info("Run jobs locally");
		Runtime runtime = Runtime.getRuntime();   
		// Execute locally logolExec.sh with args
		for(int i=0;i<jobs.size();i++) {
			     
	        Process logolExec;
			try {
				logger.info("Analyse sequence "+jobs.get(i));
				if(OS.startsWith("win")) {
					logolExec = runtime.exec("ruby "+new String (installPath+System.getProperty(Constants.FILESEPARATORPROPERTY)+"LogolExec.rb "+arguments+" "+jobsArgs.get(i)+" -output "+jobsOutput.get(i)) );				
				}
				else {
					logolExec = runtime.exec(new String (installPath+System.getProperty(Constants.FILESEPARATORPROPERTY)+"LogolExec.sh "+arguments+" "+jobsArgs.get(i)+" -output "+jobsOutput.get(i)) );
				}
	       
	        	PrintStream os = new PrintStream(logolExec.getOutputStream());
	        	os.println("");
	        	os.flush();
	        	        	
	        	StreamGobbler s1 = new StreamGobbler ("stdin", logolExec.getInputStream ());
	        	StreamGobbler s2 = new StreamGobbler ("stderr", logolExec.getErrorStream ());
	        	s1.start ();
	        	s2.start ();
	        
	        	logolExec.waitFor();
				logger.info("Analyse for "+jobs.get(i)+" is over");				
				os.close();
		
			} catch (InterruptedException e) {
				System.out.println("Error during execution "+ e.getMessage());
			}
			catch (IOException e1) {
				System.out.println("Error during execution "+ e1.getMessage());
			}
		}
		return 0;
	}

	/**
	 * Not applicable when serialized, return immediatly
	 */
	public int waitForJobsOver() {
		logger.info("Each sequence in bank resulted in a specific result file.");
		for(int i=0;i<jobs.size();i++) {
		logger.info("Result file is available in work directory : "+jobs.get(i)+"-XX-YY-all.xml");
		}
		return 0;
	}
	
	/**
	 * Sets the arguments for the job execution.
	 */
	public void setJobArgs(String args) {
		arguments = args;
	}

	
	public void setInstallPath(String installDir) {
		installPath = installDir;
		
	}
	
}
