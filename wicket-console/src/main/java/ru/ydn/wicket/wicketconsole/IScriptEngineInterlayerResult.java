package ru.ydn.wicket.wicketconsole;

import org.apache.wicket.util.io.IClusterable;

public interface IScriptEngineInterlayerResult extends IClusterable {
	
	String getOut();
	
	String getError();
	
	Object getReturnedObject();
	
	void onUpdate();
}
