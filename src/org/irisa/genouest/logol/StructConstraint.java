package org.irisa.genouest.logol;

public class StructConstraint implements Constraint {

	public boolean neg=false;
	public int type=0;
	public String variableContent=null;
	public String contentConstraint=null;
	//public int distance=0;
	public String min=null;
	public String max=null;
	public String name=null;

	//Fix1683
	public String alphabetConstraint=null;
	
	public  Constants.OPTIMAL_CONSTRAINT optimal  = Constants.OPTIMAL_CONSTRAINT.OPTIMAL_NONE;
}
