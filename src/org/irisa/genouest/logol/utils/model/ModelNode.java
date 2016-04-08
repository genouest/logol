package org.irisa.genouest.logol.utils.model;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Model node
 * @author osallou
 *
 */
public class ModelNode extends AbstractNode implements LogolInterfaceNode {


	public String getGlobalInfo(Node startNode) {
		return null;
	}

	public String getNodeGrammar(Node startNode) throws DOMException, ModelDefinitionException {

		String out="";

		
		NamedNodeMap attrs = startNode.getAttributes();
		
		String modelName = attrs.getNamedItem(Commons.name).getNodeValue();
		String params = attrs.getNamedItem(Commons.params).getNodeValue();
		String[] paramList = params.split(",");
		if(showComments) {
			out += manageModelData(modelName,paramList);
		}
		else {
			out += manageModelData(modelName,paramList,false);	
		}
		
		return out;
	}

	public void update(Node item, Node matchNode) {
		addAttributes(item,matchNode);		
	}

}
