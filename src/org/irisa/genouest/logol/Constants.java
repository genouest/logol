package org.irisa.genouest.logol;

/**
 * Constants use in classes for Grammar analysis
 * @author osallou
 *
 * History:
 * 22/08/10 Fix 1664 Unicode issues with special chars
 */
public class Constants {
	
	//public static final String VERSION = "LOGOL INTERPRETER v1.1.1";

	public static final int VAR_ANALYSIS_STEP=1;
	protected static final int POSTCONDITION_ANALYSIS_STEP=2;
	protected static final int EXECUTE_STEP=3;
	
	public static final int DNA=0;
	public static final int RNA=1;
	public static final int PROTEIN=2;
	
	
	public static final String SEPARATOR = ",";
	public static final String FILESEPARATORPROPERTY = "file.separator";
	
	public static final String EMPTYSTRING="";
	
	public static final String ANY = "_";

	
	public static final String COSTSIGN = "$";
	public static final String DISTANCESIGN = "$$";
	public static final String LENGTHSIGN = "#";
	public static final String CONTENTSIGN = "?";
	public static final String BEGINSIGN = "@";
	public static final String ENDSIGN = "@@";
	public static final String EXTERNALSIGN = "external";
	
	
	
	public static final int CONTENTCONSTRAINT = 1;
	public static final int BEGINCONSTRAINT = 2;
	public static final int ENDCONSTRAINT = 3;
	public static final int LENGTHCONSTRAINT = 4;
	public static final int COSTCONSTRAINT = 5;
	public static final int PERCENTCOSTCONSTRAINT = 6;
	public static 	final int DISTANCECONSTRAINT = 7;
	public static 	final int PERCENTDISTANCECONSTRAINT = 8;
	public static 	final int SAVECONSTRAINT = 9;
	public static 	final int PARENTCONTENTCONSTRAINT = 10;
	public static 	final int REPEATCONSTRAINT = 11;	
	public static 	final int ALPHABETCONSTRAINT = 12;	
	


	public static 	final int MODIFIERCOST=100;
	public static 	final int MODIFIERMORPHPLUS=101;
	public static 	final int MODIFIERMORPHMINUS=102;

	public static 	final String LOGOLVAR="LogolVAR_";
	public static 	final String LOGOLVARREF=LOGOLVAR+"Reference_";
	public static 	final String LOGOLVARCONTENT=LOGOLVAR+"Content_";
	public static 	final String LOGOLVARBEGIN=LOGOLVAR+"Begin_";
	public static 	final String LOGOLVAREND=LOGOLVAR+"End_";
	public static 	final String LOGOLVARSIZE=LOGOLVAR+"Size_";
	public static 	final String LOGOLVARBEFORE=LOGOLVAR+"Before_";	
	public static 	final String LOGOLVARAFTER=LOGOLVAR+"After_";
	public static final String LOGOLVARPARENTREF =LOGOLVAR+ "Parent_Reference_";
	public static final String LOGOLVARSPACER =LOGOLVAR+ "Spacer_";
	public static final String LOGOLVARDATA =LOGOLVAR+ "Data_";
	public static final String LOGOLVARTMP =LOGOLVAR+ "Tmp_";
	public static final String LOGOLVARINFO =LOGOLVAR+ "Info_";
	public static final String LOGOLVARPARENT =LOGOLVAR+ "Parent_";
	public static final String LOGOLVARERRORS =LOGOLVAR+ "Errors_";
	public static final String LOGOLVARINDEL =LOGOLVAR+ "Indel_";
	public static final String LOGOLVARPOSITION =LOGOLVAR+ "Pos_";	
	
	public static final String LOGOLVARPRED = "logolPredicate_";
	public static final String LOGOLVARPOSTPONEPRED = "logolPostponePredicate_";
	
	public static final String POSTPONED = "Postponed";
	
	public static final String EXTERNAL = "external";
	
	public static final String SEQUENCELength="SEQUENCELength";
	
	public static final String INTER = "Inter_";
	
	// Repeat accessors
	public static final String NBOCCUR =".nboccur";
	public static final String MINDISTANCE =".minDistance";
	public static final String MAXDISTANCE =".maxDistance";
	public static final int DIR_ALL = 1;
	public static final int DIR_PLUS = 0;	
	
	
	public static final String OP_AND = ",";
	public static final String OP_OR = "|";
	public static final String OP_OVERLAP = ";";

	public static enum OPTIMAL_CONSTRAINT {
		OPTIMAL_NONE,
		OPTIMAL_LENGTH,
		/* Not yet supported
		OPTIMAL_COST,
		OPTIMAL_DIST,
		OPTIMAL_ERROR,
		OPTIMAL_BEGIN,
		OPTIMAL_END
		*/
	}
	
	public static enum OPTIMAL_MODE {
		MAX,
		MIN
	}
	
}
