package org.irisa.genouest.logol.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;
import org.apache.xpath.XPathAPI;
import org.irisa.genouest.logol.Constants;
import org.irisa.genouest.logol.Expression;
import org.irisa.genouest.logol.GrammarException;
import org.irisa.genouest.logol.LogolVariable;
import org.irisa.genouest.logol.Treatment;
import org.irisa.genouest.logol.VariableId;
import org.irisa.genouest.logol.XmlCountHandler;
import org.irisa.genouest.logol.utils.model.ModelConverter;
import org.irisa.genouest.logol.utils.model.ModelDefinitionException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * History:
 * Fix 1796: intermediate variables lost when put in a view
 * 20/06/11 FIX 1806
 * 16/03/13 Display number of match per file
 * 04/10/13 FIX 2242 Error with overlap
 * @author osallou
 *
 */
public class LogolUtils {

	private static final Logger logger = Logger.getLogger(org.irisa.genouest.logol.utils.LogolUtils.class);
	
	/**
	 * Incremental counter to get unique variable identifiers.
	 */
	public static int varCounter=0;
	public static int predCounter=0;	
	/**
	 * Get a new varCounter value
	 * @return counter value
	 */
	public static int getCounter() {
		int count = varCounter;
		varCounter++;
		return count;
	}
	
	/**
	 * Compute a percentage calculation on motif to get a size
	 * @param motif a prolog List to analyse
	 * @param percent the percent variable or value
	 * @return an expression with required prolog and result variable name
	 */
	public static Expression percent2int(String motif,String percent) {
	String tmp_cost = LogolUtils.getTemporaryVariable();
	String prolog =",percent2int("+motif+","+tmp_cost+","+percent+")";	
	return new Expression(prolog,tmp_cost);
	}
	
	
	/**
	 * Sends a temporary variable identifier. Id is incremented at each call.
	 * @return a temporary variable name
	 */
	public static String getTemporaryVariable() {
		String var =  Constants.LOGOLVARTMP+varCounter;
		varCounter++;
		return var;
	}
	
	
	/**
	 * Sends a temporary variable identifier for a predicate. Id is incremented at each call.
	 * @return a predicate name
	 */
	public static String getPredicateVariable() {
		String var =  Constants.LOGOLVARPRED+predCounter;
		predCounter++;
		return var;
	}	
	
	/**
	 * Sends a temporary variable identifier for a postponed predicate. Id is incremented at each call.
	 * @return a postponed predicate name
	 */
	public static String getPostponedPredicateVariable() {
		String var =  Constants.LOGOLVARPOSTPONEPRED+predCounter;
		predCounter++;
		return var;
	}	
	
	public static String addParent(LogolVariable lvar, int id) {
		String parentString="";
		// set id of the parent of the variable if any (else set to 0 by default)
		// If Parent not set, parent = current parent		
		parentString+="( (var(Parent),"+Constants.LOGOLVARPARENT+id+"= ["+lvar.parentId+"])";
		// If Parent set, add to list.
		parentString+=";(\\+ var(Parent),append(Parent,["+lvar.parentId+"],"+Constants.LOGOLVARPARENT+id+")))";		
		return parentString;
	}
	
	/**
	 * Generate a string array from a terminal e.g. a list of chars: "acgt" => ['a','c','g','t']
	 * @param sequence input terminal
	 * @return the string representation of the array
	 */
	public static String getArray(String sequence) {

		String caseSequence = sequence;
		if(Treatment.dataType== Constants.PROTEIN) {
			// Match uppercase for protein
			caseSequence = caseSequence.toUpperCase();
		}
		else {
			// Match lowercase for dna/rna
			caseSequence = caseSequence.toLowerCase();
		}
		
		String res="[";
		res+="'"+caseSequence.charAt(1)+"'";
		for(int i=2;i<caseSequence.length();i++) {
			
			if(!((String.valueOf(caseSequence.charAt(i))).equals("\""))) { res+=",'"+caseSequence.charAt(i)+"'"; }	
		}
		res+="]";
		return res;
	}
	
	
	
