package ru.ydn.wicket.wicketconsole;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.util.string.Strings;

public class HistoryItemPanel extends GenericPanel<ScriptHistoryItem>
{
	Component script;
	Component engine;
	
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
	protected void onInitialize() {
		super.onInitialize();
		
		ScriptHistoryItem obj =  getModelObject();
		IScriptEngineInterlayerResultRenderer renderer =  getModelObject().getResultObject().getRenderer();
		add(renderer.getOutView("out"));
		add(renderer.getErrorView("err"));
		
		add(new HideableLabel("returnObject"));
		add(new HideableLabel("exception"));

	}
	

	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		tag.append("class", "wc-item", " ");
	}
	
	

}
