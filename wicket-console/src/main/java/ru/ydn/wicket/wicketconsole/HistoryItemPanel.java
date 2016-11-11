package ru.ydn.wicket.wicketconsole;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.string.Strings;

import ru.ydn.wicket.wicketconsole.behavior.HideIfObjectIsEmptyBehavior;

public class HistoryItemPanel extends GenericPanel<ScriptResult>
{
	private Component script;
	private Component engine;
	
	public HistoryItemPanel(String id, IModel<ScriptResult> model,final Component inpitField,final Component engineSelect)
	{
		super(id, model instanceof CompoundPropertyModel?model:new CompoundPropertyModel<ScriptResult>(model));
		add(engine = new MultiLineLabel("engine").add(HideIfObjectIsEmptyBehavior.INSTANCE));
		add(script = new MultiLineLabel("script").add(HideIfObjectIsEmptyBehavior.INSTANCE));
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
		
		ScriptResult historyItem =  getModelObject();
		ScriptResultRendererManager renderer =  ScriptResultRendererManager.INSTANCE;
		add(new MultiLineLabel("out").add(HideIfObjectIsEmptyBehavior.INSTANCE));
		add(new MultiLineLabel("error").add(HideIfObjectIsEmptyBehavior.INSTANCE));
		add(renderer.render("result",historyItem.getResultModel()));

	}
	

	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		tag.append("class", "wc-item", " ");
	}
	
	

}
