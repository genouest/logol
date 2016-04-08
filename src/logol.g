grammar logol;
options {
	language=Java;
	backtrack=true;
}



@header {
package org.irisa.genouest.logol.parser;


import java.util.HashMap;
import java.util.Vector;
import java.util.ArrayList;
import org.irisa.genouest.logol.*;
}

@lexer::header {
package org.irisa.genouest.logol.parser;
 }

@members {

/** Map variable name to Integer object holding value */
//HashMap variables = new HashMap();
// Use LogolVariable.variables HashMap userVariables = new HashMap();
HashMap sequenceFiles = new HashMap();

HashMap logolModels = new HashMap();

Vector predicates = new Vector();

static boolean newModel=false;

HashMap macroControls = new HashMap();

ArrayList macroControlsDefs = new ArrayList();

HashMap getMacroControls() {
  return macroControls;
}

ArrayList getMacroControlsDefs() {
  return macroControlsDefs;
}

int control=0;

int expr=0;
}

/**
*	Rules definition
*
*/
	

rule @init {
		Treatment.counters.clear();
		LogolVariable.matchedVariables.clear();
		logolModels.clear();
		Treatment.definitions.clear();
		Treatment.models.clear();
		Treatment.query="";
	 }
	@after {
		
	}
	:	definitions? controls? (LINE_COMMENT|model|query|NEWLINE)+  EOF;


LINE_COMMENT
  :  '//' ~( '\r' | '\n' )* 
  ;
	 
controls @init { control=0; macroControlsDefs = new ArrayList(); }
     : 'controls:' NEWLINE? '{' NEWLINE (LINE_COMMENT|control)* '}' NEWLINE;

//control: matchexp=mathexpr negation='!'? COMPARATOR matchexp2=mathexpr { String controlText = $matchexp.value+" "+$COMPARATOR.text+" "+$matchexp2.value; if($negation!=null) { controlText = "\\+ "+controlText; } macroControlsDefs.add(controlText);} NEWLINE;
control: matchexp=mathexpr negation='!'? COMPARATOR matchexp2=mathexpr { String comparator; int count1=++expr; int count2=++expr; if($COMPARATOR.text.equals(" = ")) { comparator="is"; } else { comparator = $COMPARATOR.text; } String controlText = "MATHEXPR"+count1+" is ("+$matchexp.value+"), MATHEXPR"+count2+" is ("+$matchexp2.value+"), MATHEXPR"+count1+comparator+"MATHEXPR"+count2; if($negation!=null) { controlText = "\\+ "+controlText; } macroControlsDefs.add(controlText);} NEWLINE;

mathexpr returns [String value]
    :   e=mathatom {$value = $e.value;}
        (   '+' e=mathatom {$value += " + "+$e.value;}
        |   '-' e=mathatom {$value += " - "+$e.value;}
        |   '*' e=mathatom {$value += " * "+$e.value;}
        |   '/' e=mathatom {$value += " / "+$e.value;}
        )*
    ;

modvar returns [String value]: ModelName '.' VARIABLE {$value =  $ModelName.text+"."+ $VARIABLE.text;};

mathatom returns [String value] 
	@init{ 
	ArrayList controlVars = new ArrayList();
	 } 
    :   first=INT ('.' second=INT)? {$value = $first.text; if(($second!=null) && (!$second.text.equals(""))) { $value += "."+$second.text;} }
