package ru.ydn.wicket.wicketconsole;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

public interface IScriptResultRenderer {
	public Component render(String id, IModel<Object> data);
}
