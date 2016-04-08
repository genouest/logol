package org.irisa.genouest.logol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.irisa.genouest.logol.types.FixedVariable;
import org.irisa.genouest.logol.types.ModelVariable;
import org.irisa.genouest.logol.types.RepeatVariable;
import org.irisa.genouest.logol.types.Variable;
import org.irisa.genouest.logol.types.ViewVariable;
import org.irisa.genouest.logol.utils.LogolUtils;



/**
 * This class generate some Prolog code based on parser result.
 * <br/>The <code>LogolVariable</code> contains all data needed to get intelligent parsing.
 * @author osallou
 * @version 1.0
 *
 * History:
 * 22/08/10 Fix 1664 Unicode issues with special chars
 * 28/04/11 @FIX 1794
 * 21/10/13 @FIX 2244 length of constraint in Interval can be > 1
 *
 */
public class Treatment {

	private static final Logger logger = Logger.getLogger(org.irisa.genouest.logol.Treatment.class);

	/**
	 * Type of sequence data to analyse.
	 * 0: DNA
	 * 1: RNA
	 * 2: Protein
	 */
	public static int dataType = 0;

	/**
	 * Decides if previous spacer must be saved, happens for first variable of each rule.
	 */
	public static boolean saveAny=false;

	private static int parseStep = 0;


	public static HashMapCounter counters = new HashMapCounter();


	/*
	 * List of models
	 */
	public static Vector<Model> models = new Vector<Model>();

	/*
	 * List of definitions (init of data)
	 */
	public static Vector<String> definitions = new Vector<String>();

	/**
	 * Query e.g. sequence of models
	 */
	public static String query="";

	public static HashMap<Integer,LogolVariable> varInfo = new HashMap<Integer,LogolVariable>();

	public static String uID="";

	public static String workingDir = "";

	public static String filename="";

	public static Model currentModel= new Model();

	/**
	 * Maximum distance to use when looking for a word and distance is not yet known.
	 * Default is 1 e.g. 100%.
	 */
	public static double DEFAULTMAXDISTANCE = 1;

	/*
	 * Match any character
	 */
	public static int isAny = -1;
	public static String isAnyMin="";
	public static String isAnyMax="";

	//public static String offset="0";

	/**
	 * Minimum length of a variable X
	 */
	public static int minLength=1;
	/**
	 * Maximum length of a variable X
	 */
	public static int maxLength=0;
	/**
	 * Maximum length of a spacer .*
	 */
	public static int maxSpacerLength=0;

	/**
	 * Sequence length
	 */
	public static int sequenceLength=0;

	/**
	 * Maximum length of a complete match
	 */
	public static long maxResultSize=0;

	/**
	 * Strategy to apply when looking for parents.
	 * 0: Get all parents at first X, then compare with next when reached. All solutions are evaluated once.
	 * 1: Save parent as equal to first match in string. Then evaluate all parents for each next call (interesting if applying a strategy limiting. number of possibilities
	 */
	public static int parentStrategy=0;

	/**
	 * Installation path
	 */
	public static String installPath=null;
	/**
	 * Path to suffixSearch shell
	 */
	public static String suffixSearchPath=null;

	/**
	 * ID of the current variable
	 */
	int varID=-1;

	/**
	 * Prolog sentence for the current variable treatment
	 */
	String prologSentence = null;


	/**
	 * True if there is at least one optimal constraint
	 */
	static boolean optimalConstraint = false;

	/**
	 * Contains the list of variable names with an optimal constraint in grammar with their min/max values
	 */
	public static HashMap<String,String[]> optimalVariables = new HashMap<String,String[]>();


	static HashMap<String,ArrayList<String>> metacontrols = null;

	static ArrayList<String> metacontrolsdefs = null;

	public static void reset() {
		 dataType = 0;
		 saveAny=false;
		 parseStep = 0;
		 counters = new HashMapCounter();
		 models = new Vector<Model>();
		 definitions = new Vector<String>();
		 query="";
		 varInfo = new HashMap<Integer,LogolVariable>();
		 uID="";
		 workingDir = "";
		 filename="";
		 currentModel= new Model();
		 DEFAULTMAXDISTANCE = 1;
		 isAny = -1;
		 isAnyMin="";
		 isAnyMax="";
		 minLength=1;
		 maxLength=0;
		 maxSpacerLength=0;
		 maxResultSize=0;
		 parentStrategy=0;
		 installPath=null;
		 suffixSearchPath=null;
		 optimalConstraint=false;
		 metacontrolsdefs=null;
		 metacontrols=null;
	}


	public Treatment() {
		prologSentence="";
	}


