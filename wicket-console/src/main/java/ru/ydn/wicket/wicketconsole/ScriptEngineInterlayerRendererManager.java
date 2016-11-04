package ru.ydn.wicket.wicketconsole;

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScriptEngineInterlayerRendererManager {

	public static final ScriptEngineInterlayerRendererManager INSTANCE = new ScriptEngineInterlayerRendererManager();
	private static final Logger LOG = LoggerFactory.getLogger(ScriptEngineInterlayerRendererManager.class);

	Map<Class<? extends IScriptEngineInterlayerResult>,IScriptEngineInterlayerResultRenderer> renderers;

	private ScriptEngineInterlayerRendererManager() {
		renderers = new HashMap<Class<? extends IScriptEngineInterlayerResult>,IScriptEngineInterlayerResultRenderer>();
		addRenderer(JavaxResultSimpleRenderer.class,JavaxScriptEngineInterlayerResult.class);
	}
	
	public void addRenderer(Class<? extends IScriptEngineInterlayerResultRenderer> rendererClass,Class<? extends IScriptEngineInterlayerResult> resultClass){
		try {
			renderers.put(resultClass, rendererClass.newInstance());
		} catch (Exception e) {
			LOG.error("Cannot make script engine interlayer renderer using class '"+rendererClass.getName()+"'",e);
		}
	}
	
	public IScriptEngineInterlayerResultRenderer getByResultObject(IScriptEngineInterlayerResult resultObject){
		return renderers.get(resultObject.getClass());
	}
	
	public Component getErrorView(String id, IModel<IScriptEngineInterlayerResult> resultObject){
		IScriptEngineInterlayerResultRenderer renderer = getByResultObject(resultObject.getObject());
		if (renderer!=null){
			return renderer.getErrorView(id, resultObject);
		}
		return null;
	}
	
	public Component getOutView(String id, IModel<IScriptEngineInterlayerResult> resultObject){
		IScriptEngineInterlayerResultRenderer renderer = getByResultObject(resultObject.getObject());
		if (renderer!=null){
			return renderer.getOutView(id, resultObject);
		}
		return null;
	}

}
