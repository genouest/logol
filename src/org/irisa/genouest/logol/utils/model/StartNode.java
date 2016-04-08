package org.irisa.genouest.logol.utils.model;
import org.w3c.dom.Node;

/**
 * Start node e.g. a model definition
 * @author osallou
 *
 */
public class StartNode extends AbstractNode implements LogolInterfaceNode {

	
	
	public StartNode() {
		
	}
	
	public String getNodeGrammar(Node startNode) throws ModelDefinitionException {
		String out="";
		
		
		
		// This is a variable
		String modelName = startNode.getAttributes().getNamedItem(Commons.name).getNodeValue();
		String params = startNode.getAttributes().getNamedItem(Commons.params).getNodeValue();
		String[] paramList = params.split(",");
		
		out = manageModelData(modelName,paramList,false)+"==>";
		

		
		return out;
	}

	public String getGlobalInfo(Node startNode) {
		return null;
	}

	public void update(Node item, Node matchNode) {
		// TODO Auto-generated method stub
		
	}
}
