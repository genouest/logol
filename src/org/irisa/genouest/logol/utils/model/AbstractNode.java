package org.irisa.genouest.logol.utils.model;
import java.util.HashMap;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Base node for the designer. Contains all common methods to manage constraints etc..
 * @author osallou
 * History: 05/05/09 Fix 1335 add double quotes for morphism
 *          21/09/09 Fix 1415 missing percentage option for cost and distance
 *
 */
public abstract class AbstractNode implements LogolInterfaceNode {
	
	boolean showComments = true;
	
	public HashMap<String,String> terminals = null;
	
	
	public void addAttributes(Node modelNode,Node matchNode) {
		
		long begin=0;
		long end=0;
		int errors=0;
		int distance=0;
		String text="";
		
		
		NodeList matchChilds = matchNode.getChildNodes();
		int modelId=1;
		for(int i=0;i<matchChilds.getLength();i++) {
			Node child = matchChilds.item(i);
			if(child.getNodeName().equals("begin")) {
				begin = Long.parseLong(child.getTextContent());				
			}
			if(child.getNodeName().equals("end")) {
				end = Long.parseLong(child.getTextContent());						
			}
			if(child.getNodeName().equals("errors")) {
				errors = Integer.parseInt(child.getTextContent());				
			}
			if(child.getNodeName().equals("distance")) {
				distance = Integer.parseInt(child.getTextContent());				
			}			
			if(child.getNodeName().equals("text")) {
				text = child.getTextContent();				
			}
		}		
		
		
		Document modelDocument = modelNode.getOwnerDocument();
		
		Attr bnode = modelDocument.createAttribute("begin");
		bnode.setNodeValue(Long.toString(begin));
		Attr enode = modelDocument.createAttribute("end");
		enode.setNodeValue(Long.toString(end));
		Attr errnode = modelDocument.createAttribute("errors");
		errnode.setNodeValue(Integer.toString(errors));
		Attr distnode = modelDocument.createAttribute("distance");
		distnode.setNodeValue(Integer.toString(distance));		

		Element elt = (Element) modelNode;
		elt.setAttributeNode(bnode);
		elt.setAttributeNode(enode);
		elt.setAttributeNode(errnode);
		elt.setAttributeNode(distnode);		
		
		NodeList list = elt.getChildNodes();
		for(int i=0;i<list.getLength();i++){
			if(list.item(i).getNodeName().equals("mxCell")) {
				Element cell = (Element) list.item(i);
				cell.setAttribute("style", cell.getAttribute("style")+";strokeColor=green;fillColor=green");
				break;
			}
		}
		
	}
	
	
	public HashMap<String, String> getTerminals() {
		return terminals;
	}


	public void setTerminals(HashMap<String, String> terminals) {
		this.terminals = terminals;
	}


	public boolean doOverlap(Node node) {
		Node overlap = node.getAttributes().getNamedItem("overlap");
		if(overlap!=null) {
			if(overlap.getNodeValue().equals("on")) {
				return true;
			}
		}
		return false;
	}
	
	
	String manageMorhpism(String data) {		
		if(data==null || data.length()==0) { return null; }
		//Fix 1335
		String name = null;
		if(data.startsWith("+")||data.startsWith("-")) {
			name = "\""+data.substring(1)+"\"";
			return data.substring(0, 1)+name; 		
		}
		else {
			name = "\""+data+"\"";
			return "+"+name;
		}
	}
	
	
	String manageParentData(String data)  {
		
		if(data==null || data.length()==0)  { return null; }
		else {
			if(data.startsWith("\"")) {
				// This is a terminal
				return data.toLowerCase();
			}
			else {
				// This is a var name
				// Does variable already exist?
				return manageParentVariable(data);
			}
		}
		
	}
	
	String manageModelData(String data, String[] params) throws ModelDefinitionException {
		return manageModelData(data,params,true);
	}
	
