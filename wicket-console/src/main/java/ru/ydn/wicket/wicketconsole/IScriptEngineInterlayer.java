package ru.ydn.wicket.wicketconsole;

public interface IScriptEngineInterlayer {
	
	void setName(String name);

	String getName();
	
	IScriptEngineInterlayerResult eval(String command);

}
