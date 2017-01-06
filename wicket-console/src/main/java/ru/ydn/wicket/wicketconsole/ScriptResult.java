package ru.ydn.wicket.wicketconsole;

import java.io.Serializable;

import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;

public final class ScriptResult implements Serializable, IDetachable
{
	private String engine;
	private String script;
	private String out;
	private String error;
	private IModel<?> resultModel;
	
	public ScriptResult(String engine, String script)
	{
		this.engine = engine;
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
	
	public IModel<?> getResultModel() {
		return resultModel;
	}
	
	public void setResultModel(IModel<?> resultModel) {
		this.resultModel = resultModel;
	}
	
	public Object getResult() {
		return resultModel!=null?resultModel.getObject():null;
	}
	
	@SuppressWarnings("unchecked")
	public <T> void setResult(T object) {
		if(resultModel==null) resultModel = new StorageModel<T>(object);
		else ((IModel<T>)resultModel).setObject(object);
	}
	
	
	@Override
	public String toString() {
		return "ScriptHistoryItem [script=" + script + ", engine=" + engine + ", out=" + out + ", error=" + error + "]";
	}

	@Override
	public void detach() {
		if(resultModel!=null) resultModel.detach();
	}
	
}
