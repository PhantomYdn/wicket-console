package ru.ydn.wicket.wicketconsole;

import java.io.Serializable;

public class ScriptHistoryItem implements Serializable
{
	private String script;
	private IScriptEngineInterlayerResult returnObject;
	private Exception exception;
	private String out;
	private String err;
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
	public IScriptEngineInterlayerResult getReturnObject() {
		return returnObject;
	}
	public void setReturnObject(IScriptEngineInterlayerResult returnObject) {
		this.returnObject = returnObject;
	}
	public Exception getException() {
		return exception;
	}
	public void setException(Exception exception) {
		this.exception = exception;
	}
	public String getOut() {
		return out;
	}
	public void setOut(String out) {
		this.out = out;
	}
	public String getErr() {
		return err;
	}
	public void setErr(String err) {
		this.err = err;
	}
	public String getEngine() {
		return engine;
	}
	public void setEngine(String engine) {
		this.engine = engine;
	}
	
	public String getContent()
	{
		return toString();
	}
	@Override
	public String toString() {
		return "ScriptHistoryItem [engine="+engine+", script=" + script + ", returnObject="
				+ returnObject + ", exception=" + exception + ", out=" + out
				+ ", err=" + err + "]";
	}
	
}
