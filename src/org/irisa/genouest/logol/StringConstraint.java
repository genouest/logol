package org.irisa.genouest.logol;

public class StringConstraint implements Constraint {
	public boolean neg=false;
	public int type=0;
	public String variableContent=null;
	public String contentConstraint=null;
	public String min=null;
	public String max=null;
	
	public  Constants.OPTIMAL_CONSTRAINT optimal  = Constants.OPTIMAL_CONSTRAINT.OPTIMAL_NONE;
	
	
}