//  CONSTRAINT: ( '@@' | '@' | '#' | '$$' | '$' | 'p$$' | 'p$' | '%' '"'  LOWID '"' );
    | op='@@' { controlVars.add($op.text); } '[' modvar1=modvar { controlVars.add($modvar1.value);} ( ',' modvar2=modvar { controlVars.add($modvar2.value); })* ']' { $value = "Control"+control; macroControls.put($value,controlVars); control++; }
    | op='@' { controlVars.add($op.text); } '[' modvar1=modvar { controlVars.add($modvar1.value);} ( ',' modvar2=modvar { controlVars.add($modvar2.value); })* ']' { $value = "Control"+control; macroControls.put($value,controlVars); control++; }
    | op='#' { controlVars.add($op.text); } '[' modvar1=modvar { controlVars.add($modvar1.value);} ( ',' modvar2=modvar { controlVars.add($modvar2.value); })* ']' { $value = "Control"+control; macroControls.put($value,controlVars); control++; }
    | op='$$' { controlVars.add($op.text); } '[' modvar1=modvar { controlVars.add($modvar1.value);} ( ',' modvar2=modvar { controlVars.add($modvar2.value); })* ']' { $value = "Control"+control; macroControls.put($value,controlVars); control++; }
    | op='$' { controlVars.add($op.text); } '[' modvar1=modvar { controlVars.add($modvar1.value);} ( ',' modvar2=modvar { controlVars.add($modvar2.value); })* ']' { $value = "Control"+control; macroControls.put($value,controlVars); control++; }
    | op='p$$' { controlVars.add($op.text); } '[' modvar1=modvar { controlVars.add($modvar1.value);} ( ',' modvar2=modvar { controlVars.add($modvar2.value); })* ']' { $value = "Control"+control; macroControls.put($value,controlVars); control++; }
    | op='p$' { controlVars.add($op.text); } '[' modvar1=modvar { controlVars.add($modvar1.value);} ( ',' modvar2=modvar { controlVars.add($modvar2.value); })* ']' { $value = "Control"+control; macroControls.put($value,controlVars); control++; }
    | ('%' '"' alphabet=LOWID { controlVars.add("\""+$alphabet.text+"\""); } '"') '[' modvar1=modvar  { controlVars.add($modvar1.value);} ( ',' modvar2=modvar { controlVars.add($modvar2.value); })* ']' { $value = "Control"+control; macroControls.put($value,controlVars); control++; }
    |   '(' e=mathexpr ')' {$value = '(' + $e.value + ')';}
    ;

model   @init{ Model mod = new Model();
		
	 } 
	@after{ /*if($vars!=null) {	
		for(int i=0;i<$vars.size();i++) {
			org.antlr.runtime.CommonToken token = (org.antlr.runtime.CommonToken) $vars.get(i);
			mod.vars.add(token.getText());			
		}
		}*/
		mod.predicate=$myview.value;
		mod.text=$myview.text;	
				
		Treatment.models.add(mod);	
		logolModels.put(mod.name,mod);
		
					
	}	
	:	curmod=ModelName { mod.name=$curmod.text; mod.name=mod.name.replaceAll("\%\\w+\%", ""); Treatment.currentModel=mod; newModel=true; Boolean isPrimaryModel  = LogolVariable.mainModels.get(mod.name); /* If model is a primary model, search is not constrained to the start of the sequence  */ if(isPrimaryModel!=null) { Treatment.isAny=0; Treatment.saveAny=false; Treatment.isAnyMax = "SEQUENCELength";} } '(' vars+=VARIABLE?  (',' vars+=VARIABLE )* ')' { if($vars!=null) {	for(int i=0;i<$vars.size();i++) { org.antlr.runtime.CommonToken token = (org.antlr.runtime.CommonToken) $vars.get(i); mod.vars.add(token.getText()); Treatment.currentModel=mod;}} } '==>'  myview=view[0];


ModelName returns [ String name]  
	:	'mod' INT ('%'('a'..'z'|'A'..'Z'|'0'..'9')+'%')? { $name="mod"+$INT.text; };

