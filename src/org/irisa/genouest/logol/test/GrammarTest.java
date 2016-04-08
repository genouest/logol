package org.irisa.genouest.logol.test;

import static org.junit.Assert.*;
import static org.junit.Assume.*;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import junit.framework.TestCase;

import org.antlr.runtime.RecognitionException;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.apache.xpath.XPathAPI;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.Sequence;
import org.biojavax.bio.seq.RichSequenceIterator;
import org.irisa.genouest.logol.GrammarException;
import org.irisa.genouest.logol.Logol;
import org.irisa.genouest.logol.LogolVariable;
import org.irisa.genouest.logol.Treatment;

import org.irisa.genouest.logol.types.ViewVariable;
import org.irisa.genouest.logol.utils.MyRevCompSequence;
import org.irisa.genouest.logol.utils.MySequence;
import org.irisa.genouest.logol.utils.converter.FastaConverter;
import org.irisa.genouest.logol.utils.converter.GFFConverter;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class GrammarTest {

	private static final Logger logger = Logger.getLogger(org.irisa.genouest.logol.test.GrammarTest.class);

	private static final String OS = System.getProperty("os.name").toLowerCase();


	private static final String FILESEPARATORPROPERTY = "file.separator";

	private static String sequenceFile= "test.fasta";

	private static final String propFile="logol.properties";

	public static String installDir = ".";

	private static final int start=0;
	private static final int end=45;

	private static PropertiesConfiguration config = null;

	String workDir = null;

	private void init() {
		if(config==null) {
			 String configFile = installDir+System.getProperty(FILESEPARATORPROPERTY)+"test"+System.getProperty(FILESEPARATORPROPERTY)+"logol.properties";
			 File  lv_file = new File(configFile);

			 try {
					config = new PropertiesConfiguration(lv_file.getAbsolutePath());
				} catch (ConfigurationException e1) {
					fail();
				}
		}
	Logol.reset();
	Treatment.reset();
	ViewVariable.reset();
	LogolVariable.reset();

	String path = System.getProperty("logol.install");
	if(path!=null) {
		installDir = path;
	}
	sequenceFile= "test.fasta";

	}

	 private static void copyFile(String srFile, String dtFile){
		    try{
		      File f1 = new File(srFile);
		      File f2 = new File(dtFile);
		      InputStream in = new FileInputStream(f1);
		      //To Overwrite the file.
		      OutputStream out = new FileOutputStream(f2);

		      byte[] buf = new byte[1024];
		      int len;
		      while ((len = in.read(buf)) > 0){
		        out.write(buf, 0, len);
		      }
		      in.close();
		      out.close();
		      System.out.println("File copied.");
		    }
		    catch(FileNotFoundException ex){
		      fail(ex.getMessage() + " in the specified directory.");
		      System.exit(0);
		    }
		    catch(IOException e){
		      fail(e.getMessage());
		    }
		  }




		private void checkNoMatch(Vector result,int fileID) throws ParserConfigurationException, SAXException, IOException, TransformerException {
			String resFilePath = Logol.getOutputSeqName();

			File resFile = new File(resFilePath);

			logger.info("Checking file "+resFilePath);

			assertTrue(resFile.exists());


			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document = builder.parse(resFile);
			XPath xpath = XPathFactory.newInstance().newXPath();
			String expression=null;

			expression = "/sequences/match";

			Node matchNode = XPathAPI.selectSingleNode(document,expression);
			assertTrue(matchNode==null);

		}

		 /**
		  * Count number of matches.
		  * @param fileID
		  * @throws ParserConfigurationException
		  * @throws SAXException
		  * @throws IOException
		  * @throws TransformerException
		  */
		private int getNbResult(int fileID) throws ParserConfigurationException, SAXException, IOException, TransformerException {
			String resFilePath = Logol.getOutputSeqName();

			File resFile = new File(resFilePath);

			logger.info("Checking file "+resFilePath);

			assertTrue(resFile.exists());

			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document = builder.parse(resFile);
			String expression=null;


			expression = "/sequences/match";
			NodeList matchNode = XPathAPI.selectNodeList(document,expression);
			int nb = 0;
			if(matchNode!=null) {
				nb = matchNode.getLength();
			}
			return nb;
		}
	 /**
	  * Checks variable begin and end position based on a Vector of test positions. Only first level of variable node can be tested.
	  * Sub nodes e.g. sub-variable does cannot be tested by this function.
	  * @param result
	  * @param fileID
	  * @throws ParserConfigurationException
	  * @throws SAXException
	  * @throws IOException
	  * @throws TransformerException
	  */
	private void checkResult(Vector result,int fileID) throws ParserConfigurationException, SAXException, IOException, TransformerException {
		String resFilePath = Logol.getOutputSeqName();

		File resFile = new File(resFilePath);

		logger.info("Checking file "+resFilePath);

		assertTrue(resFile.exists());

		if(result==null) {
			return;
		}

		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document document = builder.parse(resFile);
		XPath xpath = XPathFactory.newInstance().newXPath();
		String expression=null;


		for(int i=0;i<result.size();i++) {
		 expression = "/sequences/match/variable[@name='"+((String[]) result.get(i))[0]+"']";

		Node matchNode = XPathAPI.selectSingleNode(document,expression);
		assertTrue(matchNode!=null);
		NodeList variableData = matchNode.getChildNodes();

		int nbDataNode = variableData.getLength();
		for (int j=0;j<nbDataNode;j++) {
			Node dataNode = variableData.item(j);
			if(dataNode!=null && dataNode.getNodeName()!=null) {

			if(dataNode.getNodeName().equals("begin")) {
				String value=dataNode.getFirstChild().getNodeValue();
				assertTrue(value.equals(((String[]) result.get(i))[1]));
			}
			if(dataNode.getNodeName().equals("end")) {
				String value=dataNode.getFirstChild().getNodeValue();
				assertTrue(value.equals(((String[]) result.get(i))[2]));
			}
			}
		}



		}

		assertTrue(1==1);

	}

	private void execute(String sequence, String grammarFile, boolean doReverse) {
		sequenceFile = sequence;
		execute(grammarFile, doReverse);
	}

	private void execute(String grammarFile, boolean doReverse) {

		String[] args = new String[] { "-conf",installDir+System.getProperty(FILESEPARATORPROPERTY)+"test"+System.getProperty(FILESEPARATORPROPERTY)+"logol.properties" , "-g",installDir+System.getProperty(FILESEPARATORPROPERTY)+"test"+System.getProperty(FILESEPARATORPROPERTY)+grammarFile , "-s",installDir+System.getProperty(FILESEPARATORPROPERTY)+"test"+System.getProperty(FILESEPARATORPROPERTY)+sequenceFile, "-max"," 10", "-all"};
		try {
			Logol.main(args);
		} catch (ConfigurationException e) {
			fail(e.getMessage());
		} catch (IOException e) {
			fail(e.getMessage());
		}  catch (InterruptedException e) {
			fail(e.getMessage());
		} catch (ParseException e) {
			fail(e.getMessage());
		} catch (GrammarException e) {
			fail(e.getMessage());
		}


	}

	private void execute(String sequence, String grammarFile) {
		sequenceFile = sequence;
		execute(grammarFile);
	}

	private void executeProtein(String sequence, String grammarFile) {
		sequenceFile = sequence;
		executeProtein(grammarFile);
	}

	private void execute(String grammarFile) {
		String[] args = new String[] { "-conf",installDir+System.getProperty(FILESEPARATORPROPERTY)+"test"+System.getProperty(FILESEPARATORPROPERTY)+"logol.properties" , "-g",installDir+System.getProperty(FILESEPARATORPROPERTY)+"test"+System.getProperty(FILESEPARATORPROPERTY)+grammarFile , "-s",installDir+System.getProperty(FILESEPARATORPROPERTY)+"test"+System.getProperty(FILESEPARATORPROPERTY)+sequenceFile, "-max","10"};
		try {
			Logol.main(args);
		} catch (ConfigurationException e) {
			fail(e.getMessage());
		} catch (IOException e) {
			fail(e.getMessage());
		} catch (InterruptedException e) {
			fail(e.getMessage());
		} catch (ParseException e) {
			fail(e.getMessage());
		} catch (GrammarException e) {
			fail(e.getMessage());
		}

	}

	private void executeProtein(String grammarFile) {
		String[] args = new String[] { "-protein", "-conf",installDir+System.getProperty(FILESEPARATORPROPERTY)+"test"+System.getProperty(FILESEPARATORPROPERTY)+"logol.properties" , "-g",installDir+System.getProperty(FILESEPARATORPROPERTY)+"test"+System.getProperty(FILESEPARATORPROPERTY)+grammarFile , "-s",installDir+System.getProperty(FILESEPARATORPROPERTY)+"test"+System.getProperty(FILESEPARATORPROPERTY)+sequenceFile, "-max","10"};
		try {
			Logol.main(args);
		} catch (ConfigurationException e) {
			fail(e.getMessage());
		} catch (IOException e) {
			fail(e.getMessage());
		} catch (InterruptedException e) {
			fail(e.getMessage());
		} catch (ParseException e) {
			fail(e.getMessage());
		} catch (GrammarException e) {
			fail(e.getMessage());
		}

	}

	private void executeModel(String sequence, String grammarFile, boolean doReverse) {
		sequenceFile = sequence;
		executeModel(grammarFile, doReverse);
	}

	private void executeModel(String sequence, String grammarFile) {
		sequenceFile = sequence;
		executeModel(grammarFile);
	}

	private void executeModel(String modelFile) {
		String[] args = new String[] { "-conf",installDir+System.getProperty(FILESEPARATORPROPERTY)+"test"+System.getProperty(FILESEPARATORPROPERTY)+"logol.properties" , "-m",installDir+System.getProperty(FILESEPARATORPROPERTY)+"test"+System.getProperty(FILESEPARATORPROPERTY)+modelFile , "-s",installDir+System.getProperty(FILESEPARATORPROPERTY)+"test"+System.getProperty(FILESEPARATORPROPERTY)+sequenceFile, "-max","10"};
		try {
			Logol.main(args);
		} catch (ConfigurationException e) {
			fail(e.getMessage());
		} catch (IOException e) {
			fail(e.getMessage());
		} catch (InterruptedException e) {
			fail(e.getMessage());
		} catch (ParseException e) {
			fail(e.getMessage());
		} catch (GrammarException e) {
			fail(e.getMessage());
		}

	}

	private void executeModel(String modelFile, boolean doReverse) {
		String[] args = new String[] { "-conf",installDir+System.getProperty(FILESEPARATORPROPERTY)+"test"+System.getProperty(FILESEPARATORPROPERTY)+"logol.properties" , "-m",installDir+System.getProperty(FILESEPARATORPROPERTY)+"test"+System.getProperty(FILESEPARATORPROPERTY)+modelFile , "-s",installDir+System.getProperty(FILESEPARATORPROPERTY)+"test"+System.getProperty(FILESEPARATORPROPERTY)+sequenceFile, "-max","10","-all"};
		try {
			Logol.main(args);
		} catch (ConfigurationException e) {
			fail(e.getMessage());
		} catch (IOException e) {
			fail(e.getMessage());
		} catch (InterruptedException e) {
			fail(e.getMessage());
		} catch (ParseException e) {
			fail(e.getMessage());
		} catch (GrammarException e) {
			fail(e.getMessage());
		}

	}
	@Test
	public void testHalt() {
		init();

		   Vector<String[]> result = new Vector<String[]>();
		   result.add(new String[] {"LogolVAR_3","0","0"});
		   result.add(new String[] {"LogolVAR_4","1","1"});
		   result.add(new String[] {"LogolVAR_5","2","2"});

		execute("halt.logol",true);

		   try {
			   checkResult(result,0);
		   } catch (ParserConfigurationException e) {
			   fail(e.getMessage());
		   } catch (SAXException e) {
			   fail(e.getMessage());
		   } catch (IOException e) {
			   fail(e.getMessage());
		   } catch (TransformerException e) {
			   fail(e.getMessage());
		   }

	}

     @Test
	 public void testPartialReverseTesting() {
		// Test with offset and partial file content
                String[] args = new String[] { "-conf",installDir+System.getProperty(FILESEPARATORPROPERTY)+"test"+System.getProperty(FILESEPARATORPROPERTY)+"logol.properties" , "-g",installDir+System.getProperty(FILESEPARATORPROPERTY)+"test"+System.getProperty(FILESEPARATORPROPERTY)+"partialcomplement.logol" , "-s",installDir+System.getProperty(FILESEPARATORPROPERTY)+"test"+System.getProperty(FILESEPARATORPROPERTY)+sequenceFile, "-max"," 10", "-all","-start","5","-end","35","-offset","1000"};
                try {
                        Logol.main(args);
                        Vector<String[]> result = new Vector<String[]>();
                        result.add(new String[] {"LogolVAR_1","1030","1027"});
                        result.add(new String[] {"LogolVAR_2","1026","1019"});
                        result.add(new String[] {"LogolVAR_3","1018","1015"});
			checkResult(result,0);
                } catch (ConfigurationException e) {
                        fail(e.getMessage());
                } catch (IOException e) {
                        fail(e.getMessage());
                }  catch (InterruptedException e) {
                        fail(e.getMessage());
                } catch (ParseException e) {
                        fail(e.getMessage());
                } catch (GrammarException e) {
                        fail(e.getMessage());
                }
                  catch (ParserConfigurationException e) {
                                fail(e.getMessage());
                        } catch (SAXException e) {
                                fail(e.getMessage());
                        } catch (TransformerException e) {
                                fail(e.getMessage());
                        }


}


	 @Test
	 public void testReverseTesting() {
		 init();

			Vector<String[]> result = new Vector<String[]>();
			result.add(new String[] {"LogolVAR_1","0","2"});
			result.add(new String[] {"LogolVAR_2","3","14"});
			result.add(new String[] {"LogolVAR_3","15","17"});

		 execute("all.logol",true);

			try {
				checkResult(result,0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}

	 }

                @Test
                public void testComplementTesting() {
                   init();

                        Vector<String[]> result = new Vector<String[]>();
                        result.add(new String[] {"LogolVAR_1","30","27"});
                        result.add(new String[] {"LogolVAR_2","26","19"});
                        result.add(new String[] {"LogolVAR_3","18","15"});

                        execute("complement.logol",true);

                        try {
                                checkResult(result,0);
                        } catch (ParserConfigurationException e) {
                                fail(e.getMessage());
                        } catch (SAXException e) {
                                fail(e.getMessage());
                        } catch (IOException e) {
                                fail(e.getMessage());
                        } catch (TransformerException e) {
                                fail(e.getMessage());
                        }

         }

	 @Test
	 public void testModelReverseTesting() {
		 init();

			Vector<String[]> result = new Vector<String[]>();
			result.add(new String[] {"LogolVAR_1","0","2"});
			result.add(new String[] {"LogolVAR_2","3","14"});
			result.add(new String[] {"LogolVAR_3","15","17"});

		 executeModel("all.lgd",true);

			try {
				checkResult(result,0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}

	 }

	 @Test
	 public void testVariableUnknown(){
			init();

			Vector<String[]> result = new Vector<String[]>();
			result.add(new String[] {"LogolVAR_1","4","6"});
			result.add(new String[] {"LogolVAR_2","7","10"});
			result.add(new String[] {"LogolVAR_3","11","14"});

			execute("variable_unknown.logol");

			try {
				checkResult(result,0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}
	 }

	@Test
	public void testVariable() {

		init();

		Vector<String[]> result = new Vector<String[]>();
		result.add(new String[] {"LogolVAR_1","4","6"});
		result.add(new String[] {"LogolVAR_2","7","10"});
		result.add(new String[] {"LogolVAR_3","11","14"});

		execute("variable.logol");

		try {
			checkResult(result,0);
		} catch (ParserConfigurationException e) {
			fail(e.getMessage());
		} catch (SAXException e) {
			fail(e.getMessage());
		} catch (IOException e) {
			fail(e.getMessage());
		} catch (TransformerException e) {
			fail(e.getMessage());
		}

	}

	@Test
	public void testModelVariable() {

		init();

		Vector<String[]> result = new Vector<String[]>();
		result.add(new String[] {"LogolVAR_1","4","6"});
		result.add(new String[] {"LogolVAR_2","7","10"});
		result.add(new String[] {"LogolVAR_3","11","14"});

		executeModel("variable.lgd");

		try {
			checkResult(result,0);
		} catch (ParserConfigurationException e) {
			fail(e.getMessage());
		} catch (SAXException e) {
			fail(e.getMessage());
		} catch (IOException e) {
			fail(e.getMessage());
		} catch (TransformerException e) {
			fail(e.getMessage());
		}

	}


	@Test
	public void testComment() {

		init();

		Vector<String[]> result = new Vector<String[]>();
		result.add(new String[] {"LogolVAR_1","4","6"});
		result.add(new String[] {"LogolVAR_2","7","10"});
		result.add(new String[] {"LogolVAR_3","11","14"});

		execute("comment.logol");

		try {
			checkResult(result,0);
		} catch (ParserConfigurationException e) {
			fail(e.getMessage());
		} catch (SAXException e) {
			fail(e.getMessage());
		} catch (IOException e) {
			fail(e.getMessage());
		} catch (TransformerException e) {
			fail(e.getMessage());
		}

	}

	@Test
	public void testComment2() {
		// validate comment lines, takes same search than testComment
		init();

		Vector<String[]> result = new Vector<String[]>();
		result.add(new String[] {"LogolVAR_1","4","6"});
		result.add(new String[] {"LogolVAR_2","7","10"});
		result.add(new String[] {"LogolVAR_3","11","14"});

		execute("comment2.logol");

		try {
			checkResult(result,0);
		} catch (ParserConfigurationException e) {
			fail(e.getMessage());
		} catch (SAXException e) {
			fail(e.getMessage());
		} catch (IOException e) {
			fail(e.getMessage());
		} catch (TransformerException e) {
			fail(e.getMessage());
		}

	}


	 @Test
	 public void testCostPostpone(){
			init();

			Vector<String[]> result = new Vector<String[]>();
			result.add(new String[] {"LogolVAR_1","4","7"});
			result.add(new String[] {"LogolVAR_2","8","11"});
			result.add(new String[] {"LogolVAR_3","12","15"});


			execute("cost_postpone.logol");


			try {
				checkResult(result,0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}

	 }


	 @Test
	 public void testBeginPostpone(){
			init();

			Vector<String[]> result = new Vector<String[]>();
			result.add(new String[] {"LogolVAR_1","4","7"});
			result.add(new String[] {"LogolVAR_2","8","10"});
			result.add(new String[] {"LogolVAR_3","11","12"});


			execute("begin_postpone.logol");


			try {
				checkResult(result,0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}

	 }

	 @Test
	 public void testBegin(){
			init();

			Vector<String[]> result = new Vector<String[]>();
			result.add(new String[] {"LogolVAR_1","4","7"});
			result.add(new String[] {"LogolVAR_2","8","9"});
			result.add(new String[] {"LogolVAR_3","10","11"});

			execute("begin.logol");


			try {
				checkResult(result,0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}

	 }

	 @Test
	 public void testAmbiguous(){

			init();

			assumeTrue(config.getInt("suffix.tool")==0);

			Vector<String[]> result = new Vector<String[]>();
			result.add(new String[] {"LogolVAR_1","4","7"});
			result.add(new String[] {"LogolVAR_2","8","9"});
			result.add(new String[] {"LogolVAR_3","10","11"});

			execute("ambiguous.logol");


			try {
				checkResult(result,0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}

	 }

	 @Test
	 public void testModelBegin(){
			init();

			Vector<String[]> result = new Vector<String[]>();
			result.add(new String[] {"LogolVAR_1","4","7"});
			result.add(new String[] {"LogolVAR_2","8","9"});
			result.add(new String[] {"LogolVAR_3","10","11"});

			executeModel("begin.lgd");


			try {
				checkResult(result,0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}

	 }

	 @Test
	 public void testCallModel(){
			init();

			Vector<String[]> result = new Vector<String[]>();
			result.add(new String[] {"LogolVAR_1","4","6"});
			result.add(new String[] {"LogolVAR_2","7","10"});
			result.add(new String[] {"LogolVAR_3","11","14"});
			execute("call_model.logol");


			try {
				checkResult(result,0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}

	 }

	 @Test
	 public void testCallModel2(){
			init();

			Vector<String[]> result = new Vector<String[]>();
			result.add(new String[] {"LogolVAR_1","4","6"});
			result.add(new String[] {"LogolVAR_2","7","10"});
			result.add(new String[] {"LogolVAR_3","11","14"});
			execute("call_model2.logol");


			try {
				checkResult(result,0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}

	 }

	 @Test
	 public void testCallModel3(){
			init();

			Vector<String[]> result = new Vector<String[]>();
			result.add(new String[] {"LogolVAR_1","4","10"});
			execute("call_model3.logol");


			try {
				checkResult(result,0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}

	 }

	 @Test
	 public void testCallModel4(){
			init();

			Vector<String[]> result = new Vector<String[]>();
			result.add(new String[] {"LogolVAR_1","13","16"});
			execute("call_model4.logol");


			try {
				checkResult(result,0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}

	 }

	 @Test
	 public void testEnd(){
			init();

			Vector<String[]> result = new Vector<String[]>();
			result.add(new String[] {"LogolVAR_1","4","7"});
			result.add(new String[] {"LogolVAR_3","8","12"});

			execute("end.logol");


			try {
				checkResult(result,0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}

	 }

	 @Test
	 public void testModelMorphism(){
			init();

			Vector<String[]> result = new Vector<String[]>();
			result.add(new String[] {"LogolVAR_1","4","6"});
			result.add(new String[] {"LogolVAR_2","7","10"});

			executeModel("morphism.lgd");


			try {
				checkResult(result,0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}

	 }


	 @Test
	 public void testMorphism(){
			init();

			Vector<String[]> result = new Vector<String[]>();
			result.add(new String[] {"LogolVAR_1","4","6"});
			result.add(new String[] {"LogolVAR_2","7","10"});

			execute("morphism.logol");


			try {
				checkResult(result,0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}

	 }

	 @Test
	 public void testMultipleModel(){
			init();

			Vector<String[]> result = new Vector<String[]>();
			result.add(new String[] {"LogolVAR_1","4","6"});
			result.add(new String[] {"LogolVAR_2","7","10"});
			result.add(new String[] {"LogolVAR_3","7","10"});

			execute("multiple_model.logol");


			try {
				checkResult(result,0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}

	 }

	 @Test
	 public void testOrCondition(){
			init();

			Vector<String[]> result = new Vector<String[]>();
			result.add(new String[] {"LogolVAR_1","4","7"});
			result.add(new String[] {"LogolVAR_2","8","14"});

			execute("or_condition.logol");


			try {
				checkResult(result,0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}

	 }

	 @Test
	 public void testOrCondition2(){
			init();

			Vector<String[]> result = new Vector<String[]>();
			result.add(new String[] {"LogolVAR_1","7","10"});

			execute("or_condition2.logol");


			try {
				checkResult(result,0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}

	 }

	 @Test
	 public void testOverlap(){
			init();

			Vector<String[]> result = new Vector<String[]>();
			result.add(new String[] {"LogolVAR_1","2","7"});
			result.add(new String[] {"LogolVAR_2","6","13"});

			execute("overlap.logol");


			try {
				checkResult(result,0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}

	 }

	 @Test
	 public void testParentExact(){
			init();

			Vector<String[]> result = new Vector<String[]>();
			result.add(new String[] {"LogolVAR_1","4","6"});
			result.add(new String[] {"LogolVAR_2","7","10"});
			result.add(new String[] {"LogolVAR_3","11","14"});

			execute("parent_exact.logol");


			try {
				checkResult(result,0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}

	 }

	 @Test
	 public void testParentError(){
			init();

			Vector<String[]> result = new Vector<String[]>();
			result.add(new String[] {"LogolVAR_1","4","6"});
			result.add(new String[] {"LogolVAR_2","7","10"});
			result.add(new String[] {"LogolVAR_3","11","14"});

			execute("parent_witherror.logol");


			try {
				checkResult(result,0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}

	 }

	 @Test
	 public void testParentError2(){
			init();

			Vector<String[]> result = new Vector<String[]>();
			result.add(new String[] {"LogolVAR_1","4","7"});
			result.add(new String[] {"LogolVAR_2","8","11"});
			result.add(new String[] {"LogolVAR_3","12","15"});

			execute("parent_witherror2.logol");


			try {
				checkResult(result,0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}

	 }

	 @Test
	 public void testRepeatAny(){
			init();

			Vector<String[]> result = new Vector<String[]>();
			result.add(new String[] {"LogolVAR_1","4","6"});
			result.add(new String[] {"LogolVAR_2","7","14"});

			execute("repeat_any.logol");


			try {
				checkResult(result,0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}

	 }

	 @Test
	 public void testRepeatInterSpacer(){
			init();

			Vector<String[]> result = new Vector<String[]>();
			result.add(new String[] {"LogolVAR_1","4","6"});
			result.add(new String[] {"LogolVAR_2","7","14"});


			execute("repeat_interspacer.logol");


			try {
				checkResult(result,0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}

	 }

	 @Test
	 public void testRepeatOverlap(){
			init();

			Vector<String[]> result = new Vector<String[]>();
			result.add(new String[] {"LogolVAR_1","27","30"});
			result.add(new String[] {"LogolVAR_2","31","37"});

			execute("repeat_overlap.logol");


			try {
				checkResult(result,0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}

	 }

	 @Test
	 public void testRepeatStartSpacer(){
			init();

			Vector<String[]> result = new Vector<String[]>();
			result.add(new String[] {"LogolVAR_1","4","6"});
			result.add(new String[] {"LogolVAR_3","17","19"});

			execute("repeat_startspacer.logol");


			try {
				checkResult(result,0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}

	 }

	 @Test
	 public void testRepeat(){
			init();

			Vector<String[]> result = new Vector<String[]>();
			result.add(new String[] {"LogolVAR_1","4","6"});
			result.add(new String[] {"LogolVAR_2","7","10"});

			execute("repeat.logol");


			try {
				checkResult(result,0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}

	 }

	 @Test
	 public void testRepeat2(){
			init();

			Vector<String[]> result = new Vector<String[]>();
			result.add(new String[] {"LogolVAR_1","4","6"});
			result.add(new String[] {"LogolVAR_2","7","10"});

			execute("repeat2.logol");


			try {
				checkResult(result,0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}

	 }

	 @Test
	 public void testRepeatAnySpacer(){
			init();

			execute("repeat_anyspacer.logol");

			try {
				int count = getNbResult(0);
				assertTrue(count>0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}

	 }

	 @Test
	 public void testReverse(){
			init();

			Vector<String[]> result = new Vector<String[]>();
			result.add(new String[] {"LogolVAR_1","0","2"});
			result.add(new String[] {"LogolVAR_2","3","5"});
			result.add(new String[] {"LogolVAR_3","6","8"});

			execute("reverse.logol");


			try {
				checkResult(result,0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}

	 }

	 @Test
	 public void testVariablePostContent(){
			init();

			Vector<String[]> result = new Vector<String[]>();
			result.add(new String[] {"LogolVAR_1","4","6"});
			result.add(new String[] {"LogolVAR_2","7","10"});
			result.add(new String[] {"LogolVAR_3","11","14"});

			execute("variable_postcontent.logol");


			try {
				checkResult(result,0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}

	 }

	 @Test
	 public void testVariablePostContentWithError(){
			init();

			Vector<String[]> result = new Vector<String[]>();
			result.add(new String[] {"LogolVAR_1","4","6"});
			result.add(new String[] {"LogolVAR_2","7","8"});
			result.add(new String[] {"LogolVAR_3","9","10"});

			execute("variable_postcontent_witherror.logol");


			try {
				checkResult(result,0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}

	 }

	 @Test
	 public void testVariableWithError(){
			init();

			Vector<String[]> result = new Vector<String[]>();
			result.add(new String[] {"LogolVAR_1","16","18"});
			result.add(new String[] {"LogolVAR_2","19","22"});
			result.add(new String[] {"LogolVAR_3","23","26"});

			execute("variablewitherror.logol");


			try {
				checkResult(result,0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}

	 }

	 @Test
	 public void testVariableWithDistance(){
			init();

			Vector<String[]> result = new Vector<String[]>();
			result.add(new String[] {"LogolVAR_1","16","18"});
			result.add(new String[] {"LogolVAR_2","19","22"});

			execute("variablewithdistance.logol");


			try {
				checkResult(result,0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}

	 }



	 @Test
	 public void testVariableWithDistance2(){
			init();

			Vector<String[]> result = null;

			execute("variablewithdistance2.logol");


			try {
				checkNoMatch(result,0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}

	 }

	 @Test
	 public void testVariableWithErrorAndDistance(){
			init();

			Vector<String[]> result = new Vector<String[]>();
			result.add(new String[] {"LogolVAR_1","16","18"});
			result.add(new String[] {"LogolVAR_2","19","22"});

			execute("variablewitherroranddistance.logol");


			try {
				checkResult(result,0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}

	 }


	 @Test
	 public void testMyCost(){

		 	assumeTrue(! OS.startsWith("win"));

			init();

			Vector<String[]> result = new Vector<String[]>();
			result.add(new String[] {"LogolVAR_1","16","18"});
			result.add(new String[] {"LogolVAR_2", "19","22"});
			result.add(new String[] {"LogolVAR_3","23","25"});

			execute("mycost.logol");


			try {
				checkResult(result,0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}

	 }


	 @Test
	 public void testAnySpacer(){
			init();

			Vector<String[]> result = new Vector<String[]>();
			result.add(new String[] {"LogolVAR_1","0","14"});
			result.add(new String[] {"LogolVAR_2", "15","17"});

			execute("anyspacer.logol");


			try {
				checkResult(result,0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}

	 }

	 @Test
	 public void testModelAnySpacer(){
			init();

			Vector<String[]> result = new Vector<String[]>();
			result.add(new String[] {"LogolVAR_1","0","14"});
			result.add(new String[] {"LogolVAR_2", "15","17"});

			executeModel("anyspacer.lgd");


			try {
				checkResult(result,0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}

	 }

	 @Test
	 public void testVMatch() {
		 init();

		 String vmatchconfigFile = installDir+System.getProperty(FILESEPARATORPROPERTY)+"test"+System.getProperty(FILESEPARATORPROPERTY)+"vmatch.properties";
		 File  lv_file = new File(vmatchconfigFile);
		 PropertiesConfiguration vmatchconfig  = null;

		 try {
			 vmatchconfig = new PropertiesConfiguration(lv_file.getAbsolutePath());
			} catch (ConfigurationException e1) {
				fail();
			}

			assumeTrue(vmatchconfig.getInt("suffix.tool")==1);
			File vmatch = new File(vmatchconfig.getString("suffix.path")+"/vmatch");
			assumeTrue(vmatch.exists());

			Vector<String[]> result = new Vector<String[]>();
			result.add(new String[] {"LogolVAR_1","4","11"});
			result.add(new String[] {"LogolVAR_2","12","31"});
			result.add(new String[] {"LogolVAR_3","32","48"});
			result.add(new String[] {"LogolVAR_4","49","68"});
			result.add(new String[] {"LogolVAR_5","69","76"});


			String[] args = new String[] { "-conf",installDir+System.getProperty(FILESEPARATORPROPERTY)+"test"+System.getProperty(FILESEPARATORPROPERTY)+"vmatch.properties" , "-g",installDir+System.getProperty(FILESEPARATORPROPERTY)+"test"+System.getProperty(FILESEPARATORPROPERTY)+"vmatch.logol" , "-s",installDir+System.getProperty(FILESEPARATORPROPERTY)+"test"+System.getProperty(FILESEPARATORPROPERTY)+"vmatch.fasta", "-max"," 10", "-all"};
			try {
				Logol.main(args);
			} catch (ConfigurationException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			}  catch (InterruptedException e) {
				fail(e.getMessage());
			} catch (ParseException e) {
				fail(e.getMessage());
			} catch (GrammarException e) {
				fail(e.getMessage());
			}


			try {
				checkResult(result,0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}

	 }

	 @Test
	 public void testVMatch2() {
		 init();

		 String vmatchconfigFile = installDir+System.getProperty(FILESEPARATORPROPERTY)+"test"+System.getProperty(FILESEPARATORPROPERTY)+"vmatch.properties";
		 File  lv_file = new File(vmatchconfigFile);
		 PropertiesConfiguration vmatchconfig  = null;

		 try {
			 vmatchconfig = new PropertiesConfiguration(lv_file.getAbsolutePath());
			} catch (ConfigurationException e1) {
				fail();
			}

			assumeTrue(vmatchconfig.getInt("suffix.tool")==1);
			File vmatch = new File(vmatchconfig.getString("suffix.path")+"/vmatch");
			assumeTrue(vmatch.exists());

			Vector<String[]> result = new Vector<String[]>();
			result.add(new String[] {"LogolVAR_1","1","3"});


			String[] args = new String[] { "-conf",installDir+System.getProperty(FILESEPARATORPROPERTY)+"test"+System.getProperty(FILESEPARATORPROPERTY)+"vmatch.properties" , "-g",installDir+System.getProperty(FILESEPARATORPROPERTY)+"test"+System.getProperty(FILESEPARATORPROPERTY)+"vmatch2.logol" , "-s",installDir+System.getProperty(FILESEPARATORPROPERTY)+"test"+System.getProperty(FILESEPARATORPROPERTY)+"vmatch.fasta", "-max"," 10", "-all"};
			try {
				Logol.main(args);
			} catch (ConfigurationException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			}  catch (InterruptedException e) {
				fail(e.getMessage());
			} catch (ParseException e) {
				fail(e.getMessage());
			} catch (GrammarException e) {
				fail(e.getMessage());
			}


			try {
				checkResult(result,0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}

	 }

	 @Test
	 public void testVMatch3() {
		 init();

		 String vmatchconfigFile = installDir+System.getProperty(FILESEPARATORPROPERTY)+"test"+System.getProperty(FILESEPARATORPROPERTY)+"vmatch.properties";
		 File  lv_file = new File(vmatchconfigFile);
		 PropertiesConfiguration vmatchconfig  = null;

		 try {
			 vmatchconfig = new PropertiesConfiguration(lv_file.getAbsolutePath());
			} catch (ConfigurationException e1) {
				fail();
			}

			assumeTrue(vmatchconfig.getInt("suffix.tool")==1);
			File vmatch = new File(vmatchconfig.getString("suffix.path")+"/vmatch");
			assumeTrue(vmatch.exists());

			Vector<String[]> result = new Vector<String[]>();
			result.add(new String[] {"LogolVAR_1","55","58"});


			String[] args = new String[] { "-conf",installDir+System.getProperty(FILESEPARATORPROPERTY)+"test"+System.getProperty(FILESEPARATORPROPERTY)+"vmatch.properties" , "-g",installDir+System.getProperty(FILESEPARATORPROPERTY)+"test"+System.getProperty(FILESEPARATORPROPERTY)+"vmatch3.logol" , "-s",installDir+System.getProperty(FILESEPARATORPROPERTY)+"test"+System.getProperty(FILESEPARATORPROPERTY)+"vmatch.fasta", "-max"," 10", "-all"};
			try {
				Logol.main(args);
			} catch (ConfigurationException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			}  catch (InterruptedException e) {
				fail(e.getMessage());
			} catch (ParseException e) {
				fail(e.getMessage());
			} catch (GrammarException e) {
				fail(e.getMessage());
			}


			try {
				checkResult(result,0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}

	 }

	 @Test
	 public void testNegContent(){
			init();

			Vector<String[]> result = new Vector<String[]>();
			result.add(new String[] {"LogolVAR_1","4","7"});
			result.add(new String[] {"LogolVAR_2", "8","10"});

			execute("negcontent.logol");


			try {
				checkResult(result,0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}

	 }

	 @Test
	 public void testNegContent2(){
			init();

			Vector<String[]> result = new Vector<String[]>();
			result.add(new String[] {"LogolVAR_1","0","2"});

			execute("negcontent2.logol");


			try {
				checkResult(result,0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}

	 }

	 @Test
	 public void testNegContent3(){
			init();

			Vector<String[]> result = new Vector<String[]>();
			result.add(new String[] {"LogolVAR_1","3","5"});

			execute("negcontent3.logol");


			try {
				checkResult(result,0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}

	 }

	 @Test
	 public void testModelnegContent(){
			init();

			Vector<String[]> result = new Vector<String[]>();
			result.add(new String[] {"LogolVAR_1","4","7"});
			result.add(new String[] {"LogolVAR_2", "8","10"});

			executeModel("negcontent.lgd");


			try {
				checkResult(result,0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}

	 }

	 @Test
	 public void testNotRepeat(){
			init();

			Vector<String[]> result = new Vector<String[]>();
			result.add(new String[] {"LogolVAR_1","4","7"});
			result.add(new String[] {"LogolVAR_2","8","15"});

			execute("notrepeat.logol");


			try {
				checkResult(result,0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}

	 }

	 @Test
	 public void testNotView(){
			init();

			Vector<String[]> result = new Vector<String[]>();
			result.add(new String[] {"LogolVAR_1","4","7"});
			result.add(new String[] {"LogolVAR_2","8","10"});

			execute("notview.logol");


			try {
				checkResult(result,0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}

	 }

     @Test
     public void testViewBegin(){
            init();

            Vector<String[]> result = new Vector<String[]>();
            result.add(new String[] {"LogolVAR_1","11","14"});

            execute("view_begin.logol");


            try {
                checkResult(result,0);
            } catch (ParserConfigurationException e) {
                fail(e.getMessage());
            } catch (SAXException e) {
                fail(e.getMessage());
            } catch (IOException e) {
                fail(e.getMessage());
            } catch (TransformerException e) {
                fail(e.getMessage());
            }

     }


		@Test
		public void testConversion() {

			init();

			String[] args = new String[] { "-conf",installDir+System.getProperty(FILESEPARATORPROPERTY)+"test"+System.getProperty(FILESEPARATORPROPERTY)+"logol.properties" , "-g",installDir+System.getProperty(FILESEPARATORPROPERTY)+"test"+System.getProperty(FILESEPARATORPROPERTY)+"variable.logol" , "-s",installDir+System.getProperty(FILESEPARATORPROPERTY)+"test"+System.getProperty(FILESEPARATORPROPERTY)+sequenceFile, "-max","10","-fasta","-gff"};
			try {
				Logol.main(args);
			} catch (ConfigurationException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (InterruptedException e) {
				fail(e.getMessage());
			} catch (ParseException e) {
				fail(e.getMessage());
			} catch (GrammarException e) {
				fail(e.getMessage());
			}
			String resFilePath = Logol.getOutputSeqName();

			FastaConverter fc = new FastaConverter();
			fc.convert2Fasta(resFilePath, resFilePath+".fasta");
			File resFile = new File(resFilePath+".fasta");
			logger.info("Checking file "+resFilePath);
			assertTrue(resFile.exists());
			GFFConverter gc = new GFFConverter();
			gc.convert2GFF(resFilePath, resFilePath+".gff");
			resFile = new File(resFilePath+".gff");
			logger.info("Checking file "+resFilePath);
			assertTrue(resFile.exists());

		}

		@Test
		public void testExternal() {

			assumeTrue(! OS.startsWith("win"));

			init();

			Vector<String[]> result = new Vector<String[]>();
			result.add(new String[] {"LogolVAR_1","4","6"});
			result.add(new String[] {"LogolVAR_2","7","10"});
			result.add(new String[] {"LogolVAR_3","11","12"});

			execute("external.logol");

			try {
				checkResult(result,0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}

		}

		@Test
		public void testExternalWithSpacer() {

			assumeTrue(! OS.startsWith("win"));

			init();

			Vector<String[]> result = new Vector<String[]>();
			result.add(new String[] {"LogolVAR_1","4","6"});
			result.add(new String[] {"LogolVAR_2","7","10"});
			result.add(new String[] {"LogolVAR_3","11","12"});
			result.add(new String[] {"LogolVAR_4","13","15"});

			execute("external2.logol");

			try {
				checkResult(result,0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}

		}

		@Test
		public void testMySequence() {
			BufferedReader br;
			try {
				br = new BufferedReader(new FileReader("test/"+sequenceFile));
				RichSequenceIterator iter = (RichSequenceIterator) org.irisa.genouest.logol.utils.MySequenceIterator.readFasta(br);
			    if(iter.hasNext()) {
			    	Sequence seq =  iter.nextSequence();

			    	seq = new MySequence(seq.getName(),seq.subStr(0, 10));

			    	assertTrue(seq.seqString().startsWith("ccccaa"));
			    	assertTrue(seq.getName()!=null);
			    	assertTrue(seq.getName().contains("nc_000018"));
			    	File outFile = new File("test/tmp/out.fasta");
		      	  	BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(outFile));
		      	  	MyRevCompSequence rev = new MyRevCompSequence(seq);
		      	  	System.out.println(rev.seqString());
		      	  	assertTrue(rev.seqString().startsWith("cgtttt"));

			    }
			    else {
			    	fail("no sequence found");
			    }

			} catch (FileNotFoundException e) {
				fail(e.getMessage());
			} catch (NoSuchElementException e) {
				fail(e.getMessage());
			} catch (BioException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			}

		}


		@Test
		public void testAlphabet() {

			init();

			Vector<String[]> result = new Vector<String[]>();
			result.add(new String[] {"LogolVAR_1","0","2"});
			result.add(new String[] {"LogolVAR_2","3","7"});

			execute("alphabet.logol");

			try {
				checkResult(result,0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}
		}

		@Test
		public void testOptimal() {

			init();

			Vector<String[]> result = new Vector<String[]>();
			result.add(new String[] {"LogolVAR_1","4","7"});
			result.add(new String[] {"LogolVAR_2","8","15"});

			execute("optimal.logol");

			try {
				checkResult(result,0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}
		}

		@Test
		public void testModelOptimal() {

			init();

			Vector<String[]> result = new Vector<String[]>();
			result.add(new String[] {"LogolVAR_1","4","7"});
			result.add(new String[] {"LogolVAR_2","8","15"});

			executeModel("optimal.lgd");

			try {
				checkResult(result,0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}
		}


		@Test
		public void test1799() {

			init();

			Vector<String[]> result = new Vector<String[]>();
			result.add(new String[] {"LogolVAR_1","0","1"});
			result.add(new String[] {"LogolVAR_2","2","1"});

			execute("1799.fasta","1799.logol");

			try {
				checkResult(result,0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}
		}

		@Test
		public void test1800() {

			init();

			Vector<String[]> result = new Vector<String[]>();
			result.add(new String[] {"LogolVAR_1","1","3"});
			result.add(new String[] {"LogolVAR_2","4","5"});
			result.add(new String[] {"LogolVAR_3","6","7"});

			execute("1800.fasta","1800.logol");

			try {
				checkResult(result,0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}
		}


		@Test
		public void testMetaControl() {

			init();

			Vector<String[]> result = new Vector<String[]>();
			result.add(new String[] {"LogolVAR_1","0","2"});
			result.add(new String[] {"LogolVAR_2","3","4"});
			result.add(new String[] {"LogolVAR_3","5","8"});

			execute("test.fasta","metacontrol.logol");

			try {
				checkResult(result,0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}
		}

		@Test
		public void test1806() {

			init();

			Vector<String[]> result = new Vector<String[]>();
			result.add(new String[] {"LogolVAR_1","7","14"});

			execute("test.fasta","1806.logol");

			try {
				checkResult(result,0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}
		}

		@Test
		public void testProtein() {

			init();

			Vector<String[]> result = new Vector<String[]>();
			result.add(new String[] {"LogolVAR_1","5","14"});

			executeProtein("protein.fasta","protein.logol");

			try {
				checkResult(result,0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}
		}

		@Test
		public void test2234() {

			init();

			Vector<String[]> result = null;

			execute("2244.fasta","2244.logol");

			try {
				checkNoMatch(result,0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}
		}

		@Test
		public void test2235() {

			init();

			Vector<String[]> result = new Vector<String[]>();
			result.add(new String[] {"LogolVAR_1","5","16"});

			execute("2235.fasta","2235.logol");

			try {
				checkResult(result,0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}
		}

		@Test
		public void test2242() {

			init();

			Vector<String[]> result = new Vector<String[]>();
			result.add(new String[] {"LogolVAR_1","9","14"});
			result.add(new String[] {"LogolVAR_2","15","15"});
			result.add(new String[] {"LogolVAR_3","16","16"});
			execute("2242.fasta","2242.logol");

			try {
				checkResult(result,0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}
		}

		@Test
		public void test2243() {

			init();

			Vector<String[]> result = new Vector<String[]>();
			result.add(new String[] {"LogolVAR_1","3","6"});
			execute("2243.fasta","2243.logol");

			try {
				checkResult(result,0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}
		}

		@Test
		public void test2244() {

			init();

			Vector<String[]> result = new Vector<String[]>();
			result.add(new String[] {"LogolVAR_1","7","8"});
			execute("test.fasta","fix2244.logol");

			try {
				checkResult(result,0);
			} catch (ParserConfigurationException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (TransformerException e) {
				fail(e.getMessage());
			}
		}

        @Test
        public void testMorphismVar() {

            init();

            Vector<String[]> result = new Vector<String[]>();
            result.add(new String[] {"LogolVAR_1","5","5"});
            execute("test.fasta","morphism_var.logol");

            try {
                checkResult(result,0);
            } catch (ParserConfigurationException e) {
                fail(e.getMessage());
            } catch (SAXException e) {
                fail(e.getMessage());
            } catch (IOException e) {
                fail(e.getMessage());
            } catch (TransformerException e) {
                fail(e.getMessage());
            }
        }

        @Test
        public void testComplexString() {

            init();

            Vector<String[]> result = new Vector<String[]>();
            result.add(new String[] {"LogolVAR_1","6","9"});
            execute("test.fasta","complex.logol");

            try {
                checkResult(result,0);
            } catch (ParserConfigurationException e) {
                fail(e.getMessage());
            } catch (SAXException e) {
                fail(e.getMessage());
            } catch (IOException e) {
                fail(e.getMessage());
            } catch (TransformerException e) {
                fail(e.getMessage());
            }
        }

   	 @Test
   	 public void testRubyCassiopee() {

		// Skip test in Travis
		try {
		 	String is_travis = System.getenv("TRAVIS");
		 	assumeTrue(is_travis==null);
		}
		catch(Exception e) {
			assumeTrue(1==0);
			return;
		}

   		 init();

 		Vector<String[]> result = new Vector<String[]>();
 		result.add(new String[] {"LogolVAR_1","4","6"});
 		result.add(new String[] {"LogolVAR_2","7","10"});
 		result.add(new String[] {"LogolVAR_3","11","14"});


   			String[] args = new String[] { "-conf",installDir+System.getProperty(FILESEPARATORPROPERTY)+"test"+System.getProperty(FILESEPARATORPROPERTY)+"ruby-cassiopee.properties" , "-g",installDir+System.getProperty(FILESEPARATORPROPERTY)+"test"+System.getProperty(FILESEPARATORPROPERTY)+"variable.logol" , "-s",installDir+System.getProperty(FILESEPARATORPROPERTY)+"test"+System.getProperty(FILESEPARATORPROPERTY)+"test.fasta", "-max"," 10", "-all"};
   			try {
   				Logol.main(args);
   			} catch (ConfigurationException e) {
   				fail(e.getMessage());
   			} catch (IOException e) {
   				fail(e.getMessage());
   			}  catch (InterruptedException e) {
   				fail(e.getMessage());
   			} catch (ParseException e) {
   				fail(e.getMessage());
   			} catch (GrammarException e) {
   				fail(e.getMessage());
   			}


   			try {
   				checkResult(result,0);
   			} catch (ParserConfigurationException e) {
   				fail(e.getMessage());
   			} catch (SAXException e) {
   				fail(e.getMessage());
   			} catch (IOException e) {
   				fail(e.getMessage());
   			} catch (TransformerException e) {
   				fail(e.getMessage());
   			}

   	 }

        /*
        @Test
        public void testindel() {

            init();

            Vector<String[]> result = new Vector<String[]>();
            result.add(new String[] {"LogolVAR_1","5","5"});
            execute("indel.fasta","indel.logol");

            try {
                checkResult(result,0);
            } catch (ParserConfigurationException e) {
                fail(e.getMessage());
            } catch (SAXException e) {
                fail(e.getMessage());
            } catch (IOException e) {
                fail(e.getMessage());
            } catch (TransformerException e) {
                fail(e.getMessage());
            }
       } */

}
