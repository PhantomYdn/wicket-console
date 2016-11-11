package ru.ydn.wicket.wicketconsole;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import org.apache.wicket.model.IModel;

public class ScriptExecutor
{
	private static final LinkedList<IScriptEngineFactory> ENGINES_FACTORIES = new LinkedList<IScriptEngineFactory>();
	private static transient ScriptEngineManager manager;
	
	static {
		ENGINES_FACTORIES.add(new IScriptEngineFactory() {
			
			private Collection<String> supportedEngines;
			
			@Override
			public IScriptEngine createScriptEngine(String name) {
				if(manager==null) manager = new ScriptEngineManager();
				ScriptEngine engine = manager.getEngineByName(name);
				return engine!=null?new EmbeddedScriptEngine(name, engine):null;
			}

			@Override
			public Collection<String> getSupportedEngines() {
				if(supportedEngines==null) {
					if(manager==null) manager = new ScriptEngineManager();
					List<String> engines = new ArrayList<String>();
					for(ScriptEngineFactory factory : manager.getEngineFactories()){
						engines.add(factory.getEngineName());
					}
					supportedEngines = Collections.unmodifiableCollection(engines);
				}
				return supportedEngines;
			}
			
		});
	}
	
	public static void registerScriptEngineFactory(IScriptEngineFactory scriptEngineFactory) {
		ENGINES_FACTORIES.push(scriptEngineFactory);
	}
	
	public static Collection<String> getSupportedEngines() {
		Set<String> supportedEngines = new HashSet<String>();
		for(IScriptEngineFactory factory: ENGINES_FACTORIES) {
			supportedEngines.addAll(factory.getSupportedEngines());
		}
		return supportedEngines;
	}
	
	private LinkedList<ScriptResult> history = new LinkedList<ScriptResult>();
	
	private Map<String,IScriptEngine> runningEngines;
	
	public ScriptExecutor()
	{
		runningEngines = new HashMap<String,IScriptEngine>();
	}
	
	public ScriptResult execute(String command,String scriptEngineName)
	{
		ScriptResult newItem = executeWithoutHistory(command,scriptEngineName);
		history.add(newItem);
		newItem.setEngine(scriptEngineName);
		return newItem;
	}

	public ScriptResult execute(String command)
	{
		return execute(command,"JavaScript");
	}
	
	public ScriptResult executeWithoutHistory(String command,String scriptEngineName)
	{
		return getScriptEngine(scriptEngineName).eval(command);
	}
	
	public IScriptEngine getScriptEngine(String engineName)
	{
		IScriptEngine engine = runningEngines.get(engineName);
		if(engine==null)
		{
			for(IScriptEngineFactory factory : ENGINES_FACTORIES){
				engine = factory.createScriptEngine(engineName);
				if(engine!=null) {
					runningEngines.put(engineName, engine);
					break;
				}
			}
		}
		return engine;
	}
	

	public LinkedList<ScriptResult> getHistory() {
		return history;
	}
	
}