view [int parentId]  returns [String value] 
	@init {
		$value="";
		String operator;
		Entity curView = new Entity();
		int tmpcounter=0;
		String tmpdata="";
		int tmpIsAny = Treatment.isAny;
		String tmpIsAnyMin = Treatment.isAnyMin;
		String tmpIsAnyMax = Treatment.isAnyMax;				
		boolean tmpSaveAny = Treatment.saveAny;
		
	}
	@after {
		$value=tmpdata;
		tmpcounter = (Integer) Treatment.counters.get(parentId); tmpcounter++; Treatment.counters.put(parentId,tmpcounter);
	}

	:	( ( op=(',' | ';'  | '|')? { if($op!=null && !$op.text.equals(Constants.OP_OR)) { tmpIsAny = Treatment.isAny; tmpIsAnyMin = Treatment.isAnyMin; tmpIsAnyMax = Treatment.isAnyMax; tmpSaveAny = Treatment.saveAny; tmpcounter = (Integer) Treatment.counters.get(parentId); tmpcounter++; Treatment.counters.put(parentId,tmpcounter); }} ( ent=constrainedEntity[parentId,tmpIsAny,tmpIsAnyMin,tmpIsAnyMax,tmpSaveAny]  ) ){ if($op==null) {operator=null; tmpdata+=curView.add($ent.predicate,operator,0,tmpcounter); /*tmpcounter = (Integer) counters.get(parentId); tmpcounter++; counters.put(parentId,tmpcounter);*/ } else { operator=$op.text; if(!operator.equals("|")) { /*tmpcounter = (Integer) counters.get(parentId); tmpcounter++; counters.put(parentId,tmpcounter);*/ tmpdata+=curView.add($ent.predicate,operator,0,tmpcounter); /*tmpcounter = (Integer) counters.get(parentId); tmpcounter++; counters.put(parentId,tmpcounter);*/ } else { tmpdata+=curView.add($ent.predicate,operator,0,tmpcounter);}}; } )+   ;



query	@after{
		org.antlr.runtime.CommonToken tokenVar=null;
		int varcount=0;
		if(Treatment.getParseStep()==2) {
		// Pre analysis step
		
		String result="Z=[";
		String query="query(Z):-";
		int i;
		Model model=null;
		org.antlr.runtime.CommonToken token=null;

		for(i=0;i<$models.size();i++) {
		if(i>0) query+=",";
		token = (org.antlr.runtime.CommonToken) $models.get(i);
		model =(Model)logolModels.get(token.getText());
		// Define the model as a primary model
		LogolVariable.mainModels.put(token.getText(),new Boolean(true));
		//Treatment.models.add(model);
		if((model.vars!=null) && (model.vars.size()==0)) {
		//query+=model.name+"(Z"+i+")";
		query+=model.name+"(LOGOLVARDUMMY)";
		}
		else {
		query+=model.name+"(";
			tokenVar = (org.antlr.runtime.CommonToken) $vars.get(varcount);
			varcount++;				
			query+=tokenVar.getText();
			if(i>0) result+=",";
			result+=tokenVar.getText();
			
			for(int j=1;j<model.vars.size();j++) {
			tokenVar = (org.antlr.runtime.CommonToken) $vars.get(varcount);
			varcount++;
			query+=","+tokenVar.getText();
			result+=","+tokenVar.getText();
			}
		
		
		query+=")";
		
		
		}
				
		}
		result+="].";
		Treatment.query=query+","+result;		
		
		}
		
		
		// Last step
		if(Treatment.getParseStep()==3) {
		varcount=0;
		// Execute query(Z) with Z = read sequence
		
		//String query="query(Z):- retractall(varDefinition(_,_,_,_,_, _, _, _)),bb_get('seq1',Sequence)";
		String query="query(Z):- retractall(varDefinition(_,_,_,_,_, _, _, _))";
		
		int i;
		Model model=null;
		org.antlr.runtime.CommonToken token=null;
		for(i=0;i<$models.size();i++) {
		query+=",";
		token = (org.antlr.runtime.CommonToken) $models.get(i);
		model =(Model)logolModels.get(token.getText());
		
		//Treatment.models.add(model);
		if((model.vars!=null) & (model.vars.size()==0)) {
		query+=model.name+"(0,[],_,_"+","+model.name.toUpperCase()+",_,_)";
		//query+=model.name+"(Sequence,[],_,_"+","+model.name.toUpperCase()+",_,_)";
		}
		else {
		//query+=model.name+"(Sequence,[],_,";
		query+=model.name+"(0,[],_,";			
			tokenVar = (org.antlr.runtime.CommonToken) $vars.get(varcount);
			varcount++;				
			query+=tokenVar.getText();

			
			for(int j=1;j<model.vars.size();j++) {
			tokenVar = (org.antlr.runtime.CommonToken) $vars.get(varcount);
			varcount++;
			query+=","+tokenVar.getText();

						
			}
		
		
		query+=",_"+","+model.name.toUpperCase()+",_,_)";
		
		}
		}
		
		query+=",Z=[";
		for(i=0;i<$models.size();i++) {
			token = (org.antlr.runtime.CommonToken) $models.get(i);			
			model =(Model)logolModels.get(token.getText());
			if(i>0) query+=",";
			query+=model.name.toUpperCase();
		}
		query+="],outputfile(ResFile),";
		if(Logol.singlesOnly==true) {
		query+="matchExist('"+Logol.getFilterType()+"',Z),";
		}
		query+=Treatment.checkMetaControls(getMacroControls(),getMacroControlsDefs());
		query+="(config(Config),nth0(1,Config,USEOPTIMAL),(USEOPTIMAL=0->writeallresult(ResFile,Z);(getCounter(NEWID),parseResults(Z,OPTVARS),isMax(Z,OPTVARS,NEWID)))).";
		Treatment.query=query;
		}
		
		
		//System.out.println(query);
		}
	:	models+=ModelName '('(vars+=VARIABLE)? (',' vars+=VARIABLE)* ')'  ('.' models+=ModelName '('(vars+=VARIABLE)? (',' vars+=VARIABLE)* ')')* '==*>' seq=Sequence ('.' sequences)*;


