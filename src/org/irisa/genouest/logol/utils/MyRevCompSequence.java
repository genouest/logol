package org.irisa.genouest.logol.utils;
import org.apache.log4j.Logger;
import org.biojava.bio.seq.Sequence;

/**
 * Simple Sequence implementation to fix memory issues with BioJava DNATools functions.
 * Implements minimum number of functions for sequence data manipulation (no symbols and features)
 * Fix : 1660
 * @author osallou
 *
 */
public class MyRevCompSequence extends MySequence {
	
	private static final Logger logger = Logger.getLogger(org.irisa.genouest.logol.utils.MyRevCompSequence.class);

	static final Character achar = new Character('a');
	static final Character cchar = new Character('c');
	static final Character gchar = new Character('g');
	static final Character tchar = new Character('t');
	static final Character uchar = new Character('u');
	
	boolean isdna = true; // vs rna
	
	boolean foundOtherCharacter=false;
	
	
	public MyRevCompSequence(Sequence seq) {
		StringBuffer data  = new StringBuffer(seq.length());
		setHeader(seq.getName());
		int count=seq.length()-1;
		String seqcontent = seq.seqString();
		if(seqcontent.contains("t"))  {
			isdna = true;
		}
		else {
			isdna = false;
		}
		for(int i=seq.length()-1;i>=0;i--) {
			try {
				data.append(compl(seqcontent.charAt(i)));

			} catch (Exception e) {
				System.err.println(e.getMessage());
				data = null;
				break;
			}
			count--;
		}
		
		if(foundOtherCharacter) {
			logger.warn("During conversion found other characters than [acgt]");
		}

		setContent(data.toString());
	}

	private char compl(char charAt) throws Exception {

		if(charAt==achar) {
			return tchar;
		}
		if(charAt==cchar) {
			return gchar;
		}
		if(charAt==gchar) {
			return cchar;
		}
		if(charAt==tchar) {
			return achar;
		}
		if(charAt==uchar) {
			return achar;
		}
		foundOtherCharacter=true;
		return charAt;
	}
}