	/**
	 * get the current step of parsing
	 * @return the current step id
	 */
	public static int getParseStep() {
		return parseStep;
	}

	/**
	 * Defines current step
	 * @param step See Constants
	 */
	public static void setParseStep(int step) {
		parseStep=step;
	}

	/**
	 * Creates Prolog query for the current variable
	 * @param lvar	variable to analyse
	 * @param countVar	id of the current variable
	 * @return	Prolog string
	 * @throws GrammarException
	 */
	public String get(LogolVariable lvar, int countVar) {

		// Comments in variable name or model name are removed directly in parser treatment with a replaceAll("[a-zA-Z0-9]+%[a-zA-Z0-9]+%","")

		// Update text so that if a commentary name is placed to replace var or model name by commentary name
		// e.g. replace  ?LOGOLVAR1%myexplanitoryname%  by ?myexplanitory
		// This update impacts textual information only, not the prolog content.
		Pattern p = Pattern.compile("([a-zA-Z0-9]+)%([a-zA-Z0-9]+)%");
		Matcher m = p.matcher(lvar.text);
		while(m.find()) {
			lvar.text=lvar.text.replaceAll(m.group(1)+"%"+m.group(2)+"%", m.group(2));
		}






		logger.debug("Analyse variable "+countVar+" : "+ lvar.text);

		varID = countVar;

		lvar.id=countVar;
		lvar.model=currentModel.name;

		// Get variable definitions information

		if(getParseStep()==Constants.VAR_ANALYSIS_STEP) {
		initVarInfo(lvar,countVar);
		}

		if(getParseStep()==Constants.POSTCONDITION_ANALYSIS_STEP) {
		// Analyse variables that need to postpone some treatments
			// Record all variables information
			LogolVariable.varData.put(String.valueOf(countVar), lvar);

			return getVariableParser(lvar);
		}

		if(getParseStep()==Constants.EXECUTE_STEP) {
		// Generate prolog according to var info and previous steps analysis
		try {

		LogolVariable var = (LogolVariable)varInfo.get(countVar);


		if(var!=null) {
			// If variable already declared, add info (case of OR treatments where var ID is the same)
			//lvar.text=var.text+";"+lvar.text;
			lvar.id=countVar;
			varInfo.put(countVar, lvar);
		}
		else
		{ varInfo.put(countVar, lvar); }


		// If it is a group (A,B,C..)
		if(lvar.type==2) {

			ViewVariable viewVar = new ViewVariable(lvar.id);
			return viewVar.content(lvar);

		}
		// This is a model
		if(lvar.type==3) {

			ModelVariable modelVar = new ModelVariable(lvar.id);
			return modelVar.content(lvar);
		}

		if(lvar.name!=null && Constants.ANY.equals(lvar.name)) {
				isAny = countVar;
				addAnyLengthConstraint(lvar);
				return "("+Constants.LOGOLVARAFTER+varID+"="+Constants.LOGOLVARBEFORE+varID+","+Constants.LOGOLVARSPACER+varID+"=0)";
		}


		if(lvar.fixedValue!=null) {

				FixedVariable fixedVar = new FixedVariable(lvar.id);
				prologSentence+=fixedVar.content(lvar);
				}
		else if(lvar.name!=null){

				Variable variableVar = new Variable(lvar.id);
				prologSentence+=variableVar.content(lvar);
				}
		else {

			RepeatVariable repeatVar = new RepeatVariable(lvar.id);
			prologSentence+=repeatVar.content(lvar);

		}

		}
		catch(GrammarException e) {
			System.out.println("GRAMMAR FAILURE\n"+e.getMessage());
			System.exit(1);
		}

		}


		return prologSentence;
	}