sequences
	:	 Sequence | '(' Sequence ('.' Sequence)* ')';

Sequence:	 'SEQ' INT ;
//sequence:	SequenceName '=' SequenceFileName;
SequenceDef 
	:	'SEQ' INT ',' ANY '.fsa'  ;

definitions 
	:	'def:' NEWLINE? '{' ((def=definition {Treatment.definitions.add($def.content);} NEWLINE)|LINE_COMMENT|NEWLINE)* '}' NEWLINE ;
	
definition returns [String content]	:
			 ('matrix' '(' id1=LOWID ',' id2=LOWID ',' mat=matrix ')')  { $content="matrix('"+$id1.text+"','"+$id2.text+"',"+$mat.text+")"; }
			| ( 'morphism' '(' id1=LOWID ',' id2=LOWID ',' id3=LOWID ')' ) { $content="morphism('"+$id1.text+"','"+$id2.text+"','"+$id3.text+"')"; };


matrix	:	'[' INT (',' INT)* ']' ;

constrainedEntity [int parentId, int tmpIsAny, String tmpIsAnyMin,String tmpIsAnyMax,boolean tmpSaveAny] returns [String predicate]  
	@init { LogolVariable lvar=null; String pretreatment="";
		int curCount = (Integer) Treatment.counters.get(parentId);
		Treatment.isAny = tmpIsAny;
		Treatment.isAnyMin = tmpIsAnyMin;
		Treatment.isAnyMax = tmpIsAnyMax;
		Treatment.saveAny = tmpSaveAny;
		// If it is not preanalysis step
		if(Treatment.getParseStep()!=2) {
		// FIX remove parentId test for internal predicates
		//if(parentId==0) {
			 if(curCount>0 & !newModel) {
			 	/*
			 	* set Constants.LOGOLVAR_Before_i to value Constants.LOGOLVAR_After_(i-1), e.g. preceding remaining list
			 	*/				 		
			 	pretreatment = "("+Constants.LOGOLVARBEFORE+Integer.toString(curCount)+"="+Constants.LOGOLVARAFTER+Integer.toString(curCount-1)+",";

			 	}
			 else { 
			 	
			 	if(newModel) newModel=false;
			 	// Save length of whole sequence
				// pretreatment = "sequenceData(_,SEQUENCELength),";
			 	/*
			 	* set Constants.LOGOLVAR_Before_i to value whole sequence
			 	*/					 			 
				 pretreatment += "("+Constants.LOGOLVARBEFORE+Integer.toString(curCount)+"= Input,";

			 }
		//	 }
		//else { pretreatment ="("; }
		
		}
		else { pretreatment ="("; }
		
		
	
	
	 } 
	@after{ 
			 /*
			 * Get treatment to apply to this variable
			 */
			 
			 lvar.parentId=Integer.toString(parentId);
			 String treatment= new Treatment().get(lvar,curCount);
			 String posttreatment = ")";
			$predicate = pretreatment+"\n"+treatment+"\n"+posttreatment+"\n";
			 
			   
		
			   
			 
	}	 
	: lentity=entity[parentId] { lvar = $lentity.lvar; lvar.text = $lentity.text; } (':' '{' stg=stringConstraints {if($stg.text!=null) lvar.stringConstraints=$stg.scout; lvar.text += ":{"+$stg.text+"}"; } '}')?  (':' '{' stc=structConstraints { lvar.structConstraints=$stc.scout; lvar.text += ":{"+$stc.text+"}";} '}')?  ;
	//	  lentity=entity[parentId] { lvar=$lentity.lvar; } 
	//	|  lentity=entity[parentId] ':' '{' stringConstraints { lvar = $lentity.lvar; lvar.stringConstraints=$stringConstraints.scout;}  '}' 
	//	|  lentity=entity[parentId] '::' '{' structConstraints '}' { lvar = $lentity.lvar; lvar.structConstraints=$structConstraints.scout;}  
	//	|  lentity=entity[parentId] ':' '{' stringConstraints '}' ':' '{' structConstraints '}' { lvar = $lentity.lvar; lvar.stringConstraints=$stringConstraints.scout; lvar.structConstraints=$structConstraints.scout;}  ;

