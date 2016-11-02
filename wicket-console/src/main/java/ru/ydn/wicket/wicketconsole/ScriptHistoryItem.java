package ru.ydn.wicket.wicketconsole;

import java.io.Serializable;

public class ScriptHistoryItem implements Serializable
{
	private String script;
	private IScriptEngineInterlayerResult resultObject;
	private String engine;
	
	public ScriptHistoryItem(String script)
	{
		super();
		this.script = script;
	}
	public String getScript() {
		return script;
	}
	public void setScript(String script) {
		this.script = script;
	}
	public String getEngine() {
		return engine;
	}
	public void setEngine(String engine) {
		this.engine = engine;
	}
	
	public IScriptEngineInterlayerResult getResultObject() {
		return resultObject;
	}
	public void setResultObject(IScriptEngineInterlayerResult resultObject) {
		this.resultObject = resultObject;
	}

	public String getContent()
	{
		return toString();
	}
	@Override
	public String toString() {
		return "ScriptHistoryItem [engine="+engine+", script=" + script + ", returnObject="
				+ resultObject.getReturnedObject() + ", out=" + resultObject.getOut()
				+ ", err=" + resultObject.getError() + "]";
	}
	
}
