package sandbox.ac;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class MyTest_AC_eval {

	public static void main(String[] args) throws ScriptException {

		String name = "3+4";
		
		testEvalScript(name);
	}

	private static void testEvalScript (String name) throws ScriptException{
	    ScriptEngineManager manager = new ScriptEngineManager();
	    ScriptEngine engine = manager.getEngineByName("js");        
	
		if(name!=null)
		{
			int res;
			res = (int) engine.eval(name);
			System.out.println(res + " this is sure");
		}
		
	}
}