	/**
	 * Used by pre-analysis to get variables
	 * @param predicate
	 * @return a Set of variables saved in grammar for the current predicate
	 */
	public static HashSet<String> getAllUserVariables(String predicate) {
	
		HashSet<String> varSet = new HashSet<String>();
		
		// Look for user references 
		Pattern p = Pattern.compile("var\\((\\w+\\d+)\\)");
		Matcher m = p.matcher(predicate);
		while(m.find()) {	
			for(int i=1; i<=m.groupCount(); i++) {
				if(m.group(i)!=null) {
				varSet.add(m.group(i));
				}
			}				
		}
	
		p = Pattern.compile("logolPredicate_\\d+\\((\\w+\\d+)(,\\w+\\d+)+\\)");
		m = p.matcher(predicate);
		while(m.find()) {	
			for(int i=1; i<=m.groupCount(); i++) {
				String var = m.group(i);
				if(var!=null) {
				if(var.startsWith(",")) { var=var.substring(1); }
				varSet.add(var);
				}
			}				
		}		
				
		
		
		return varSet;
	}

	/**
	 * Gets all saved variable reference for the input data string
	 * @param data
	 * @param id current id of variable, -1 if not applicable
	 * @param model  current model
	 * @return a List of data reference
	 */
	public static String getAllSavedVariables(String data,int id,String model) {
		return getSavedVariables(data, id, model, true);
	}

	/**
	 * Gets the saved variable reference of the current predicate for the input data string
	 * @param data
	 * @param id current id of variable, -1 if not applicable
	 * @param model  current model
	 * @return a List of data reference
	 */
	public static String getSavedVariables(String data,int id,String model) {
		return getSavedVariables(data, id, model, false);
	}

	/**
	 * Gets the saved variable reference for the input data string
	 * @param data
	 * @param id current id of variable, -1 if not applicable
	 * @param model  current model
	 * @param all Get all referenced variables or only current predicate ones
	 * @return a List of data reference
	 */
	public static String getSavedVariables(String data,int id,String model, boolean all) {
		
		String usedVars= "[";	
		Vector<String> foundVariables=new Vector<String>();
		String s_id=null;
		
		// Look for used references (input or output)
		Pattern p = Pattern.compile("saveVariable\\('"+Constants.LOGOLVAR+"(\\d+)'");
		Matcher m = p.matcher(data);
		while(m.find()) {	
			for(int i=1; i<=m.groupCount(); i++) {
				if(!foundVariables.contains(m.group(i))) {
					s_id = String.valueOf(id);					
					if(id==-1 || (id>-1 && !s_id.equals(m.group(i)))) { foundVariables.add(m.group(i)); 
					if(!usedVars.equals("[")) { usedVars+=","; }
					usedVars+=Constants.LOGOLVARREF+m.group(i);
					}
				}
			}				
		}	
		
		if(all) {
		// Now add overlapped variables found in vector overlappedVariables
		/* Overlapped variables need to be managed differently  because they are not saved
		 * in current model, data references etc... are passed via parameters of called predicated.
		 * When an overlap is done, variable reference is saved in vector.
		 */
		//View localView = new View();
		//String model = localView.currentModel;
			for(int i=0;i<LogolVariable.overlappedVariables.size();i++) {
				VariableId lv_var = LogolVariable.overlappedVariables.get(i);
				logger.debug("overlap var = "+lv_var.model+","+lv_var.name);
				if(lv_var.model.equals(model)) {
					if(!usedVars.equals("[")) { usedVars+=","; }
						usedVars+=Constants.LOGOLVARREF+lv_var.name;
					}
			}
		}
		
		usedVars+="]";
		
		
		return usedVars;
	}	
	
	

	/**
	 * Looks for variable errors to know which variables are used in a predicate
	 * @param predicate	predicate string to analyse to get used variables
	 * @return	Set of used variables (unique)
	 */
	public static HashSet<String> getErrorVariables(String predicate) {
		
		Vector<String> foundVariables=new Vector<String>();
		
		HashSet<String> varSet = new HashSet<String>();
		
		// Look for used references (input or output)
		Pattern p = Pattern.compile("("+Constants.LOGOLVARERRORS+"\\d+)");
		Matcher m = p.matcher(predicate);
		while(m.find()) {	
			for(int i=1; i<=m.groupCount(); i++) {
				if(!foundVariables.contains(m.group(i))) {
					foundVariables.add(m.group(i));
					varSet.add(m.group(i));
				}
			}				
		}	
				
		return varSet;
	}	
	
	

