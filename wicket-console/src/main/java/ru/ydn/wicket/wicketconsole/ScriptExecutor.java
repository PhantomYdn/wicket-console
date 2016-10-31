package ru.ydn.wicket.wicketconsole;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.LinkedList;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

public class ScriptExecutor
{
	private LinkedList<ScriptHistoryItem> history = new LinkedList<ScriptHistoryItem>();
	
	private IScriptEngineInterlayer engine;
	
	public ScriptExecutor()
	{
	}
	
	public ScriptHistoryItem execute(String command,String scriptEngineName)
	{
		engine = ScriptEngineInterlayerManager.INSTANCE.getByName(scriptEngineName);
		ScriptHistoryItem newItem = execute(command);
		newItem.setEngine(scriptEngineName);
		return newItem;
	}
	
	public ScriptHistoryItem execute(String command)
	{
		ScriptHistoryItem historyItem = executeWithoutHistory(command);
		history.add(historyItem);
		return historyItem;
	}
	
	public ScriptHistoryItem executeWithoutHistory(String command)
	{
		ScriptHistoryItem historyItem = new ScriptHistoryItem(command);
			IScriptEngineInterlayerResult result = getScriptEngine().eval(command);
			historyItem.setResultObject(result);
			historyItem.setReturnObject(result.getReturnedObject());
			historyItem.setOut(result.getOut());
			historyItem.setErr(result.getError());

		return historyItem;
	}
	
	public IScriptEngineInterlayer getScriptEngine()
	{
		if(engine==null)
		{
			engine = ScriptEngineInterlayerManager.INSTANCE.getByName("JavaScript");
		}
		return engine;
	}

	public LinkedList<ScriptHistoryItem> getHistory() {
		return history;
	}
	
}