	String manageModelData(String data, String[] params,boolean addComments) throws ModelDefinitionException  {
		String model="";
		if(data==null || data.length()==0)  { throw new ModelDefinitionException("Error model name is empty"); }
		else {
			
			// This is a variable
			if(Commons.modelMap.containsKey(data)) {
				model= Commons.modelMap.get(data);
				if(addComments && data.length()>0) {
					model+="%"+data+"%";
				}
			}
			else {
				String var = Commons.LOGOLMOD+Commons.getModelCounter();
				Commons.modelMap.put(data,var);
				model= var;
				if(addComments && data.length()>0) {
					model+="%"+data+"%";
				}
			}
			model+="(";
			if(params!=null) {
			for(int i=0;i<params.length;i++) {
				if(i>0) {
					model+=",";
				}
				model+=manageVariable(params[i],addComments);
			}
			}
			model+=")";
			
		}
		return model;
	}
	
	String manageSpacerData(String data) throws ModelDefinitionException {
		if(data.length()==0) {
			return null;
		}
		return manageInterval(data,Commons.spacerAccessors,Commons.LENGTHSIGN);
	}
	
	String manageRepeatData(String data) throws ModelDefinitionException {
		if(data.length()==0)  {
			return null;
		}
		return manageInterval(data,Commons.repeatAccessors,"");
	}
	
	String isNegation(String neg,String data) {
		if(data==null) {
			return null;
		}
		if(neg==null || neg.length()==0) {
			return data;
		}
		return Commons.NEGATIVESIGN+data;
	}
	
	String manageBeginData(String data) throws ModelDefinitionException {
		if(data.length()==0) {
			return null;
		}
		return Commons.BEGINSIGN+manageInterval(data,Commons.beginAccessors,Commons.BEGINSIGN);
	}
	
	String manageEndData(String data) throws ModelDefinitionException {
		if(data.length()==0) {
			return null;
		}
		return Commons.ENDSIGN+manageInterval(data,Commons.endAccessors,Commons.ENDSIGN);
	}
	
	String manageSizeData(String data,boolean optimal) throws ModelDefinitionException {
		if(data.length()==0) { 
			return null;
		}
		String isOptimal ="";
		if(optimal) {
			isOptimal = Commons.OPTIMALSIGN;
		}
		return Commons.LENGTHSIGN+isOptimal+manageInterval(data,Commons.sizeAccessors,Commons.LENGTHSIGN);
	}
	
	String manageCostData(String data) throws ModelDefinitionException {
		if(data.length()==0) {
			return null;
		}
		//FIX 1415
		if(data.startsWith("%")) {
			data = data.substring(1);
			return "p"+Commons.COSTSIGN+manageInterval(data,Commons.costAccessors,Commons.COSTSIGN);
		}
		return Commons.COSTSIGN+manageInterval(data,Commons.costAccessors,Commons.COSTSIGN);
	}
	
	String manageDistData(String data) throws ModelDefinitionException {
		if(data.length()==0) { 
			return null;
		}
		//FIX 1415
		if(data.startsWith("%")) {
			data = data.substring(1);
			return "p"+Commons.DISTANCESIGN+manageInterval(data,Commons.distAccessors,Commons.DISTANCESIGN);
		}		
		return Commons.DISTANCESIGN+manageInterval(data,Commons.distAccessors,Commons.DISTANCESIGN);
	}
	
	//@FIX 1683
	String manageAlphabetData(String alphabet,String percentage) throws ModelDefinitionException {
		if(alphabet.length()==0 || percentage.length()==0) {
			return null;
		}
		try {
			Integer.parseInt(percentage);
			}
			catch (Exception e) {
			throw new ModelDefinitionException("Error, percentage is not an integer");
			}
		
		return Commons.ALPHABETSIGN+" \""+alphabet+"\":"+percentage;
	}
	
	String manageContentData(String data) {
		if(data.length()==0) {
			return null;
		}
		return manageVariable(data,Commons.contentAccessors,Commons.CONTENTSIGN);
	}
	
	String manageSaveData(String data) {
		if(data.length()==0) {
			return null;
		}
		return manageVariable(data,Commons.saveAccessors,Commons.SAVESIGN);
	}
	
	String manageInterval(String data,String[] accessors,String defaultAccessor) throws ModelDefinitionException {
		if(data.length()==0) {
			return null;
		}
		String[] intervals = data.split(",");
		switch(intervals.length) {
		case 1: {
			return "[0,"+manageVariableOperation(data,accessors,defaultAccessor)+"]";			
		}
		case 2: {
			return "["+manageVariableOperation(intervals[0],accessors,defaultAccessor)+","+manageVariableOperation(intervals[1],accessors,defaultAccessor)+"]";
		}
		default: {
			throw new ModelDefinitionException("Error: interval must be the form X or X,Y. You have defined more than 2 variables: "+data);
		}
		
		}
	}