	/**
	 * Used as pre-analyse to write some proglog content. Checks if a variable is used and when it is saved.
	 * @param lvar current variable info to analyse
	 * @return data to write to manage this variable
	 */
	private String getVariableParser(LogolVariable lvar) {



		//TODO optimizations for an other pre-analyse step:
		// ((fixed(2),((\+var(X11));(var(X11),X11='mod1,2')));(\+fixed(2),((\+var(X11),write(X11),write(' used in mod1,2'),nl;(var(X11))))))
		// Get number of repeats for a variable (need to save cost/distance to keep max), then search repeats that match position and constraint for current match
		// this avoid looking for all possible data but limit on possible repeats
		// 1) if size is known, search on all repeats for this size
		// 2) if size unknown, search spacer then reject matches if no repeat available for this one.


		String data="";
		String save="";
		String use="";
		/*
		 * Type is defined per constraint type:<br/>
		 * <li>CONTENTCONSTRAINT</li>
		 * <li>PARENTCONTENTCONSTRAINT</li>
		 * <li>LENGTHCONSTRAINT</li>
		 * <li>BEGINCONSTRAINT</li>
		 * <li>ENDCONSTRAINT</li>
		 * <li>COSTCONSTRAINT</li>
		 * <li>DISTANCECONSTRAINT</li>
		 * <li>REPEATCONSTRAINT</li>
		 */

		if(lvar.name!=null && lvar.name.equals(Constants.EXTERNALSIGN)) return "(var(Dummy);\\+var(Dummy))";

		if(lvar.type!=3) {
			// return for type 2 the predicate
			if(lvar.type==2) {
				Entity view = new Entity();
				use += ","+view.analysispredicate(lvar.fixedValue, varID,true);
			}

			// scan all constraints to check if a variable is used.
			if(lvar.name!=null && !lvar.name.equals(Constants.ANY)) {
				// This is a variable reference
				if(!lvar.isParent) { use += ",((\\+var("+lvar.name+"));(var("+lvar.name+"),"+lvar.name+"='"+lvar.model+","+lvar.id+","+Constants.CONTENTCONSTRAINT+"'))"; }
			}



			StringConstraint sc=null;

			for(int i=0;i<lvar.stringConstraints.size();i++) {
				sc = (StringConstraint)lvar.stringConstraints.get(i);

				switch(sc.type) {
					case Constants.SAVECONSTRAINT: {
						//if(!data.equals("")) data+=",";
						save+="((\\+var("+sc.variableContent+"),format(OutStream,'~w,~w,~w~N',["+sc.variableContent+",'"+lvar.model+"','"+lvar.id+"']),nl);(var("+sc.variableContent+")))";

						LogolVariable.matchedVariables.add(sc.variableContent);
						break;
					}
					case Constants.CONTENTCONSTRAINT: {
						if(sc.variableContent!=null) {
							// This is a variable reference
							use += ",((\\+var("+sc.variableContent+"));(var("+sc.variableContent+"),"+sc.variableContent+"='"+lvar.model+","+lvar.id+","+Constants.CONTENTCONSTRAINT+"'))";
						}
						break;
					}
					case Constants.LENGTHCONSTRAINT: {
						use+=checkIntervalConstraint(sc.min,lvar.model,lvar.id,Constants.LENGTHCONSTRAINT);
						use+=checkIntervalConstraint(sc.max,lvar.model,lvar.id,Constants.LENGTHCONSTRAINT);
						break;
					}
					case Constants.ENDCONSTRAINT: {
						use+=checkIntervalConstraint(sc.min,lvar.model,lvar.id,Constants.ENDCONSTRAINT);
						use+=checkIntervalConstraint(sc.max,lvar.model,lvar.id,Constants.ENDCONSTRAINT);
						break;
					}
					case Constants.BEGINCONSTRAINT: {
						use+=checkIntervalConstraint(sc.min,lvar.model,lvar.id,Constants.BEGINCONSTRAINT);
						use+=checkIntervalConstraint(sc.max,lvar.model,lvar.id,Constants.BEGINCONSTRAINT);
						break;
					}
					default: {
						break;
					}
				}
			}

			StructConstraint stc=null;

			for(int i=0;i<lvar.structConstraints.size();i++) {
				stc = (StructConstraint)lvar.structConstraints.get(i);

				switch(stc.type) {
					case Constants.PERCENTCOSTCONSTRAINT:
					case Constants.COSTCONSTRAINT: {
						use+=checkIntervalConstraint(stc.min,lvar.model,lvar.id,Constants.COSTCONSTRAINT);
						use+=checkIntervalConstraint(stc.max,lvar.model,lvar.id,Constants.COSTCONSTRAINT);
						break;
					}
					case Constants.PERCENTDISTANCECONSTRAINT:
					case Constants.DISTANCECONSTRAINT: {
						use+=checkIntervalConstraint(stc.min,lvar.model,lvar.id,Constants.DISTANCECONSTRAINT);
						use+=checkIntervalConstraint(stc.max,lvar.model,lvar.id,Constants.DISTANCECONSTRAINT);
						break;
					}
					default: {
						break;
					}
				}
			}

			// This is a repeat
			if(lvar.repeatType!=0 && lvar.repeatQuantity!=null) {
				// This is a repeat
				// REMARK RepeatParam cannot be constrained
				//RepeatEntity
				Interval repeats = new Interval(lvar.repeatQuantity);
				use+=checkIntervalConstraint(repeats.x,lvar.model,lvar.id,Constants.REPEATCONSTRAINT);
				use+=checkIntervalConstraint(repeats.y,lvar.model,lvar.id,Constants.REPEATCONSTRAINT);
			}

			if(lvar.type==2) {
				// If this is a call to a view predicate, do not limit
			data = "( 1=1" + use + ")";
			}
			else {
			data = "((fixed("+lvar.id+")" + use + ");(\\+fixed("+lvar.id+")))";
			}


			if(!Constants.EMPTYSTRING.equals(save)) {data+= "," +  save; }

			if(Constants.EMPTYSTRING.equals(use)&&Constants.EMPTYSTRING.equals(save)) {
				// There no variable use
				data="(var(Dummy);\\+var(Dummy))";
			}


		}
		else {
			Model mod = lvar.mod;
			String model = mod.name+"(";
			if(mod.vars.size()==0) {
				model+="LOGOLVARDUMMY";
			}
			else {
				for(int j=0;j<mod.vars.size();j++) {
					if(j>0)  {data+=","; }
					model+=mod.vars.get(j);
				}
			}
			model+=")";
			return model;
		}

			return data;
	}


