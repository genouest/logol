package org.irisa.genouest.logol.types;

import java.util.Vector;

import org.apache.log4j.Logger;
import org.irisa.genouest.logol.Constants;
import org.irisa.genouest.logol.Expression;
import org.irisa.genouest.logol.GrammarException;
import org.irisa.genouest.logol.Interval;
import org.irisa.genouest.logol.LogolVariable;
import org.irisa.genouest.logol.Modifier;
import org.irisa.genouest.logol.StringConstraint;
import org.irisa.genouest.logol.StructConstraint;
import org.irisa.genouest.logol.Treatment;
import org.irisa.genouest.logol.VariableId;
import org.irisa.genouest.logol.utils.LogolUtils;

/**
 * 
 * @author osallou
 * History:
 * 01/03/10 @FIX 1578
 * 12/10/10 @FIX 1683
 * 25/05/10 @FIX 1800
 * 11/10/13 @FIX 2243
 */
public abstract class AbstractVariable {

	
	private static final Logger logger = Logger.getLogger(org.irisa.genouest.logol.types.AbstractVariable.class);
	
	int varID = -1;
	String prologSentence="";
	
	public AbstractVariable(int id) {
		varID = id;		
	}
	
	
	public AbstractVariable() {	
	}
	
	/**
	 * Return prolog sentence content
	 * @return a prolog content
	 */
	public String getProlog() {
		return prologSentence;
	}
	
	/**
	 * Check if a begin position constraint is applied on this variable
	 * @param lvar	the current variable
	 * @param useSpacer decide to use current variable spacers to calculate position. Can be done
	 * only if begin constraint is check after variable match.
	 * @return	begin position management string in prolog
	 * @throws GrammarException 
	 */
	@SuppressWarnings("unchecked")
	protected int isBeginConstraint(LogolVariable lvar,boolean useSpacer) throws GrammarException {
		String beginConstraint="";			
		
		
		Vector<StringConstraint> constraints = lvar.stringConstraints;
		for(int i=0;i<constraints.size();i++) {
			StringConstraint constr = (StringConstraint) constraints.get(i);
			if(constr.type==Constants.BEGINCONSTRAINT) {
				
				if(lvar.postpone(Constants.BEGINCONSTRAINT,constr)) {break; }
				
				/* get position constraint and set in return sentence the prolog constraint
				 * checkPosition(WholeListSize,ListWordMatch, MinCondition,MaxCondition,Spacer)
				 */
				String min = constr.min;
				String max = constr.max;
				beginConstraint += ",";
				if (constr.neg==true) { beginConstraint +="\\+ "; }
				beginConstraint += " checkPosition_pos("+Constants.LOGOLVARBEFORE+varID;
				

				Expression expr = new Expression().getExpressionData(min);
				beginConstraint +=","+expr.variable;
				prologSentence+=expr.expression;
				
				

				expr = new Expression().getExpressionData(max);
				beginConstraint +=","+expr.variable;
				prologSentence+=expr.expression;
				
				if(useSpacer) {
					beginConstraint+= ","+Constants.LOGOLVARSPACER+varID+")";
				}
				else {
					beginConstraint+= ",0)";
				}
			}			
		}	
		prologSentence+=beginConstraint;
		return 0;
	}
	
	
	/**
	 * checks that found match has a min percentage of a defined alphabet
	 * Fix 1683
	 * @param lvar the current variable
	 * @return status code
	 */
	protected int isAlphabetConstraint(LogolVariable lvar) {
		Vector<StructConstraint> constraints = lvar.structConstraints;
		

		
		for(int i=0;i<constraints.size();i++) {
			StructConstraint constr = (StructConstraint) constraints.get(i);
			if(constr.type==Constants.ALPHABETCONSTRAINT) {
				String alphabet = LogolUtils.getArray(constr.alphabetConstraint);
				prologSentence+= ",checkAlphabetPercentage("+Constants.LOGOLVARREF+varID+","+alphabet+","+constr.min+")";						
				break;
			}
		}
			
		return 0;
	}
	
	/**
	 * Check if an end position constraint is applied on this variable
	 * @param lvar	the current variable
	 * @return	status code
	 * @throws GrammarException 
	 */
	@SuppressWarnings("unchecked")
	protected int isEndConstraint(LogolVariable lvar) throws GrammarException {
		String endConstraint="";
		
		Vector<StringConstraint> constraints = lvar.stringConstraints;
		for(int i=0;i<constraints.size();i++) {
			StringConstraint constr = (StringConstraint) constraints.get(i);
			if(constr.type==Constants.ENDCONSTRAINT) {
								
				if(lvar.postpone(Constants.ENDCONSTRAINT,constr)) {break; }
				
				/* get position constraint and set in return sentence the prolog constraint
				 * checkPosition(WholeListSize,ListWordMatch, MinCondition,MaxCondition,Spacer)
				 */
				
				
				String min = constr.min;
				String max = constr.max;
				endConstraint += ",";
				if (constr.neg==true) { endConstraint +="\\+ "; }
				endConstraint += " checkPosition_pos("+Constants.LOGOLVARAFTER+varID;

				Expression expr = new Expression().getExpressionData(min);
				endConstraint+= "," + expr.variable;
				prologSentence+=expr.expression;
				expr = new Expression().getExpressionData(max);
				endConstraint+= "," + expr.variable;
				prologSentence+=expr.expression;
				
				// no spacer after match
				endConstraint+= ",0)";
				
				

			}			
		}		
		prologSentence+=endConstraint;
		return 0;
	}
	
	
	
