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
	private static final ScriptEngineManager manager = new ScriptEngineManager();
	
	private LinkedList<ScriptHistoryItem> history = new LinkedList<ScriptHistoryItem>();
	
	private ScriptContext ctx;
	
	private ScriptEngine engine;
	
	public ScriptExecutor()
	{
		resetContext();
	}
	
	public void resetContext()
	{
		ScriptContext ctx = new SimpleScriptContext();
		ctx.setWriter(new StringWriter());
		ctx.setErrorWriter(new StringWriter());
		ctx.setReader(new StringReader(""));
		this.ctx = ctx;
	}
	
	public ScriptHistoryItem execute(String command)
	{
		ScriptHistoryItem historyItem = executeWithoutHistory(command);
		history.add(historyItem);
		return historyItem;
	}
	
	private static String getContentAndReset(StringWriter writer)
	{
		StringBuffer buf = writer.getBuffer();
		String ret = buf.toString();
		buf.setLength(0);
		//buf.delete(0, buf.length());
		return ret;
	}
	
	public ScriptHistoryItem executeWithoutHistory(String command)
	{
		ScriptHistoryItem historyItem = new ScriptHistoryItem(command);
		try
		{
			historyItem.setReturnObject(getScriptEngine().eval(command, ctx));
		} catch (ScriptException e)
		{
			historyItem.setException(e);
		}
		historyItem.setOut(getContentAndReset((StringWriter)ctx.getWriter()));
		historyItem.setErr(getContentAndReset((StringWriter)ctx.getErrorWriter()));
		return historyItem;
	}
	
	public ScriptEngine getScriptEngine()
	{
		if(engine==null)
		{
			engine = manager.getEngineByName("JavaScript");
		}
		return engine;
	}

	public LinkedList<ScriptHistoryItem> getHistory() {
		return history;
	}
	
	
}
