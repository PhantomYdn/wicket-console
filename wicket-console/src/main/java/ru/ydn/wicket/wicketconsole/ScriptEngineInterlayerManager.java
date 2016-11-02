package ru.ydn.wicket.wicketconsole;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScriptEngineInterlayerManager {

	public static final ScriptEngineInterlayerManager INSTANCE = new ScriptEngineInterlayerManager();
	
	Map<String,Class<? extends IScriptEngineInterlayer>> interlayers;
	
	private ScriptEngineInterlayerManager() {
		interlayers = new HashMap<String,Class<? extends IScriptEngineInterlayer>>();
		addInterlayer("JavaScript",JavaxScriptEngineInterlayer.class);
	}
	
	public void addInterlayer(String name,Class<? extends IScriptEngineInterlayer> class1){
		interlayers.put(name, class1);
	}

	public IScriptEngineInterlayer getByName(String name){
		try {
			IScriptEngineInterlayer newInterlayer = interlayers.get(name).newInstance();
			newInterlayer.setName(name);
			return newInterlayer;
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public List<String> getNames(){
		return new ArrayList<String>(interlayers.keySet());
	}

}