	private String manageVariableOperation(String data,String[] accessors, String defaultAccessor) {

		// This is not a number so it is a variable or an operation
			String[] operations = data.split("\\"+Commons.PLUS);
			String operation = Commons.PLUS;
			if(operations.length==1)  {
				operations = data.split("\\"+Commons.MINUS);
				operation = Commons.MINUS;
			}
			if(operations.length==1) {
				return manageVariable(data,accessors,defaultAccessor);
			}
			
			return manageVariable(operations[0],accessors,defaultAccessor)+" "+operation+" "+manageVariable(operations[1],accessors,defaultAccessor);
		

	}

	
	private String manageVariable(String data) {
		return manageVariable(data,null,"",true);
	}

	private String manageVariable(String data,boolean addComments) {
		return manageVariable(data,null,"",addComments);
	}
	
	
	private String manageVariable(String data,String[] accessors, String defaultAccessor) {
		return manageVariable(data,accessors,defaultAccessor,true);
	}
	
	private String manageVariable(String inData,String[] accessors, String defaultAccessor,boolean addComments) {

		String data = inData;
		
		try {
			// Check if it is an integer only
			int value = Integer.parseInt(data);
			return String.valueOf(value);
			}
		catch(NumberFormatException e) {
			// This is a variable
			// Does it start with an accessor
			String startWithAccessor = null;
			if(accessors!=null) {
			for(int i=0;i<accessors.length;i++) {
			if(data.startsWith(accessors[i])) {
				startWithAccessor=accessors[i];
				break;
			}
			}
			}
			
			if(data.startsWith("\"")) {
				// This is a terminal
				return defaultAccessor+data.toLowerCase();
			}

			if(terminals.containsKey(data)) {
				// if it is a terminal definition, return accessor + content
				return defaultAccessor+"\""+terminals.get(data).toLowerCase()+"\"";
			}
			
			if(startWithAccessor!=null) {
				data = data.substring(startWithAccessor.length());
			}
			else {
				startWithAccessor=defaultAccessor;
			}
			
			if(Commons.varMap.containsKey(data)) {
				String out = startWithAccessor+Commons.varMap.get(data);
				if(addComments && data.length()>0) {
					out +="%"+data+"%";
				}
				return out;
			}
			else {
				String var = Commons.LOGOLVAR+Commons.getVarCounter();
				Commons.varMap.put(data,var);
				String out = startWithAccessor+var;
				if(addComments && data.length()>0) {
					out += "%"+data+"%";
				}
				return out;
			}
		}
	}
	
	private String manageParentVariable(String data) {
		return manageParentVariable(data,true);
	}
	
	private String manageParentVariable(String data, boolean addComments) {

			if(terminals.containsKey(data)) {
				return "\""+terminals.get(data).toLowerCase()+"\"";
			}
		
			// This is a variable
			if(Commons.parentVarMap.containsKey(data)) {
				String out= Commons.parentVarMap.get(data);
				if(addComments && data.length()>0) {
					out+="%"+data+"%";
				}
				return out;
				
			}
			else {
				String var = Commons.LOGOLVAR+Commons.getVarCounter();
				Commons.parentVarMap.put(data,var);
				if(addComments && data.length()>0) {
					var+="%"+data+"%";
				}
				return var;
			}
		
	}
	
	
	
	
	protected String addConstraints(String[] constraints) {
		int nbConstraints = constraints.length;
		if(nbConstraints==0) {
			return "";
		}
		String out = "";
		int nbmatch=0;

		for(int i=0;i<nbConstraints;i++) {
			if(constraints[i]!=null) {
				if(nbmatch>0) {
					out+=",";
				}
				out+=constraints[i];
				nbmatch++;
			}
		}
		
		return out;
	}


	public boolean isShowComments() {
		return showComments;
	}


	public void setShowComments(boolean showComments) {
		this.showComments = showComments;
	}
	
}
