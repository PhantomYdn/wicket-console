package ru.ydn.wicket.wicketconsole;

import java.io.Serializable;

import org.apache.wicket.util.io.IClusterable;

public interface IScriptEngineInterlayerResult extends IClusterable {
	
	String getOut();
	
	String getError();
	
	Object getReturnedObject();
	
	void onUpdate();
	
	public IScriptEngineInterlayerResultRenderer getRenderer();

	public void setRenderer(IScriptEngineInterlayerResultRenderer renderer);
}
