package org.irisa.genouest.logol.utils.model;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Variable or fixed content node 
 * @author osallou
 * History: 20/04/09 Fix 1321 Fixed content in content constraint fails
 *
 */
public class VariableNode extends AbstractNode implements LogolInterfaceNode {

	public String getGlobalInfo(Node startNode) {
		
		return null;
	}

	public String getNodeGrammar(Node startNode) throws ModelDefinitionException {
		String out="";
		boolean isStringConstrained=false;
		boolean isStructConstrained=false;
		
		NamedNodeMap attrs = startNode.getAttributes();
			
		String nodeId = attrs.getNamedItem("id").getNodeValue();
		
		String not_data = null;
		if(attrs.getNamedItem(Commons.not_data)!=null) not_data=attrs.getNamedItem(Commons.not_data).getNodeValue();
		String parentName = manageParentData(attrs.getNamedItem(Commons.parent).getNodeValue());
		
		String begin = manageBeginData(attrs.getNamedItem(Commons.stg_begin).getNodeValue());		
		if(begin!=null)  {
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
		

		String content = manageContentData(attrs.getNamedItem(Commons.stg_content).getNodeValue());
		if(content!=null) {
			isStringConstrained = true;	
		}
		
		String save = manageSaveData(attrs.getNamedItem(Commons.stg_save).getNodeValue());
		if(save!=null) {
			isStringConstrained = true;
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
		
		/*if(cost!=null && dist!=null) {
			throw new ModelDefinitionException("Error, both cost and distance are set");
		}*/
		
		String morphism = null;
		if(attrs.getNamedItem(Commons.morphism)!=null) {
			morphism = manageMorhpism(attrs.getNamedItem(Commons.morphism).getNodeValue());
		}
		
		/**
		 * If negative constraint is set, add negative sign before morphism
		 */
		if(not_data!=null && not_data.length()>0) {
			out += Commons.NEGATIVESIGN+" ";
		}
		
		if(morphism!=null) {
			out+=morphism+" ";
		}
		
		if(parentName==null) {
			if(content==null) {
				throw new ModelDefinitionException("Error, both data and content are not set - node id: "+nodeId);
			}
			else {
				if(content.startsWith(Commons.CONTENTSIGN+"\"")) {
					// If this is a fixed content value, remove content sign because
					// fixed values are not expected with a content constraint as first part
					content = content.substring(1);
				}
				parentName = content;
				content = null;
				out += parentName;
			}
		}
		else {
			out += parentName;
		}
		if(isStringConstrained) {
			String constraints = addConstraints(new String[] {begin,end,size,content,save});
			if(constraints.length()>0) {
				out+=":{";
				out+= constraints;
				out+="}";
			}
		}
		
		if(isStructConstrained) {
			if(not_data!=null && not_data.length()>0) {
			throw new ModelDefinitionException("Error, cost and distance constraints are not allowed with negative content constraints - nodeid: "+nodeId);
			}
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

	public void update(Node item, Node matchNode) {
		addAttributes(item,matchNode);	
	}

}