entity [int parentId] returns [ LogolVariable lvar] 
	@init{ 
		Model mod = new Model();
		int count=(Integer) Treatment.counters.get(parentId);
		
		LogolVariable tmpvar = new LogolVariable();
		
		// Add variable to the list of variables
		LogolVariable.variables.add(count);
	}
	:	neg='!'? { if($neg!=null) { tmpvar.neg =true;} } entityVariable { $lvar=$entityVariable.varEntity; $lvar.neg = tmpvar.neg; } 
		| curmod=ModelName { mod.name=$curmod.text; mod.name=mod.name.replaceAll("\%\\w+\%", "");  } '(' vars+=VARIABLE?  (',' vars+=VARIABLE )* ')' { if($vars!=null) {	for(int i=0;i<$vars.size();i++) {org.antlr.runtime.CommonToken token = (org.antlr.runtime.CommonToken) $vars.get(i);mod.vars.add(token.getText().replaceAll("\%\\w+\%", ""));}}  tmpvar.type=3; tmpvar.mod=mod; $lvar=tmpvar;}
		| neg='!'? { if($neg!=null) { tmpvar.neg =true;} } modifier
complexstring { tmpvar.modifier=$modifier.mod; tmpvar.fixedValue=$complexstring.text; $lvar=tmpvar;}
		| complexstring { tmpvar.fixedValue=$complexstring.text; $lvar=tmpvar;}
		| '!' complexstring { tmpvar.fixedValue=$complexstring.text; tmpvar.neg=true;$lvar=tmpvar;} 
		| repeat[count] { $lvar=$repeat.lvar; } 
