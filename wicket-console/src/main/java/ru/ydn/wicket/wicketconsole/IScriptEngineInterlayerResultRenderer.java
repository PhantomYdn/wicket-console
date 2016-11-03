package ru.ydn.wicket.wicketconsole;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

public interface IScriptEngineInterlayerResultRenderer {
	public Component getErrorView(String id, IModel<IScriptEngineInterlayerResult> data);
	public Component getOutView(String id, IModel<IScriptEngineInterlayerResult> data);
}
