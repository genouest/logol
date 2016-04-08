package org.irisa.genouest.logol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;



public class LogolVariable {
	
	private static final Logger logger = Logger.getLogger(org.irisa.genouest.logol.LogolVariable.class);
	
	// Defines models called as primary models in query
	public static HashMap<String,Boolean> mainModels = new HashMap<String,Boolean>();		
	
	// Name vs Parser internal reference (example: X  vs 5 (eg logolVar_Reference_5) )
	public static HashMap<VariableId,String> userVariables = new HashMap<VariableId,String>();
	
	// Variables defined as input/output in call parameters
	public static HashMap<VariableId,String> paramVariables = new HashMap<VariableId,String>();
	
	// Intermediates variables defined as input/output in model parameters call
	public static Vector<VariableId> tmpVariables = new Vector<VariableId>();
	
	public static HashMap<VariableId,String> parentVariables = new HashMap<VariableId,String>();
	
	public static Vector<String> matchedVariables = new Vector<String>();
	
	public static Vector<LogolVariable> postponedVariables = new Vector<LogolVariable>();
	
	public static Vector<VariableId> overlappedVariables = new Vector<VariableId>();
		
	/**
	 * Defines after pre-analyse, where a variable is defined compared to its used. If not present, means that var is already defined when called
	 */
	public static HashMap<VariableId,VariableId> constrainedVariables = new HashMap<VariableId,VariableId>();
	
	//public static Vector useParent = new Vector();
	
	// List of variables defined (logolVar_X).
	public static Vector<Integer> variables = new Vector<Integer>();
	
	
	/**
	 * Records all variable info for all models. Vector is filled in an analysis step, before treatment
	 */
	public static HashMap<String,LogolVariable> varData = new HashMap<String,LogolVariable>();
	
    // Defines if test positive or negative condition
	/**
	 * 0 = fixed
	 * 1 = variable
	 * 2 = group
	 * 3 = model
	 */
	public int type=0;
	public boolean neg=false;
	public String name=null;
	public int id=0;
	public String fixedValue=null;
	public String content=null;
	public String saveAs=null;
	@SuppressWarnings("unchecked")
	public Vector<StringConstraint> stringConstraints = new Vector<StringConstraint>();
	public Vector<StructConstraint> structConstraints = new Vector<StructConstraint>();
	// Interval
	public String repeatQuantity=null;
	/**
	 * Repeat type defines the test to apply between repeats
	 * 0 = undefined
	 * 1 = AND
	 * 2 = OVERLAP
	 */
	public int repeatType=0;
	// Interval
	public String repeatParam=null;		
	// External:
	public String extra=null;

	public Vector<String> externalIn=new Vector<String>();

	public Vector<String> externalOut= new Vector<String>();
	
	public Modifier modifier=null;
	
	public String parentId="0";
	
	public String model="";
	
	public String text="";
	// Model
	public Model mod=null;
	
	// reference to a linked variable, used in postponed variable treatments
	public String referenceModel=null;
	public String referenceID="0";
	
	public boolean isParent=false;
	
	/*
	 * Specific sub data required for repeat variables
	 * -1: undefined
	 * 0: number of occur
	 * 1: minimum distance
	 * 2: maximum distance
	 */
	public int dataType=-1;
	
	/**
	 * By default, no optimal constraint is set on variable
	 */
	Constants.OPTIMAL_CONSTRAINT optimalConstraint = Constants.OPTIMAL_CONSTRAINT.OPTIMAL_NONE;
	Constants.OPTIMAL_MODE optimalMode = Constants.OPTIMAL_MODE.MAX;
	
