package ru.ydn.wicket.wicketconsole;

import java.io.Serializable;

public interface IScriptEngineInterlayerResult {
	
	String getOut();
	
	String getError();
	
	Object getResult();
}
