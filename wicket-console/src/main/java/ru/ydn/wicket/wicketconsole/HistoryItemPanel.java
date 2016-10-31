package ru.ydn.wicket.wicketconsole;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.string.Strings;

public class HistoryItemPanel extends GenericPanel<ScriptHistoryItem>
{
	private Component script;
	private Component engine;
	
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

	public HistoryItemPanel(String id, IModel<ScriptHistoryItem> model,final Component inpitField,final Component engineSelect)
	{
		super(id, model instanceof CompoundPropertyModel?model:new CompoundPropertyModel<ScriptHistoryItem>(model));
		add(engine = new HideableLabel("engine"));
		add(script = new HideableLabel("script"));
		add(new HideableLabel("out"));
		add(new HideableLabel("err"));
		add(new HideableLabel("returnObject"));
		add(new HideableLabel("exception"));
		add(new AjaxLink("reuse"){
			@Override
			public void onClick(AjaxRequestTarget target) {
				inpitField.setDefaultModelObject(script.getDefaultModelObject());
				engineSelect.setDefaultModelObject(engine.getDefaultModelObject());
				target.add(engineSelect);
				target.add(inpitField);
			}
		});
	}

	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		tag.append("class", "wc-item", " ");
	}
	
	

}
