package org.irisa.genouest.logol.utils.model;
import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Spacer node
 * @author osallou
 *
 */
public class SpacerNode extends AbstractNode implements LogolInterfaceNode {

	public String getGlobalInfo(Node startNode) {
		return null;
	}

	public String getNodeGrammar(Node startNode) throws DOMException, ModelDefinitionException {
		String out="";
		boolean isStringConstrained=false;
		
		NamedNodeMap attrs = startNode.getAttributes();
		
		out+=Commons.ANYSIGN;
			
		String size = manageSizeData(attrs.getNamedItem(Commons.stg_size).getNodeValue(),false);
		if(size!=null) {
			isStringConstrained = true;
		}
		
		if(isStringConstrained) {
			String constraints = addConstraints(new String[] {size});
			if(constraints.length()>0) {
				out+=":{";
				out+= constraints;
				out+="}";
			}
		}
		
		return out;
	}

	public void update(Node item, Node matchNode) {
		addAttributes(item,matchNode);
		
	}

}