	/**
	 * Manage the constraints for all variables constrained by current variable
	 * @param lvar
	 * @return all the predicates to call with appropriate parameters
	 * @throws GrammarException 
	 */
	protected String managePostponedVariables(LogolVariable lvar) throws GrammarException {
		
		LogolVariable var =null;
		String constraints="";
		Vector<LogolVariable> lvars = new Vector<LogolVariable>();
		for(int i=0;i<LogolVariable.postponedVariables.size();i++){
			var = (LogolVariable) LogolVariable.postponedVariables.get(i);
			// If postpone treatment is related to current variable
			//System.out.println("#DEBUG varid = "+var.id+", ref = "+var.referenceID+", cur id = "+lvar.id);
			if(var.referenceID.equals(String.valueOf(lvar.id))) { lvars.add(var); }
		}
		if(lvars.size()!=0) { constraints+=managePostponedVariable(lvars,lvar); }
		return constraints;
	}

	/**
	 * Manage all constraints for current variable when postponed
	 * @param lvars
	 * @return predicate to call with appropriate parameters
	 * @throws GrammarException 
	 */
	private String managePostponedVariable(Vector<LogolVariable> lvars, LogolVariable currentlvar) throws GrammarException {
		
		int constrainedType=-1;
		// If cost/distance constrained, content will also be constrained, need to know the reference
		// of original variable data.
		//int contentReference=-1;
		String contentReference = null;
		
		boolean isCostConstrained =false;
		int costVar=-1;
		boolean isDistanceConstrained =false;
		int distanceVar=-1;
		boolean isContentConstrained =false;
		int contentVar=-1;
		boolean isBeginConstrained =false;
		int beginVar=-1;
		boolean isEndConstrained =false;
		int endVar=-1;
		boolean isLengthConstrained =false;
		int lengthVar=-1;
		boolean isRepeatConstrained =false;
		int repeatVar=-1;		
		

		
		String constraint="";
		int tmpVarId=-1;
		StringConstraint constr=null;
		
		String motif="";
		String prevmatch="";
		
		boolean percent=false;
		
		LogolVariable lvar=null;
		
		for(int i=0;i<lvars.size();i++) {
			lvar = (LogolVariable) lvars.get(i);
			if(lvar.hasConstraint(Constants.CONTENTCONSTRAINT)) { isContentConstrained=true; contentVar=i; }
			if(lvar.hasConstraint(Constants.BEGINCONSTRAINT)) { isBeginConstrained=true; beginVar=i; }
			if(lvar.hasConstraint(Constants.ENDCONSTRAINT)) { isEndConstrained=true; endVar=i; }
			if(lvar.hasConstraint(Constants.LENGTHCONSTRAINT)) { isLengthConstrained=true; lengthVar=i; }
			if(lvar.hasConstraint(Constants.COSTCONSTRAINT) || lvar.hasConstraint(Constants.PERCENTCOSTCONSTRAINT)) { isCostConstrained=true; costVar=i; }
			if(lvar.hasConstraint(Constants.DISTANCECONSTRAINT) || lvar.hasConstraint(Constants.PERCENTDISTANCECONSTRAINT)) { isDistanceConstrained=true; distanceVar=i; }
			if(lvar.hasConstraint(Constants.REPEATCONSTRAINT)) { isRepeatConstrained=true; repeatVar=i; }
		}
		
		if(isLengthConstrained) {
			constrainedType = Constants.LENGTHCONSTRAINT;
			constraint+=",(";
			lvar = (LogolVariable) lvars.get(lengthVar);
			//contentReference = Integer.valueOf(lvar.referenceID);
			contentReference = lvar.referenceID;
			logger.debug("now manage length constraint of "+lvar.model+":"+lvar.id+" at variable "+currentlvar.model+":"+currentlvar.id);
			String word = LogolUtils.getTemporaryVariable();
			constraint+= "getVariable("+Constants.LOGOLVARREF+lvar.id+",_,_,_,"+word+",_,_,_,_)";
			
		
			constr = lvar.getStringConstraint(Constants.LENGTHCONSTRAINT);	
			
			String min = constr.min;			
			String max = constr.max;
			
			Expression exp = new Expression().getExpressionData(min);
			min = exp.variable;
			if(exp.expression!=null) {
				constraint+=exp.expression;
			}
			exp = new Expression().getExpressionData(max);
			max = exp.variable;
			if(exp.expression!=null) {
				constraint+=exp.expression;
			}
			
			if(lvar.getOptimalConstraint()==Constants.OPTIMAL_CONSTRAINT.OPTIMAL_LENGTH) {
				Treatment.setOptimalConstraint(true);
			}
			
			constraint += ",";
			if (constr.neg==true) { constraint +="\\+ ("; }
			constraint += word+">="+min;
			constraint += ","+word+"=<"+max;
			
			
			// no spacer after match
			constraint+= ")";
			constraint+=")";
			
		}
		
		
		//getVariable(R,Content,Begin,End,Size,Info,Level,Errors,Name)				
		if(isBeginConstrained || isEndConstrained) {
			
			String t_position =LogolUtils.getTemporaryVariable();
			
			constraint+=",(";
			if(isBeginConstrained) {
				constrainedType = Constants.BEGINCONSTRAINT;
				lvar = (LogolVariable) lvars.get(beginVar);
				//contentReference = Integer.valueOf(lvar.referenceID);
				contentReference = lvar.referenceID;
				logger.debug("now manage begin constraint of "+lvar.model+":"+lvar.id+" at variable "+currentlvar.model+":"+currentlvar.id);
				constraint+= "getVariable("+Constants.LOGOLVARREF+lvar.id+",_,"+t_position+",_,_,_,_,_,_)";
				constr = lvar.getStringConstraint(Constants.BEGINCONSTRAINT);
				}
			else { 
				constrainedType = Constants.ENDCONSTRAINT;
				lvar = (LogolVariable) lvars.get(endVar);
				//contentReference = Integer.valueOf(lvar.referenceID);
				contentReference = lvar.referenceID;
				logger.debug("now manage end constraint of "+lvar.model+":"+lvar.id+" at variable "+currentlvar.model+":"+currentlvar.id);
				constraint+= "getVariable("+Constants.LOGOLVARREF+lvar.id+",_,_,"+t_position+",_,_,_,_,_)";
				constr = lvar.getStringConstraint(Constants.ENDCONSTRAINT);
				}	
			
			String min = constr.min;
			String max = constr.max;
			
			
			Expression exprMin = new Expression().getExpressionData(min);
			
			constraint+=exprMin.expression;
			Expression exprMax = new Expression().getExpressionData(max);
			
			constraint+=exprMax.expression;
			
			constraint += ",";
			
			if (constr.neg==true) { constraint +="\\+ "; }	
			// Take begin or end value (without offset) and compare
			constraint += " checkPosition("+t_position;

			constraint+=","+ exprMin.variable;
			
			constraint+=","+ exprMax.variable;
			

			
			// no spacer after match
			constraint+= ")";
			constraint+=")";
			
		}
		
		if(isRepeatConstrained) {
			constraint+=",(";
			// get INFO, compare number of elements
			String infoData = LogolUtils.getTemporaryVariable();
			constraint+= "getVariable("+Constants.LOGOLVARREF+lvar.id+",_,_,_,_,"+infoData+",_,_,_)";			
			lvar = (LogolVariable) lvars.get(repeatVar);
			//contentReference = Integer.valueOf(lvar.referenceID);
			contentReference = lvar.referenceID;
			Interval interval = new Interval(lvar.repeatQuantity);
			Expression minExpr = new Expression().getExpressionData(interval.x);
			Expression maxExpr = new Expression().getExpressionData(interval.y);
			if(!minExpr.expression.equals(Constants.EMPTYSTRING)) {
				constraint+=minExpr.expression;
			}
			if(!maxExpr.expression.equals(Constants.EMPTYSTRING)) {
				constraint+=maxExpr.expression;
			}
			String repeatQuantity = LogolUtils.getTemporaryVariable();		
			constraint+=",length("+infoData+","+repeatQuantity+")";
			constraint+=","+repeatQuantity+">="+minExpr.variable;
			constraint+=","+repeatQuantity+"=<"+maxExpr.variable;
			constraint+=")";
		}
		
		// If there is a constraint on content (and possibly cost/distance)
		if(isContentConstrained) {
			// If there is a cost/distance constraint, there will be whatever a content constraint
			constrainedType = Constants.CONTENTCONSTRAINT;
			constraint+=",(";
			//int reference=-1;
			String reference=null;
			
			lvar = (LogolVariable) lvars.get(contentVar);
			prevmatch = LogolUtils.getTemporaryVariable();
			logger.debug("now manage content constraint of "+lvar.model+":"+lvar.id+" at variable "+currentlvar.model+":"+currentlvar.id);			
			// Get data saved at variable time analysis
			constraint+= "getVariable("+Constants.LOGOLVARREF+lvar.id+","+prevmatch+",_,_,_,_,_,_,_)";
			
			// Get content constraint
			constr = lvar.getStringConstraint(Constants.CONTENTCONSTRAINT);
			String name = null;
			// It was a variable content e.g. ?SX
			if(constr.variableContent!=null) {
			name = (String) LogolVariable.userVariables.get(new VariableId(constr.variableContent,currentlvar.model));
			// Not a saved variable, is it a parameter?
			if(name==null) { name = (String) LogolVariable.paramVariables.get(new VariableId(constr.variableContent,currentlvar.model)); }
			if(name!=null) {
			//reference = Integer.parseInt(name);
			reference = name;
			
			contentReference = reference;
			logger.debug("reference of constraint = "+reference+", current is "+currentlvar.id);
			}
			// Get referenced variable content
			motif = LogolUtils.getTemporaryVariable();
			constraint+= ",getVariable("+Constants.LOGOLVARREF+reference+","+motif+",_,_,_,_,_,_,_)";
					
			}
			// it was a fixed content e.g. "acgt"
			if(constr.contentConstraint!=null) {
				motif= LogolUtils.getArray(constr.contentConstraint);
				//contentReference = Integer.valueOf(lvar.referenceID);
				contentReference = lvar.referenceID;
			}
			
			
			// If modifier present, apply it on content
			if(lvar.modifier!=null)	{	
				
				Expression lv_modExpr = applyModifier(lvar,motif); 
				motif = lv_modExpr.variable;
				constraint += ","+lv_modExpr.expression;

				}
			// Now test if motif is equal depending on cost/distance constraints
			if(!isCostConstrained && !isDistanceConstrained) {
				//apply constraints  isexact apply motif on prevmatch
				// As it is a full exact match, there must be no remaining list as output
				constraint+= ",isexact("+prevmatch+","+motif+",_,FullExact),FullExact=[]";
			}
			if(isCostConstrained && !isDistanceConstrained) {
				// FIXME other variables cannot refer to cost because it is not saved (not yet tested). Could update content and used after....
				LogolVariable tmpVar = (LogolVariable) lvars.get(costVar);
				StructConstraint stc = tmpVar.getStructConstraint(Constants.COSTCONSTRAINT);
				if(stc == null) { stc = tmpVar.getStructConstraint(Constants.PERCENTCOSTCONSTRAINT); percent=true; }
				//String maxCost =getExpressionData(stc.max);
				Expression expr = new Expression().getExpressionData(stc.max);
				String maxCost =  expr.variable;
				constraint+=expr.expression;
				if(percent) {
					String tmp_maxCost = LogolUtils.getTemporaryVariable();
					constraint+=",percent2int("+motif+","+tmp_maxCost+","+maxCost+")";
					maxCost=tmp_maxCost;
					}

				constraint += ",isexactwitherroronly("+prevmatch+","+motif+","+ maxCost+",_,FullExact),FullExact=[]";
				
				
			}
			if(isDistanceConstrained && !isCostConstrained) {				
				// FIXME other variables cannot refer to distance because it is not saved (not yet tested). Could update content and used after....
				LogolVariable tmpVar = (LogolVariable) lvars.get(distanceVar);
				StructConstraint stc = tmpVar.getStructConstraint(Constants.DISTANCECONSTRAINT);
				if(stc == null) { stc = tmpVar.getStructConstraint(Constants.PERCENTDISTANCECONSTRAINT); percent=true; }
				//String maxDistance =getExpressionData(stc.max);
				Expression expr = new Expression().getExpressionData(stc.max);
				String maxDistance = expr.variable;
				constraint+=expr.expression;
				if(percent) {
					String tmp_maxDistance = LogolUtils.getTemporaryVariable();
					constraint+=",percent2int("+motif+","+tmp_maxDistance+","+maxDistance+")";
					maxDistance=tmp_maxDistance;
					}
				//constraint += "isexactwithgapanderror("+prevmatch+","+motif+","+ maxDistance+",Errors,Queue),length(Queue,QueueLength),RemainingErrors is "+maxDistance+" - Errors,QueueLength=<RemainingErrors";
				constraint += ",isexactwithgaponly("+prevmatch+","+motif+","+ maxDistance+",Errors,Queue),length(Queue,QueueLength),RemainingErrors is "+maxDistance+" - Errors,QueueLength=<RemainingErrors";
			}
			if(isDistanceConstrained && isCostConstrained) {				
				// FIXME other variables cannot refer to distance because it is not saved (not yet tested). Could update content and used after....
				LogolVariable tmpVar = (LogolVariable) lvars.get(distanceVar);
				StructConstraint stc = tmpVar.getStructConstraint(Constants.DISTANCECONSTRAINT);
				if(stc == null) { stc = tmpVar.getStructConstraint(Constants.PERCENTDISTANCECONSTRAINT); percent=true; }
				//String maxDistance =getExpressionData(stc.max);
				Expression expr = new Expression().getExpressionData(stc.max);
				String maxDistance = expr.variable;
				constraint+=expr.expression;
				if(percent) {
					String tmp_maxDistance = LogolUtils.getTemporaryVariable();
					constraint+=",percent2int("+motif+","+tmp_maxDistance+","+maxDistance+")";
					maxDistance=tmp_maxDistance;
					}
				StructConstraint stc2 = tmpVar.getStructConstraint(Constants.COSTCONSTRAINT);
				if(stc2 == null) { stc2 = tmpVar.getStructConstraint(Constants.PERCENTCOSTCONSTRAINT); percent=true; }
				//String maxDistance =getExpressionData(stc.max);
				Expression expr2 = new Expression().getExpressionData(stc2.max);
				String maxCost = expr.variable;
				prologSentence+=expr.expression;
				if(percent) {
					String tmp_maxCost = LogolUtils.getTemporaryVariable();
					constraint+=",percent2int("+motif+","+tmp_maxCost+","+maxCost+")";
					maxCost=tmp_maxCost;
					}				
				//isexactwithdistinctgapanderror([X|Y],[E1 | E2], N, NDist, MaxError, MaxDistance, Errors, DistanceErrors, Z)
				constraint+=",isexactwithdistinctgapanderror("+prevmatch+","+motif+", "+maxCost+", "+maxDistance+", Errors, DistanceErrors, Queue),length(Queue,QueueLength),RemainingErrors is "+maxDistance+" - (Errors + DistanceErrors),QueueLength=<RemainingErrors";
			}

			constraint+=")";
			
		}
		else {
		if(isCostConstrained) {
			/* Cost contrainted but not content constrained, means it is a user specific cost function.
			 * We only want to look at previous match and apply specific function based on cost parameter.
			 * 
			 */
			// If there is a cost/distance constraint, there will be whatever a content constraint
			
			constraint+=",(";
			//int reference=-1;
			String reference=null;
			lvar = (LogolVariable) lvars.get(costVar);
			//contentReference = Integer.valueOf(lvar.referenceID);
			contentReference = lvar.referenceID;
			logger.debug("now manage cost constraint of "+lvar.model+":"+lvar.id+" at variable "+currentlvar.model+":"+currentlvar.id);			
			// Get data saved at variable time analysis
			prevmatch = LogolUtils.getTemporaryVariable();
			constraint+= "getVariable("+Constants.LOGOLVARREF+lvar.id+","+prevmatch+",_,_,_,_,_,_,_)";
			

				StructConstraint stc = lvar.getStructConstraint(Constants.COSTCONSTRAINT);
				if(stc == null) { stc = lvar.getStructConstraint(Constants.PERCENTCOSTCONSTRAINT); percent=true; }
				
				Expression expr = new Expression().getExpressionData(stc.max);
				String maxCost =  expr.variable;
				prologSentence+=expr.expression;
				if(percent) {
					String tmp_maxCost = LogolUtils.getTemporaryVariable();
					constraint+=",percent2int("+motif+","+tmp_maxCost+","+maxCost+")";
					maxCost=tmp_maxCost;
					}
				
				// User defined cost function, no comparison with previous match, just check sequence.
				constraint += ",myCost('"+stc.name+"',"+prevmatch+","+maxCost+",_)";
	
				constraint+=")";
		}
			
			
		}
		
		
		// This is a parent content constraint
		/* FIXME parent content constraint NOT SUPPORTED FOR THE MOMENT
		if(!isContentConstrained && isCostConstrained) {
			// This is a X:[0,#Y] like constraint  e.g. cost constraint is on parent
			//  If not defined, get parentalcost on data, else compare with existing
			String cost="";
			int currentId;
			int resultId;
			int parentId;
			int reference=-1;
	
			lvar = (LogolVariable) lvars.get(costVar);
			StructConstraint stc = lvar.getStructConstraint(COSTCONSTRAINT);
			if(stc == null) { stc = lvar.getStructConstraint(PERCENTCOSTCONSTRAINT); percent=true; }
			String maxCost =getExpressionData(stc.max);			
			
			String name = (String) LogolVariable.parentVariables.get(new VariableId(lvar.name,lvar.model));
			if(name!=null) {
			reference = Integer.parseInt(name);
			parentRef=reference;
			}
			
			
			// If parent not yet known
			constraint+= "((\\+var("+LOGOLVARPARENTREF+reference+")";
			
			constraint+= "getVariableByRef("+LOGOLVARREF+lvar.id+","+LOGOLVARTMP+varCounter+",_,_,_,_,_,_,_)";
			currentId=varCounter;			
			varCounter++;
			constraint+= "getVariableByRef("+LOGOLVARPARENTREF+reference+","+LOGOLVARTMP+varCounter+",_,_,_,_,_,_,_)";
			parentId=varCounter;			
			varCounter++;			
			if(percent) {
				constraint+=",percent2int("+LOGOLVARTMP+currentId+","+LOGOLVARTMP+varCounter+","+maxCost+")";
				maxCost=LOGOLVARTMP+varCounter;
				varCounter++;
				}
			constraint += "isexactwitherroronly("+LOGOLVARTMP+currentId+","+LOGOLVARTMP+parentId+","+ maxCost+",_,_)";			
			
			
			constraint+=")";
			// If parent is still unknown, save all variations
			constraint+= ";(var("+LOGOLVARPARENTREF+lvar.id+")";
	
			constraint+= ",getVariableByRef("+LOGOLVARREF+lvar.id+","+LOGOLVARTMP+varCounter+",_,_,_,_,_,_,_)";
			currentId=varCounter;
			varCounter++;
					
			if(percent){
				constraint+=",percent2int("+LOGOLVARTMP+","+LOGOLVARTMP+currentId+","+maxCost+")";
				maxCost=LOGOLVARTMP+varCounter;
				varCounter++;	
				}															
			
			constraint +=",parentalCost(substitution,"+LOGOLVARTMP + currentId+","+ LOGOLVARTMP + varCounter+","+maxCost+")";
			resultId = varCounter;
			varCounter++;
			constraint +=",assert(varDefinition("+LOGOLVARPARENT+lvar.id+","+LOGOLVARTMP+resultId+",_,_,_, _, _, _),"+LOGOLVARPARENTREF+lvar.id+")";
			
			constraint+="))";
			
			
		}
		*/
		
		String postponedVariables = LogolVariable.getPostponedVariableList(currentlvar.model);
		
		//create a predicate and call it.
		// Get all postponed variables for this model
		//String callPredicate = ",append(PostponedVariables,"+postponedVariables+","+Constants.LOGOLVARTMP+varCounter+"),"+Constants.LOGOLVARPOSTPONEPRED+predCount+"("+Constants.LOGOLVARTMP+varCounter+","+Constants.LOGOLVARREF+lvar.id;
		String t_postponedvar = LogolUtils.getTemporaryVariable();
		String t_postponedPred = LogolUtils.getPostponedPredicateVariable();
		String callPredicate = ",append(PostponedVariables,"+postponedVariables+","+t_postponedvar+"),"+t_postponedPred+"("+t_postponedvar;
		
		//String predicate = Constants.LOGOLVARPOSTPONEPRED+predCount+"(PostponedVariables,"+Constants.LOGOLVARREF+lvar.id;
		String predicate = t_postponedPred+"(PostponedVariables";
		
		predicate+="):- is4me(PostponedVariables,"+lvar.id+","+constrainedType+","+Constants.LOGOLVARREF+lvar.id+","+Constants.LOGOLVARREF+contentReference+")"+constraint+".";
		
		callPredicate+=")";
		ViewVariable.predicates.add(predicate);
		
		return callPredicate;
	}		
	
	
	
	
	