	public static void reset() {
		mainModels = new HashMap<String,Boolean>();		
		userVariables = new HashMap<VariableId,String>();
		paramVariables = new HashMap<VariableId,String>();
		parentVariables = new HashMap<VariableId,String>();
		matchedVariables = new Vector<String>();
		postponedVariables = new Vector<LogolVariable>();
		overlappedVariables = new Vector<VariableId>();
		constrainedVariables = new HashMap<VariableId,VariableId>();
		variables = new Vector<Integer>();
	}
	
	
	
	
		/**
		 * Copy input LogolVariable main attributes, without the constraints.
		 * 
		 */
		public LogolVariable copyInfo() {
			
			LogolVariable tmpVar = new LogolVariable();
			
			tmpVar.type=this.type;
			tmpVar.neg=this.neg;
			tmpVar.name=this.name;
			tmpVar.id=this.id;
			tmpVar.fixedValue=this.fixedValue;
			tmpVar.content=this.content;
			tmpVar.saveAs=null;
			tmpVar.stringConstraints = new Vector<StringConstraint>();
			tmpVar.structConstraints = new Vector<StructConstraint>();			
			tmpVar.repeatQuantity=null;
			tmpVar.repeatType=0;
			tmpVar.repeatParam=null;		
			tmpVar.extra=null;
			tmpVar.modifier=this.modifier;			
			tmpVar.parentId=this.parentId;			
			tmpVar.model=this.model;			
			tmpVar.text="";
			tmpVar.mod=this.mod;
			tmpVar.referenceModel=this.referenceModel;
			tmpVar.referenceID=this.referenceID;			
			tmpVar.isParent=this.isParent;	
			
			return tmpVar;
		}
	
		public LogolVariable() {
			
		}	
		public LogolVariable(String mod,String id,String parentModel,String parentName,StringConstraint sc) {
			this.model=mod;
			this.id=Integer.parseInt(id);
			this.referenceModel=parentModel;
			this.referenceID=parentName;
			this.stringConstraints.add(sc);
		}

		public LogolVariable(String mod,String id,String parentModel,String parentName,StructConstraint sc) {
			this.model=mod;
			this.id=Integer.parseInt(id);
			this.referenceModel=parentModel;
			this.referenceID=parentName;
			this.structConstraints.add(sc);
		}		
		
		public LogolVariable(String mod,String id,String parentModel,String parentName,Modifier modif) {
			this.model=mod;
			this.id=Integer.parseInt(id);
			this.referenceModel=parentModel;
			this.referenceID=parentName;
			this.modifier=modif;
		}		
		
		public boolean hasConstraint(int constraintType) {
			if(constraintType== Constants.REPEATCONSTRAINT && this.repeatQuantity!=null) {
				return true;
			}
			StringConstraint sc1=null;
			for(int i=0;i<stringConstraints.size();i++){
				sc1 = (StringConstraint) stringConstraints.get(i);
				if(sc1.type==constraintType) { return true;}
			}
			StructConstraint sc2=null;
			for(int i=0;i<structConstraints.size();i++){
				sc2 = (StructConstraint) structConstraints.get(i);
				if(sc2.type==constraintType) { return true;}
			}			
			return false;
		}

		
		public StringConstraint getStringConstraint(int constraintType) {
			StringConstraint sc1=null;
			for(int i=0;i<stringConstraints.size();i++){
				sc1 = (StringConstraint) stringConstraints.get(i);
				if(sc1.type==constraintType) { break;}
			}

			return sc1;
		}		
		
		public StructConstraint getStructConstraint(int constraintType) {
			StructConstraint sc1=null;
			for(int i=0;i<structConstraints.size();i++){
				sc1 = (StructConstraint) structConstraints.get(i);
				if(sc1.type==constraintType) { break;}
			}
			return sc1;
		}	
		
		public boolean hasPostponedConstraint(int constraintType) {
			if(LogolVariable.constrainedVariables.containsKey(new VariableId(this.model,String.valueOf(this.id),String.valueOf(constraintType)))) {
				return true;
			}
			return false;
		}
		
		/**
		 * Checks if constraint treatment must be postponed according to previous analysis.
		 * If true, copy the variable and add it to postponedVariables.
		 * @param constraintType	Type of the constraint
		 * @param sc	constraint to postponed
		 * @return true if must be postponed.
		 */
		public boolean postpone(int constraintType, StringConstraint sc) {			
			if(LogolVariable.constrainedVariables.containsKey(new VariableId(this.model,String.valueOf(this.id),String.valueOf(constraintType)))) {
				LogolVariable ltmp = this.copyInfo();
				ltmp.stringConstraints.add(sc);
				VariableId var = LogolVariable.constrainedVariables.get(new VariableId(this.model,String.valueOf(this.id),String.valueOf(constraintType)));
				ltmp.referenceID=var.name;
				ltmp.referenceModel=var.model;
				// If there is a modifier and it is a content modifier, postpone it too
				if(constraintType==Constants.CONTENTCONSTRAINT && this.modifier!=null) { ltmp.modifier=this.modifier; }
				postponedVariables.add(ltmp);
				logger.debug("postpone treatment for "+this.model+":"+this.id+":"+sc.type+" saved at "+ltmp.referenceModel+":"+ltmp.referenceID);
				return true;
			} else { return false; }
		}

