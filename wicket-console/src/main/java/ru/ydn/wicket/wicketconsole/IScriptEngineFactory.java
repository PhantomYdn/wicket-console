package ru.ydn.wicket.wicketconsole;

import java.util.Collection;

public interface IScriptEngineFactory {
	public IScriptEngine createScriptEngine(String name);
	public Collection<String> getSupportedEngines();
}
