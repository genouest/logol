package org.irisa.genouest.logol.dispatcher;

import java.io.File;
import java.util.UUID;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.JobTemplate;
import org.ggf.drmaa.Session;
import org.ggf.drmaa.SessionFactory;
import org.irisa.genouest.logol.Constants;

/*
 * Manage the submission of jobs on a SGE system using DRMAA Java library.
 */
public class SGEJobManager implements JobManager {

	private static final Logger logger = Logger.getLogger(org.irisa.genouest.logol.dispatcher.SGEJobManager.class);
	
	private static String OS = System.getProperty("os.name").toLowerCase();

	
	Vector<String> jobs = new Vector<String>();
	Vector<String> jobsOutput = new Vector<String>();
	Vector<String> jobsArgs = new Vector<String>();
	Vector<String> jobIds = new Vector<String>();
	
	String arguments = "";
	
	static SessionFactory factory = null;
	static Session session=null; 
	
	
	String jobId = null;
	
	String installPath=null;
	
	UUID uID = null;
	
	public SGEJobManager() {
		logger.info("Init DRM communication");
		if(factory==null) {
			factory = SessionFactory.getFactory();
		}
		if(session==null) {
			session = factory.getSession();		
		}
		if(session==null) {
			logger.error(" Drm session could not open");
		}
		logger.info("DRM system: "+session.getDrmSystem());
		try {
			session.init("");
		} catch (DrmaaException e) {
			logger.error("Could not init the DRM session: "+e.getMessage());
		}
	}
	
	public int addJob(String sequence, String output,String jobArgs) {
		jobs.add(sequence);
		jobsArgs.add(jobArgs);
		jobsOutput.add(output);
		return 0;
	}

	public int runJobs(String nativeSpecifications) {
		JobTemplate jt;
		for(int i=0;i<jobs.size();i++) {
		try {
			jt = session.createJobTemplate();
			if(nativeSpecifications!=null) {
				jt.setNativeSpecification(nativeSpecifications);
			}
			
			jt.setJobName("LogolMatch");
			
			String out = jobsOutput.get(i);
			// Redirect DRM streams 
		    jt.setOutputPath(":" + out+".out");		
			jt.setErrorPath(":" + out + ".err");
			if(OS.startsWith("win")) {
				jt.setArgs(new String[] {installPath + System.getProperty(Constants.FILESEPARATORPROPERTY)+ "LogolExec.rb", arguments,jobsArgs.get(i)," -output "+jobsOutput.get(i)});
			    jt.setRemoteCommand("ruby");				
			}
			else {
				jt.setArgs(new String[] {arguments,jobsArgs.get(i)," -output "+jobsOutput.get(i)});
				jt.setRemoteCommand(installPath + System.getProperty(Constants.FILESEPARATORPROPERTY)+ "LogolExec.sh");
			}
		    jobId = session.runJob(jt);
		    logger.info("Executing job: "+jobId);
		    jobIds.add(jobId);
		    session.deleteJobTemplate(jt);
		} catch (DrmaaException e) {			
			logger.error("Error during job submission: "+e.getMessage());
			return -1;
		}
		}


		return 0;
	}

	public int waitForJobsOver() {
		
		try {
			session.synchronize(jobIds, Session.TIMEOUT_WAIT_FOREVER, true);
			session.exit();
		} catch (DrmaaException e) {
			logger.error("Error during job submission: "+e.getMessage());
		}
		logger.info("Each sequence in bank resulted in a specific result file.");
		for(int i=0;i<jobs.size();i++) {			
			logger.debug("Result file is available in work directory : "+jobsOutput.get(i));
			}
		return 0;
	}
	
	public void setJobArgs(String args) {
		arguments=args;
	}

	public void setInstallPath(String installDir) {
		installPath = installDir;
		
	}

}
