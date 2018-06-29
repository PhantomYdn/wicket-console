package ru.ydn.wicket.wicketconsole;

import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import com.github.openjson.JSONException;
import com.github.openjson.JSONObject;

public class EmbeddedScriptEngine implements IScriptEngine{

	private String name;
	private ScriptEngine engine;

	private transient ScriptContext ctx;
	
	private boolean asJSONComatibleAvailable = true;

	
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
	public ScriptResult eval(String command,IScriptContext context) {
		ScriptResult result = new ScriptResult(name, command);
		if (context!=null){
			Set<Entry<String, Object>> bindings = context.getBindings().entrySet();
			for (Entry<String, Object> binding : bindings) {
				ctx.setAttribute(binding.getKey(), binding.getValue(), ScriptContext.ENGINE_SCOPE);
			}
		}
		
		try {
			Object ret = engine.eval(command, ctx);
			ctx.getBindings(ScriptContext.ENGINE_SCOPE).put("$result", ret);
			ret = tryConvertToJSON(ret);
			result.setResultModel(new StorageModel<Object>(ret));
			result.setOut(getContentAndClear((StringWriter)ctx.getWriter()));
			result.setError(getContentAndClear((StringWriter)ctx.getErrorWriter()));
		} catch (Throwable e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			result.setError(sw.toString());
		}
		return result;
	}
	
	public Object tryConvertToJSON(Object ret) {
		try {
			if(ret instanceof Bindings) {
				if(asJSONComatibleAvailable) {
					try {
						ret = engine.eval("Java.asJSONCompatible($result)", ctx);
					} catch (Exception e) {
						asJSONComatibleAvailable = false;
					}
				}
				return JSONObject.wrap(ret);
			}
			return ret;
		} catch (JSONException e) {
			return ret;
		}
	}


}
