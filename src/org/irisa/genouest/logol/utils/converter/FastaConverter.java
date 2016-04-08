package org.irisa.genouest.logol.utils.converter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.xpath.XPathAPI;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.SequenceIterator;
import org.biojava.utils.ChangeVetoException;
import org.biojavax.Namespace;
import org.biojavax.bio.BioEntry;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;
import org.biojavax.bio.seq.io.FastaFormat;
import org.biojavax.bio.seq.io.FastaHeader;
import org.biojavax.bio.seq.io.RichStreamWriter;
import org.irisa.genouest.logol.utils.LogolUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class FastaConverter {

	/**
	 * Fasta conversion from Logol result file
	 * @param args input and output file names
	 */
	public static void main(String[] args) {
		if(args.length==2) {
			FastaConverter fc = new FastaConverter();
			fc.convert2Fasta(args[0], args[1]);
			}
			else {
				System.out.println("Usage: Converter inputXMLFile outputFastaFile");
			}
	}
	
	public FastaConverter() {
		
	}

	/**
	 * Convert a Logol output XML file to fasta format
	 * @param inputFile Logol output
	 * @param outputFile New fasta result file
	 */
	public void convert2Fasta(String inputFile, String outputFile) {
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
	
	
	
    /**
     * Writes <CODE>Sequence</CODE>s from a <code>SequenceIterator</code> to an <code>OutputStream </code>in
     * Fasta Format.  This makes for a useful format filter where a
     * <code>StreamReader</code> can be sent to the <code>RichStreamWriter</code> after formatting.
     * @param os The stream to write fasta formatted data to
     * @param in The source of input <CODE>RichSequence</CODE>s
     * @param ns a <code>Namespace</code> to write the <CODE>RichSequence</CODE>s to. <CODE>Null</CODE> implies that it should
     * use the namespace specified in the individual sequence.
     * @param length Size of the sequence line.
     * @throws java.io.IOException if there is an IO problem
     */
    public synchronized void writeFasta(OutputStream os, SequenceIterator in, Namespace ns, FastaHeader header,int length)
    throws IOException {
        FastaFormat fastaFormat = new FastaFormat();
        fastaFormat.setLineWidth(length);
        if(header != null){
            fastaFormat.setHeader(header);
        }

        RichStreamWriter sw = new RichStreamWriter(os,fastaFormat);        
        sw.writeStream(in,ns);       
    }
    
    
    /**
     * Writes a single <code>Sequence</code> to an <code>OutputStream</code> in Fasta format.
     * @param os the <code>OutputStream</code>.
     * @param seq the <code>Sequence</code>.
     * @param ns a <code>Namespace</code> to write the sequences to. Null implies that it should
     *              use the namespace specified in the individual sequence.
     * @throws java.io.IOException if there is an IO problem
     */
    public synchronized void writeFasta(OutputStream os, Sequence seq, Namespace ns)
    throws IOException {
        writeFasta(os, new org.biojavax.bio.seq.RichSequence.IOTools.SingleRichSeqIterator(seq),ns, null,seq.length());
    }
	

}
