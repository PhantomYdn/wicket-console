package ru.ydn.wicket.wicketconsole.calc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

/**
 * JSR-223 factory of {@link CalcScriptEngine}, a tiny engine that exists only in the tests, so they don't depend on a
 * JavaScript engine (Nashorn left the JDK in 15). Registered in {@code META-INF/services/javax.script.ScriptEngineFactory}.
 */
public class CalcScriptEngineFactory implements ScriptEngineFactory
{
	public static final String NAME = "Calc";

	@Override
	public String getEngineName() {
		return "Calc test engine";
	}

	@Override
	public String getEngineVersion() {
		return "1.0";
	}

	@Override
	public List<String> getExtensions() {
		return Collections.singletonList("calc");
	}

	@Override
	public List<String> getMimeTypes() {
		return Collections.emptyList();
	}

	@Override
	public List<String> getNames() {
		return Collections.singletonList(NAME);
	}

	@Override
	public String getLanguageName() {
		return "calc";
	}

	@Override
	public String getLanguageVersion() {
		return "1.0";
	}

	@Override
	public Object getParameter(String key) {
		if(ScriptEngine.ENGINE.equals(key)) return getEngineName();
		else if(ScriptEngine.ENGINE_VERSION.equals(key)) return getEngineVersion();
		else if(ScriptEngine.NAME.equals(key)) return NAME;
		else if(ScriptEngine.LANGUAGE.equals(key)) return getLanguageName();
		else if(ScriptEngine.LANGUAGE_VERSION.equals(key)) return getLanguageVersion();
		else return null;
	}

	@Override
	public String getMethodCallSyntax(String obj, String m, String... args) {
		throw new UnsupportedOperationException("calc has no method calls");
	}

	@Override
	public String getOutputStatement(String toDisplay) {
		return "print(\"" + toDisplay + "\")";
	}

	@Override
	public String getProgram(String... statements) {
		return String.join(";", Arrays.asList(statements));
	}

	@Override
	public ScriptEngine getScriptEngine() {
		return new CalcScriptEngine(this);
	}
}