	/**
	 * Apply a modifier on variable
	 * @param lvar LogolVariable containing the modifier information
	 * @param motif string to transform (fixed or from a previous variable)
	 * @return	Expression with variable reference to use after transformation, and required expressions to set before
	 */
	protected Expression applyModifier(LogolVariable lvar,String motif) {		
		Modifier mod = lvar.modifier;
		Expression expr = new Expression();
		String lv_sentence = "";
		String newmotif="";
		
		String modifierName = mod.modifierName.replaceAll("\"", "'");
		if(modifierName.equals("'wc'")) {
			//If rna or protein, apply different predefined morphism
			if(Treatment.dataType==Constants.RNA) { modifierName = "'wcrna'"; logger.debug("Apply wc on rna");}
			if(Treatment.dataType==Constants.PROTEIN) { modifierName = "'wcprot'"; logger.debug("Apply wc on protein"); }
			if(Treatment.dataType==Constants.DNA) { modifierName = "'wcdna'"; logger.debug("Apply wc on dna"); }
		}
		
		if(modifierName.equals("'reverse'")) {
			// This is a reverse only, sign is not taken into account
			newmotif = LogolUtils.getTemporaryVariable();
			lv_sentence+="reverse("+motif+","+newmotif+")";
			
			
			expr.variable=newmotif;
			expr.expression=lv_sentence;
			
			return expr;
		}
		
		
		newmotif = LogolUtils.getTemporaryVariable();
		//System.out.println("#DEBUG: apply modifier "+modifierName+" with operator "+mod.type);
		if(mod.type==Modifier.MODIFIERMORPHMINUS) {			
			lv_sentence+="applymorphism("+motif+","+modifierName+",1,"+newmotif+")";				
		}
		if(mod.type==Modifier.MODIFIERMORPHPLUS) {
			lv_sentence+="applymorphism("+motif+","+modifierName+",0,"+newmotif+")";	
		}
		
		expr.variable=newmotif;
		expr.expression=lv_sentence;
		
		return expr;
	}
	
	
	/**
	 * Evaluates prolog to create when some content constraints are postponed for current variable
	 * @param model current model
	 * @param inMin minimum length
	 * @param inMax maximum length
	 */	
	protected void manageContentPostponed(String model, String inMin, String inMax) {
		
		String min = inMin;
		String max = inMax;
		
		// check if constraining var is fixed content or has length constraint
		// REMARK Issue:   (X:#3:_SX | X:#8:_SX)  both have same var id but different constraints, cannot manage content
		// count number of ref, if one only take data, if other, do not take into account
		LogolVariable constrainedVar = LogolVariable.varData.get(String.valueOf(varID));
		VariableId var = LogolVariable.constrainedVariables.get(new VariableId(model,String.valueOf(varID),String.valueOf(Constants.CONTENTCONSTRAINT)));		
		LogolVariable constrainingVar = LogolVariable.varData.get(var.name);
		
		// If no length constraint, try to find some other constraints that could impact length
		if(min.equals(String.valueOf(Treatment.minLength))&& max.equals(String.valueOf(Treatment.maxLength))) {
			// constraining var is defined by a fixed content
			if(constrainingVar.fixedValue!=null) {
		
					// If current var has a distance constraint, need to take it into account to define max length
					if(constrainingVar.hasConstraint(Constants.DISTANCECONSTRAINT) || constrainingVar.hasConstraint(Constants.PERCENTDISTANCECONSTRAINT) || constrainedVar.hasConstraint(Constants.DISTANCECONSTRAINT) || constrainedVar.hasConstraint(Constants.PERCENTDISTANCECONSTRAINT)) 
					{
						StructConstraint str = constrainedVar.getStructConstraint(Constants.DISTANCECONSTRAINT);
						if(str==null) {
							str = constrainedVar.getStructConstraint(Constants.PERCENTDISTANCECONSTRAINT);
						}
						if(Character.isDigit(str.max.charAt(0))) {
							// If max distance is an integer, add it to max size
							max=String.valueOf(constrainingVar.fixedValue.length()-2+Integer.parseInt(str.max));
						}
						else {
							// If max distance is a variable reference, var may not be known at this time, 
							// use computation defaults e.g. size * 2 (100% of distance, configurable).
							max=String.valueOf(Math.round((constrainingVar.fixedValue.length()-2)*Treatment.DEFAULTMAXDISTANCE));
						}
						if(Character.isDigit(str.min.charAt(0))) {
							// If min distance is an integer , reduce min size, minimum is "minLength".
							if(constrainingVar.fixedValue.length()-2>Integer.parseInt(str.min)) {
							min=String.valueOf(constrainingVar.fixedValue.length()-2-Integer.parseInt(str.min));
							}
							else {
								min=String.valueOf(Treatment.minLength);
							}
						}
						else {
							// If max distance is a variable reference, var may not be known at this time,
							// use minLength
							min=String.valueOf(Treatment.minLength);
						}
					}
					else {
						// If no distance constraint, then use same size as word
						// @Fix 2243: Remove 2 because fixedValue has quotes around it
						min=String.valueOf(constrainingVar.fixedValue.length()-2);
						max=String.valueOf(constrainingVar.fixedValue.length()-2);
					}
				
				
			}
			if(constrainingVar.name!=null) {
				// has length constraint?
				if(constrainingVar.hasConstraint(Constants.LENGTHCONSTRAINT)) {
					StringConstraint lengthcs = constrainingVar.getStringConstraint(Constants.LENGTHCONSTRAINT);
					if(lengthcs.neg==false) {
					// Use constraint only if not a negative constraint
					if(Character.isDigit(lengthcs.min.charAt(0))) {
						// If min distance is an integer
						min=String.valueOf(lengthcs.min);
					}
					else {
						// If min length is a variable reference, var may not be known at this time,
						// use minLength
						min=String.valueOf(Treatment.minLength);
					}
					if(Character.isDigit(lengthcs.max.charAt(0))) {
						// If max distance is an integer
						max=String.valueOf(lengthcs.max);
					}
					else {
						// If max length is a variable reference, var may not be known at this time,
						// use maxLength
						max=String.valueOf(Treatment.maxLength);
					}	
				}
				}
				
			}
		
		}
		// end of treatment if no length constraint
		
		if(Treatment.isAny>-1) {
			String t_spacerResult = LogolUtils.getTemporaryVariable();
			String SpacerPredicate = "spacer_withresult_pos(0,"+min+","+max+","+ t_spacerResult +","+Constants.LOGOLVARAFTER+varID+")";
			
			String spacerMin="0";
			String spacerMax= Integer.toString(Treatment.maxSpacerLength);
			
			if(Treatment.saveAny==false) {
				// For "first" variables, we search with infinite gap e.g. max sequence size
				spacerMax = Integer.toString(Treatment.sequenceLength);
			}
			
			// Specify min and max size of spacer to shorten the analysis, if defined.
			if(!Constants.EMPTYSTRING.equals(Treatment.isAnyMin)) { spacerMin = Treatment.isAnyMin; }
			if(!Constants.EMPTYSTRING.equals(Treatment.isAnyMax) && !Constants.SEQUENCELength.equals(Treatment.isAnyMax)) { spacerMax = Treatment.isAnyMax; }
			else {
				//spacerMax = LogolUtils.getTemporaryVariable();
				//prologSentence += ",length("+Constants.LOGOLVARBEFORE+varID+","+spacerMax+")";
			}
			
			prologSentence += ",anySpacer_pos("+Constants.LOGOLVARBEFORE+varID+","+Constants.LOGOLVARAFTER+varID+","+SpacerPredicate+",1,"+spacerMin+","+spacerMax+","+Constants.LOGOLVARSPACER+varID+")";
			prologSentence += ","+Constants.LOGOLVARERRORS+varID+"=0";
			prologSentence += ","+Constants.LOGOLVARINDEL+varID+"=0";
		}
		else {
			String t_spacerResult = LogolUtils.getTemporaryVariable();
			prologSentence += ",spacer_withresult_pos("+Constants.LOGOLVARBEFORE+varID+","+min+","+max+","+ t_spacerResult +","+Constants.LOGOLVARAFTER+varID+")";
			
			prologSentence += ","+Constants.LOGOLVARSPACER+varID+"=0"; 
			prologSentence += ","+Constants.LOGOLVARERRORS+varID+"=0";
			prologSentence += ","+Constants.LOGOLVARINDEL+varID+"=0";
		}
		
		prologSentence+=","+Constants.LOGOLVARINFO+varID+"=[]";
		
	}
	
