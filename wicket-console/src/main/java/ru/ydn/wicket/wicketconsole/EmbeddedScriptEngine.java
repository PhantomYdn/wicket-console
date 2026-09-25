package ru.ydn.wicket.wicketconsole;

import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.openjson.JSONException;
import com.github.openjson.JSONObject;

public class EmbeddedScriptEngine implements IScriptEngine{
	
	private static final Logger LOG = LoggerFactory.getLogger(EmbeddedScriptEngine.class);
	/** {@code ScriptEngineFactory.getEngineName()} of GraalJS ({@code com.oracle.truffle.js.scriptengine.GraalJSEngineFactory}) */
	private static final String GRAALJS_ENGINE_NAME = "Graal.js";

	private String name;
	private ScriptEngine engine;

	private transient ScriptContext ctx;
	
	private boolean asJSONComatibleAvailable = true;

	
	public EmbeddedScriptEngine(String name, ScriptEngine engine) {
		this.name = name;
		this.engine = engine;
		this.ctx = new SimpleScriptContext();
		// Engine-created bindings keep the engine's state between commands: GraalJS starts a new JS context on every
		// eval when the engine-scope bindings are not its own (a plain SimpleBindings)
		Bindings engineBindings = engine.createBindings();
		if(engineBindings!=null) {
			if(isGraalJS(engine)) allowJavaAccess(engineBindings);
			ctx.setBindings(engineBindings, ScriptContext.ENGINE_SCOPE);
		}
		ctx.setWriter(new StringWriter());
		ctx.setErrorWriter(new StringWriter());
		ctx.setReader(new StringReader(""));
	}

	private static boolean isGraalJS(ScriptEngine engine) {
		ScriptEngineFactory factory = engine.getFactory();
		return factory!=null && GRAALJS_ENGINE_NAME.equals(factory.getEngineName());
	}
	
	/**
	 * GraalJS denies access to Java by default: scripts can't call methods of bound Java objects, and {@code Java.type}
	 * and {@code java.*} don't exist. Nashorn, which the console was built for, allowed both. Restore that for this
	 * console's own engines only, through GraalJS's documented "magic" bindings options, which must be set before the
	 * first eval. IO, threads and native access stay as GraalJS configures them.
	 */
	private static void allowJavaAccess(Bindings bindings) {
		try {
			bindings.put("polyglot.js.allowHostAccess", true);
			bindings.put("polyglot.js.allowHostClassLookup", (Predicate<String>) className -> true);
		} catch (RuntimeException e) {
			LOG.warn("Can't enable Java access for GraalJS scripts in the console", e);
		}
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
		
		result.start();
		try {
			Object ret = engine.eval(command, ctx);
			ctx.getBindings(ScriptContext.ENGINE_SCOPE).put("$result", ret);
			ret = tryConvertToJSON(ret);
			result.setResultModel(new StorageModel<Object>(ret));
		} catch (Throwable e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			result.setError(sw.toString());
		} finally {
			result.setOut(getContentAndClear((StringWriter)ctx.getWriter()));
			result.prependError(getContentAndClear((StringWriter)ctx.getErrorWriter()));
		}
		result.finish();
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
