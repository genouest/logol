package org.irisa.genouest.logol.utils.model;
import org.w3c.dom.Node;

/**
 * Rule node. Start point to define a rule in logol e.g. list of models
 * @author osallou
 *
 */
public class RuleNode extends AbstractNode implements LogolInterfaceNode {

	public String getGlobalInfo(Node startNode) throws ModelDefinitionException {
		return "";
	}

	public String getNodeGrammar(Node startNode)
			throws ModelDefinitionException {
		return "";
	}

	public void update(Node item, Node matchNode) {
	}

}
