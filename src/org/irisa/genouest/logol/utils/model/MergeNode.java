package org.irisa.genouest.logol.utils.model;
import org.w3c.dom.Node;

/**
 * Merge node, used after a Fork node to merge the branches of the OR conditions.
 * @author osallou
 *
 */
public class MergeNode extends AbstractNode implements LogolInterfaceNode {

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
