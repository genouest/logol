package org.irisa.genouest.logol;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.irisa.genouest.logol.types.ViewVariable;
import org.irisa.genouest.logol.utils.LogolUtils;



/**
 * Entity class in charge or managing operators between variable entities (ANR,OR,OVERLAP).
 * @author osallou
 *
 */
public class Entity {

	private static final Logger logger = Logger.getLogger(org.irisa.genouest.logol.Entity.class);

	/*
	 * Contains generated predicates
	 */
	//public static Vector<String> predicates = new Vector<String>();
	
	/*
	 * Predicates used for pre-analysis
	 */
	//private static Vector<String> analysisPredicates = new Vector<String>();
	//private static int analysisPredCount = 0;
	//private static int predCount = 0;
	
	//public static final String OP_AND = ",";
	//public static final String OP_OR = "|";
	//public static final String OP_OVERLAP = ";";
	
	//private static final String EMPTYSTRING="";
	
	
	//public String currentModel=null;
	
	
	/*public static void reset() {
		predicates = new Vector<String>();
		analysisPredicates = new Vector<String>();
		//analysisPredCount = 0;
		predCount = 0;
	}*/
	
	/*
	 * Manage a prolog predicate content according to operator
	 * returns prolog to execute
	 * If operator = ',' returns predicate with operator
	 * If operator = ";" create predicate return overlap on new predicate
	 * If operator = '|' return predicate with operator
	 * Type = 0, this is a predicate
	 * Type = 1, this is a group (xxx)
	 * @param pred prolog content to add
	 * @param operator in front of sentence
	 * @param type of current prolog content (variable, model, view...)
	 * @param id identifier of current variable
	 * @return prolog to concatenate.
	 */
	public String add(String pred, String operator,int type, int id) {
		
		String predicate = pred;
		
		String prolog = Constants.EMPTYSTRING;
		
		/* This is a view (several predicate calls), so map to a replicate and make shortcut to it.
		 * or this is an overlap. As each test is made of several predicates (test, save, etc..), need to create a predicate for the whole
		 */
	
		if(Treatment.getParseStep()==Constants.VAR_ANALYSIS_STEP) { return Constants.EMPTYSTRING; }
		
		if(Treatment.getParseStep()==Constants.POSTCONDITION_ANALYSIS_STEP) {
			
			if (Constants.EMPTYSTRING.equals(predicate)) { return Constants.EMPTYSTRING; }
			
			if(type==2) {
				return analysispredicate(predicate,id,false);
			}
			
			if(operator==null) { return predicate; }
			if(Constants.OP_AND.equals(operator)||Constants.OP_OVERLAP.equals(operator)) {
				return ","+predicate;
			}
			if(Constants.OP_OR.equals(operator)) {
				return ";"+predicate;
			}
		}
		
		
		//first predicate
		if(operator==null) { return predicate; }

		if(type==2 || Constants.OP_OVERLAP.equals(operator)) {

			if(type==2) {
			predicate = ViewVariable.map2predicate(predicate,id);
			}
			else {
				String model = LogolVariable.getModel(id);
				// Set BEFORE = AFTER if type = 0
				prolog=","+Constants.LOGOLVARBEFORE+id+"="+Constants.LOGOLVARAFTER+(id-1);
				//prolog = "("+Constants.LOGOLVARPOSITION+id+" is "+Constants.LOGOLVARPOSITION+(id-1)+" + "+(id-1)+",";
				//prolog += "(getCharsFromPosition("+Constants.LOGOLVARPOSITION+id+","+Treatment.maxLength+","+Constants.LOGOLVARBEFORE+id+"),";
				String postponedList = LogolUtils.getTemporaryVariable();
				prolog+= ",append(PostponedVariables,"+LogolVariable.getPostponedVariableList(model)+","+postponedList+")";
				
				/* Add current variable as an overlapped variable because we need to
				 * be able to get its reference later on as a saved variable.
				 */ 
				LogolVariable.overlappedVariables.add(new VariableId(model,String.valueOf(id)));
				predicate = ViewVariable.map2predicate(predicate,id,postponedList,true);
			}
			
		}		
		
		
		if(Constants.OP_AND.equals(operator)) {
			return operator+predicate;
		}
		if(Constants.OP_OR.equals(operator)) {
			return ";"+predicate;
		}
		if(Constants.OP_OVERLAP.equals(operator)) {			
			String tmp_var = LogolUtils.getTemporaryVariable();
			prolog+= ",getVariable("+Constants.LOGOLVARREF+(id-1)+","+tmp_var+",_,_,_,_,_,_,_)";			
			prolog+=","+"testOverlap_pos("+Constants.LOGOLVARBEFORE+id+","+predicate+","+tmp_var+","+Constants.LOGOLVARDATA+id+","+Constants.LOGOLVARAFTER+id+")";
			
			return prolog;
		}
		return null;
	}


	/**
	 * Take a predicate, find all required variables, add it to internal predicates, and return a call to this predicate.
	 * @param predicate
	 * @param id
	 * @param useDummy Add DUMMY variable if no parameter, to avoid empty predicate calls
	 * @return prolog call to predicate
	 */
	public String analysispredicate(String predicate, int id,boolean useDummy) {
		//String pred = Constants.LOGOLVARPRED+predCount;
		String pred = LogolUtils.getPredicateVariable();
		// Predicate that will be called from main
		String callPred = pred;
		
		String refVariable=Constants.EMPTYSTRING;
		
		//predCount++;
		
		HashSet<String> vars = LogolUtils.getAllUserVariables(predicate);
		Iterator<String> it = vars.iterator();
		pred+="(";
		callPred+="(";
		if(it.hasNext()) {
			refVariable=(String) it.next();
			pred+=refVariable;
			callPred+=refVariable;
		}
		
		while(it.hasNext()) {
			refVariable=(String) it.next();		
			pred+=","+refVariable;
			callPred+=","+refVariable;
			
		}	
		if(useDummy && vars.size()==0) {
			pred+="LOGOLVARDUMMY";
			callPred+="LOGOLVARDUMMY";
		}
		pred+=") :- "+predicate+".";
		callPred+=")";
		ViewVariable.addPredicate(pred);
		
		return callPred;
	}

	
	 
	
}
