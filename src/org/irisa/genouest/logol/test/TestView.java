package org.irisa.genouest.logol.test;

import static org.junit.Assert.*;

import org.irisa.genouest.logol.Expression;
import org.irisa.genouest.logol.GrammarException;
import org.irisa.genouest.logol.LogolVariable;
import org.irisa.genouest.logol.Treatment;
import org.irisa.genouest.logol.Entity;
import org.irisa.genouest.logol.VariableId;
import org.junit.Before;
import org.junit.Test;

public class TestView {

	
    @Before
    public void setUp() {
    	LogolVariable.userVariables.clear();
    	LogolVariable.userVariables.put(new VariableId("LOGOLTEST",Treatment.currentModel.name),"1");
    }
	
	
	
	@Test
	public void testExpression() throws GrammarException {

		Expression exp = new Expression();
		exp = exp.getExpressionData("100");
		assert(exp.variable.equals("100"));
		
		exp = exp.getExpressionData("$LOGOLTEST");
		
		assert(exp.variable.contains("LogolVAR_Tmp"));
		assert(exp.expression.equals("LogolVAR_Tmp"));
		exp = exp.getExpressionData("$LOGOLTEST + 100");
		assert(exp.variable.contains("LogolVAR_Tmp"));
		assert(exp.expression.equals("+100"));
			
		exp = exp.getExpressionData("100 - $LOGOLTEST");
		assert(exp.variable.contains("LogolVAR_Tmp"));
		assert(exp.expression.equals("100-"));
		
		assert(1==1);
	}
}
