package org.irisa.genouest.logol.utils;


import java.util.ArrayList;
import java.util.Map;

import org.irisa.genouest.logol.Constants;
import org.junit.Test;

public class UtilsTest {

	@Test
	public void testGetDiff() {
		 GrammarTools gTool = GrammarTools.getInstance();
	 
		 Integer res = gTool.getDiff("120", "50");
		 assert(res==70);
		 res= gTool.getDiff("#VAR1 + 30", "#VAR1 - 20");
		 assert(res==50);
		 res= gTool.getDiff("#VAR1 + 30", "#VAR1");
		 assert(res==30);
	}
	
	@Test
	public void testGenerateGrammars() {
		
		 GrammarTools gTool = GrammarTools.getInstance();
		 Map<String,String[]> vars =gTool.getVariables();
		 vars.put("TEST1", new String[] { "0", "3", Constants.OPTIMAL_MODE.MAX.toString()});
		 vars.put("TEST2", new String[] { "0", "2", Constants.OPTIMAL_MODE.MAX.toString()});
		 vars.put("TEST3", new String[] { "0", "2", Constants.OPTIMAL_MODE.MAX.toString()});
		 gTool.generateGrammars();
			for(ArrayList<String[]> list : gTool.grammarCombinations.values()){
				assert(list.size()==36);
			}
	}

}
