package ru.ydn.wicket.wicketconsole;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class JavaxScriptEngineInterlayer implements IScriptEngineInterlayer{

	private static final ScriptEngineManager manager = new ScriptEngineManager();

	String name;
	ScriptEngine engine;

	
	public JavaxScriptEngineInterlayer(String name, String javaxName) {
		this.name = name;
		engine = manager.getEngineByName(javaxName);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public IScriptEngineInterlayerResult eval(String command) throws ScriptException {
		JavaxScriptEngineInterlayerResult result = new JavaxScriptEngineInterlayerResult();
		result.setReturnedObject(engine.eval(command, result.getScriptContext()));
		result.onUpdate();
		return result;
	}

}