	/**
	 * Looks for variable references to know which variables are used in a predicate
	 * @param predicate	predicate string to analyse to get used variables
	 * @return	Set of used variables (unique)
	 */
	public static HashSet<String> getAllUsedVariables(String predicate) {
		
		Vector<String> foundVariables=new Vector<String>();
		
		HashSet<String> varSet = new HashSet<String>();
		
		// Look for used references (input or output)
		Pattern p = Pattern.compile("("+Constants.LOGOLVARREF+"(Inter_)?\\d+)");
		Matcher m = p.matcher(predicate);
		while(m.find()) {	
			for(int i=1; i<=m.groupCount(); i++) {
				if(m.group(i)!=null && !foundVariables.contains(m.group(i))) {
					logger.debug("ADD "+m.group(i)+" to var set");
					foundVariables.add(m.group(i));
					varSet.add(m.group(i));
				}
			}				
		}
		
		// Look for parent references too
		p = Pattern.compile("("+Constants.LOGOLVARPARENTREF+"\\d+)");
		m = p.matcher(predicate);
		while(m.find()) {	
			for(int i=1; i<=m.groupCount(); i++) {
				if(m.group(i)!=null) {
				logger.debug("ADD "+m.group(i)+" to var set");
				varSet.add(m.group(i));
				}
			}				
		}		
				
		return varSet;
	}
	/**
	 * Get the first Before reference to get first intput
	 * @param predicate Predicate to analyse
	 * @return reference to the input variable
	 */
	public static String getPredicateInput(String predicate) {
		Pattern p = Pattern.compile(Constants.LOGOLVARBEFORE+"(\\d+)");
		Matcher m = p.matcher(predicate);
			
		if (m.find()) { return m.group(1); }			
		return null;
	}
	
	/**
	 * Get the last After reference to get last output
	 * @param predicate Predicate to analyse
	 * @return reference to the output variable
	 */
	public static String getPredicateOutput(String predicate) {
		Vector<String> foundVariables=new Vector<String>();
		
		Pattern p = Pattern.compile(Constants.LOGOLVARAFTER+"(\\d+)");
		Matcher m = p.matcher(predicate);
		String res=null;
		while(m.find()) {
			
			for(int i=1; i<=m.groupCount(); i++) {
				if(!foundVariables.contains(m.group(i))) {
					foundVariables.add(m.group(i));
					res=m.group(i);
				}
			}				
		}	
		return res;			

	}

