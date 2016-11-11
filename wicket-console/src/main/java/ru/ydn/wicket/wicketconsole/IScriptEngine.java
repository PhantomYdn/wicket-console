package ru.ydn.wicket.wicketconsole;

import org.apache.wicket.model.IModel;

public interface IScriptEngine {
	
	String getName();
	
	ScriptResult eval(String command);

}
