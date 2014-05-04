package ru.ydn.wicket.wicketconsole;

import java.io.Serializable;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.string.Strings;

public class HistoryItemPanel extends GenericPanel<ScriptHistoryItem>
{
	private static class HideableLabel extends Label
	{

		public HideableLabel(String id, IModel<?> model)
		{
			super(id, model);
		}

		public HideableLabel(String id, Serializable label)
		{
			super(id, label);
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

	public HistoryItemPanel(String id, IModel<ScriptHistoryItem> model)
	{
		super(id, model instanceof CompoundPropertyModel?model:new CompoundPropertyModel<ScriptHistoryItem>(model));
		add(new HideableLabel("script"));
		add(new HideableLabel("out"));
		add(new HideableLabel("err"));
		add(new HideableLabel("returnObject"));
		add(new HideableLabel("exception"));
	}

	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		tag.append("class", "wc-item", " ");
	}
	
	

}
