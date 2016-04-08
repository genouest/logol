package org.irisa.genouest.logol.utils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.NoSuchElementException;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.Sequence;
import org.biojavax.bio.BioEntry;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;
import org.irisa.genouest.logol.Constants;
import org.irisa.genouest.logol.Treatment;


/**
 * Simple Sequence implementation to fix memory issues with BioJava DNATools functions.
 * Implements minimum number of functions for sequence data manipulation (no symbols and features)
 * Fix : 1660
 * @author osallou
 *
 */
public class MySequenceIterator implements RichSequenceIterator {

	BufferedReader fastaReader = null;
	
	long sequencePosition = 0;
	
	String nextSeqHeader = null;
	
	boolean fileOver = false;
	
	
	public MySequenceIterator() {
		
	}
	
	/**
	 * Accessor to a Fasta reader
	 * @param br BufferedReader to a Fasta file
	 * @return An iterator on Fasta sequences included in file
	 */
	public static MySequenceIterator readFasta(BufferedReader br) {		
		MySequenceIterator my = new MySequenceIterator();
		my.fastaReader = br;
		return my;		
	}
	
	
	
 	

	public RichSequence nextRichSequence() throws NoSuchElementException,
			BioException {
		// TODO Auto-generated method stub
		return null;
	}


	public boolean hasNext() {
		String line = null;
		try {
			if(nextSeqHeader==null && !fileOver) {
				// First read
			line = fastaReader.readLine();
			if(line.startsWith(">")) {
				nextSeqHeader = line;
				return true;
			}
			else {
				return false;
			}
			}
			else if(!fileOver){
				// We have a pending header
				return true;
			}
			else {
				// file is over
				return false;
			}
		} catch (IOException e) {
			System.err.println(e.getMessage());
			return false;
		}
	}
	

	public BioEntry nextBioEntry() throws NoSuchElementException, BioException {
		// TODO Auto-generated method stub
		return null;
	}

	
	public Sequence nextSequence() throws NoSuchElementException, BioException {
		String line="";
		StringBuffer sequenceContent = new StringBuffer("");
		MySequence sequence = new MySequence();
		sequence.setHeader(nextSeqHeader);
		boolean readFasta = true;
		try {
			while(readFasta && (line = fastaReader.readLine())!=null) {
				if(line.startsWith(">")) {
					readFasta = false;
					nextSeqHeader = line;
				}
				else {
					// For proteins set uppercase
					if(Treatment.dataType==Constants.PROTEIN) {
						sequenceContent.append(line.toUpperCase());
					}
					else {
						sequenceContent.append(line.toLowerCase());
					}
				}
			}
			if(line==null)   {
				fileOver = true;
			}
			sequence.setContent(sequenceContent.toString());
			
		} catch (IOException e) {
			System.err.println(e.getMessage());
			fileOver = true;
		}
		return sequence;
	}

	
}