//		| '!external' '(' progname=ANY (',' '[' in+=ANY (',' in+=ANY)* ']' ',' '[' out+=ANY (',' out+=ANY)* ']')? ')' { tmpvar.name="external"; tmpvar.extra=$progname.text; for(int i=0;i<$in.size();i++) { tmpvar.externalIn.add(((org.antlr.runtime.CommonToken)$in.get(i)).getText()); } for(int i=0;i<$in.size();i++) { tmpvar.externalOut.add(((org.antlr.runtime.CommonToken)$out.get(i)).getText()); }  $lvar=tmpvar;}
		| '!external' '(' progname=LOWID (',' '[' (in+=VARIABLE|in+=INT) (','   (in+=VARIABLE|in+=INT))* ']' (',' '['  (out+=VARIABLE|out+=INT) (','(out+=VARIABLE|out+=INT))* ']')?)? ')' { tmpvar.name="external"; tmpvar.extra=$progname.text; if($in!=null) for(int i=0;i<$in.size();i++) { tmpvar.externalIn.add(((org.antlr.runtime.CommonToken)$in.get(i)).getText()); } if($out!=null) for(int i=0;i<$out.size();i++) { tmpvar.externalOut.add(((org.antlr.runtime.CommonToken)$out.get(i)).getText()); }  $lvar=tmpvar;}
		| neg='!'? { if($neg!=null) { tmpvar.neg =true;} }'(' viewcontent=view[count] { tmpvar.fixedValue=$viewcontent.value; tmpvar.type=2; $lvar=tmpvar; } ')'
		 ;

entityVariable returns [LogolVariable varEntity]
	:	entityvar=stringVariableElement { LogolVariable var = $entityvar.lvar; $varEntity=var; }  ;



stringVariableElement returns [LogolVariable lvar]	
	:	modifierData=modifier? variable=stringVariable { $lvar = $variable.lvar; if($modifierData.mod!=null) $lvar.modifier=$modifierData.mod; };

modifier returns [ Modifier mod] 
	@init { Modifier tmpMod = new Modifier(); }
	:  	(morph=morphism {tmpMod.type=$morph.op;tmpMod.modifierName=$morph.name;$mod=tmpMod;}) | ('$' st=string {tmpMod.type=Constants.MODIFIERCOST;tmpMod.modifierName=$st.text;$mod=tmpMod;  });

stringConstraints returns [Vector scout] 
	@init{	$scout = new Vector();	}
	:	 stc1=stringConstraint  { $scout.add($stc1.sc); } (',' stc2=stringConstraint {$scout.add($stc2.sc);} )* ;
	
stringConstraint returns [StringConstraint sc] 
	@init{ StringConstraint tmpConst = new StringConstraint(); } 
	@after{$sc=tmpConst;}
	:	'_' VARIABLE { tmpConst.type=Constants.SAVECONSTRAINT; tmpConst.variableContent=$VARIABLE.text.replaceAll("\%\\w+\%", "");;} 
		| '?' VARIABLE { tmpConst.type=Constants.CONTENTCONSTRAINT; tmpConst.variableContent=$VARIABLE.text.replaceAll("\%\\w+\%", "");;}
		| '?' string { tmpConst.type=Constants.CONTENTCONSTRAINT; tmpConst.contentConstraint=$string.text;} 
		| '@' interval { tmpConst.type=Constants.BEGINCONSTRAINT; /*tmpConst.contentConstraint=$interval.text;*/ tmpConst.min=$interval.min; tmpConst.max=$interval.max;} 
		| '@@' interval { tmpConst.type=Constants.ENDCONSTRAINT; /*tmpConst.contentConstraint=$interval.text;*/ tmpConst.min=$interval.min; tmpConst.max=$interval.max;}
		| '#' optimal='OPT'? interval { tmpConst.type=Constants.LENGTHCONSTRAINT; /*tmpConst.contentConstraint=$interval.text;*/ tmpConst.min=$interval.min; tmpConst.max=$interval.max; if($optimal!=null && $optimal.text!=null) { tmpConst.optimal  = Constants.OPTIMAL_CONSTRAINT.OPTIMAL_LENGTH; }}
		| '!' '@' interval { tmpConst.type=Constants.BEGINCONSTRAINT; /*tmpConst.contentConstraint=$interval.text;*/ tmpConst.min=$interval.min; tmpConst.max=$interval.max; tmpConst.neg=true;} 
		| '!' '@@' interval { tmpConst.type=Constants.ENDCONSTRAINT; /*tmpConst.contentConstraint=$interval.text;*/ tmpConst.min=$interval.min; tmpConst.max=$interval.max; tmpConst.neg=true;}
		| '!' '#' interval { tmpConst.type=Constants.LENGTHCONSTRAINT; /*tmpConst.contentConstraint=$interval.text;*/ tmpConst.min=$interval.min; tmpConst.max=$interval.max; tmpConst.neg=true;};
	

