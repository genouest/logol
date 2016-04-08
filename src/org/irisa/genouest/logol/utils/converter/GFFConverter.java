package org.irisa.genouest.logol.utils.converter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.xpath.XPathAPI;
import org.biojava.bio.BioException;
import org.biojava.bio.program.gff.GFFEntrySet;
import org.biojava.bio.program.gff.GFFRecord;
import org.biojava.bio.program.gff.SimpleGFFRecord;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.StrandedFeature;
import org.biojavax.bio.seq.RichSequenceIterator;
import org.biojavax.bio.seq.RichSequence.IOTools;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * History:
 * Fix 1659 O. Sallou 21/07/10: GFF output should start at position 1, add 1 to positions
 * @author osallou
 *
 */
public class GFFConverter {

	private final static String TAB = "\t";
	
	/**
	 * GFF converter from Logol result file
	 * @param args input and output file
	 */
	public static void main(String[] args) {
		if(args.length==2) {
			GFFConverter gff = new GFFConverter();
			gff.convert2GFF(args[0], args[1]);
			}
			else {
				System.out.println("Usage: Converter inputXMLFile outputFastaFile");
			}
	}
	
	public GFFConverter() {
		
	}
	
	public void writeGFF(String outputFile, GFFEntrySet gffSet) throws IOException {
		File outFile = new File(outputFile);
		PrintWriter wr = new PrintWriter(outFile);
		
		wr.println("##gff-version 3");
		
		Iterator it = gffSet.lineIterator();
		while(it.hasNext()) {
			GFFRecord record = (GFFRecord) it.next();
			String strand="+";
			if(record.getStrand()==StrandedFeature.NEGATIVE) strand="-";
			wr.println(record.getSeqName()+TAB+record.getSource()+TAB+record.getFeature()+TAB+record.getStart()+TAB+record.getEnd()+TAB+record.getScore()+TAB+strand+TAB+record.getFrame()+TAB+printParams(record.getGroupAttributes()));
		}
		
		wr.close();		
	}

	private String printParams(Map groupAttributes) {
		String params="";
		Set keys = groupAttributes.keySet();
		Iterator it = keys.iterator();
		while(it.hasNext()) {
			String key = (String)it.next();
			Vector attValues = (Vector) groupAttributes.get(key);
			if(!params.equals("")) params+=";";
			params+=key+"=";
			for(int i=0;i<attValues.size();i++) {
				if(i>0) params+=",";
				params+=encode((String)attValues.get(i));
			}
			
		}
		return params;
	}

