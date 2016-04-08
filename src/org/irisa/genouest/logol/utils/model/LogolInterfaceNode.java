package org.irisa.genouest.logol.utils.model;
import java.util.HashMap;

import org.w3c.dom.Node;

/**
 * Generic interface for all nodes of the designer
 * @author osallou
 *
 */
public interface LogolInterfaceNode {
	
	/**
	 * Check if overlap is set
	 * @param startNode
	 * @return true is overlap is checked
	 */
	public boolean doOverlap(Node startNode);
	
	/**
	 * Get the logol grammar for the node
	 * @param startNode Node to convert
	 * @return String logol representation
	 * @throws ModelDefinitionException
	 */
	public String getNodeGrammar(Node startNode) throws ModelDefinitionException;
	
	/**
	 * Gets information of a group of nodes, used for views or repeats for example.
	 * @param startNode current node to convert
	 * @return String logol representation
	 * @throws ModelDefinitionException
	 */
	public String getGlobalInfo(Node startNode)throws ModelDefinitionException;
	
	/**
	 * Add comments to logol generated data or not
	 * @param showComments True if comments should be set
	 */
	public void setShowComments(boolean showComments);
	

	/**
	 * Sets the list of predefined terminals
	 * @param terminals
	 */
	public void setTerminals(HashMap<String, String> terminals);

	public void update(Node item, Node matchNode);
	
}
