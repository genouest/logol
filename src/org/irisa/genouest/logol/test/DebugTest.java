package org.irisa.genouest.logol.test;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.irisa.genouest.logol.StructConstraint;
import org.irisa.genouest.logol.parser.logolLexer;
import org.irisa.genouest.logol.parser.logolParser;

public class DebugTest {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws RecognitionException 
	 */
	public static void main(String[] args) throws IOException, RecognitionException {
		String text="#[mod1.VAR1,mod1.VAR2]=6\n";
		InputStream is = new ByteArrayInputStream(text.getBytes("UTF-8"));
		ANTLRInputStream input = new ANTLRInputStream(is);
		logolLexer lexer = new logolLexer(input);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		logolParser parser = new logolParser(tokens);
		parser.control();
		//StructConstraint stc = parser.structConstraint();
		//System.out.println(stc.alphabetConstraint);
	}

}
