package ru.ydn.wicket.wicketconsole;

import java.io.Serializable;
import java.util.Map;

public interface IScriptContext extends Serializable{
	
	public Map<String,Object> getBindings();
	
}
