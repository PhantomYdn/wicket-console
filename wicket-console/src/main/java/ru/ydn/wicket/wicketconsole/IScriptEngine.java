package ru.ydn.wicket.wicketconsole;

public interface IScriptEngine {
	
	String getName();
	
	ScriptResult eval(String command,IScriptContext context);

}
