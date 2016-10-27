package ru.ydn.wicket.wicketconsole;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScriptEngineInterlayerManager {

	public static final ScriptEngineInterlayerManager INSTANCE = new ScriptEngineInterlayerManager();

	Map<String,IScriptEngineInterlayer> interlayers;
	
	private ScriptEngineInterlayerManager() {
		interlayers = new HashMap<String,IScriptEngineInterlayer>();
		addInterlayer(new JavaxScriptEngineInterlayer("JavaScript","JavaScript"));
	}
	
	void addInterlayer(IScriptEngineInterlayer interlayer){
		interlayers.put(interlayer.getName(), interlayer);
	}
	
	public IScriptEngineInterlayer getByName(String name){
		return interlayers.get(name);
	}
	
	public List<String> getNames(){
		return new ArrayList<String>(interlayers.keySet());
	}

}