structConstraints returns [Vector scout] @init{
					$scout = new Vector();
					} :	stc1=structConstraint  { $scout.add($stc1.sc); } (',' stc2=structConstraint {$scout.add($stc2.sc);} )* ;
					
		

structConstraint returns [StructConstraint sc] 
	@init{ StructConstraint tmpConst = new StructConstraint(); }  
	@after{$sc=tmpConst;}
	:	'_' VARIABLE { tmpConst.type=Constants.SAVECONSTRAINT; tmpConst.variableContent=$VARIABLE.text.replaceAll("\%\\w+\%", "");} 
		| '?' VARIABLE { tmpConst.type=Constants.CONTENTCONSTRAINT; tmpConst.variableContent=$VARIABLE.text.replaceAll("\%\\w+\%", "");} 
		| '?' string { tmpConst.type=Constants.CONTENTCONSTRAINT; tmpConst.contentConstraint=$string.text;} 
		| '%' string ':' percentage=INT { tmpConst.type=Constants.ALPHABETCONSTRAINT; tmpConst.alphabetConstraint=$string.text; tmpConst.min=$percentage.text;}
		| cost=('$'|'p$') name=LOWID? interval  { if($name!=null && $name.text!=null) {tmpConst.name=$name.text;} if($cost.text.equals(Constants.COSTSIGN)) {tmpConst.type=Constants.COSTCONSTRAINT;} else {tmpConst.type=Constants.PERCENTCOSTCONSTRAINT;} /*tmpConst.contentConstraint=$interval.text;*/ tmpConst.min=$interval.min; tmpConst.max=$interval.max;}
		| cost=('$$'|'p$$') interval  {  if($cost.text.equals(Constants.DISTANCESIGN)) {tmpConst.type=Constants.DISTANCECONSTRAINT;} else {tmpConst.type=Constants.PERCENTDISTANCECONSTRAINT;} /*tmpConst.contentConstraint=$interval.text;*/ tmpConst.min=$interval.min; tmpConst.max=$interval.max;}
		| '!' cost=('$'|'p$') name=LOWID? interval { if($name!=null && $name.text!=null) {tmpConst.name=$name.text;} if($cost.text.equals(Constants.COSTSIGN)) {tmpConst.type=Constants.COSTCONSTRAINT;} else {tmpConst.type=Constants.PERCENTCOSTCONSTRAINT;} /*tmpConst.contentConstraint=$interval.text;*/ tmpConst.neg=true; tmpConst.min=$interval.min; tmpConst.max=$interval.max;}
		| '!' cost=('$$'|'p$$') interval { if($cost.text.equals(Constants.DISTANCESIGN)) {tmpConst.type=Constants.DISTANCECONSTRAINT;} else {tmpConst.type=Constants.PERCENTDISTANCECONSTRAINT;} /*tmpConst.contentConstraint=$interval.text;*/ tmpConst.neg=true; tmpConst.min=$interval.min; tmpConst.max=$interval.max;};


structureValues
	:	structureValue (',' structureValue);

structureValue
	:	'?' VARIABLE | '$' intexpression;
	
morphism returns [int op, String name]:	 oper=operator { if($oper.text.equals("+")) {$op=Constants.MODIFIERMORPHPLUS;}  else {$op=Constants.MODIFIERMORPHMINUS;} } val=string {$name=$val.text;};

weight	:	'$' string;

stringVariable returns [ LogolVariable lvar]
	@init { boolean isParent=true;  }
	:	'.*'  {$lvar=new LogolVariable();$lvar.name="_";} | (parent='?'? { if($parent!=null) { isParent=false;} } var=VARIABLE {$lvar=new LogolVariable();$lvar.name=$var.text.replaceAll("\%\\w+\%", ""); if(isParent)  {$lvar.isParent=true; } else {$lvar.isParent=false;}}) ;
	



