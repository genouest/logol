package org.irisa.genouest.logol.types;

import java.util.HashSet;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.irisa.genouest.logol.Constants;
import org.irisa.genouest.logol.Expression;
import org.irisa.genouest.logol.GrammarException;
import org.irisa.genouest.logol.Interval;
import org.irisa.genouest.logol.LogolVariable;
import org.irisa.genouest.logol.Treatment;
import org.irisa.genouest.logol.utils.LogolUtils;

/**
 * Class managing repeat variable types
 * @author osallou
 * History: 02/06/09 Bug 1374 Add spacer as return var
 * 28/04/11 Bug 1794
 *
 */
public class RepeatVariable extends ViewVariable {

	private static final Logger logger = Logger.getLogger(org.irisa.genouest.logol.types.RepeatVariable.class);
	
	public RepeatVariable(int varID) {
		super(varID);
	}
	
	
	/**
	 * Manage repeat like variables
	 * @param lvar	current variable
	 * @return prolog content
	 * @throws GrammarException
	 */
	public String  content(LogolVariable lvar) throws GrammarException {
		
		logger.debug("Analysing repeat variable "+lvar.name);
		
		// manage repeat content
		int err=-1;
		int allowOverlap=0;
		
		boolean repeatQuantityPosponed=false;
		
		prologSentence+=LogolUtils.addParent(lvar,varID);	
		
		if (Treatment.isAny==-1) { err = isBeginConstraint(lvar,false); }
		
		
		String repeatPredicate="";
		// Add postponed variables
		String postponedList = LogolUtils.getTemporaryVariable();
		repeatPredicate= ",append(PostponedVariables,"+LogolVariable.getPostponedVariableList(lvar.model)+","+postponedList+")";
		
		// Get min and max repeat.
		String max = null;
		String min = null;
		Expression minRepeat=null;
		Expression maxRepeat=null;
		// checks if repeat quantity check is to be postponed
		if(!lvar.repeatPostpone())  {		
		
		if(lvar.repeatQuantity!=null) {
		max = new Interval(lvar.repeatQuantity).y;
		min = new Interval(lvar.repeatQuantity).x;
		minRepeat = new Expression().getExpressionData(min);	
		maxRepeat = new Expression().getExpressionData(max);
		
		if(minRepeat.expression!=null) {
			repeatPredicate+=minRepeat.expression;
		}
		if(maxRepeat.expression!=null) {
			repeatPredicate+=maxRepeat.expression;
		}
		
		
		}
		else {
			minRepeat = new Expression("","0");
			maxRepeat = new Expression("","0");
		}
		
		}
		else { repeatQuantityPosponed=true; }
		// Map repeat to a specific predicate to be called by repeat predicate.
		String predicate = RepeatVariable.map2predicate(lvar.content,lvar.id,postponedList,false);
		//repeatPredicate([ X | Y], Z, Pred, N, AllowStartSpacer, AllowIntermediateSpacer, AllowOverlap, NumberSpacer) :- repeatPredicate([ X | Y], Z, Pred, 0, N, AllowStartSpacer,AllowIntermediateSpacer, AllowOverlap, [], NumberSpacer).

		
		if(lvar.repeatType==1) {
			allowOverlap=0;
			}
		else {
			allowOverlap=1;
			}
		
		// Sets the intermediate spacer parameters
		
		String minSpacerVar = "-1";
		String maxSpacerVar = "-1";
		int allowIntermediateSpacer=0;
		
		int allowStartSpacer=0;
		String minStartSpacerVar ="0";
		String maxStartSpacerVar = String.valueOf(Treatment.maxSpacerLength);

		
		if(Treatment.saveAny==false) {
			// For "first" variables, we search with infinite gap e.g. max sequence size
			maxStartSpacerVar = Integer.toString(Treatment.sequenceLength);
		}
		
		
		if(Treatment.isAny>-1)  {			
			allowStartSpacer=1;
			
			if(!Constants.EMPTYSTRING.equals(Treatment.isAnyMin)) { minStartSpacerVar = Treatment.isAnyMin; }			
			if(!Constants.EMPTYSTRING.equals(Treatment.isAnyMax) && !Constants.SEQUENCELength.equals(Treatment.isAnyMax)) { maxStartSpacerVar = Treatment.isAnyMax; }	
			
		}
		
		String minIntermediateSpacer = new Interval(lvar.repeatParam).x;
		String maxIntermediateSpacer = new Interval(lvar.repeatParam).y;
		Expression minRepeatSpacer = new Expression().getExpressionData(minIntermediateSpacer);	
		Expression maxRepeatSpacer = new Expression().getExpressionData(maxIntermediateSpacer);
		
		if(minRepeatSpacer.expression!=null)  {
			repeatPredicate+=minRepeatSpacer.expression;
			minSpacerVar = minRepeatSpacer.variable;
			allowIntermediateSpacer=1;
		}
		if(maxRepeatSpacer.expression!=null) {
			repeatPredicate+=maxRepeatSpacer.expression;	
			maxSpacerVar = maxRepeatSpacer.variable;
			allowIntermediateSpacer=1;
		}
		if(maxSpacerVar.equals("0")) {
			allowIntermediateSpacer=0;
		}
		
		//                  repeatPredicate([ X | Y],                               Pred,          N,                 AllowStartSpacer,         MinStartSpacer,       MaxStartSpacer,        AllowIntermediateSpacer,     MinSpacer,             MaxSpacer,       AllowOverlap,     NumberSpacer,                         Errors,                              Info,                               Z)
		repeatPredicate +=",repeatPredicate_pos("+Constants.LOGOLVARBEFORE+lvar.id+", "+predicate+", "+maxRepeat.variable+", "+allowStartSpacer+","+minStartSpacerVar+","+maxStartSpacerVar+","+ allowIntermediateSpacer +"," + minSpacerVar + "," + maxSpacerVar+", "+allowOverlap +", "+Constants.LOGOLVARSPACER+lvar.id+",["+Constants.LOGOLVARERRORS+lvar.id+","+Constants.LOGOLVARINDEL+lvar.id+"],"+Constants.LOGOLVARINFO+lvar.id+", "+Constants.LOGOLVARAFTER+lvar.id+")";
		
		//repeatPredicate += ","+Constants.LOGOLVARPARENT+lvar.id+"="+lvar.parentId;		
		
	
		//compare number of repeats eg size of INFO to min
		if(!repeatQuantityPosponed) {
		String t_quantity = LogolUtils.getTemporaryVariable();
		repeatPredicate +=",length("+Constants.LOGOLVARINFO+lvar.id+","+t_quantity+"),"+t_quantity+">="+minRepeat.variable;
		}
		
		if (Treatment.isAny>-1 && err>=0) { err = isBeginConstraint(lvar,true); }
		
		if(err>=0) { err = isEndConstraint(lvar); }		
		
		
		//save global variable, info is set in repeat
		repeatPredicate += ",saveVariable('"+Constants.LOGOLVAR+lvar.id+"',"+Constants.LOGOLVARBEFORE+lvar.id+","+Constants.LOGOLVARSPACER+lvar.id+","+Constants.LOGOLVARAFTER+lvar.id+","+Constants.LOGOLVARINFO+lvar.id+",'"+lvar.text+"',["+Constants.LOGOLVARERRORS+varID+","+Constants.LOGOLVARINDEL+varID+"],"+lvar.getConfig()+","+Constants.LOGOLVARREF+lvar.id+")\n";
		
		
		//Check for postponed variable constraints
		prologSentence+=managePostponedVariables(lvar);
		
		//save any if required		
		if(Treatment.isAny>-1 && Treatment.saveAny) {
			repeatPredicate += ",saveVariable('"+Constants.LOGOLVAR+Treatment.isAny+"',"+Constants.LOGOLVARBEFORE+lvar.id+","+Constants.LOGOLVARSPACER+lvar.id+",[],'"+Constants.ANY+"',[0,0],[0,0],"+Constants.LOGOLVARREF+Treatment.isAny+")\n";
		}
		
		Treatment.saveAny=true;
		Treatment.isAny=-1;
		Treatment.isAnyMin="";
		Treatment.isAnyMax="";		
		
		prologSentence+=repeatPredicate;
		
		// View constraints applies the same way than for repeat, e.g. it applies to a global predicate, not a single variable.
		// Will check global cost, length... constraints
		applyConstraints(lvar);
		
		
		return prologSentence;
		
	}
	

