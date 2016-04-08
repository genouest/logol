package org.irisa.genouest.logol.types;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.irisa.genouest.logol.Constants;
import org.irisa.genouest.logol.Expression;
import org.irisa.genouest.logol.GrammarException;
import org.irisa.genouest.logol.LogolVariable;
import org.irisa.genouest.logol.StringConstraint;
import org.irisa.genouest.logol.StructConstraint;
import org.irisa.genouest.logol.Treatment;
import org.irisa.genouest.logol.Constants.OPTIMAL_CONSTRAINT;
import org.irisa.genouest.logol.utils.LogolUtils;

/**
 * Class managing a view type
 * @author osallou
 * History: 02/06/09 Bug 1374 Add spacer as return var
 * 		    28/07/09 Bug 1397 Add support for negative content constraint
 *          12/10/10 @FIX 1683 Add alphabet constraint
 *          20/11/13 Bug Move begin constraint check after analysis
 */
public class ViewVariable extends AbstractVariable {

	private static final Logger logger = Logger.getLogger(org.irisa.genouest.logol.types.ViewVariable.class);
	
	
	private static Vector<String> analysisPredicates = new Vector<String>();
	private static int predCount = 0;
	
	
	/**
	 * Adds an internal predicate
	 * @param pred String containing the prolog predicate definition
	 */
	public static void addPredicate(String pred) {
		analysisPredicates.add(pred);
	}
	
	/**
	 * Gets the definition of a prolog internal predicate
	 * @param predId
	 * @return predicate prolog content
	 */
	public static String getPredicate(int predId) {
		return analysisPredicates.get(predId);
	}
	
	/**
	 * Gets the number of internal predicates
	 * return number of internal predicates
	 */
	public static int getPredicateArraySize() {
		return analysisPredicates.size();
	}	
	
	
	/*
	 * Contains generated predicates
	 */
	public static Vector<String> predicates = new Vector<String>();	
	
	//public String currentModel=null;
	
	/**
	 * Reset static variables
	 */
	public static void reset() {
		predicates = new Vector<String>();
		analysisPredicates = new Vector<String>();
		predCount = 0;
	}
	
	
	
	public ViewVariable(int varID) {
		super(varID);
	}
	
