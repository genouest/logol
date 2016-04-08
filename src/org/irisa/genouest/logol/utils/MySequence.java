package org.irisa.genouest.logol.utils;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.biojava.bio.Annotation;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.FeatureFilter;
import org.biojava.bio.seq.FeatureHolder;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.Feature.Template;
import org.biojava.bio.symbol.Alphabet;
import org.biojava.bio.symbol.AlphabetManager;
import org.biojava.bio.symbol.Edit;
import org.biojava.bio.symbol.FiniteAlphabet;
import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.Symbol;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.utils.ChangeListener;
import org.biojava.utils.ChangeType;
import org.biojava.utils.ChangeVetoException;

/**
 * Simple Sequence implementation to fix memory issues with BioJava DNATools functions.
 * Implements minimum number of functions for sequence data manipulation (no symbols and features)
 * Fix : 1660
 * @author osallou
 *
 */
public class MySequence implements Sequence {

	private static final Logger logger = Logger.getLogger(org.irisa.genouest.logol.utils.MySequence.class);
	
	private static final String SAMPLEHEADER =">Logol sequence";
	
	String content = null;
	
	long position = 0;
	
	String header=SAMPLEHEADER;
	
	public static void writeFasta(BufferedOutputStream bos, Sequence seq) {
		writeFasta(bos,seq,seq.length());
	}
	
	/**
	 * Writes sequence to an output stream with header and sequence data with a max line length
	 * @param bos Output stream
	 * @param seq {@link Sequence} to print
	 * @param length Maximum size of a line to force carriage return
	 */
	public static void writeFasta(BufferedOutputStream bos, Sequence seq, int length) {
		try {
			String name = seq.getName();
			if(!name.startsWith(">")) name = ">"+name;
			bos.write((name+"\n").getBytes());	
			if(length<seq.length()) {
				int beginIndex=0;
				while(beginIndex<seq.length()) {				
				bos.write((seq.seqString().substring(beginIndex, beginIndex+length)+"\n").getBytes());
				beginIndex+=length;
				}
			}
			else {
				bos.write((seq.seqString()+"\n").getBytes());
			}
			bos.flush();
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}
	
	public MySequence() {
		
	}
	
	public MySequence(String head,String data) {
		header = head;
		if(header==null) header = SAMPLEHEADER;
		if(!header.startsWith(">")) {
			header = ">"+header;
		}
		content = data;
	}
	
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
		

	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	
	
	public String getName() {
		return header;
	}


	public String getURN() {
		// TODO Auto-generated method stub
		return null;
	}


	public void edit(Edit arg0) throws IndexOutOfBoundsException,
			IllegalAlphabetException, ChangeVetoException {
		// TODO Auto-generated method stub

	}


	public Alphabet getAlphabet() {
		// TODO Auto-generated method stub

		FiniteAlphabet dna = DNATools.getDNA();

		return dna;
	}


	public Iterator iterator() {
		// TODO Auto-generated method stub
		return null;
	}


	public int length() {
		return content.length();
	}


	public String seqString() {
		return content;
	}


	public SymbolList subList(int arg0, int arg1)
			throws IndexOutOfBoundsException {
		// TODO Auto-generated method stub
		return null;

	}


	public String subStr(int arg0, int arg1) throws IndexOutOfBoundsException {
		// Minus 1 to keep some behavior as biojava , start position is 1
		if(arg0>0) arg0--;
		return content.substring(arg0, arg1);
	}


	public Symbol symbolAt(int arg0) throws IndexOutOfBoundsException {
		// TODO Auto-generated method stub		
		return null;
	}


	public List toList() {
		// TODO Auto-generated method stub
		return null;
	}


	public void addChangeListener(ChangeListener arg0) {
		// TODO Auto-generated method stub

	}


	public void addChangeListener(ChangeListener arg0, ChangeType arg1) {
		// TODO Auto-generated method stub

	}


	public boolean isUnchanging(ChangeType arg0) {
		// TODO Auto-generated method stub
		return false;
	}


	public void removeChangeListener(ChangeListener arg0) {
		// TODO Auto-generated method stub

	}


	public void removeChangeListener(ChangeListener arg0, ChangeType arg1) {
		// TODO Auto-generated method stub

	}


	public boolean containsFeature(Feature arg0) {
		// TODO Auto-generated method stub
		return false;
	}


	public int countFeatures() {
		// TODO Auto-generated method stub
		return 0;
	}


	public Feature createFeature(Template arg0) throws BioException,
			ChangeVetoException {
		// TODO Auto-generated method stub
		return null;
	}


	public Iterator<Feature> features() {
		// TODO Auto-generated method stub
		return null;
	}


	public FeatureHolder filter(FeatureFilter arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public FeatureHolder filter(FeatureFilter arg0, boolean arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public FeatureFilter getSchema() {
		// TODO Auto-generated method stub
		return null;
	}


	public void removeFeature(Feature arg0) throws ChangeVetoException,
			BioException {
		// TODO Auto-generated method stub

	}


	public Annotation getAnnotation() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setPosition(long sequencePosition) {
		position = sequencePosition;
		
	}

}
