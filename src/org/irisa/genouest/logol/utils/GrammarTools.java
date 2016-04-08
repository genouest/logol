package org.irisa.genouest.logol.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.irisa.genouest.logol.Constants;
import org.irisa.genouest.logol.Expression;


public class GrammarTools {

	private static final Logger logger = Logger.getLogger(GrammarTools.class);
	
	static GrammarTools grammarTool = null;
	/*
	 * Map of var name / [min,max,direction]
	 */
	Map<String, String[]> variables = new HashMap<String, String[]>();
	
	Map<String, String> counters = new HashMap<String, String>();
	
	ArrayList<String> gotchaElements = new ArrayList<String>();
	
	
	/**
	 * Grammar combinations for each optmized variable. Contains all combinations (ArrayList) with counter value for each variable (String[])
	 */
	Map<String,ArrayList<String[]>> grammarCombinations = new HashMap<String,ArrayList<String[]>>();
	
	long nbSolutions = 0;

	
	String currentMainCounter = null;
	String currentCounter = null;

	public Map<String, String[]> getVariables() {
		return variables;
	}

	public void setVariables(Map<String, String[]> variables) {
		this.variables = variables;
	}

	public Map<String, String> getCounters() {
		return counters;
	}

	public void setCounters(Map<String, String> counters) {
		this.counters = counters;
	}
	
	public static GrammarTools getInstance() {
		if(grammarTool==null) {
			grammarTool = new GrammarTools();
		}
		return grammarTool;
	}
	
	/**
	 * Generate X grammar file based on input grammar file and variables.
	 * Replace the $VARNAME$ by counter value
	 */
	public void generateGrammars() {
		// Init counters		
		moveCounter(variables,true);
		logger.debug("Generates "+gotchaElements.size() + "solutions");
		gotchaElements.clear();
	}
	
	void moveCounter(Map<String,String[]> vars,boolean isMain) {

		if(vars.isEmpty()) {
			String gotcha = "";
			String[] varCounters = new String[counters.size()];
			int count=0;
			for(Entry<String,String> counter : counters.entrySet()) {
				gotcha += "#"+counter.getKey()+":"+counter.getValue();
				nbSolutions++;
				varCounters[count] =  counter.getKey()+":"+counter.getValue();
				count++;
			}
			
			
			if(!gotcha.equals("") && !gotchaElements.contains(gotcha)) {	
				logger.debug(gotcha);
				gotchaElements.add(gotcha);
				
				// Add grammar element combination
				ArrayList<String[]> combinations = grammarCombinations.get(currentMainCounter);
				if(combinations==null){
					combinations = new ArrayList<String[]>();
					grammarCombinations.put(currentMainCounter, combinations);
				}
				
				combinations.add(varCounters);
				
			}	
			return;
		}
		
		for(String variable : vars.keySet()) {
			// Take max - min
			
			if(isMain) {
				currentMainCounter = variable;
				if(variables.get(variable)[2].equals(Constants.OPTIMAL_MODE.MAX.toString())) {
					counters.put(variable, variables.get(variable)[0]);
				}
				else {
					counters.put(variable, variables.get(variable)[1]);
				}
				gotchaElements.clear();
			}
			else {
				if(variables.get(variable)[2].equals(Constants.OPTIMAL_MODE.MIN.toString())) {
					counters.put(variable, variables.get(variable)[0]);
				}
				else {
					counters.put(variable, variables.get(variable)[1]);
				}
			}
			
			//int diff = Integer.valueOf(vars.get(variable)[1])-Integer.valueOf(vars.get(variable)[0])+1;
			
			int diff = getDiff(vars.get(variable)[1],vars.get(variable)[0]) +1;
			
			// Copy variables map, but remove current one
			Map<String,String[]> newMap = new HashMap<String,String[]>();
			for(Entry<String,String[]> tmpvar : vars.entrySet()) {
				if(!variable.equals(tmpvar.getKey())) newMap.put(tmpvar.getKey(), tmpvar.getValue());
			}
					
			for(int i=0;i<diff;i++) {
				
			moveCounter(newMap,false);	
				
			// Now update current counter
			String value = counters.get(variable);						

											
			if(isMain) {
			if(variables.get(currentMainCounter)[2].equals(Constants.OPTIMAL_MODE.MAX.toString())) {
				counters.put(variable, String.valueOf(Integer.valueOf(value)+1));
			}
			else {
				counters.put(variable, String.valueOf(Integer.valueOf(value)-1));
			}
			}
			else {
				if(variables.get(currentMainCounter)[2].equals(Constants.OPTIMAL_MODE.MIN.toString())) {
					counters.put(variable, String.valueOf(Integer.valueOf(value)+1));
				}
				else {
					counters.put(variable, String.valueOf(Integer.valueOf(value)-1));
				}				
			}
			
			

			}
			
			logger.debug("Loop for "+variable+" si over");
			
			
			//reset current counter
			if(isMain){
			if(vars.get(variable)[2].equals(Constants.OPTIMAL_MODE.MAX.toString())) {
				counters.put(variable, vars.get(variable)[0]);
			}
			else {
				counters.put(variable, vars.get(variable)[1]);
			}
			}
			else {
				if(vars.get(variable)[2].equals(Constants.OPTIMAL_MODE.MIN.toString())) {
					counters.put(variable, vars.get(variable)[0]);
				}
				else {
					counters.put(variable, vars.get(variable)[1]);
				}
			}
		}
		
		

	}

	
	/**
	 * Analyse an expression to extract int value
	 * @param expr Grammar expression such as: 100 or #VAR1 + 100
	 * @return
	 */
	private Integer analyseExpression(String expr) {
		String[] expression = expr.split("\\s");
		// First character MUST be length,cost... constraint #,?...
			
		// Default suppose this is a: VARIABLE operator INT
		int variableIndice = 0;
		
		if(Character.isDigit(expression[0].charAt(0))){
			// This is a: INT operator VARIABLE
			variableIndice = 2;
		}
		
		// If expression is : INT
		if(expression.length==1 && variableIndice==2){
			return new Integer(expr);
		}
		
		
		if(expression.length>1) {	
			if(expression[1].equals("+")) {
				return new Integer(expression[2]);
			
			}
			else {
				return new Integer(expression[2])*(-1);	
			}						
		}
		else {									
			return new Integer(0);
		}
		
	}
	
	/**
	 * Analyse min and max expressions to determine difference. Expression must refer to same variables e.g. [#VAR1 - 10, #VAR1 + 10] or INTs [10, 100].
	 * @param max Maximum value defined as an expression
	 * @param min Minimum value defined as an expression
	 * @return Difference as an int
	 */
	protected Integer getDiff(String max,String min) {
		Integer diff = 0;

		Integer exprMax = analyseExpression(max);
		Integer exprMin = analyseExpression(min);
		logger.debug("Found difference: "+exprMax+" - "+exprMin);
		diff = exprMax - exprMin;
		
		return diff;
	}

	public static void main(String[] args) {
		 GrammarTools gTool = GrammarTools.getInstance();
		 Map<String,String[]> vars =gTool.getVariables();
		 vars.put("TEST1", new String[] { "0", "3", Constants.OPTIMAL_MODE.MAX.toString()});
		 vars.put("TEST2", new String[] { "0", "2", Constants.OPTIMAL_MODE.MAX.toString()});
		 vars.put("TEST3", new String[] { "0", "2", Constants.OPTIMAL_MODE.MAX.toString()});
		 gTool.generateGrammars();
	}

	public Map<String, ArrayList<String[]>> getGrammarCombinations() {
		return grammarCombinations;
	}
}
