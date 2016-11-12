package ru.ydn.wicket.wicketconsole;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.string.Strings;

import ru.ydn.wicket.wicketconsole.behavior.HideIfObjectIsEmptyBehavior;

public class DefaultScriptResultRenderer implements IScriptResultRenderer {
	
	public DefaultScriptResultRenderer(){
	}

	@Override
	public Component render(String id, IModel<?> data) {
		return new MultiLineLabel(id, data)
				.add(HideIfObjectIsEmptyBehavior.INSTANCE);
	}

}
