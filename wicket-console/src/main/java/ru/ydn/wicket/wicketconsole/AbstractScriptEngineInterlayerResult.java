package ru.ydn.wicket.wicketconsole;

public abstract class AbstractScriptEngineInterlayerResult implements IScriptEngineInterlayerResult{

	private transient Object result;
	private String out;
	private String error;

	
	@Override
	public String getOut() {
		return out;
	}

	public void setOut(String out) {
		this.out = out;
	}

	@Override
	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}
	
	@Override
	public Object getReturnedObject() {
		return result;
	}

	public void setReturnedObject(Object result) {
		this.result = result;
	}

}
