package org.irisa.genouest.logol.utils.model;
import java.util.HashMap;

/**
 * General usage functions or constants
 * @author osallou
 * @version 1.0
 * 
 * History:
 * 22/08/10 Fix 1664 Unicode issues with special chars
 *
 */

public class Commons {
	

	public static final String AND = ",";
	public static final String OVERLAP = ";";
	public static final String OR = "|";
	
	public static final String COSTSIGN = "$";
	public static final String DISTANCESIGN = "$$";
	public static final String LENGTHSIGN = "#";
	public static final String CONTENTSIGN = "?";
	public static final String BEGINSIGN = "@";
	public static final String ENDSIGN = "@@";
	public static final String SAVESIGN = "_";
	public static final String ANYSIGN = ".*";
	public static final String ALPHABETSIGN = "%";
	public static final String OPTIMALSIGN = "OPT";
	
	public static final String NEGATIVESIGN = "!";
	
	public static final String[] beginAccessors = new String[] { BEGINSIGN,ENDSIGN};
	public static final String[] endAccessors = new String[] { BEGINSIGN,ENDSIGN};
	public static final String[] sizeAccessors = new String[] { LENGTHSIGN};
	
	public static final String[] costAccessors = new String[] { COSTSIGN, DISTANCESIGN};
	public static final String[] distAccessors = new String[] { COSTSIGN, DISTANCESIGN};
	
	public static final String[] contentAccessors = new String[] { CONTENTSIGN};
	public static final String[] saveAccessors = new String[] { SAVESIGN };
	
	public static final String[] spacerAccessors = new String[] { LENGTHSIGN };
	public static final String[] repeatAccessors = null;
	
	// Variable info
	public static final String LOGOLVAR = "LOGOLVAR";
	public static final String LOGOLMOD = "mod";	
	public static final String PLUS = "+";
	public static final String MINUS = "-";
	
	public static final String label = "label";
	public static final String description = "description";
	public static final String overlap = "overlap";
	public static final String parent = "data";
	public static final String params = "parameters";
	public static final String name = "name";
	public static final String morphism = "morphism";
	public static final String not_data = "negdata";
	public static final String data = "data";
	
	public static final String in = "in";
	public static final String out = "out";
	
	
	// String constraints
	public static final String not_begin = "neg_begin";
	public static final String stg_begin = "stg_begin";
	public static final String not_end = "neg_end";
	public static final String stg_end = "stg_end";
	public static final String not_size = "neg_size";
	public static final String stg_size = "stg_size";
	public static final String stg_optimalsize = "stg_optimalsize";
	// not implemented public static final String not_content = "neg_content";
	public static final String stg_content = "stg_content";
	public static final String stg_save = "stg_save";
	
	// Struc constraints
	public static final String not_cost = "neg_cost";
	public static final String stc_cost = "stc_cost";
	public static final String not_dist = "neg_dist";
	public static final String stc_dist = "stc_dist";
	
	public static final String stc_alphabet = "stc_alphabet";
	public static final String stc_alphabetpercent = "stc_alphabetpercent";
	
	
	
	// Repeat constraint
	public static final String spacer = "spacer";
	public static final String nbrepeat = "nb_repeat";	
	
	static int modelCounter=0;
	static int varCounter=0;
	
	static HashMap<String,String> parentVarMap = new HashMap<String,String>();
	static HashMap<String,String> varMap = new HashMap<String,String>();
	static HashMap<String,String> modelMap = new HashMap<String,String>();
	
	/**
	 * Reset variable maps
	 */
	public static void reset() {
		varMap = new HashMap<String,String>();
		parentVarMap = new HashMap<String,String>();
	}
	
	/**
	 * Get a counter value for the models. Counter is incremented at each call.
	 * @return the counter value
	 */
	public static int getModelCounter() {
		modelCounter++;
		return modelCounter;
	}
	/**
	 * Get a counter value for the variables. Counter is incremented at each call.
	 * @return the counter value
	 */
	public static int getVarCounter() {
		varCounter++;
		return varCounter;
	}
}
