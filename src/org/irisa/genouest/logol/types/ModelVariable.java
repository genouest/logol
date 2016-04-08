package org.irisa.genouest.logol.types;

import org.apache.log4j.Logger;
import org.irisa.genouest.logol.Constants;
import org.irisa.genouest.logol.GrammarException;
import org.irisa.genouest.logol.LogolVariable;
import org.irisa.genouest.logol.Treatment;
import org.irisa.genouest.logol.VariableId;
import org.irisa.genouest.logol.utils.LogolUtils;

/**
 * Class managing model variable type
 * @author osallou
 * History 03/08/09 O. Sallou Fix 1399 Model parameters sometimes badly managed
 * 28/04/11 Fix 1794
 *
 */
public class ModelVariable extends AbstractVariable {

	private static final Logger logger = Logger.getLogger(org.irisa.genouest.logol.types.ModelVariable.class);
	
	public ModelVariable(int varID) {
		super(varID);
	}
	
	/**
	 * Return prolog content for the variable definition
	 * @param lvar variable to analyse
	 * @return prolog sentence to manage constraints etc...
	 * @throws GrammarException
	 */
	public String content(LogolVariable lvar) throws GrammarException {
		
		logger.debug("Analysing model variable "+lvar.name);
		
		// set id of the parent of the variable if any (else set to 0 by default)
		// If Parent not set, parent = current parent
		
		prologSentence+=LogolUtils.addParent(lvar,varID);
				
		String SpacerPredicate="";
		String spacerMin="0";
		String spacerMax= Integer.toString(Treatment.maxSpacerLength);
		
		if(Treatment.saveAny==false) {
			// For "first" variables, we search with infinite gap e.g. max sequence size
			spacerMax = Integer.toString(Treatment.sequenceLength);
		}
		
		// Specify min and max size of spacer to shorten the analysis, if defined.
		if(!Constants.EMPTYSTRING.equals(Treatment.isAnyMin)) { spacerMin = Treatment.isAnyMin; }
		if(!Constants.EMPTYSTRING.equals(Treatment.isAnyMax) && !Constants.SEQUENCELength.equals(Treatment.isAnyMax)) { spacerMax = Treatment.isAnyMax; }
		
		String postponedVariables = LogolUtils.getTemporaryVariable();
		prologSentence += ",append(PostponedVariables,"+LogolVariable.getPostponedVariableList(lvar.model)+","+postponedVariables+")";
		
		
		SpacerPredicate+=lvar.mod.name+"(";
		SpacerPredicate+=Constants.LOGOLVARBEFORE+lvar.id;
		SpacerPredicate+=","+postponedVariables;
		SpacerPredicate+=","+lvar.model.toUpperCase()+"_"+Constants.POSTPONED;
		for(int i=0;i<lvar.mod.vars.size();i++) {			
			String var = (String)lvar.mod.vars.get(i);
			if(LogolVariable.userVariables.containsKey(new VariableId(var,lvar.model))) {SpacerPredicate+=","+Constants.LOGOLVARREF+LogolVariable.userVariables.get(new VariableId(var,lvar.model)); }
			else if(LogolVariable.parentVariables.containsKey(new VariableId(var,lvar.model))) { SpacerPredicate+=","+Constants.LOGOLVARPARENTREF+LogolVariable.parentVariables.get(new VariableId(var,lvar.model));}
			else if (LogolVariable.paramVariables.containsKey(new VariableId(var,lvar.model))) {
				SpacerPredicate+=","+Constants.LOGOLVARREF+LogolVariable.paramVariables.get(new VariableId(var,lvar.model));
				}
			else {
				//@Fix 1399
				// This is an intermediate variable, used only to transport info between models	
				// Use negative counter to get a reference different from other variables.
				String lv_tmpcount =  Constants.INTER+(LogolUtils.getCounter());
				LogolVariable.userVariables.put(new VariableId(var,lvar.model),lv_tmpcount);						
				String lv_tmpvar = Constants.LOGOLVARREF + lv_tmpcount;
				//SpacerPredicate+=","+var;
				SpacerPredicate+=","+lv_tmpvar;

			}
		}

		SpacerPredicate+=","+Constants.LOGOLVARPARENT+lvar.id+","+Constants.LOGOLVARINFO+varID+",["+Constants.LOGOLVARERRORS+lvar.id+","+Constants.LOGOLVARINDEL+lvar.id+"],"+Constants.LOGOLVARAFTER+lvar.id;
		SpacerPredicate+= ")";
		
		
		if(Treatment.isAny>-1) {
		prologSentence += ",anySpacer_pos("+Constants.LOGOLVARBEFORE+varID+","+Constants.LOGOLVARAFTER+varID+","+SpacerPredicate+",1,"+spacerMin+","+spacerMax+","+Constants.LOGOLVARSPACER+varID+")";
		}
		else  {
			prologSentence+=","+SpacerPredicate;
			prologSentence+=","+Constants.LOGOLVARSPACER+varID+"=0";
		}
		
		
		
		//Save model content
		prologSentence += ",saveVariable('"+Constants.LOGOLVAR+varID+"',"+Constants.LOGOLVARBEFORE+varID+","+Constants.LOGOLVARSPACER+varID+","+Constants.LOGOLVARAFTER+varID+","+Constants.LOGOLVARINFO+varID+",'"+lvar.text+"',["+Constants.LOGOLVARERRORS+varID+","+Constants.LOGOLVARINDEL+varID+"],"+lvar.getConfig()+","+Constants.LOGOLVARREF+varID+")\n";
		
		
		
		//Check for postponed variable constraints
		prologSentence+=managePostponedVariables(lvar);
		
		if(Treatment.isAny>-1 && Treatment.saveAny) {
			prologSentence += ",saveVariable('"+Constants.LOGOLVAR+Treatment.isAny+"',"+Constants.LOGOLVARBEFORE+varID+","+Constants.LOGOLVARSPACER+varID+",[],'"+Constants.ANY+"',[0,0],[0,0],"+Constants.LOGOLVARREF+Treatment.isAny+")\n";
		}
		
		Treatment.saveAny=true;
		Treatment.isAny=-1;
		Treatment.isAnyMin="";
		Treatment.isAnyMax="";
		
		return prologSentence;
	}
	
	/**
	 * Constraints cannot be applied directly on a model. If required, model should be put in a view variable
	 * , e.g. ...,(mymodel),...
	 * @param lvar 
	 * @return -1
	 * @throws GrammarException
	 */
	protected int applyConstraints(LogolVariable lvar) throws GrammarException {
		return -1;
	}
}