	private  String encode(String param) {
		//TODO replace special chars with ascii value
		String res = param.replaceAll(";", "%28");
		res = res.replaceAll(",", "%30");
		res = res.replaceAll(" ", "%20");
		return res;
	}
	
	
	/**
	 * Read fasta file from results, load seq name from file
	 * Size will be min/max of matches
	 * @param inputFile File of matches for a sequence
	 * @param outputFile File to write the GFF
	 * @throws NoSuchElementException
	 * @throws BioException
	 */
	public void convert2GFF(String inputFile, String outputFile) throws NoSuchElementException {
		
		DocumentBuilder builder;
		Node matchNode=null;
		
		try {
			  
		      int startSeq = 0;
		      int endSeq = 0;
			
		      SimpleGFFRecord gffRecord=null;
		      SimpleGFFRecord gffSubRecord=null;
		      GFFEntrySet gffSet=new GFFEntrySet();

			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

			InputStream in = new FileInputStream(inputFile);
			
			Document document = builder.parse(in);

			String expressionData = "/sequences/match";
			String expressionHeader="/sequences/fastaHeader";
			matchNode = XPathAPI.selectSingleNode(document,expressionHeader);
			String header = matchNode.getTextContent();
			// Get seq name from header, create dummy sequence
			header = ">"+header+"\nacgtacgt\n";
		    BufferedReader br = new BufferedReader(new StringReader(header));
		    RichSequenceIterator iter =  null;		      
		    iter= (RichSequenceIterator)IOTools.readFastaDNA(br,null);
		      
		    Sequence seq = iter.nextSequence();
		    String seqName = seq.getName();
			
			
			NodeList matchNodeList = XPathAPI.selectNodeList(document,expressionData);
			for(int j=0;j<matchNodeList.getLength();j++) {
		    NodeList childNodes = matchNodeList.item(j).getChildNodes();
			
			String sequenceData ="";
			String begin="";
			String end="";
			String errors="";
			String distance="";
			String id="";
			
			for(int i=0;i<childNodes.getLength();i++) {				
				Node curNode = childNodes.item(i);
				if(curNode.getNodeName().equals("id")) {
					id=curNode.getTextContent();
					break;
				}
			}
			
			
			Vector<String[]> vars = new Vector<String[]>();
			
			String[] data = new String[4];
			gffRecord = new SimpleGFFRecord();
			
			
			String mainMatchedModel="";
			
			for(int i=0;i<childNodes.getLength();i++) {
				Node curNode = childNodes.item(i);
				if(curNode.getNodeName().equals("begin")) {
					begin=curNode.getTextContent();
					
				}
				if(curNode.getNodeName().equals("end")) {
					end=curNode.getTextContent();
				}
				if(curNode.getNodeName().equals("errors")) {
					errors=curNode.getTextContent();
				}
				if(curNode.getNodeName().equals("distance")) {
					distance=curNode.getTextContent();
				}				
				if(curNode.getNodeName().equals("variable")) {
					
					gffSubRecord = new SimpleGFFRecord();
					
					NodeList lv_childNodes = curNode.getChildNodes();
					data = new String[5];
					for(int k=0;k<lv_childNodes.getLength();k++) {												
						
						Node lv_curNode = lv_childNodes.item(k);						
						if(lv_curNode.getNodeName().equals("begin")) {							
							data[1]=lv_curNode.getTextContent();
						}
						if(lv_curNode.getNodeName().equals("text")) {							
							data[0]= lv_curNode.getTextContent();							
							if(!mainMatchedModel.equals("")) mainMatchedModel+=",";
							mainMatchedModel+=data[0];
						}
						if(lv_curNode.getNodeName().equals("end")) {
							data[2]=lv_curNode.getTextContent();
						}
						if(lv_curNode.getNodeName().equals("errors")) {
							data[3]=lv_curNode.getTextContent();
						}
						if(lv_curNode.getNodeName().equals("distance")) {
							data[4]=lv_curNode.getTextContent();
						}						
						
					}
					gffSubRecord.setSeqName(seqName);
					gffSubRecord.setEnd(Integer.valueOf(data[2]+1));
					gffSubRecord.setStart(Integer.valueOf(data[1]+1));
					if(Integer.valueOf(data[1])<Integer.valueOf(data[2])) {
						gffSubRecord.setStrand(StrandedFeature.POSITIVE);
					}
					else {
						gffSubRecord.setStrand(StrandedFeature.NEGATIVE);
					}
					gffSubRecord.setFeature("match_part");
					gffSubRecord.setSource("LogolMatch");
					Map<String,Vector<String>> map = gffSubRecord.getGroupAttributes();
					Vector<String> v = addParam("Submodel match");
					map.put("Note", v );
					v = addParam(data[0]);
					map.put("SubModel", v);
					v = addParam(data[3]);
					map.put("Errors",v);
					v = addParam(data[4]);
					map.put("Distance",v);					
					v = addParam(id);
					map.put("Parent", v);
					gffSubRecord.setGroupAttributes(map);
					//vars.add(data);
					gffSet.add(gffSubRecord);
				}
			
			}
			
			String direction="+";
			
			if(Integer.valueOf(begin)>Integer.valueOf(end)) {
				String tmpData = end;
				end = begin;
				begin = tmpData;
				direction="-";
			}
			
			if(Integer.valueOf(begin)<Integer.valueOf(end)) {
				gffRecord.setStrand(StrandedFeature.POSITIVE);
			}
			else {
				gffRecord.setStrand(StrandedFeature.NEGATIVE);
			}
			
			if(Integer.valueOf(begin)<startSeq || startSeq==0) {
				startSeq = Integer.valueOf(begin);
			}
			if(Integer.valueOf(end)>endSeq) {
				endSeq = Integer.valueOf(end);
			}
			
			gffRecord.setSeqName(seqName);
			gffRecord.setEnd(Integer.valueOf(end)+1);
			gffRecord.setStart(Integer.valueOf(begin)+1);
			gffRecord.setFeature("match");
			gffRecord.setSource("LogolMatch");
			Map map = gffRecord.getGroupAttributes();
			Vector<String> v = addParam("Model match");
			map.put("Note", v);
			v = addParam(mainMatchedModel);
			map.put("Model", v);
			v = addParam(errors);
			map.put("Errors", v);
			v = addParam(distance);
			map.put("Distance", v);			
			v = addParam(id);
			map.put("ID", v);
			
			gffSet.add(gffRecord);					
			}
			
			in.close();			
			
			gffRecord = new SimpleGFFRecord();
			gffRecord.setSeqName(seqName);
			gffRecord.setEnd(endSeq+1);
			gffRecord.setStart(startSeq+1);
			gffRecord.setFeature("region");
			gffRecord.setSource("LogolMatch");
			Map map = gffRecord.getGroupAttributes();
			Vector<String> v = addParam(seqName);
			map.put("Sequence", v);
			gffSet.add(gffRecord);
			
			writeGFF(outputFile, gffSet);

			
		} catch (ParserConfigurationException e) {
			System.out.println("ERROR: "+e.getMessage());
		} catch (SAXException e) {
			System.out.println("ERROR: "+e.getMessage());
		} catch (IOException e) {
			System.out.println("ERROR: "+e.getMessage());
		} catch (TransformerException e) {
			System.out.println("ERROR: "+e.getMessage());
		} catch (BioException e) {
			System.out.println("ERROR: "+e.getMessage());
		}
		
		
	}	
	
	/**
	 * Creates a vector from a string
	 * @param string2
	 * @return New vector with the string
	 */
	private Vector<String> addParam(String string2) {
		Vector<String> v = new Vector<String>();
		v.add(string2);
		return v;
		
	}

}
