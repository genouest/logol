package org.irisa.genouest.logol;

public class VariableId {
		public String name;
		public String model;
		public String type;
		
		public VariableId() {
			
		}
		
		public VariableId(String mod,String id) {
			this.name=id;
			this.model=mod;
			this.type="0";
		}
		
		public VariableId(String mod,String id,String typ) {
			this.name=id;
			this.model=mod;
			this.type=typ;
		}	
		
		
		public boolean equals(Object object){
			if(object instanceof VariableId){
			VariableId obj = (VariableId) object;
			if(obj.name.equals(this.name)&&obj.model.equals(this.model)&&obj.type.equals(this.type)) { return true; }
			else {  return false; }
			}
			else {  return false; }
		}
		
		public int hashCode() {			
			String code = name+"#"+model+"#"+type;
			return code.hashCode();
		}
		
}