	/**
	 *  Checks in interval definitions if related variable constraint the analyse
	 * @param data
	 * @param model
	 * @param id
	 * @param type
	 * @return some prolog to add to preanalysis file
	 */
	private String checkIntervalConstraint(String data,String model,int id,int type) {
		String use="";
		if(data.equals(Constants.ANY)) {
			return use;
		}
		if(!Character.isDigit(data.charAt(0))) {
			String[] expression = data.split("\\s");
			// First character MUST be length constraint #
			/*  Fix interval constraint analysis where length of constraint can be > 1
			 *
			 * 	public static final String COSTSIGN = "$";
				public static final String DISTANCESIGN = "$$";
				public static final String LENGTHSIGN = "#";
				public static final String CONTENTSIGN = "?";
				public static final String BEGINSIGN = "@";
				public static final String ENDSIGN = "@@";
			 */
			String[] possibleConstraints = new String[] {Constants.CONTENTSIGN, Constants.DISTANCESIGN, Constants.COSTSIGN, Constants.LENGTHSIGN, Constants.ENDSIGN, Constants.BEGINSIGN};
			String variable = null;
			String varType = null;
			for(int i=0;i<possibleConstraints.length;i++) {
				if(expression[0].startsWith(possibleConstraints[i])) {
					variable = expression[0].substring(possibleConstraints[i].length());
					varType = expression[0].substring(0,possibleConstraints[i].length());
					break;
				}
			}
			if(variable==null) {
				logger.error("Could not found a matching constraint in Interval");
				return null;
			}
			if(!Constants.CONTENTSIGN.equals(varType)) {
			use += ",((\\+var("+variable+"));(var("+variable+"),"+variable+"='"+model+","+id+","+type+"'))";
			}
			else {
				// variable is like  ?SX.nboccur , must keep SX only
				String[] t_tmpExpr = variable.split("\\.");
				variable = t_tmpExpr[0];

				use += ",((\\+var("+variable+"));(var("+variable+"),"+variable+"='"+model+","+id+","+type+"'))";
			}
		}
		return use;
	}


	/**
	 * If ANY variable is set with a length constraint, set internal variable to use them at next variable treatment.
	 * @param lvar
	 * @throws GrammarException
	 */
	private void addAnyLengthConstraint(LogolVariable lvar) throws GrammarException {
		StringConstraint sc=null;
		String min="";
		String max="";
		 for(int i=0;i<lvar.stringConstraints.size();i++) {
			sc = (StringConstraint)lvar.stringConstraints.get(i);

			switch(sc.type) {

				case Constants.LENGTHCONSTRAINT: {
					min = sc.min;
					max = sc.max;
					Expression expr = new Expression(true).getExpressionData(min);
					isAnyMin = expr.variable;
					prologSentence+=expr.expression;
					//isAnyMin=getExpressionData(min);
					expr = new Expression(true).getExpressionData(max);
					isAnyMax = expr.variable;
					prologSentence+=expr.expression;
					//isAnyMax=getExpressionData(max);

					break;
				}
				default: {
					break;
				}

			}

		 }

	}


