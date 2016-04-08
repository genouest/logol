package org.irisa.genouest.logol;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * History: 26/03/09 Fix 1271 Add error email
 * @author osallou
 *
 */
public class GrammarException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4618111458451653730L;
	
	private static String errFile = null;

	public static void setErrFile(String path) {
			errFile = path;
	}
	
	public GrammarException() {
	}

	public GrammarException(String arg0) {
		super(arg0);
		// Try to write to file. If not possible, never mind, standard treatment still available
		if(errFile!=null) {
		try {
			FileChannel destChannel = new FileOutputStream(errFile,true).getChannel();
			destChannel.lock();
			String err = arg0;
			byte[] msg = err.getBytes();
			ByteBuffer buff = ByteBuffer.allocate(msg.length);
			buff.put(msg);
			buff.flip();
			destChannel.write(buff);
			
			destChannel.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
		}
		
	}

	public GrammarException(Throwable arg0) {
		super(arg0);
	}

	public GrammarException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
