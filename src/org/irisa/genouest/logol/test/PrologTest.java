package org.irisa.genouest.logol.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;


import junit.framework.TestCase;

class StreamGobbler implements Runnable {
		
	String name;
	InputStream is;
	public Thread thread;
	
	public String data="";

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
	if(s != null) {
		data += s;
	}
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

public class PrologTest extends TestCase {

	private static final Logger logger = Logger.getLogger(org.irisa.genouest.logol.test.PrologTest.class);
	
	private static final String FILESEPARATORPROPERTY = "file.separator";
	private static final String separator = System.getProperty(FILESEPARATORPROPERTY);
	private static String prolog = "sicstus ";
	
	private static final String SUCCESS="write('SUCCESS'),nl";
	private static final String FAIL="write('FAIL'),nl";
	
	private String installDir=".";
	
	private String outFile="";
	
	private String inFile="";
	
    @Before
    public void setUp() {
    	if(System.getProperty("prolog")!=null && ! System.getProperty("prolog").equals("")) {
    		prolog = System.getProperty("prolog");
    	}
    	
		String path = System.getProperty("logol.install");
		if(path!=null) {
			installDir = path;
		}
		outFile = installDir+"/test/tmp/out_ut.xml";
		inFile  = installDir+"/test/test.fasta";
    }

	
	private void callProgram(String prologTest) {
		Runtime runtime = Runtime.getRuntime();
		Process logol=null;
		StreamGobbler s1=null;
		StreamGobbler s2=null;
		try {
			
			prologTest ="openSequenceStream('"+inFile+"',74),bb_put('matchCounter',1),assert(offset(0)),assert(maxsize(\"100\")),assert(outputfile('"+outFile+"')),"+prologTest;
			prologTest +=",closeSequenceStream";
            
			String prologExecLine=prolog;
			
			logger.debug("prolog cmd= "+prologExecLine);
			logol = runtime.exec(prologExecLine);
		} catch (IOException e1) {
			logger.error(e1.getMessage());
			fail();
		}
        try {
        	PrintStream os = new PrintStream(logol.getOutputStream());
        	logger.debug("prolog= "+"(restore('"+installDir+"/prolog/logol.sav'),"+"("+prologTest+",halt));(write('FAILURE'),nl,halt).");
        	os.println("(restore('"+installDir+"/prolog/logol.sav'),"+"("+prologTest+",halt));(write('FAILURE'),nl,halt).");
        	os.flush();
        	        	
        	s1 = new StreamGobbler ("stdin", logol.getInputStream ());
        	s2 = new StreamGobbler ("stderr", logol.getErrorStream ());
        	s1.start ();
        	s2.start ();
        	
        	logol.waitFor();
        	
			os.close();
			
			s1.thread.join();
			s2.thread.join();
        	
        	assertTrue(s1.data.matches("SUCCESS"));
		

		} catch (InterruptedException e) {
			logger.error("Error during execution "+ e.getMessage());
			fail(e.getMessage());
		} 	

	}
	//TODO add tests for prolog library, calling external prolog program and prolog content
	 @Test
	 public void testWordContent() {
		 String test="getWordContent([a,a,c,c,c],[c,c,c],0,WordContent),WordContent=[a,a],"+SUCCESS;
		 callProgram(test);

	 }
	 
	@Test
	 public void testReadFile() {
		 String test="getCharsFromPosition(0,3,Z),Z=[c,c,c],"+SUCCESS;
		 callProgram(test);
	 }
	
	@Test
	 public void testIsExactPos() {
		 String test="isexact_pos( 0, [c,c,c] , Errors, OutPos),Errors=0,"+SUCCESS;
		 callProgram(test);
	 
	 }	
	
	@Test
	 public void testIsExactWithGapAndErrorPos_CostError() {
		 String test="isexactwithgapanderror_pos( 0, [c,g,c] , 1, Errors, OutPos),Errors=1,"+SUCCESS;
		 callProgram(test);
	 
	 }		

	@Test
	 public void testIsExactWithGapAndErrorPos_DistError() {
		 String test="isexactwithgapanderror_pos( 0, [c,g,c,c] , 1, Errors, OutPos),Errors=1,"+SUCCESS;
		 callProgram(test);
	 
	 }	
	
	
	@Test
	 public void testIsExactWithErrorOnlyPos() {
		 String test="isexactwitherroronly_pos( 0, [c,g,c] , 1, Errors, OutPos),Errors=1,"+SUCCESS;
		 callProgram(test);
	 
	 }

	@Test
	 public void testIsExactWithDistinctGapAndErrorPos() {
		 String test="isexactwithdistinctgapanderror_pos( 0, [a,c,c,c,g,a,a] , 1, 1, Errors, DistanceErrors, OutPos),Errors=1,DistanceErrors=1,"+SUCCESS;
		 callProgram(test);
	 
	 }
	
	@Test
	 public void testIsExactWithGapOnlyPos() {
		 String test="isexactwithgaponly( [a,c,c,c,g,t], [a,c,g,t] , 2,  Errors, OutPos),Errors=2,"+SUCCESS;
		 callProgram(test);
	 
	 }
	
	
	@Test
	public void testMorphism() {
		//applymorphism([X|Y],Morph,Reverse,Z)
		/*
		 * 	morphism('wcdna','t','a').
			morphism('wcdna','a','t').
			morphism('wcdna','c','g').
			morphism('wcdna','g','c').
			morphism('wcrna','u','a').
			morphism('wcrna','a','u').
			morphism('wcrna','c','g').
			morphism('wcrna','g','c').
		 */
		String test="applymorphism(['t','a','c'],'wcdna',0,Z),Z=['a','t','g'],"+SUCCESS;
		callProgram(test);
	}
	
	@Test
	public void testReverseMorphism() {
		//applymorphism([X|Y],Morph,Reverse,Z)
		/*
		 * 	morphism('wcdna','t','a').
			morphism('wcdna','a','t').
			morphism('wcdna','c','g').
			morphism('wcdna','g','c').
			morphism('wcrna','u','a').
			morphism('wcrna','a','u').
			morphism('wcrna','c','g').
			morphism('wcrna','g','c').
		 */
		String test="applymorphism(['t','a','c'],'wcdna',1,Z),Z=['g','t','a'],"+SUCCESS;
		callProgram(test);
	}	
	
	@Test
	public void testMorphismList() {
		String test="applymorphism(['a','t','t'],'foo',0,Z),Z=['a','a','c'],"+SUCCESS;
		callProgram(test);
	}
	
	
	@Test
	 public void testNotExact() {
		 String test="\\+notexact( [a,c,g,t], [a,c,g,t] , Out),"+SUCCESS;
		 callProgram(test);	 
	 }	

	@Test
	 public void testNotExact2() {
		 String test="notexact( [a,c,c,g,t], [a,c,g,t] , Out),Out=[c,g,t],"+SUCCESS;
		 callProgram(test);	 
	 }		
	
	
	@Test
	 public void testNotExactPos() {
		 String test="\\+notexact_pos( 0, [c,c,c] , 0, 0, Errors, OutPos),"+SUCCESS;
		 callProgram(test);	 
	 }	
	@Test
	 public void testNotExactPos2() {
		 String test="notexact_pos( 0, [c,c,c] , 3, 5, Errors, OutPos),OutPos=4,"+SUCCESS;
		 callProgram(test);	 
	 }	
	@Test
	 public void testNotExactPos3() {
		 String test="notexact_pos( 0, [c,c,c] , 3, 5, Errors, OutPos),OutPos=5,"+SUCCESS;
		 callProgram(test);	 
	 }
	
}