	/**
	 * Set the prolog content for the view. Supports a spacer in front of a view (isAny), though
	 * spacer use is not competitive. It force the testing of a complete view with a slicing on the
	 * sequence, mainly if no length constraint is set on spacer. For better performance, spacer should be 
	 * set inside the view.
	 * @param lvar variable describing the view
	 * @throws GrammarException 
	 */
	public String content(LogolVariable lvar) throws GrammarException {

		logger.debug("Analysing view variable "+lvar.name);
		
		String lv_afterpos = null;
		String lv_min=null;
		String lv_max=null;
		

		
		prologSentence+=LogolUtils.addParent(lvar,varID);
		
		//if (Treatment.isAny==-1) {  isBeginConstraint(lvar,false); }
	
		//Fix1397
		//if(lvar.neg==true) { lv_afterpos = LogolUtils.getTemporaryVariable(); }
		//else { lv_afterpos = Constants.LOGOLVARAFTER+varID; }
		
		if(lvar.neg==true) {
			if(!lvar.hasConstraint(Constants.LENGTHCONSTRAINT)) throw new GrammarException("Error, view ("+lvar.fixedValue+") has not size constraint? It is mandatory for negative content constraints!");
			
			StringConstraint lv_lengthConstraint = lvar.getStringConstraint(Constants.LENGTHCONSTRAINT);
			// Set lv_mon and lv_max with length constraint
			lv_min = lv_lengthConstraint.min;
			lv_max = lv_lengthConstraint.max;
			
			
			if (lv_lengthConstraint.neg==true) { throw new GrammarException("Error, using negative constraint on size while using negative content constraint"); }
			//min=getExpressionData(min);
			Expression minexpr = new Expression().getExpressionData(lv_min);
			lv_min= minexpr.variable;
			prologSentence+=minexpr.expression;
			//max=getExpressionData(max);
			Expression maxexpr = new Expression().getExpressionData(lv_max);
			lv_max= maxexpr.variable;
			prologSentence+=maxexpr.expression;	
		}	
					
		if(Treatment.isAny==-1) {
		
			prologSentence += ViewVariable.map2predicate(lvar.fixedValue, varID, null, false, lv_min, lv_max, lv_afterpos);
		
		//Fix 1374 Remove spacer set to 0
		//prologSentence += ","+Constants.LOGOLVARSPACER+varID+"=0";
		}
		else {
		String SpacerPredicate = ViewVariable.map2predicate(lvar.fixedValue, varID, null, false, null, null, null);
		String spacerMin="0";
		String spacerMax= Integer.toString(Treatment.maxSpacerLength);
		
		if(Treatment.saveAny==false) {
			// For "first" variables, we search with infinite gap e.g. max sequence size
			spacerMax = Integer.toString(Treatment.sequenceLength);
		}
		
		// Specify min and max size of spacer to shorten the analysis, if defined.
		if(!Constants.EMPTYSTRING.equals(Treatment.isAnyMin)) { spacerMin = Treatment.isAnyMin; }
		if(!Constants.EMPTYSTRING.equals(Treatment.isAnyMax)) { spacerMax = Treatment.isAnyMax; }
	
		if(lvar.neg==true) {			
			//apply notpred_pos(Id,Pred,Min,Max,Value)
			prologSentence += ",notpred_pos("+varID+","+Constants.LOGOLVARBEFORE+varID+",";
		}
		else {
			prologSentence += ",";
		}
		
		prologSentence += "anySpacer_pos("+Constants.LOGOLVARBEFORE+varID+","+Constants.LOGOLVARAFTER+varID+","+SpacerPredicate+",1,"+spacerMin+","+spacerMax+","+Constants.LOGOLVARSPACER+varID+")";
		
		if(lvar.neg==true) {
			//apply notpred_pos(Id,Pred,Min,Max,Value)
			prologSentence += ","+lv_min+","+lv_max+","+lv_afterpos+")";
		}
		
		}
		
		
		
		
		//prologSentence += ","+LOGOLVARINFO+varID+"=[]";
		// Managed by addParent now prologSentence += ","+Constants.LOGOLVARPARENT+varID+"="+lvar.parentId;
		
		
		applyConstraints(lvar);
		
        // isAny condition in view is forwarded to sub elements, so we do the
        // same in any case
        // As isAny is forwarded, we do not know if we slide on the sequence or
        // search with an index search tool, this
        // prevent us from checking position before gettign a result
        // from the view.

        if (Treatment.isAny==-1) {  isBeginConstraint(lvar,true); }		
		if (Treatment.isAny>-1) { isBeginConstraint(lvar,true); }
		
		isEndConstraint(lvar);
		
		prologSentence += ",saveVariable('"+Constants.LOGOLVAR+varID+"',"+Constants.LOGOLVARBEFORE+varID+","+Constants.LOGOLVARSPACER+varID+","+Constants.LOGOLVARAFTER+varID+","+Constants.LOGOLVARINFO+varID+",'"+lvar.text+"',["+Constants.LOGOLVARERRORS+varID+","+Constants.LOGOLVARINDEL+varID+"],"+lvar.getConfig()+","+Constants.LOGOLVARREF+varID+")\n";
		
		
		prologSentence +=","+Constants.LOGOLVARPARENTREF+varID+"="+Constants.LOGOLVARREF+varID;
		
		isAlphabetConstraint(lvar); 
		
		//Check for postponed variable constraints
		prologSentence+=managePostponedVariables(lvar);
		
		
		// Save previous spacer
		if(Treatment.isAny>-1 && Treatment.saveAny) {
			prologSentence += ",saveVariable('"+Constants.LOGOLVAR+Treatment.isAny+"',"+Constants.LOGOLVARBEFORE+varID+","+Constants.LOGOLVARSPACER+varID+",[],'"+Constants.ANY+"',[0,0],[0,0],"+Constants.LOGOLVARREF+Treatment.isAny+")\n";
		}
		
		Treatment.isAny=-1;
		Treatment.isAnyMin="";
		Treatment.isAnyMax="";				
		Treatment.saveAny=true;

				
		return prologSentence;
		
	}
	
	
	/**
	 * Analyse constraints on variable to generate prolog
	 * @param lvar variable to analyse
	 * @return 0
	 * @throws GrammarException
	 */
	protected int applyConstraints(LogolVariable lvar) throws GrammarException {
		
		StringConstraint sc = null;
		
		String lengthConstraint = "";
		String min="0";
		String max="0";
		
		
		for(int i=0;i<lvar.stringConstraints.size();i++) {
			sc = (StringConstraint)lvar.stringConstraints.get(i);
			
			switch(sc.type) {

				case Constants.SAVECONSTRAINT: {
					LogolVariable.matchedVariables.add(sc.variableContent);
					break;
				}
				
				case Constants.LENGTHCONSTRAINT: {
					if(lvar.neg) { break; }
					
					if(sc.optimal==Constants.OPTIMAL_CONSTRAINT.OPTIMAL_LENGTH) {
						Treatment.setOptimalConstraint(true);
						lvar.setOptimalConstraint(OPTIMAL_CONSTRAINT.OPTIMAL_LENGTH);
					}
					
					
					if(lvar.postpone(Constants.LENGTHCONSTRAINT,sc)) { break;}
					min = sc.min;
					max = sc.max;
					
					
					lengthConstraint += ",wordSize_pos("+Constants.LOGOLVARBEFORE+varID+","+Constants.LOGOLVARAFTER+varID+","+Constants.LOGOLVARSPACER+varID+","+Constants.LOGOLVARSIZE+varID+")";
					lengthConstraint += ",";
					if (sc.neg==true) { lengthConstraint +="\\+ "; }
					//min=getExpressionData(min);
					Expression minexpr = new Expression().getExpressionData(min);
					min= minexpr.variable;
					prologSentence+=minexpr.expression;
					
					lengthConstraint += Constants.LOGOLVARSIZE+varID+">="+min;
					
					lengthConstraint += ",";
					if (sc.neg==true) { lengthConstraint +="\\+ "; }
					//max=getExpressionData(max);
					Expression maxexpr = new Expression().getExpressionData(max);
					max= maxexpr.variable;
					prologSentence+=maxexpr.expression;
					
					lengthConstraint += Constants.LOGOLVARSIZE+varID+"=<"+max;					
					

					break;
				}
				case Constants.CONTENTCONSTRAINT: {
					// Not implemented, non sense on a view
					break;
				}
				default: {
					break;
				}
			
			}
			
		}		
		
		
		StructConstraint stc = null;
		String distance="";
		String cost="";
		String mindistance="";
		String mincost="";
		StructConstraint userSpecificCostFunction=null;
		
		boolean costPostponed=false;
		boolean distPostponed=false;
		
		/**
		 * Contains the id under which temporary variable is stored if required for later used
		 */
		

		boolean percentC = false;
		boolean percentD = false;
		
		// Struct constraints
		for(int i=0;i<lvar.structConstraints.size();i++) {
			stc = (StructConstraint)lvar.structConstraints.get(i);
			switch(stc.type) {
				case Constants.COSTCONSTRAINT: {
					if(distPostponed) {
						lvar.forcepostpone(stc,Constants.DISTANCECONSTRAINT);
						break;
					}
					if(lvar.postpone(Constants.COSTCONSTRAINT,stc)) { costPostponed=true; break;}
					//cost = getExpressionData(stc.max);
					Expression expr = new Expression().getExpressionData(stc.max);
					cost= expr.variable;
					prologSentence+=expr.expression;
					Expression minexpr = new Expression().getExpressionData(stc.min);
					mincost= minexpr.variable;
					prologSentence+=minexpr.expression;
					if(stc.name!=null) {
						userSpecificCostFunction=stc;
					}
					break;
				}
				case Constants.PERCENTCOSTCONSTRAINT: {
					if(distPostponed) {
						lvar.forcepostpone(stc,Constants.DISTANCECONSTRAINT);
						break;
					}
					if(lvar.postpone(Constants.COSTCONSTRAINT,stc)) { costPostponed=true; break;}
					percentC=true;
					//cost = getExpressionData(stc.max);
					Expression expr = new Expression().getExpressionData(stc.max);
					cost= expr.variable;
					prologSentence+=expr.expression;
					Expression minexpr = new Expression().getExpressionData(stc.min);
					mincost= minexpr.variable;
					prologSentence+=minexpr.expression;
					if(stc.name!=null) {
						userSpecificCostFunction=stc;
					}

					break;
				}
				case Constants.DISTANCECONSTRAINT: {
					if(costPostponed) {
						lvar.forcepostpone(stc,Constants.COSTCONSTRAINT);
						break;
					}
					if(lvar.postpone(Constants.DISTANCECONSTRAINT,stc)) { distPostponed=true; break;}
					//distance = getExpressionData(stc.max);
					Expression expr = new Expression().getExpressionData(stc.max);
					distance= expr.variable;
					prologSentence+=expr.expression;
					Expression minexpr = new Expression().getExpressionData(stc.min);
					mindistance= minexpr.variable;
					prologSentence+=minexpr.expression;	
					
					break;
				}
				case Constants.PERCENTDISTANCECONSTRAINT: {
					if(costPostponed) {
						lvar.forcepostpone(stc,Constants.COSTCONSTRAINT);
						break;
					}
					if(lvar.postpone(Constants.DISTANCECONSTRAINT,stc)) { distPostponed=true; break;}
					percentD=true;
					//distance = getExpressionData(stc.max);
					Expression expr = new Expression().getExpressionData(stc.max);
					distance= expr.variable;
					prologSentence+=expr.expression;
					Expression minexpr = new Expression().getExpressionData(stc.min);
					mindistance= minexpr.variable;
					prologSentence+=minexpr.expression;	
					break;
				}				

				case Constants.CONTENTCONSTRAINT: {
					//None
					break;
				}
				default: {
					break;
				}
				
			}
		}		
		

		// checkcost,distance
		if(!Constants.EMPTYSTRING.equals(cost)) { 

			
			// Cost function is a user specific one.
			if(userSpecificCostFunction!=null) {
				String wordSize = LogolUtils.getTemporaryVariable();
				prologSentence += ",wordSize_pos("+Constants.LOGOLVARBEFORE+varID+","+Constants.LOGOLVARAFTER+varID+","+Constants.LOGOLVARSPACER+varID+","+wordSize+")";
				
				String wordContent = LogolUtils.getTemporaryVariable();
				prologSentence+=",getContent("+Constants.LOGOLVARBEFORE+varID+","+wordSize+","+Constants.LOGOLVARSPACER+varID+","+wordContent+")";
				
				
				if(percentC){
					String tmp_cost = LogolUtils.getTemporaryVariable();
					prologSentence+=",percent2int("+wordContent+","+tmp_cost+","+cost+")";
					cost=tmp_cost;	
				}			
				// Do not get errors from function. View is already a combination of internal variable errors, ERRORS parameter is already set with usual calculations.
				prologSentence+=",myCost('"+userSpecificCostFunction.name+"',"+wordContent+","+cost+",_)";
			}
			else {
				
				if(percentC) {
					String wordSize = LogolUtils.getTemporaryVariable();
					prologSentence += ",wordSize_pos("+Constants.LOGOLVARBEFORE+varID+","+Constants.LOGOLVARAFTER+varID+","+Constants.LOGOLVARSPACER+varID+","+wordSize+")";
					String percentCost=LogolUtils.getTemporaryVariable();
					prologSentence += ","+percentCost+"is ("+cost+"*"+wordSize+"/100)";
					cost=percentCost;
					String minpercentCost=LogolUtils.getTemporaryVariable();
					prologSentence += ","+minpercentCost+"is ("+mincost+"*"+wordSize+"/100)";
					mincost=minpercentCost;
				}				
				// check high constraint
				prologSentence+=","+Constants.LOGOLVARERRORS+varID+"=<"+cost;
				// check low constraint
				prologSentence+=","+Constants.LOGOLVARERRORS+varID+">="+mincost;
			}
		}
		
		//checkCost([X|Y],CurPos,Matrix,Cost,Min,Max,Z)
		if(!Constants.EMPTYSTRING.equals(distance)) { 
			if(percentD) {
				String wordSize = LogolUtils.getTemporaryVariable();
				prologSentence += ",wordSize_pos("+Constants.LOGOLVARBEFORE+varID+","+Constants.LOGOLVARAFTER+varID+","+Constants.LOGOLVARSPACER+varID+","+wordSize+")";
				String percentDistance=LogolUtils.getTemporaryVariable();
				prologSentence += ","+percentDistance+"is ("+distance+"*"+wordSize+"/100)";
				distance=percentDistance;
				String minpercentDistance=LogolUtils.getTemporaryVariable();
				prologSentence += ","+minpercentDistance+"is ("+mindistance+"*"+wordSize+"/100)";
				mindistance=minpercentDistance;
			}
			prologSentence+=","+Constants.LOGOLVARINDEL+varID+"=<"+distance;
			prologSentence+=","+Constants.LOGOLVARINDEL+varID+">="+mindistance;
		}
		
		prologSentence+=lengthConstraint;
		
		
		//prologSentence+=managePostponedVariables(lvar);
		
		return 0;
	}
	
	
	
	
	/**
	 * Same as map2predicate(String predicate,int id, String plist,boolean overlap) with overlap set to false and no postponed list
	 * @param predicate progol sentence to map to an internal predicate
	 * @param id identifier of the current variable
	 * @return prolog call to predicate
	 */
	public static String map2predicate(String predicate,int id) {
		return map2predicate(predicate,id, null);
	}
	
