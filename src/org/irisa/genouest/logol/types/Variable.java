package org.irisa.genouest.logol.types;

import java.util.UUID;

import org.apache.log4j.Logger;
import org.irisa.genouest.logol.Constants;
import org.irisa.genouest.logol.Expression;
import org.irisa.genouest.logol.GrammarException;
import org.irisa.genouest.logol.Logol;
import org.irisa.genouest.logol.LogolVariable;
import org.irisa.genouest.logol.StringConstraint;
import org.irisa.genouest.logol.StructConstraint;
import org.irisa.genouest.logol.Treatment;
import org.irisa.genouest.logol.VariableId;
import org.irisa.genouest.logol.Constants.OPTIMAL_CONSTRAINT;
import org.irisa.genouest.logol.Constants.OPTIMAL_MODE;
import org.irisa.genouest.logol.utils.LogolUtils;

/**
 * Standard class managing treatment for a variable content
 * @author osallou
 *
 * History:
 * 15/01/09 @FIX 1281
 * 09/02/09 @FIX 1288
 * 12/06/09 @FIX 1378
 * 23/07/09 @FIX 1394
 * 24/07/09 @FIX 1269
 * 01/03/10 @FIX 1578
 * 12/10/10 @FIX 1683
 * 12/04/11 @FIX 1797
 * 28/04/11 @FIX 1794
 * 26/05/11 @FIX 1800
 * 20/06/11 @FIX 1806
 * 04/10/13 Fix case when distance but no cost
 */
public class Variable extends AbstractVariable{

	private static final Logger logger = Logger.getLogger(org.irisa.genouest.logol.types.Variable.class);


	// If set, means same variable name is used in an other previous save (OR conditions).
	// Need to set variable based on current content
	String duplicateSaveName = null;

	public Variable(int varID) {
		super(varID);
	}

