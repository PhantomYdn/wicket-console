package ru.ydn.wicket.wicketconsole;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class JavaxScriptEngineInterlayer implements IScriptEngineInterlayer{

	private static final ScriptEngineManager manager = new ScriptEngineManager();

	String name;
	ScriptEngine engine;

	
	public JavaxScriptEngineInterlayer() {
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public IScriptEngineInterlayerResult eval(String command) {
		if (engine == null){
			engine = manager.getEngineByName(name);
		}

		JavaxScriptEngineInterlayerResult result = new JavaxScriptEngineInterlayerResult();
		
		try {
			result.setReturnedObject(engine.eval(command, result.getScriptContext()));
			result.onUpdate();
		} catch (ScriptException e) {
			result.setError(e.getMessage());
		}
		
		return result;
	}


}
