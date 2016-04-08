package org.irisa.genouest.logol.types;

import org.apache.log4j.Logger;
import org.irisa.genouest.logol.Constants;
import org.irisa.genouest.logol.GrammarException;
import org.irisa.genouest.logol.LogolVariable;
import org.irisa.genouest.logol.Treatment;
import org.irisa.genouest.logol.utils.LogolUtils;

/**
 * Default class managing Fixed variable type
 * @author osallou
 *
 */
public class FixedVariable extends Variable {

	private static final Logger logger = Logger.getLogger(org.irisa.genouest.logol.types.FixedVariable.class);
	
	public FixedVariable(int varID) {
		super(varID);
	}
	
	/**
	 * Return prolog content for the variable definition
	 * @param lvar variable to analyse
	 * @return prolog sentence to manage constraints etc...
	 * @throws GrammarException
	 */
	public String content(LogolVariable lvar) throws GrammarException {

		logger.debug("Analysing fixed variable "+lvar.fixedValue);
		
		int err = 0;
		
		prologSentence+=LogolUtils.addParent(lvar,varID);
		
		if (Treatment.isAny==-1) { err = isBeginConstraint(lvar,false); }
		
			err = applyConstraints(lvar);

		if (Treatment.isAny>-1) { err = isBeginConstraint(lvar,true); }
		
		err = isEndConstraint(lvar);

		 /*
		 * save variable once rule has been matched. Keep reference in LOGOLVAR_Reference_i
		 */

		prologSentence += ",saveVariable('"+Constants.LOGOLVAR+varID+"',"+Constants.LOGOLVARBEFORE+varID+","+Constants.LOGOLVARSPACER+varID+","+Constants.LOGOLVARAFTER+varID+","+Constants.LOGOLVARINFO+varID+",'"+lvar.text+"',["+Constants.LOGOLVARERRORS+varID+","+Constants.LOGOLVARINDEL+varID+"],"+lvar.getConfig()+","+Constants.LOGOLVARREF+varID+")\n";
		
		// No parents save as it is a fixed content, no parental comparison

		//Check for postponed variable constraints
		prologSentence+=managePostponedVariables(lvar);

		
		// Save previous spacer
		if(Treatment.isAny>-1 && Treatment.saveAny) {
			prologSentence += ",saveVariable('"+Constants.LOGOLVAR+Treatment.isAny+"',"+Constants.LOGOLVARBEFORE+varID+","+Constants.LOGOLVARSPACER+varID+",[],'"+Constants.ANY+"',[0,0],[0,0],"+Constants.LOGOLVARREF+Treatment.isAny+")\n";
		}
		
		Treatment.saveAny=true;
		Treatment.isAny=-1;
		Treatment.isAnyMin="";
		Treatment.isAnyMax="";
		
		return prologSentence;
	}
}
