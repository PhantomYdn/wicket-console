package ru.ydn.wicket.wicketconsole;

import java.io.Serializable;

public interface IScriptEngineInterlayerResult extends Serializable{
	
	String getOut();
	
	String getError();
	
	Object getReturnedObject();
	
	void onUpdate();
	
	public IScriptEngineInterlayerResultRenderer getRenderer();

	public void setRenderer(IScriptEngineInterlayerResultRenderer renderer);
}