//repeat	returns [ LogolVariable lvar]
repeat[int parentId]	returns [ LogolVariable lvar]
	@init { 	$lvar= new LogolVariable(); 
			// If isAny set, postpone treatment of spacer because it will be managed in repeatContent, should not be applied on internal variable
		        int tmpAny = Treatment.isAny;
		        String tmpAnyMin = Treatment.isAnyMin;
		        String tmpAnyMax = Treatment.isAnyMax;
		        boolean tmpSaveAny = Treatment.saveAny;
		        Treatment.isAny=-1;
		        Treatment.isAnyMin="";
		        Treatment.isAnyMax="";
		        Treatment.saveAny=false;
	
	}
	@after {	Treatment.isAny = tmpAny;
       			Treatment.isAnyMin = tmpAnyMin;
			Treatment.isAnyMax = tmpAnyMax;
			Treatment.saveAny = tmpSaveAny;
		}
	//:	('repeat(' (view[parentId] parameters?) ')' '+' interval? ) { $lvar.content=$view.value; $lvar.repeatQuantity=$interval.text; $lvar.repeatType=$parameters.type; $lvar.repeatParam=$parameters.intervalParam;} ;
	:	'repeat(' view[parentId] type=(','|';')? typeinterval=interval?  { $lvar.content=$view.value; if($type!=null) { if($type.text.equals(",")) {$lvar.repeatType=1; } else {$lvar.repeatType=2;} $lvar.repeatParam=$typeinterval.text.replaceAll("\%\\w+\%", ""); }}   ')+' repeatinterval=interval? {if(repeatinterval!=null) {$lvar.repeatQuantity=$repeatinterval.text.replaceAll("\%\\w+\%", "");} } ;
//( op=(','| ';' | '|')?  (ent=constrainedEntity[parentId] { $value+=curView.execute($ent.predicate,$op.text,0,countVar); })  )*

parameters returns [ int type, String intervalParam]
	:	t='_,' interval | t='_;' interval { if($t.text.equals("_,")) { $type=1;} else { $type=2;} $intervalParam=$interval.text;};


test: '%' string ':' percentage=INT;


operator: '+' | '-';

VARIABLE: ('A'..'Z')+('0'..'9')+ ('%'('a'..'z'|'A'..'Z'|'0'..'9')+'%')?;

string	: '"' LOWID '"';

orstring : '[' LOWID+ ('|' LOWID+ )* ']';
complexstring :  '"' (LOWID|orstring)+   '"';

		
VARID	:	('A'..'Z')+;
LOWID	: ('a'..'z'|'\-')+;
ID  :   ('a'..'z'|'A'..'Z')+ ;
INT :   ('0'..'9')+ ;
ANY	: ('a'..'z'|'A'..'Z'|'0'..'9')+ ;	
NEWLINE: '\r'? '\n' ;
WHITESPACE : ( '\t' | ' ' | '\r' | '\n'| '\u000C' )+ { $channel = HIDDEN; };
	

COMPARATOR: ( '>' | '>=' | '=' | '<' | '=<' );

varrefinteger:	VARIABLE | '@' VARIABLE | '@@' VARIABLE | '#' VARIABLE | '$' VARIABLE | '$$' VARIABLE | ('?' VARIABLE ('.nboccur' | '.minDistance' | '.maxDistance'));	

intexpression:	'_' | INT | varrefinteger | (varrefinteger operator INT) | ( INT operator varrefinteger);

interval returns [String min, String max] : ('[' s1=intexpression ',' s2=intexpression ']' { $min = $s1.text.replaceAll("\%\\w+\%", ""); $max = $s2.text.replaceAll("\%\\w+\%", ""); })| ('[' s3=intexpression ']'  { $min = $s3.text.replaceAll("\%\\w+\%", ""); $max = $s3.text.replaceAll("\%\\w+\%", ""); }) ;



