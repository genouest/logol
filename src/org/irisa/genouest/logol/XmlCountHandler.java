package org.irisa.genouest.logol;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * FIX 1768 use sax for xml parsing
 * @author osallou
 *
 */
public class XmlCountHandler extends DefaultHandler {

		  private boolean collectTag = false;
		  private int totalCount = 0;
		  
		  private String tag = "match";

		
		  public XmlCountHandler(String matchtag) {
			  tag = matchtag;
		  }
		  
		  public void startElement(String namespaceUri,
		                           String localName,
		                           String qualifiedName,
		                           Attributes attributes)
		      throws SAXException {
		    if (qualifiedName.equals(tag)) {
		      collectTag = true;
		      totalCount++;
		    } 
		  }



		public int getTotalCount() {
			return totalCount;
		}
		  

		 
		  

		 
	
	
}