	/**
	 * Return prolog content for the variable definition
	 * @param lvar variable to analyse
	 * @return prolog sentence to manage constraints etc...
	 * @throws GrammarException
	 */
	public String content(LogolVariable lvar) throws GrammarException {
		logger.debug("Analysing variable content "+lvar.name);

		int err=0;
		String reference=null;
		int saveParent=0;

		if(lvar.name.equals("HALT1")) {
			prologSentence += "!";
			return prologSentence;
		}


		if (Treatment.getParseStep()==Constants.VAR_ANALYSIS_STEP) { return ""; }

		if(!Constants.EXTERNALSIGN.equals(lvar.name)){
			String name = (String)LogolVariable.userVariables.get(new VariableId(lvar.name,lvar.model));
			if(name!=null) {
				reference = name;
			}

		}

		prologSentence+=LogolUtils.addParent(lvar,varID);

		// If parents are saved at first match only, remove existing reference due to previous backward searchs.
		if(Treatment.parentStrategy==0) {
			prologSentence+=",retractall(varDefinition("+Constants.LOGOLVARPARENT+varID+",_,_,_,_,_,_,_))";
		}

		if (Treatment.isAny==-1) { err = isBeginConstraint(lvar,false); }


		if(err>=0) { err = applyConstraints(lvar); }


		if(err>0) { saveParent=err; }


		// If isAny > -1 e.g. spacer allowed, begin constraint can be matched only after word match

		if (Treatment.isAny>-1 && err>=0) { err = isBeginConstraint(lvar,true); }


		if(err>=0) { err = isEndConstraint(lvar); }

		 /*
		 * save variable once rule has been matched. Keep reference in LOGOLVAR_Reference_i
		 */

		prologSentence += ",saveVariable('"+Constants.LOGOLVAR+varID+"',"+Constants.LOGOLVARBEFORE+varID+","+Constants.LOGOLVARSPACER+varID+","+Constants.LOGOLVARAFTER+varID+","+Constants.LOGOLVARINFO+varID+",'"+lvar.text+"',["+Constants.LOGOLVARERRORS+varID+","+Constants.LOGOLVARINDEL+varID+"],"+lvar.getConfig()+","+Constants.LOGOLVARREF+varID+")\n";
		// If saveParent > 0, save all parents for later comparison
		if(saveParent>0)
		{
			prologSentence +=",assert(varDefinition('"+Constants.LOGOLVARPARENT+varID+"',"+Constants.LOGOLVARTMP+saveParent+",_,_,_, _, _, _),"+Constants.LOGOLVARPARENTREF+varID+")";
		}
		else {
			prologSentence +=","+Constants.LOGOLVARPARENTREF+varID+"="+Constants.LOGOLVARREF+varID;
		}

		/**
		 *  ..., ( (A:_AA) | (B:_AA) ),..., ?AA
		 */
		if(duplicateSaveName!=null) {
			String name = (String) LogolVariable.userVariables.get(new VariableId(duplicateSaveName,lvar.model));
			if(name==null) { name = (String) LogolVariable.paramVariables.get(new VariableId(duplicateSaveName,lvar.model)); }
			if(name!=null) {
				prologSentence +=","+Constants.LOGOLVARREF+name+"="+Constants.LOGOLVARREF+varID;

			}
		}

		if(err>=0) { err = isAlphabetConstraint(lvar); }

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



	/**
	 * Apply content and cost related constraints to a variable and look for match
	 * @param lvar variable to analyse
	 * @return parent id if any
	 * @throws GrammarException
	 */
	protected int applyConstraints(LogolVariable lvar) throws GrammarException {

		//int reference=-1;
		String reference=null;

		/**
		 * Contains the id under which temporary variable is stored if required for later used
		 */

		int resultId =0;

		boolean parentalCost = false;

		String motif = null;

		//FIX 1394
		boolean negContentConstrained = false;
		boolean lengthConstrained = false;

		// String constraints

		StringConstraint sc = null;
		/**
		 * If first definition of variable, then there is not yet a content, so if no constraint
		 * is defined we must search for a spacer, else for a motif
		 */


		String lengthConstraint = "";
		String min= Integer.toString(Treatment.minLength);
		String max = Integer.toString(Treatment.maxLength);



		// Set to true if a content constraint applies.
		boolean contentConstrained=false;

		// There is a postpone constraint on content
		boolean contentPostponed=false;
		// There is a structural postpone constraint
		boolean structurePostponed=false;

		StringConstraint contentConstraint=null;



		//Match X:{}:{} , save parent reference	if not a known instance
		if(lvar.isParent) {
			String var = LogolVariable.parentVariables.get(new VariableId(lvar.name,Treatment.currentModel.name));
			if(var==null) {
				LogolVariable.parentVariables.put(new VariableId(lvar.name,Treatment.currentModel.name), Integer.toString(varID));
			}
		}

		//manage if content constraint directly e.g. ?X:$[0,3]
		if(lvar.name!=null && !lvar.isParent) {
			// create a constraint based on var name and test if need to postpone
			contentConstraint = new StringConstraint();
			contentConstraint.variableContent=lvar.name;
			contentConstraint.type=Constants.CONTENTCONSTRAINT;
			if(lvar.postpone(Constants.CONTENTCONSTRAINT, contentConstraint)) {
				contentPostponed=true;
			}
		}

		//Fix 1391
		if(lvar.neg==true) {
			negContentConstrained=true;
		}

		//in string and struct constraint, if constraint is in postponedVariables treatments, ignore it.
		for(int i=0;i<lvar.stringConstraints.size();i++) {
			sc = (StringConstraint)lvar.stringConstraints.get(i);

			switch(sc.type) {
				case Constants.SAVECONSTRAINT: {
					// Fix 1806
					if(LogolVariable.matchedVariables.contains(sc.variableContent)) {
						duplicateSaveName = sc.variableContent;
					}
					else {
					LogolVariable.matchedVariables.add(sc.variableContent);
					}
					break;
				}

				case Constants.LENGTHCONSTRAINT: {

					if(sc.optimal==Constants.OPTIMAL_CONSTRAINT.OPTIMAL_LENGTH) {
						Treatment.setOptimalConstraint(true);
						lvar.setOptimalConstraint(OPTIMAL_CONSTRAINT.OPTIMAL_LENGTH);
					}


					if(lvar.postpone(Constants.LENGTHCONSTRAINT,sc)) {
						// TODO Check if length of constraining variable can be used
						break;
						}

					lengthConstrained=true;

					min = sc.min;
					max = sc.max;
					lengthConstraint += ",wordSize_pos("+Constants.LOGOLVARBEFORE+varID+","+Constants.LOGOLVARAFTER+varID+","+Constants.LOGOLVARSPACER+varID+","+Constants.LOGOLVARSIZE+varID+")";

					lengthConstraint += ",";


					if (sc.neg==true) { lengthConstraint +="\\+ "; }
					//min = getExpressionData(min);
					Expression minexpr = new Expression().getExpressionData(min);
					min=minexpr.variable;
					// Min length size must be at least 1
					//if(min.equals("0")) { min="1"; }
					prologSentence+=minexpr.expression;


					lengthConstraint += Constants.LOGOLVARSIZE+varID+">="+min;

					lengthConstraint += ",";



					if (sc.neg==true) { lengthConstraint +="\\+ "; }
					//max=getExpressionData(max);
					Expression maxexpr = new Expression().getExpressionData(max);
					max=maxexpr.variable;
					prologSentence+=maxexpr.expression;



					lengthConstraint += Constants.LOGOLVARSIZE+varID+"=<"+max;

					if(sc.neg==true) {
					// If this is a negative constraint, we cannot use those values as later info to limit to size of the match during the search
					// so reset min and max to init value.
					 min= Integer.toString(Treatment.minLength);
					 max = Integer.toString(Treatment.maxLength);
					}



					break;
				}
				case Constants.CONTENTCONSTRAINT: {
					if(lvar.postpone(Constants.CONTENTCONSTRAINT,sc)) {
						contentPostponed=true;
						logger.debug("CONSTRAINT postpone content");
						break;
						}



					if(lvar.fixedValue!=null)  {
						throw new GrammarException("ERROR: Using a content constraint in a fixed declaration "+lvar.text);
					}

					if(sc.variableContent!=null) {
						// Content constraint applies on instance only, not parent
						if(LogolVariable.parentVariables.containsKey(sc.variableContent))  {
							throw new GrammarException("ERROR: Using a parent reference ("+sc.variableContent+") in content constraint "+lvar.text);
						}

						String name = (String) LogolVariable.userVariables.get(new VariableId(sc.variableContent,lvar.model));
						// Not a saved variable, is it a parameter?
						if(name==null) { name = (String) LogolVariable.paramVariables.get(new VariableId(sc.variableContent,lvar.model)); }
						if(name!=null) {
						//reference = Integer.parseInt(name);
							reference = name;

							motif = LogolUtils.getTemporaryVariable();
							contentConstrained=true;

						prologSentence += ",((\\+ var("+Constants.LOGOLVARREF+reference+"),getVariable("+Constants.LOGOLVARREF+reference+","+motif+",_,_,_,_,_,_,_));(var("+Constants.LOGOLVARREF+reference+"))),";


						}
						else {
							// Variable is not a parameter of current model, not yet known (though not yet postponed too).
							throw new GrammarException("Internal Error: Could not get variable reference");
						}
					}
					if(sc.contentConstraint!=null) {

							motif= LogolUtils.getArray(sc.contentConstraint);
							prologSentence+=",";
							contentConstrained=true;


					}

					if(sc.neg==true) {
						negContentConstrained=true;
					}

					break;
				}

				default: {
					break;
				}

			}

		}


		StructConstraint stc = null;
		String distance="";
		StructConstraint userSpecificCostFunction=null;
		String cost="";
		String userCost="";
		String mincost="";
		String mindistance="";
		//int currentId=0;
		String currentVar = "";

		boolean percentC = false;
		boolean percentD = false;

		boolean distPostponed=false;
		boolean costPostponed=false;

		// Struct constraints
		for(int i=0;i<lvar.structConstraints.size();i++) {
			stc = (StructConstraint)lvar.structConstraints.get(i);
			switch(stc.type) {
				case Constants.COSTCONSTRAINT: {
					if(negContentConstrained==true) {
						throw new GrammarException("Negative content constraints cannot have cost constraints");
					}
					// If content constrained, cannot apply cost, so postpone it.
					if(contentPostponed) { lvar.forcepostpone(stc,Constants.CONTENTCONSTRAINT); break;}
					if(distPostponed) { lvar.forcepostpone(stc,Constants.DISTANCECONSTRAINT); break;}
					if(lvar.postpone(Constants.COSTCONSTRAINT,stc)) {
						costPostponed=true;
						structurePostponed=true;
						if(stc.name==null) {
							// postpone content only when not a user specific cost function
							lvar.forcepostpone(stc);
							}
						break;
					}


					//cost = getExpressionData(stc.max);
					Expression expr = new Expression().getExpressionData(stc.max);

					if(stc.name!=null) {
						userSpecificCostFunction=stc;
						userCost=expr.variable;

					}
					else {
					cost=expr.variable;
					Expression minexpr = new Expression().getExpressionData(stc.min);
					mincost=minexpr.variable;
					prologSentence+=minexpr.expression;
					}

					prologSentence+=expr.expression;



					break;
				}
				case Constants.PERCENTCOSTCONSTRAINT: {
					if(negContentConstrained==true) {
						throw new GrammarException("Negative content constraints cannot have cost constraints");
					}
					// If content constrained, cannot apply cost, so postpone it.
					if(contentPostponed) { lvar.forcepostpone(stc,Constants.CONTENTCONSTRAINT); break;}
					if(distPostponed) { lvar.forcepostpone(stc,Constants.DISTANCECONSTRAINT); break;}
					if(lvar.postpone(Constants.COSTCONSTRAINT,stc)) {
						structurePostponed=true;
						costPostponed=true;
						lvar.forcepostpone(stc);
						break;
					}

					percentC=true;
					//cost = getExpressionData(stc.max);
					Expression expr = new Expression().getExpressionData(stc.max);
					if(stc.name!=null) {
						userSpecificCostFunction=stc;
						userCost=expr.variable;

					}
					else {
					cost=expr.variable;
					Expression minexpr = new Expression().getExpressionData(stc.min);
					mincost=minexpr.variable;
					prologSentence+=minexpr.expression;
					}
					prologSentence+=expr.expression;


					break;
				}
				case Constants.DISTANCECONSTRAINT: {
					if(negContentConstrained==true) {
						throw new GrammarException("Negative content constraints cannot have distance constraints");
					}
					// If content constrained, cannot apply cost, so postpone it.
					if(contentPostponed) { lvar.forcepostpone(stc,Constants.CONTENTCONSTRAINT); break;}
					if(costPostponed) { lvar.forcepostpone(stc,Constants.COSTCONSTRAINT); break;}
					if(lvar.postpone(Constants.DISTANCECONSTRAINT,stc)) {
						structurePostponed=true;
						distPostponed=true;
						lvar.forcepostpone(stc);

						break;
					}
					//distance = getExpressionData(stc.max);
					Expression expr = new Expression().getExpressionData(stc.max);
					distance=expr.variable;
					prologSentence+=expr.expression;
					Expression minexpr = new Expression().getExpressionData(stc.min);
					mindistance=minexpr.variable;
					prologSentence+=minexpr.expression;
					break;
				}
				case Constants.PERCENTDISTANCECONSTRAINT: {
					if(negContentConstrained==true) {
						throw new GrammarException("Negative content constraints cannot have distance constraints");
					}
					// If content constrained, cannot apply cost, so postpone it.
					if(contentPostponed) { lvar.forcepostpone(stc,Constants.CONTENTCONSTRAINT); break;}
					if(costPostponed) { lvar.forcepostpone(stc,Constants.COSTCONSTRAINT); break;}
					if(lvar.postpone(Constants.DISTANCECONSTRAINT,stc)) {
						distPostponed=true;
						structurePostponed=true;
						lvar.forcepostpone(stc);

						break;
					}

					percentD=true;
					//distance = getExpressionData(stc.max);
					Expression expr = new Expression().getExpressionData(stc.max);
					distance=expr.variable;
					prologSentence+=expr.expression;
					Expression minexpr = new Expression().getExpressionData(stc.min);
					mindistance=minexpr.variable;
					prologSentence+=minexpr.expression;

					break;
				}
				case Constants.CONTENTCONSTRAINT: {
					break;
				}

				default: {
					break;
				}
			}
		}

		// If there is a content constraint add it too, cannot use it now
		if(contentPostponed) {
			logger.debug("postpone content treatment");
			manageContentPostponed(lvar.model,min,max);
			// If it is a variable and there is no length constraint, create one with configured max length for a match
			if(lvar.name!=null && Constants.EMPTYSTRING.equals(lengthConstraint)) {
				lengthConstraint += ",wordSize_pos("+Constants.LOGOLVARBEFORE+varID+","+Constants.LOGOLVARAFTER+varID+","+Constants.LOGOLVARSPACER+varID+","+Constants.LOGOLVARSIZE+varID+")";
				lengthConstraint += Constants.LOGOLVARSIZE+varID+"=<"+max;
			}
			else {
			prologSentence+=lengthConstraint;
			}
			if(lvar.isParent) {
				// It is content constrained, so no cost constraint applies on parent
				prologSentence +=","+Constants.LOGOLVARPARENTREF+lvar.id+"="+Constants.LOGOLVARREF+lvar.id;
			}
			return resultId;
		}
		if(lvar.isParent && structurePostponed) {
			throw new GrammarException("Applying a 'not yet known' cost constraint on a parent variable is not supported: "+lvar.text);
			/*
			System.out.println("#DEBUG postpone parent treatment");
			// This is a parent+cost comparison e.g. X:$SY , cost is not known, postpone treatment
			manageContentPostponed(lvar.model,varID,min,max);

			// Add parent in postponed variables to pass the variable reference in models, however set referenceid to -1
			// so that variable is not selected in treatments as a condition

			String name = (String) LogolVariable.parentVariables.get(new VariableId(lvar.name,lvar.model));
			int parentRef=-1;
			if(name!=null) {
				parentRef = Integer.parseInt(name);
			}
			LogolVariable tmpVar = lvar.copyInfo();
			tmpVar.referenceID="-1";
			LogolVariable.postponedVariables.add(tmpVar);


			return resultId;
			*/
		}
		if(!lvar.isParent && structurePostponed) {

			StringConstraint constr = new StringConstraint();
			constr.type=Constants.CONTENTCONSTRAINT;
			if(lvar.fixedValue!=null) {
				constr.contentConstraint=lvar.fixedValue;
			}
			if(lvar.name!=null) {
				constr.variableContent=lvar.name;
			}
			if(lvar.hasConstraint(Constants.CONTENTCONSTRAINT)) {
				constr = lvar.getStringConstraint(Constants.CONTENTCONSTRAINT);
			}

			// Need to postpone content checks due to structure constraints
			if(costPostponed) lvar.forcepostpone(constr,Constants.COSTCONSTRAINT);
			if(distPostponed) lvar.forcepostpone(constr,Constants.DISTANCECONSTRAINT);

			manageStructurePostponed(lvar,lvar.model,min,max,cost,distance);
			// If it is a variable and there is no length constraint, create one with configured max length for a match
			if(lvar.name!=null && Constants.EMPTYSTRING.equals(lengthConstraint)) {
				lengthConstraint += ",wordSize_pos("+Constants.LOGOLVARBEFORE+varID+","+Constants.LOGOLVARAFTER+varID+","+Constants.LOGOLVARSPACER+varID+","+Constants.LOGOLVARSIZE+varID+")";
				lengthConstraint += Constants.LOGOLVARSIZE+varID+"=<"+max;
			}
			else {
			prologSentence+=lengthConstraint;
			}
			return resultId;

		}

		// This is a string constant e.g. "acgt"
		if(lvar.fixedValue!=null&&lvar.type!=2) {
			motif= LogolUtils.getArray(lvar.fixedValue);
			prologSentence+=",";
		}
		if(lvar.name!=null && lvar.name.equals(Constants.EXTERNALSIGN)) {
			prologSentence+=",";
		}




		//Cost applies on parent only if it is not constrained on content, directly or by a stringconstraint
		if(!Constants.EMPTYSTRING.equals(cost) && !contentConstrained && lvar.isParent) { parentalCost=true; }


		// If content is not yet constrained and it is a variable
		if(!contentConstrained && lvar.name!=null && !Constants.EXTERNAL.equals(lvar.name)){
			String name=null;

			motif = LogolUtils.getTemporaryVariable();

			// If need to compare with parent due to previous declaration, get parent ref, not ref
			if(lvar.isParent) {
				// it is a parent reference.
				name = (String) LogolVariable.parentVariables.get(new VariableId(lvar.name,lvar.model));
				if(name!=null)
				{ //reference = Integer.parseInt(name);
					reference = name;
				}
				//If LOGOLVARREF+reference is a variable, means it is not yet a known clause, so do not get content

				if(Treatment.parentStrategy==0) {
					// If known get parent reference
				prologSentence += ",((\\+ var("+Constants.LOGOLVARREF+reference+"),getParentVariable("+Constants.LOGOLVARPARENTREF+reference+","+motif+"));(var("+Constants.LOGOLVARREF+reference+"))),";
				}
				else {
					// If known, get variable:
					String tmpVar = LogolUtils.getTemporaryVariable();
					prologSentence += ",((\\+ var("+Constants.LOGOLVARREF+reference+"),getVariable("+Constants.LOGOLVARREF+reference+","+tmpVar;
					// Load cost only, parental function does not apply to indel
					String tmpCost = LogolUtils.getTemporaryVariable();
					prologSentence += ",_,_,_,_,_,["+tmpCost+",_],_)";
					String parentVar = LogolUtils.getTemporaryVariable();
					/**
					 * Get parent value of previous match variable
					 * if(parent is not set)
					 * 		then apply parentalcost predicate to get all possible parents, and save found matches in current variable structure as parent value
					 * 		else use parent value as motif
					 *  This method is used to calculate possible parents only once, and at 2nd match only. Parent calculation is costly, we
					 *  try to calculate it at latest time.
					 *  #Fix 1288
					 */
					prologSentence += ",(getParent("+Constants.LOGOLVARREF+reference+","+parentVar+"),((var("+parentVar+")"+",parentalCost('substitution',"+tmpVar+","+ motif+","+tmpCost+")"+",setParent("+Constants.LOGOLVARREF+reference+","+motif+"));(\\+var("+parentVar+"),"+motif+"="+parentVar+")))";

					//prologSentence += ",parentalCost('substitution',"+tmpVar+","+ motif+","+tmpCost+")";
					prologSentence+= ");(var("+Constants.LOGOLVARREF+reference+"))),";
				}

			}
			else {
				// This is not a parent but a saved content variable
				name = (String) LogolVariable.userVariables.get(new VariableId(lvar.name,lvar.model));
				if(name==null) {name = (String) LogolVariable.paramVariables.get(new VariableId(lvar.name,lvar.model)); }
				if(name!=null)
				{ //reference = Integer.parseInt(name);
					reference = name;
				}
				// If LOGOLVARREF+reference is a variable, means it is not yet a known clause, so do not get content


					prologSentence += ",((\\+ var("+Constants.LOGOLVARREF+reference+"),getVariable("+Constants.LOGOLVARREF+reference+","+motif+",_,_,_,_,_,_,_));(var("+Constants.LOGOLVARREF+reference+"))),";


			}


			//}
		}


		// Apply modifier if present
		// If content is not yet known, cannot apply it yet
		if(lvar.modifier!=null)	{

				Expression lv_modExpr = applyModifier(lvar,motif);
				motif = lv_modExpr.variable;
				prologSentence += lv_modExpr.expression+",";

		}





		//String dir=Treatment.workingDir+System.getProperty(Constants.FILESEPARATORPROPERTY);
		//if(!workingDir.endsWith("/")) { dir += "/"; }


		//String filename=dir+"tmp_"+LogolUtils.getCounter()+"."+Treatment.uID;
		String filename=".tmp_"+LogolUtils.getCounter();



		/* If motif is null, then it means that variable has never been met before
		 * so we can set motif to a dummy content, it will never be used. The condition
		 *  var(X) will always be true.
		 */
		if(motif==null) { motif="[d,u,m,m,y]"; }

		if(Treatment.isAny>-1) {

			// check if LOGOLVARREF+reference is known. If yes use predicate else use spacer_withresult
			prologSentence+="(";

			//check with preanalysis
			//If this is a variable need to know if already matched previously
			if(lvar.name!=null && reference!=null) {
			prologSentence+="(\\+ var("+Constants.LOGOLVARREF+reference+")";
			}
			else {
				// Dummy test to keep structure with parenthesis ...
				prologSentence+="( 1=1";
			}


			// call suffix match

			// Set minimum and maximum start position of next match
			String suffixMin="0";
			// If suffixMax is 0, then no constraint on maximum, not important for suffix search
			String suffixMax= "0";
			// Specify min and max size of spacer to shorten the analysis, if defined.
			if(!Constants.EMPTYSTRING.equals(Treatment.isAnyMin)) { suffixMin = Treatment.isAnyMin; }
			if(!Constants.EMPTYSTRING.equals(Treatment.isAnyMax) && !Constants.SEQUENCELength.equals(Treatment.isAnyMax)) { suffixMax = Treatment.isAnyMax; }


			// If a begin constraint exist, use it to limit spacer size in suffix search
			if(lvar.hasConstraint(Constants.BEGINCONSTRAINT)&&!lvar.hasPostponedConstraint(Constants.BEGINCONSTRAINT)) {
				StringConstraint lv_constr = lvar.getStringConstraint(Constants.BEGINCONSTRAINT);
				//Fix1269
				if(lv_constr.neg==false) {
					// Cannot use a negative constraint as information

				String lv_min = lv_constr.min;
				String lv_max = lv_constr.max;

				// Evaluate current position
				String lv_curPos = LogolUtils.getTemporaryVariable();
				//prologSentence += ",getPosition(SEQUENCELength,"+Constants.LOGOLVARBEFORE+varID+","+lv_curPos+")";
				prologSentence += ",getPosition_pos("+Constants.LOGOLVARBEFORE+varID+","+lv_curPos+")";

				Expression expr = new Expression().getExpressionData(lv_min);
				lv_min =expr.variable;
				prologSentence+=expr.expression;


				expr = new Expression().getExpressionData(lv_max);
				lv_max =expr.variable;
				prologSentence+=expr.expression;

				suffixMin = LogolUtils.getTemporaryVariable();
				prologSentence+=","+suffixMin+ " is "+lv_min+"-"+lv_curPos;
				suffixMax = LogolUtils.getTemporaryVariable();
				prologSentence+=","+suffixMax+ " is "+lv_max+"-"+lv_curPos;

				logger.debug("use begin constraint of variable to limit previous spacer size, update suffix max");
				}

			}
			else if(lvar.hasConstraint(Constants.ENDCONSTRAINT)&&!lvar.hasPostponedConstraint(Constants.ENDCONSTRAINT)) {
				StringConstraint lv_constr = lvar.getStringConstraint(Constants.ENDCONSTRAINT);
				if(lv_constr.neg==false) {
					// Cannot use a negative constraint as information

				String lv_min = lv_constr.min;
				String lv_max = lv_constr.max;

				// Evaluate current position
				String lv_curPos = LogolUtils.getTemporaryVariable();
				//prologSentence += ",getPosition(SEQUENCELength,"+Constants.LOGOLVARBEFORE+varID+","+lv_curPos+")";
				prologSentence += ",getPosition_pos("+Constants.LOGOLVARBEFORE+varID+","+lv_curPos+")";

				Expression expr = new Expression().getExpressionData(lv_min);
				lv_min =expr.variable;
				prologSentence+=expr.expression;


				expr = new Expression().getExpressionData(lv_max);
				lv_max =expr.variable;
				prologSentence+=expr.expression;

				// Can use suffixMax only as a larger selection, Min is not enough as size migth not be known
				suffixMax = LogolUtils.getTemporaryVariable();
				prologSentence+=","+suffixMax+ " is "+lv_max+"-"+lv_curPos;

				logger.debug("use end constraint of variable to limit previous spacer size, update suffix max");
				}
			}


			if(lvar.name!=null && lvar.name.equals(Constants.EXTERNALSIGN)) {
				// If external call
				//Fix 1578
				prologSentence +=  "," + externalPredicate(lvar,true,suffixMin,suffixMax);
			}
			else {

			// getsuffixmatch(MotifFilename,Motif,Distance,SequenceLength,InputSeq,MuteOnly,Errors,OutputSeq,SpacerSize)

			// Distance and cost are set
			if(!Constants.EMPTYSTRING.equals(distance) && !Constants.EMPTYSTRING.equals(cost)) {
				if(percentC){
					String tmp_cost = LogolUtils.getTemporaryVariable();
					prologSentence+=",percent2int("+motif+","+tmp_cost+","+cost+")";
					cost=tmp_cost;
					String tmp_mincost = LogolUtils.getTemporaryVariable();
					prologSentence+=",percent2int("+motif+","+tmp_mincost+","+mincost+")";
					mincost=tmp_mincost;
					}
				if(percentD){
					String tmp_distance = LogolUtils.getTemporaryVariable();
					prologSentence+=",percent2int("+motif+","+tmp_distance+","+distance+")";
					distance = tmp_distance;
					String tmp_mindistance = LogolUtils.getTemporaryVariable();
					prologSentence+=",percent2int("+motif+","+tmp_mindistance+","+mindistance+")";
					mindistance = tmp_mindistance;
				}
				// Max errors is cost + distance
				String tmp_err = LogolUtils.getTemporaryVariable();
				prologSentence+=","+tmp_err+" is "+cost+" + "+distance;
				//Fix 1378: extension too large for comparison
				if(Logol.getSuffixTool() == 0 || Logol.getSuffixTool() == 1) {
					prologSentence+=",getsuffixmatch_pos('"+Treatment.suffixSearchPath+"','"+filename+".fsa',"+motif+","+tmp_err+","+ Treatment.sequenceLength+" ,"+Constants.LOGOLVARBEFORE+varID+",0,_,_,"+suffixMin+","+suffixMax+","+Constants.LOGOLVARSPACER+varID+")";
				}
				else {
					prologSentence+=",cassiopee_pos('"+Treatment.suffixSearchPath+"','"+filename+".fsa',"+motif+","+cost+","+distance+","+ Treatment.sequenceLength+" ,"+Constants.LOGOLVARBEFORE+varID+",0,_,_,"+suffixMin+","+suffixMax+","+Constants.LOGOLVARSPACER+varID+","+Treatment.dataType+")";
				}
				//check distance and cost values with tmp_err, get as res VARINDEL and VARERRORS
				String lv_matchpos = LogolUtils.getTemporaryVariable();
				prologSentence+=","+lv_matchpos+" is "+Constants.LOGOLVARBEFORE+varID+" + "+Constants.LOGOLVARSPACER+varID;

				//Fix 1378: extension too large for comparison
				/*
				String lv_matchsize = LogolUtils.getTemporaryVariable();
				prologSentence+=","+lv_matchsize+" is "+Constants.LOGOLVARAFTER+varID+ " - " +Constants.LOGOLVARBEFORE+varID+" - "+Constants.LOGOLVARSPACER+varID;
				String lv_matchlist = LogolUtils.getTemporaryVariable();
				prologSentence+=",getCharsFromPosition("+lv_matchpos+","+lv_matchsize+","+lv_matchlist+")";
				*/

				prologSentence+=",isexactwithdistinctgapanderror_pos("+lv_matchpos+","+motif+", "+cost+", "+distance+", "+Constants.LOGOLVARERRORS+varID+", "+Constants.LOGOLVARINDEL+varID+","+Constants.LOGOLVARAFTER+varID+")";

				prologSentence+=","+Constants.LOGOLVARINDEL+varID+">="+mindistance;
				prologSentence+=","+Constants.LOGOLVARERRORS+varID+">="+mincost;
			}

			// distance but no cost
			if(!Constants.EMPTYSTRING.equals(distance) && Constants.EMPTYSTRING.equals(cost)) {
				if(percentD){
				String tmp_distance = LogolUtils.getTemporaryVariable();
				prologSentence+=",percent2int("+motif+","+tmp_distance+","+distance+")";
				distance = tmp_distance;
				String tmp_mindistance = LogolUtils.getTemporaryVariable();
				prologSentence+=",percent2int("+motif+","+tmp_mindistance+","+mindistance+")";
				mindistance = tmp_mindistance;
				}

				if(Logol.getSuffixTool() == 0 || Logol.getSuffixTool() == 1) {
					prologSentence+=",getsuffixmatch_pos('"+Treatment.suffixSearchPath+"','"+filename+".fsa',"+motif+","+distance+","+Treatment.sequenceLength+","+Constants.LOGOLVARBEFORE+varID+",0,"+Constants.LOGOLVARINDEL+varID+",_,"+suffixMin+","+suffixMax+","+Constants.LOGOLVARSPACER+varID+")";
				}
				else {
					prologSentence+=",cassiopee_pos('"+Treatment.suffixSearchPath+"','"+filename+".fsa',"+motif+",0,"+distance+","+Treatment.sequenceLength+","+Constants.LOGOLVARBEFORE+varID+",0,"+Constants.LOGOLVARINDEL+varID+",_,"+suffixMin+","+suffixMax+","+Constants.LOGOLVARSPACER+varID+","+Treatment.dataType+")";
				}

				//TODO check it is distance only
				String matchpos = LogolUtils.getTemporaryVariable();
				prologSentence+=","+matchpos+" is "+Constants.LOGOLVARBEFORE+varID+" + "+Constants.LOGOLVARSPACER+varID;
				//isexactwithdistinctgapanderror_pos(InputPos,[E1 | E2], MaxError, MaxDistance, Errors, DistanceErrors, OutPos)
				prologSentence+=",isexactwithdistinctgapanderror_pos("+matchpos+","+motif+",0, "+distance+", "+Constants.LOGOLVARERRORS+varID+","+Constants.LOGOLVARINDEL+varID+","+Constants.LOGOLVARAFTER+varID+")";
				prologSentence += ","+Constants.LOGOLVARERRORS+varID+"=0";
				prologSentence+=","+Constants.LOGOLVARINDEL+varID+">="+mindistance;
			}
			// Cost but no distance
			if(Constants.EMPTYSTRING.equals(distance) && !Constants.EMPTYSTRING.equals(cost)){
				if(percentC){
				String tmp_cost = LogolUtils.getTemporaryVariable();
				prologSentence+=",percent2int("+motif+","+tmp_cost+","+cost+")";
				cost=tmp_cost;
				String tmp_mincost = LogolUtils.getTemporaryVariable();
				prologSentence+=",percent2int("+motif+","+tmp_mincost+","+mincost+")";
				mincost=tmp_mincost;
				}

				if(Logol.getSuffixTool() == 0 || Logol.getSuffixTool() == 1) {
					prologSentence+=",getsuffixmatch_pos('"+Treatment.suffixSearchPath+"','"+filename+".fsa',"+motif+","+cost+","+Treatment.sequenceLength+" ,"+Constants.LOGOLVARBEFORE+varID+",1,"+Constants.LOGOLVARERRORS+varID+","+Constants.LOGOLVARAFTER+varID+","+suffixMin+","+suffixMax+","+Constants.LOGOLVARSPACER+varID+")";
				}
				else {
					prologSentence+=",cassiopee_pos('"+Treatment.suffixSearchPath+"','"+filename+".fsa',"+motif+","+cost+",0,"+Treatment.sequenceLength+" ,"+Constants.LOGOLVARBEFORE+varID+",1,"+Constants.LOGOLVARERRORS+varID+","+Constants.LOGOLVARAFTER+varID+","+suffixMin+","+suffixMax+","+Constants.LOGOLVARSPACER+varID+","+Treatment.dataType+")";
				}
				prologSentence += ","+Constants.LOGOLVARINDEL+varID+"=0";
				prologSentence+=","+Constants.LOGOLVARERRORS+varID+">="+mincost;
			}
			// no cost nor distance
			if(Constants.EMPTYSTRING.equals(distance) && Constants.EMPTYSTRING.equals(cost)){
				if(userSpecificCostFunction==null) {
					if(negContentConstrained==true) {
						/*
						 * Need to use spacer_withresult_pos on notexact_pos
						 * The negative constraint requires a length constraint, or use the motif size as default constraint
						 * For spacer, use usual analysis
						 *
						 */
						if(!lengthConstrained){
							// Take motif length
							max = LogolUtils.getTemporaryVariable();
							min=max;
							prologSentence +=",length("+motif+","+max+")";
						}
						String  contentPred = "notexact_pos("+Constants.LOGOLVARBEFORE+varID+","+motif+","+min+","+max+","+Constants.LOGOLVARERRORS+varID+","+Constants.LOGOLVARAFTER+varID+")";
						prologSentence += searchWithSpacer(lvar,contentPred);
						prologSentence += ","+Constants.LOGOLVARINDEL+varID+"=0";

					}
					else {
						// Basic treatment
						if(Logol.getSuffixTool() == 0 || Logol.getSuffixTool() == 1) {
							prologSentence+=",getsuffixmatch_pos('"+Treatment.suffixSearchPath+"','"+filename+".fsa',"+motif+",0,"+Treatment.sequenceLength+" ,"+Constants.LOGOLVARBEFORE+varID+",1,"+Constants.LOGOLVARERRORS+varID+","+Constants.LOGOLVARAFTER+varID+","+suffixMin+","+suffixMax+","+Constants.LOGOLVARSPACER+varID+")";
						}
						else {
							prologSentence+=",cassiopee_pos('"+Treatment.suffixSearchPath+"','"+filename+".fsa',"+motif+",0,0,"+Treatment.sequenceLength+" ,"+Constants.LOGOLVARBEFORE+varID+",1,"+Constants.LOGOLVARERRORS+varID+","+Constants.LOGOLVARAFTER+varID+","+suffixMin+","+suffixMax+","+Constants.LOGOLVARSPACER+varID+","+Treatment.dataType+")";
						}
					}
				prologSentence += ","+Constants.LOGOLVARINDEL+varID+"=0";
				}
				else {
					//If not null, do not set ERRORS parameters, will be set later in myCost function
					if(Logol.getSuffixTool() == 0 || Logol.getSuffixTool() == 1) {
						prologSentence+=",getsuffixmatch_pos('"+Treatment.suffixSearchPath+"','"+filename+".fsa',"+motif+",0,"+Treatment.sequenceLength+" ,"+Constants.LOGOLVARBEFORE+varID+",1,_,"+Constants.LOGOLVARAFTER+varID+","+suffixMin+","+suffixMax+","+Constants.LOGOLVARSPACER+varID+")";
					}
					else {
						prologSentence+=",cassiopee_pos('"+Treatment.suffixSearchPath+"','"+filename+".fsa',"+motif+",0,0,"+Treatment.sequenceLength+" ,"+Constants.LOGOLVARBEFORE+varID+",1,_,"+Constants.LOGOLVARAFTER+varID+","+suffixMin+","+suffixMax+","+Constants.LOGOLVARSPACER+varID+","+Treatment.dataType+")";					}

				}
			}

			}

			//If this is a variable need to know if already matched previously
			if(lvar.name!=null  && reference!=null) {
			prologSentence+=") ; (var("+Constants.LOGOLVARREF+reference+")";


			//use anySpacer on predicate, must create specific predicate instead of calling spacer_withresult directly
			// anySpacer([X|Y],Z,Pred,Spacer, Min, Max, NumberSpacer)
			String tmp_spacer = LogolUtils.getTemporaryVariable();
			currentVar = tmp_spacer;
			String SpacerPredicate = "spacer_withresult_pos([],"+min+","+max+","+ tmp_spacer +","+Constants.LOGOLVARAFTER+varID+")";

			String spacerMin="0";

			// Set default max size to sequence size
			String spacerMax= Integer.toString(Treatment.maxSpacerLength);

			if(Treatment.saveAny==false) {
				// For "first" variables, we search with infinite gap e.g. max sequence size
				spacerMax = Integer.toString(Treatment.sequenceLength);
			}

			// Specify min and max size of spacer to shorten the analysis, if defined.
			if(!Constants.EMPTYSTRING.equals(Treatment.isAnyMin)) { spacerMin = Treatment.isAnyMin; }

			// If there is a max, and not the whole sequence
			// @FIX 1281
			if(!Constants.EMPTYSTRING.equals(Treatment.isAnyMax) && !Constants.SEQUENCELength.equals(Treatment.isAnyMax)) { spacerMax = Treatment.isAnyMax; }
			else {
				boolean constraintFound=false;
				// If max length of ANY is not set, max is equal to remaining size.
				// In fact, it is remaining  size less word match size, but it is not yet known.
				if(lvar.hasConstraint(Constants.BEGINCONSTRAINT)&&!lvar.hasPostponedConstraint(Constants.BEGINCONSTRAINT)) {
					StringConstraint lv_constr = lvar.getStringConstraint(Constants.BEGINCONSTRAINT);
					//Fix 1269
					if(lv_constr.neg==false) {

					String lv_min = lv_constr.min;
					String lv_max = lv_constr.max;

					// Evaluate current position
					String lv_curPos = LogolUtils.getTemporaryVariable();
					//prologSentence += ",getPosition(SEQUENCELength,"+Constants.LOGOLVARBEFORE+varID+","+lv_curPos+")";
					prologSentence += ",getPosition_pos("+Constants.LOGOLVARBEFORE+varID+","+lv_curPos+")";

					Expression expr = new Expression().getExpressionData(lv_min);
					lv_min =expr.variable;
					prologSentence+=expr.expression;


					expr = new Expression().getExpressionData(lv_max);
					lv_max =expr.variable;
					prologSentence+=expr.expression;

					spacerMin = LogolUtils.getTemporaryVariable();
					prologSentence+=","+spacerMin+ " is "+lv_min+"-"+lv_curPos;
					spacerMax = LogolUtils.getTemporaryVariable();
					prologSentence+=","+spacerMax+ " is "+lv_max+"-"+lv_curPos;



					logger.debug("use begin constraint of variable to limit previous spacer size, update spacer max");
					constraintFound=true;
					}
				}
				else if(lvar.hasConstraint(Constants.ENDCONSTRAINT)&&!lvar.hasPostponedConstraint(Constants.ENDCONSTRAINT)) {
					StringConstraint lv_constr = lvar.getStringConstraint(Constants.ENDCONSTRAINT);
					//Fix 1269
					if(lv_constr.neg==false) {

					String lv_min = lv_constr.min;
					String lv_max = lv_constr.max;

					// Evaluate current position
					String lv_curPos = LogolUtils.getTemporaryVariable();
					//prologSentence += ",getPosition(SEQUENCELength,"+Constants.LOGOLVARBEFORE+varID+","+lv_curPos+")";
					prologSentence += ",getPosition_pos("+Constants.LOGOLVARBEFORE+varID+","+lv_curPos+")";

					Expression expr = new Expression().getExpressionData(lv_min);
					lv_min =expr.variable;
					prologSentence+=expr.expression;


					expr = new Expression().getExpressionData(lv_max);
					lv_max =expr.variable;
					prologSentence+=expr.expression;

					spacerMax = LogolUtils.getTemporaryVariable();
					prologSentence+=","+spacerMax+ " is "+lv_max+"-"+lv_curPos;



					logger.debug("use end constraint of variable to limit previous spacer size, update spacer max");
					constraintFound=true;
					}
				}


				/* If saveAny is true, means it is an intermediate spacer, so apply max size
				 * Else, it is the start of a model, so max spacer if the size of the remaining sequence.
				 * If a previous constraint has been found, the constraint will be more restrictive.
				 */

				if(Treatment.saveAny && !constraintFound) {
				logger.debug("set spacerMax to maxSpacerLength");
				spacerMax = Integer.toString(Treatment.maxSpacerLength);
				}

			}



			prologSentence += ",anySpacer_pos("+Constants.LOGOLVARBEFORE+varID+","+Constants.LOGOLVARAFTER+varID+","+SpacerPredicate+",1,"+spacerMin+","+spacerMax+","+Constants.LOGOLVARSPACER+varID+")";
			// If user function defined, errors will be defined later on
			if(userSpecificCostFunction==null) {
				if(Treatment.parentStrategy==0) {prologSentence += ","+Constants.LOGOLVARERRORS+varID+"=0"; prologSentence += ","+Constants.LOGOLVARINDEL+varID+"=0"; }
				else {
					if(!Constants.EMPTYSTRING.equals(cost)) {
					// Set ERRORS to cost value
					if(percentC){
						String tmp_cost = LogolUtils.getTemporaryVariable();
						prologSentence+=",percent2int("+motif+","+tmp_cost+","+cost+")";
						cost=tmp_cost;
						String tmp_mincost = LogolUtils.getTemporaryVariable();
						prologSentence+=",percent2int("+motif+","+tmp_mincost+","+mincost+")";
						mincost=tmp_mincost;
					}
					prologSentence += ","+Constants.LOGOLVARERRORS+varID+"=< "+cost;
					prologSentence += ","+Constants.LOGOLVARERRORS+varID+">= "+mincost;
					}
					else {
						prologSentence += ","+Constants.LOGOLVARERRORS+varID+"=0";
					}
					if(!Constants.EMPTYSTRING.equals(distance)) {
					if(percentD){
						String tmp_distance = LogolUtils.getTemporaryVariable();
						prologSentence+=",percent2int("+motif+","+tmp_distance+","+distance+"),";
						distance = tmp_distance;
						String tmp_mindistance = LogolUtils.getTemporaryVariable();
						prologSentence+=",percent2int("+motif+","+tmp_mindistance+","+mindistance+"),";
						mindistance = tmp_mindistance;
						}
					prologSentence += ","+Constants.LOGOLVARINDEL+varID+"=< "+distance;
					prologSentence += ","+Constants.LOGOLVARINDEL+varID+">= "+mindistance;
					}
					else {
						prologSentence += ","+Constants.LOGOLVARINDEL+varID+"=0";
					}
				}

			}


			if(parentalCost && Treatment.parentStrategy==0) {

					if(percentC){
					String tmp_cost = LogolUtils.getTemporaryVariable();
					prologSentence+=",percent2int("+motif+","+tmp_cost+","+cost+")";
					cost=tmp_cost;
					}

				resultId = LogolUtils.getCounter();
				prologSentence +=",parentalCost('substitution',"+currentVar+","+ Constants.LOGOLVARTMP + resultId+","+cost+")";

			}

			prologSentence+=")";

			} // end if lvar.name!=null
			else {
				prologSentence+=")";
			}
			prologSentence+=")";
		} // End if isAny>-1
		else {
			//If LOGOLVARREF+reference is a variable (eg not yet defined), then call spacer_with_result, else
			// call appropriate method

			prologSentence+="(";
			//If this is a variable need to know if already matched previously
			if(lvar.name!=null  && reference!=null && !lvar.name.equals(Constants.EXTERNALSIGN)) {
			prologSentence+=" (\\+ var("+Constants.LOGOLVARREF+reference+")";
			}
			else {
				// Dummy test to keep structure with parenthesis ...
				prologSentence+="( 1=1";
			}

			//if not external
			if((lvar.name==null) ||(lvar.name!=null && !lvar.name.equals(Constants.EXTERNALSIGN))) {
				//cost and distance
				if(!Constants.EMPTYSTRING.equals(cost) && !Constants.EMPTYSTRING.equals(distance)) {
					if(percentC){
						String tmp_cost = LogolUtils.getTemporaryVariable();
						prologSentence+=",percent2int("+motif+","+tmp_cost+","+cost+")";
						cost=tmp_cost;
						String tmp_mincost = LogolUtils.getTemporaryVariable();
						prologSentence+=",percent2int("+motif+","+tmp_mincost+","+mincost+")";
						mincost=tmp_mincost;
					}
					if(percentD) {
						String tmp_distance = LogolUtils.getTemporaryVariable();
						prologSentence+=",percent2int("+motif+","+tmp_distance+","+distance+")";
						distance=tmp_distance;
						String tmp_mindistance = LogolUtils.getTemporaryVariable();
						prologSentence+=",percent2int("+motif+","+tmp_mindistance+","+mindistance+")";
						mindistance=tmp_mindistance;
						}
					//isexactwithdistinctgapanderror_pos(InputPos,[E1 | E2], MaxError, MaxDistance, Errors, DistanceErrors, OutPos)
					prologSentence+=",isexactwithdistinctgapanderror_pos("+Constants.LOGOLVARBEFORE+varID+","+motif+", "+cost+", "+distance+", "+Constants.LOGOLVARERRORS+varID+", "+Constants.LOGOLVARINDEL+varID+", "+Constants.LOGOLVARAFTER+varID+")";
					prologSentence += ","+Constants.LOGOLVARSPACER+varID+"=0";
					prologSentence += ","+Constants.LOGOLVARERRORS+varID+">="+mincost;
					prologSentence += ","+Constants.LOGOLVARINDEL+varID+">="+mindistance;
				}


				// cost but no distance
				if(!Constants.EMPTYSTRING.equals(cost) && Constants.EMPTYSTRING.equals(distance)) {
					// substitution only
					if(percentC){
					String tmp_cost = LogolUtils.getTemporaryVariable();
					prologSentence+=",percent2int("+motif+","+tmp_cost+","+cost+")";
					cost=tmp_cost;
					String tmp_mincost = LogolUtils.getTemporaryVariable();
					prologSentence+=",percent2int("+motif+","+tmp_mincost+","+mincost+")";
					mincost=tmp_mincost;
					}

					prologSentence += ",isexactwitherroronly_pos("+Constants.LOGOLVARBEFORE+varID+","+motif+","+ cost+","+ Constants.LOGOLVARERRORS+varID+","+ Constants.LOGOLVARAFTER+varID +")";
					prologSentence += ","+Constants.LOGOLVARSPACER+varID+"=0";
					prologSentence += ","+Constants.LOGOLVARINDEL+varID+"=0";
					prologSentence += ","+Constants.LOGOLVARERRORS+varID+">="+mincost;
				}
				// distance but no cost
				if(!Constants.EMPTYSTRING.equals(distance) && Constants.EMPTYSTRING.equals(cost)) {
					// insert and deletion allowed
					if(percentD) {
					String tmp_distance = LogolUtils.getTemporaryVariable();
					prologSentence+=",percent2int("+motif+","+tmp_distance+","+distance+")";
					distance=tmp_distance;
					String tmp_mindistance = LogolUtils.getTemporaryVariable();
					prologSentence+=",percent2int("+motif+","+tmp_mindistance+","+mindistance+")";
					mindistance=tmp_mindistance;
					}
					prologSentence += ",isexactwithgaponly_pos("+Constants.LOGOLVARBEFORE+varID+","+motif+","+ distance+","+ Constants.LOGOLVARINDEL+varID+","+ Constants.LOGOLVARAFTER+varID +")";
					prologSentence += ","+Constants.LOGOLVARSPACER+varID+"=0";
					prologSentence += ","+Constants.LOGOLVARERRORS+varID+"=0";
					prologSentence += ","+Constants.LOGOLVARINDEL+varID+">="+mindistance;

				}
				// no cost nor distance
				if(Constants.EMPTYSTRING.equals(cost) && Constants.EMPTYSTRING.equals(distance)) {
					// exact match
					if(userSpecificCostFunction==null) {

						if(negContentConstrained==true) {
							/*
							 * Need to use spacer_withresult_pos on notexact_pos
							 * The negative constraint requires a length constraint, or use the motif size as default constraint
							 * For spacer, use usual analysis
							 *
							 */
							if(!lengthConstrained){
								// Take motif length
								max = LogolUtils.getTemporaryVariable();
								min=max;
								prologSentence +=",length("+motif+","+max+")";
							}
							prologSentence += ",notexact_pos("+Constants.LOGOLVARBEFORE+varID+","+motif+","+min+","+max+","+Constants.LOGOLVARERRORS+varID+","+Constants.LOGOLVARAFTER+varID+")";
							prologSentence += ","+Constants.LOGOLVARINDEL+varID+"=0";


						}
						else {
							// Default case
							prologSentence+= ",isexact_pos("+Constants.LOGOLVARBEFORE+varID+","+motif+","+Constants.LOGOLVARERRORS+varID+","+Constants.LOGOLVARAFTER+varID+")";
							prologSentence += ","+Constants.LOGOLVARINDEL+varID+"=0";
						}


					}
					else {
						//If not null, do not set ERRORS parameters, will be set later in myCost function
						prologSentence+= ",isexact_pos("+Constants.LOGOLVARBEFORE+varID+","+motif+",_,"+Constants.LOGOLVARAFTER+varID+")";
					}
					prologSentence += ","+Constants.LOGOLVARSPACER+varID+"=0";
				}

			} // end if not external

				//If this is a variable need to know if already matched previously
				if(lvar.name!=null  && reference!=null) {
				prologSentence+=") ; (var("+Constants.LOGOLVARREF+reference+")";

				String tmp_spacer = LogolUtils.getTemporaryVariable();
				currentVar = tmp_spacer;
				prologSentence += ",spacer_withresult_pos("+Constants.LOGOLVARBEFORE+varID+","+min+","+max+","+ tmp_spacer +","+Constants.LOGOLVARAFTER+varID+")";
				prologSentence += ","+Constants.LOGOLVARSPACER+varID+"=0";
				// If user function defined, errors will be defined later on
				if(userSpecificCostFunction==null) {

					if(Treatment.parentStrategy==0) {prologSentence += ","+Constants.LOGOLVARERRORS+varID+"=0"; prologSentence += ","+Constants.LOGOLVARINDEL+varID+"=0"; }
					else {
						// Set ERRORS to cost value
						if(!Constants.EMPTYSTRING.equals(cost)) {
						if(percentC){
							String tmp_cost = LogolUtils.getTemporaryVariable();
							prologSentence+=",percent2int("+motif+","+tmp_cost+","+cost+")";
							cost=tmp_cost;
							}
						prologSentence += ","+Constants.LOGOLVARERRORS+varID+"= "+cost;
						}
						else {
							prologSentence += ","+Constants.LOGOLVARERRORS+varID+"= 0";
						}
						if(!Constants.EMPTYSTRING.equals(distance)) {
						if(percentD) {
							String tmp_distance = LogolUtils.getTemporaryVariable();
							prologSentence+=",percent2int("+motif+","+tmp_distance+","+distance+")";
							distance=tmp_distance;
							}
						prologSentence += ","+Constants.LOGOLVARINDEL+varID+"= "+distance;
						}
						else {
							prologSentence += ","+Constants.LOGOLVARINDEL+varID+"= 0";
						}

					}
				}

				//currentId = LogolUtils.getCounter();

				if(parentalCost && Treatment.parentStrategy==0) {

						if(percentC){
						String tmp_cost = LogolUtils.getTemporaryVariable();
						prologSentence+=",percent2int("+motif+","+tmp_cost+","+cost+")";
						cost=tmp_cost;
						}
					resultId = LogolUtils.getCounter();
					prologSentence +=",parentalCost('substitution',"+currentVar+","+ Constants.LOGOLVARTMP + resultId+","+cost+")";

				}

				prologSentence+=")";

				} // end if lvar.name!=null
				else if(lvar.name!=null && lvar.name.equals(Constants.EXTERNALSIGN)) {
					//Fix 1578
					// if external call, reference is null
					prologSentence += "," + externalPredicate(lvar,false,null,null);
					prologSentence+=")";
				}
				else {
					prologSentence+=")";
				}
				prologSentence+=")";

		}

		// If it is a variable and there is no length constraint, create one with configured max length for a match
		if(lvar.name!=null && Constants.EMPTYSTRING.equals(lengthConstraint)) {
			lengthConstraint += ",wordSize_pos("+Constants.LOGOLVARBEFORE+varID+","+Constants.LOGOLVARAFTER+varID+","+Constants.LOGOLVARSPACER+varID+","+Constants.LOGOLVARSIZE+varID+")";
			lengthConstraint += Constants.LOGOLVARSIZE+varID+"=<"+max;
		}
		else {
		prologSentence+=lengthConstraint;
		}

		prologSentence+=","+Constants.LOGOLVARINFO+varID+"=[]";

		// Cost function is a user specific one.
		if(userSpecificCostFunction!=null) {

			String wordSize = LogolUtils.getTemporaryVariable();
			prologSentence+=",wordSize_pos("+Constants.LOGOLVARBEFORE+varID+","+Constants.LOGOLVARAFTER+varID+","+Constants.LOGOLVARSPACER+varID+","+wordSize+")";
			String wordContent = LogolUtils.getTemporaryVariable();

			if(percentC){
				String tmp_cost = LogolUtils.getTemporaryVariable();
				prologSentence+=",percent2int("+wordContent+","+tmp_cost+","+userCost+")";
				userCost=tmp_cost;
			}

			prologSentence+=",wordContent_pos("+Constants.LOGOLVARBEFORE+varID+","+wordSize+","+Constants.LOGOLVARSPACER+varID+","+wordContent+")";
			/*
			 * WARNING specific cost function program must be installed under tools directory
			 */
			prologSentence+=",myCost('"+Treatment.installPath+"/tools/"+userSpecificCostFunction.name+"',"+wordContent+","+userCost+","+Constants.LOGOLVARERRORS+varID+")";
			// Distance do not apply to cost function, set value to zero
			prologSentence += ","+Constants.LOGOLVARINDEL+varID+"=0";
		}


		return resultId;
	}

	/**
	 * Search for a specific predicate using the anySpacer predicate to go along the sequence
	 * @param lvar Current variable under analyse
	 * @param predicate Predicate to apply on each position
	 * @return Prolog code to use
	 * @throws GrammarException
	 */
	private String searchWithSpacer(LogolVariable lvar, String predicate) throws GrammarException {

		String lv_prolog ="";

		String spacerMin="0";
		// Set default max size to sequence size
		String spacerMax= Integer.toString(Treatment.maxSpacerLength);

		// Specify min and max size of spacer to shorten the analysis, if defined.
		if(!Constants.EMPTYSTRING.equals(Treatment.isAnyMin)) { spacerMin = Treatment.isAnyMin; }

		// If there is a max, and not the whole sequence
		// @FIX 1281
		if(!Constants.EMPTYSTRING.equals(Treatment.isAnyMax) && !Constants.SEQUENCELength.equals(Treatment.isAnyMax))
		{ spacerMax = Treatment.isAnyMax; }
		else {
			boolean constraintFound=false;
			// If max length of ANY is not set, max is equal to remaining size.
			// In fact, it is remaining  size less word match size, but it is not yet known.
			if(lvar.hasConstraint(Constants.BEGINCONSTRAINT)&&!lvar.hasPostponedConstraint(Constants.BEGINCONSTRAINT)) {
				StringConstraint lv_constr = lvar.getStringConstraint(Constants.BEGINCONSTRAINT);
				String lv_min = lv_constr.min;
				String lv_max = lv_constr.max;

				// Evaluate current position
				String lv_curPos = LogolUtils.getTemporaryVariable();
				//prologSentence += ",getPosition(SEQUENCELength,"+Constants.LOGOLVARBEFORE+varID+","+lv_curPos+")";
				lv_prolog += ",getPosition_pos("+Constants.LOGOLVARBEFORE+varID+","+lv_curPos+")";

				Expression expr = new Expression().getExpressionData(lv_min);
				lv_min =expr.variable;
				lv_prolog+=expr.expression;


				expr = new Expression().getExpressionData(lv_max);
				lv_max =expr.variable;
				lv_prolog+=expr.expression;

				spacerMin = LogolUtils.getTemporaryVariable();
				lv_prolog+=","+spacerMin+ " is "+lv_min+"-"+lv_curPos;
				spacerMax = LogolUtils.getTemporaryVariable();
				lv_prolog+=","+spacerMax+ " is "+lv_max+"-"+lv_curPos;


				logger.debug("use begin constraint of variable to limit previous spacer size, update spacer max");
				constraintFound=true;
			}
			else if(lvar.hasConstraint(Constants.ENDCONSTRAINT)) {
				//TODO manage an end constraint, need to remove a word size eg max match size
			}


			/* If saveAny is true, means it is an intermediate spacer, so apply max size
			 * Else, it is the start of a model, so max spacer if the size of the remaining sequence.
			 * If a previous constraint has been found, the constraint will be more restrictive.
			 */

			if(Treatment.saveAny && !constraintFound) {
			logger.debug("set spacerMax to maxSpacerLength");
			spacerMax = Integer.toString(Treatment.maxSpacerLength);
			}

		}


		lv_prolog += ",anySpacer_pos("+Constants.LOGOLVARBEFORE+varID+","+Constants.LOGOLVARAFTER+varID+","+predicate+",1,"+spacerMin+","+spacerMax+","+Constants.LOGOLVARSPACER+varID+")";

		return lv_prolog;
	}


}