	/*
	 * From a predicate or a list of predicate, create a new predicate and add it to a list of predicates.
	 * Returns a call to this newly created predicate.
	 * @param predicate progol sentence to map to an internal predicate
	 * @id identifier of the current variable
	 * @plist List of postponed variables
	 * @param overlap specific tag for management of overlaps
	 * @return prolog predicate call 
	 */
	public static String map2predicate(String predicate,int id, String plist,boolean overlap) {
		// New predicate to create

		String postponedList = null;
		
		String pred = Constants.LOGOLVARPRED+getPredCount();
		// Predicate that will be called from main
		String callPred = pred;
		
		String postponedVars="";
		
		String refVariable=Constants.EMPTYSTRING;
		
		
	
		// Search for variables used
		HashSet<String> vars = LogolUtils.getAllUsedVariables(predicate);
		String before = Constants.LOGOLVARAFTER+(Integer.parseInt(LogolUtils.getPredicateInput(predicate))-1);
		String after = Constants.LOGOLVARAFTER+LogolUtils.getPredicateOutput(predicate);

		
		String model = LogolVariable.getModel(id);
		
		Iterator<String> it = vars.iterator();
		pred+="("+before;
		pred+=",PostponedVariables";
		
		if(plist==null) {
		postponedList=LogolUtils.getTemporaryVariable();
		postponedVars= "append(PostponedVariables,"+LogolVariable.getPostponedVariableList(model)+","+postponedList+")";
	   
		}
		else {
			postponedList=plist;
		}
		
		callPred+="("+Constants.LOGOLVARBEFORE+id;
		callPred+=","+postponedList;
		while(it.hasNext()) {
			refVariable=(String) it.next();
			pred+=","+refVariable;
			callPred+=","+refVariable;
			
		}
		// Do not manage Spacer value as in ViewVariable because it is already managed in RepeatPredicate predicate
		pred+=",Parent,["+Constants.LOGOLVARERRORS+id+","+Constants.LOGOLVARINDEL+id+"],"+Constants.LOGOLVARINFO+id+","+after+") :- ("+predicate+")";
		callPred+=","+Constants.LOGOLVARPARENT+id+",["+Constants.LOGOLVARERRORS+id+","+Constants.LOGOLVARINDEL+id+"],"+Constants.LOGOLVARINFO+id+","+Constants.LOGOLVARAFTER+id+")";			


		
		// Compute the cost of all variables used in the predicate
		vars = LogolUtils.getErrorVariables(predicate);
		it = vars.iterator();
		String elt = it.next();
		String list = "[" + elt;
		String listindel = "[" + elt.replaceAll(Constants.LOGOLVARERRORS, Constants.LOGOLVARINDEL);
		while(it.hasNext()) {
			elt = it.next();
			list+=","+elt;
			listindel+=","+elt.replaceAll(Constants.LOGOLVARERRORS, Constants.LOGOLVARINDEL);
		}
		list+="]";
		listindel+="]";
		
		if(!overlap) {
			pred += ","+Constants.LOGOLVARINFO+id+"= " + LogolUtils.getSavedVariables(predicate,id,model);
		}
		
		pred += ",computeCost("+list+",Cost),"+"computeCost("+listindel+",Indel),"+Constants.LOGOLVARINDEL+id+"=Indel,"+Constants.LOGOLVARERRORS+id+"=Cost.";
		
		predicates.add(pred);
		
		if(plist==null) {
			callPred = ","+postponedVars+","+callPred;
		}
		
		
		return callPred;
	}
	
	
	
}
