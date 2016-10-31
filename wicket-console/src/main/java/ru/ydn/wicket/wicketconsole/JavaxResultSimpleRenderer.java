package ru.ydn.wicket.wicketconsole;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.string.Strings;

public class JavaxResultSimpleRenderer implements IScriptEngineInterlayerResultRenderer {
	
	private static class HideableLabel extends MultiLineLabel
	{

		public HideableLabel(String id, IModel<?> model)
		{
			super(id, model);
		}

		public HideableLabel(String id, String label)
		{
			super(id, label);
		}

		public HideableLabel(String id)
		{
			super(id);
		}

		@Override
		protected void onConfigure() {
			super.onConfigure();
			setVisible(!Strings.isEmpty(getDefaultModelObjectAsString()));
		}
		
	}
	JavaxScriptEngineInterlayerResult data;
	
	public JavaxResultSimpleRenderer(JavaxScriptEngineInterlayerResult data){
		this.data = data;
	}

	public Component getErrorView(String name) {
		return new HideableLabel(name, data.getError());
	}

	@Override
	public Component getOutView(String name) {
		return new HideableLabel(name, data.getOut());
	}

}