	/**
	 * Show on output stream the usage based on a list of options
	 * @param usageOptions List of options
	 */
	public static void showUsage(Options usageOptions) {
		Collection optionList = usageOptions.getOptions();
		Iterator it = optionList.iterator();
		System.out.println("Usage:");
		System.out.println("Minimum arguments are -m for logol model or -g for logol grammar, and -s for input sequence");
		while(it.hasNext()) {
			Option opt = (Option) it.next();
			System.out.println(" - "+opt.getOpt()+" : "+opt.getDescription());
		}
		
	}	
	
	
	/**
	 * Convert a Logol output XML file to fasta format
	 * @param inputFile Logol output
	 * @param outputFile New fasta result file
	 */
	public static void convert2Fasta(String inputFile, String outputFile) {
		DocumentBuilder builder;
		Node matchNode=null;
	
		String res="";
		
		try {
			
			PrintWriter wr = new PrintWriter(new File(outputFile));

			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

			InputStream in = new FileInputStream(inputFile);


			Document document = builder.parse(in);

			String expressionData = "/sequences/match";
			String expressionHeader="/sequences/fastaHeader";
			matchNode = XPathAPI.selectSingleNode(document,expressionHeader);
			String header = matchNode.getTextContent();
		
			NodeList matchNodeList = XPathAPI.selectNodeList(document,expressionData);
			for(int j=0;j<matchNodeList.getLength();j++) {
		    NodeList childNodes = matchNodeList.item(j).getChildNodes();
			
			String sequenceData ="";
			String begin="";
			String end="";
			
			for(int i=0;i<childNodes.getLength();i++) {
				Node curNode = childNodes.item(i);
				if(curNode.getNodeName().equals("begin")) {
					begin=curNode.getTextContent();
					
				}
				if(curNode.getNodeName().equals("end")) {
					end=curNode.getTextContent();
				}
				if(curNode.getNodeName().equals("variable")) {
					NodeList lv_childNodes = curNode.getChildNodes();
					for(int k=0;k<lv_childNodes.getLength();k++) {
						Node lv_curNode = lv_childNodes.item(k);						
						if(lv_curNode.getNodeName().equals("content")) {
							if(lv_curNode.getTextContent().startsWith("NULL")) {
								sequenceData += "";
							}
							else { sequenceData+=lv_curNode.getTextContent(); }
							break;
						}
						
					}
				}
			}
			
			wr.println(">"+header+" "+begin+","+end);
			int length = sequenceData.length();
			// For fasta, max recommended line size is 80 chars 
			if(length>80) {
			int count = length/80;
			
			int beginIndex =0;
			int endIndex = 80;
				for(int c = 0; c < count+1; c++) {				
		    	wr.println(sequenceData.substring(beginIndex, endIndex));
		    	beginIndex+=80;
		    	if( ((beginIndex+80)<length ) ) { endIndex+=80; }
		    	else {
		    		endIndex = length-1;
		    	}
		    	
				}
		    
			}
			else {
				wr.write(sequenceData+"\n");
			}
			
			}
			
			in.close();
			wr.flush();
			wr.close();
			
		} catch (ParserConfigurationException e) {
			System.out.println("ERROR: "+e.getMessage());
		} catch (SAXException e) {
			System.out.println("ERROR: "+e.getMessage());
		} catch (IOException e) {
			System.out.println("ERROR: "+e.getMessage());
		} catch (TransformerException e) {
			System.out.println("ERROR: "+e.getMessage());
		}
		
		
	}
	
	
	public static void zipFiles(Vector<String> filenames, String outFilename) {
	    // These are the files to include in the ZIP file
	    
	    // Create a buffer for reading the files
	    byte[] buf = new byte[1024];
	    
	    try {
	        // Create the ZIP file
	        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outFilename));
	    
	        // Compress the files
	        for (int i=0; i<filenames.size(); i++) {
	        	File tmpFile = new File((String)filenames.get(i));
	        	if(tmpFile.exists()) {
	        	
	            FileInputStream in = new FileInputStream((String)filenames.get(i));	            
	            
	            // Add ZIP entry to output stream.
	            out.putNextEntry(new ZipEntry(tmpFile.getName()));
	    
	            // Transfer bytes from the file to the ZIP file
	            int len;
	            while ((len = in.read(buf)) > 0) {
	                out.write(buf, 0, len);
	            }
	    
	            // Complete the entry
	            out.closeEntry();
	            in.close();
	        	}
	        }
	    
