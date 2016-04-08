package org.irisa.genouest.logol.utils;

@Deprecated
public class Converter {


	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length==2) {
		LogolUtils.convert2Fasta(args[0], args[1]);
		}
		else {
			System.out.println("Usage: Converter inputXMLFile outputFastaFile");
		}

	}



}