		/**
		 * Checks if constraint treatment must be postponed according to previous analysis.
		 * If true, copy the variable and add it to postponedVariables.
		 * @param constraintType	Type of the constraint
		 * @param sc	constraint to postponed
		 * @return true if must be postponed.
		 */
		public boolean postpone(int constraintType, StructConstraint sc) {			
			if(LogolVariable.constrainedVariables.containsKey(new VariableId(this.model,String.valueOf(this.id),String.valueOf(constraintType)))) {
				LogolVariable ltmp = this.copyInfo();
				ltmp.structConstraints.add(sc);
				VariableId var = LogolVariable.constrainedVariables.get(new VariableId(this.model,String.valueOf(this.id),String.valueOf(constraintType)));
				ltmp.referenceID=var.name;				
				postponedVariables.add(ltmp);
				ltmp.referenceModel=var.model;
				logger.debug("# postpone treatment for "+this.model+":"+this.id+":"+sc.type+" saved at "+ltmp.referenceModel+":"+ltmp.referenceID);
				return true;			
			} else { return false; }
		}	
		
		/**
		 * Checks for the postpone of checks for repeat quantity
		 */
		public boolean repeatPostpone() {
			if(LogolVariable.constrainedVariables.containsKey(new VariableId(this.model,String.valueOf(this.id),String.valueOf(Constants.REPEATCONSTRAINT)))) {
			logger.debug("# postpone treatment for "+this.model+":"+this.id+": repeat quantity");
				LogolVariable ltmp = this.copyInfo();
				ltmp.repeatQuantity = this.repeatQuantity;
				LogolVariable.postponedVariables.add(ltmp);
				return true;
			}
			else { return false; }
		}			
		
		/**
		 * 	Force a constraint to be postponed, no check
		 * @param sc	String constraint to be postponed
		 * @param type  Type of forcing constraint
		 */
		public void forcepostpone(StringConstraint sc, int type) {
			logger.debug("# postpone treatment for "+this.model+":"+this.id+":"+sc.type);
				LogolVariable ltmp = this.copyInfo();
				VariableId var = LogolVariable.constrainedVariables.get(new VariableId(this.model,String.valueOf(this.id),String.valueOf(type)));
				ltmp.referenceID=var.name;
				ltmp.referenceModel=var.model;
				ltmp.stringConstraints.add(sc);
				LogolVariable.postponedVariables.add(ltmp);

		}
		
		/**
		 * 	Force a constraint to be postponed, no check
		 * @param sc	String constraint to be postponed
		 */
		public void forcepostpone(StringConstraint sc) {
			logger.debug("# postpone treatment for "+this.model+":"+this.id+":"+sc.type);
				LogolVariable ltmp = this.copyInfo();
				ltmp.stringConstraints.add(sc);
				LogolVariable.postponedVariables.add(ltmp);

		}		
		

		/**
		 * 	Force a constraint to be postponed, no check
		 * @param sc	Struct constraint to be postponed
		 * @param type  Type of forcing constraint
		 */
		public void forcepostpone(StructConstraint sc, int type) {
			logger.debug("# postpone treatment for "+this.model+":"+this.id+":"+sc.type);
				LogolVariable ltmp = this.copyInfo();
				VariableId var = LogolVariable.constrainedVariables.get(new VariableId(this.model,String.valueOf(this.id),String.valueOf(type)));
				ltmp.referenceID=var.name;
				ltmp.referenceModel=var.model;
				ltmp.structConstraints.add(sc);
				LogolVariable.postponedVariables.add(ltmp);

		}		
		
