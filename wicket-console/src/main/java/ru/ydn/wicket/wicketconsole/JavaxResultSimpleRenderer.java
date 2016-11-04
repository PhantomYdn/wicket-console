package ru.ydn.wicket.wicketconsole;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.string.Strings;

public class JavaxResultSimpleRenderer implements IScriptEngineInterlayerResultRenderer {
	
	public JavaxResultSimpleRenderer(){
	}

	@Override
	public Component getErrorView(String id,IModel<IScriptEngineInterlayerResult> data) {
		return new MultiLineLabel(id, new PropertyModel<String>(data, "error"))
						.add(HideIfObjectIsEmptyBehavior.INSTANCE);
	}

	@Override
	public Component getOutView(String id,IModel<IScriptEngineInterlayerResult> data) {
		return new MultiLineLabel(id, new PropertyModel<String>(data, "out"))
						.add(HideIfObjectIsEmptyBehavior.INSTANCE);
	}

}
