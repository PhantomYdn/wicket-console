package ru.ydn.wicket.wicketconsole;

import javax.script.ScriptException;

public interface IScriptEngineInterlayer {
	
	String getName();
	
	IScriptEngineInterlayerResult eval(String command) throws Exception;

}
