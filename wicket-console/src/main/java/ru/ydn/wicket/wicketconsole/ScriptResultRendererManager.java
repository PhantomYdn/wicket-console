package ru.ydn.wicket.wicketconsole;

import java.util.LinkedList;

import org.apache.wicket.Component;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.model.IModel;

public class ScriptResultRendererManager implements IScriptResultRenderer{
	
	public static final ScriptResultRendererManager INSTANCE = new ScriptResultRendererManager();
	
	private LinkedList<IScriptResultRenderer> renderers = new LinkedList<IScriptResultRenderer>();
	
	public ScriptResultRendererManager() {
		registerRenderer(new DefaultScriptResultRenderer());
	}
	
	public void registerRenderer(IScriptResultRenderer renderer) {
		renderers.push(renderer);
	}

	@Override
	public Component render(String id, IModel<Object> data) {
		for(IScriptResultRenderer renderer:renderers) {
			Component ret = renderer.render(id, data);
			if(ret!=null) return ret;
		}
		throw new WicketRuntimeException("Object "+data.getObject()+" can't be rendered");
	}


}
