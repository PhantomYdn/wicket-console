package ru.ydn.wicket.wicketconsole;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

public class ScriptExecutor
{
	private LinkedList<ScriptHistoryItem> history = new LinkedList<ScriptHistoryItem>();
	
	private Map<String,IScriptEngineInterlayer> engines;
	
	public ScriptExecutor()
	{
		engines = new HashMap<String,IScriptEngineInterlayer>();
	}
	
	public ScriptHistoryItem execute(String command,String scriptEngineName)
	{
		ScriptHistoryItem newItem = executeWithoutHistory(command,scriptEngineName);
		history.add(newItem);
		newItem.setEngine(scriptEngineName);
		return newItem;
	}

	public ScriptHistoryItem execute(String command)
	{
		return execute(command,"JavaScript");
	}
	
	public ScriptHistoryItem executeWithoutHistory(String command,String scriptEngineName)
	{
		ScriptHistoryItem historyItem = new ScriptHistoryItem(command);
			IScriptEngineInterlayerResult result = getScriptEngine(scriptEngineName).eval(command);
			historyItem.setResultObject(result);
		return historyItem;
	}
	
	public IScriptEngineInterlayer getScriptEngine(String engineName)
	{
		IScriptEngineInterlayer engine = engines.get(engineName);
		if(engine==null)
		{
			engine = ScriptEngineInterlayerManager.INSTANCE.getByName(engineName);
			engines.put(engineName, engine);
		}
		return engine;
	}
	

	public LinkedList<ScriptHistoryItem> getHistory() {
		return history;
	}
	
}