		/**
		 * 	Force a constraint to be postponed, no check
		 * @param sc	Struct constraint to be postponed
		 */
		public void forcepostpone(StructConstraint sc) {
			logger.debug("# postpone treatment for "+this.model+":"+this.id+":"+sc.type);
				LogolVariable ltmp = this.copyInfo();
				ltmp.structConstraints.add(sc);
				LogolVariable.postponedVariables.add(ltmp);
		}

		
		
		
		/**
		 * List all postponed data for the current model, including other models (with their own postponed data) called in current model
		 * @param model current model name
		 * @return a List [ [A,B,C,D], [E,F,G,H],... ]
		 */
		public static String getPostponedVariableList(String model) {

			boolean first=true;
			LogolVariable lvar=null;
			String list="[";
			String data="";
			// List all postponed data for the current model
			Iterator<LogolVariable> it = postponedVariables.iterator();
			while(it.hasNext())  {
				lvar = (LogolVariable) it.next();
				if(lvar.model.equals(model)) {
					//data = "[ "+ lvar.referenceID;
					data = "[ "+ lvar.id;
					//add type of postpone eg begin,end,content
					data += ",";
					if(lvar.hasConstraint(Constants.BEGINCONSTRAINT)) {
						data+=Constants.BEGINCONSTRAINT;
					}
					if(lvar.hasConstraint(Constants.ENDCONSTRAINT)) {
						data+=Constants.ENDCONSTRAINT;
					}
					if(lvar.hasConstraint(Constants.CONTENTCONSTRAINT)) {
						data+=Constants.CONTENTCONSTRAINT;
					}	
					if(lvar.hasConstraint(Constants.LENGTHCONSTRAINT)) {
						data+=Constants.LENGTHCONSTRAINT;
					}
					if(lvar.hasConstraint(Constants.REPEATCONSTRAINT)) {
						data+=Constants.REPEATCONSTRAINT;
					}
					if(lvar.hasConstraint(Constants.COSTCONSTRAINT)) {
						data+=Constants.COSTCONSTRAINT;
					}					
					if(lvar.hasConstraint(Constants.DISTANCECONSTRAINT)) {
						data+=Constants.DISTANCECONSTRAINT;
					}	
					if(lvar.hasConstraint(Constants.PERCENTCOSTCONSTRAINT)) {
						data+=Constants.PERCENTCOSTCONSTRAINT;
					}					
					if(lvar.hasConstraint(Constants.PERCENTDISTANCECONSTRAINT)) {
						data+=Constants.PERCENTDISTANCECONSTRAINT;
					}					
					// ***********************
					data +=","+Constants.LOGOLVARREF+lvar.id;
					
					data+=","+Constants.LOGOLVARREF+lvar.referenceID+"]";
					
					if(first) {  first=false; }
					else {
						list+=",";
					}
					
					list+=data;
				}
			}
				
			Model mod=null;
			// concat models vars except current
			for(int i=0;i<Treatment.models.size();i++) {
				mod = Treatment.models.get(i);
				// If not the current model
				if(!mod.name.equals(model)) {
					if(first) {  first=false; }
					else {
						list+=",";
					}
					list += mod.name.toUpperCase()+"_"+Constants.POSTPONED;
				}
			}
				
			list+="]";
			
				
			return list;
		}

		public static String getModel(int id) {
			LogolVariable lvar = (LogolVariable) varData.get(String.valueOf(id));
			if(lvar!=null) {
				return lvar.model;
			}
			return null;
		}




		public Constants.OPTIMAL_CONSTRAINT getOptimalConstraint() {
			return optimalConstraint;
		}




		public void setOptimalConstraint(Constants.OPTIMAL_CONSTRAINT optimalConstraint) {
			this.optimalConstraint = optimalConstraint;
		}
		
		/**
		 * Checks if a variable is constrained with an optimal constraint
		 * @return
		 */
		public boolean isOptimalConstrainted(){
			if(this.optimalConstraint!=Constants.OPTIMAL_CONSTRAINT.OPTIMAL_NONE) {
				return true;
			}
			else {
				return false;
			}
		}




		public Constants.OPTIMAL_MODE getOptimalMode() {
			return optimalMode;
		}




		public void setOptimalMode(Constants.OPTIMAL_MODE optimalMode) {
			this.optimalMode = optimalMode;
		}


		public String getConfig() {
			String config = "[";
			// For future use (debug?)
			config+="0";
			if(this.isOptimalConstrainted()) {
				config+=",1";
			}
			else {
				config+=",0";
			}
			config+="]";
			return config;
		}
		
				
}