	/**
	 * Evaluates prolog to create when some structures constraints are postponed for current variable
	 * @param currentlvar current variable begin analysed
	 * @param model current model
	 * @param inMin minimum length
	 * @param inMax maximum length
	 * @param cost maximal cost
	 * @param distance maximal distance
	 */
	protected void manageStructurePostponed(LogolVariable currentlvar, String model, String inMin, String inMax, String cost, String distance) {
		String min = inMin;
		String max = inMax;
		// Content is not constrained, and this is not a parent, so content is known.
		// We can deduce its length
		// if cost, use length  of word content as max and min
		// if distance, use size of word * 2 as max	

		if(currentlvar.fixedValue!=null) {
		if(min.equals(Integer.toString(Treatment.minLength))) {
			if(!Constants.EMPTYSTRING.equals(distance)) {
				min=Long.toString(Math.round((currentlvar.fixedValue.length()-2)*Treatment.DEFAULTMAXDISTANCE));	
			}
			else {
			min=Integer.toString(currentlvar.fixedValue.length()-2);
			}
		}
		if(max.equals(Integer.toString(Treatment.maxLength))) {
			if(!Constants.EMPTYSTRING.equals(distance)) {
				max=Long.toString(Math.round((currentlvar.fixedValue.length()-2)*Treatment.DEFAULTMAXDISTANCE));	
			}
			else {
			max=Integer.toString(currentlvar.fixedValue.length()-2);
			}
			if(Integer.parseInt(max)>Treatment.maxLength) { max = Integer.toString(Treatment.maxLength); }
		}

		}
		//int reference=-1;
		String reference=null;
		if(currentlvar.name!=null) {
			String name = (String) LogolVariable.userVariables.get(new VariableId(currentlvar.name,currentlvar.model));
			// Not a saved variable, is it a parameter?
			if(name==null) { name = (String) LogolVariable.paramVariables.get(new VariableId(currentlvar.name,currentlvar.model)); }
			if(name!=null) {
			//reference = Integer.parseInt(name);
				reference = name;
			}			
			
			String t_size = LogolUtils.getTemporaryVariable();
			// Get size of referenced variable
			prologSentence+= ",getVariable("+Constants.LOGOLVARREF+reference+",_,_,_,"+t_size+",_,_,_,_)";
			if(min.equals(Integer.toString(Treatment.minLength))) {
				min=t_size;
				// If there is distance, * 2 the size
				if(!Constants.EMPTYSTRING.equals(distance)) {
					String t_newmin = LogolUtils.getTemporaryVariable();
					prologSentence+=","+t_newmin+" is "+min+" * 2";
					min=t_newmin;					
				}
			}
			if(max.equals(Integer.toString(Treatment.maxLength))) {
				max=LogolUtils.getTemporaryVariable();
				// If there is distance, * 2 the size
				if(!Constants.EMPTYSTRING.equals(distance)) {
					String t_newmax = LogolUtils.getTemporaryVariable();
					prologSentence+=","+t_newmax+" is "+max+" * 2";
					max=t_newmax;					
				}
			}	
			

			
		}

		if(Treatment.isAny>-1) {
			String t_spacer = LogolUtils.getTemporaryVariable();
			String SpacerPredicate = "spacer_withresult_pos(0,"+min+","+max+","+ t_spacer +","+Constants.LOGOLVARAFTER+varID+")";
			
			String spacerMin="0";
			String spacerMax= Integer.toString(Treatment.maxSpacerLength);
			
			if(Treatment.saveAny==false) {
				// For "first" variables, we search with infinite gap e.g. max sequence size
				spacerMax = Integer.toString(Treatment.sequenceLength);
			}
			
			// Specify min and max size of spacer to shorten the analysis, if defined.
			if(!Constants.EMPTYSTRING.equals(Treatment.isAnyMin)) { spacerMin = Treatment.isAnyMin; }
			if(!Constants.EMPTYSTRING.equals(Treatment.isAnyMax) && !Constants.SEQUENCELength.equals(Treatment.isAnyMax)) { spacerMax = Treatment.isAnyMax; }
			else {
				//spacerMax = LogolUtils.getTemporaryVariable();
				//prologSentence += ",length("+Constants.LOGOLVARBEFORE+varID+","+spacerMax+")";
			}
			
			prologSentence += ",anySpacer_pos("+Constants.LOGOLVARBEFORE+varID+","+Constants.LOGOLVARAFTER+varID+","+SpacerPredicate+",1,"+spacerMin+","+spacerMax+","+Constants.LOGOLVARSPACER+varID+")";
			prologSentence += ","+Constants.LOGOLVARERRORS+varID+"=0";
			prologSentence += ","+Constants.LOGOLVARINDEL+varID+"=0";
		}
		else {
			String t_spacer = LogolUtils.getTemporaryVariable();
			prologSentence += ",spacer_withresult_pos("+Constants.LOGOLVARBEFORE+varID+","+min+","+max+","+ t_spacer +","+Constants.LOGOLVARAFTER+varID+")";		
			prologSentence += ","+Constants.LOGOLVARSPACER+varID+"=0"; 
			prologSentence += ","+Constants.LOGOLVARERRORS+varID+"=0";
			prologSentence += ","+Constants.LOGOLVARINDEL+varID+"=0";
		}
		
		prologSentence+=","+Constants.LOGOLVARINFO+varID+"=[]";		
		
		
		
	}
	
