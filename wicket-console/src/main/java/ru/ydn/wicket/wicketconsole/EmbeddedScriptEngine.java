package ru.ydn.wicket.wicketconsole;

import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import org.apache.wicket.model.IModel;
import org.apache.wicket.util.io.IOUtils;

public class EmbeddedScriptEngine implements IScriptEngine{

	private String name;
	private ScriptEngine engine;

	private transient ScriptContext ctx;

	
	public EmbeddedScriptEngine(String name, ScriptEngine engine) {
		this.name = name;
		this.engine = engine;
		this.ctx = new SimpleScriptContext();
		ctx.setWriter(new StringWriter());
		ctx.setErrorWriter(new StringWriter());
		ctx.setReader(new StringReader(""));
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
	public ScriptResult eval(String command) {
		ScriptResult result = new ScriptResult(command);
		
		try {
			Object ret = engine.eval(command, ctx);
			result.setResultModel(new StorageModel<Object>(ret));
			result.setOut(getContentAndClear((StringWriter)ctx.getWriter()));
			result.setError(getContentAndClear((StringWriter)ctx.getErrorWriter()));
		} catch (ScriptException e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			String error = sw.toString();
			pw.close();
			result.setError(error);
		}
		return result;
	}


}
