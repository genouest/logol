package org.irisa.genouest.logol.utils.model;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.apache.xerces.impl.dv.util.Base64;
import org.apache.xpath.NodeSet;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


class LogolNode {
	
	String data="";
	Node node=null;
	
	public LogolNode() {
		
	}
	
	public LogolNode(String text,Node p_node) {
		data=text;
		node = p_node;
	}
}

/**
 * 
 * @author osallou
 * History : 21/06/09 O. Sallou Fix1380 missing space for multiple models
 * 			 28/07/09 O. Sallou Fix1397 Add support for negative constraint on views
 * 			 21/01/10 O. Sallou Fix1551 Analyser does not color correctly path in Designer
 */
public class ModelConverter {

	private static final Logger logger = Logger.getLogger(org.irisa.genouest.logol.utils.model.ModelConverter.class);

	
	Vector<String> containers = new Vector<String>();
	
	public HashMap<String,String> terminals = null;
	
	public HashMap<String,String> metacontrols = null;
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		ModelConverter lgConv = new ModelConverter();
		String out = lgConv.encode(args[0]);
		System.out.println(out);
		
		//String msg = lgConv.mapSequenceMatch(args[0],"0-1",args[1]);
		//System.out.println(msg);

	}
	
	
	/**
	 * Maps a Logol Match to the initial model to show the result
	 * @param match Result match file path
	 * @param id identifier of the match in the file
	 * @param outFile output file name
	 * @return New diagram file to load in sequence diagram analyser
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws TransformerException 
	 * @throws ModelDefinitionException 
	 */
	public String mapSequenceMatch(String match, String id, String outFile) throws ParserConfigurationException, SAXException, IOException, TransformerException, ModelDefinitionException {
	
		DocumentBuilder builder;
		
		InputStream in = new FileInputStream(match);
		builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		
		
		
		Document document = builder.parse(in);
		// Load terminal definitions
		String matchExpression="/sequences/match[id=\""+id+"\"]";
		Node matchNode = XPathAPI.selectSingleNode(document,matchExpression);
		//TODO get model id to know on which model rule we should apply result
		NodeList matchChilds = matchNode.getChildNodes();
		int modelId=1;
		for(int i=0;i<matchChilds.getLength();i++) {
			Node child = matchChilds.item(i);
			if(child.getNodeName().equals("model")) {
				modelId = Integer.parseInt(child.getTextContent());				
			}
		}

		// Get model from tag model in result match file
		String modelFromMatchExpression="/sequences/model";
		Node modelFromMatchNode = XPathAPI.selectSingleNode(document,modelFromMatchExpression);
		if(modelFromMatchNode==null) {
			return "Model is not present in result file";
		}
		String model = new String(Base64.decode(modelFromMatchNode.getTextContent()));		
		
		//InputStream in2 = new FileInputStream(model);
		InputStream in2 = new ByteArrayInputStream(model.getBytes());
		
		Document modelDocument = builder.parse(in2);
		
		terminals = new HashMap<String,String>();
		
		// Load terminal definitions
		String terminalExpression="/mxGraphModel/root/Terminal";
		NodeList matchNodeList = XPathAPI.selectNodeList(modelDocument,terminalExpression);
		for(int t=0;t<matchNodeList.getLength();t++) {
			String name = matchNodeList.item(t).getAttributes().getNamedItem(Commons.label).getNodeValue();
			String data = matchNodeList.item(t).getAttributes().getNamedItem(Commons.data).getNodeValue();
			terminals.put(name, data);
		}		
			
		
		String ruleExpression="/mxGraphModel/root/Rule";
		Node ruleNode = XPathAPI.selectSingleNode(modelDocument,ruleExpression);
		//Loop over all model rules, stop at selected model and fill data with match
		NodeList nextRuleNodes = null;
		for(int mod=0;mod<modelId;mod++) {
			nextRuleNodes = getNextElements(modelDocument,ruleNode);
		}

		LogolInterfaceNode curNode = getCurrentNode(nextRuleNodes.item(0));
		curNode.update(nextRuleNodes.item(0), matchNode);	
			
			if(curNode instanceof ModelNode) {
				//TODO manage model between node and match e.g. add new attributes with value error etc... from match
				// search corresponding model and start mapping .../Start[@label=..]
				String modelExpression="/mxGraphModel/root/Start[@name=\""+nextRuleNodes.item(0).getAttributes().getNamedItem("name").getTextContent()+"\"]";
				Node modelNode = XPathAPI.selectSingleNode(modelDocument,modelExpression);	
				curNode.update(modelNode,matchNode);	
				NodeList list = matchNode.getChildNodes();
				NodeSet varlist = new NodeSet();
				for(int l = 0; l< list.getLength(); l++) {
					if(list.item(l).getNodeName().equals("variable")) {
						varlist.addNode(list.item(l));
					}
				}
				
				mapChildNodes(modelNode, getVariableNextNode(matchNode.getFirstChild()),0);
			}

	        DOMSource domSource = new DOMSource(modelDocument);
	        

	        FileWriter writer = new FileWriter(outFile);
	        
	        StreamResult result = new StreamResult(writer);
	        TransformerFactory tf = TransformerFactory.newInstance();
	        Transformer transformer = tf.newTransformer();
	        transformer.transform(domSource, result);
	        writer.flush();
	        writer.close();
	        		
		return null;
	}
	
	
	private Node getVariableNextNode(Node match){
		Node next=null;
		Node tmpNode = match.getNextSibling();
		
		// Case of repeat:
		/**
		 * If a next node on same level has the same name then a previous one, go up
		 */		
		if(tmpNode == null)  {
			logger.debug("next is null go up ");
			//Fix 1551 : error when showing in designer
			if(match.getParentNode()!=null) {
				return getVariableNextNode(match.getParentNode());
			}
			else return null;
			//return match.getParentNode().getNextSibling();
		}
		
		
		
		if(!tmpNode.getNodeName().equals("variable")) {
			next = getVariableNextNode(tmpNode);
		}
		else {
			Node prevNode = match.getPreviousSibling();
			boolean notFound=true;
			while(prevNode!=null && notFound==true) {
				// Compare names
				// if equals set notfound=false;
				if(prevNode.getNodeName().equals("variable")) {
				String nextName = tmpNode.getAttributes().getNamedItem("name").getTextContent();
				String prevName = prevNode.getAttributes().getNamedItem("name").getTextContent();
				logger.debug("Comparing: "+nextName+" with "+prevName);
				if(nextName.equals(prevName)) {
					notFound=false;
					break;
				}
				}
				prevNode = prevNode.getPreviousSibling();
			}
			
			if(notFound) {
			next = tmpNode;
			}
			else {
				logger.debug("repeat case, go up ");
				return match.getParentNode().getNextSibling();
			}
		}
		logger.debug("RETURN "+next);
		return next;
	}
	

	
	private Node getLastVariableNode(Node match){		
		Node next=null;
		if(match.getNodeName().equals("variable")) return match;
				
		Node tmpNode = match.getPreviousSibling();
		
		if(tmpNode!=null) next = getLastVariableNode(tmpNode);
	
		if(tmpNode == null) return null;
		
		return next;
	}
	
	private void mapChildNodes(Node model, Node matchitem, int index) {
	//private void mapChildNodes(Node model, NodeList matchNodes, int index) {
		
		try {
			
			if(matchitem==null) return;
			
			logger.debug("VAR: "+matchitem.getNodeName());
			// If this is a view
			NodeList list = matchitem.getChildNodes();
			String matchtext="";
			for(int l=0;l<list.getLength();l++) {
				if(list.item(l).getNodeName().equals("text")) {
					matchtext = list.item(l).getTextContent();
					break;
				}
			}
			logger.debug("var text: "+matchtext);
			
			if(matchtext.startsWith("(")) {
				logger.debug("matching a view: "+matchtext);
					// Go to childs, stay at same place in model diagram
					mapChildNodes(model,getVariableNextNode(matchitem.getFirstChild()),0);
					return;
			}
	
			// If this is a repeat
			if(matchtext.startsWith("repeat(")) {
					// Go to childs, stay at same place in model diagram
					// Show last child only
					//mapChildNodes(model,getLastVariableNode(matchitem.getLastChild()),0);
				mapChildNodes(model,getVariableNextNode(matchitem.getFirstChild()),0);
					
					return;
			}			

			
			NodeList childs = getNextElements(model.getOwnerDocument(),model);
			for(int c=0;c<childs.getLength();c++) {
				//update all childs except if repeat
				//TODO detect view change to apply modifications on views
				// CAUTION in logol, fork/merge will create a view in results
				// repeat will create a view in results
				// Repeats, could specify number of repeats in repeat block?? maybe directly in view
				Node item = childs.item(c);
				LogolInterfaceNode curNode = getCurrentNode(item);				
				
				if(curNode instanceof RepeatNode) return;
				if(curNode instanceof EndNode) return;
				if(curNode instanceof VariableNode || curNode instanceof SpacerNode) {
					//TODO check model is equal to definition
					curNode.setTerminals(terminals);
					String grammar = curNode.getNodeGrammar(item);

					NodeList matchChilds = matchitem.getChildNodes();
					String text="";					
					for(int i=0;i<matchChilds.getLength();i++) {						
						Node child = matchChilds.item(i);
						
						if(child.getNodeName().equals("text")) {
							
							text = child.getTextContent();
							logger.debug("text of current var= "+text);
							break;
						}
					}
					Pattern p = Pattern.compile("(LOGOLVAR\\d+%(\\w+)%)");
					Matcher m = p.matcher(grammar);	
					String grammarTransform = grammar;
					while(m.find()) {				
						grammarTransform = grammarTransform.replaceAll(m.group(1), m.group(2));					
					}					
					logger.debug("grammarTransform="+grammarTransform);
					
					if(curNode instanceof SpacerNode) {
						grammarTransform = grammarTransform.replaceAll("\\.\\*","_");
						}
					
					logger.debug(text+" =? "+grammarTransform);
					if(text.equals(grammarTransform)) {									
						
						curNode.update(item, matchitem);	
						mapChildNodes(item,getVariableNextNode(matchitem),0);
					}
				}
				if(curNode instanceof ForkNode || curNode instanceof MergeNode) {
					//nothing continue , the branches will be managed in loop (getNextElements) for fork.
					// For merge, nothing to do.
					mapChildNodes(item,matchitem,index);
				}
				if(curNode instanceof ModelNode) {
					curNode.update(item,matchitem);
					mapChildNodes(item,getVariableNextNode(matchitem),0);
					String modelExpression="/mxGraphModel/root/Start[@name=\""+item.getAttributes().getNamedItem("name").getTextContent()+"\"]";
					
					Node modelNode = XPathAPI.selectSingleNode(item.getOwnerDocument(),modelExpression);

					curNode.update(modelNode, matchitem);

					mapChildNodes(modelNode,getVariableNextNode(matchitem.getFirstChild()),0);
				}
				
			}
			
			
		} catch (TransformerException e) {
			logger.error(e.getMessage());
		} catch (ParserConfigurationException e) {
			logger.error(e.getMessage());
		} catch (SAXException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (ModelDefinitionException e) {
			logger.error(e.getMessage());
		}
	}
	

	/**
	 * Encode a model to a logol representation
	 * @param model File path to the model
	 * @return Logol String representation
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws TransformerException
	 * @throws ModelDefinitionException
	 */
	public String encode(String model) throws ParserConfigurationException, SAXException, IOException, TransformerException, ModelDefinitionException  {
		
		terminals = new HashMap<String,String>();
		metacontrols = new HashMap<String,String>();
		
		DocumentBuilder builder;
		String out="";

		
		InputStream in = new FileInputStream(model);
		builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document document = builder.parse(in);
		

		// Load terminal definitions
		String terminalExpression="/mxGraphModel/root/Terminal";
		NodeList matchNodeList = XPathAPI.selectNodeList(document,terminalExpression);
		for(int t=0;t<matchNodeList.getLength();t++) {
			String name = matchNodeList.item(t).getAttributes().getNamedItem(Commons.label).getNodeValue();
			String data = matchNodeList.item(t).getAttributes().getNamedItem(Commons.data).getNodeValue();
			terminals.put(name, data);
		}
		
		// Load meta controls definitions
		String metacontrolExpression="/mxGraphModel/root/Metacontrol";
		NodeList metaNodeList = XPathAPI.selectNodeList(document,metacontrolExpression);
		for(int t=0;t<metaNodeList.getLength();t++) {
			String name = metaNodeList.item(t).getAttributes().getNamedItem(Commons.label).getNodeValue();
			String data = metaNodeList.item(t).getAttributes().getNamedItem(Commons.data).getNodeValue();
			metacontrols.put(name, data);
		}	
		
		String startExpression="/mxGraphModel/root/Start";
		LogolNode logolNode = new LogolNode();
		matchNodeList = XPathAPI.selectNodeList(document,startExpression);		
		
		if(matchNodeList.getLength()==0) {
			throw new ModelDefinitionException("Error, there is no start node");
		}
		
		// Loop for all start e.g. all models.
		containers.add("1");
		for(int a=0;a<matchNodeList.getLength();a++) {
			Node startNode = matchNodeList.item(a);
			//System.out.println("matched start"+startNode.getNodeName());
			logolNode = treatNodes(document,startNode,"1",false,false);
			out += logolNode.data+"\n";
			
			for(String meta : metacontrols.keySet()) {
				String metacontrol = metacontrols.get(meta);

				for(String key: terminals.keySet()) {
					if(metacontrol.contains(key)) {
						metacontrol.replaceAll(key, terminals.get(key));
					}
				}
				for(String key: Commons.varMap.keySet()) {
					if(!key.equals("")  && metacontrol.contains(key)) {
						metacontrol = metacontrol.replaceAll(key, Commons.varMap.get(key));
					}
				}
				metacontrols.put(meta, metacontrol);
			}
			
			
			
			Commons.reset();
			
			
		}
		
		String ruleExpression="/mxGraphModel/root/Rule";
		NodeList ruleNodeList = XPathAPI.selectNodeList(document,ruleExpression);
		if(ruleNodeList.getLength()>1) {
			throw new ModelDefinitionException("Error, only one rule can be defined!");
		}
		if(ruleNodeList.getLength()==0) {
			throw new ModelDefinitionException("Error, no rule is defined!");
		}	
		NodeList nextRuleNodes = getNextElements(document,ruleNodeList.item(0));
		if(nextRuleNodes.getLength()>1) {
			throw new ModelDefinitionException("Error, models after rule are only sequential, not parallel!");
		}
		if(nextRuleNodes.getLength()==0) {
			throw new ModelDefinitionException("Error, no model is defined after rule!");
		}		
		out += treatRuleNodes(document,nextRuleNodes.item(0),false)+"==*>SEQ1";
		
		
		// Load matrix definitions
		String morphismExpression="/mxGraphModel/root/Morphism";
		matchNodeList = XPathAPI.selectNodeList(document,morphismExpression);
		String definitions="";
		for(int t=0;t<matchNodeList.getLength();t++) {
			if(t==0) definitions = "def:{\n";
			
			String name = matchNodeList.item(t).getAttributes().getNamedItem(Commons.name).getNodeValue();
			String inData = matchNodeList.item(t).getAttributes().getNamedItem(Commons.in).getNodeValue();
			String outData = matchNodeList.item(t).getAttributes().getNamedItem(Commons.out).getNodeValue();
			if(terminals.containsKey(inData)) inData = terminals.get(inData);
			if(terminals.containsKey(outData)) inData = terminals.get(outData);
			definitions+="morphism("+name+","+inData+","+outData+").\n";			
			if(t==(matchNodeList.getLength()-1)) definitions+="}\n";
			
		}	
		
		String controls="";
		int m=0;
		for(String meta : metacontrols.keySet()) {
			if(m==0) controls = "controls:{\n";
			String metacontrol = metacontrols.get(meta);
			controls+=metacontrol+"\n";
			if(m==(metacontrols.size()-1)) controls+="}\n";
			m++;
		}
		
		
		out = definitions+controls+out;
		
		return out;
	}
	
	
	
	/**
	 * Recursive treatment for the nodes to get Logol representation
	 * @param document the XML model document
	 * @param startNode Node corresponding to Start node.
	 * @param addOperator True if operator "," or ";" or "|" should be added in front of variable representation
	 * @return logol representation for the list of nodes following the rule node
	 * @throws ModelDefinitionException
	 * @throws TransformerException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private String treatRuleNodes(Document document, Node startNode,boolean addOperator) throws ModelDefinitionException, TransformerException, ParserConfigurationException, SAXException, IOException {

		String out="";
		LogolInterfaceNode curNode = getCurrentNode(startNode);
		curNode.setTerminals(terminals);
		if(curNode instanceof EndNode) {
			return "";
		}
		if(curNode instanceof ModelNode) {
				curNode.setShowComments(false);
				if(addOperator) {
					//@Fix 1380
					out+=". ";
				}
				out+= curNode.getNodeGrammar(startNode);
				NodeList nextRuleNodes = getNextElements(document,startNode);
				if(nextRuleNodes.getLength()>1) {
					throw new ModelDefinitionException("Error, models after rule are only sequential, not parallel!");
				}
				if(nextRuleNodes.getLength()==0) {
					return "";
				}
				out += treatRuleNodes(document,nextRuleNodes.item(0),true);

		
				
		}
		else {			
			throw new ModelDefinitionException("Error, only models are acceptable blocks for a rule! Error on id = "+startNode.getAttributes().getNamedItem("id").getNodeValue());
		}
		
		return out;
	}






	/**
	 * Recursive treatment for the nodes to get Logol representation
	 * @param document the XML model document
	 * @param startNode Node corresponding to Start node.
	 * @param addOperator True if operator "," or ";" or "|" should be added in front of variable representation
	 * @return logol representation for the list of nodes following the start node
	 * @throws ModelDefinitionException
	 * @throws TransformerException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private  LogolNode treatNodes(Document document,Node startNode, String previousParent, boolean stop, boolean addOperator) throws TransformerException, ParserConfigurationException, SAXException, IOException, ModelDefinitionException {
		
		
		NodeList nextNodes = null;
		String out="";
		
	
		LogolInterfaceNode curNode = getCurrentNode(startNode);
		curNode.setTerminals(terminals);

		if(curNode instanceof MergeNode || curNode instanceof EndNode) {
			// For merge, it is a closure, so no operator must be set now, will be done on next element.
			addOperator=false;
		}
		
		String operator = Commons.AND;		
		if(addOperator && curNode.doOverlap(startNode)) {
			operator = Commons.OVERLAP;
		}
		
		String currentParent = getParent(document,startNode);
		int levels=0;
		
		if(currentParent.equals(previousParent)) {
			// We still are in same parent, nothing to do
			if(addOperator) {
				out+=operator;
			}
		}
		else if((levels=isChild(document,currentParent,previousParent,0))>0) {
			// This is a child swimlane
			//System.out.println("new parent "+currentParent+" from "+previousParent);
			// We go in a new parent eg new view			
			if(addOperator) {
				operator = Commons.AND;	
				if(new ViewNode().doOverlap(getNode(document,currentParent))) {
					operator = Commons.OVERLAP;
				}
				out+=operator;
				addOperator=false;
			}
			for(int level=0;level<levels-1;level++) {
			out += "(";
			//previousParent = getParent(document,getNode(document,previousParent));
			}
			//Fix 1397
			if(new ViewNode().isNegative(getNode(document,currentParent))) {
				out+=Commons.NEGATIVESIGN;
			}
			out+="(";
			previousParent = getParent(document,getNode(document,previousParent));
		}
		
		else if((levels=isParent(document,currentParent,previousParent,0))>0) {
			// We have gone up
			//System.out.println("back to previous parent "+currentParent+" from "+previousParent+" nb level="+levels);
			for(int level=0;level<levels;level++) {	
			ViewNode viewNode = new ViewNode();
			viewNode.setTerminals(terminals);
			out +=")"+(viewNode.getGlobalInfo(getNode(document,previousParent)));
			previousParent = getParent(document,getNode(document,previousParent));
			}
			
			if(addOperator) {
				operator = Commons.AND;	
				if(new ViewNode().doOverlap(getNode(document,currentParent))) {
					operator = Commons.OVERLAP;
				}
				out+=operator;
			}
		}
		else {
			// This is a side swimlane
			//System.out.println("side swimlane "+currentParent+" from "+previousParent);
			out +=")"+(new ViewNode().getGlobalInfo(getNode(document,previousParent)));
			operator = Commons.AND;	
			if(new ViewNode().doOverlap(getNode(document,currentParent))) {
				operator = Commons.OVERLAP;
			}
			out+=operator;
			out +="(";
		}		

		
		NodeList sources=null;
		int nbrepeat = 0;
		

		//for(int i=0;i<nextNodes.getLength();i++) {
		sources = getSources(document,startNode);
		// This is a merge or a repeat loopback
		//Analyse sources to see if repeat.
		for(int rep=0;rep<sources.getLength();rep++) {
			//System.out.println("parent id "+sources.item(rep).getAttributes().getNamedItem("id").getNodeValue());
			if(isRepeatNode(sources.item(rep))) {
				// Add operator only once
				if(nbrepeat==0 && addOperator) {
					// Already done before
					//out+=operator;
				}
				nbrepeat++;								
				out +="repeat(";
			}
		}	
		
		//}
		
		
		
		LogolNode logolNode = new LogolNode();
		//out += ","+startNode.getNodeName()+"-"+startNode.getAttributes().getNamedItem("id").getNodeValue();
		out += curNode.getNodeGrammar(startNode);
		//System.out.println("out for var "+ startNode.getNodeName()+" : "+out);
		
		nextNodes = getNextElements(document,startNode);
		
		for(int next=0;next<nextNodes.getLength();next++) {
		if(isRepeatNode(nextNodes.item(next))) {
			RepeatNode rep = new RepeatNode();
			rep.setTerminals(terminals);
			out+= rep.getNodeGrammar(nextNodes.item(next));
			out+= ")";
			out+= rep.getGlobalInfo(nextNodes.item(next));
			/* Operator set at function start
			if(rep.doOverlap(nextNodes.item(next))) {
				out+=Commons.OVERLAP;
			}
			else {
				out+=Commons.AND;
			}
			*/
		}

		}
		
		/** 
		 * Now remove nodes corresponding to outgoing repeats as those are already managed. This will
		 * prevent from treating it as a fork (1 to many)
		 * 
		 */
		NodeSet tmpNodeList = new NodeSet();
		for(int next=0;next<nextNodes.getLength();next++) {
			if(!isRepeatNode(nextNodes.item(next))) {
				tmpNodeList.addNode( nextNodes.item(next));
			}
		}
		nextNodes = tmpNodeList;
		
		
		if(stop)  {
			return new LogolNode(out,nextNodes.item(0));
		}
		


		
		if(nextNodes.getLength()==0) {
			if(startNode.getNodeName().equals("End")) {
			return new LogolNode(out,null);
			}
			else {
				// ERROR, do not end on a terminate
				throw new ModelDefinitionException("Error, node "+startNode.getAttributes().getNamedItem("id").getNodeValue()+" does not end on a End node");
			}
		}


		
		if(nextNodes.getLength()==1) {
			
			sources = getSources(document,nextNodes.item(0));
			// Remove repeat, they are not taken into account for merge type decision
			sources = removeRepeats(sources);
			//System.out.println(nbSources+" sources for "+nextNodes.item(0).getAttributes().getNamedItem("id").getNodeValue());
			if(sources.getLength()>1) {

				//System.out.println("Merge "+nextNodes.item(0).getAttributes().getNamedItem("id").getNodeValue());
				//logolNode.node = nextNodes.item(0);
				
				logolNode = treatNodes(document,nextNodes.item(0),currentParent,true,true);
				out += logolNode.data;
				
				
									
			}
			else {
			// AND treatment
				boolean t_operator=true;
				if(curNode instanceof StartNode) {
					//System.out.println("current match a startnode");
					t_operator=false;
				}
			logolNode = treatNodes(document,nextNodes.item(0),currentParent,false,t_operator);
			out += logolNode.data;
			}
		}
		else {
			//This is a fork or a repeat
			
			
			//This is a fork
			//	System.out.println("Fork");
			// Manage until merge

			out +="(";
			logolNode = treatNodes(document,nextNodes.item(0),currentParent,false,false);
			out += "("+logolNode.data+")";			
			
			//System.out.println("data for branch "+logolNode.data);
			//if(logolNode.node!=null) System.out.println("data for branch "+logolNode.node.getAttributes().getNamedItem("id").getNodeValue());
			
			for(int i=1;i<nextNodes.getLength();i++) {
				if(!isRepeatNode(nextNodes.item(i))) {
				logolNode = treatNodes(document,nextNodes.item(i),currentParent,false,false);
				out += Commons.OR+"("+logolNode.data+")";
				//System.out.println("data for branch "+logolNode.data);
				//if(logolNode.node!=null) System.out.println("data for branch "+logolNode.node.getAttributes().getNamedItem("id").getNodeValue());
				}
				
			}
			out +=")";
			// Manage merge eg remaining after fork
			
			if(logolNode.node==null) {
				//System.out.println("no remaining after merge");
			}
			else {
				//System.out.println("manage merge "+logolNode.node.getAttributes().getNamedItem("id").getNodeValue());
			out += treatNodes(document,logolNode.node,currentParent,false,true).data;
			}
		}

		logolNode.data=out;
		//System.out.println("return "+logolNode.data+", "+logolNode.node);
		
		return logolNode;
	}
	
	
	/**
	 * Gets a node from a node id
	 * @param document XML model document
	 * @param previousParent id to get
	 * @return the matched node
	 * @throws TransformerException
	 */
	private Node getNode(Document document, String previousParent) throws TransformerException {
		String expr="/mxGraphModel/root/*[@id=\""+previousParent+"\"]";		
		Node node = XPathAPI.selectSingleNode(document,expr);
		return node;
	}


	/**
	 * Gets a node interface from current node type
	 * @param startNode Node to analyse
	 * @return specific type node
	 * @throws ModelDefinitionException
	 */
	protected LogolInterfaceNode getCurrentNode(Node startNode) throws ModelDefinitionException {
		String out = "";
		
		if(startNode.getNodeName().equals("Start")) {
			// This is a new model
			return new StartNode();
		}
		if(startNode.getNodeName().equals("End")) {
			// This is a new model
			return new EndNode();
		}
		if(startNode.getNodeName().equals("Variable")) {
			// This is a new model
			return new VariableNode();
		}		
		if(startNode.getNodeName().equals("Swimlane")) {
			// This is a new model
			return new ViewNode();
		}	
		if(startNode.getNodeName().equals("Spacer")) {
			// This is a new model
			return new SpacerNode();
		}
		if(startNode.getNodeName().equals("Model")) {
			// This is a new model
			return new ModelNode();
		}	
		if(startNode.getNodeName().equals("Merge")) {
			// This is a merge of nodes
			return new MergeNode();
		}	
		if(startNode.getNodeName().equals("Fork")) {
			// This is a fork of nodes
			return new ForkNode();
		}			
		return null;
	}
	
	
	/**
	 * Looks if current node is a repeat node
	 * @param item Node to analyse
	 * @return True if it is a repeat
	 */
	private  boolean isRepeatNode(Node item) {
		//System.out.println("isrepeatnode "+item.getAttributes().getNamedItem("id").getNodeValue());
		//System.out.println("isrepeatnode "+item.getAttributes().getNamedItem("label").getNodeValue());
		String parentLabel = item.getAttributes().getNamedItem("label").getNodeValue();
		if(parentLabel==null) {
			return false;
		}
		if(parentLabel.equals("repeat")) {
			return true; 
		}
		return false;
	}


	/**
	 * Checks recursively if current parent node is a parent of previous parent node in XML hiearchy
	 * @param document XML document
	 * @param currentParent id of the parent
	 * @param previousParent id of the previous parent
	 * @param levels current number of levels in hiearchy
	 * @return number of levels between the parents. 0 if none.
	 * @throws TransformerException
	 */
	private  int isParent(Document document, String currentParent,
			String previousParent, int levels) throws TransformerException {
		String expr1="/mxGraphModel/root/*[@id=\""+previousParent+"\"]";
		int res=0;
		
		Node node = XPathAPI.selectSingleNode(document,expr1);
		
		String expr2="mxCell";
		
		Node cell = XPathAPI.selectSingleNode(node,expr2);
		
		if(cell!=null && cell.getAttributes().getNamedItem("parent")!=null )  {
			//System.out.println("search parent previous="+previousParent+",parent= "+cell.getAttributes().getNamedItem("parent").getNodeValue());
			if(cell.getAttributes().getNamedItem("parent").getNodeValue().equals(currentParent)) {
				return (levels+1);
			}
			//else return false;
			else  {
				if((res=isParent(document,currentParent,cell.getAttributes().getNamedItem("parent").getNodeValue(),(levels+1)))>0) {
					return res;
				}
				else {
					return 0;
				}
				
			}
			
			//[@parent=\""+previousParent+"\"
		}
		else {
			return res;
		}
	}

	/**
	 * Checks recursively if current parent node is a child of previous parent node in XML hiearchy
	 * @param document XML document
	 * @param currentParent id of the parent
	 * @param previousParent id of the previous parent
	 * @param levels current number of levels in hiearchy
	 * @return number of levels between the parents. 0 if none.
	 * @throws TransformerException
	 */
	private  int isChild(Document document, String currentParent,
			String previousParent,int levels) throws TransformerException {
		//System.out.println("isChild "+currentParent+","+previousParent);
		String expr1="/mxGraphModel/root/*[@id=\""+currentParent+"\"]";
		int res=0;
		Node node = XPathAPI.selectSingleNode(document,expr1);
		if(node==null) {
			return levels;
		}
		String expr2="mxCell";
		
		Node cell = XPathAPI.selectSingleNode(node,expr2);
		
		/*
		if(cell!=null)  {
			if(cell.getAttributes().getNamedItem("parent").getNodeValue().equals(previousParent)) return true;
			else return false;
			//[@parent=\""+previousParent+"\"
		}
		else return levels;
		*/
		
		if(cell!=null && cell.getAttributes().getNamedItem("parent")!=null )  {
			//System.out.println("search parent previous="+previousParent+",parent= "+cell.getAttributes().getNamedItem("parent").getNodeValue());
			if(cell.getAttributes().getNamedItem("parent").getNodeValue().equals(previousParent)) {
				return (levels+1);
			}
			//else return false;
			else  {
				if((res=isParent(document,previousParent,cell.getAttributes().getNamedItem("parent").getNodeValue(),(levels+1)))>0) {
					return res;
				}
				else {
					return 0;
				}
				
			}
		}
		else {
			return res;
		}
		
		
		
	}


	/**
	 * Gets the parent node of current node
	 * @param document XML document
	 * @param node Node to analyse
	 * @return the parent node
	 * @throws TransformerException
	 */
	private  String getParent(Document document,Node node) throws TransformerException {
		String parentExpression="mxCell";
		
		Node cell = XPathAPI.selectSingleNode(node,parentExpression);
		if(cell.getAttributes().getNamedItem("parent")!=null) {
			return cell.getAttributes().getNamedItem("parent").getNodeValue();
		}
		else {
			return "";
		}
		
	}
	
	/**
	 * Gets the Nodes that are a source(input) for the current node
	 * @param document XML document
	 * @param node node to analyse
	 * @return List of matching nodes
	 * @throws TransformerException
	 */
	private  NodeList getSources(Document document,Node node) throws TransformerException {
		int nb=0;
		NodeSet sourceNodes = new NodeSet();
		String sourceExpression="/mxGraphModel/root/Edge/mxCell[@target=\""+node.getAttributes().getNamedItem("id").getNodeValue()+"\"]";
		
		NodeList edges = XPathAPI.selectNodeList(document,sourceExpression);
		
		for(int i=0;i<edges.getLength();i++) {
		String previousExpression="/mxGraphModel/root/*[@id=\""+edges.item(i).getAttributes().getNamedItem("source").getNodeValue()+"\"]";		
		Node source = XPathAPI.selectSingleNode(document,previousExpression);
		sourceNodes.addNode(source);
		}
		
		return sourceNodes;
	}
	
	/**
	 * gets the elements after (next) current node
	 * @param document XML document
	 * @param inputNode Node to analyse
	 * @return List of matching nodes
	 * @throws TransformerException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	protected  NodeList getNextElements(Document document,Node inputNode) throws TransformerException, ParserConfigurationException, SAXException, IOException {
		
		String nodeID = inputNode.getAttributes().getNamedItem("id").getNodeValue();
		

		String edgeExpression="/mxGraphModel/root/Edge/mxCell[@source=\""+nodeID+"\"]";
		
		NodeList targets = XPathAPI.selectNodeList(document,edgeExpression);
	
		NodeSet nextNodes = new NodeSet();
		for(int i=0;i<targets.getLength();i++) {
			
			String target = targets.item(i).getAttributes().getNamedItem("target").getNodeValue();
			String targetExpression="/mxGraphModel/root/*[@id=\""+target+"\"]";		
			Node targetNode = XPathAPI.selectSingleNode(document,targetExpression);
			if(targetNode!=null) {
				nextNodes.addNode(targetNode);
			}
		}
		
		
		return (NodeList) nextNodes;
	}




	/**
	 * Writes model grammar file to logol grammar file
	 * @param modelFile Input path to model file
	 * @param grammarFile Output path to logol file
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws TransformerException
	 * @throws ModelDefinitionException
	 */
	public void decode(String modelFile, String grammarFile) throws ParserConfigurationException, SAXException, IOException, TransformerException, ModelDefinitionException {
		String out = this.encode(modelFile);
		logger.info("Decoded grammar:\n"+out);
		File outfile = new File(grammarFile);
		logger.info("Write grammar file "+grammarFile);
		PrintWriter fw = new PrintWriter(outfile);	
		fw.println(out);
		fw.flush();
		fw.close();
	}

	/**
	 * Remove the repeat type nodes from a list of nodes
	 * @param sources Input List
	 * @return New list without repeat nodes
	 */
	private NodeList removeRepeats(NodeList sources) {
		NodeSet list = new NodeSet();
		for(int i=0;i<sources.getLength();i++) {
		String label = sources.item(i).getAttributes().getNamedItem("label").getNodeValue();
		if(label==null || !label.equals("repeat")) {
			list.addNode(sources.item(i));
		}
		}
		return list;
	}
	
}
