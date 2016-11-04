package ru.ydn.wicket.wicketconsole;

import java.io.StringReader;
import java.io.StringWriter;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

public class JavaxScriptEngineInterlayer implements IScriptEngineInterlayer{

	private static final ScriptEngineManager manager = new ScriptEngineManager();

	private String name;
	private ScriptEngine engine;

	private transient ScriptContext ctx;

	
	public JavaxScriptEngineInterlayer() {
		this.ctx = new SimpleScriptContext();
		ctx.setWriter(new StringWriter());
		ctx.setErrorWriter(new StringWriter());
		ctx.setReader(new StringReader(""));
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}
	
	private static String getContentAndClear(StringWriter writer)
	{
		StringBuffer buf = writer.getBuffer();
		String ret = buf.toString();
		buf.setLength(0);
		return ret;
	}
	
	@Override
	public IScriptEngineInterlayerResult eval(String command) {
		if (engine == null){
			engine = manager.getEngineByName(name);
		}

		JavaxScriptEngineInterlayerResult result = new JavaxScriptEngineInterlayerResult();
		
		try {
			result.setReturnedObject(engine.eval(command, ctx));
			result.setOut(getContentAndClear((StringWriter)ctx.getWriter()));
			result.setError(getContentAndClear((StringWriter)ctx.getErrorWriter()));
			result.onUpdate();
		} catch (ScriptException e) {
			result.setError(e.getMessage());
		}
		
		return result;
	}


}
