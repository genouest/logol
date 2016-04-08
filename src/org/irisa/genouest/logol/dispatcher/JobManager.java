package org.irisa.genouest.logol.dispatcher;

/**
 * Generic interface to submit some jobs and wait for the end of all jobs.
 * @author osallou
 *
 */
public interface JobManager {
	
	public int addJob(String sequence, String output,String jobArgs);
	
	public int runJobs(String nativeSpecifications);

	public int waitForJobsOver();
	
	public void setJobArgs(String args);

	public void setInstallPath(String installDir);
}
