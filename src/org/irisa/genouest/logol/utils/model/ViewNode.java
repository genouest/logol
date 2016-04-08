package org.irisa.genouest.logol.utils.model;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Convert a view node to a logol view
 * @author osallou
*  History			 28/07/09 O. Sallou Fix1397 Add support for negative constraint on views 
 *
 */
public class ViewNode extends AbstractNode implements LogolInterfaceNode {

	
	//Fix1397
	/**
	 * Checks if view has a negative content constraint
	 * @param startNode node to analyse
	 * @return true if constraint is present
	 */
	public boolean isNegative(Node startNode) {
		NamedNodeMap attrs = startNode.getAttributes();
		
		String not_data = null;
		if(attrs.getNamedItem(Commons.not_data)!=null) not_data=attrs.getNamedItem(Commons.not_data).getNodeValue();
		
		if(not_data!=null && not_data.length()>0) {
			return true;
		}
		return false;
	}
	
	
	public String getGlobalInfo(Node startNode) throws ModelDefinitionException {
		String out="";
		
		boolean isStringConstrained=false;
		boolean isStructConstrained=false;
		
		NamedNodeMap attrs = startNode.getAttributes();	
		
		String nodeId = attrs.getNamedItem("id").getNodeValue();
		
		String begin = manageBeginData(attrs.getNamedItem(Commons.stg_begin).getNodeValue());
		if(begin!=null) {
			isStringConstrained = true;
			begin = isNegation(attrs.getNamedItem(Commons.not_begin).getNodeValue(),begin);
		}
		
		String end = manageEndData(attrs.getNamedItem(Commons.stg_end).getNodeValue());
		if(end!=null) {
			isStringConstrained = true;
			end = isNegation(attrs.getNamedItem(Commons.not_end).getNodeValue(),end);
		}
		
		
		
		boolean optimalSize = false;
		if(attrs.getNamedItem(Commons.stg_optimalsize)!=null && attrs.getNamedItem(Commons.stg_optimalsize).getNodeValue().length()!=0) {
			optimalSize =true;
		}
		String size = manageSizeData(attrs.getNamedItem(Commons.stg_size).getNodeValue(),optimalSize);
		if(size!=null) {
			isStringConstrained = true;
			size = isNegation(attrs.getNamedItem(Commons.not_size).getNodeValue(),size);
		}
		
		
		String save = null;
		if(attrs.getNamedItem(Commons.stg_save)!=null) {		
			save = manageSaveData(attrs.getNamedItem(Commons.stg_save).getNodeValue());
		if(save!=null) {
			isStringConstrained = true;
		}
		}
		
		String cost = manageCostData(attrs.getNamedItem(Commons.stc_cost).getNodeValue());
		if(cost!=null) {
			isStructConstrained = true;
			cost = isNegation(attrs.getNamedItem(Commons.not_cost).getNodeValue(),cost);
		}
		
		String dist = manageDistData(attrs.getNamedItem(Commons.stc_dist).getNodeValue());
		if(dist!=null) {
			isStructConstrained = true;
			dist = isNegation(attrs.getNamedItem(Commons.not_dist).getNodeValue(),dist);
		}
		
		String alphabet = null;
		if(attrs.getNamedItem(Commons.stc_alphabet)!=null && attrs.getNamedItem(Commons.stc_alphabetpercent)!=null) {
			alphabet = manageAlphabetData(attrs.getNamedItem(Commons.stc_alphabet).getNodeValue(),attrs.getNamedItem(Commons.stc_alphabetpercent).getNodeValue());
		if(alphabet!=null) {
			isStructConstrained = true;
		}
		}
		
		if(cost!=null && dist!=null) {
			throw new ModelDefinitionException("Error, both cost and distance are set- nodeid: "+nodeId);
		}
		

		if(isStringConstrained) {
			String constraints = addConstraints(new String[] {begin,end,size,save});
			if(constraints.length()>0) {
				out+=":{";
				out+= constraints;
				out+="}";
			}
		}
		
		if(isStructConstrained) {			
			String constraints = null;
			if(alphabet!=null) { constraints = addConstraints(new String[] {cost,dist,alphabet}); }
			else {
				constraints = addConstraints(new String[] {cost,dist});
			}
			if(constraints.length()>0) {
				out+=":{";
				out+= constraints;
				out+="}";
			}			
		}
		
		return out;
	}

	public String getNodeGrammar(Node startNode) {
		return "";
	}

	public void update(Node item, Node matchNode) {
		// TODO Auto-generated method stub
		
	}

}
