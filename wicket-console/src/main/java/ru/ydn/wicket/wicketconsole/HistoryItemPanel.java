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
	
	public HistoryItemPanel(String id, IModel<ScriptHistoryItem> model,final Component inpitField,final Component engineSelect)
	{
		super(id, model instanceof CompoundPropertyModel?model:new CompoundPropertyModel<ScriptHistoryItem>(model));
		add(engine = new MultiLineLabel("engine").add(new HideIfObjectIsEmptyBehavior()));
		add(script = new MultiLineLabel("script").add(new HideIfObjectIsEmptyBehavior()));
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
		if (renderer!=null){
			add(renderer.getOutView("out"));
			add(renderer.getErrorView("err"));
		}else{
			add(new MultiLineLabel("out").add(new HideIfObjectIsEmptyBehavior()));
			add(new MultiLineLabel("err").add(new HideIfObjectIsEmptyBehavior()));
		}
		
		add(new MultiLineLabel("returnObject").add(new HideIfObjectIsEmptyBehavior()));
		add(new MultiLineLabel("exception").add(new HideIfObjectIsEmptyBehavior()));

	}
	

	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		tag.append("class", "wc-item", " ");
	}
	
	

}