	        // Complete the ZIP file
	        out.close();
	    } catch (IOException e) {
	    	logger.error("Error during zip file creation: "+e.getMessage());
	    }

	}
	
	/**
	 * Sends a mail to a list of recipents
	 * @param props Mail properties (see javax.mail), such as smtp host....
	 * @param recipients List of "To:"
	 * @param subject Subject of the message
	 * @param message Message to send
	 * @param from "From:" user to use
	 * @throws MessagingException
	 */
	public static void sendMail( Properties props,String recipients[ ], String subject, String message , String from) throws MessagingException 
	{
	    boolean debug = false;


	    // create some properties and get the default Session
	    Session session = Session.getDefaultInstance(props, null);
	    session.setDebug(debug);

	    // create a message
	    Message msg = new MimeMessage(session);

	    // set the from and to address
	    InternetAddress addressFrom = new InternetAddress(from);
	    msg.setFrom(addressFrom);

	    InternetAddress[] addressTo = new InternetAddress[recipients.length]; 
	    for (int i = 0; i < recipients.length; i++)
	    {
	        addressTo[i] = new InternetAddress(recipients[i]);
	    }
	    msg.setRecipients(Message.RecipientType.TO, addressTo);
	   

	    // Setting the Subject and Content Type
	    msg.setSubject(subject);
	    msg.setContent(message, "text/plain");
	    Transport.send(msg);
	}
	
	
	/**
	 * Counts the number of models required to match the sequence
	 * @param grammar Logol Grammar file
	 * @param model Model file
	 * @return Number of models found
	 * @throws IOException
	 * @throws GrammarException
	 */
	public static int countGrammarModels(String grammar, String model) throws IOException, GrammarException {

		String logol="";
		
		if(model!=null) {
			ModelConverter lgConv = new ModelConverter();
			try {
				logol = lgConv.encode(model);
			} catch (ParserConfigurationException e) {
				throw new GrammarException("Error while decoding model: "+e.getMessage());
			} catch (SAXException e) {
				throw new GrammarException("Error while decoding model: "+e.getMessage());
			} catch (TransformerException e) {
				throw new GrammarException("Error while decoding model: "+e.getMessage());
			} catch (ModelDefinitionException e) {
				throw new GrammarException("Error while decoding model: "+e.getMessage());
			}	
		}
		else if(grammar!=null) {
		FileReader fr     = new FileReader(grammar);
        BufferedReader br = new BufferedReader(fr);
        
        String line="";
        
        while((line=br.readLine())!=null) {
        	logol+=line+"\n";
        }
        br.close();
        fr.close();
		}
		else {
			throw new GrammarException("Error, both grammar and model are null");
		}
        
        String[] logolLines = logol.split("\\n");
        System.out.println(logolLines[0]);
        int count=0;
        for(int i=0;i<logolLines.length;i++) {
        	count=0;
        	if(logolLines[i].contains("==*>")) {
        		Pattern p = Pattern.compile("(mod\\d+)");
        		Matcher m = p.matcher(logolLines[i]);
        		String res=null;
        		while(m.find()) {        			
        			for(int j=1; j<=m.groupCount(); j++) {
        				count++;
        			}				
        		}
        	}
        }
        logger.debug("found "+count+" models");
        return count;
	}
	
	
	
	
	/**
	 * Split a sequence in several sub sequences according to size and number of processor
	 * @param sequenceSize size of input sequence
	 * @param nbjobs Max number of jobs (number of processors or number of drm jobs)
	 * @param minSplitSize minimum size to split a sequence
	 * @param maxSize Maximal size of a match
	 * @return	An array of posision on sequence to create sub-sequences
	 */
	public static Integer[][] splitSequence(int sequenceSize, int nbjobs, int minSplitSize, int maxSize,int nbModels) {
		
		logger.debug("Try to split: params are seqsize: "+sequenceSize+", nbjobs: "+nbjobs+", minsplitsize: "+minSplitSize+", maxSize: "+maxSize+", nbmodels: "+nbModels);
		
		int maxSplit=1;

		if(maxSize==0) {
			// If equal to 0, e.g. no max known, set it to sequenceSize.
			return new Integer[][] { {1,sequenceSize}};
		}
		
		int slicing = maxSize;
		
		if ( maxSize < minSplitSize) { 
			maxSize = minSplitSize;
		}
	
		// If maxSize = 0, there  is no limit on match result size, so file cannot be splitted
		// If several models, cannot split file because models applies to the whole sequence, not same part only
		// If sequence size is not large enough, sequence cannot be splitted
	     if(maxSize==0 || nbjobs==1 || nbModels>1 || (sequenceSize < (maxSize*3)) )  {
	    	 logger.info("Sequence not splitted, only 1 thread or job will analyse the sequence");
	    	 return new Integer[][] { {1,sequenceSize}};	    	 
	     }
	      //
	      // To split the sequence, it must be larger than 3 times the max result size.
	      // To find a result, we need twice its size in the sequence, so all parts of file must be >= maxResultSize*2.
	      //   |----------------------------| Whole sequence
	      //   |-----------------|			 First split = 2 * maxSize
	      //             |------------------| Second Split = 2 * maxSize
	      //
	    	 while((sequenceSize/(nbjobs+1))<(maxSize*2)) {
	    		 nbjobs--;
			 }

			 maxSplit = sequenceSize;
			 if(nbjobs>0) {
				 maxSplit = sequenceSize/nbjobs;
			 }
		 

	     logger.debug("file size = "+(sequenceSize));	     
	     logger.debug("split = "+maxSplit);
	     
	     int start=1;
	     int end=sequenceSize;     	    

	     if(nbjobs==0) {
	    	 return new Integer[][] { {1,sequenceSize}};
	     }
	     
	     Integer[][] subsequences = new Integer[nbjobs][2];
	     
	     for(int i=0;i<nbjobs;i++) {
	  
	    	 start = (i * maxSplit)+1;
	    	 if(i>0) {
	    		 //start -= maxSize;
	    		 start-=slicing;
	    	 }
	    	 if(i==(nbjobs-1)) {
	    		 end = sequenceSize;
	    	 }
	    	 else {
	    		 end = (i * maxSplit) + maxSplit;
	    	 }
	    	 subsequences[i][0]=start;
	    	 subsequences[i][1]=end;
	     
	     }
		
	     logger.info("Sequence splitted in "+subsequences.length+" subsequences.");
		
		return subsequences;
	}
	
	/**
	 * Copy a file content to an other
	 * @param inFile path to input
	 * @param outFile path to output
	 * @return true if copy succeeded, false in case of error
	 */
	public static boolean copyFile(String inFile, String outFile) {
		FileChannel in = null; // input channel
		FileChannel out = null; // output channel
		 
		boolean result = true;
		
		try {
		  // Init
		  in = new FileInputStream(inFile).getChannel();
		  out = new FileOutputStream(outFile).getChannel();
		 
		  // Copy from in to out
		  long nbTransfer = in.transferTo(0, in.size(), out);
		  if(nbTransfer==0) {
			  result=false;
		  }
		} catch (Exception e) {
		  logger.error(e.getMessage());
		  result=false;
		} finally { // close
		  if(in != null) {
		  	try {
			  in.close();
			} catch (IOException e) {
				return false;
			}
		  }
		  if(out != null) {
		  	try {
			  out.close();
			} catch (IOException e) {
				return false;
			}
		  }
		}
		return result;
		
	}
	
	/**
	 * Move a file to an other place or name
	 * @param inFile input file
	 * @param outFile output file
	 * @return true of move succeeded, else returns false
	 */
	public static boolean moveFile(String inFile,String outFile) {
		
		File in = new File(inFile);
		File out = new File(outFile);

	        if( !out.exists() ) {
	                // On essaye avec renameTo
	                boolean result = in.renameTo(out);
	                if( !result ) {
	                        // On essaye de copier
	                        result = true;
	                        result &= copyFile(inFile,outFile);
	                        if(result) {
	                        	result &= in.delete();
	                        }
	                        
	                } return(result);
	        } else {
	                // If destination file exist, do not move
	                return(false);
	        } 
	

	}
	
	public static int countMatch(String xmlFile) {
		int count=0;
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
	        parser.parse(xmlFile, (DefaultHandler)handler);
	        count = handler.getTotalCount();

	      } catch(Exception e) {
	    	  logger.error(e.getMessage());
	      }
	      logger.debug(xmlFile+": "+count);
		 return count;		
	}

	public static int countMatches(Vector<String> xmlFiles) {
		int totalCount=0;
        logger.info("**** Results ****");
	    for(int i=0;i<xmlFiles.size();i++) {
	        String xmlFile = xmlFiles.get(i);
	        int count = countMatch(xmlFile);
	        totalCount += count;
	        logger.info("*\t"+xmlFile+"\t"+count);
   
	    }    
	     logger.info("****\tTotal matches:\t"+totalCount);
		 return totalCount;
	}
	
}
