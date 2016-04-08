package org.irisa.genouest.logol.utils.model;
import org.w3c.dom.Node;

/**
 * Fork node, use to manage OR condition
 * @author osallou
 *
 */
public class ForkNode extends AbstractNode implements LogolInterfaceNode {

	public String getGlobalInfo(Node startNode) throws ModelDefinitionException {
		return "";
	}

	public String getNodeGrammar(Node startNode)
			throws ModelDefinitionException {
		return "";
	}

	public void update(Node item, Node matchNode) {
		// TODO Auto-generated method stub
		
	}

}
