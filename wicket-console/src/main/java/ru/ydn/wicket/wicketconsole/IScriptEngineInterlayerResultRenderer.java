package ru.ydn.wicket.wicketconsole;

import org.apache.wicket.Component;

public interface IScriptEngineInterlayerResultRenderer {
	public Component getErrorView(String id);
	public Component getOutView(String id);
}
