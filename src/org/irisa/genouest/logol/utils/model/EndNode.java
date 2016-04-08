package org.irisa.genouest.logol.utils.model;
import org.w3c.dom.Node;

/**
 * Termination Node
 * @author osallou
 *
 */
public class EndNode extends AbstractNode implements LogolInterfaceNode{

	public String getGlobalInfo(Node startNode) {
		return null;
	}

	public String getNodeGrammar(Node startNode) {
		return "";
	}

	public void update(Node item, Node matchNode) {
		// TODO Auto-generated method stub
		
	}

}
