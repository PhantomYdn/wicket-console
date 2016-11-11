package ru.ydn.wicket.wicketconsole;

import java.io.Serializable;

import org.apache.wicket.model.IModel;

public final class ScriptResult implements Serializable
{
	private String script;
	private String engine;
	private String out;
	private String error;
	private IModel<Object> resultModel;
	
	public ScriptResult(String script)
	{
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
	
	public String getOut() {
		return out;
	}
	public void setOut(String out) {
		this.out = out;
	}
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	
	public IModel<Object> getResultModel() {
		return resultModel;
	}
	
	public void setResultModel(IModel<Object> resultModel) {
		this.resultModel = resultModel;
	}
	
	public Object getResult() {
		return resultModel!=null?resultModel.getObject():null;
	}
	
	public void setResult(Object object) {
		if(resultModel==null) resultModel = new StorageModel<Object>(object);
		else resultModel.setObject(object);
	}
	
	
	@Override
	public String toString() {
		return "ScriptHistoryItem [script=" + script + ", engine=" + engine + ", out=" + out + ", error=" + error + "]";
	}
	
}