	/**
	 * Call a built-in predicate to search for a specifci pattern
	 * @param lvar variable to analyse
	 * @param allowSpacer Allow spacer in front of match
	 * @param minSpacer if spacer allowed, what is minimum spacer size
	 * @param maxSpacer if spacer allowed, what is maximum spacer size
	 * @return The prolog content to call the built-in predicate
	 * @throws GrammarException
	 */
	public String externalPredicate(LogolVariable lvar,boolean allowSpacer,String minSpacer,String maxSpacer) throws GrammarException {
		String externalSentence="";
		if(lvar.name!=null && lvar.name.equals(Constants.EXTERNALSIGN)) {
			// if external call, reference is null
			externalSentence+=lvar.extra;
			if(allowSpacer) {
				externalSentence+="_withspacer"	;
			}
			externalSentence+="([";
			externalSentence+=Constants.LOGOLVARBEFORE+lvar.id;
			if(allowSpacer) {
				externalSentence +=","+minSpacer+","+maxSpacer;
			}
			for(String input :lvar.externalIn) {
				if(Character.isDigit(input.charAt(0))) {
					// This is an int
					externalSentence+=","+input;
				}
				else {
					// This is a variable search for variable referece
					int inputreference = -1;
					if(LogolVariable.userVariables.get(new VariableId(input,Treatment.currentModel.name))!=null) 		
					{ inputreference= Integer.parseInt((String)LogolVariable.userVariables.get(new VariableId(input,Treatment.currentModel.name))); }
					if(LogolVariable.paramVariables.get(new VariableId(input,Treatment.currentModel.name))!=null) 		
					{ inputreference= Integer.parseInt((String)LogolVariable.paramVariables.get(new VariableId(input,Treatment.currentModel.name))); }
					
					if(inputreference==-1) {
						throw new GrammarException("Referenced variable could not be found, check variable exist.");
					}
					externalSentence+=","+Constants.LOGOLVARREF+inputreference;
				}
			}
			externalSentence +="]";
			externalSentence +=",[";
			for(String output :lvar.externalOut) {
				if(Character.isDigit(output.charAt(0))) {
					// This is an int
					externalSentence+=output+",";
				}
				else {
					// This is a variable search for variable referece
					int inputreference = -1;
					if(LogolVariable.userVariables.get(new VariableId(output,Treatment.currentModel.name))!=null) 		
					{ inputreference= Integer.parseInt((String)LogolVariable.userVariables.get(new VariableId(output,Treatment.currentModel.name))); }
					if(LogolVariable.paramVariables.get(new VariableId(output,Treatment.currentModel.name))!=null) 		
					{ inputreference= Integer.parseInt((String)LogolVariable.paramVariables.get(new VariableId(output,Treatment.currentModel.name))); }
					
					if(inputreference==-1) {
						throw new GrammarException("Referenced variable could not be found, check variable exist.");
					}
					externalSentence+=","+Constants.LOGOLVARREF+inputreference;
				}
			}					
			externalSentence +=Constants.LOGOLVARSPACER+varID+", "+Constants.LOGOLVARERRORS+varID+", "+Constants.LOGOLVARINDEL+varID+", "+Constants.LOGOLVARAFTER+varID;
			externalSentence +="]";
			externalSentence+=")";
		}
		else throw new GrammarException("Error, this variable do not use external predicate: "+lvar.name);
		return externalSentence;
	}
	
}
