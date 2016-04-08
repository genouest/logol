package org.irisa.genouest.logol.utils.model;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Repeat node
 * @author osallou
 *
 */
public class RepeatNode extends AbstractNode implements LogolInterfaceNode {

	public String getGlobalInfo(Node startNode) throws ModelDefinitionException {
		String out="+";
		NamedNodeMap attrs = startNode.getAttributes();
		String repeats = manageRepeatData(attrs.getNamedItem(Commons.nbrepeat).getNodeValue());
		if(repeats!=null) {
			out += repeats;
		}
		
		return out;
	}

	public String getNodeGrammar(Node startNode)
			throws ModelDefinitionException {
		// manage overlap and spacer
		String out="";
		NamedNodeMap attrs = startNode.getAttributes();

		
		if(doOverlap(startNode)) {
			out+=Commons.OVERLAP;
		}
		else {
			out+=Commons.AND;
		}
		
		
		String spacers = manageSpacerData(attrs.getNamedItem(Commons.spacer).getNodeValue());
		if(spacers!=null) { out+=spacers; }
		else {
			out+="[0,0]";
		}
		
		return out;
	}

	public void update(Node item, Node matchNode) {
		// TODO Auto-generated method stub
		
	}

}
