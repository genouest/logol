package org.irisa.genouest.logol;

import org.irisa.genouest.logol.utils.LogolUtils;

/**
 * Expression class is used to manage logol expression using variables and integer combinations.
 * <br/>It can be made of:
 * <li>an interger: 3</li>
 * <li>a variable reference: #SX1</li>
 * <li>a combination of both: #SX1 + 3</li>
 * <br/>In case of combination, variable must always be set first
 * @author osallou
 * 
 * History:
 * 21/10/13 @FIX 2244 length of constraint in Interval can be > 1
 *
 */
public class Expression {

	public String expression=null;
	public String variable=null;
	private boolean isSpacer=false;
	
	/**
	 * Default expression creation. Uses variable settings, not spacer ones.
	 */
	public Expression() {
	}
	
	/**
	 * Defines a new expression.
	 * @param spacer Set if expression refer to a spacer or a variable for defaults settings.
	 */
	public Expression(boolean spacer) {
		this.isSpacer=spacer;
	}
	
	public Expression(String expr, String var) {
		this.expression=expr;
		this.variable=var;
	}
	
	/**
	 * Analyse an expression, and map it in a prolog query and a variable referencing the result
	 * @param value the expression to analyse
	 * @return an expression with resulting prolog
	 * @throws GrammarException 
	 */
	public Expression getExpressionData(String value) throws GrammarException {
		
		String prolog="";
		
		/*
		if(Character.isDigit(value.charAt(0))) {	
			try {
			Integer.parseInt(value);
			}
			catch(NumberFormatException e) {
				throw new GrammarException("First parameter is not a variable while starting with a digit. Value must be integer only are be like \"variable + digit\": "+value);
			}
			return new Expression("",value);
		}
		*/
		
		// If equals no limit character (valid usually for max only)
		if(Constants.ANY.equals(value)) {
			if(this.isSpacer) {
				return new Expression("",Integer.toString(Treatment.maxSpacerLength));
			}
			else {
			return new Expression("",Integer.toString(Treatment.maxLength));
			}
		}
		
		String[] expression = value.split("\\s");
		// First character MUST be length,cost... constraint #,?...
		

		
		// Default suppose this is a: VARIABLE operator INT
		int variableIndice = 0;
		
		if(Character.isDigit(expression[0].charAt(0))){
			// This is a: INT operator VARIABLE
			variableIndice = 2;
		}
		
		// If expression is : INT
		if(expression.length==1 && variableIndice==2){
			return new Expression("",value);
		}
		
		
		//String variable = expression[variableIndice].substring(1);
		String subVariable = null;
		//String type = expression[variableIndice].substring(0, 1);
		
		String[] possibleConstraints = new String[] {Constants.CONTENTSIGN, Constants.DISTANCESIGN, Constants.COSTSIGN, Constants.LENGTHSIGN, Constants.ENDSIGN, Constants.BEGINSIGN};
		String variable = null;
		String type = null;
		for(int i=0;i<possibleConstraints.length;i++) {
			if(expression[variableIndice].startsWith(possibleConstraints[i])) {
				variable = expression[variableIndice].substring(possibleConstraints[i].length());
				type = expression[variableIndice].substring(0,possibleConstraints[i].length());
				break;
			}
		}
		
		if(variable==null || type==null) {
			throw new GrammarException("Could not match any constraint on variable definition: "+expression[variableIndice]);
		}
		
		//repeat variable sub data
		if(Constants.CONTENTSIGN.equals(type)) {
			// Separate variable name and accessor
			String[] t_tmpExpr = variable.split("\\.");
			variable = t_tmpExpr[0];
			subVariable = t_tmpExpr[1];
		}
		
		int reference = -1;
		if(LogolVariable.userVariables.get(new VariableId(variable,Treatment.currentModel.name))!=null) 		
		{ reference= Integer.parseInt((String)LogolVariable.userVariables.get(new VariableId(variable,Treatment.currentModel.name))); }
		if(LogolVariable.paramVariables.get(new VariableId(variable,Treatment.currentModel.name))!=null) 		
		{ reference= Integer.parseInt((String)LogolVariable.paramVariables.get(new VariableId(variable,Treatment.currentModel.name))); }
		
		if(reference==-1) {
			throw new GrammarException("Referenced variable could not be found, check variable exist. If it is a composed data e.g. VAR + 1, check a space is present between variable, operator and integer: "+value);
		}
		
		String tmpVar = LogolUtils.getTemporaryVariable();
		
		if(Constants.LENGTHSIGN.equals(type)) { prolog += ",getVariable("+Constants.LOGOLVARREF+reference+",_,_,_,"+tmpVar+",_,_,_,_)"; }
		if(Constants.BEGINSIGN.equals(type)) { prolog += ",getVariable("+Constants.LOGOLVARREF+reference+",_,"+tmpVar+",_,_,_,_,_,_)"; }
		if(Constants.ENDSIGN.equals(type)) { prolog += ",getVariable("+Constants.LOGOLVARREF+reference+",_,_,"+tmpVar+",_,_,_,_,_)"; }
		// Cost or distance
		if(Constants.COSTSIGN.equals(type)) { prolog += ",getVariable("+Constants.LOGOLVARREF+reference+",_,_,_,_,_,_,["+tmpVar+",_],_)"; }
		if(Constants.DISTANCESIGN.equals(type)) { prolog += ",getVariable("+Constants.LOGOLVARREF+reference+",_,_,_,_,_,_,[_,"+tmpVar+"],_)"; }
		
		//repeat variable sub data
		if(Constants.CONTENTSIGN.equals(type)) {
			// Get info data
			prolog += ",getVariable("+Constants.LOGOLVARREF+reference+",_,_,_,_,"+tmpVar+",_,_,_)";
			
			if(Constants.NBOCCUR.equals(subVariable)) {
				tmpVar = LogolUtils.getTemporaryVariable();
				prolog += ",length("+tmpVar+","+tmpVar+")";
				
			}
			if(Constants.MINDISTANCE.equals(subVariable)) {
				tmpVar = LogolUtils.getTemporaryVariable();
				prolog += ",getDistance("+tmpVar+","+tmpVar+",0)";

			}
			if(Constants.MAXDISTANCE.equals(subVariable)) {
				tmpVar = LogolUtils.getTemporaryVariable();
				prolog += ",getDistance("+tmpVar+","+tmpVar+",1)";

			}			
		}
		
		String varData = LogolUtils.getTemporaryVariable();
		if(expression.length>1) {
			
			if(variableIndice==0) {			
			if(expression[1].equals("+")) {
				prolog +=","+varData+" is "+tmpVar+"+"+expression[2];
			
			}
			else {
				prolog +=","+varData+" is "+tmpVar+"-"+expression[2];	
			}
			}
			else {
				if(expression[1].equals("+")) {
					prolog +=","+varData+" is "+expression[0]+"+"+tmpVar;
				
				}
				else {
					prolog +=","+varData+" is "+expression[0]+"-"+tmpVar;	
				}
			}
			
		}
		else {									
			prolog +=","+varData+"="+tmpVar;
		}
		return new Expression(prolog,varData);
	}
	
	
	
	
	
	
	
}