	/**
	 * Initialize some var information based on a first parsing.<br/>
	 * Set the equivalence between a var name and an id
	 * @param lvar
	 * @param countVar
	 */
	private void initVarInfo(LogolVariable lvar, int countVar) {

		if (lvar.name!=null && Constants.EXTERNAL.equals(lvar.name)) { return; }


		StringConstraint sc =null;
		for(int i=0;i<lvar.stringConstraints.size();i++) {
			sc = (StringConstraint)lvar.stringConstraints.get(i);

			switch(sc.type) {
				case Constants.SAVECONSTRAINT: {
					// Match X:{ _SX} , save instance as SX
					//if(LogolVariable.userVariables.get(sc.variableContent)==null) { LogolVariable.userVariables.put(sc.variableContent, Integer.toString(countVar)); }
					if(LogolVariable.userVariables.get(new VariableId(sc.variableContent,lvar.model))==null) { LogolVariable.userVariables.put(new VariableId(sc.variableContent,currentModel.name), Integer.toString(countVar)); }

					break;
				}
				default: {
					break;
				}
			}
		}



	}


	public static boolean isOptimalConstrainted() {
		return optimalConstraint;
	}


	public static void setOptimalConstraint(boolean constraint) {
		optimalConstraint = constraint;
	}

	/**
	 * If required, add the meta control check before saving matched variable
	 * @param controls List of control and related variables
	 * @param controlsDef Controls definition ( a>b etc...) using controls variables
	 * @return prolog code
	 */
	public static String checkMetaControls(HashMap<String,ArrayList<String>> controls, ArrayList<String> controlsDef) {
		// return ( ... ),
		if(controlsDef.size()>0) {
			metacontrols = controls;
			metacontrolsdefs = controlsDef;
			return "metacontrol(Z),";
		}
		else {
		return "";
		}
	}

	/**
	 * Get code to generate to manage meta controls
	 * @return prolog code
	 * @throws GrammarException
	 */
	public static String getMetaControls() throws GrammarException {
		String meta="";
		/*
		 *  metacontrol(Z):- foreach controls control = extractcontrol from list and operation ,
		 *                   foreach controlsDef
		 *
		 */
		if(metacontrolsdefs==null || metacontrolsdefs.size()==0)  {
			return meta;
		}
		meta+="metacontrol(Z):-";


		int count=0;
		for(String control : metacontrols.keySet()) {
			logger.debug("##META CONTROL "+control);
			if(count>0) meta+=",";
			count++;
			meta+=getMetaControl(control,metacontrols.get(control));
		}

		for(int i=0;i<metacontrolsdefs.size();i++) {
			meta+=","+metacontrolsdefs.get(i);
		}
		meta+=".\n";

		return meta;
	}


	/**
	 * Generates the code to get the prolog code setting the control variable value from the list of variables
	 * @param control name of the control variable
	 * @param arrayList Array with operator and list of variables
	 * @return prolog code to execute
	 * @throws GrammarException
	 */
	private static String getMetaControl(String control,
			ArrayList<String> arrayList) throws GrammarException {
		String result="";

		String operator = arrayList.get(0);
		for(int i=0;i<arrayList.size();i++) {
		logger.debug("META CONTROL VARS: "+arrayList.get(i));
		}

		result+="getControlVal(Z,"+control+",";
		if(operator.equals(Constants.BEGINSIGN)) { result+="1,[]";}
		else if(operator.equals(Constants.ENDSIGN)) { result+="2,[]"; }
		else if(operator.equals(Constants.LENGTHSIGN)) { result+="3,[]";}
		else if(operator.equals(Constants.COSTSIGN)) { result+="4,[]"; }
		else if(operator.equals(Constants.DISTANCESIGN)) { result+="5,[]"; }
		else if(operator.equals("p"+Constants.COSTSIGN)) { result+="6,[]"; }
		else if(operator.equals("p"+Constants.DISTANCESIGN)) { result+="7,[]"; }
		// Alphabet percentage
		else { result+="8,"+LogolUtils.getArray(operator.trim()); }
		result+=",[";
		for(int i=1;i<arrayList.size();i++) {
			if(i>1) { result+=","; }
			String[] var = arrayList.get(i).split("\\.");
			String logolvar =getVariableName(var[0],var[1]);
			if(logolvar==null) {
				throw new GrammarException("Error, this variable is not known: "+arrayList.get(i));
			}
			result+="'"+logolvar+"'";
		}
		result+="])";

		return result;
	}

	private static String getVariableName(String model, String name) {
		String val=null;

		Set<Integer> keys = varInfo.keySet();
		Iterator<Integer> it = keys.iterator();
		while(it.hasNext()) {
			Integer key = (Integer)it.next();
			LogolVariable lvar = (LogolVariable) varInfo.get(key);
			if(lvar.model.equals(model) && lvar.text.contains("_"+name)) {
				return Constants.LOGOLVAR+key;
			}

		}
		return val;
	}
}