	/**
	 * Same as map2predicate(String predicate,int id, String plist,boolean overlap) with overlap set to false
	 * @param predicate progol sentence to map to an internal predicate
	 * @param id identifier of the current variable
	 * @param plist List of postponed variables
	 * @return prolog call to predicate
	 */
	public static String map2predicate(String predicate,int id, String plist) {
		return map2predicate(predicate,id, plist,false);
	}
	
	/**
	 * From a predicate or a list of predicate, create a new predicate and add it to a list of predicates.
	 * Returns a call to this newly created predicate.
	 * @param predicate progol sentence to map to an internal predicate
	 * @param id identifier of the current variable
	 * @param plist List of postponed variables
	 * @param overlap specific tag for management of overlaps
	 * @return prolog predicate call 
	 */
	public static String map2predicate(String predicate,int id, String plist,boolean overlap) {
		return map2predicate(predicate,id,plist,overlap,null,null,null);
	}
	
	/**
	 * From a predicate or a list of predicate, create a new predicate and add it to a list of predicates.
	 * Returns a call to this newly created predicate.
	 * @param predicate progol sentence to map to an internal predicate
	 * @param id identifier of the current variable
	 * @param plist List of postponed variables
	 * @param overlap specific tag for management of overlaps
	 * @param min size to use in cas of negative constraint, null if positive constraint
	 * @param max size to use in cas of negative constraint, null if positive constraint
	 * @param afterpos Variable name to use to store output position. If null, use standard name.
	 * @return prolog predicate call 
	 */
	public static String map2predicate(String predicate,int id, String plist,boolean overlap,String min,String max,String afterpos) {
		

		String postponedList = null;
		
		String pred = Constants.LOGOLVARPRED+predCount;
		// Predicate that will be called from main
		String callPred = pred;
		
		String postponedVars="";
		
		String refVariable=Constants.EMPTYSTRING;
		
		predCount++;
	
		// Search for variables used
		HashSet<String> vars = LogolUtils.getAllUsedVariables(predicate);
		String before = Constants.LOGOLVARAFTER+(Integer.parseInt(LogolUtils.getPredicateInput(predicate))-1);
		String after = Constants.LOGOLVARAFTER+LogolUtils.getPredicateOutput(predicate);
		//Fix 1374 Add spacer as return var
		String spacer = Constants.LOGOLVARSPACER+LogolUtils.getPredicateInput(predicate);
		
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
		// get sequencelength 'cause could be used in predicate
		//Fix 1374 Add spacer as return var
		pred+=",Parent,["+Constants.LOGOLVARERRORS+id+","+Constants.LOGOLVARINDEL+id+"],"+Constants.LOGOLVARINFO+id+","+spacer+","+after+") :- ("+predicate+")";
		
		if(min!=null&&max!=null) {
			callPred+=","+Constants.LOGOLVARPARENT+id+",[_,_],_,_,_)";
		}
		else {
			callPred+=","+Constants.LOGOLVARPARENT+id+",["+Constants.LOGOLVARERRORS+id+","+Constants.LOGOLVARINDEL+id+"],"+Constants.LOGOLVARINFO+id+","+Constants.LOGOLVARSPACER+id+","+Constants.LOGOLVARAFTER+id+")";			
		}
		
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
		
		if(min!=null&&max!=null) {
				pred += ","+Constants.LOGOLVARINDEL+id+"=0,"+Constants.LOGOLVARERRORS+id+"=0.";
		}
		else {
				pred += ",computeCost("+list+",Cost),"+"computeCost("+listindel+",Indel),"+Constants.LOGOLVARINDEL+id+"=Indel,"+Constants.LOGOLVARERRORS+id+"=Cost.";			
		}
		
		predicates.add(pred);
		
		if(min!=null&&max!=null) {			
			//apply notpred_pos(Id,Pred,Min,Max,afterpos)
			if(afterpos==null) { afterpos =Constants.LOGOLVARAFTER+id; }			
			callPred = "notpred_pos("+id+","+Constants.LOGOLVARBEFORE+id+","+ callPred+","+min+","+max+","+afterpos+")";
			callPred += ","+Constants.LOGOLVARERRORS+id+"=0"+","+Constants.LOGOLVARINDEL+id+"=0";
			callPred += ","+Constants.LOGOLVARINFO+id+"=[]";
			callPred += ","+Constants.LOGOLVARSPACER+id+"=0";
			}
		
		
		if(plist==null) {
			callPred = ","+postponedVars+","+callPred;
		}
		
		
		return callPred;
	}

	public static int getPredCount() {
		int count = predCount;
		predCount++;
		return count;
	}

	public static void setPredCount(int predCount) {
		ViewVariable.predCount = predCount;
	}
	
	
}
